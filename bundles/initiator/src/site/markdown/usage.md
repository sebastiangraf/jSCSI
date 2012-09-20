<a href="https://github.com/disy/jSCSI"><img style="position: absolute; top: 0; right: 0; border: 0;" src="https://s3.amazonaws.com/github/ribbons/forkme_right_green_007200.png" alt="Fork me on GitHub"/></a>

# Description

The initiator is entirely written in Java. Based on its use-case as library, it has no functionality to run as a demon process but the ability to be bound as dependency in any java-based project.

It is easily available over maven provided over central-repo:

```xml

<dependency>
	<groupId>org.jscsi</groupId>
	<artifactId>initiator</artifactId>
	<version>2.3</version>
</dependency>
```

### Configuration

The configuration of the initiator takes place over the class org.jscsi.initiator.Configuration storing global configurations as well as session-specific ones. Instead of building a suitable configuration by hand, a xsd is provided under src/main/resources/jscsi.xsd.
Please note that every own xml used for configuring the jSCSI-initiator must satisfy the denoted xsd. One example of such an xml is given at the end of the document.

A detailed description of the supported settings is given in the classes org.jscsi.parser.datasegment.OperationalTextKey in the commons-bundle. If the config is created by hand, the same settings than denoted by the xsd must be set. Otherwise the initiator may not be fully functional (even if no exception might be thrown).

### Handling the Initiator

The main class org.jscsi.initiator.Initiator takes Configuration, created with the help of such an XML, as argument while creation. However, no login is performed while the initiator-object is created since multiple sessions are accessible over one object.
The moment the org.jscsi.initiator.Initiator#getSession(String)-method is called, the login is performed. Afterwards, read-/write-methods are able if the access was successful. Otherwise an error is thrown.

### Reading and Writing

The main class org.jscsi.initiator.Initiator also offers the ability to perform read- and write-operations. Fully capable to support multithreaded-environments, jSCSI offers a method to for non-blocking read-/write-operations (returning Future-objects) as well as to block directly within each read-/write-access.

### Shutting down the initiator

The initiator should cancel all session before losing its reference. This can be done over org.jscsi.initiator.connection.Session#close().

Example of a configuration-xml

			
	<?xml version="1.0" encoding="UTF-8" ?>
	<configuration xmlns="http://www.jscsi.org/2006-09"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="http://www.jscsi.org/2006-09 jscsi.xsd">

		<global>
			<AuthMethod>None</AuthMethod>
			<DataDigest>None</DataDigest>
			<DataPDUInOrder>Yes</DataPDUInOrder>
			<DataSequenceInOrder>Yes</DataSequenceInOrder>
			<DefaultTime2Retain>20</DefaultTime2Retain>
			<DefaultTime2Wait>2</DefaultTime2Wait>
			<ErrorRecoveryLevel>0</ErrorRecoveryLevel>
			<FirstBurstLength>65536</FirstBurstLength>
			<HeaderDigest>None</HeaderDigest>
			<IFMarker>No</IFMarker>
			<IFMarkInt>2048</IFMarkInt>
			<ImmediateData>Yes</ImmediateData>
			<InitialR2T>Yes</InitialR2T>
			<InitiatorAlias>InitiatorAlias</InitiatorAlias>
			<InitiatorName>InitiatorName</InitiatorName>
			<MaxBurstLength>262144</MaxBurstLength>
			<MaxConnections>1</MaxConnections>
			<MaxOutstandingR2T>1</MaxOutstandingR2T>
			<MaxRecvDataSegmentLength>8192</MaxRecvDataSegmentLength>
			<OFMarker>No</OFMarker>
			<OFMarkInt>2048</OFMarkInt>
			<SessionType>Normal</SessionType>
		</global>


		<!-- "bench4.disy.inf.uni-konstanz.de" (134.34.165.156) is a openSolaris running an Sun  Solaris iSCSI Target with 1 disk (file)-->
		<!-- only purpose is benching. It is used by many other projects, so you need to call the admin to get a timeslot for the jSCSI initiator project-->
		<!-- <target id="testing-bench4" address="134.34.165.156" port="3260">
				<ImmediateData>Yes</ImmediateData>
				<InitiatorName>IdefixInitiator</InitiatorName>
				<TargetName>iqn.1986-03.com.sun:02:c0cb806a-afa0-62d1-877c-9f8df7543824.sandbox</TargetName>
		</target> -->


		<!-- "xen2.disy.inf.uni-konstanz.de" (134.34.165.133) is a vmlinux running an iSCSI Enterprise Target with 2 disks (files)-->
		<!-- only purpose is testing the jSCSI initiator project, should be available 24/7 -->
		<!-- multiple user tests at the same time can be possible, so don't wonder if data on disks changes from time to time -->
		<target id="testing-xen2-disk1" address="192.168.0.134" port="3260">
			<ImmediateData>No</ImmediateData>
			<InitiatorName>IdefixInitiator</InitiatorName>
			<TargetName>iqn.2007-10.de.uni-konstanz.inf.disy.xen2:disk1</TargetName>
		</target>

		<target id="testing-xen2-disk1" address="217.119.233.170" port="3260">
			<ImmediateData>No</ImmediateData>
			<InitiatorName>IdefixInitiator</InitiatorName>
			<TargetName>iqn.2007-10.de.uni-konstanz.inf.disy.xen2:disk1</TargetName>
		</target>

		<!-- "xen2.disy.inf.uni-konstanz.de" (134.34.165.133) is a vmlinux running an iSCSI Enterprise Target with 2 disks (files)-->
		<!-- only purpose is testing the jSCSI initiator project, should be available 24/7 -->
		<!-- multiple user tests at the same time can be possible, so don't wonder if data on disks changes from time to time-->
		<target id="testing-xen2-disk2" address="192.168.0.134" port="3260">
			<ImmediateData>No</ImmediateData>
			<InitiatorName>IdefixInitiator</InitiatorName>
			<TargetName>iqn.2007-10.de.uni-konstanz.inf.disy.xen2:disk2</TargetName>
		</target>

		<!-- <target id="testing-xen1-disk3" address="134.34.165.132" port="3261">
			<ImmediateData>No</ImmediateData>
			<InitiatorName>IdefixInitiator</InitiatorName>
			<TargetName>iqn.2007-10.de.uni-konstanz.inf.disy.xen1:disk3</TargetName>
			<MaxConnections>8</MaxConnections>
			<IFMarkInt>2048~4048</IFMarkInt>
			<OFMarkInt>2048~4048</OFMarkInt>
		</target>

		<target id="xen1-disk1" address="134.34.165.132" port="3260">
			<ImmediateData>No</ImmediateData>
			<InitiatorName>IdefixInitiator</InitiatorName>
			<MaxConnections>2</MaxConnections>
			<TargetName>iqn.2007-10.de.uni-konstanz.inf.disy.xen1:disk1</TargetName>
		</target>

		<target id="xen1-disk2" address="134.34.165.132" port="3260">
			<ImmediateData>No</ImmediateData>
			<InitiatorName>IdefixInitiator</InitiatorName>
			<MaxConnections>2</MaxConnections>
			<TargetName>iqn.2007-10.de.uni-konstanz.inf.disy.xen1:disk2</TargetName>
		</target> -->



	</configuration>
	
			