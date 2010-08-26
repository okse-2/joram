/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.shared;

import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import com.scalagent.engine.shared.BaseWTO;

public class QueueWTO extends BaseWTO {
	
	private String name;
	private Date creationDate = new Date();
    private String DMQId;
    private String destinationId;
    private long nbMsgsDeliverSinceCreation;
    private long nbMsgsReceiveSinceCreation;
    private long nbMsgsSentToDMQSinceCreation;
    private long period;
    private String[] rights;
    private boolean freeReading;
    private boolean freeWriting;
    private int threshold;
    private int waitingRequestCount;
    private int pendingMessageCount;
    private int deliveredMessageCount;
    private int nbMaxMsg;
    private Vector<String> messagesList;
    
    
    public String getName() { return name; }
    public Date getCreationDate() { return creationDate; }
    public String getCreationDateinString() { return creationDate.toString(); }
    public long getCreationTimeInMillis() { return creationDate.getTime(); }
    public String getDMQId() { return DMQId; }
    public String getDestinationId() { return destinationId; }
    public long getNbMsgsDeliverSinceCreation() { return nbMsgsDeliverSinceCreation; }
    public long getNbMsgsReceiveSinceCreation() { return nbMsgsReceiveSinceCreation; }
    public long getNbMsgsSentToDMQSinceCreation() { return nbMsgsSentToDMQSinceCreation; }
    public long getPeriod() { return period; }
    public String[] getRights() { return rights; }
    public boolean isFreeReading() { return freeReading; }
    public boolean isFreeWriting() { return freeWriting; }
    public int getThreshold() { return threshold; }
	public int getWaitingRequestCount() { return waitingRequestCount; }
    public int getPendingMessageCount() { return pendingMessageCount; }    
    public int getDeliveredMessageCount() { return deliveredMessageCount; }
	public int getNbMaxMsg() { return nbMaxMsg; }
	public Vector<String> getMessagesList() { return messagesList; }
	
   
    
    public void setName(String name) { this.name = name; }
    public void setCreationDate(Date date) { this.creationDate = date; }
    public void setDMQId(String DMQId) { this.DMQId = DMQId; }
    public void setDestinationId(String destinationId) { this.destinationId = destinationId; }
    public void setNbMsgsDeliverSinceCreation(long nbMsgsDeliverSinceCreation) { this.nbMsgsDeliverSinceCreation = nbMsgsDeliverSinceCreation; }
    public void setNbMsgsReceiveSinceCreation(long nbMsgsReceiveSinceCreation) { this.nbMsgsReceiveSinceCreation = nbMsgsReceiveSinceCreation; }
    public void setNbMsgsSentToDMQSinceCreation(long nbMsgsSentToDMQSinceCreation) { this.nbMsgsSentToDMQSinceCreation = nbMsgsSentToDMQSinceCreation; }
    public void setPeriod(long period) { this.period = period; }
    public void setRights(String[] rights) { this.rights = rights; }
    public void setFreeReading(boolean freeReading) { this.freeReading = freeReading; }
    public void setFreeWriting(boolean freeWriting) { this.freeWriting = freeWriting; }
	public void setThreshold(int threshold) { this.threshold = threshold; }
	public void setWaitingRequestCount(int waitingRequestCount) { this.waitingRequestCount=waitingRequestCount; }    
	public void setPendingMessageCount(int pendingMessageCount) { this.pendingMessageCount=pendingMessageCount; }    
	public void setDeliveredMessageCount(int deliveredMessageCount) { this.deliveredMessageCount=deliveredMessageCount; }
	public void addMessageToList(String messageId) { messagesList.add(messageId); }
	public void removeMessageFromList(String messageId) { messagesList.remove(messageId); }
	public void cleanWaitingRequest() { setWaitingRequestCount(0); }
	public void cleanPendingMessage() { setPendingMessageCount(0); }
	
	public void setNbMaxMsg(int nbMaxMsg) { this.nbMaxMsg = nbMaxMsg; }
    
    
    public QueueWTO(String name, Date creationDate, String DMQId, 
    		String destinationId, long nbMsgsDeliverSinceCreation, long nbMsgsReceiveSinceCreation, 
    		long nbMsgsSentToDMQSinceCreation, long period, String[] rights, 
    		boolean freeReading, boolean freeWriting, int threshold, int waitingRequestCount, int pendingMessageCount, int deliveredMessageCount, int nbMaxMsg) {
    	    	
    	this.id=name;
    	this.name=name;
    	this.creationDate=creationDate;
    	this.DMQId=DMQId;
    	this.destinationId=destinationId;
    	this.nbMsgsDeliverSinceCreation=nbMsgsDeliverSinceCreation;
    	this.nbMsgsReceiveSinceCreation=nbMsgsReceiveSinceCreation;
    	this.nbMsgsSentToDMQSinceCreation=nbMsgsSentToDMQSinceCreation;
    	this.period=period;
    	this.rights=rights;
    	this.freeReading=freeReading;
    	this.freeWriting=freeWriting;
    	this.threshold=threshold;
    	this.waitingRequestCount=waitingRequestCount;
    	this.pendingMessageCount=pendingMessageCount;
    	this.deliveredMessageCount=deliveredMessageCount;
    	this.nbMaxMsg=nbMaxMsg;
    	
    	this.messagesList = new Vector<String>();
    	
    }
  
    public QueueWTO() {
    	this.messagesList = new Vector<String>();
    }
    
	@Override
	public String toString(){
	    return 
	    "[name="+name
	    +", messagesList="+messagesList
	    +" ]";
    }
    
    public String toStringFullContent(){
	    return 
	    "[name="+name
	    +", creationDate="+creationDate
	    +", DMQId="+DMQId
	    +", nbMsgsDeliverSinceCreation="+nbMsgsDeliverSinceCreation
	    +", nbMsgsReceiveSinceCreation="+nbMsgsReceiveSinceCreation
	    +", nbMsgsSentToDMQSinceCreation="+nbMsgsSentToDMQSinceCreation
	    +", period="+period
	    +", rights="+rights
	    +", freeReading="+freeReading
	    +", freeWriting="+freeWriting
	    +", threshold="+threshold
	    +", waitingRequestCount="+waitingRequestCount
	    +", pendingMessageCount="+pendingMessageCount
	    +", deliveredMessageCount="+deliveredMessageCount
	    +", nbMaxMsg="+nbMaxMsg
	    +" ]";   
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((DMQId == null) ? 0 : DMQId.hashCode());
		result = prime * result
				+ ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + deliveredMessageCount;
		result = prime * result
				+ ((destinationId == null) ? 0 : destinationId.hashCode());
		result = prime * result + (freeReading ? 1231 : 1237);
		result = prime * result + (freeWriting ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + nbMaxMsg;
		result = prime
				* result
				+ (int) (nbMsgsDeliverSinceCreation ^ (nbMsgsDeliverSinceCreation >>> 32));
		result = prime
				* result
				+ (int) (nbMsgsReceiveSinceCreation ^ (nbMsgsReceiveSinceCreation >>> 32));
		result = prime
				* result
				+ (int) (nbMsgsSentToDMQSinceCreation ^ (nbMsgsSentToDMQSinceCreation >>> 32));
		result = prime * result + pendingMessageCount;
		result = prime * result + (int) (period ^ (period >>> 32));
		result = prime * result + Arrays.hashCode(rights);
		result = prime * result + threshold;
		result = prime * result + waitingRequestCount;
		return result;
	}
    
    
    @Override
	public boolean equals(Object anObj){
    	if(anObj==null)
    		return false;
    	if(anObj==this)
    		return true;
    	if(!(anObj instanceof QueueWTO))
    		return false;
    	QueueWTO obj = (QueueWTO)anObj;
    	if(obj.name.equals(this.name))
    		return true;
    	return false;
    }
    

  
    @Override
	public QueueWTO clone() {
  	
    	QueueWTO queue = new QueueWTO();
  	
    	queue.setName(this.getName());
    	queue.setCreationDate(this.getCreationDate());
    	queue.setDMQId(this.getDMQId());
    	queue.setDestinationId(this.getDestinationId());
    	queue.setNbMsgsDeliverSinceCreation(this.getNbMsgsDeliverSinceCreation());
    	queue.setNbMsgsReceiveSinceCreation(this.getNbMsgsReceiveSinceCreation());
    	queue.setNbMsgsSentToDMQSinceCreation(this.getNbMsgsSentToDMQSinceCreation());
    	queue.setPeriod(this.getPeriod());
    	queue.setRights(this.getRights());
    	queue.setFreeReading(this.isFreeReading());
    	queue.setFreeWriting(this.isFreeWriting());
    	queue.setThreshold(this.getThreshold());
    	queue.setWaitingRequestCount(this.getWaitingRequestCount());
    	queue.setPendingMessageCount(this.getPendingMessageCount());
    	queue.setDeliveredMessageCount(this.getDeliveredMessageCount());
    	queue.setNbMaxMsg(this.getNbMaxMsg());
    	
	     return queue;
  	}
    
    @Override
	public boolean equalsContent(Object anObj) {
		
		if(!equals(anObj))
			return false;
		
		QueueWTO obj = (QueueWTO)anObj;

		boolean eq =  
		equalsWithNull(this.name, obj.name)
		&& equalsWithNull(this.creationDate, obj.creationDate)
		&& equalsWithNull(this.DMQId, obj.DMQId)
		&& equalsWithNull(this.destinationId, obj.destinationId)
		&& equalsWithNull(this.nbMsgsDeliverSinceCreation, obj.nbMsgsDeliverSinceCreation)
		&& equalsWithNull(this.nbMsgsReceiveSinceCreation, obj.nbMsgsReceiveSinceCreation)
		&& equalsWithNull(this.nbMsgsSentToDMQSinceCreation, obj.nbMsgsSentToDMQSinceCreation)
		&& equalsWithNull(this.period, obj.period)
		&& equalsWithNull(Arrays.asList(this.rights), Arrays.asList(obj.rights))
		&& equalsWithNull(this.freeReading, obj.freeReading)
		&& equalsWithNull(this.freeWriting, obj.freeWriting)
		&& equalsWithNull(this.threshold, obj.threshold)
		&& equalsWithNull(this.waitingRequestCount, obj.waitingRequestCount)
		&& equalsWithNull(this.pendingMessageCount, obj.pendingMessageCount)
		&& equalsWithNull(this.deliveredMessageCount, obj.deliveredMessageCount)
		&& equalsWithNull(this.nbMaxMsg, obj.nbMaxMsg);
	
		return eq;

    }
}