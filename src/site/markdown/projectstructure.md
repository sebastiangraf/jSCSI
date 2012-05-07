<a href="https://github.com/disy/jSCSI"><img style="position: absolute; top: 0; right: 0; border: 0;" src="https://s3.amazonaws.com/github/ribbons/forkme_right_green_007200.png" alt="Fork me on GitHub"/></a>

# Overall Structure

The modularity of jSCSI is represented by its module structure. Fully mavenized, the functionality are split into different modules depending on each other.

The denoted figure represents the project structure of jSCSI:
![project schema](images/projectschema.svg)

* All common functionalities for parsing are handles over a commons-bundle. These functionalities include the parsing of PDUs as well as common exceptions. The centralized handling of such functionalities guarantee a similar behavior of the target and the initiator including application which depend on those.
* The initiator is handled over a depending module. Its applicability as library depending on the commons-bundle offers easy access to (nearly) any target instance. All functionality necessary to work with iSCSI-targets is provided by this bundle.
* Examples of utilization of such functionalities is provided by the initiator extensions: At the moment the initiator offer ways to implement a storage pool similar to those provided via ZFS or LVM. Another example is an eclipse-plugin for visualization of blocks.
* Besides the initiator and its implementing services, jSCSI offers a target as well. The current implementation works as demon thread as well as library leveraging from the common functionalities from the commons-bundle.
* The target itself has the ability to act as a base for further implementations similar to the initiator. At the moment, the target extensions include (only) a SCSI-layer provided by Cleversafe.

The presented overall structure is reflected by the maven bundles. Detailed information about the utilization, the technical implementation as well as documentation is provided by the websites of the bundles initiator, target and commons.

Please note, that parts of the project are neither supported nor further developed. Since jSCSI acts as a base for student projects as well as for other projects, the following projects are concluded and frozen.

## Storage Pool

The design and implementation of the storage pool was a bachelors' project by Bastian Lemke. The idea was to implement a jSCSI-based layer for combining targets similar to the volume manager provided by ZFS or LVM as shown in the following figure. ![pool tree ](images/pool-tree.svg)

A detailed description is available as [bachelor thesis](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-58078) of Bastian Lemke (unfortunately in German only).

### Status

The storage still works with the newest version of the initiator but is not maintained any more.

## Whiskas Block Visualization

Another application for the initiator is the visualization of blocks. Resulting in an Eclipse plugin, block access pattern are visualized like denoted in the following figure. ![whiskas plugin](images/fullscreen2.png)

A detailed description is available as [InfoVis Poster](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-69096).

### Status

The plugin is included in the source tree although it is not maintained any more. Since jSCSI as well as the eclipse-framework experienced major changes since 2007, the plugin is currently not functional.

## SCSI-Layer

Since jSCSI acted as component within the Dispersed Storage from Cleversafe, Clefersafe contributed a SCSI-layer to jSCSI. This layer maps SCSI-commands to Java offering native support for SCSI-devices. More information about the Dispersed Storage can be found under the website of the [WSJ Cleversafe ](http://online.wsj.com/article/SB122227003788371453.html) won with their approach.

### Status

The SCSI-layer stored as target extension is due to licensing issues not bound into our target neither supported as well. Provided by Cleversafe, we host this contribution even though no utilization is done by our framework.