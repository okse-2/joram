/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.common;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Yohann CINTRE
 */
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
