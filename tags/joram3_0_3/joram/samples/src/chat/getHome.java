/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * The present code contributor is JC Waeny.
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
        
        props.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi.NamingContextFactory");
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
