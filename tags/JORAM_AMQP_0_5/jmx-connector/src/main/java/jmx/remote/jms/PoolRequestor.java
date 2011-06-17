package jmx.remote.jms;

import java.util.LinkedList;

public class PoolRequestor {
	public 	LinkedList listRequestors;
	int freeRequestor = 0;
    
	public PoolRequestor(int n) {
    	for (int i = 0; i < n; i++) {
    	 this.listRequestors.add(new Requestor());
	}
    	
	
	}
	
    public Requestor getRequestor(){
    	if(listRequestors.get(freeRequestor)!= null){
    		return (Requestor) listRequestors.get(freeRequestor);
    	}
    	
		return null;
    	
    }
	public void returnRequestor(Requestor requestor){
		listRequestors.add(requestor);
		freeRequestor++;
		
	}
		
}

