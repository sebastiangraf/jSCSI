jSCSI - Target implementation
=========

This bundle represents the target implementation of jSCSI. Before start, it is highly recommend to create a file by hand e.g. with 


```
mkfile 1g storage.dat
```

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

* 2012/04/12: Added support for multiple targets within one process (Thanks to David Smith-Uchida)
* 2012/03/27: Added file creation by target (Thanks to Stephen Davidson)
* 2012/02/28: Move to github entirely
* 2012/01/01: Move to github for open source release


Errors
===========

```
Mar 15, 2013 4:31:26 PM org.jclouds.logging.jdk.JDKLogger logError
SEVERE: error after writing 335872/524288 bytes to https://grave9283746.s3-eu-west-1.amazonaws.com/2004
java.io.IOException: Error writing request body to server
    at sun.net.www.protocol.http.HttpURLConnection$StreamingOutputStream.checkError(HttpURLConnection.java:3174)
    at sun.net.www.protocol.http.HttpURLConnection$StreamingOutputStream.write(HttpURLConnection.java:3157)
    at com.google.common.io.CountingOutputStream.write(CountingOutputStream.java:53)
    at com.google.common.io.ByteStreams.copy(ByteStreams.java:211)
    at org.jclouds.io.payloads.BasePayload.writeTo(BasePayload.java:67)
    at org.jclouds.http.internal.JavaUrlHttpCommandExecutorService.writePayloadToConnection(JavaUrlHttpCommandExecutorService.java:246)
    at org.jclouds.http.internal.JavaUrlHttpCommandExecutorService.convert(JavaUrlHttpCommandExecutorService.java:218)
    at org.jclouds.http.internal.JavaUrlHttpCommandExecutorService.convert(JavaUrlHttpCommandExecutorService.java:76)
    at org.jclouds.http.internal.BaseHttpCommandExecutorService.invoke(BaseHttpCommandExecutorService.java:142)
    at org.jclouds.rest.internal.InvokeHttpMethod.invoke(InvokeHttpMethod.java:131)
    at org.jclouds.rest.internal.InvokeHttpMethod.apply(InvokeHttpMethod.java:97)
    at org.jclouds.rest.internal.InvokeHttpMethod.apply(InvokeHttpMethod.java:59)
    at org.jclouds.rest.internal.DelegatesToInvocationFunction.handle(DelegatesToInvocationFunction.java:137)
    at org.jclouds.rest.internal.DelegatesToInvocationFunction.invoke(DelegatesToInvocationFunction.java:125)
    at $Proxy47.putObject(Unknown Source)
    at org.jclouds.s3.blobstore.S3BlobStore.putBlob(S3BlobStore.java:241)
    at org.jclouds.aws.s3.blobstore.AWSS3BlobStore.putBlob(AWSS3BlobStore.java:98)
    at org.jclouds.s3.blobstore.S3BlobStore.putBlob(S3BlobStore.java:219)
    at org.jscsi.target.storage.JCloudsStorageModule$WriteTask.call(JCloudsStorageModule.java:388)
    at org.jscsi.target.storage.JCloudsStorageModule$WriteTask.call(JCloudsStorageModule.java:1)
    at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:334)
    at java.util.concurrent.FutureTask.run(FutureTask.java:166)
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1110)
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:603)
    at java.lang.Thread.run(Thread.java:722)
```