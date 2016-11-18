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
MINOR_REV   = 1

APPLI_VERS	=	$(MAJOR_REV).$(MIDDLE_REV).$(MINOR_REV)
SVN_TAG_REV =	Release-$(MAJOR_REV).$(MIDDLE_REV).$(MINOR_REV)

PACKAGE        = Astor
TANGO_HOME     = /segfs/tango
SVN_ASTOR_PATH = $(SVN_TCS)/tools/astor
DOC_ORIG   = $(TANGO_HOME)/tools/admin/astor/doc
DOC_HOME   = $(TANGO_HOME)/doc/www/tango/tango_doc/tools_doc/astor_doc/
JAR_DIR    = $(TANGO_HOME)/release/java/appli

# -----------------------------------------------------------------
#
#		The compiler flags
#
#------------------------------------------------------------------

BIN_DIR   = ../../../bin
JAVAFLAGS = -Xlint:unchecked -deprecation -d $(BIN_DIR)
JAVAC = javac $(JAVAFLAGS)

#-------------------------------------------------------------------


#-----------------------------------------------------------------

all:	 trace $(PACKAGE)  exe



trace:
	@echo $(CLASSPATH)

$(PACKAGE):
	$(JAVAC) *.java

utils:
	$(JAVAC) tools/*.java

tac:
	$(JAVAC) access/*.java

full:
	@make clean
	$(JAVAC) tools/*.java
	$(JAVAC) access/*.java
	$(JAVAC) statistics/*.java
	$(JAVAC) tango_release/*.java
	$(JAVAC) *.java
	@cp tango_release/*.html $(BIN_DIR)/admin/astor/tango_release

images:
	cp -R img $(BIN_DIR)/admin/astor

exe:
	@./astor

clean:
	rm  -Rf $(BIN_DIR)/admin/astor/tools
	rm  -Rf $(BIN_DIR)/admin/astor/access
	rm  -Rf $(BIN_DIR)/admin/astor/statistics
	rm  -f  $(BIN_DIR)/admin/astor/tango_release/*.class
	rm  -f  $(BIN_DIR)/admin/astor/*.class

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


#	$(JAVA) -classpath   /segfs/tango/release/java/astor/org.tango.pogo.jar  \
#		pogo.make_util.ReleaseNote2html -java  "Astor Release Note"   admin.astor
#	$(JAVAC) *.java

jar:
	@make version
	@make_jar $(PACKAGE) $(APPLI_VERS) $(JAR_DIR)

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

tag:
	@echo "Tagging  $(PACKAGE)  for $(SVN_TAG_REV)"
	svn copy  $(SVN_ASTOR_PATH)/trunk \
	          $(SVN_ASTOR_PATH)/tags/$(PACKAGE)-$(SVN_TAG_REV)
