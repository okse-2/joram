# Copyright (C) 1996 - 2000 BULL
# Copyright (C) 1996 - 2000 INRIA
# 

#
# this file should be included in all Makefiles with the command
# include $(GENERAL_MK). It assumes some variables are defined,
# for example by executing the rc_files/*/local.sh command file.
# ROOTDIR= <the directory parent of the makefiles directory>
# GENERAL_MK= $ROOTDIR/makefiles/general.mk
# JAVAPATH= <where to find the Java base classes - classes.zip>
# PATH_SEP= <: on Unix, ; on Windows>
#

help:
	@echo general targets are: clean all test doc wc

PWD := $(shell pwd)
WINPWD := $(filter //%,$(PWD))
ifdef WINPWD
DRIVE := $(word 1,$(subst /, ,$(PWD)))
#PWD := $(DRIVE):$(patsubst //$(DRIVE)%,%,$(PWD))
endif

CLASSPATH := $(CLASSPATH:%=%$(PATH_SEP))$(JAVAPATH)$(PATH_SEP)$(SRCDIR://$(DRIVE)%=$(DRIVE):%)
ifndef JAVA
JAVA = java
endif
ifndef JAVAC
JAVAC = javac
endif
ifndef JVERSION
JVERSION := $(shell $(JAVA) -version 2>&1 |\
	(read version; expr "$$version" : 'java version "\(.*\)"'))
JREVISION := $(word 2,$(subst ., ,$(JVERSION)))
endif
ifdef PACKAGE
RELPATH := $(subst .,/,$(PACKAGE))
else
RELPATH := $(patsubst /%,%,$(patsubst $(SRCDIR)%,%,$(PWD)))
PACKAGE := $(subst /,.,$(RELPATH))
endif

_JAVA_CLASS_ = %
_JAVA_SOURCE_ = %.java
_JAVA_OBJECT_ = $(OBJDIR)/$(RELPATH)/%.class
_JFLEX_SOURCE_ = %.flex
_JCUP_SOURCE_ = %.cup
_JCUP_PARSER_ = %Parser
_JCUP_PARSER_SOURCE_ = %Parser.java
_JCUP_PARSER_OBJECT_ = $(OBJDIR)/$(RELPATH)/%Parser.class
_JCUP_SYMBOLS_ = %Symbols
_JCUP_SYMBOLS_SOURCE_ = %Symbols.java
_JCUP_SYMBOLS_OBJECT_ = $(OBJDIR)/$(RELPATH)/%Symbols.class


INT_LIST := \
	    1  2  3  4  5  6  7  8  9 \
	10 11 12 13 14 15 16 17 18 19 \
	20 21 22 23 24 25 26 27 28 29 \
	30 31 32 33 34 35 36 37 38 39 \
	40 41 42 43 44 45 46 47 48 49 \
	50 51 52 53 54 55 56 57 58 59 \
	60 61 62 63 64 65 66 67 68 69 \
	70 71 72 73 74 75 76 77 78 79 \
	80 81 82 83 84 85 86 87 88 89 \
	90 91 92 93 94 95 96 97 98 99

clean::
	rm -f core *~

_TEST_SUBDIR_ := $(wildcard tests)
ifdef _TEST_SUBDIR_
_SUBDIRS_ += $(_TEST_SUBDIR_)
clean test:: %: SUBDIR_tests_%
endif

ifdef TESTS
include $(SRCDIR)/makefiles/test.mk
endif

ifdef LIBRARIES
include $(SRCDIR)/makefiles/library.mk
endif

ifdef JNI_LIBRARIES
include $(SRCDIR)/makefiles/jnilibrary.mk
endif

# either SOURCES or CLASSES may be defined
ifdef SOURCES
include $(SRCDIR)/makefiles/file.mk
else
ifdef CLASSES
include $(SRCDIR)/makefiles/file.mk
else
ifdef JCUP
include $(SRCDIR)/makefiles/file.mk
endif
endif
endif

ifdef PACKAGES
_SUBDIRS_ += $(PACKAGES)
endif
ifdef _SUBDIRS_
include $(SRCDIR)/makefiles/package.mk
endif

cleandoc::
	-rm -f $(OBJDIR)/doc_packages

ifndef JAVADOC
JAVADOC = javadoc
ifeq ($(JREVISION), 2)
ifndef JDKDOC
JDKDOC = http://dyade/aaa/public/java/jdk1.2.1/api
endif
JAVADOC := $(JAVADOC) -link $(JDKDOC)
endif
endif

ifndef _CLASSPATH_
# from file.mk
_CLASSPATH_ = $(CLASSPATH)
endif
ifdef DOC_CLASSPATH
_DOC_CLASSPATH_ = $(_CLASSPATH_)$(PATH_SEP)$(DOC_CLASSPATH)
else
ifdef DOC_LIBS
_DOC_CLASSPATH_ = $(_CLASSPATH_)$(PATH_SEP)$(LIBDIR://$(DRIVE)%=$(DRIVE):%)/$(subst :,$(PATH_SEP)$(LIBDIR://$(DRIVE)%=$(DRIVE):%)/,$(DOC_LIBS))
else
_DOC_CLASSPATH_ = $(_CLASSPATH_)
endif
endif
_DOC_PACKAGES_ = $(shell cat $(OBJDIR)/doc_packages)
_DOC_OPTIONS_ = public protected package private
$(_DOC_OPTIONS_:%=doc_%): cleandoc colldoc
	-rm -fr $(ROOTDIR)/$@/$(patsubst ,.,$(RELPATH))
	@mkdir -p $(ROOTDIR)/$@/$(patsubst ,.,$(RELPATH))
	cd $(ROOTDIR)/src; \
	$(JAVADOC) -classpath '$(_DOC_CLASSPATH_)' \
	  -d $(ROOTDIR://$(DRIVE)%=$(DRIVE):%)/$@/$(patsubst ,.,$(RELPATH)) \
	  -$(@:doc_%=%) \
	  $(subst private,-author,$(filter private,$(@:doc_%=%))) \
	  $(_DOC_PACKAGES_)
doc:: doc_protected doc_private

wc:
	@lwc() { \
	  ldir=$$1; shift; \
	  FC=0; LC=0; \
	  files=`ls $$ldir/*.java 2>/dev/null`; \
	  if [ $$? -eq 0 ]; then \
	    FC0=`echo "$$files" | wc -l`; \
	    FC=`expr $$FC + $$FC0`; \
	    LC0=`wc -l $$files | \
	      awk '{if (NR==1) res=$$1} \
		   /total$$/ {res=$$1} \
		   END {print res}'`; \
	    LC=`expr $$LC + $$LC0`; \
	  fi; \
	  dirs=`ls -l $$ldir | sed -n '/^d/s/.* //p'`; \
	  if [ ! -z "$$dirs" ]; then \
	    for dir in $$dirs; do \
	      set `lwc $$ldir/$$dir`; \
	      FC=`expr $$FC + $$1`; \
	      LC=`expr $$LC + $$2`; \
	    done; \
	  fi; \
	  echo $$FC $$LC; \
	}; \
	lwc .
