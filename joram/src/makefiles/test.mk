# Copyright (C) 1996 - 2000 BULL
# Copyright (C) 1996 - 2000 INRIA

_TEST_CLASSES_1_ = $(foreach test,$(TESTS),$($(test)_CLASSES))
# removes duplicates from _TEST_CLASSES_1_ while keeping original ordering
_TEST_CLASSES_2_ = $(filter-out %/cnt/,\
	$(join $(addsuffix /cnt/,$(INT_LIST)),$(_TEST_CLASSES_1_)))
_TEST_CLASSES_ = $(patsubst /%,%,$(filter-out %/,$(subst /cnt/,/ /,\
	$(foreach w,$(_TEST_CLASSES_2_),\
	  $(filter $w,\
	    $(word 1,\
	      $(filter %/cnt$(filter-out %/,$(subst /cnt/,/ /,$w)),\
		$(_TEST_CLASSES_2_))))))))
CLASSES += $(_TEST_CLASSES_)

# reset target cleans all execution data,
# i.e. agent servers operational persistency
reset::
	cd $(OBJDIR)/$(RELPATH);\
	  rm -rf storage
#	cd $(OBJDIR)/$(RELPATH);\
#	rm -f \[*\] Channel_mq_S_* *_mq_in *_mq_out *.audit
#	rm -rf previous current phase

clean:: reset

# Usually tests will be executed in a single hub agent server.
# Distributed tests may be designed using the SERVER_ID variable
# set to a non hub server id.
ifndef SERVER_ID
SERVER_ID = 0
endif


ifndef MAKE_DEPS
$(OBJDIR)/$(RELPATH)/testdep.mk: Makefile
	@mkdir -p $(OBJDIR)/$(RELPATH)
	@rm -f $@
	$(MAKE) MAKE_DEPS=1 $(TESTS:%=testdep_%)
-include $(OBJDIR)/$(RELPATH)/testdep.mk
else
$(TESTS:%=testdep_%):
	echo "$(@:testdep_%=test_%): $($(@:testdep_%=%)_CLASSES:%=$(OBJDIR)/$(RELPATH)/%.class)" >> $(OBJDIR)/$(RELPATH)/testdep.mk
endif


# Notifications en/decoding may be checked using class CheckNotification.
# Typical use of that test is
# test_XXX:
#	$(CHECK_NOTIFICATION) fr.dyade.aaa.agent.tests.NotificationChecker 0 XXX
# the 0 argument makes the notification sent from SERVER_ID to the hub
CHECK_NOTIFICATION = cd $(OBJDIR)/$(RELPATH);\
	$(JAVA) -classpath '$(CLASSPATH)$(PATH_SEP)$(OBJDIR://$(DRIVE)%=$(DRIVE):%)' \
	  fr.dyade.aaa.agent.tests.CheckNotification $(SERVER_ID) storage

# make all first, then perform automatic tests (none currently)
test:: all

$(TESTS): %: test_%
.PHONY: $(TESTS)
