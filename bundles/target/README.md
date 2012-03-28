jSCSI - Target implementation
=========

This bundle represents the target implementation of jSCSI. 

Installation on different systems
---------

Note that new storage media needs to be formatted with a FileSystem that the client system's understand.

MS Windows 7 (& MS 2008?)
-------------------------

1. Connect to Drive
  * Control Panel, search for SCSI
  * Discovery -> Discover Portal
  * Server IP address.

2. Format Drive
  * Computer Management -> Storage -> Disk Management
  * Right click on Drive, then Select Format, select Drive Letter, File System, etc.
  Drive should format and automount.


Changelog
=========

* 2012/03/27: Added file creation by target (Thanks to Stephen Davidson)
* 2012/02/28: Move to github entirely
* 2012/01/01: Move to github for open source release


