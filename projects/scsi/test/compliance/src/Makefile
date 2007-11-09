# -----------------------------------------------------------------------------
# Date:     Thu Nov  8 18:01:08 CST 2007
# Author:   John Quigley <jquigley@cleversafe.com>
# Revision: $Id$
# -----------------------------------------------------------------------------

SHELL   = /bin/sh
CC      = gcc
CTAGS   = cscope -Rb

DEFS    = -DDEBUG
FLAGS   = -O2 -Wall -std=c99 -g
CFLAGS  = $(FLAGS) $(DEFS) -I.	-I/usr/lib/dbus-1.0/include \
				-I/usr/include/dbus-1.0     \
				-I/usr/include/glib-2.0     \
				-I/usr/lib/glib-2.0/include
LDLIBS  = -ldbus-1 -ldbus-glib-1
LDFLAGS = $(FLAGS)

prefix  = /usr/local
bindir  = $(prefix)/bin

.SUFFIXES:
.SUFFIXES: .c .o

##### Do not edit below this line, please. #####

SERVER_BIND  = $(addprefix dbus/, s_g_data_transfer.h)
SERVER_SRCS  = $(addprefix dbus/, server.c)
SERVER_OBJS  = $(addprefix dbus/, server.o)
SERVER_BINS  = server

CLIENT_BIND  = $(addprefix dbus/, s_g_transport.h)
CLIENT_SRCS  = $(addprefix dbus/, client.c)
CLIENT_OBJS  = $(addprefix dbus/, client.o)
CLIENT_BINS  = client

EXAMPLE_OBJS = $(addprefix dbus/, dbus-glib-example.o)
EXAMPLE_SRCS = $(addprefix dbus/, dbus-glib-example.c)
EXAMPLE_BINS = dbus-glib-example

OBJS = $(EXAMPLE_OBJS) $(SERVER_OBJS) $(CLIENT_OBJS)
SRCS = $(EXAMPLE_SRCS) $(SERVER_SRCS) $(CLIENT_SRCS) $(SERVER_BIND) $(CLIENT_BIND)
BINS = $(EXAMPLE_BINS) $(SERVER_BINS) $(CLIENT_BINS)

##### Phony rules

.PHONY:		all
all:		examples server client

.PHONY:		all
examples:	$(EXAMPLE_BINS)

.PHONY:		tags
tags:		$(SRCS)
		$(CTAGS) $(SRCS)

.PHONY:		clean
clean:
		rm -rf $(OBJS)
		rm -rf $(BINS)
		rm -rf $(SERVER_BIND) $(CLIENT_BIND)
		rm -rf cscope.out

##### Real rules

$(EXAMPLE_BINS):	$(EXAMPLE_OBJS)
			$(CC) $(LDFLAGS) -o $@ $^ $(LDLIBS)

$(SERVER_BINS):		$(SERVER_OBJS)
			$(CC) $(LDFLAGS) -o $@ $^ $(LDLIBS)

$(CLIENT_BINS):		$(CLIENT_OBJS)
			$(CC) $(LDFLAGS) -o $@ $^ $(LDLIBS)

$(EXAMPLE_OBJS):	$(EXAMPLE_SRCS)
$(SERVER_OBJS):		$(SERVER_SRCS) $(SERVER_BIND)
$(CLIENT_OBJS):		$(CLIENT_SRCS) $(CLIENT_BIND)

dbus/s_g_data_transfer.h:	dbus/s_g_data_transfer.xml 
	dbus-binding-tool --mode=glib-server --prefix=s_g_data_transfer dbus/s_g_data_transfer.xml > dbus/s_g_data_transfer.h

dbus/s_g_transport.h:		dbus/s_g_transport.xml 
	dbus-binding-tool --mode=glib-client dbus/s_g_transport.xml > dbus/s_g_transport.h

# EOF

