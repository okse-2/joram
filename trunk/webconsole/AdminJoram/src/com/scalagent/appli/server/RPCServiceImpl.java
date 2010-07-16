/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server;


import com.scalagent.appli.client.RPCService;
import com.scalagent.engine.server.BaseRPCServiceImpl;


/**
 * The server side implementation of the RPC service.
 */
public class RPCServiceImpl extends BaseRPCServiceImpl implements RPCService {


	private static final long serialVersionUID = 8205399613624535903L;
	public static final String SPECIFICATION_TEST_INTERACTOR_TYPE="SpecificationInteractorType";

	/**
	 * Returns the facade to access session beans and pojo objects related to devicesTests
	 * @return
	 */


public RPCServiceImpl() {
	
	System.out.println("### appli.server.RPCServiceImpl loaded : création du service RPC");

	cache = new RPCServiceCache();

	}		
}
