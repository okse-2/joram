/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client;


public interface ApplicationMessages extends com.google.gwt.i18n.client.Messages {

	/** TOPIC WIDGET **/	

	/**
	 * Translated "Refresh".
	 * 
	 * @return translated "Refresh"
	 */
	@DefaultMessage("Refresh")
	@Key("topicWidget.buttonRefresh.title")
	String topicWidget_buttonRefresh_title();

	/**
	 * Translated "Click to refresh list".
	 * 
	 * @return translated "Click to refresh list"
	 */
	@DefaultMessage("Click to refresh list")
	@Key("topicWidget.buttonRefresh.prompt")
	String topicWidget_buttonRefresh_prompt();

	/**
	 * Translated "Actions".
	 * 
	 * @return translated "Actions"
	 */
	@DefaultMessage("Actions")
	@Key("topicWidget.buttonSection.title")
	String topicWidget_buttonSection_title();

	/**
	 * Translated "Topic".
	 * 
	 * @return translated "Topic"
	 */
	@DefaultMessage("Topic")
	@Key("topicWidget.idField.summary")
	String topicWidget_idFieldL_summary();

	/**
	 * Translated "Message".
	 * 
	 * @return translated "Message"
	 */
	@DefaultMessage("Message")
	@Key("topicWidget.nbMsgsDeliverSinceCreationField.summary")
	String topicWidget_nbMsgsDeliverSinceCreationFieldL_summary();

	/**
	 * Translated "Id".
	 * 
	 * @return translated "Id"
	 */
	@DefaultMessage("Id")
	@Key("topicWidget.idField.title")
	String topicWidget_idFieldL_title();

	/**
	 * Translated "Name".
	 * 
	 * @return translated "Name"
	 */
	@DefaultMessage("Name")
	@Key("topicWidget.nameField.title")
	String topicWidget_nameFieldL_title();

	/**
	 * Translated "Msgs Delivered".
	 * 
	 * @return translated "Msgs Delivered"
	 */
	@DefaultMessage("Msgs Delivered")
	@Key("topicWidget.nbMsgsDeliverSinceCreation.title")
	String topicWidget_nbMsgsDeliverSinceCreationFieldL_title();

	/**
	 * Translated "Msgs Recieved".
	 * 
	 * @return translated "Msgs Recieved"
	 */
	@DefaultMessage("Msgs Recieved")
	@Key("topicWidget.nbMsgsRecieveSinceCreationFieldL.title")
	String topicWidget_nbMsgsRecievesSinceCreationFieldL_title();

	/**
	 * Translated "Msgs Sent".
	 * 
	 * @return translated "Msgs Sent"
	 */
	@DefaultMessage("Msgs Sent")
	@Key("topicWidget.nbMsgsSentSinceCreationFieldL.title")
	String topicWidget_nbMsgsSentSinceCreationFieldL_title();
	
	
	/**
	 * Translated "Free Reading".
	 * 
	 * @return translated "Free Reading"
	 */
	@DefaultMessage("Free Reading")
	@Key("topicWidget.freeReadingField.title")
	String topicWidget_freeReadingFieldL_title();
	
	/**
	 * Translated "Free Writing".
	 * 
	 * @return translated "Free Writing"
	 */
	@DefaultMessage("Free Writing")
	@Key("topicWidget.freeWritingField.title")
	String topicWidget_freeWritingFieldL_title();

	/**
	 * Translated "Select a topic to view its details".
	 * 
	 * @return translated "Select a topic to view its details"
	 */
	@DefaultMessage("Select a topic to view its details")
	@Key("topicWidget.topicDetail.emptyMessage")
	String topicWidget_topicDetail_emptyMessage();

	/**
	 * Translated "Id".
	 * 
	 * @return translated "Id"
	 */
	@DefaultMessage("Id")
	@Key("topicWidget.idFieldD.title")
	String topicWidget_idFieldD_title();

	/**
	 * Translated "Name".
	 * 
	 * @return translated "Name"
	 */
	@DefaultMessage("Name")
	@Key("topicWidget.nameFieldD.title")
	String topicWidget_nameFieldD_title();

	/**
	 * Translated "Creation date".
	 * 
	 * @return translated "Creation date"
	 */
	@DefaultMessage("Creation date")
	@Key("topicWidget.creationDateFieldD.title")
	String topicWidget_creationDateFieldD_title();

	/**
	 * Translated "Subscriber".
	 * 
	 * @return translated "Subscriber"
	 */
	@DefaultMessage("Subscriber")
	@Key("topicWidget.subscriberIdsFieldD.title")
	String topicWidget_subscriberIdsFieldD_title();

	/**
	 * Translated "DMQ Id".
	 * 
	 * @return translated "DMQ Id"
	 */
	@DefaultMessage("DMQ Id")
	@Key("topicWidget.DMQIdFieldD.title")
	String topicWidget_DMQIdFieldD_title();

	/**
	 * Translated "Destination Id".
	 * 
	 * @return translated "Destination Id"
	 */
	@DefaultMessage("Destination Id")
	@Key("topicWidget.destinationIdFieldD.title")
	String topicWidget_destinationIdFieldD_title();

	/**
	 * Translated "Msgs Delivered".
	 * 
	 * @return translated "Msgs Delivered"
	 */
	@DefaultMessage("Msgs Delivered")
	@Key("topicWidget.nbMsgsDeliverSinceCreationFieldD.title")
	String topicWidget_nbMsgsDeliverSinceCreationFieldD_title();

	/**
	 * Translated "Msgs Recieved".
	 * 
	 * @return translated "Msgs Recieved"
	 */
	@DefaultMessage("Msgs Recieved")
	@Key("topicWidget.nbMsgsRecieveSinceCreationFieldD.title")
	String topicWidget_nbMsgsRecievesSinceCreationFieldD_title();

	/**
	 * Translated "Msgs Sent".
	 * 
	 * @return translated "Msgs Sent"
	 */
	@DefaultMessage("Msgs Sent")
	@Key("topicWidget.nbMsgsSentSinceCreationFieldD.title")
	String topicWidget_nbMsgsSentSinceCreationFieldD_title();

	/**
	 * Translated "Period".
	 * 
	 * @return translated "Period"
	 */
	@DefaultMessage("Period")
	@Key("topicWidget.periodFieldD.title")
	String topicWidget_periodFieldD_title();

	/**
	 * Translated "Rights".
	 * 
	 * @return translated "Rights"
	 */
	@DefaultMessage("Rights")
	@Key("topicWidget.rightsFieldD.title")
	String topicWidget_rightsFieldD_title();

	/**
	 * Translated "Free Reading".
	 * 
	 * @return translated "Free Reading"
	 */
	@DefaultMessage("Free Reading")
	@Key("topicWidget.freeReadingFieldD.title")
	String topicWidget_freeReadingFieldD_title();

	/**
	 * Translated "Free Writing".
	 * 
	 * @return translated "Free Writing"
	 */
	@DefaultMessage("Free Writing")
	@Key("topicWidget.freeWritingFieldD.title")
	String topicWidget_freeWritingFieldD_title();

	/**
	 * Translated "Topics".
	 * 
	 * @return translated "Topics"
	 */
	@DefaultMessage("Topics")
	@Key("topicWidget.listStackSection.title")
	String topicWidget_listStackSection_title();

	/**
	 * Translated "Topic Details".
	 * 
	 * @return translated "Topic Details"
	 */
	@DefaultMessage("Topic Details")
	@Key("topicWidget.viewSectionSection.title")
	String topicWidget_viewSectionSection_title();


	

	/** QUEUE WIDGET **/

	/**
	 * Translated "Refresh".
	 * 
	 * @return translated "Refresh"
	 */
	@DefaultMessage("Refresh")
	@Key("queueWidget.buttonRefresh.title")
	String queueWidget_buttonRefresh_title();

	/**
	 * Translated "Click to refresh list".
	 * 
	 * @return translated "Click to refresh list"
	 */
	@DefaultMessage("Click to refresh list")
	@Key("queueWidget.buttonRefresh.prompt")
	String queueWidget_buttonRefresh_prompt();

	/**
	 * Translated "Actions".
	 * 
	 * @return translated "Actions"
	 */
	@DefaultMessage("Actions")
	@Key("queueWidget.buttonSection.title")
	String queueWidget_buttonSection_title();

	/**
	 * Translated "Show Details".
	 * 
	 * @return translated "Show Details"
	 */
	@DefaultMessage("Show Details")
	@Key("queueWidget.buttonBrowse.title")
	String queueWidget_buttonBrowse_title();

	/**
	 * Translated "Click to open details in a new tab".
	 * 
	 * @return translated "Click to open details in a new tab"
	 */
	@DefaultMessage("Click to open details in a new tab")
	@Key("queueWidget.buttonBrowse.prompt")
	String queueWidget_buttonBrowse_prompt();

	/**
	 * Translated "Clear".
	 * 
	 * @return translated "Clear"
	 */
	@DefaultMessage("Clear")
	@Key("queueWidget.buttonClearRequest.title")
	String queueWidget_buttonClearRequest_title();

	/**
	 * Translated "Click to clean waiting requests".
	 * 
	 * @return translated "Click to clean waiting requests"
	 */
	@DefaultMessage("Click to clean waiting requests")
	@Key("queueWidget.buttonClearRequest.prompt")
	String queueWidget_buttonClearRequest_prompt();

	/**
	 * Translated "Clear Message".
	 * 
	 * @return translated "Clear Message"
	 */
	@DefaultMessage("Clear Message")
	@Key("queueWidget.buttonClearMessage.title")
	String queueWidget_buttonClearMessage_title();

	/**
	 * Translated "Click to clean pending messages".
	 * 
	 * @return translated "Click to clean pending messages"
	 */
	@DefaultMessage("Click to clean pending messages")
	@Key("queueWidget.buttonClearMessage.prompt")
	String queueWidget_buttonClearMessage_prompt();

	/**
	 * Translated "Delete Queue".
	 * 
	 * @return translated "Delete Queue"
	 */
	@DefaultMessage("Delete Queue")
	@Key("queueWidget.buttonDelete.title")
	String queueWidget_buttonDelete_title();

	/**
	 * Translated "Click to delete the queue on JORAM".
	 * 
	 * @return translated "Click to delete the queue on JORAM"
	 */
	@DefaultMessage("Click to delete the queue on JORAM")
	@Key("queueWidget.buttonDelete.prompt")
	String queueWidget_buttonDelete_prompt();

	/**
	 * Translated "Queue".
	 * 
	 * @return translated "Queue"
	 */
	@DefaultMessage("Queue")
	@Key("queueWidget.nameField.summary")
	String queueWidget_nameFieldL_summary();

	/**
	 * Translated "Message".
	 * 
	 * @return translated "Message"
	 */
	@DefaultMessage("Message")
	@Key("queueWidget.nbMsgsDeliverSinceCreationFieldL.summary")
	String queueWidget_nbMsgsDeliverSinceCreationFieldL_summary();

	/**
	 * Translated "Id".
	 * 
	 * @return translated "Id"
	 */
	@DefaultMessage("Id")
	@Key("queueWidget.idFieldL.title")
	String queueWidget_idFieldL_title();

	/**
	 * Translated "Name".
	 * 
	 * @return translated "Name"
	 */
	@DefaultMessage("Name")
	@Key("queueWidget.nameFieldL.title")
	String queueWidget_nameFieldL_title();

	/**
	 * Translated "Msgs Delivered".
	 * 
	 * @return translated "Msgs Delivered"
	 */
	@DefaultMessage("Msgs Delivered")
	@Key("queueWidget.nbMsgsDeliverSinceCreationFieldL.title")
	String queueWidget_nbMsgsDeliverSinceCreationFieldL_title();

	/**
	 * Translated "Free Reading".
	 * 
	 * @return translated "Free Reading"
	 */
	@DefaultMessage("Free Reading")
	@Key("queueWidget.freeReadingFieldL.title")
	String queueWidget_freeReadingFieldL_title();

	/**
	 * Translated "Select a queue to view more details".
	 * 
	 * @return translated "Select a queue to view more details"
	 */
	@DefaultMessage("Select a queue to view more details")
	@Key("queueWidget.queueDetail.emptyMessage")
	String queueWidget_queueDetail_emptyMessage();

	/**
	 * Translated "Id".
	 * 
	 * @return translated "Id"
	 */
	@DefaultMessage("Id")
	@Key("queueWidget.idFieldD.title")
	String queueWidget_idFieldD_title(); 

	/**
	 * Translated "Name".
	 * 
	 * @return translated "Name"
	 */
	@DefaultMessage("Name")
	@Key("queueWidget.nameFieldD.title")
	String queueWidget_nameFieldD_title(); 

	/**
	 * Translated "Creation date".
	 * 
	 * @return translated "Creation date"
	 */
	@DefaultMessage("Creation date")
	@Key("queueWidget.cerationDateFieldD.title")
	String queueWidget_creationDateFieldD_title(); 

	/**
	 * Translated "DMQ Id".
	 * 
	 * @return translated "DMQ Id"
	 */
	@DefaultMessage("DMQ Id")
	@Key("queueWidget.DMQIdFieldD.title")
	String queueWidget_DMQIdFieldD_title(); 

	/**
	 * Translated "Destination Id".
	 * 
	 * @return translated "Destination Id"
	 */
	@DefaultMessage("Destination Id")
	@Key("queueWidget.destinationIdFieldD.title")
	String queueWidget_destinationIdFieldD_title(); 

	/**
	 * Translated "Msgs Delivered".
	 * 
	 * @return translated "Msgs Delivered"
	 */
	@DefaultMessage("Msgs Delivered")
	@Key("queueWidget.nbMsgsDeliverSinceCreationFieldD.title")
	String queueWidget_nbMsgsDeliverSinceCreationFieldD_title(); 

	/**
	 * Translated "Msgs Recieved".
	 * 
	 * @return translated "Msgs Recieved"
	 */
	@DefaultMessage("Msgs Recieved")
	@Key("queueWidget.nbMsgsRecieveSinceCreationFieldD.title")
	String queueWidget_nbMsgsRecieveSinceCreationFieldD_title();  

	/**
	 * Translated "Msgs Sent".
	 * 
	 * @return translated "Msgs Sent"
	 */
	@DefaultMessage("Msgs Sent")
	@Key("queueWidget.nbMsgsSentToDMQSinceCreationFieldD.title")
	String queueWidget_nbMsgsSentToDMQSinceCreationFieldD_title(); 

	/**
	 * Translated "Period".
	 * 
	 * @return translated "Period"
	 */
	@DefaultMessage("Period")
	@Key("queueWidget.periodFieldD.title")
	String queueWidget_periodFieldD_title(); 

	/**
	 * Translated "Rights".
	 * 
	 * @return translated "Rights"
	 */
	@DefaultMessage("Rights")
	@Key("queueWidget.RightsFieldD.title")
	String queueWidget_RightsFieldD_title(); 

	/**
	 * Translated "Free Reading".
	 * 
	 * @return translated "Free Reading"
	 */
	@DefaultMessage("Free Reading")
	@Key("queueWidget.freeReadingFieldD.title")
	String queueWidget_freeReadingFieldD_title(); 

	/**
	 * Translated "Free Writing".
	 * 
	 * @return translated "Free Writing"
	 */
	@DefaultMessage("Free Writing")
	@Key("queueWidget.freeWritingFieldD.title")
	String queueWidget_freeWritingFieldD_title(); 

	/**
	 * Translated "Threshold".
	 * 
	 * @return translated "Threshold"
	 */
	@DefaultMessage("Threshold")
	@Key("queueWidget.thresholdFieldD.title")
	String queueWidget_thresholdFieldD_title(); 

	/**
	 * Translated "Waiting Requests".
	 * 
	 * @return translated "Waiting Requests"
	 */
	@DefaultMessage("Waiting Requests")
	@Key("queueWidget.waitingRequestCountFieldD.title")
	String queueWidget_waitingRequestCountFieldD_title(); 

	/**
	 * Translated "Pending Messages".
	 * 
	 * @return translated "Pending Messages"
	 */
	@DefaultMessage("Pending Messages")
	@Key("queueWidget.pendingMessageCountFieldD.title")
	String queueWidget_pendingMessageCountFieldD_title(); 

	/**
	 * Translated "Delivered Messages".
	 * 
	 * @return translated "Delivered Messages"
	 */
	@DefaultMessage("Delivered Messages")
	@Key("queueWidget.deliveredMessageCountFieldD.title")
	String queueWidget_deliveredMessageCountFieldD_title(); 

	/**
	 * Translated "Max Messages".
	 * 
	 * @return translated "Max Messages"
	 */
	@DefaultMessage("Max Messages")
	@Key("queueWidget.nbMaxMessFieldD.title")
	String queueWidget_nbMaxMessFieldD_title(); 

	/**
	 * Translated "Queues".
	 * 
	 * @return translated "Queues"
	 */
	@DefaultMessage("Queues")
	@Key("queueWidget.listStackSection.title")
	String queueWidget_listStackSection_title(); 

	/**
	 * Translated "Queue Details".
	 * 
	 * @return translated "Queue Details"
	 */
	@DefaultMessage("Queue Details")
	@Key("queueWidget.viewSection.title")
	String queueWidget_viewSection_title(); 

	/**
	 * Translated "Msgs Recieved".
	 * 
	 * @return translated "Msgs Recieved"
	 */
	@DefaultMessage("Msgs Recieved")
	@Key("queueWidget.nbMsgsRecieveSinceCreationFieldL.title")
	String queueWidget_nbMsgsRecieveSinceCreationFieldL_title(); 

	/**
	 * Translated "Message".
	 * 
	 * @return translated "Message"
	 */
	@DefaultMessage("Message")
	@Key("queueWidget.nbMsgsRecieveSinceCreationFieldL.summary")
	String queueWidget_nbMsgsRecieveSinceCreationFieldL_summary(); 

	/**
	 * Translated "Msgs Sent".
	 * 
	 * @return translated "Msgs Sent"
	 */
	@DefaultMessage("Msgs Sent")
	@Key("queueWidget.nbMsgsSentToDMQSinceCreationFieldL.title")
	String queueWidget_nbMsgsSentToDMQSinceCreationFieldL_title(); 

	/**
	 * Translated "Message".
	 * 
	 * @return translated "Message"
	 */
	@DefaultMessage("Message")
	@Key("queueWidget.nbMsgsSentToDMQSinceCreationFieldL.summary")
	String queueWidget_nbMsgsSentToDMQSinceCreationFieldL_summary(); 

	/**
	 * Translated "Waiting Request".
	 * 
	 * @return translated "Waiting Request"
	 */
	@DefaultMessage("Waiting Request")
	@Key("queueWidget.waitingRequestFieldL.title")
	String queueWidget_waitingRequestFieldL_title(); 

	/**
	 * Translated "Pending Message".
	 * 
	 * @return translated "Pending Message"
	 */
	@DefaultMessage("Pending Message")
	@Key("queueWidget.pendingMessageFieldL.title")
	String queueWidget_pendingMessageFieldL_title(); 

	/**
	 * Translated "Browse".
	 * 
	 * @return translated "Browse"
	 */
	@DefaultMessage("Browse")
	@Key("queueWidget.browseFieldL.title")
	String queueWidget_browseFieldL_title(); 

	/*
	 * Translated "Action".
	 * 
	 * @return translated "Action"
	 */
	@DefaultMessage("Action")
	@Key("queueWidget.actionFieldL.title")
	String queueWidget_actionFieldL_title(); 

	/**
	 * Translated "Delete".
	 * 
	 * @return translated "Delete"
	 */
	@DefaultMessage("Delete")
	@Key("queueWidget.deleteFieldL.title")
	String queueWidget_deleteFieldL_title(); 

	/**
	 * Translated "Do you really want to delete this queue?".
	 * 
	 * @return translated "Do you really want to delete this queue?"
	 */
	@DefaultMessage("Do you really want to delete this queue?")
	@Key("queueWidget.confirmDelete")
	String queueWidget_confirmDelete(); 
	
	/**
	 * Translated "Clear Waiting Request".
	 * 
	 * @return translated "Clear Waiting Requests"
	 */
	@DefaultMessage("Clear Waiting Requests")
	@Key("queueWidget.labelClearWaitingRequests.title")
	String queueWidget_labelClearWaitingRequests_title(); 
	
	/**
	 * Translated "Click to clear the waiting requests for this Queue".
	 * 
	 * @return translated "Click to clear the waiting requests for this Queue"
	 */
	@DefaultMessage("Click to clear the waiting requests for this Queue")
	@Key("queueWidget.labelClearWaitingRequests.tooltip")
	String queueWidget_labelClearWaitingRequests_tooltip(); 
	
	/**
	 * Translated "Clear Pending Messages".
	 * 
	 * @return translated "Clear Pending Messages"
	 */
	@DefaultMessage("Clear Pending Messages")
	@Key("queueWidget.labelClearPendingMessages.title")
	String queueWidget_labelClearPendingMessages_title(); 
	
	/**
	 * Translated "Click to clear the pending messages for this Queue".
	 * 
	 * @return translated "Click to clear the pending messages for this Queue"
	 */
	@DefaultMessage("Click to clear the pending messages for this Queue")
	@Key("queueWidget.labelClearPendingMessages.tooltip")
	String queueWidget_labelClearPendingMessages_tooltip(); 
	

	
	
	/** QUEUEDETAIL WIDGET **/

	/**
	 * Translated "Do you really want to delete this message?".
	 * 
	 * @return translated "Do you really want to delete this message?"
	 */
	@DefaultMessage("Do you really want to delete this message?")
	@Key("queueDetailWidget.confirmDelete")
	String queueDetailWidget_confirmDelete(); 

	/** Translated "Delete Message".
	 * 
	 * @return translated "Delete Message"
	 */
	@DefaultMessage("Delete Message")
	@Key("queueDetailWidget.buttonDelete.title")
	String queueDetailWidget_buttonDelete_title();

	/**
	 * Translated "Click to delete this message on JORAM".
	 * 
	 * @return translated "Click to delete this message on JORAM"
	 */
	@DefaultMessage("Click to delete this message on JORAM")
	@Key("queueDetailWidget.buttonDelete.prompt")
	String queueDetailWidget_buttonDelete_prompt();

	/** Translated "Id".
	 * 
	 * @return translated "Id"
	 */
	@DefaultMessage("Id")
	@Key("queueDetailWidget.idFieldL.title")
	String queueDetailWidget_idFieldL_title(); 

	/** Translated "Persistent".
	 * 
	 * @return translated "Persistent"
	 */
	@DefaultMessage("Persistent")
	@Key("queueDetailWidget.persistentFieldL.title")
	String queueDetailWidget_persistentFieldL_title(); 

	/** Translated "Redelivered".
	 * 
	 * @return translated "Redelivered"
	 */
	@DefaultMessage("Redelivered")
	@Key("queueDetailWidget.redeliveredFieldL.title")
	String queueDetailWidget_redeliveredFieldL_title(); 

	/** Translated "Delivery Count".
	 * 
	 * @return translated "Delivery Count"
	 */
	@DefaultMessage("Delivery Count")
	@Key("queueDetailWidget.deliveryCountFieldL.title")
	String queueDetailWidget_deliveyCountFieldL_title(); 

	/** Translated "Prioprity".
	 * 
	 * @return translated "Prioprity"
	 */
	@DefaultMessage("Prioprity")
	@Key("queueDetailWidget.priorityFieldL.title")
	String queueDetailWidget_priorityFieldL_title(); 

	/** Translated "Type".
	 * 
	 * @return translated "Type"
	 */
	@DefaultMessage("Type")
	@Key("queueDetailWidget.typeFieldL.title")
	String queueDetailWidget_typeFieldL_title(); 

	/** Translated "Delete".
	 * 
	 * @return translated "Delete"
	 */
	@DefaultMessage("Delete")
	@Key("queueDetailWidget.deleteFieldL.title")
	String queueDetailWidget_deleteFieldL_title(); 

	/** Translated "Id".
	 * 
	 * @return translated "Id"
	 */
	@DefaultMessage("Id")
	@Key("queueDetailWidget.idFieldD.title")
	String queueDetailWidget_idFieldD_title(); 

	/** Translated "Expiration".
	 * 
	 * @return translated "Expiration"
	 */
	@DefaultMessage("Expiration")
	@Key("queueDetailWidget.expirationFieldD.title")
	String queueDetailWidget_expirationFieldD_title(); 

	/** Translated "Index".
	 * 
	 * @return translated "Index"
	 */
	@DefaultMessage("Index")
	@Key("queueDetailWidget.indexFieldD.title")
	String queueDetailWidget_indexFieldD_title(); 

	/** Translated "Timestamp".
	 * 
	 * @return translated "Timestamp"
	 */
	@DefaultMessage("Timestamp")
	@Key("queueDetailWidget.timestampFieldD.title")
	String queueDetailWidget_timestampFieldD_title(); 

	/** Translated "Persistent".
	 * 
	 * @return translated "Persistent"
	 */
	@DefaultMessage("Persistent")
	@Key("queueDetailWidget.persistentFieldD.title")
	String queueDetailWidget_persistentFieldD_title(); 

	/** Translated "Redelivered".
	 * 
	 * @return translated "Redelivered"
	 */
	@DefaultMessage("Redelivered")
	@Key("queueDetailWidget.redeliveredFieldD.title")
	String queueDetailWidget_redeliveredFieldD_title(); 

	/** Translated "Delivery Count".
	 * 
	 * @return translated "Delivery Count"
	 */
	@DefaultMessage("Delivery Count")
	@Key("queueDetailWidget.deliveryCountFieldD.title")
	String queueDetailWidget_deliveyCountFieldD_title(); 

	/** Translated "Prioprity".
	 * 
	 * @return translated "Prioprity"
	 */
	@DefaultMessage("Prioprity")
	@Key("queueDetailWidget.priorityFieldD.title")
	String queueDetailWidget_priorityFieldD_title(); 

	/** Translated "Type".
	 * 
	 * @return translated "Type"
	 */
	@DefaultMessage("Type")
	@Key("queueDetailWidget.typeFieldD.title")
	String queueDetailWidget_typeFieldD_title(); 

	/** Translated "Text".
	 * 
	 * @return translated "Text"
	 */
	@DefaultMessage("Text")
	@Key("queueDetailWidget.textFieldD.title")
	String queueDetailWidget_textFieldD_title(); 
	
	/** Translated "Properties".
	 * 
	 * @return translated "Properties"
	 */
	@DefaultMessage("Properties")
	@Key("queueDetailWidget.propertiesFieldD.title")
	String queueDetailWidget_propertiesFieldD_title(); 

	
	/** Translated "This queue no longer exists on JORAM".
	 * 
	 * @return translated "This queue no longer exists on JORAM"
	 */
	@DefaultMessage("This queue no longer exists on JORAM")
	@Key("queueDetailWidget.refreshButton.tooltip")
	String queueDetailWidget_refreshbutton_tooltip(); 
	
	/**
	 * Translated "Queue Details".
	 * 
	 * @return translated "Queue Details"
	 */
	@DefaultMessage("Queue Details")
	@Key("queueDetailWidget.buttonSection.title")
	String queueDetailWidget_buttonSection_title(); 
	
	/**
	 * Translated "Message List".
	 * 
	 * @return translated "Message List"
	 */
	@DefaultMessage("Message List")
	@Key("queueDetailWidget.listSection.title")
	String queueDetailWidget_listSection_title(); 
	
	/**
	 * Translated "Message Details".
	 * 
	 * @return translated "Message Details"
	 */
	@DefaultMessage("Message Details")
	@Key("queueDetailWidget.detailsSection.title")
	String queueDetailWidget_detailsSection_title(); 

	/**
	 * Translated "Select a message to view more details".
	 * 
	 * @return translated "Select a message to view more details"
	 */
	@DefaultMessage("Select a message to view more details")
	@Key("queueDetailWidget.messageDetail.emptyMessage")
	String queueDetailWidget_messageDetail_emptyMessage();
	


	/** MAIN WIDGET **/

	/**
	 * Translated "JORAM Administration Panel".
	 * 
	 * @return translated "JORAM Administration Panel"
	 */
	@DefaultMessage("JORAM Administration Panel")
	@Key("mainWidget.headerLabel.title")
	String mainWidget_headerLabel_title();

	/**
	 * Translated "Server Info".
	 * 
	 * @return translated "Server Info"
	 */
	@DefaultMessage("Server Info")
	@Key("mainWidget.tabInfo.title")
	String mainWidget_tabInfo_title();

	/**
	 * Translated "Topics".
	 * 
	 * @return translated "Topics"
	 */
	@DefaultMessage("Topics")
	@Key("mainWidget.tabTopic.title")
	String mainWidget_tabTopic_title();

	/**
	 * Translated "Queues".
	 * 
	 * @return translated "Queues"
	 */
	@DefaultMessage("Queues")
	@Key("mainWidget.tabQueue.title")
	String mainWidget_tabQueue_title();

	/**
	 * Translated "Subscriptions".
	 * 
	 * @return translated "Subscriptions"
	 */
	@DefaultMessage("Subscriptions")
	@Key("mainWidget.tabSubscription.title")
	String mainWidget_tabSubscription_title();

	/**
	 * Translated "Users".
	 * 
	 * @return translated "Users"
	 */
	@DefaultMessage("Users")
	@Key("mainWidget.tabUsers.title")
	String mainWidget_tabUsers_title();

	/**
	 * Translated "Connections".
	 * 
	 * @return translated "Connections"
	 */
	@DefaultMessage("Connections")
	@Key("mainWidget.tabConnections.title")
	String mainWidget_tabConnections_title();

	/**
	 * Translated "Errors".
	 * 
	 * @return translated "Errors"
	 */
	@DefaultMessage("Errors")
	@Key("mainWidget.tabErrors.title")
	String mainWidget_tabErrors_title();


	

	/** USER WIDGET **/

	/**
	 * Translated "Name".
	 * 
	 * @return translated "Name"
	 */
	@DefaultMessage("Name")
	@Key("userWidget.nameField.title")
	String userWidget_nameFieldL_title();

	/**
	 * Translated "Period".
	 * 
	 * @return translated "Period"
	 */
	@DefaultMessage("Period")
	@Key("userWidget.periodField.title")
	String userWidget_periodFieldL_title();

	/**
	 * Translated "Msgs Sent".
	 * 
	 * @return translated "Msgs Sent"
	 */
	@DefaultMessage("Msgs Sent")
	@Key("userWidget.msgsSentField.title")
	String userWidget_msgsSentFieldL_title();

	/**
	 * Translated "Subscription".
	 * 
	 * @return translated "Subscription"
	 */
	@DefaultMessage("Subscription")
	@Key("userWidget.subscriptionField.title")
	String userWidget_subscriptionFieldL_title();
	
	/**
	 * Translated "Actions".
	 * 
	 * @return translated "Actions"
	 */
	@DefaultMessage("Actions")
	@Key("userWidget.actionsSection.title")
	String userWidget_actionsSection_title();

	/**
	 * Translated "Users".
	 * 
	 * @return translated "Users"
	 */
	@DefaultMessage("Users")
	@Key("userWidget.usersSection.title")
	String userWidget_usersSection_title();
	
	/**
	 * Translated "User Details".
	 * 
	 * @return translated "User Details"
	 */
	@DefaultMessage("User Details")
	@Key("userWidget.userDetailsSection.title")
	String userWidget_userDetailsSection_title();
	
	/**
	 * Translated "Select a user to view more details".
	 * 
	 * @return translated "Select a user to view more details"
	 */
	@DefaultMessage("Select a user to view more details")
	@Key("userWidget.userDetail.emptyMessage")
	String userWidget_userDetail_emptyMessage();	
	
	
	
	/** USERDETAILS WIDGET **/

	/**
	 * Translated "User Details".
	 * 
	 * @return translated "User Details"
	 */
	@DefaultMessage("User Details")
	@Key("userWidget.userDetailsSection.title")
	String userDetailsWidget_userDetailsSection_title();
	
	/**
	 * Translated "Subscriptions".
	 * 
	 * @return translated "Subscriptions"
	 */
	@DefaultMessage("Subscriptions")
	@Key("userDetailsWidget.subscriptionsSection.title")
	String userDetailsWidget_subscriptionsSection_title();
	
	/**
	 * Translated "Subscription Details".
	 * 
	 * @return translated "Subscription Details"
	 */
	@DefaultMessage("Subscription Details")
	@Key("userDetailsWidget.subscriptionDetailsSection.title")
	String userDetailsWidget_subscriptionDetailsSection_title();
	
	/**
	 * Translated "Select a message to view more details".
	 * 
	 * @return translated "Select a message to view more details"
	 */
	@DefaultMessage("Select a message to view more details")
	@Key("userDetailWidget.messageDetail.emptyMessage")
	String userDetailWidget_messageDetail_emptyMessage();	
	
	
	

	/** SUBSCRIPTION WIDGET **/

	/** Translated "Name".
	 * 
	 * @return translated "Name"
	 */
	@DefaultMessage("Name")
	@Key("subscriptionWidget.nameFieldL.title")
	String subscriptionWidget_nameFieldL_title(); 
	
	/** Translated "Active".
	 * 
	 * @return translated "Active"
	 */
	@DefaultMessage("Active")
	@Key("subscriptionWidget.activeFieldL.title")
	String subscriptionWidget_activeFieldL_title(); 
	
	/** Translated "Msgs Delivered".
	 * 
	 * @return translated "Msgs Delivered"
	 */
	@DefaultMessage("Msgs Delivered")
	@Key("subscriptionWidget.msgsDeliveredFieldL.title")
	String subscriptionWidget_msgsDeliveredFieldL_title(); 

	/** Translated "Msgs Sent".
	 * 
	 * @return translated "Msgs Sent"
	 */
	@DefaultMessage("Msgs Sent")
	@Key("subscriptionWidget.msgsSentFieldL.title")
	String subscriptionWidget_msgsSentFieldL_title(); 

	/** Translated "Pending".
	 * 
	 * @return translated "Pending"
	 */
	@DefaultMessage("Pending")
	@Key("subscriptionWidget.pendingFieldL.title")
	String subscriptionWidget_pendingFieldL_title();
	
	/** Translated "Name".
	 * 
	 * @return translated "Name"
	 */
	@DefaultMessage("Name")
	@Key("subscriptionWidget.nameFieldD.title")
	String subscriptionWidget_nameFieldD_title(); 
	
	/** Translated "Active".
	 * 
	 * @return translated "Active"
	 */
	@DefaultMessage("Active")
	@Key("subscriptionWidget.activeFieldD.title")
	String subscriptionWidget_activeFieldD_title(); 
	
	/** Translated "Nb Max Msgs".
	 * 
	 * @return translated "Nb Max Msgs"
	 */
	@DefaultMessage("Nb Max Msgs")
	@Key("subscriptionWidget.nbMaxMsgsFieldD.title")
	String subscriptionWidget_nbMaxMsgsFieldD_title();
	
	/** Translated "Context Id".
	 * 
	 * @return translated "Context Id"
	 */
	@DefaultMessage("Context Id")
	@Key("subscriptionWidget.contextIdFieldD.title")
	String subscriptionWidget_contextIdFieldD_title();
	
	/** Translated "Msgs Delivered".
	 * 
	 * @return translated "Msgs Delivered"
	 */
	@DefaultMessage("Msgs Delivered")
	@Key("subscriptionWidget.msgsDeliveredFieldD.title")
	String subscriptionWidget_msgsDeliveredFieldD_title(); 

	/** Translated "Msgs Sent".
	 * 
	 * @return translated "Msgs Sent"
	 */
	@DefaultMessage("Msgs Sent")
	@Key("subscriptionWidget.msgsSentFieldD.title")
	String subscriptionWidget_msgsSentFieldD_title();
	
	/** Translated "Pending".
	 * 
	 * @return translated "Pending"
	 */
	@DefaultMessage("Pending")
	@Key("subscriptionWidget.pendingFieldD.title")
	String subscriptionWidget_pendingFieldD_title();
	
	/** Translated "Selector".
	 * 
	 * @return translated "Selector"
	 */
	@DefaultMessage("Selector")
	@Key("subscriptionWidget.selectorFieldD.title")
	String subscriptionWidget_selectorFieldD_title();
	
	/** Translated "SubRequest".
	 * 
	 * @return translated "SubRequest"
	 */
	@DefaultMessage("SubRequest")
	@Key("subscriptionWidget.subRequestFieldD.title")
	String subscriptionWidget_subRequestFieldD_title();

	/** Translated "Actions".
	 * 
	 * @return translated "Actions"
	 */
	@DefaultMessage("Actions")
	@Key("subscriptionWidget.actionsSection.title")
	String subscriptionWidget_actionsSection_title();
	
	/** Translated "Subscriptions".
	 * 
	 * @return translated "Subscriptions"
	 */
	@DefaultMessage("Subscriptions")
	@Key("subscriptionWidget.subscriptionsSection.title")
	String subscriptionWidget_subscriptionsSection_title();
	
	/** Translated "Subscription Details".
	 * 
	 * @return translated "Subscription Details"
	 */
	@DefaultMessage("Subscription Details")
	@Key("subscriptionWidget.subscriptionDetailsSection.title")
	String subscriptionWidget_subscriptionDetailsSection_title();
	
	/**
	 * Translated "Select a subscription to view more details".
	 * 
	 * @return translated "Select a subscription to view more details"
	 */
	@DefaultMessage("Select a subscription to view more details")
	@Key("subscriptionWidget.subscriptionDetail.emptyMessage")
	String subscriptionWidget_subscriptionDetail_emptyMessage();
	
	
	
	
	/** SUBSCRIPTION DETAIL WIDGET **/
	
	/** Translated "Subscription Details".
	 * 
	 * @return translated "Subscription Details"
	 */
	@DefaultMessage("Subscription Details")
	@Key("subscriptionDetailWidget.subscriptionDetailsSection.title")
	String subscriptionDetailWidget_subscriptionDetailsSection_title();
	
	/** Translated "Messages".
	 * 
	 * @return translated "Messages"
	 */
	@DefaultMessage("Messages")
	@Key("subscriptionDetailWidget.messagesSection.title")
	String subscriptionDetailWidget_messagesSection_title();
	
	/** Translated "Message Details".
	 * 
	 * @return translated "Message Details"
	 */
	@DefaultMessage("Messages Details")
	@Key("subscriptionDetailWidget.messageDetailsSection.title")
	String subscriptionDetailWidget_messageDetailsSection_title();
	
	/**
	 * Translated "Select a message to view more details".
	 * 
	 * @return translated "Select a message to view more details"
	 */
	@DefaultMessage("Select a message to view more details")
	@Key("subscriptionDetailWidget.messageDetail.emptyMessage")
	String subscriptionDetailWidget_messageDetail_emptyMessage();
	
	
	
	
	
	/** LOGIN WIDGET **/
	
	/** Translated "Username".
	 * 
	 * @return translated "Username"
	 */
	@DefaultMessage("Username")
	@Key("loginWidget.usernameField.title")
	String loginWidget_usernameField_title();
	
	/** Translated "Password".
	 * 
	 * @return translated "Password"
	 */
	@DefaultMessage("Password")
	@Key("loginWidget.passwordField.title")
	String loginWidget_passwordField_title();
	
	/** Translated "Password".
	 * 
	 * @return translated "Password"
	 */
	@DefaultMessage("Login")
	@Key("loginWidget.loginButton.title")
	String loginWidget_loginButton_title();
	
	
	
	/** SERVER WIDGET **/
	

	/** Translated "Queues".
	 * 
	 * @return translated "Queues"
	 */
	@DefaultMessage("Queues")
	@Key("serverWidget.queues")
	String serverWidget_queues();
	
	/** Translated "Topics".
	 * 
	 * @return translated "Topics"
	 */
	@DefaultMessage("Topics")
	@Key("serverWidget.topics")
	String serverWidget_topics();
	
	/** Translated "Users".
	 * 
	 * @return translated "Users"
	 */
	@DefaultMessage("Users")
	@Key("serverWidget.users")
	String serverWidget_users();
	
	/** Translated "Subscriptions".
	 * 
	 * @return translated "Subscriptions"
	 */
	@DefaultMessage("Subscriptions")
	@Key("serverWidget.subscriptions")
	String serverWidget_subscriptions();
	
	/** Translated "Count"
	 * 
	 * @return translated "Count"
	 */
	@DefaultMessage("Count")
	@Key("serverWidget.count")
	String serverWidget_count();
	
	/** Translated "Engine"
	 * 
	 * @return translated "Engine"
	 */
	@DefaultMessage("Engine")
	@Key("serverWidget.engine")
	String serverWidget_engine();
	
	/** Translated "Network"
	 * 
	 * @return translated "Network"
	 */
	@DefaultMessage("Network")
	@Key("serverWidget.network")
	String serverWidget_network();
	
	/** Translated "Avg 1 min"
	 * 
	 * @return translated "Avg 1 min"
	 */
	@DefaultMessage("Avg 1 min")
	@Key("serverWidget.avg1min")
	String serverWidget_avg1min();
	
	/** Translated "Avg 5 min"
	 * 
	 * @return translated "Avg 5 min"
	 */
	@DefaultMessage("Avg 5 min")
	@Key("serverWidget.avg5min")
	String serverWidget_avg5min();
	
	/** Translated "Avg 15 min"
	 * 
	 * @return translated "Avg 15 min"
	 */
	@DefaultMessage("Avg 15 min")
	@Key("serverWidget.avg15min")
	String serverWidget_avg15min();
	
	
	
	
	
	
	
	
	/** ALL **/

	/**
	 * Translated "en".
	 * 
	 * @return translated "en"
	 */
	@DefaultMessage("en")
	@Key("locale")
	String locale();
	
	/**
	 * Translated "Time".
	 * 
	 * @return translated "Time"
	 */
	@DefaultMessage("Time")
	@Key("common_time")
	String common_time();
	
	/**
	 * Translated "Delivered".
	 * 
	 * @return translated "Delivered"
	 */
	@DefaultMessage("Delivered")
	@Key("common_delivered")
	String common_delivered();
	
	/**
	 * Translated "Recieved".
	 * 
	 * @return translated "Recieved"
	 */
	@DefaultMessage("Recieved")
	@Key("common_recieved")
	String common_recieved();
	
	/**
	 * Translated "Sent DMQ".
	 * 
	 * @return translated "Sent DMQ"
	 */
	@DefaultMessage("Sent DMQ")
	@Key("common_sentDMQ")
	String common_sentDMQ();
	
	/**
	 * Translated "Pending".
	 * 
	 * @return translated "Pending"
	 */
	@DefaultMessage("Pending")
	@Key("common_pending")
	String common_pending();
	
	/**
	 * Translated "Subscription Count".
	 * 
	 * @return translated "Subscription Count"
	 */
	@DefaultMessage("Subscription Count")
	@Key("common_subscription")
	String common_subscription();

}
