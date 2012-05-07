<a href="https://github.com/disy/jSCSI"><img style="position: absolute; top: 0; right: 0; border: 0;" src="https://s3.amazonaws.com/github/ribbons/forkme_right_green_007200.png" alt="Fork me on GitHub"/></a>

# Examples

The following examples use the configuration as shown on the /usage.html usage usage page especially related to the target-names.

Note to import the dependency for the jSCSI initiator and the snapshot-repo of sonatype in your pom.
			
	<dependencies>>
		...
		<dependency>
			<groupId>org.jscsi</groupId>
			<artifactId>initiator</artifactId>
			<version>2.2-SNAPSHOT</version>
		</dependency>
	</dependencies>
	...
	<repositories>
		...
		<repository>
			<id>sonatype-nexus-snapshots</id>
			<url>https://oss.sonatype.org/content/repositories/snapshots/</url>
		</repository>
	</repositories>
	
			
Example 1

In this example, a simple login and logout is performed. If any failure occurs within the access to the target (more concise if testing-xen2-disk1 is neither loaded nor available), an exception is thrown.

	public class SimpleLoginLogout {

		public static void main(final String[] args) throws NoSuchSessionException, TaskExecutionException,ConfigurationException {
			//init of the target
			String target = "testing-xen2-disk1";
			Initiator initiator = new Initiator(Configuration.create());
			//creating session, performing login on target
			initiator.createSession(target);
			//closing the session
			initiator.closeSession(target);
		}
	}
			

## Example 2

The following example denotes a normal read-/write-operation including a check of the data.
		
	public class SingleThreadedReadWrite {

		public static void main(final String[] args) throws NoSuchSessionException, TaskExecutionException, ConfigurationException {
			//init of test structures
			int numBlocks = 50;
			int address = 12345;
			ByteBuffer writeData = ByteBuffer.allocate(512 * numBlocks);
			ByteBuffer readData = ByteBuffer.allocate(512 * numBlocks);
			Random random = new Random(System.currentTimeMillis());
			random.nextBytes(writeData.array());

			//init of initiator and the session
			String target = "testing-xen2-disk1";
			Initiator initiator = new Initiator(Configuration.create());
			initiator.createSession(target);
		
			//writing the data single threaded
			initiator.write(target, writeData, address,writeData.capacity());

			//reading the data single threaded
			initiator.read(target, readData, address, readData.capacity());

			//closing the session
			initiator.closeSession(target);
		
			//correctness check
			if(!Arrays.equals(writeData.array(),readData.array())){
				throw new IllegalStateException("Data read must be equal to the data written");
			}
		}
	}
			
## Example 3

Extending the former example, the read and write has the ability to occur within non-blocking operations making sense especially when working within multiple targets.
		
	public class MultiThreadedReadWrite {

		public static void main(final String[] args) throws NoSuchSessionException, TaskExecutionException, ConfigurationException, InterruptedException, ExecutionException {
			//init of test structures
			int numBlocks = 50;
			int address = 12345;
			final ByteBuffer writeData1 = ByteBuffer.allocate(512 * numBlocks);
			final ByteBuffer readData1 = ByteBuffer.allocate(512 * numBlocks);
			final ByteBuffer writeData2 = ByteBuffer.allocate(512 * numBlocks);
			final ByteBuffer readData2 = ByteBuffer.allocate(512 * numBlocks);
			Random random = new Random(System.currentTimeMillis());
			random.nextBytes(writeData1.array());
			random.nextBytes(writeData2.array());
					
			//init of initiator and the session
			String target1 = "testing-xen2-disk1";
			String target2 = "testing-xen2-disk2";
			Initiator initiator = new Initiator(Configuration.create());
			initiator.createSession(target1);
			initiator.createSession(target2);

			//writing the first target multithreaded
			final Future<Void> write1 = initiator.multiThreadedWrite(target1, writeData1, address, writeData1.capacity());
			//writing the second target multithreaded
			final Future<Void> write2 = initiator.multiThreadedWrite(target2, writeData2, address, writeData2.capacity());
						
			//Blocking until writes are concluded
			write1.get();
			write2.get();
					
			//Getting the data from the first target multithreaded
			final Future<Void> read1 = initiator.multiThreadedRead(target1, readData1, address, readData1.capacity());
			//Getting the data from the second target multithreaded
			final Future<Void> read2 = initiator.multiThreadedRead(target2, readData2, address, readData2.capacity());
					
			//Blocking until reads are concluded
			read1.get();
			read2.get();
					
			//closing the targets
			initiator.closeSession(target1);
			initiator.closeSession(target2);
		
			//correctness check
			if(!Arrays.equals(writeData1.array(),readData1.array()) || !Arrays.equals(writeData2.array(),readData2.array())){
				throw new IllegalStateException("Data read must be equal to the data written");
			}
		
		}
	}
			