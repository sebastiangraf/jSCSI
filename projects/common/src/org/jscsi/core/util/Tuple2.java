package org.jscsi.core.util;

public class Tuple2<T1, T2>
{
   public T1 first;
   public T2 second;
   
   /////////////////////////////////////////////////////////////////////////////
   // constructors
   
   
   public Tuple2(T1 first, T2 second)
   {
      this.first = first;
      this.second = second;
   }

   
   /////////////////////////////////////////////////////////////////////////////
   // getters/setters
   
   public T1 getFirst()
   {
      return first;
   }

   public void setFirst(T1 first)
   {
      this.first = first;
   }

   public T2 getSecond()
   {
      return second;
   }

   public void setSecond(T2 second)
   {
      this.second = second;
   }
   
   @Override
   public String toString()
   {
      return "<Tuple value-1: " + this.first + " value-2: " + this.second + ">";
   }
}
