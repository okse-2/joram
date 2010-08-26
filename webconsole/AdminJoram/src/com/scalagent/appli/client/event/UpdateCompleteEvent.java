/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event;

import com.google.gwt.event.shared.GwtEvent;



public class UpdateCompleteEvent extends GwtEvent<UpdateCompleteHandler> {

	public static Type<UpdateCompleteHandler> TYPE = new Type<UpdateCompleteHandler>();
	
	String info;
	
	
	public UpdateCompleteEvent(String info) {
		this.info = info;
	}
	
	@Override
	public final Type<UpdateCompleteHandler> getAssociatedType() {
		return TYPE;
	}
	@Override
	public void dispatch(UpdateCompleteHandler handler) {
		handler.onUpdateComplete(info);
	}

}
