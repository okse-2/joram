/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Jose Carlos Waeny
 * Contributor(s):
 */
package chat;

import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Properties;

/**
 * Launching JNDI:
 *
 * @author	JC Waeny 
 * @email       jc@waeny.2y.net
 * @version     1.0
 */
public class getHome {

    public static InitialContext context() throws Exception {
        
        Properties props    = new Properties();
        Properties sysProps = System.getProperties();
        
        props.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
        props.setProperty("java.naming.provider.url", "joram://localhost:16400");
        
        sysProps.putAll(props);
        System.setProperties(sysProps);
        
        try {
            return ( new InitialContext() );
        } catch (Exception e) {
            throw new Exception( e.toString() );
        }
        
    }
    
    public static InitialContext context(String host, String port) throws Exception {
        
        Properties props    = new Properties();
        Properties sysProps = System.getProperties();
        
        props.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi.NamingContextFactory");
        props.setProperty("java.naming.provider.url", "joram://" + host + ":" + port);
        
        sysProps.putAll(props);
        System.setProperties(sysProps);
        
        try {
            return ( new InitialContext() );
        } catch (Exception e) {
            throw new Exception( e.toString() );
        }
        
    }    
}
