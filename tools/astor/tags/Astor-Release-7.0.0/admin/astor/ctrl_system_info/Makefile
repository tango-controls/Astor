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
	rm  -Rf $(BIN_DIR)/admin/astor/ctrl_system_info
