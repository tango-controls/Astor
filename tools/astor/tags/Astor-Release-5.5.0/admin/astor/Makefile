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
# Revision 3.60  2011/02/11 10:00:26  pascal_verdier
# Pb with TAC when adding addresses on "All Users" fixed.
# No reference on app_util classes any more.
# Change splash screen image.
#
# Revision 3.59  2011/01/13 13:04:17  pascal_verdier
# Increase release number
#
# Revision 3.58  2011/01/10 12:48:13  pascal_verdier
# TAC is now displayed as database servers.
# StartServersAtStarteup starter class property management added.
# Display access mode in Tango Access panel.
#
# Revision 3.57  2011/01/04 14:33:03  pascal_verdier
# Password added for access control dialog.
# Do not try to subscribe on Starter events if starter device not exported.
#
# Revision 3.56  2010/11/30 09:27:47  pascal_verdier
# For multi servers command, if the command is done throu the starter,
# it is done by a thread and a delay has been added between servers.
#
# Revision 3.55  2010/11/29 13:54:45  pascal_verdier
# Multi servers command added.
# Uptime for servers added.
#
# Revision 3.54  2010/10/08 07:41:27  pascal_verdier
# Minor changes.
#
# Revision 3.53  2010/06/17 08:48:11  pascal_verdier
# Pb on display startup level dialog in case of many devices fixed.
#
# Revision 3.52  2010/06/17 07:41:22  pascal_verdier
# Start new server can take several servers (multiple selection).
#
# Revision 3.51  2010/06/04 14:12:55  pascal_verdier
# Global command to change startup level added.
#
# Revision 3.50  2010/04/08 10:41:47  pascal_verdier
# Minor changes.
#
# Revision 3.49  2010/01/05 13:47:22  pascal_verdier
# Best management of subscribe error window at startup.
#
# Revision 3.48  2009/06/02 15:19:05  pascal_verdier
# Remove serialization between HostStateThread and HostInfoDialogVector.
#
# Revision 3.47  2008/11/19 09:59:56  pascal_verdier
# New tests done on Access control.
# Pool Threads management added.
# Size added as preferences.
#
# Revision 3.46  2008/09/12 11:51:23  pascal_verdier
# Minor changes
#
# Revision 3.45  2008/06/16 11:50:39  pascal_verdier
# Level trees are now displayed on 2 rows.
#
# Revision 3.44  2008/05/26 11:49:12  pascal_verdier
# Host info dialog servers are managed in a jtree.
#
# Revision 3.43  2008/04/07 10:53:35  pascal_verdier
# Branch info modified.
#
# Revision 3.42  2008/03/27 08:07:15  pascal_verdier
# Compatibility with Starter 4.0 and after only !
# Better management of server list.
# Server state MOVING managed.VacGaugeServer
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


MAJOR_REV   = 5
MIDDLE_REV  = 5
MINOR_REV   = 0

APPLI_VERS	=	$(MAJOR_REV).$(MIDDLE_REV).$(MINOR_REV)
SVN_TAG_REV =	Release-$(MAJOR_REV)_$(MIDDLE_REV)_$(MINOR_REV)

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
