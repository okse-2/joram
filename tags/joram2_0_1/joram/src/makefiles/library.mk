# Copyright (C) 1996 - 2000 BULL
# Copyright (C) 1996 - 2000 INRIA

# This makefile is included in general.mk whenever the LIBRARIES variable
# is defined.
# For example, if you want to build a library 'foo.jar' from files 'a.java'
# and sub package 'bar' your Makefile should look like this:
# ----------
# LIBRARIES = foo.jar
# foo.jar_CLASSES = a
# foo.jar_PACKAGES = bar
# include $(GENERAL_MK)
# ----------
#
# This file defines the following targets:
# all:   to build the class files from the java files and build the library.
# clean: to clean all sub packages
# doc:   to build the appropriate documentation files from the source

_LIBRARIES_ = $(LIBRARIES:%=$(LIBDIR)/%)
all:: $(_LIBRARIES_)

_LIB_PACKAGES_ = $(foreach lib,$(LIBRARIES),$($(lib)_PACKAGES))
_SUBDIRS_ += $(_LIB_PACKAGES_)

_LIB_CLASSES_ = $(filter-out @% %@,$(subst @,@ @,\
	$(foreach lib,$(LIBRARIES),$($(lib)_CLASSES))))
ifneq "$(strip $(_LIB_CLASSES_))" ""
CLASSES += $(_LIB_CLASSES_)
endif

ifndef MAKE_DEPS
$(OBJDIR)/$(RELPATH)/libdep.mk: Makefile
	@mkdir -p $(OBJDIR)/$(RELPATH)
	@rm -f $@
	$(MAKE) MAKE_DEPS=1 $(LIBRARIES:%=libdep_%)
include $(OBJDIR)/$(RELPATH)/libdep.mk
else
$(LIBRARIES:%=libdep_%):
	echo "$(@:libdep_%=$(LIBDIR)/%): $($(@:libdep_%=%)_PACKAGES:%=SUBDIR_%_all)" >> $(OBJDIR)/$(RELPATH)/libdep.mk
	echo "$(@:libdep_%=$(LIBDIR)/%): $(patsubst $(_JAVA_CLASS_),$(_JAVA_OBJECT_),$(filter-out @% %@,$(subst @,@ @,$($(@:libdep_%=%)_CLASSES))))" >> $(OBJDIR)/$(RELPATH)/libdep.mk
endif

# Add a second space before all names, circumventing a known bug in make B20.1
# with no found solution
_LIBRARY_ = $(@:$(LIBDIR)/%=%)
_LIBRARY_CLASSES_ = $(subst @,,$(filter-out %@,$(subst @,@ @,\
		      $($(_LIBRARY_)_CLASSES))))
# take inner classes into account
$(_LIBRARIES_:%= %):
	@rm -f $@
	cd $(OBJDIR);\
	  jar cf0 $(@://$(DRIVE)%=$(DRIVE):%) \
	    $($(_LIBRARY_)_PACKAGES:%=$(RELPATH)/%) \
	    $(_LIBRARY_CLASSES_:%=$(RELPATH)/%.class) \
	    $(foreach class,$(_LIBRARY_CLASSES_),\
	      $(subst $$,\$$,\
		$(patsubst $(OBJDIR)/%,%,\
		  $(wildcard $(OBJDIR)/$(RELPATH)/$(class)\$$*.class))))

colldoc clean test:: %: $(foreach pkg,$(_LIB_PACKAGES_),SUBDIR_$(pkg)_%)
clean::
	-rm -f $(_LIBRARIES_)

$(LIBRARIES): %: $(LIBDIR)/%

.PHONY: $(LIBRARIES)
