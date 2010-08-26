package com.scalagent.engine.client.event;

import com.google.gwt.event.shared.GwtEvent;


public class TestClickEvent extends GwtEvent<TestClickHandler> {

	public static Type<TestClickHandler> TYPE = new Type<TestClickHandler>();
	private String userInputValue;
	
	public TestClickEvent(String userInputValue) {
		this.userInputValue = userInputValue;
	}
	
	@Override
	public void dispatch(TestClickHandler handler) {
			handler.onTestClick(userInputValue);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<TestClickHandler> getAssociatedType() {
		return TYPE;
	}

	
}