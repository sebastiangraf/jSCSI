
package org.jscsi.parser.digest;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.zip.CRC32;

import org.perfidix.annotation.BenchClass;

/**
 * <h1>DigestBench</h1>
 * <p/>
 * This class is a benchmark to measure the performance of the Java implemented
 * CRC32 Digest with Intel's Slicing-by-4 CRC32C Digest implementation.
 * 
 * @author Volker Wildi
 */
@BenchClass(runs = 1000)
public class DigestBench {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The length (in bytes) of the block to protect with a digest. */
  private static final int BLOCK_SIZE = 8192;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The array with only zeros as content. */
  private final byte[] nullArray;

  /** The array with random content. */
  private final byte[] randomArray;

  /** The ByteBuffer with only zeros as content. */
  private final ByteBuffer nullByteBuffer;

  /** The ByteBuffer with random content (same as <code>randomArray</code>). */
  private final ByteBuffer randomByteBuffer;

  /** The random number generator to fill the buffer to send. */
  private final Random randomGenerator;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The instance for the Java implemented CRC32 Digest. */
  private final CRC32 crc32Digest;

  /** The instance for the Intel's Slicing-by-4 CRC32C Digest. */
  private final CRC32CDigest crc32cDigest;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** Constructor to create a new, empty <code>DigestBench</code> instance. */
  public DigestBench() {

    nullArray = new byte[BLOCK_SIZE];
    nullByteBuffer = ByteBuffer.allocate(BLOCK_SIZE);

    randomArray = new byte[BLOCK_SIZE];
    randomGenerator = new Random();
    randomGenerator.nextBytes(randomArray);

    randomByteBuffer = ByteBuffer.wrap(randomArray);

    crc32Digest = new CRC32();
    crc32cDigest = new CRC32CDigest();
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Measures the time to calculate the CRC32 digest value of an empty byte
   * array.
   */
  public final void benchCRCwithNullArray() {

    crc32Digest.update(nullArray, 0, BLOCK_SIZE);
  }

  /**
   * Measures the time to calculate the CRC32C digest value of an empty
   * <code>ByteBuffer</code> object.
   */
  public final void benchCRC32CwithNullArray() {

    crc32cDigest.update(nullArray, 0, BLOCK_SIZE);
  }

  /**
   * Measures the time to calculate the CRC32C digest value of an empty
   * <code>ByteBuffer</code> object.
   */
  public final void benchCRC32CwithNullByteBuffer() {

    crc32cDigest.update(nullByteBuffer, 0, BLOCK_SIZE);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Measures the time to calculate the CRC32 digest value of a byte array with
   * random-filled content.
   */
  public final void benchCRCwithRandomArray() {

    crc32Digest.update(randomArray, 0, BLOCK_SIZE);
  }

  /**
   * Measures the time to calculate the CRC32C digest value of a byte array with
   * random-filled content.
   */
  public final void benchCRC32CwithRandomArray() {

    crc32cDigest.update(randomArray, 0, BLOCK_SIZE);
  }

  /**
   * Measures the time to calculate the CRC32C digest value of a
   * <code>ByteBuffer</code> object with random-filled content.
   */
  public final void benchCRC32CwithRandomByteBuffer() {

    crc32cDigest.update(randomByteBuffer, 0, BLOCK_SIZE);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
