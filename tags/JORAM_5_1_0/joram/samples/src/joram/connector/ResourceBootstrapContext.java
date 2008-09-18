/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): BADOLLE Fabien ( ScalAgent Distributed Technologies )
 * Contributor(s):
 */
package connector;

import java.util.Timer;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.XATerminator;


public class ResourceBootstrapContext implements BootstrapContext {
    
    private WorkManager wrkMgr = null;
    
    public ResourceBootstrapContext(WorkManager wm) {
        wrkMgr = wm;
	
    }
    
    public Timer createTimer() throws UnavailableException {
        return new Timer(true);
    }
    
    public WorkManager getWorkManager() {
        return wrkMgr;
    }
    
    public XATerminator getXATerminator() {
	return null;
    }
}
