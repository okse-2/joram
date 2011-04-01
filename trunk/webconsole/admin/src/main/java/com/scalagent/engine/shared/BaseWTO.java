/**
 * (c)2010 Scalagent Distributed Technologies
 * (c)2010 Tagsys-RFID 
 */
package com.scalagent.engine.shared;


import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This is the base class from which all WTO (Web Transfer Object) classes shall inherits.
 * <br/><br/>
 * Web Transfer Objects are used to transfer data between a web server and a web client.
 * @author sgonzalez
 */
public abstract class BaseWTO implements IsSerializable, Serializable {

	/**
	 * Status indicating the object was created in the database
	 */
	public static final int NEW = 0;
	/**
	 * Status indicating the object was updated in the database
	 */
	public static final int UPDATED = 1;
	/**
	 * Status indicating the object was deleted in the database
	 */
	public static final int DELETED = 2;

	/**
	 * Status indicating the last change action performed on the related object in the database
	 */
	private int dbChangeStatus = NEW;
	protected String id = "";

	/**
	 * @return the dbChangeStatus
	 */
	public int getDbChangeStatus() {
		return dbChangeStatus;
	}

	/**
	 * @param dbChangeStatus the dbChangeStatus to set
	 */
	public void setDbChangeStatus(int dbChangeStatus) {
		this.dbChangeStatus = dbChangeStatus;
	}

	/**
	 * 
	 * @param anObj
	 * @return {@code true} if all the attributes of {@code anObj} are equals to {@code this},
	 * return {@code false} otherwise
	 */
	public abstract boolean equalsContent(Object anObj);

	/**
	 * Indicates whether an object is "equal to" another one. If the 2 objects are {@code null}, 
	 * return {@code true} else if only 1 object is {@code null} return {@code false}. Otherwise return
	 * {@code o1.equals(o2)}.
	 * @param o1
	 * @param o2
	 * @return {@code true} if o1.equals(o2), {@code false} otherwise.
	 */
  public static boolean equalsWithNull(Object o1, Object o2) {
		if((o1==null && o2!=null) || (o1!=null && o2==null))
			return false;
		if(o1==null && o2==null)
			return true;
		return o1.equals(o2);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public abstract BaseWTO clone();

}
