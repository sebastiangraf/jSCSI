<a href="https://github.com/disy/jSCSI"><img style="position: absolute; top: 0; right: 0; border: 0;" src="https://s3.amazonaws.com/github/ribbons/forkme_right_green_007200.png" alt="Fork me on GitHub"/></a>

# Description

The target is entirely written in Java. Based on its use-case as stand-alone process, it offers the ability to be started in an own process.
It is easily available over maven provided over central-repo:

```xml

<dependency>
	<groupId>org.jscsi</groupId>
	<artifactId>target</artifactId>
	<version>2.3</version>
</dependency>
```


### Configuration

The configuration of the initiator takes place over the class *org.jscsi.target.Configuration* storing global configurations as well as target-specific ones. Instead of building a suitable configuration by hand, a xsd is provided under *src/main/resources/jscsi.xsd*.
Please note that every own xml used for configuring the jSCSI-target must satisfy the denoted xsd. One example of such an xml is given at the end of the document.

A detailed description of the supported settings is given in the class *org.jscsi.parser.datasegment.OperationalTextKey* in the commons-bundle. If the config is created by hand, the same settings than denoted by the xsd must be set. Otherwise the target may not be fully functional (even if no exception might be thrown). In all cases, the Configuration must be initialized properly to get the target running

### Handling the Target

The main class *org.jscsi.target.TargetServer* refers to a Configuration, created with the help of such an XML, while running. At the moment (April 2012), the target is not supporting any authentication or authorization
If the storage, referring in the Configuration, is not existing, a creation process must triggered implicitly by the implementing StorageModule. The login-process is performed afterwards.

### Reading and Writing

The target listens on the port denoted in the Configuration an maps any request to the registered StorageModules. The main functionalities for a StorageModule is read, write and close. Fully capable to support multithreaded-environments, jSCSI offers a method to for non-blocking read-/write-operations (returning Future-objects) as well as to block directly within each read-/write-access.

### Example of initialization

The fastest way to get the jSCSI-target on is the writing of the denote configuration-xml and giving the path of the XML to the TargetServer as only parameter. If no parameter is given as parameter, the standard configuration from *target/src/main/resources/jscsi-target.xml* is taken.

### Example of a configuration-xml

See below an example of the target-config. Note, that many settings are negotiated between initiator and target and therefore not set by a configuration.

			
	<configuration xmlns="http://www.jscsi.org/2010-04"
	    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	    xsi:schemaLocation="http://www.jscsi.org/2010-04 jscsi-target.xsd">

	    <TargetList>
	        <Target>
	            <TargetName>iqn.2010-04.local-test:disk-1</TargetName>
	            <TargetAlias>jSCSI Target</TargetAlias>
	            <SyncFileStorage>
	                <Path>/tmp/storage1.dat</Path>
	                <NoCreate />
	                <!-- <Create size="0.75"/>-->>
	            </SyncFileStorage>
	        </Target>
	        <Target>
	            <TargetName>iqn.2010-04.local-test:disk-2</TargetName>
	            <TargetAlias>jSCSI Target</TargetAlias>
	            <AsyncFileStorage>
	                <Path>/tmp/storage2.dat</Path>
	                <NoCreate />
	                <!-- <Create size="0.75"/>-->
	            </AsyncFileStorage>
	        </Target>
	    </TargetList>
	    <GlobalConfig>
	        <AllowSloppyNegotiation>true</AllowSloppyNegotiation>
	        <Port>3260</Port>
	    </GlobalConfig>
	</configuration>
	
			