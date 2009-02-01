/*
 * Copyright by Volker Wildi, 2007
 */
#define _GNU_SOURCE

#include <jni.h>
#include "OpenISCSIBench.h"

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

/* Size of one block (in bytes) which is used as unit. */
#define BLOCK_SIZE 32768

/* Number of bytes for the alignment of the start address of the buffer for the read and write syscalls. */
#define ALIGN 512

/* Returns the minimum of both integer numbers, but with possible side effects. */
#define MIN(X,Y) ((X) < (Y) ? (X) : (Y))

/* File descriptor for the given device. */
static int fd;

/*
 * Class:     OpenISCSIBench
 * Method:    openDevice
 * Signature: (Ljava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_OpenISCSIBench_openDevice
  (JNIEnv *env, jobject obj, jstring deviceName, jboolean buffered)
{
	int flags = O_RDWR;
	if (buffered == JNI_TRUE)
	{
		flags |= O_DIRECT;
	}

	const char* pDeviceName = (*env)->GetStringUTFChars (env, deviceName, NULL);
	if (pDeviceName != NULL)
	{
		fd = open (pDeviceName, flags);


		(*env)->ReleaseStringUTFChars (env, deviceName, pDeviceName);
		if (fd == -1)
			{
				// error occured
				jclass exception = (*env)->FindClass (env, "java/lang/Exception");
				(*env)->ThrowNew (env, exception, "Device not opened.");
			}
	}
}

/*
 * Class:     OpenISCSIBench
 * Method:    closeDevice
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_OpenISCSIBench_closeDevice
  (JNIEnv *env, jobject obj)
{
	close (fd);
}

/*
 * Class:     OpenISCSIBench
 * Method:    nativeReadBlock
 * Signature: (J[B)V
 */
JNIEXPORT void JNICALL Java_OpenISCSIBench_nativeReadBlock
  (JNIEnv *env, jobject obj, jlong address, jbyteArray data, jboolean copyToBuffer)
{
	int len = (int) (*env)->GetArrayLength (env, data);

	char buf [len + ALIGN];
	char* pBase = (char*) ((ssize_t) (buf + ALIGN) & ~(ALIGN - 1));

	off_t reqOffset = BLOCK_SIZE * address;
	off_t retOffset = lseek (fd, reqOffset, SEEK_SET);
	if (retOffset != reqOffset)
	{
		// error occured
		jclass exception = (*env)->FindClass (env, "java/lang/Exception");
		(*env)->ThrowNew (env, exception, "Mismatch in offsets.");
	}

	int readLen = read (fd, pBase, len);
	if (readLen == -1)
	{
		// error occured
		jclass exception = (*env)->FindClass (env, "java/lang/Exception");
		(*env)->ThrowNew (env, exception, "No data has been read.");
	}

	if (copyToBuffer == JNI_TRUE)
	{
		(*env)->SetByteArrayRegion (env, data, 0, MIN (readLen, len), pBase);
	}
}

/*
 * Class:     OpenISCSIBench
 * Method:    nativeWriteBlock
 * Signature: (J[B)V
 */
JNIEXPORT void JNICALL Java_OpenISCSIBench_nativeWriteBlock
  (JNIEnv *env, jobject obj, jlong address, jbyteArray data)
{
	int len = (int) (*env)->GetArrayLength (env, data);
	char buf [len + ALIGN];
	char* pBase = (char*) ((ssize_t) (buf + ALIGN) & ~ (ALIGN -1));
	(*env)->GetByteArrayRegion (env, data, 0, len, pBase);

	off_t reqOffset = BLOCK_SIZE * address;
	off_t retOffset = lseek (fd, reqOffset, SEEK_SET);
	if (retOffset != reqOffset)
	{
		// error occured
		jclass exception = (*env)->FindClass (env, "java/lang/Exception");
		(*env)->ThrowNew (env, exception, "Mismatch in offsets.");
	}

	int writtenLen = write (fd, pBase, len);
	if (writtenLen == -1)
	{
		// error occured
		jclass exception = (*env)->FindClass (env, "java/lang/Exception");
		(*env)->ThrowNew (env, exception, "No data is written.");
	}
}
