# Introduction

iSCSI defines a server-client protocol for block-based data transmission whereas the client is denoted as initiator while the server is called target. This bundle represents the client denoted initiator for the rest of the documentation.

The most recent description of the framework is a TechReport from 2009 accessible over http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-84511 here.

## Block-based access from native Java-Libraries

The interface of the jSCSI initiator is easy similar to common byte-based interfaces well known from the java.io-package. The configuration can take place either over Java directly or over defined XML-files validated against given schemas. After connecting to any target, the initiator is able to read/write chunks of bytes directly out of Java to any target while offering other convenience methods fully compliant to the http://www.ietf.org/rfc/rfc3720.txt iSCSI RFC 3720.

## Fast and robust

The jSCSI-initiator represents a plain-java based adapter for accessing iSCSI-targets directly out of your Java environment. By leveraging from the concurrency-features of Java 5+, jSCSI provides easy, fail-safe and transparent Multi-Connection per Session as well as Multi-Session functionality as denoted in the figure below:
!{Multiaccess}(images/multiaccess.graffle).

As clearly visible, jSCSI offers the ability to either open multiple sessions to one target as well as to handle multiple connections per session. It is important to note, that the multi-threaded ability of jSCSI is only utilized after login when accessing any target for read-/write-purposes.

## Own needs, own requirements

jSCSI was created at the http://www.uni-konstanz.de University of Konstanz at the http://www.informatik.uni-konstanz.de/arbeitsgruppen/disy/ Distributed Systems Group}Distributed Systems Group out of the necessity to provide an architecture for storing blocks directly out of Java without any hassle of filesystems.

The jSCSI-initiator is hosted with jSCSI https://github.com/disy/jSCSI under the BSD License and guarded by Travis-CI. It can be found under the bundle jscsi-initiator.