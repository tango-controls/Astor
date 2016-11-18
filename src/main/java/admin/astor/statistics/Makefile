#+======================================================================
# $Source$
#
# Project:      Tango Device Server
#
# Description:  Makefile to generate the JAVA Tango classes package
#
# $Author: pascal_verdier $
#
# $Version$
#
# $Log$
#
#-======================================================================


TANGO_HOME     = /segfs/tango

# -----------------------------------------------------------------
#
#		The compiler flags
#
#------------------------------------------------------------------

BIN_DIR   = ../../../../bin
JAVAFLAGS = -deprecation -d $(BIN_DIR)
JAVAC = javac $(JAVAFLAGS)

#-------------------------------------------------------------------


#-----------------------------------------------------------------

all:	 trace classes  exe



trace:
	@echo $(CLASSPATH)

classes:
	$(JAVAC) *.java

exe:
	@./go

clean:
	rm  -Rf $(BIN_DIR)/admin/astor/statistics

