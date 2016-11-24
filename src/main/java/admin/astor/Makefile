#+======================================================================
# $Source: $
#
# Project:      Tango Device Server
#
# Description:  Makefile to generate the JAVA Tango classes package
#
# $Author$
#
# $Revision$
#
#-======================================================================


MAJOR_REV   = 7
MIDDLE_REV  = 0
MINOR_REV   = 2

APPLI_VERS	=	$(MAJOR_REV).$(MIDDLE_REV).$(MINOR_REV)

PACKAGE        = Astor
TANGO_HOME     = /segfs/tango
DOC_ORIG   = $(TANGO_HOME)/tools/admin/astor/doc
DOC_HOME   = $(TANGO_HOME)/doc/www/tango/tango_doc/tools_doc/astor_doc/
JAR_DIR    = $(TANGO_HOME)/release/java/appli
JAR_FILE = $(PACKAGE)-$(APPLI_VERS).jar

#-------------------------------------------------------------------
# Update version in application and generate ReleaseNote.java
#-----------------------------------------------------------------

MAIN_CLASS =	$(PACKAGE)
UPDATE_CLASS =  org.tango.pogo.pogo_gui.tools.UpdateRelease
version:
	echo "Updating date and revision number..."
	java  -cp /segfs/tango/release/java/appli/org.tango.pogo.jar \
		$(UPDATE_CLASS) \
		-file Astor.java \
		-release $(APPLI_VERS) \
		-title   "Astor Release Notes" \
		-package admin.astor


#-------------------------------------------------------------------
# Install jar file
#-----------------------------------------------------------------
MVN_TARGET=../../../../../release/target/checkout/target
install::
	mv $(MVN_TARGET)/$(JAR_FILE) $(JAR_DIR)
	cd $(JAR_DIR); \
		rm -f $(PACKAGE).jar ; \
		ln -s $(JAR_FILE)  $(PACKAGE).jar
	ls -l  $(JAR_DIR)/$(PACKAGE)*

install_doc:
	java -classpath /segfs/tango/release/java/astor/org.tango.pogo.jar  \
					pogo.make_util.ReleaseNote2html -html  "Astor Release Note"
	mv ReleaseNote.html $(DOC_ORIG)
	cp $(DOC_ORIG)/*.html    $(DOC_HOME)/
	cp $(DOC_ORIG)/*.pdf    $(DOC_HOME)/
	cp $(DOC_ORIG)/img/*     $(DOC_HOME)/img
	cp $(DOC_ORIG)/movies/*  $(DOC_HOME)/movies
	tango2www astor

FTP_TARGET	=	ftpserv:/ftp/anonymous/pub/cs/tango/Astor
JAR_FILE	=	Astor-$(APPLI_VERS).jar
install_ftp:
	@echo "rcp $(JAR_FILE) $(FTP_TARGET)"
	@cd $(JAR_DIR); rcp $(JAR_FILE) $(FTP_TARGET)
	rcp ReleaseNote $(FTP_TARGET)


OPERATION = /operation/dserver/java/appli
install_op:
	@segfs2operation $(JAR_DIR) $(MAIN_CLASS) $(APPLI_VERS) $(OPERATION)
