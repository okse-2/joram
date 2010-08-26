package com.scalagent.engine.client.presenter;


import com.google.gwt.event.shared.HandlerManager;
import com.scalagent.engine.client.BaseRPCServiceAsync;
import com.scalagent.engine.client.BaseRPCServiceCacheClient;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.scalagent.engine.client.widget.BaseWidget;
import com.scalagent.engine.shared.BaseWTO;


/**
 * This abstract presenter might be used to edit a specific WTO object.
 * It contains methods to :
 *    - retrieve edited wto
 *    - to reset edited wto to its initial state
 *    - to return its state (is in modification or creation?)
 *    
 * @author Florian
 *
 * @param <BWTO> edited BaseWTO instance
 * @param <W> widget used to edit the wto
 * @param <S> RPC service used by this presenter
 * @param <C> Cache used by this presenter
 */
public abstract class EditionPresenter<BWTO extends BaseWTO, W extends BaseWidget<?>, S extends BaseRPCServiceAsync, C extends BaseRPCServiceCacheClient> extends BasePresenter<W, S, C> {

	/** This field stores the wto edited by this widget. */
	private BWTO editedWTO;
	/** 
	 * This field stores the initial wto but this one will never be modified by user.
	 * It will be used to reset all operations user might have performed.
	 */
	private BWTO backupEditedWTO;
	
	
	/**
	 * Default constructor.
	 * 
	 * @param editedWTO wto going to be edited by this widget
	 * @param cache client cache used by this presenter
	 * @param eventBus eventBus used by this presenter
	 */
	
	@SuppressWarnings("unchecked")
	public EditionPresenter(BWTO editedWTO, S service, C cache, HandlerManager eventBus) {
		super(service, cache, eventBus);
		this.editedWTO = (BWTO)editedWTO.clone();
		this.backupEditedWTO = (BWTO)editedWTO.clone();
	}
	
	/**
	 * Returns true if the wto managed by this widget
	 * is modifying an entity. It means if the wto is already stored in the 
	 * database, so its id is different from -1.
	 * 
	 * @return true if the entity is in modification, false otherwise.
	 */
//	public boolean isModification() {
//		return (!(editedWTO.getId() == -1));
//	}

	/**
	 * Returns true if the wto managed by this presenter
	 * is creating an entity. It means if the wto is not already stored in the 
	 * database, so its id equals -1.
	 * 
	 * @return true if the entity is in creation, false otherwise.
	 */	
//	public boolean isCreation() {
//		return (editedWTO.getId() == -1);
//	}
	
	/**
	 * Returns the WTO edited by this presenter.
	 * @return WTO edited by this presenter
	 */
	public BWTO getEditedWTO() {
		return editedWTO;
	}

	/**
	 * Sets the WTO edited by this presenter.
	 * Setting the WTO will also set the bakcup wto
	 * used to reset the form.
	 * 
	 * @param wto new WTO to edit
	 */
	@SuppressWarnings("unchecked")
	public void setEditedWTO(BWTO wto) {
		this.editedWTO = wto;
		this.backupEditedWTO = (BWTO)wto.clone();
	}
	
	/**
	 * Reset the edited WTO, using the backup WTO.
	 * Note this method do not update anything in the widget.
	 */
	@SuppressWarnings("unchecked")
	public void resetEditedWTO() {
		this.editedWTO = (BWTO)backupEditedWTO.clone();
	}
	
	/**
	 * Id is used to identify tabs (1 tab = 1 presenter).
	 * It must be unique.
	 */
	@Override
	public String getId() {
		return this.getClass().getName() + "_" + editedWTO.getClass().getName() + "_" + editedWTO.getId();
	}
	
}
