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
import java.util.StringTokenizer;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * Helper class for interceptors. 
 */
public class InterceptorsHelper {
	/** logger */
  public static Logger logger = Debug.getLogger(InterceptorsHelper.class.getName());
  
	private static final String INTERCEPTOR_CLASS_NAME_SEPARATOR =",";

	/**
	 * Add the interceptors className list (used by Joram admin).
	 * 
	 * @param listInterceptorClassName list of string className interceptors (separate with INTERCEPTOR_CLASS_NAME_SEPARATOR)
	 * @param interceptors	the interceptors List (maybe IN or OUT)
	 * @throws Exception
	 */
	public static synchronized void addInterceptors(String listInterceptorClassName, List interceptors) throws Exception {
		if (listInterceptorClassName != null && interceptors != null) {
			if (logger.isLoggable(BasicLevel.DEBUG))
				logger.log(BasicLevel.DEBUG, "addInterceptors(" + listInterceptorClassName + ", " + interceptors + ')');
			String error = null;
			StringTokenizer token = new StringTokenizer(listInterceptorClassName, INTERCEPTOR_CLASS_NAME_SEPARATOR);
			while (token.hasMoreTokens()) {
				String interceptorClassName = token.nextToken();
				try {
					interceptors.add((MessageInterceptor)Class.forName(interceptorClassName).newInstance());
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
   * Remove the interceptor.
   * 
   * @param interceptorClassName the interceptor to remove.
	 * @param interceptors	the interceptors List (maybe IN or OUT)
   * @return true if removed.
   */
  private static boolean removeInterceptor(String interceptorClassName, List interceptors) {
  	boolean removed = false;
  	if (interceptorClassName != null) {
  		Iterator it = interceptors.iterator();
  		while (it.hasNext()) {
  			if (interceptorClassName.equals(it.next().getClass().getName())) {
  				removed=true;
  				it.remove();
  			}
  		}
  	}
  	return removed;
  }
  
  /**
   * Remove the interceptors.
   * 
   * @param listInterceptorClassName list of string className interceptors (separate by INTERCEPTOR_CLASS_NAME_SEPARATOR)
	 * @param interceptors	the interceptors List (maybe IN or OUT)
	 * @throws Exception
   */
  public static synchronized void removeInterceptors(String listInterceptorClassName, List interceptors) throws Exception {
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
	 * get the interceptors list.
	 * 
	 * @param interceptors  the interceptors List (maybe IN or OUT)
	 * @return string representation of interceptors List separate by INTERCEPTOR_CLASS_NAME_SEPARATOR
	 */
	public static String getListInterceptors(List interceptors) {
		if (interceptors != null) {
			if (logger.isLoggable(BasicLevel.DEBUG))
				logger.log(BasicLevel.DEBUG, "getListInterceptors(" + interceptors + ')');
			StringBuffer buff = new StringBuffer();
			Iterator it = interceptors.iterator();
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
