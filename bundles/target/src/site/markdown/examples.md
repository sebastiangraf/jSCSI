<a href="https://github.com/disy/jSCSI"><img style="position: absolute; top: 0; right: 0; border: 0;" src="https://s3.amazonaws.com/github/ribbons/forkme_right_green_007200.png" alt="Fork me on GitHub"/></a>

# Examples

The following examples use the configuration as shown on the /usage.html usage and shows the abilities of the target.

Note to import the dependency for the jSCSI target and the snapshot-repo of sonatype in your pom.

	<dependencies>>
		...
		<dependency>
			<groupId>org.jscsi</groupId>
			<artifactId>target</artifactId>
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
	
### Example 1

In this example, the target is simple started in an own java-process.
		
	java -jar target-2.2-SNAPSHOT-jar-with-dependencies.jar [CONFIG.XML]
			
Here, the target runs as own Java-Process taking an optional parameter, the configuration-xml. If no configuration is given, the configuration from the /usage.html usage site is taken.
The targets runs until its process is stopped. Note that no check against existing sessions or connections is performed.

### Example 2

The following example shows a start of the target in the existing process with an own jscsi-config.

	public class MethodStart {

		public static void main(final String[] args) throws Exception{
			//Getting the config path
			final File configFile = CONFIGPATH;
			//Creating the Configuration
			final Configuration config = Configuration.create(Configuration.CONFIGURATION_SCHEMA_FILE, configFile);
			//Starting the Target
			final TargetServer target = new TargetServer(config);
			target.call();
		}
	}

			
			

### Example 3

Extending the former example, the target is started in a callable leveraging from the easy treatment of threads in Java.
		
	public class CallableStart {

		public static void main(final String[] args) throws SAXException, ParserConfigurationException, IOException  {
			//Getting the config path
			final File configFile = Configuration.CONFIGURATION_CONFIG_FILE;
			//Creating the Configuration
			final Configuration config = Configuration.create(Configuration.CONFIGURATION_SCHEMA_FILE, configFile);
			//Starting the Target
			final TargetServer target = new TargetServer(config);
		
			//Getting an Executor
			ExecutorService threadPool = Executors.newSingleThreadExecutor();
			//Starting the target
			threadPool.submit(target);
		}
	}
			