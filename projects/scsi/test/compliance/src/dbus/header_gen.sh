#!/bin/bash

dbus-binding-tool --mode=glib-server --prefix=s_g_data_transfer  s_g_data_transfer.xml > s_g_data_transfer.h
dbus-binding-tool --mode=glib-client s_g_transport.xml > s_g_transport.h
