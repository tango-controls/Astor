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
# copyleft :    European Synchrotron Radiation Facility
#               BP 220, Grenoble 38043
#               FRANCE
#
#-======================================================================


PACKAGE    = Astor
TANGO_HOME = /segfs/tango
DOC_ORIG   = $(TANGO_HOME)/tools/admin/doc
DOC_HOME   = $(TANGO_HOME)/doc/www/tango/tango_doc/tools_doc/astor_doc/
JAR_DIR    = $TANGO_JAR_HOME/astor

# -----------------------------------------------------------------
#
#		The compiler flags
#
#------------------------------------------------------------------

BIN_DIR   = /segfs/tango/tools/admin/astor/bin
JAVAFLAGS =-deprecation -d $(BIN_DIR)
JAVAC = javac $(JAVAFLAGS)

#-------------------------------------------------------------------


#-----------------------------------------------------------------

all:	 trace $(PACKAGE)  exe

trace:
	@echo $(CLASSPATH)

$(PACKAGE):
	$(JAVAC) *.java

exe:
	@./go

clean:
	rm  -Rf $(BIN_DIR)/admin/astor/tango_release

