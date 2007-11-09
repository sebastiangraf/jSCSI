# -----------------------------------------------------------------------------
# Date:     Thu Nov  8 18:01:08 CST 2007
# Author:   John Quigley <jquigley@cleversafe.com>
# Revision: $Id$
# -----------------------------------------------------------------------------

SHELL = /bin/sh
CC    = gcc
CTAGS = ctags -x > tags

DEFS    = -DDEBUG
FLAGS   = -O2 -Wall -std=c99 -g
CFLAGS  = $(FLAGS) $(DEFS) -I.	-I/usr/lib/dbus-1.0/include \
				-I/usr/include/dbus-1.0     \
				-I/usr/include/glib-2.0     \
				-I/usr/lib/glib-2.0/include
LDLIBS  = -ldbus-1 -ldbus-glib-1
LDFLAGS = $(FLAGS)

prefix = /usr/local
bindir = $(prefix)/bin

.SUFFIXES:
.SUFFIXES: .c .o

##### Do not edit below this line, please. #####

SERVER_SRCS = $(addprefix dbus/, server.c)
SERVER_BIND = $(addprefix dbus/, s_g_data_transfer.h s_g_transport.h)
SERVER_OBJS = $(addprefix dbus/, server.o)
SERVER_BINS = server

EXAMPLE_OBJS = $(addprefix dbus/, dbus-glib-example.o)
EXAMPLE_SRCS = $(addprefix dbus/, dbus-glib-example.c)
EXAMPLE_BINS = dbus-glib-example

OBJS = $(EXAMPLE_OBJS) $(SERVER_OBJS)
SRCS = $(EXAMPLE_SRCS) $(SERVER_SRCS) $(SERVER_BIND)
BINS = $(EXAMPLE_BINS) $(SERVER_BINS)

##### Phony rules

.PHONY:		all
all:		examples server

.PHONY:		all
examples:	$(EXAMPLE_BINS)

.PHONY:		tags
tags:		$(SRCS)

.PHONY:		clean
clean:
		rm -rf $(OBJS)
		rm -rf $(BINS)
		rm -rf $(SERVER_BIND)

##### Real rules

$(EXAMPLE_BINS):	$(EXAMPLE_OBJS)
			$(CC) $(LDFLAGS) -o $@ $^ $(LDLIBS)

$(SERVER_BINS):		$(SERVER_OBJS)
			$(CC) $(LDFLAGS) -o $@ $^ $(LDLIBS)

$(EXAMPLE_OBJS):	$(EXAMPLE_SRCS)
$(SERVER_OBJS):		$(SERVER_SRCS) $(SERVER_BIND)

dbus/s_g_data_transfer.h:
	dbus-binding-tool --mode=glib-server --prefix=s_g_data_transfer  dbus/s_g_data_transfer.xml > dbus/s_g_data_transfer.h

dbus/s_g_transport.h:
	dbus-binding-tool --mode=glib-client dbus/s_g_transport.xml > dbus/s_g_transport.h

# EOF

