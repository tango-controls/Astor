#+======================================================================
# $Source$
#
# Project:      Tango Device Server
#
# Description:  Makefile to generate the JAVA Tango classes package
#
# $Author$
#
# $Version$
#
# $Log$
# Revision 3.42  2008/03/27 08:07:15  pascal_verdier
# Compatibility with Starter 4.0 and after only !
# Better management of server list.
# Server state MOVING managed.
# Hard kill added on servers.
# New features on polling profiler.
#
# Revision 3.41  2008/03/03 14:55:21  pascal_verdier
# Starter Release_4 management.
#
# Revision 3.40  2007/08/20 14:06:08  pascal_verdier
# ServStatePanel added on HostInfoDialog (Check states option).
#
# Revision 3.39  2007/04/04 13:00:26  pascal_verdier
# Database attribute properties editor added.
#
# Revision 3.38  2007/03/27 08:56:11  pascal_verdier
# Preferences added.
#
# Revision 3.37  2007/03/08 13:44:32  pascal_verdier
# LastCollections property added.
#
# Revision 3.36  2007/01/08 08:21:07  pascal_verdier
# Disable Start Server button if Starter is MOVING.
#
# Revision 3.35  2006/09/19 13:04:46  pascal_verdier
# Access control manager added.
#
# Revision 3.34  2006/06/26 12:24:08  pascal_verdier
# Bug fixed in miscellaneous host collection.
#
# Revision 3.33  2006/06/13 13:52:14  pascal_verdier
# During StartAll command, sleep(500) added between 2 hosts.
# MOVING states added for collection.
#
# Revision 3.32  2006/05/17 07:52:21  pascal_verdier
# MOVING state added.
#
# Revision 3.31  2006/04/19 12:06:58  pascal_verdier
# Host info dialog modified to use icons to display server states.
#
# Revision 3.30  2006/04/12 13:38:37  pascal_verdier
# *** empty log message ***
#
# Revision 3.29  2006/01/11 08:46:13  pascal_verdier
# PollingProfiler added.
#
# Revision 3.28  2005/12/01 10:00:23  pascal_verdier
# Change TANGO_HOST added (needs TangORB-4.7.7 or later).
#
# Revision 3.27  2005/11/25 07:43:28  pascal_verdier
# Host panel can be opened from DevBrowser.
#
# Revision 3.26  2005/11/24 12:24:57  pascal_verdier
# DevBrowser utility added.
# MkStarter utility added.
#
# Revision 3.25  2005/11/17 12:30:33  pascal_verdier
# Analysed with IntelliJidea.
#
# Revision 3.24  2005/10/19 09:50:15  pascal_verdier
# path changed.
#
# Revision 3.23  2005/10/17 14:12:13  pascal_verdier
# *** empty log message ***
#
# Revision 3.22  2005/10/14 14:30:45  pascal_verdier
# rcp ReleaseNotes added.
#
# Revision 3.21  2005/09/15 13:43:01  pascal_verdier
# *** empty log message ***
#
# Revision 3.20  2005/04/25 08:55:36  pascal_verdier
# Start/Stop servers from shell command line added.
#
# Revision 3.19  2005/03/15 10:22:31  pascal_verdier
# Sort servers before creating panel buttons.
#
# Revision 3.18  2005/03/11 14:07:54  pascal_verdier
# Pathes have been modified.
#
# Revision 3.17  2005/02/10 15:38:19  pascal_verdier
# Event subscritions have been serialized.
#
# Revision 3.16  2005/02/03 13:31:58  pascal_verdier
# Display message if subscribe event failed.
# Display hosts using events (Starter/Astor).
#
# Revision 3.15  2005/01/18 08:48:19  pascal_verdier
# Tools menu added.
# Not controlled servers list added.
#
# Revision 3.14  2004/12/08 09:54:06  pascal_verdier
# *** empty log message ***
#
# Revision 3.13  2004/11/23 14:07:22  pascal_verdier
# Minor changes.
#
# Revision 3.12  2004/09/28 07:01:51  pascal_verdier
# Problem on two events server list fixed.
#
# Revision 3.11  2004/07/09 08:12:49  pascal_verdier
# HostInfoDialog is now awaken only on servers change.
#
# Revision 3.9  2004/06/17 09:19:58  pascal_verdier
# Refresh performence problem solved by removing tool tips on JTree.
#
# Revision 3.8  2004/04/13 12:17:29  pascal_verdier
# DeviceTree class uses the new browsing database commands.
#
# Revision 3.7  2004/03/03 08:31:04  pascal_verdier
# The server restart command has been replaced by a stop and start command in a thread.
# The delete startup level info has been added.
#
# Revision 3.6  2004/02/04 14:37:43  pascal_verdier
# Starter logging added
# Database info added on CtrlServersDialog.
#
# Revision 3.5  2003/11/25 15:56:45  pascal_verdier
# Label on hosts added.
# Notifyd begin to be controled.
#
# Revision 3.4  2003/11/07 09:58:46  pascal_verdier
# Host info dialog automatic resize implemented.
#
# Revision 3.3  2003/10/20 08:55:15  pascal_verdier
# Bug on tree popup menu position fixed.
#
# Revision 3.2  2003/09/08 11:05:28  pascal_verdier
# *** empty log message ***
#
# Revision 3.1  2003/06/19 12:57:57  pascal_verdier
# Add a new host option.
# Controlled servers list option.
#
# Revision 3.0  2003/06/04 12:37:52  pascal_verdier
# Main window uses now a Jtree to display hosts.
#
# Revision 2.1  2003/06/04 12:33:11  pascal_verdier
# Main window uses now a Jtree to display hosts.
#
# Revision 2.0  2003/01/16 15:22:35  verdier
#
# copyleft :    European Synchrotron Radiation Facility
#               BP 220, Grenoble 38043
#               FRANCE
#
#-======================================================================

APPLI_VERS	=	5.0.1

PACKAGE    = Astor
TANGO_HOME = /segfs/tango
DOC_ORIG   = $(TANGO_HOME)/tools/admin/doc
DOC_HOME   = $(TANGO_HOME)/doc/www/tango/tango_doc/tools_doc/astor_doc/
JAR_DIR    = $(TANGO_JAR_HOME)/appli

# -----------------------------------------------------------------
#
#		The compiler flags
#
#------------------------------------------------------------------

BIN_DIR   = /segfs/tango/tools/bin
JAVAFLAGS = -g -deprecation -d $(BIN_DIR)
#JAVAC = javac $(JAVAFLAGS)
JAVAC = jmake $(JAVAFLAGS)

#-------------------------------------------------------------------


#-----------------------------------------------------------------

all:	 trace $(PACKAGE)  exe

trace:
	@echo $(CLASSPATH)

$(PACKAGE):
	$(JAVAC) *.java
#	cd tools; $(JAVAC) *.java

exe:
	@./astor
#	java -DTANGO_HOST=corvus:10000 admin.astor.tools/DevBrowser
#	java -DTANGO_HOST=orion:11000 admin.astor.ServArchitectureDialog

clean:
	rm  -Rf $(BIN_DIR)/admin/astor

MAIN_CLASS=		$(PACKAGE)
jar :
	echo "Updating date and revision number..."
	upd_rev  -f $(MAIN_CLASS).java  -r $(APPLI_VERS)
	java pogo.make_util.ReleaseNote2html -java  "Astor Release Note"   admin.astor
	$(JAVAC) *.java
	@make_jar $(PACKAGE) $(APPLI_VERS) $(JAR_DIR)

install_doc:
	java pogo.make_util.ReleaseNote2html -html  "Astor Release Note"
	mv ReleaseNote.html $(DOC_ORIG)
	cp $(DOC_ORIG)/*.html    $(DOC_HOME)/
	cp $(DOC_ORIG)/img/*     $(DOC_HOME)/img
	cp $(DOC_ORIG)/movies/*  $(DOC_HOME)/movies
	tango2www astor

FTP_TARGET	=	mars:/ftp/pub/cs/tango/Astor
JAR_FILE	=	Astor-$(APPLI_VERS).jar
install_ftp:
	@echo "rcp $(JAR_FILE) $(FTP_TARGET)"
	@cd $(JAR_DIR); rcp $(JAR_FILE) $(FTP_TARGET)
	rcp ReleaseNote $(FTP_TARGET)


OPERATION = /operation/dserver/java/appli
install_op:
	@segfs2operation $(JAR_DIR) $(MAIN_CLASS) $(APPLI_VERS) $(OPERATION)
