# Copyright (C) 1996 - 2000 BULL
# Copyright (C) 1996 - 2000 INRIA
# 
# This makefile is included in general.mk whenever the PACKAGES variable
# is defined to the set of packages defined in the directory.
# So, if you have a 'foo' package, included in 'w3c' and containing 'bar1'
# and 'bar2' sub packages, your Makefile should look like this:
# ----------
# PACKAGES=bar1 bar2
# include $(GENERAL_MK)
# ----------
# The PACKAGE variable is already set in general.mk to w3c.foo.
#
# This make file is also indirectly included in library.mk.
#
# This make file defines the following targets:
# all:   to build the class files from the java files.
# clean: to clean all sub packages
# colldoc:   to build the appropriate documentation files from the source
# It also defines phony targets named by the sub package name, allowing
# to compile just the sub package.
# It also defines internal targets named SUBDIR_<dir>_<tgt>, used in library.mk
# to request compiling of a sub package.


TARGETS= init all lib colldoc clean clobber test

$(TARGETS):: %: $(foreach pkg,$(PACKAGES),SUBDIR_$(pkg)_%)
_SUBDIR_TARGET_ = $(subst _, ,$@)

$(foreach dir,$(_SUBDIRS_),$(foreach tgt,$(TARGETS),SUBDIR_$(dir)_$(tgt))):
	@if [ ! -d $(word 2,$(_SUBDIR_TARGET_)) ]; then \
	  echo skipping $(word 2,$(_SUBDIR_TARGET_)); \
	else \
	  cd $(word 2,$(_SUBDIR_TARGET_)) && \
	    $(MAKE) $(word 3,$(_SUBDIR_TARGET_)); \
	fi

$(PACKAGES): %: SUBDIR_%_all

.PHONY: $(PACKAGES)
