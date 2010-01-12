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

#include <stdio.h>
#include <stdlib.h>
#include <glib.h>
#include <dbus/dbus-glib-bindings.h>
#include <glib/gprintf.h>

typedef struct SGDataTransfer SGDataTransfer;
typedef struct SGDataTransferClass SGDataTransferClass;

GType s_g_data_transfer_get_type (void);

struct SGDataTransfer
{
  GObject parent;
};

struct SGDataTransferClass
{
  GObjectClass parent;
};


#define S_G_DATA_TRANSFER_TYPE_OBJECT    (s_g_data_transfer_get_type ())

G_DEFINE_TYPE(SGDataTransfer, s_g_data_transfer, G_TYPE_OBJECT)

gboolean 
service_response(SGDataTransfer* dataTransfer, const char * IN_initiatorPort, 
                 const char * IN_targetPort, const gint64 IN_lun, const GArray* IN_senseData,
                 const gint32 IN_status, const gint32 IN_serviceResponse, GError **error);

gboolean 
send_data_in(SGDataTransfer* dataTransfer, const char * IN_initiatorPort, 
                 const char * IN_targetPort, const gint64 IN_lun, const GArray* IN_input);


GArray* 
receive_data_out(SGDataTransfer* dataTransfer, const char * IN_initiatorPort, 
                 const char * IN_targetPort, const gint64 IN_lun);

#include "s_g_data_transfer.h"

static void
s_g_data_transfer_init (SGDataTransfer *obj)
{
}

static void
s_g_data_transfer_class_init (SGDataTransferClass *klass)
{
}

gboolean 
service_response(SGDataTransfer* dataTransfer, const char * IN_initiatorPort, 
                 const char * IN_targetPort, const gint64 IN_lun, const GArray* IN_senseData,
                 const gint32 IN_status, const gint32 IN_serviceResponse, GError **error)
{
   g_printf("In method: service_response()\n");

   // implement
   if(1)
   {

      return TRUE;
   }
   else
   {
      // set error using g_set_error method
      return FALSE;
   }
}


gboolean 
send_data_in(SGDataTransfer* dataTransfer, const char * IN_initiatorPort, 
                 const char * IN_targetPort, const gint64 IN_lun, const GArray* IN_input)
{
   g_printf("In method: send_data_in()\n");

   // implement
   return TRUE;
}


GArray* 
receive_data_out(SGDataTransfer* dataTransfer, const char * IN_initiatorPort, 
                 const char * IN_targetPort, const gint64 IN_lun)
{
   g_printf("In method: receive_data_out()\n");

   // implement
   return 0;
}


int
main (int argc, char **argv)
{
   DBusGConnection *connection;
   GError *error;
   SGDataTransfer *dataTransfer;
   GMainLoop *mainloop;
  
   g_printf("starting server...\n");

   g_type_init ();

   // Install type 
   dbus_g_object_type_install_info (S_G_DATA_TRANSFER_TYPE_OBJECT, &dbus_glib_s_g_data_transfer_object_info);

   mainloop = g_main_loop_new (NULL, FALSE);

   // Create a connection
   error = NULL;
   connection = dbus_g_bus_get (DBUS_BUS_SYSTEM,
                                &error);
   if (connection == NULL)
   {
      g_printerr ("Failed to open connection to bus: %s\n",
                  error->message);
      g_error_free (error);
      exit (1);
   }
   
   g_printf("connection to bus established\n");

   // Create the object
   dataTransfer = g_object_new (S_G_DATA_TRANSFER_TYPE_OBJECT, NULL); 

   if (dataTransfer == NULL)
   {
      g_printerr ("Failed to create g_object!");
      exit (1);
   }

   g_printf("SGDataTransfer object created\n");

   // Registering the object should export it to the BUS
   dbus_g_connection_register_g_object (connection,
                                        "/org/jscsi/sg/dbus/SGDataTransfer",
                                        G_OBJECT(dataTransfer));

   g_printf("object exported\n");

   g_main_loop_run (mainloop);
   
   exit(0);
}

// EOF

