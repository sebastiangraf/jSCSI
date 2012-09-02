<a href="https://github.com/disy/jSCSI"><img style="position: absolute; top: 0; right: 0; border: 0;" src="https://s3.amazonaws.com/github/ribbons/forkme_right_green_007200.png" alt="Fork me on GitHub"/></a>

# Introduction

iSCSI defines a server-client protocol for block-based data transmission whereas the client is denoted as initiator while the server is called target. This bundle represents the client denoted initiator for the rest of the documentation.

The most recent description of the framework is a [TechReport](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-84511).

## Block-based access from native Java-Libraries

The interface of the jSCSI initiator is easy similar to common byte-based interfaces well known from the java.io-package. The configuration can take place either over Java directly or over defined XML-files validated against given schemas. After connecting to any target, the initiator is able to read/write chunks of bytes directly out of Java to any target while offering other convenience methods fully compliant to the [iSCSI RFC 3720](http://www.ietf.org/rfc/rfc3720.txt).

## Fast and robust

The jSCSI-initiator represents a plain-java based adapter for accessing iSCSI-targets directly out of your Java environment. By leveraging from the concurrency-features of Java 5+, jSCSI provides easy, fail-safe and transparent Multi-Connection per Session as well as Multi-Session functionality as denoted in the figure below:

![Multiaccess](images/multiaccess.svg)

As clearly visible, jSCSI offers the ability to either open multiple sessions to one target as well as to handle multiple connections per session. It is important to note, that the multi-threaded ability of jSCSI is only utilized after login when accessing any target for read-/write-purposes.

## Working with different targets

The jSCSI-initiator was tested with the [Linux ISCSI Enterprise Target](http://iscsitarget.sourceforge.net/) as well as with the jSCSI-target. A Wireshark pcab with the IET can be found [here](jscsiInitWithIET.pcab) whereas a Wireshark pcab with our own target can be found [here](jscsiInitWithJSCSI.pcab).

## Own needs, own requirements

jSCSI was created at the [University of Konstanz](http://www.uni-konstanz.de) at the [Distributed Systems Group](http://www.disy.uni-konstanz.de/) out of the necessity to provide an architecture for storing blocks directly out of Java without any hassle of filesystems.

The jSCSI-initiator is hosted with [github](https://github.com/disy/jSCSI) under the [BSD License](http://www.opensource.org/licenses/BSD-3-Clause) and guarded by Travis-CI: [![Build Status](https://secure.travis-ci.org/disy/jSCSI.png)](http://travis-ci.org/disy/jSCSI)