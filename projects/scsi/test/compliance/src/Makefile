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

EXAMPLE_OBJS = $(addprefix dbus/, dbus-glib-example.o)
EXAMPLE_SRCS = $(addprefix dbus/, dbus-glib-example.c)
EXAMPLE_BINS = dbus-glib-example

OBJS = $(EXAMPLE_OBJS)
SRCS = $(EXAMPLE_SRCS)
BINS = $(EXAMPLE_BINS)

##### Phony rules

.PHONY:		all
all:		examples

.PHONY:		all
examples:	dbus-glib-example

.PHONY:		tags
tags:		$(SRCS)

.PHONY:		clean
clean:
		rm -rf dbus-glib-example
		rm -rf $(EXAMPLE_OBJS)

##### Real rules

$(EXAMPLE_BINS):	$(EXAMPLE_OBJS)
			$(CC) $(LDFLAGS) -o $@ $^ $(LDLIBS)

# EOF

