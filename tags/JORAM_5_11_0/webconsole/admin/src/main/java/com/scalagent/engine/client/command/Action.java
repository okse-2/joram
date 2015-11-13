package com.scalagent.engine.client.command;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This interface is used to send a request to RPC service via the method
 * TestRPCService.execute(Action<R>).
 * It must be parametrized by the class of the response.
 * Use the annotation CalledMethod to specify which classes must be executed in order to generate
 * the response to this request:
 * @CalledMethod(value=GetDevicesActionImpl.class) -> will cause the class GetDevicesActionImpl to be called.
 * 
 * Also see ActionImpl in the server package of the application.
 *  
 * @author Florian Gimbert
 * @param <R> Response class used to send the response to the client.
 */
public interface Action<R extends Response> extends IsSerializable {

}
