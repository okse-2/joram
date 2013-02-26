/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * Helper class to handle interceptors in destination and user's proxy. 
 */
public class InterceptorsHelper {
	/** logger */
  public static Logger logger = Debug.getLogger(InterceptorsHelper.class.getName());
  
	private static final String INTERCEPTOR_CLASS_NAME_SEPARATOR =",";

	/**
	 * Add the specified interceptors in the list.
	 * 
	 * @param listInterceptorClassName list of string className interceptors (separate with INTERCEPTOR_CLASS_NAME_SEPARATOR)
	 * @param interceptors	the interceptors List.
	 * @param interceptorProp the init properties parameter.
	 * @throws Exception
	 */
	public static synchronized void addInterceptors(String listInterceptorClassName, List<MessageInterceptor> interceptors, Properties interceptorProp) throws Exception {
		if (listInterceptorClassName != null && interceptors != null) {
			if (logger.isLoggable(BasicLevel.DEBUG))
				logger.log(BasicLevel.DEBUG, "addInterceptors(" + listInterceptorClassName + ", " + interceptors + ')');
			String error = null;
			StringTokenizer token = new StringTokenizer(listInterceptorClassName, INTERCEPTOR_CLASS_NAME_SEPARATOR);
			while (token.hasMoreTokens()) {
				String interceptorClassName = token.nextToken();
				try {
					MessageInterceptor interceptor = (MessageInterceptor)Class.forName(interceptorClassName).newInstance();
					interceptor.init(interceptorProp);
					interceptors.add(interceptor);
				} catch(Throwable t) {
					if (logger.isLoggable(BasicLevel.WARN))
						logger.log(BasicLevel.WARN, "addInterceptors", t);
					StringWriter sw = new StringWriter();
					t.printStackTrace(new PrintWriter(sw));
					if (error == null)
						error = "(" + interceptorClassName + " exc=" + sw.toString() + ')';
					else
						error = error + "(" + interceptorClassName + " exc=" + sw.toString() + ')';
				}
			}
			if (error != null)
				throw new Exception(error);
		}
	}

  /**
   * Remove the first occurrence of interceptorClassName.
   * 
   * @param interceptorClassName the interceptor to remove.
	 * @param interceptors	the interceptors List.
   * @return true if removed.
   */
  private static boolean removeInterceptor(String interceptorClassName, List<MessageInterceptor> interceptors) {
  	boolean removed = false;
  	if (interceptorClassName != null) {
  		Iterator<MessageInterceptor> it = interceptors.iterator();
  		while (it.hasNext()) {
  			if (interceptorClassName.equals(it.next().getClass().getName())) {
  				removed=true;
  				it.remove();
  				break;
  			}
  		}
  	}
  	return removed;
  }
  
  /**
   * Remove the first occurrence of interceptorClassName.
   * 
   * @param listInterceptorClassName list of string className interceptors (separate by INTERCEPTOR_CLASS_NAME_SEPARATOR)
	 * @param interceptors	the interceptors List.
	 * @throws Exception
   */
  public static synchronized void removeInterceptors(String listInterceptorClassName, List<MessageInterceptor> interceptors) throws Exception {
  	if (listInterceptorClassName != null && interceptors != null) {
  		if (logger.isLoggable(BasicLevel.DEBUG))
  			logger.log(BasicLevel.DEBUG, "removeInterceptors(" + listInterceptorClassName + ", " + interceptors + ')');
  		String error = null;
  		StringTokenizer token = new StringTokenizer(listInterceptorClassName, INTERCEPTOR_CLASS_NAME_SEPARATOR);
  		while (token.hasMoreTokens()) {
  			String interceptorClassName = token.nextToken();
  			try {
  				removeInterceptor(interceptorClassName, interceptors);
  			} catch(Throwable t) {
  				if (logger.isLoggable(BasicLevel.WARN))
  					logger.log(BasicLevel.WARN, "removeInterceptors", t);
  				StringWriter sw = new StringWriter();
					t.printStackTrace(new PrintWriter(sw));
  				if (error == null)
  					error = "(" + interceptorClassName + " exc=" + sw.toString() + ')';
  				else
  					error = error + "(" + interceptorClassName + " exc=" + sw.toString() + ')';
  			}
  		}
  		if (error != null)
  			throw new Exception(error);
  	}
  }

  /**
   * Replace the first occurrence of oldInterceptorClassName by the newInterceptorClassName.
   * 
   * @param newInterceptorClassName the new className interceptor.
   * @param oldInterceptorClassName the old className interceptor.
	 * @param interceptors	the interceptors List.
	 * @return true if replaced.
	 * @throws Exception
   */
  public static synchronized boolean replaceInterceptor(String newInterceptorClassName, String oldInterceptorClassName, List<MessageInterceptor> interceptors, Properties interceptorProp) throws Exception {
  	if (newInterceptorClassName != null && oldInterceptorClassName != null && interceptors != null) {
  		if (logger.isLoggable(BasicLevel.DEBUG))
  			logger.log(BasicLevel.DEBUG, "replaceInterceptor(" + newInterceptorClassName + ", " + oldInterceptorClassName + ", " + interceptors + ')');
  		try {
  			boolean replaced = false;
  			Iterator<MessageInterceptor> it = interceptors.iterator();
  			while (it.hasNext()) {
  				MessageInterceptor oldMI = (MessageInterceptor) it.next();
  				if (oldInterceptorClassName.equals(oldMI.getClass().getName())) {
  					int index = interceptors.indexOf(oldMI);
  					interceptors.remove(index);
  					MessageInterceptor interceptor = (MessageInterceptor)Class.forName(newInterceptorClassName).newInstance();
  					interceptor.init(interceptorProp);
  					interceptors.add(index, interceptor);
  					if (logger.isLoggable(BasicLevel.DEBUG))
  						logger.log(BasicLevel.DEBUG, "replaceInterceptor index = " + index);
  					replaced=true;
  					break;
  				}
  			}
  			return replaced;
  		} catch(Throwable t) {
  			if (logger.isLoggable(BasicLevel.WARN))
  				logger.log(BasicLevel.WARN, "replaceInterceptor", t);
  			StringWriter sw = new StringWriter();
  			t.printStackTrace(new PrintWriter(sw));
  			throw new Exception("(" + newInterceptorClassName + " exc=" + sw.toString() + ')');
  		}
  	}
  	return false;
  }
  
	/**
	 * get the interceptors list.
	 * 
	 * @param interceptors  the interceptors List.
	 * @return string representation of interceptors List separate by INTERCEPTOR_CLASS_NAME_SEPARATOR
	 */
	public static String getListInterceptors(List<MessageInterceptor> interceptors) {
		if (interceptors != null) {
			if (logger.isLoggable(BasicLevel.DEBUG))
				logger.log(BasicLevel.DEBUG, "getListInterceptors(" + interceptors + ')');
			StringBuffer buff = new StringBuffer();
			Iterator<MessageInterceptor> it = interceptors.iterator();
			while (it.hasNext()) {
				MessageInterceptor messageInterceptor = (MessageInterceptor) it.next();
				buff.append(messageInterceptor.getClass().getName());
				if (it.hasNext()) buff.append(INTERCEPTOR_CLASS_NAME_SEPARATOR);
			}
			return buff.toString();
		}
		return null;
	}
}
