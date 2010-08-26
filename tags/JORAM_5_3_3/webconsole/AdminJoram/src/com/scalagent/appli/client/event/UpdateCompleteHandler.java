/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.event;

import com.google.gwt.event.shared.EventHandler;


public interface UpdateCompleteHandler extends EventHandler {

	public void onUpdateComplete(String info);

}
