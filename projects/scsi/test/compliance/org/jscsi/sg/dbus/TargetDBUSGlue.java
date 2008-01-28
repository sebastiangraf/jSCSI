//Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
//Cleversafe Dispersed Storage(TM) is software for secure, private and
//reliable storage of the world's data using information dispersal.
//
//Copyright (C) 2005-2007 Cleversafe, Inc.
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
//USA.
//
//Contact Information: 
// Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email: licensing@cleversafe.org
//
//END-OF-HEADER
//-----------------------
//@author: John Quigley <jquigley@cleversafe.com>
//@date: January 1, 2008
//---------------------

package org.jscsi.sg.dbus;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

//TODO: Describe class or interface
public class TargetDBUSGlue
{
   private static String S_G_DATA_TRANSFER_OBJECT_PATH = "/org/jscsi/sg/dbus/SGDataTransfer";

   /**
    * @param args
    */
   public static void main(String[] args)
   {

      DBusConnection client = null;

      try
      {
         client = DBusConnection.getConnection(DBusConnection.SYSTEM);

         // Get the remote object
         SGDataTransfer dataTransfer =
               (SGDataTransfer) client.getRemoteObject("SYSTEM", S_G_DATA_TRANSFER_OBJECT_PATH,
                     SGDataTransfer.class);

         if (dataTransfer == null)
         {
            System.out.println("null object returned");
            System.exit(0);
         }

         // Call methods on it
         dataTransfer.serviceResponse("", "", 0, new byte[0], 0, 0);
         //dataTransfer.sendDataIn("", "", 0, new byte[2]);
      }
      catch (DBusException e)
      {
         e.printStackTrace();
      }
      finally
      {
         if (client != null)
         {
            System.out.println("disconnecting client");
            client.disconnect();
         }
      }

   }

}
