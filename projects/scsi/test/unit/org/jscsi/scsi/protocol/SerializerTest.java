
package org.jscsi.scsi.protocol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jscsi.scsi.protocol.cdb.CDBFactory;

public class SerializerTest
{

   private static String inputFile = "cdb.in";

   private class TagValue
   {
      private int bits;
      private String tag;
      private String value; // template uses string value
      private long intValue; // instance uses long (intValue)

      public TagValue(TagValue tv)
      {
         bits = tv.bits;
         tag = tv.tag;
         value = tv.value;
         intValue = tv.intValue;
      }

      public TagValue(int b, String t, String v)
      {
         bits = b;
         tag = t;
         value = v;
      }

      public TagValue(int b, String t, long v)
      {
         bits = b;
         tag = t;
         intValue = v;
      }

      int getBits()
      {
         return bits;
      }

      String getTag()
      {
         return tag;
      }

      String getValue()
      {
         return value;
      }

      long getIntValue()
      {
         return intValue;
      }

      public String toString()
      {
         if (value != null)
            return (" | " + tag + " | " + bits + " | " + value);
         else
            return (" | " + tag + " | " + bits + " | " + Long.toHexString(intValue).toUpperCase());
      }
   }

   private class EncodableSpec
   {
      private Class cls;
      private List<TagValue> tagList = new ArrayList<TagValue>();

      private List<EncodableSpec> instanceList = null;

      private int size = 0; // number of bits
      private byte[] dataBytes;

      public EncodableSpec(String defaultPackage, String className) throws ClassNotFoundException
      {
         try
         {
            // We check this first because we don't want spurious linkages for classes
            // with no package name explicitly defined.
            this.cls = Class.forName(defaultPackage + "." + className);
         }
         catch (Exception e)
         {
            this.cls = Class.forName(className);
         }

         if (!Encodable.class.isAssignableFrom(this.cls))
         {
            throw new ClassNotFoundException("Found class does not implement Encodable: "
                  + this.cls.getName());
         }

         size = 0;
         dataBytes = null;
      }

      public EncodableSpec(Class cls)
      {
         this.cls = cls;
         this.size = 0;
         dataBytes = null;
      }

      public EncodableSpec(EncodableSpec encodable)
      {
         this.cls = encodable.cls;
         this.size = encodable.size;
         for (TagValue tv : encodable.tagList)
         {
            this.tagList.add(new TagValue(tv));
         }
      }

      public int getSize()
      {
         return size;
      }

      public void setSize(int n)
      {
         size = n;
      }

      public byte[] getDataBytes()
      {
         return dataBytes;
      }

      public void setDataBytes(byte[] data)
      {
         dataBytes = data;
      }

      public void addInstance(EncodableSpec encodable)
      {
         if (instanceList == null)
         {
            instanceList = new ArrayList<EncodableSpec>();
         }
         instanceList.add(encodable);
      }

      public String getName()
      {
         return this.cls.getName();
      }

      public Class<Encodable> getClassObject()
      {
         return this.cls;
      }

      public String toString()
      {
         String result = ("< " + this.cls.getName() + "=" + size);
         for (TagValue tv : tagList)
         {
            result += tv.toString();
         }
         if (instanceList != null)
         {
            result += "( inst=" + instanceList.size() + " )";
         }
         result += ">";
         return result;
      }

      public void printInstances()
      {
         for (EncodableSpec one : instanceList)
         {
            System.out.println(one.toString());
         }
      }

      public void addTag(int bits, String tag, String val)
      {
         tagList.add(new TagValue(bits, tag, val));
         size += bits;
      }

      public void addTag(int bits, String tag, long val)
      {
         tagList.add(new TagValue(bits, tag, val));
         size += bits;
      }

      public void reduceBits(int b)
      {
         size -= b;
      }

      public List<byte[]> deepCopy(List<byte[]> srcList)
      {
         List<byte[]> dest = new ArrayList<byte[]>();
         for (byte[] ba : srcList)
         {
            byte[] newBa = new byte[ba.length];
            for (int i = 0; i < ba.length; i++)
               newBa[i] = ba[i];
            dest.add(newBa);
         }

         return dest;
      }

      public void generateInstances()
      {
         // add first instance.
         this.addInstance(new EncodableSpec(this.cls));

         for (TagValue tv : this.tagList)
         {
            List<Long> valueList = convertValue(tv.getBits(), tv.getTag(), tv.getValue());
            List<EncodableSpec> newList = new ArrayList<EncodableSpec>();
            for (long val : valueList)
            {
               // add 'val' for each instance
               for (EncodableSpec inst : this.instanceList)
               {
                  EncodableSpec instCopy = new EncodableSpec(inst);
                  instCopy.addTag(tv.getBits(), tv.getTag(), val);
                  newList.add(instCopy);
               }
            }
            // set the new List as instanceList.
            this.instanceList = newList;
         }
      }

      /**
       * Generates List if byteArrays, each element is an array bytes that is filled with but
       * pattern generated from the specification.
       * 
       * Assumes that parserCDBspec() is called first to parse the specifications.
       * 
       * @return
       */

      public byte[] generateBytes()
      {
         int sizeBytes = (int) Math.ceil(size * 1.0 / 8);
         long value = 0;
         // System.out.println("NumBits=" + size + ", numBytes=" + sizeBytes);
         byte[] result = new byte[sizeBytes];

         int currentByteIndx = 0;
         int currentBit = 7;
         byte currentByte = 0;
         for (int i = 0; i < tagList.size(); i++)
         {
            TagValue tv = tagList.get(i);
            int bits = tv.getBits();
            value = tv.getIntValue();

            // copy bits from value into currentByte
            boolean empty = true;
            while (bits > 0)
            {
               empty = false;
               long mask = 1 << (bits - 1);
               if ((mask & value) != 0)
               {
                  currentByte = (byte) (currentByte | (1 << currentBit));
               }
               // value = value >>> 1;
               bits--;
               currentBit--;
               if (currentBit < 0)
               {
                  // currentByte is filled, move to next byte
                  result[currentByteIndx] = currentByte;
                  empty = true;
                  currentByteIndx++;
                  currentBit = 7;
                  currentByte = 0;
               }
            }
            if (!empty)
            {
               // write the last byte whatever it has, may be partially filled.
               result[currentByteIndx] = currentByte;
            }
         }
         this.setDataBytes(result);
         return result;
      }

      private List<Long> convertValue(int numBits, String tag, String valString)
      {
         List<Long> valList = new ArrayList<Long>();
         long localVal = 0;
         if (valString.startsWith("std"))
         {
            // add the beginning boundary condition.
            valList.add(0L);
            localVal = 1;
            for (int i = 1; i < numBits; i++)
            {
               localVal = localVal << 1;
               localVal |= 1;
               if (i % 8 == 0)
               {
                  valList.add(localVal);
               }
            }
            valList.add((long) Math.pow(2, numBits) - 1);
         }
         else if (valString.startsWith("random"))
         {
            int numRandom = 1; // default generate just 1.
            // generate random value of size bits;
            if (System.getProperty("test.randomSize." + getName() + "." + tag) != null)
            {
               numRandom = Integer.getInteger("test.randomSize." + getName() + "." + tag);
            }
            else if (System.getProperty("test.randomSize." + getName()) != null)
            {
               numRandom = Integer.getInteger("test.randomSize." + getName());
            }
            else if (System.getProperty("test.randomSize") != null)
            {
               // Random property is set. Use it.
               numRandom = Integer.getInteger("test.randomSize");
            }

            if (valString.equals("random"))
               valList = generateRandomBits(numBits, numRandom, false);
            else
               valList = generateRandomBits(numBits, numRandom, true);
         }
         else if (valString.substring(0, 1).equals("["))
         {
            // we have a list specification. Parse all the values.
            String stripBraces = valString.substring(1, valString.length() - 1);
            String valStrings[] = stripBraces.split(";");
            for (String one : valStrings)
            {
               String baseStr = one.substring(1, 2);
               String oneVal = null;
               int base = 10;
               if (baseStr.equals("x"))
               {
                  base = 16;
                  oneVal = one.substring(2);
               }
               else if (baseStr.equals("b"))
               {
                  base = 2;
                  oneVal = one.substring(2);
               }
               else if (baseStr.equals("o"))
               {
                  base = 8;
                  oneVal = one.substring(2);
               }
               localVal = Long.parseLong(oneVal, base);
               valList.add(localVal);
            }
         }
         else
         {
            String baseStr = valString.substring(1, 2);
            int base = 10;
            if (baseStr.equals("x"))
            {
               base = 16;
               valString = valString.substring(2);
            }
            else if (baseStr.equals("b"))
            {
               base = 2;
               valString = valString.substring(2);
            }
            else if (baseStr.equals("o"))
            {
               base = 8;
               valString = valString.substring(2);
            }
            localVal = Long.parseLong(valString, base);
            valList.add(localVal);
         }

         return valList;
      }

      public List<Long> generateRandomBits(int numBits, int numRandom, boolean assci)
      {
         List<Long> randomList = new ArrayList<Long>();

         long bitMask = 0;
         for (int i = 0; i < numBits; i++)
         {
            bitMask = (bitMask << 1) | 1;
         }
         Random rnb = new Random(12345);

         int numGenerated = 1;
         while (numGenerated <= numRandom)
         {
            long ran = rnb.nextLong() & bitMask;
            if (assci)
            {
               // printable ASCII values fall [33-126] range (except whitespace).
               if ((numGenerated < 33) || (numGenerated > 126))
                  continue;
            }
            if (uniqueNumber(randomList, ran))
            {
               randomList.add(ran);
               numGenerated++;
            }
         }
         return randomList;
      }

      private boolean uniqueNumber(List<Long> list, long ran)
      {
         for (long num : list)
         {
            if (num == ran)
            {
               return false;
            }
         }
         return true;
      }
   }

   private List<EncodableSpec> parsedSpecLines = new ArrayList<EncodableSpec>();

   private Serializer serializer;

   public SerializerTest(Serializer serializer, String defaultPackage, String testSpecification)
   {
      this.serializer = serializer;
      this.parseOneSpec(defaultPackage, testSpecification);

      System.out.println("======================================================");
      System.out.println("PARSED CDB SPEC FOLLOWS ");
      System.out.println("======================================================");

      this.printParsedCDBs();

   }

   protected SerializerTest(Serializer serializer, String defaultPackage, File testSpecifications)
   {
      this.serializer = serializer;
      this.parseAllSpecs(defaultPackage, testSpecifications.getAbsolutePath());

      System.out.println("======================================================");
      System.out.println("PARSED CDB SPEC FOLLOWS ");
      System.out.println("======================================================");

      this.printParsedCDBs();
   }

   public void runTest() throws Exception
   {
      this.generateAllInstances();
      this.printAllInstances();
      this.generateByteData();

      Exception exception = null;

      for (EncodableSpec spec : this.parsedSpecLines)
      {
         System.out.println(spec.getName() + " instances:");
         for (EncodableSpec inst : spec.instanceList)
         {
            byte[] data = inst.getDataBytes();
            System.out.println();
            exception = this.executeTest(serializer, inst, data);

            System.out.println();
         }
      }

      if (exception != null)
      {
         throw exception;
      }
   }

   private List<EncodableSpec> getParsedSpecLines()
   {
      return parsedSpecLines;
   }

   private void parseOneSpec(String defaultPackage, String line)
   {
      if (line.equals(""))
      {
         return;
      }
      // check for comment lines.
      String firstChar = line.substring(0, 1);
      if (firstChar.equals("#"))
      {
         // ignore the line if it starts with a '#'.
         return;
      }
      String[] results = line.split(",");

      EncodableSpec encodable;
      try
      {
         encodable = new EncodableSpec(defaultPackage, results[0]);
      }
      catch (ClassNotFoundException e)
      {
         e.printStackTrace();
         System.err.println("Skipping spec line " + results[0] + "; class not found");
         throw new RuntimeException("Skipping spec line " + results[0] + "; class not found");
      }

      int bits = 0;
      for (int i = 1; i < results.length; i++)
      {
         String[] elements = results[i].split("=");

         String tag = elements[0];
         String[] values = elements[1].split(":");
         bits = Integer.parseInt(values[0]);
         String val = values[1];
         encodable.addTag(bits, tag, val);
      }

      if ((encodable.getSize() % 8) != 0)
      {
         System.err.println("Spec line not at byte boundry: " + encodable.getSize());
         throw new RuntimeException("Spec line not at byte boundry: " + encodable.getSize());
      }
      else
      {
         parsedSpecLines.add(encodable);
      }
   }

   private void generateAllInstances()
   {
      for (EncodableSpec cdb : this.parsedSpecLines)
      {
         cdb.generateInstances();
      }
   }

   /**
    * @param fileName
    *           with CDB specifications.
    */
   private void parseAllSpecs(String defaultPackage, String path)
   {

      String line;
      int numLines = 0;
      // parse all the lines one by one.
      try
      {
         BufferedReader in = new BufferedReader(new FileReader(path));
         while ((line = in.readLine()) != null)
         {
            numLines++;
            parseOneSpec(defaultPackage, line);
         }
         in.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return;
      }
   }

   private void printParsedCDBs()
   {
      for (EncodableSpec cdb : parsedSpecLines)
      {
         System.out.println(cdb.toString());
      }
   }

   private void printAllInstances()
   {
      for (EncodableSpec cdb : parsedSpecLines)
      {
         cdb.printInstances();
      }
   }

   private void generateByteData()
   {
      for (EncodableSpec cdb : this.parsedSpecLines)
      {
         for (EncodableSpec inst : cdb.instanceList)
         {
            inst.generateBytes();
         }
      }
   }

   /**
    * @param args
    */
   public static void main(String[] args)
   {

      Serializer serializer = new CDBFactory();
      String defaultPackage = "org.jscsi.scsi.protocol.cdb";
      String inputPath = System.getProperty("test.input", inputFile);

      SerializerTest parser = new SerializerTest(serializer, defaultPackage, new File(inputPath));

      try
      {
         parser.runTest();
      }
      catch (Exception e)
      {
         System.err.println("Test failed: " + e.getMessage());
      }

   }

   private Exception checkEncodableFields(EncodableSpec spec, Encodable encodable)
   {
      String checkResult = null;
      try
      {
         for (TagValue tv : spec.tagList)
         {
            if (tv.getTag().equals("reserved"))
               continue;
            // find access method and compare the value.
            String prefix = null;
            if (tv.getBits() > 1)
            {
               // the field is more than 1 bit, hence the type of the field is a Number.
               prefix = "get";
               Method accessMethod = encodable.getClass().getMethod(prefix + tv.getTag());
               Object valObj = accessMethod.invoke(encodable);
               Number val = null;
               try
               {
                  val = (Number) valObj;
               }
               catch (ClassCastException e)
               {
               }
               if (accessMethod.getReturnType().equals("boolean"))
               {
                  // return value of the field is boolean
                  Long valInput = (Long) tv.getIntValue();
                  if (val.intValue() == valInput.intValue())
                  {
                     System.out.format("Success: %s : %X == %X %n", tv.getTag(), val,
                           tv.getIntValue());
                  }
                  else
                  {
                     System.out.format("Fail: %s : %X != %X %n", tv.getTag(), val, tv.getIntValue());
                     checkResult = "Non-matching field detected";
                  }
               }
               else if (val != null)
               {
                  // return value of the field is some number (long, byte, int, etc)
                  Long valInput = (Long) tv.getIntValue();
                  if (tv.getBits() == 64 && val.longValue() < 0)
                  {
                     System.out.format("Limit: %s : %X (cannot check value over LONG_MAX) %n",
                           tv.getTag(), val);
                  }
                  else if (valInput.longValue() == val.longValue())
                  {
                     System.out.format("Success: %s : %X == %X %n", tv.getTag(), val,
                           tv.getIntValue());
                  }
                  else
                  {
                     System.out.format("Fail: %s : %X != %X %n", tv.getTag(), val, tv.getIntValue());
                     checkResult = "Non-matching field detected";
                  }
               }
               else if (accessMethod.getReturnType().equals(byte[].class))
               {
                  System.out.println("Fail: " + tv.getTag() + " : byte arrays are not supported");
                  checkResult = "Unsupported return type for field (" + tv.getTag() + ")";
               }
               else
               {
                  System.out.println("Fail: " + tv.getTag() + " : unsupported field return type: "
                        + accessMethod.getReturnType());
                  checkResult = "Unsupported return type for field (" + tv.getTag() + ")";
               }
            }
            else
            {
               // the width of the field is one bit, hence the type is boolean.
               prefix = "is";
               Method accessMethod = encodable.getClass().getMethod(prefix + tv.getTag());
               Object valObj = accessMethod.invoke(encodable);
               Boolean val = (Boolean) valObj;
               Long valInput = (Long) tv.getIntValue();
               if (((valInput == 1) && (val)) || ((valInput == 0) && !val))
               {
                  System.out.format("Success: %s : %b == %X %n", tv.getTag(), val, tv.getIntValue());
               }
               else
               {
                  System.out.format("Fail: %s : %b != %X %n", tv.getTag(), val, tv.getIntValue());
                  checkResult = "Non-matching field detected";
               }
            }
         }
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         return ex;
      }

      if (checkResult != null)
      {
         return new RuntimeException(checkResult);
      }
      else
      {
         return null;
      }
   }

   // Returns an Exception or an Encodable, caller has to check
   private Object executeDecodingTest(Serializer serializer, EncodableSpec spec, byte[] data)
   {
      for (byte b : data)
      {
         System.out.format("%02X ", b);
      }
      System.out.println();

      // Run the serializer with the data as input.
      Encodable encodable = null;
      try
      {
         encodable = serializer.decode(ByteBuffer.wrap(data));

         if (encodable == null)
         {
            return new RuntimeException(
                  "Serializer did not return encodable object (returned null)");
         }
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         return ex;
      }

      // verify that the returned encodable object is the same as the specification indicates
      if (!spec.getClassObject().isInstance(encodable))
      {
         System.out.format("Fail: returned class %s is not instance of %s %n",
               encodable.getClass().getName(), spec.getClassObject().getName());
         return new RuntimeException("Returned instance does not match specification class:"
               + encodable.getClass().getName());
      }

      // verify the correctness by checking each tag and its value.
      Exception checkResult = checkEncodableFields(spec, encodable);

      return checkResult == null ? encodable : checkResult;
   }

   protected Exception executeTest(Serializer serializer, EncodableSpec spec, byte[] data)
         throws IOException
   {
      System.out.println("Check decode: " + spec.getName());
      Object obj = executeDecodingTest(serializer, spec, data);
      if (obj instanceof Exception || obj == null)
      {
         return (Exception) obj;
      }
      else if (obj instanceof Encodable)
      {
         System.out.println("Check encode: " + spec.getName());
         data = ((Encodable) obj).encode();
         obj = executeDecodingTest(serializer, spec, data);
         if (obj == null || obj instanceof Encodable)
         {
            return null;
         }
         else if (obj instanceof Exception)
         {
            return (Exception) obj;
         }
      }
      throw new RuntimeException("Improper object type returned.");
   }
}
