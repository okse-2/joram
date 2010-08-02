/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.event.common;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Yohann CINTRE
 */
public interface UpdateCompleteHandler extends EventHandler {

	public void onUpdateComplete(String info);

}
