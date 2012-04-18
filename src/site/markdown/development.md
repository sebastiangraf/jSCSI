# Development

## The Device interface

The first release of jSCSI provides a simple interface for a device, i.e., Device:



A Device is implemented with the following semantics: 
Multiple threads can concurrently call the read(...), write(...), and getX() methods. Each method call of one thread is executed synchronously. 
Operation queueing and reordering is the task of the device implementation whereas caching is the responsibility of the upper layers.

All the Devices are fully stackable, this means that they can be combined arbitrarily:

Currently, following Devices exists:

### JSCSIDevice

Represents a single iSCSI initiator. It can be connected to exactly one target defined in the jSCSI configuration. An example for a suitable configuration is:

	
		
				

					
						None
						None
						Yes
						Yes
						20
						2
						0
						65536
						None
						No
						2048
						Yes
						Yes
						InitiatorAlias
						InitiatorName
						262144
						1
						1
						8192
						No
						2048
						Normal
					

					
						No
						IdefixInitiator
						iqn.2007-10.de.uni-konstanz.inf.disy.xen2:disk1
						
					
			
			device = new JSCSIDevice(disk1);
		
ATTENTION: before a device can be used, it has to be opened:

			device.open();
		
### Raid1Device

Implements a RAID 1 (Mirroring) over an arbitrary number of other Devices. 
The Devices to mirror have to be handed over in the constructor:

			device1 = new JSCSIDevice(disk1);
				device2 = new JSCSIDevice(disk2);
				device = new Raid1Device(new Device[] {device1, device2});
		
### Raid0Device

Stripes the data over all available Devices:



By default, the data is distributed over the Devices in 8KB chunks. Other chunk sizes can be set in the constructor (chunk size in bytes):

			device = new Raid0Device(new Device[] {...}, 4096);
		
WhiskasDevice

Interface for the Whiskas Eclipse Plugin.

PrefetchDevice / WriteBufferDevice

This two Devices implements a simple prefetcher and a simple write buffer. They only exist for testing purposes... 
The WriteBufferDevice has an additional flush() method.

DummyDevice

Also created for testing purposes. Simulates reads and writes and does not store anything anywhere...