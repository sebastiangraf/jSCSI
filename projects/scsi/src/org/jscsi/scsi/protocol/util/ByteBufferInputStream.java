
package org.jscsi.scsi.protocol.util;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

// TODO: Describe class or interface
public class ByteBufferInputStream extends InputStream
{
   private ByteBuffer buffer;
   
   public ByteBufferInputStream( ByteBuffer buffer )
   {
      this.buffer = buffer;
   }
   
   public ByteBufferInputStream( ByteBuffer buffer, boolean duplicate )
   {
      if ( duplicate ) 
      {
         this.buffer = buffer.duplicate();
      }
      else
      {
         this.buffer = buffer;
      }
   }
   
   public ByteBuffer getByteBuffer()
   {
      return this.buffer;
   }
   
   @Override
   public int available() throws IOException
   {
      return this.buffer.remaining();
   }

   @Override
   public void close() throws IOException
   {
      super.close();
   }

   @Override
   public synchronized void mark(int arg0)
   {
      this.buffer.mark();
   }

   @Override
   public boolean markSupported()
   {
      return true;
   }

   @Override
   public int read(byte[] dst, int offset, int length) throws IOException
   {
      if ( length > this.buffer.remaining() )
      {
         length = this.buffer.remaining();
      }
      
      this.buffer.get(dst, offset, length);
      return length;
   }

   @Override
   public int read(byte[] dst) throws IOException
   {
      int length = dst.length;
      if ( length > this.buffer.remaining() )
      {
         length = this.buffer.remaining();
      }
      
      this.buffer.get(dst, 0, length);
      return length;
   }

   @Override
   public synchronized void reset() throws IOException
   {
      this.buffer.reset();
   }
   
   @Override
   public long skip( long length ) throws IOException
   {
      if ( length > this.buffer.remaining() )
      {
         length = this.buffer.remaining();
      }
      buffer.position((int)(buffer.position()+length));
      return length;
   }

   @Override
   public int read() throws IOException
   {
      try
      {
         int t = (int) this.buffer.get() & 0xFF;
         return t;
      }
      catch (BufferUnderflowException e)
      {
         throw new IOException("Buffer underflow: " + e.getMessage());
      }
   }

}


