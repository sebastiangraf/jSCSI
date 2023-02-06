# jSCSI - A Java iSCSI Framework

jSCSI is a feature-complete iSCSI implementation in Java only.
Platform-independent and fast, jSCSI represents a premium example how low-level protocols can be pushed to higher levels.
jSCSI contains a server (target), a client (initiator) and common classes to work with the protocol.

[![Build Status](https://secure.travis-ci.org/sebastiangraf/jSCSI.png)](http://travis-ci.org/sebastiangraf/jSCSI)

## Using jSCSI

* Get the latest jar over Github or Maven

```xml
<dependency>
	<groupId>org.jscsi</groupId>
	<artifactId>target</artifactId>
	<version>2.5.2</version>
</dependency>
<dependency>
	<groupId>org.jscsi</groupId>
	<artifactId>initiator</artifactId>
	<version>2.5.2</version>
</dependency>
```

* Use the target and/or the initiator-interfaces in your project

Note that the target is capable to run on its own while the initiator can only be utilized as library.
To run the target, please execute:
```
mvn SCHEMA.xsd CONFIG.xml
``` 
The schema and an example config are accessible as download as well and included under bundles/target/src/main/resources .

For further documentation and as an example, please refer to the examples in the initiator- and target-module.

* Run the target as standalone executable jar

  1. Put the target configuration XML file in a sub-folder named ```config```.
  See also ```bundles/target/src/main/resources/jscsi-target.xml```
  2. Put the log configuration XML file in the ```config``` sub-folder.
  See also ```bundles/target/src/main/resources/logback.xml```
  3. Put commons-{version}.jar, target-{version}.jar and their dependencies jar in a sub-folder named ```lib```.
  4. Run
```bash
java                                                    \
     --class-path "lib/*"                               \
     -Dlogback.configurationFile=config/logback.xml     \
     org.jscsi.target.TargetServer                      \
     config/jscsi-target.xml                            \
     > log/console.log                                  \
     2>&1                                               \
&
```

## Content

* README: this readme file
* LICENSE: license file
* bundles: bundles containing the projects
* pom.xml: Simple pom (yes we use Maven)

## Build
* jar only:
```bash
mvn -Dmaven.test.skip=true -Dmaven.source.skip=true -Dmaven.javadoc.skip=true clean package
```
* jar, javadoc and sources:
```bash
mvn -Dmaven.test.skip=true -Pdoclint-java8-disable clean package
```

## License

This work is released in the public domain under the BSD 3-clause license

## Further information

The project is currently under refactoring, the documentation is accessible under http://jscsi.org (pointing to http://disy.github.com/jscsi/) and a mailinglist has been set up:
https://mailman.uni-konstanz.de/mailman/listinfo/jscsi

## Publications

* A TechReport describes the second iteration of the framework: [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-84511)
* The framework was presented at the Jazoon '07 as work in progress: [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-84424)
* jSCSI acted as backend for a block visualization presented at the InfoVis 2006: [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-69096)

## Concluded Thesis

* Target 1.0 (english): TO FOLLOW
* Initiator 2.0 (german only): [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-130096)
* Storage Pool (german only): [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-58078)
* Initiator 1.0 (english): [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-175134)

Any questions, just contact sebastian.graf AT uni-konstanz.de

## Involved People

jSCSI is maintained by:

* Sebastian Graf (Current Project Lead)

Former people include:

* Andreas Rain (Testing)
* Nuray Gürler (Websites Refactoring)
* Andreas Ergenzinger (jSCSI 2.0, target)
* Patrice Brend'amour (jSCSI 2.0, initiator)
* Marcus Specht (jSCSI target evaluation)
* Halddor Janetzko (Whiskas Block Visualization)
* Marc Kramis (Project Lead until 2007)
* Bastian Lemke (Storage Pool)
* Volker Wildi (jSCSI 1.0, initiator)
