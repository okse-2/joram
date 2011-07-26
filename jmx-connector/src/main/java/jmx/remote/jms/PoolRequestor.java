package jmx.remote.jms;

/**
 * In the Class <b>PoolRequestor</b>,is produced the required number of requestor to allow a management tool (JConsole) to do monitoring of an applications  in multithreading.
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */

import javax.jms.Connection;

import fr.dyade.aaa.common.Pool;
import fr.dyade.aaa.util.Operation;

public class PoolRequestor {
	Connection connection;

public PoolRequestor(Connection conn){
	connection = conn;
}
	
 private static Pool pool = null;
	  
 
 		public void initPool(int capacity) {
	    pool = new Pool("Pool Requestor", capacity);
	  }
	
 		

	 public Requestor allocRequestor() {
	 Requestor requestor = null;
	
	try {
		requestor = (Requestor) pool.allocElement();
	} catch (Exception exc) {
	return new Requestor(connection);
	}

	return requestor;
	}
	 
	 public void freeRequestor(Requestor requestor) {
		    pool.freeElement(requestor);
		  }

}
