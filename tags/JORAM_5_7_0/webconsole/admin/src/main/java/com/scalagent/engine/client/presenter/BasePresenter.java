package com.scalagent.engine.client.presenter;

import com.google.gwt.event.shared.SimpleEventBus;
import com.scalagent.engine.client.BaseRPCServiceAsync;
import com.scalagent.engine.client.BaseRPCServiceCacheClient;
import com.scalagent.engine.client.widget.BaseWidget;

/**
 * Base class for all presenters.
 * 
 * @author Florian Gimbert
 *
 * @param <D> BaseWidget subclass responsible for creating the UI associated to this UI entity.
 * @param <S> BaseRPCServiceAsync subclass used to access server.
 * @param <C> BaseRPCServiceCacheClient subclass to access data.
 */
public abstract class BasePresenter<D extends BaseWidget<?>, S extends BaseRPCServiceAsync, C extends BaseRPCServiceCacheClient> {

	protected D widget;
	protected S service;
	protected C cache;
	protected SimpleEventBus eventBus;
	
	public BasePresenter(S service, C cache, SimpleEventBus eventBus) {
		this.service = service;
		this.cache = cache;
		this.eventBus = eventBus;
	}
	
	public D getWidget() {
		return widget;
	}

	public S getRPCService() {
		return service;
	}
	
	public C getCache() {
		return cache;
	}
	
  public SimpleEventBus getEventBus() {
		return eventBus;
	}
	
	/**
	 * This method must return a unique id
	 * for this presenter. It's used to identify
	 * presenters in tabs...
	 * @return unique id of the presenter.
	 */
	public String getId() {
		return this.getClass().getName();
	}
}
