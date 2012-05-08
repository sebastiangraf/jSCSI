jSCSI - A Java iSCSI Framework
=============

jSCSI is a feature-complete iSCSI implementation in Java only.
Platform-independent and fast, jSCSI represents a premium example how low-level protocols can be pushed to higher levels.
jSCSI contains a server (target), a client (initiator) and common classes to work with the protocol.

Content
-------

* README					this readme file
* LICENSE	 				license file
* parent					parent pom with global settings
* bundles					bundles
* scripts					bash scripts for syncing against disy-internal repo.
* pom.xml					Simple pom (yes we do use Maven)

[![Build Status](https://secure.travis-ci.org/disy/jSCSI.png)](http://travis-ci.org/disy/jSCSI)

License
-------

This work is released in the public domain under the BSD 3-clause license

Further information
-------

The project is currently under refactoring, the documentation is accessible under http://jscsi.org (pointing to http://disy.github.com/jscsi/) and a mailinglist has been set up:
https://mailman.uni-konstanz.de/mailman/listinfo/jscsi

###Publications

* A TechReport describes the second iteration of the framework: [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-84511)
* The framework was presented at the Jazoon '07 as work in progress: [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-84424)
* jSCSI acted as backend for a block visualization presented at the InfoVis 2006: [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-69096)

###Concluded Thesis

* Target 1.0 (english): [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-175134)
* Initiator 2.0 (german only): [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-130096)
* Storage Pool (german only): [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-58078)
* Initiator 1.0 (english): TO FOLLOW

Any questions, just contact sebastian.graf AT uni-konstanz.de

Involved People
-------

jSCSI is maintained by:

* Sebastian Graf (Current Project Lead)
* Nuray Gürler (Websites Refactoring)
* Andreas Rain (Testing)

Former people include:

* Andreas Ergenzinger (jSCSI 2.0, target)
* Patrice Brend'amour (jSCSI 2.0, initiator)
* Marcus Specht (jSCSI target evaluation)
* Halddor Janetzko (Whiskas Block Visualization)
* Marc Kramis (Project Lead until 2007)
* Bastian Lemke (Storage Pool)
* Volker Wildi (jSCSI 1.0, initiator)