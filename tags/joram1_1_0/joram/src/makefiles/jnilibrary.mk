# Copyright (C) 1996 - 2000 BULL
# Copyright (C) 1996 - 2000 INRIA

#
# JNI libraries are libxxx.so files built in the LIBDIR directory
# from a list of cccImp.o files listed in libxxx.so_OFILES variable
# and where ccc is a Java class name.
#
# the current version does not take Windows into account
#

_JNI_CLS_ = %
_JNI_SRC_ = %Imp.c
_JNI_INC_ = $(OBJDIR)/$(RELPATH)/%.h
_JNI_OBJ_ = $(OBJDIR)/$(RELPATH)/%Imp.o
_JNI_EXP_ = $(OBJDIR)/$(RELPATH)/%.exp

_JNI_LIBRARIES_ = $(JNI_LIBRARIES:%=$(LIBDIR)/%)
all:: $(_JNI_LIBRARIES_)

_JNI_LIB_OBJS_ = $(filter %Imp.o,$(foreach lib,$(JNI_LIBRARIES),$($(lib)_OFILES)))
_JNI_LIB_CLASSES_ = $(_JNI_LIB_OBJS_:%Imp.o=%)
_JNI_LIB_INCS_ = $(_JNI_LIB_CLASSES_:$(_JNI_CLS_)=$(_JNI_INC_))
_JNI_LIB_OBJECTS_ = $(_JNI_LIB_CLASSES_:$(_JNI_CLS_)=$(_JNI_OBJ_))
_JNI_LIB_EXPORTS_ = $(_JNI_LIB_CLASSES_:$(_JNI_CLS_)=$(_JNI_EXP_))

$(_JNI_LIB_INCS_): %.h: %.class
	cd $(OBJDIR); \
	  /bin/rm -f $@; \
	  javah -jni -o $@ -classpath '$(_CLASSPATH_):.' \
	    $(@:$(_JNI_INC_)=$(PACKAGE).$(_JNI_CLS_))

ifeq ($(TARGET_SYSTEM),UNIX)
JAVAINCDIR = /usr/local/java/include
CFLAGS= -g -I$(JAVAINCDIR) -I$(JAVAINCDIR)/aix \
	-I$(SRCDIR)/$(RELPATH) -I$(OBJDIR)/$(RELPATH) \
	-I$(SRCDIR) -I$(ROOTDIR)/include -DOFW
else
CFLAGS= -Id:/java/jdk/include -Id:/java/jdk/include/win32 \
	-I$(SRCDIR)/$(RELPATH) -I$(OBJDIR)/$(RELPATH) \
	-I$(SRCDIR) -I$(ROOTDIR)/include
endif

$(_JNI_LIB_OBJECTS_): $(_JNI_OBJ_): $(_JNI_SRC_) $(_JNI_INC_)
ifeq ($(TARGET_SYSTEM),UNIX)
	xlc_r -c -M $(CFLAGS) $($(notdir $@)_CFLAGS) -o $@ \
	  $(@:$(_JNI_OBJ_)=$(_JNI_SRC_))
else
# a confirmer ...
	cl $(CFLAGS) -Fo$@ $(@:$(_JNI_OBJ_)=$(_JNI_SRC_))
endif

$(_JNI_LIB_EXPORTS_): $(_JNI_EXP_): $(_JNI_INC_)
	grep " JNICALL " $< |\
	  sed "s/.* JNICALL //g" > $@

# generic dependencies cannot be expressed in dependency line !
# all dependencies are set for all libraries
_JNI_LIBRARY_OBJECTS_ = $($(@:$(LIBDIR)/%=%)_OFILES:%Imp.o=$(OBJDIR)/$(RELPATH)/%Imp.o)
_EXPORT_LINES_ = $(_JNI_LIBRARY_OBJECTS_:%Imp.o=-bE\:%.exp)
$(_JNI_LIBRARIES_): $(_JNI_LIB_OBJECTS_)
ifeq ($(TARGET_SYSTEM),UNIX)
$(_JNI_LIBRARIES_): $(_JNI_LIB_EXPORTS_)
$(_JNI_LIBRARIES_):
	ld -o $@ -bnoentry -bM:SRE \
	  -blibpath:/lib:/usr/lib -lc_r \
	  -blibpath:/usr/lib/threads:/usr/lib:/lib \
	  $(_EXPORT_LINES_) \
	  $(patsubst %,-bE:%,$(wildcard $(@:$(LIBDIR)/lib%.so=%.exp))) \
	  -L/usr/local/java/lib/aix/native_threads \
	  $($(@:$(LIBDIR)/%=%)_LDFLAGS) \
	  $(_JNI_LIBRARY_OBJECTS_)
else
$(_JNI_LIBRARIES_):
# a confirmer ...
	cl -LD -Fe$@ $(_JNI_LIBRARY_OBJECTS_)
endif

clean::
	-cd $(OBJDIR)/$(RELPATH); rm -f *.o *.u *.h *.exp
	-rm -f $(_JNI_LIBRARIES_)

$(JNI_LIBRARIES): %: $(LIBDIR)/%
$(_JNI_LIB_CLASSES_:%=%.h): %.h: $(_JNI_INC_)

.PHONY: $(JNI_LIBRARIES) $(_JNI_LIB_CLASSES_:%=%.h)
