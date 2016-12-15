#+======================================================================
# $Source: $
#
# Project:      Tango Device Server
#
# Description:  Makefile to patch the revision notes in java source classes
#
# $Author$
#
# $Revision$
#
#-======================================================================


MAJOR_REV   = 7
MIDDLE_REV  = 0
MINOR_REV   = 6

APPLI_VERS	=	$(MAJOR_REV).$(MIDDLE_REV).$(MINOR_REV)

MAIN_CLASS = Astor
PACKAGE = admin.astor
DOC_HOME = ./doc

TANGO_HOME     = /segfs/tango
#-------------------------------------------------------------------
# Update version in application and generate ReleaseNote.java
#-----------------------------------------------------------------

POGO = $(TANGO_HOME)/release/java/appli/org.tango.pogo.jar
UPDATE_CLASS =  org.tango.pogo.pogo_gui.tools.UpdateRelease
SRC_HOME=src/main/java/admin/astor
version:
	echo "Updating date and revision number..."
	java  -cp $(POGO)   $(UPDATE_CLASS) \
		-file $(SRC_HOME)/$(MAIN_CLASS).java \
		-release $(APPLI_VERS) \
		-title   "$MAIN_CLASS) Release Notes" \
		-package $(PACKAGE) \
		-note_path $(SRC_HOME)
