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


MAJOR_REV   = 5
MIDDLE_REV  = 5
MINOR_REV   = 4

APPLI_VERS	=	$(MAJOR_REV).$(MIDDLE_REV).$(MINOR_REV)
SVN_TAG_REV =	Release-$(MAJOR_REV).$(MIDDLE_REV).$(MINOR_REV)

PACKAGE        = Astor
TANGO_HOME     = /segfs/tango
SVN_ASTOR_PATH = $(SVN_TCS)/tools/astor
DOC_ORIG   = $(TANGO_HOME)/tools/admin/doc
DOC_HOME   = $(TANGO_HOME)/doc/www/tango/tango_doc/tools_doc/astor_doc/
JAR_DIR    = $(TANGO_HOME)/release/java/appli

# -----------------------------------------------------------------
#
#		The compiler flags
#
#------------------------------------------------------------------

BIN_DIR   = ../../bin
JAVAFLAGS = -deprecation -d $(BIN_DIR)
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
	$(JAVAC) *.java

images:
	cp -R img $(BIN_DIR)/admin/astor

exe:
	@./astor

clean:
	rm  -Rf $(BIN_DIR)/admin/astor/tools
	rm  -Rf $(BIN_DIR)/admin/astor/access
	rm  -Rf $(BIN_DIR)/admin/astor/statistics
	rm      $(BIN_DIR)/admin/astor/*.class

JAVA=/segfs/tango/jdk/Linux/jdk1.6.0/bin/java
MAIN_CLASS=		$(PACKAGE)
version:
	echo "Updating date and revision number..."
	upd_rev  -f $(MAIN_CLASS).java  -r $(APPLI_VERS)
	$(JAVA) -classpath   /segfs/tango/release/java/appli/org.tango.pogo.jar  \
		pogo.make_util.ReleaseNote2html -java  "Astor Release Note"   admin.astor
	$(JAVAC) *.java

jar:
	@make version
	@make_jar $(PACKAGE) $(APPLI_VERS) $(JAR_DIR)

install_doc:
	$(JAVA) pogo.make_util.ReleaseNote2html -html  "Astor Release Note"
	mv ReleaseNote.html $(DOC_ORIG)
	cp $(DOC_ORIG)/*.html    $(DOC_HOME)/
	cp $(DOC_ORIG)/img/*     $(DOC_HOME)/img
	cp $(DOC_ORIG)/movies/*  $(DOC_HOME)/movies
	tango2www astor

FTP_TARGET	=	maya:/ftp/pub/cs/tango/Astor
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
