/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker
 * Contributor(s):
 */
package fr.dyade.aaa.jndi2.impl;

import javax.naming.NamingException;

public interface NamingContextMBean {

   public String[] getNamingContext();

   public String getStrOwnerId();

   public void setStrOwnerId(String strOwnerId);
   
   public void createSubcontext(String ctxName) throws NamingException;

   public void destroySubcontext() throws NamingException;
   
   public String lookup(String name) throws NamingException;

   public void unbind(String name) throws NamingException;
}
