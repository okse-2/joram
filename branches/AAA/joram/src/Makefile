# Copyright (C) 1996 - 2000 BULL
# Copyright (C) 1996 - 2000 INRIA
#
# The contents of this file are subject to the Joram Public License,
# as defined by the file JORAM_LICENSE.TXT 
# 
# You may not use this file except in compliance with the License.
# You may obtain a copy of the License on the Objectweb web site
# (www.objectweb.org). 
# 
# Software distributed under the License is distributed on an "AS IS" basis,
# WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
# the specific terms governing rights and limitations under the License. 
# 
# The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
# fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
# released May 24, 2000. 
# 
# The Initial Developer of the Original Code is Dyade. The Original Code and
# portions created by Dyade are Copyright Bull and Copyright INRIA.
# All Rights Reserved.
#

PACKAGES = fr

SHIP_LIBRARIES = a3rt.jar nwlogs.jar ogc.jar ocl.jar

build_all: all test ship_all scripts doc_protected doc_private
ship_all: ship_dtd $(SHIP_LIBRARIES:%.jar=ship_%)

a3rt.jar_LIBRARIES = \
	a3util.jar \
	a3agent.jar \
	a3ip.jar \
	a3task.jar \
	a3ns.jar \
	a3config.jar

ocl.jar_LIBRARIES = \
	a3model.jar \
	a3sconf.jar \
	a3generation.jar \
	a3dt.jar

ogc.jar_LIBRARIES = \
	a3model.jar \
	a3editors.jar \
	a3sconf.jar \
	a3generation.jar \
	a3gct.jar \
	a3dt.jar \
	a3gdt.jar

nwlogs.jar_LIBRARIES = \
	a3ar.jar \
	a3nwar.jar \
	a3nwtest.jar

# ships the H323 plug-in
# builds a single jar file and a single shared library
h323_LIBRARIES = \
	a3agent.jar \
	a3task.jar \
	a3ip.jar \
	a3nw.jar \
	a3plugin.jar \
	a3h323.jar
h323_JNI_LIBRARIES = \
	liba3plugin.so \
	liba3h323.so

scripts:
	cd fr/dyade/aaa/nw/ar; $(MAKE) configs scripts

# collects all the dtd addenda
ship_dtd:
	@echo Building $(SHIP)/OCLscript.dtd
	@echo '<?xml encoding="US-ASCII"?>' > $(SHIP)/OCLscript.dtd
	@extensions="void"; \
	for f in `find . -name OCL\*Script.dtd -print`; \
	do \
	  ext=`expr $$f : ".*/OCL\(.*\)Script.dtd"`; \
	  echo $$ext; \
	  grep "<!ENTITY % $${ext}Element " $$f > /dev/null || ( \
	    echo "Missing definition of entity $${ext}Element in $$f."; \
	    false ); \
	  echo >> $(SHIP)/OCLscript.dtd; \
	  echo "<!-- OCLscript dtd extension $$ext -->" \
		>> $(SHIP)/OCLscript.dtd; \
	  cat $$f >> $(SHIP)/OCLscript.dtd; \
	  extensions="$$extensions | %$${ext}Element;"; \
	done; \
	echo >> $(SHIP)/OCLscript.dtd; \
	echo "<!-- OCLscript dtd list of extensions -->" \
		>> $(SHIP)/OCLscript.dtd; \
	echo "<!ENTITY % extension '$$extensions'>" \
		>> $(SHIP)/OCLscript.dtd; \
	cat fr/dyade/aaa/services/generation/OCLscript.dtd >> $(SHIP)/OCLscript.dtd

DOC_LIBS = xerces.jar:swing.jar:swingall.jar:OB.jar:OBNaming.jar:jndi.jar:jms.jar
DYADE_DOC_DIR = /infosystem/www/htdocs/aaa/private/aaadoc
doc_dyade:
	cp -rp $(ROOTDIR)/doc_protected $(DYADE_DOC_DIR)/apidoc.new
	/bin/rm -fr $(DYADE_DOC_DIR)/apidoc
	mv $(DYADE_DOC_DIR)/apidoc.new $(DYADE_DOC_DIR)/apidoc
	cp -rp $(ROOTDIR)/doc_private $(DYADE_DOC_DIR)/apidoc_private.new
	/bin/rm -fr $(DYADE_DOC_DIR)/apidoc_private
	mv $(DYADE_DOC_DIR)/apidoc_private.new $(DYADE_DOC_DIR)/apidoc_private

ship_h323:
	-rm -fr $(SHIP)/tmp
	-mkdir -p $(SHIP)/tmp/fr
	cd $(SHIP)/tmp; \
	for j in $(h323_LIBRARIES); do \
	  jar xf $(LIBDIR://$(DRIVE)%=$(DRIVE):%)/$$j; \
	done
	cd $(SHIP)/tmp; jar cMf $(SHIP://$(DRIVE)%=$(DRIVE):%)/nwh323.jar *
	cp -f $(h323_JNI_LIBRARIES:%=$(LIBDIR)/%) $(SHIP)
	cp -f $(SRCDIR)/fr/dyade/aaa/nw/plugin/h323/h323plugin.sh $(SHIP)
	-rm -fr $(SHIP)/tmp

$(SHIP_LIBRARIES:%.jar=ship_%): ship_%: $(SHIP)/%.jar
$(SHIP)/a3rt.jar: $(a3rt.jar_LIBRARIES:%=$(LIBDIR)/%)
$(SHIP)/ocl.jar: $(ocl.jar_LIBRARIES:%=$(LIBDIR)/%)
$(SHIP)/ogc.jar: $(ogc.jar_LIBRARIES:%=$(LIBDIR)/%)
$(SHIP)/nwlogs.jar: $(nwlogs.jar_LIBRARIES:%=$(LIBDIR)/%)
$(SHIP_LIBRARIES:%= $(SHIP)/%):
	-rm -fr $(SHIP)/tmp
	-mkdir -p $(SHIP)/tmp
	cd $(SHIP)/tmp; \
	for j in $($(@:$(SHIP)/%=%)_LIBRARIES); do \
	  jar xf $(LIBDIR://$(DRIVE)%=$(DRIVE):%)/$$j; \
	done
	cd $(SHIP)/tmp; jar cMf $(@://$(DRIVE)%=$(DRIVE):%) *
	-rm -fr $(SHIP)/tmp

include $(GENERAL_MK)
