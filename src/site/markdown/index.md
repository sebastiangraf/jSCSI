<a href="https://github.com/disy/jSCSI"><img style="position: absolute; top: 0; right: 0; border: 0;" src="https://s3.amazonaws.com/github/ribbons/forkme_right_green_007200.png" alt="Fork me on GitHub"/></a>

#Overview

jSCSI is the first Java iSCSI initiator continuously developed since 2006. Entirely written in Java, it allows easy access to iSCSI as block-accessing protocol from both sides, server(target) and client(initiator).

#Build status

jSCSI is guarded by Travis CI: [![Build Status](https://secure.travis-ci.org/disy/jSCSI.png)](http://travis-ci.org/disy/jSCSI)

##The iSCSI protocol

The iSCSI protocol defines how a client (iSCSI initiator) accesses a block device on a server (iSCSI target) over a TCP/IP network. It is inspired by the existing SCSI protocol used to access local hard drives or other devices in a block-oriented fashion. 
Being standardized in April 2004 with [RFC 3720](http://www.ietf.org/rfc/rfc3720.txt), it was quickly	adopted, not least because it is believed to offer a better price-performance ratio and fewer infrastructure changes than competing solutions such as fibre channel. 
Furthermore, recent research indicates that user-level iSCSI initiators can improve performance considerably.

##jSCSI - a plain Java iSCSI framework

jSCSI includes a Java iSCSI initiator and a Java iSCSI target. Besides an initiator and target implementation, jSCSI furthermore offers convenient methods to parse PDUs independently from the concrete appliance as server or client. Entirely written in Java, jSCSI offers easy possibilities to be extended while staying completely platform independent.

Both, intiator and target, are freely available over maven central-repo:

```xml

<dependency>
	<groupId>org.jscsi</groupId>
	<artifactId>target</artifactId>
	<version>2.5</version>
</dependency>
<dependency>
	<groupId>org.jscsi</groupId>
	<artifactId>initiator</artifactId>
	<version>2.5</version>
</dependency>
```

###jSCSI initiator

The jSCSI-initiator is represented by a library offering easy access to (nearly) any iSCSI target. The interface is leaned on common IO-interfaces making adapters quite easy. Leveraging from the easy utilization of multiple threads within Java, the initiator is able to work on Multi-Connection/Session as well as on Multi-Session basis. For more information, please refer to the initiator-bundle.

Based on its simple appliance as plain Java-library, our initiator is suited to act as a base for any low-level based operation. Examples of such applications include a storage-pool as well as a block-visualization. For more information, please refer to the intiatorExtensions-bundle.

###jSCSI target

The target at the moment enables users either to start it as a demon process storing all blocks in differend backends such as a simple RandomAccessFile or a JClouds-Backend. Abstraction for using other storages as well as a library-based handling instead of a utilization as demon are supported. For more information, please refer to the target-bundle.

Our target is suited to act as a base for further target-allocated applications like the SCSI-layer implementation from Cleversafe. For more information, please refer to the targetExtensions-bundle.

##Who worked on jSCSI?

jSCSI was created at the [Distributed Systems Group](http://www.disy.uni-konstanz.de/) from the [University of Konstanz](http://www.uni-konstanz.de/). jSCSI is licensed under the [BSD3-Clause Licence](http://www.opensource.org/licenses/BSD-3-Clause) offering easy ways to utilize the provided library. 
The project was started in 2006 by Marc Kramis and transferred in 2007 to Sebastian Graf. Since its beginning, jSCSI acts as a base for student projects. 

###Involved People

jSCSI is maintained currently by

* Sebastian Graf (Project Lead)

Concluded and adopted subprojects were:

* Andreas Rain, Test-cases
* Nuray Gürler, jSCSI websites
* Andreas Ergenzinger, jSCSI Target 1.0
* Patrice Brend'amour, jSCSI Initiator 2.0
* Markus Specht, jSCSI Target evaluation
* Bastian Lemke, jSCSI Storage Pool
* Volker Wildi, jSCSI Initiator 1.0
* Halldor Janetzko, Whiskas Block-Visualization

Within the switch of the hosting from [Sourceforge](http://sourceforge.net/projects/jscsi/) to [github](https://github.com/disy), jSCSI experiences impacts from the open-source community directly e.g.

* SCSI-layer; (thanks Cleversafe)
* Flexible disk-size; (thanks Stephen Davidson)
* Multiple target-handling within one instance; (thanks David L. Smith-Uchida)
* Working with Mac OS X Initiator (Thanks Rajesh Sharma)
