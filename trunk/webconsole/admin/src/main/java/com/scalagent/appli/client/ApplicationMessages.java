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
 * Contributor(s): 
 */
package com.scalagent.appli.client;

/**
 * @author Yohann CINTRE
 */
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
   * Translated "Msgs Received".
   * 
   * @return translated "Msgs Received"
   */
  @DefaultMessage("Msgs Received")
  @Key("topicWidget.nbMsgsReceiveSinceCreationFieldL.title")
  String topicWidget_nbMsgsReceivesSinceCreationFieldL_title();

  /**
   * Translated "Msgs Sent to DMQ".
   * 
   * @return translated "Msgs Sent to DMQ"
   */
  @DefaultMessage("Msgs Sent to DMQ")
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
   * Translated "Msgs Received".
   * 
   * @return translated "Msgs Received"
   */
  @DefaultMessage("Msgs Received")
  @Key("topicWidget.nbMsgsReceiveSinceCreationFieldD.title")
  String topicWidget_nbMsgsReceivesSinceCreationFieldD_title();

  /**
   * Translated "Msgs Sent to DMQ".
   * 
   * @return translated "Msgs Sent to DMQ"
   */
  @DefaultMessage("Msgs Sent to DMQ")
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

  /**
   * Translated "New Topic".
   * 
   * @return translated "New Topic"
   */
  @DefaultMessage("New Topic")
  @Key("topicWidget.buttonNewTopic.title")
  String topicWidget_buttonNewTopic_title();

  /**
   * Translated "Click to create a new topic".
   * 
   * @return translated "Click to create a new topic"
   */
  @DefaultMessage("Click to create a new topic")
  @Key("topicWidget.buttonNewTopic.prompt")
  String topicWidget_buttonNewTopic_prompt();

  /**
   * Translated "New Topic".
   * 
   * @return translated "New Topic"
   */
  @DefaultMessage("New Topic")
  @Key("topicWidget.winModal.title")
  String topicWidget_winModal_title();

  /**
   * Translated "Create a new Topic".
   * 
   * @return translated "Create a new Topic"
   */
  @DefaultMessage("Create a new Topic")
  @Key("topicWidget.formTitle.title")
  String topicWidget_formTitle_title();

  /**
   * Translated "Name".
   * 
   * @return translated "Name"
   */
  @DefaultMessage("Name")
  @Key("topicWidget.nameItem.title")
  String topicWidget_nameItem_title();

  /**
   * Translated "DMQ Id".
   * 
   * @return translated "DMQ Id"
   */
  @DefaultMessage("DMQ Id")
  @Key("topicWidget.DMQItem.title")
  String topicWidget_DMQItem_title();

  /**
   * Translated "Destination Id".
   * 
   * @return translated "Destination Id"
   */
  @DefaultMessage("Destination Id")
  @Key("topicWidget.destinationItem.title")
  String topicWidget_destinationItem_title();

  /**
   * Translated "Period".
   * 
   * @return translated "Period"
   */
  @DefaultMessage("Period")
  @Key("topicWidget.periodItem.title")
  String topicWidget_periodItem_title();

  /**
   * Translated "Free Reading".
   * 
   * @return translated "Free Reading"
   */
  @DefaultMessage("Free Reading")
  @Key("topicWidget.freeReadingItem.title")
  String topicWidget_freeReadingItem_title();

  /**
   * Translated "Free Writing".
   * 
   * @return translated "Free Writing"
   */
  @DefaultMessage("Free Writing")
  @Key("topicWidget.freeWritingItem.title")
  String topicWidget_freeWritingItem_title();

  /**
   * Translated "Create Topic".
   * 
   * @return translated "Create Topic"
   */
  @DefaultMessage("Create Topic")
  @Key("topicWidget.validateButton.titleCreate")
  String topicWidget_validateButton_titleCreate();

  /**
   * Translated "Update Topic".
   * 
   * @return translated "Update Topic"
   */
  @DefaultMessage("Update Topic")
  @Key("topicWidget.validateButton.titleEdit")
  String topicWidget_validateButton_titleEdit();

  /**
   * Translated "Cancel".
   * 
   * @return translated "Cancel"
   */
  @DefaultMessage("Cancel")
  @Key("topicWidget.cancelButton.title")
  String topicWidget_cancelButton_title();

  /**
   * Translated "Delete".
   * 
   * @return translated "Delete"
   */
  @DefaultMessage("Delete Topic")
  @Key("topicWidget.buttonDelete.title")
  String topicWidget_buttonDelete_title();

  /**
   * Translated "Edit".
   * 
   * @return translated "Edit"
   */
  @DefaultMessage("Edit")
  @Key("topicWidget.buttonEdit.title")
  String topicWidget_buttonEdit_title();

  /**
   * Translated "Click to delete the topic on JORAM".
   * 
   * @return translated "Click to delete the topic on JORAM"
   */
  @DefaultMessage("Click to delete the topic on JORAM")
  @Key("topicWidget.buttonDelete.prompt")
  String topicWidget_buttonDelete_prompt();

  /**
   * Translated "Click to edit the topic.
   * 
   * @return translated "Click to edit the topic"
   */
  @DefaultMessage("Click to edit the queue")
  @Key("topicWidget.buttonEdit.prompt")
  String topicWidget_buttonEdit_prompt();

  /**
   * Translated "Delete".
   * 
   * @return translated "Delete"
   */
  @DefaultMessage("Delete")
  @Key("topicWidget.deleteFieldL.title")
  String topicWidget_deleteFieldL_title();

  /**
   * Translated "Edit".
   * 
   * @return translated "Edit"
   */
  @DefaultMessage("Edit")
  @Key("topicWidget.editFieldL.title")
  String topicWidget_editFieldL_title();

  /**
   * Translated "Do you really want to delete this topic?".
   * 
   * @return translated "Do you really want to delete this topic?"
   */
  @Key("topicWidget.confirmDelete")
  @DefaultMessage("Do you really want to delete this topic?")
  String topicWidget_confirmDelete();

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
   * Translated "New Queue".
   * 
   * @return translated "New Queue"
   */
  @DefaultMessage("New Queue")
  @Key("queueWidget.buttonNewQueue.title")
  String queueWidget_buttonNewQueue_title();

  /**
   * Translated "Click to create a new queue".
   * 
   * @return translated "Click to create a new queue"
   */
  @DefaultMessage("Click to create a new queue")
  @Key("queueWidget.buttonNewQueue.prompt")
  String queueWidget_buttonNewQueue_prompt();

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
   * Translated "Delete".
   * 
   * @return translated "Delete"
   */
  @DefaultMessage("Delete")
  @Key("queueWidget.buttonDelete.title")
  String queueWidget_buttonDelete_title();

  /**
   * Translated "Edit".
   * 
   * @return translated "Edit"
   */
  @DefaultMessage("Edit")
  @Key("queueWidget.buttonEdit.title")
  String queueWidget_buttonEdit_title();

  /**
   * Translated "Click to delete the queue on JORAM".
   * 
   * @return translated "Click to delete the queue on JORAM"
   */
  @DefaultMessage("Click to delete the queue on JORAM.")
  @Key("queueWidget.buttonDelete.prompt")
  String queueWidget_buttonDelete_prompt();

  /**
   * Translated "Click to edit the queue.
   * 
   * @return translated "Click to edit the queue"
   */
  @DefaultMessage("Click to edit the queue")
  @Key("queueWidget.buttonEdit.prompt")
  String queueWidget_buttonEdit_prompt();

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
   * Translated "Msgs Received".
   * 
   * @return translated "Msgs Received"
   */
  @DefaultMessage("Msgs Received")
  @Key("queueWidget.nbMsgsReceiveSinceCreationFieldD.title")
  String queueWidget_nbMsgsReceiveSinceCreationFieldD_title();

  /**
   * Translated "Msgs Sent to DMQ".
   * 
   * @return translated "Msgs Sent to DMQ"
   */
  @DefaultMessage("Msgs Sent to DMQ")
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
   * Translated "Msgs Received".
   * 
   * @return translated "Msgs Received"
   */
  @DefaultMessage("Msgs Received")
  @Key("queueWidget.nbMsgsReceiveSinceCreationFieldL.title")
  String queueWidget_nbMsgsReceiveSinceCreationFieldL_title();

  /**
   * Translated "Message".
   * 
   * @return translated "Message"
   */
  @DefaultMessage("Message")
  @Key("queueWidget.nbMsgsRecieveSinceCreationFieldL.summary")
  String queueWidget_nbMsgsRecieveSinceCreationFieldL_summary();

  /**
   * Translated "Msgs Sent to DMQ".
   * 
   * @return translated "Msgs Sent to DMQ"
   */
  @DefaultMessage("Msgs Sent to DMQ")
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

  /**
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
   * Translated "Edit".
   * 
   * @return translated "Edit"
   */
  @DefaultMessage("Edit")
  @Key("queueWidget.editFieldL.title")
  String queueWidget_editFieldL_title();

  /**
   * Translated "Do you really want to delete this queue?".
   * 
   * @return translated "Do you really want to delete this queue?"
   */
  @DefaultMessage("Do you really want to delete this queue?")
  @Key("queueWidget.confirmDelete")
  String queueWidget_confirmDelete();

  /**
   * Translated "Clean Waiting Request".
   * 
   * @return translated "Clean Waiting Requests"
   */
  @DefaultMessage("Clean Waiting Requests")
  @Key("queueWidget.labelClearWaitingRequests.title")
  String queueWidget_labelClearWaitingRequests_title();

  /**
   * Translated "Click to clean the expired waiting requests for this Queue".
   * 
   * @return translated
   *         "Click to clean the expired waiting requests for this Queue"
   */
  @DefaultMessage("Click to clean the expired waiting requests for this Queue")
  @Key("queueWidget.labelClearWaitingRequests.tooltip")
  String queueWidget_labelClearWaitingRequests_tooltip();

  /**
   * Translated "Clean Pending Messages".
   * 
   * @return translated "Clean Pending Messages"
   */
  @DefaultMessage("Clean Pending Messages")
  @Key("queueWidget.labelClearPendingMessages.title")
  String queueWidget_labelClearPendingMessages_title();

  /**
   * Translated "Click to clean the expired pending messages for this Queue".
   * 
   * @return translated
   *         "Click to clean the expired pending messages for this Queue"
   */
  @DefaultMessage("Click to clean the expired pending messages for this Queue")
  @Key("queueWidget.labelClearPendingMessages.tooltip")
  String queueWidget_labelClearPendingMessages_tooltip();

  /**
   * Translated "New Queue".
   * 
   * @return translated "New Queue"
   */
  @DefaultMessage("New Queue")
  @Key("queueWidget.winModal.title")
  String queueWidget_winModal_title();

  /**
   * Translated "Create a new Queue".
   * 
   * @return translated "Create a new Queue"
   */
  @DefaultMessage("Create a new Queue")
  @Key("queueWidget.formTitle.title")
  String queueWidget_formTitle_title();

  /**
   * Translated "Name".
   * 
   * @return translated "Name"
   */
  @DefaultMessage("Name")
  @Key("queueWidget.nameItem.title")
  String queueWidget_nameItem_title();

  /**
   * Translated "DMQ Id".
   * 
   * @return translated "DMQ Id"
   */
  @DefaultMessage("DMQ Id")
  @Key("queueWidget.DMQItem.title")
  String queueWidget_DMQItem_title();

  /**
   * Translated "Destination Id".
   * 
   * @return translated "Destination Id"
   */
  @DefaultMessage("Destination Id")
  @Key("queueWidget.destinationItem.title")
  String queueWidget_destinationItem_title();

  /**
   * Translated "Period".
   * 
   * @return translated "Period"
   */
  @DefaultMessage("Period")
  @Key("queueWidget.periodItem.title")
  String queueWidget_periodItem_title();

  /**
   * Translated "Threshold".
   * 
   * @return translated "Threshold"
   */
  @DefaultMessage("Threshold")
  @Key("queueWidget.thresholdItem.title")
  String queueWidget_thresholdItem_title();

  /**
   * Translated "Nb Max Msgs".
   * 
   * @return translated "Nb Max Msgs"
   */
  @DefaultMessage("Nb Max Msgs")
  @Key("queueWidget.nbMaxMsgsItem.title")
  String queueWidget_nbMaxMsgsItem_title();

  /**
   * Translated "Free Reading".
   * 
   * @return translated "Free Reading"
   */
  @DefaultMessage("Free Reading")
  @Key("queueWidget.freeReadingItem.title")
  String queueWidget_freeReadingItem_title();

  /**
   * Translated "Free Writing".
   * 
   * @return translated "Free Writing"
   */
  @DefaultMessage("Free Writing")
  @Key("queueWidget.freeWritingItem.title")
  String queueWidget_freeWritingItem_title();

  /**
   * Translated "Create Queue".
   * 
   * @return translated "Create Queue"
   */
  @DefaultMessage("Create Queue")
  @Key("queueWidget.validateButton.titleCreate")
  String queueWidget_validateButton_titleCreate();

  /**
   * Translated "Update Queue".
   * 
   * @return translated "Update Queue"
   */
  @DefaultMessage("Update Queue")
  @Key("queueWidget.validateButton.titleEdit")
  String queueWidget_validateButton_titleEdit();

  /**
   * Translated "Cancel".
   * 
   * @return translated "Cancel"
   */
  @DefaultMessage("Cancel")
  @Key("queueWidget.cancelButton.title")
  String queueWidget_cancelButton_title();

  /** QUEUEDETAIL WIDGET **/

  /**
   * Translated "Do you really want to delete this message?".
   * 
   * @return translated "Do you really want to delete this message?"
   */
  @DefaultMessage("Do you really want to delete this message?")
  @Key("queueDetailWidget.confirmDelete")
  String queueDetailWidget_confirmDelete();

  /**
   * Translated "Delete".
   * 
   * @return translated "Delete"
   */
  @DefaultMessage("Delete")
  @Key("queueDetailWidget.buttonDelete.title")
  String queueDetailWidget_buttonDelete_title();

  /**
   * Translated "Click to delete this message on JORAM".
   * 
   * @return translated "Click to delete this message on JORAM"
   */
  @DefaultMessage("Action not yet implemented.")
  @Key("queueDetailWidget.buttonDelete.prompt")
  String queueDetailWidget_buttonDelete_prompt();

  /**
   * Translated "Id".
   * 
   * @return translated "Id"
   */
  @DefaultMessage("Id")
  @Key("queueDetailWidget.idFieldL.title")
  String queueDetailWidget_idFieldL_title();

  /**
   * Translated "Persistent".
   * 
   * @return translated "Persistent"
   */
  @DefaultMessage("Persistent")
  @Key("queueDetailWidget.persistentFieldL.title")
  String queueDetailWidget_persistentFieldL_title();

  /**
   * Translated "Redelivered".
   * 
   * @return translated "Redelivered"
   */
  @DefaultMessage("Redelivered")
  @Key("queueDetailWidget.redeliveredFieldL.title")
  String queueDetailWidget_redeliveredFieldL_title();

  /**
   * Translated "Delivery Count".
   * 
   * @return translated "Delivery Count"
   */
  @DefaultMessage("Delivery Count")
  @Key("queueDetailWidget.deliveryCountFieldL.title")
  String queueDetailWidget_deliveyCountFieldL_title();

  /**
   * Translated "Prioprity".
   * 
   * @return translated "Prioprity"
   */
  @DefaultMessage("Prioprity")
  @Key("queueDetailWidget.priorityFieldL.title")
  String queueDetailWidget_priorityFieldL_title();

  /**
   * Translated "Type".
   * 
   * @return translated "Type"
   */
  @DefaultMessage("Type")
  @Key("queueDetailWidget.typeFieldL.title")
  String queueDetailWidget_typeFieldL_title();

  /**
   * Translated "Delete".
   * 
   * @return translated "Delete"
   */
  @DefaultMessage("Delete")
  @Key("queueDetailWidget.deleteFieldL.title")
  String queueDetailWidget_deleteFieldL_title();

  /**
   * Translated "Edit".
   * 
   * @return translated "Edit"
   */
  @DefaultMessage("Edit")
  @Key("queueDetailWidget.editFieldL.title")
  String queueDetailWidget_editFieldL_title();

  /**
   * Translated "Id".
   * 
   * @return translated "Id"
   */
  @DefaultMessage("Id")
  @Key("queueDetailWidget.idFieldD.title")
  String queueDetailWidget_idFieldD_title();

  /**
   * Translated "Expiration".
   * 
   * @return translated "Expiration"
   */
  @DefaultMessage("Expiration")
  @Key("queueDetailWidget.expirationFieldD.title")
  String queueDetailWidget_expirationFieldD_title();

  /**
   * Translated "Index".
   * 
   * @return translated "Index"
   */
  @DefaultMessage("Index")
  @Key("queueDetailWidget.indexFieldD.title")
  String queueDetailWidget_indexFieldD_title();

  /**
   * Translated "Timestamp".
   * 
   * @return translated "Timestamp"
   */
  @DefaultMessage("Timestamp")
  @Key("queueDetailWidget.timestampFieldD.title")
  String queueDetailWidget_timestampFieldD_title();

  /**
   * Translated "Persistent".
   * 
   * @return translated "Persistent"
   */
  @DefaultMessage("Persistent")
  @Key("queueDetailWidget.persistentFieldD.title")
  String queueDetailWidget_persistentFieldD_title();

  /**
   * Translated "Redelivered".
   * 
   * @return translated "Redelivered"
   */
  @DefaultMessage("Redelivered")
  @Key("queueDetailWidget.redeliveredFieldD.title")
  String queueDetailWidget_redeliveredFieldD_title();

  /**
   * Translated "Delivery Count".
   * 
   * @return translated "Delivery Count"
   */
  @DefaultMessage("Delivery Count")
  @Key("queueDetailWidget.deliveryCountFieldD.title")
  String queueDetailWidget_deliveyCountFieldD_title();

  /**
   * Translated "Prioprity".
   * 
   * @return translated "Prioprity"
   */
  @DefaultMessage("Prioprity")
  @Key("queueDetailWidget.priorityFieldD.title")
  String queueDetailWidget_priorityFieldD_title();

  /**
   * Translated "Type".
   * 
   * @return translated "Type"
   */
  @DefaultMessage("Type")
  @Key("queueDetailWidget.typeFieldD.title")
  String queueDetailWidget_typeFieldD_title();

  /**
   * Translated "Text".
   * 
   * @return translated "Text"
   */
  @DefaultMessage("Text")
  @Key("queueDetailWidget.textFieldD.title")
  String queueDetailWidget_textFieldD_title();

  /**
   * Translated "Properties".
   * 
   * @return translated "Properties"
   */
  @DefaultMessage("Properties")
  @Key("queueDetailWidget.propertiesFieldD.title")
  String queueDetailWidget_propertiesFieldD_title();

  /**
   * Translated "This queue no longer exists on JORAM".
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

  /**
   * Translated "Edit".
   * 
   * @return translated "Edit"
   */
  @DefaultMessage("Edit")
  @Key("queueDetailWidget.buttonEdit.title")
  String queueDetailWidget_buttonEdit_title();

  /**
   * Translated "Click to edit the queue.
   * 
   * @return translated "Click to edit the queue"
   */
  @DefaultMessage("Action not yet implemented.")
  @Key("queueDetailWidget.buttonEdit.prompt")
  String queueDetailWidget_buttonEdit_prompt();

  /**
   * Translated "New Message".
   * 
   * @return translated "New Message"
   */
  @DefaultMessage("New Message")
  @Key("queueDetailWidget.winModal.title")
  String queueDetailWidget_winModal_title();

  /**
   * Translated "Create a new Message".
   * 
   * @return translated "Create a new Message"
   */
  @DefaultMessage("Create a new Queue")
  @Key("queueDetailWidget.formTitle.title")
  String queueDetailWidget_formTitle_title();

  /**
   * Translated "Queue".
   * 
   * @return translated "Queue"
   */
  @DefaultMessage("Queue")
  @Key("queueDetailWidget.queueNameItem.title")
  String queueDetailWidget_queueNameItem_title();

  /**
   * Translated "Id".
   * 
   * @return translated "Id"
   */
  @DefaultMessage("Id")
  @Key("queueDetailWidget.idItem.title")
  String queueDetailWidget_idItem_title();

  /**
   * Translated "Expiration".
   * 
   * @return translated "Expiration"
   */
  @DefaultMessage("Expiration")
  @Key("queueDetailWidget.expirationItem.title")
  String queueDetailWidget_expirationItem_title();

  /**
   * Translated "Timestamp".
   * 
   * @return translated "Timestamp"
   */
  @DefaultMessage("Timestamp")
  @Key("queueDetailWidget.timestampItem.title")
  String queueDetailWidget_timestampItem_title();

  /**
   * Translated "Priority".
   * 
   * @return translated "Priority"
   */
  @DefaultMessage("Priority")
  @Key("queueDetailWidget.priorityItem.title")
  String queueDetailWidget_priorityItem_title();

  /**
   * Translated "Text".
   * 
   * @return translated "Text"
   */
  @DefaultMessage("Text")
  @Key("queueDetailWidget.textItem.title")
  String queueDetailWidget_textItem_title();

  /**
   * Translated "Type".
   * 
   * @return translated "Type"
   */
  @DefaultMessage("Type")
  @Key("queueDetailWidget.typeItem.title")
  String queueDetailWidget_typeItem_title();

  /**
   * Translated "New Message".
   * 
   * @return translated "New Message"
   */
  @DefaultMessage("New Message")
  @Key("queueDetailWidget.buttonNewMessage.title")
  String queueDetailWidget_buttonNewMessage_title();

  /**
   * Translated "Click to create a new message".
   * 
   * @return translated "Click to create a new message"
   */
  @DefaultMessage("Click to create a new message")
  @Key("queueDetailWidget.buttonNewMessage.prompt")
  String queueDetailWidget_buttonNewMessage_prompt();

  /**
   * Translated "Update Message".
   * 
   * @return translated "Update Message"
   */
  @DefaultMessage("Update Message")
  @Key("queueDetailWidget.validateButton.titleEdit")
  String queueDetailWidget_validateButton_titleEdit();

  /**
   * Translated "Create Message".
   * 
   * @return translated "Create Message"
   */
  @DefaultMessage("Create Message")
  @Key("queueDetailWidget.validateButton.titleCreate")
  String queueDetailWidget_validateButton_titleCreate();

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
   * Translated "Msgs Sent to DMQ".
   * 
   * @return translated "Msgs Sent to DMQ"
   */
  @DefaultMessage("Msgs Sent to DMQ")
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

  /**
   * Translated "New User".
   * 
   * @return translated "New User"
   */
  @DefaultMessage("New User")
  @Key("userWidget.winModal.title")
  String userWidget_winModal_title();

  /**
   * Translated "Create a new User".
   * 
   * @return translated "Create a new User"
   */
  @DefaultMessage("Create a new User")
  @Key("userWidget.formTitle.title")
  String userWidget_formTitle_title();

  /**
   * Translated "Name".
   * 
   * @return translated "Name"
   */
  @DefaultMessage("Name")
  @Key("userWidget.nameItem.title")
  String userWidget_nameItem_title();

  /**
   * Translated "Password".
   * 
   * @return translated "Password"
   */
  @DefaultMessage("Password")
  @Key("userWidget.passwordItem.title")
  String userWidget_passwordItem_title();

  /**
   * Translated "Period".
   * 
   * @return translated "Period"
   */
  @DefaultMessage("Period")
  @Key("userWidget.periodItem.title")
  String userWidget_periodItem_title();

  /**
   * Translated "Create User".
   * 
   * @return translated "Create User"
   */
  @DefaultMessage("Create User")
  @Key("userWidget.validateButton.titleCreate")
  String userWidget_validateButton_titleCreate();

  /**
   * Translated "Update User".
   * 
   * @return translated "Update User"
   */
  @DefaultMessage("Update User")
  @Key("userWidget.validateButton.titleEdit")
  String userWidget_validateButton_titleEdit();

  /**
   * Translated "Cancel".
   * 
   * @return translated "Cancel"
   */
  @DefaultMessage("Cancel")
  @Key("userWidget.cancelButton.title")
  String userWidget_cancelButton_title();

  /**
   * Translated "New User".
   * 
   * @return translated "New User"
   */
  @DefaultMessage("New User")
  @Key("userWidget.buttonNewUser.title")
  String userWidget_buttonNewUser_title();

  /**
   * Translated "Click to create a new user".
   * 
   * @return translated "Click to create a new user"
   */
  @DefaultMessage("Click to create a new user")
  @Key("userWidget.buttonNewUser.prompt")
  String userWidget_buttonNewUser_prompt();

  /**
   * Translated "Do you really want to delete this user?".
   * 
   * @return translated "Do you really want to delete this user?"
   */
  @DefaultMessage("Do you really want to delete this user?")
  @Key("userWidget.confirmDelete")
  String userWidget_confirmDelete();

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

  /**
   * Translated "Name".
   * 
   * @return translated "Name"
   */
  @DefaultMessage("Name")
  @Key("subscriptionWidget.nameFieldL.title")
  String subscriptionWidget_nameFieldL_title();

  /**
   * Translated "Active".
   * 
   * @return translated "Active"
   */
  @DefaultMessage("Active")
  @Key("subscriptionWidget.activeFieldL.title")
  String subscriptionWidget_activeFieldL_title();

  /**
   * Translated "Msgs Delivered".
   * 
   * @return translated "Msgs Delivered"
   */
  @DefaultMessage("Msgs Delivered")
  @Key("subscriptionWidget.msgsDeliveredFieldL.title")
  String subscriptionWidget_msgsDeliveredFieldL_title();

  /**
   * Translated "Msgs Sent to DMQ".
   * 
   * @return translated "Msgs Sent to DMQ"
   */
  @DefaultMessage("Msgs Sent to DMQ")
  @Key("subscriptionWidget.msgsSentFieldL.title")
  String subscriptionWidget_msgsSentFieldL_title();

  /**
   * Translated "Pending".
   * 
   * @return translated "Pending"
   */
  @DefaultMessage("Pending")
  @Key("subscriptionWidget.pendingFieldL.title")
  String subscriptionWidget_pendingFieldL_title();

  /**
   * Translated "Name".
   * 
   * @return translated "Name"
   */
  @DefaultMessage("Name")
  @Key("subscriptionWidget.nameFieldD.title")
  String subscriptionWidget_nameFieldD_title();

  /**
   * Translated "Active".
   * 
   * @return translated "Active"
   */
  @DefaultMessage("Active")
  @Key("subscriptionWidget.activeFieldD.title")
  String subscriptionWidget_activeFieldD_title();

  /**
   * Translated "Nb Max Msgs".
   * 
   * @return translated "Nb Max Msgs"
   */
  @DefaultMessage("Nb Max Msgs")
  @Key("subscriptionWidget.nbMaxMsgsFieldD.title")
  String subscriptionWidget_nbMaxMsgsFieldD_title();

  /**
   * Translated "Context Id".
   * 
   * @return translated "Context Id"
   */
  @DefaultMessage("Context Id")
  @Key("subscriptionWidget.contextIdFieldD.title")
  String subscriptionWidget_contextIdFieldD_title();

  /**
   * Translated "Msgs Delivered".
   * 
   * @return translated "Msgs Delivered"
   */
  @DefaultMessage("Msgs Delivered")
  @Key("subscriptionWidget.msgsDeliveredFieldD.title")
  String subscriptionWidget_msgsDeliveredFieldD_title();

  /**
   * Translated "Msgs Sent to DMQ".
   * 
   * @return translated "Msgs Sent to DMQ"
   */
  @DefaultMessage("Msgs Sent to DMQ")
  @Key("subscriptionWidget.msgsSentFieldD.title")
  String subscriptionWidget_msgsSentFieldD_title();

  /**
   * Translated "Pending".
   * 
   * @return translated "Pending"
   */
  @DefaultMessage("Pending")
  @Key("subscriptionWidget.pendingFieldD.title")
  String subscriptionWidget_pendingFieldD_title();

  /**
   * Translated "Selector".
   * 
   * @return translated "Selector"
   */
  @DefaultMessage("Selector")
  @Key("subscriptionWidget.selectorFieldD.title")
  String subscriptionWidget_selectorFieldD_title();

  /**
   * Translated "SubRequest".
   * 
   * @return translated "SubRequest"
   */
  @DefaultMessage("SubRequest")
  @Key("subscriptionWidget.subRequestFieldD.title")
  String subscriptionWidget_subRequestFieldD_title();

  /**
   * Translated "Actions".
   * 
   * @return translated "Actions"
   */
  @DefaultMessage("Actions")
  @Key("subscriptionWidget.actionsSection.title")
  String subscriptionWidget_actionsSection_title();

  /**
   * Translated "Subscriptions".
   * 
   * @return translated "Subscriptions"
   */
  @DefaultMessage("Subscriptions")
  @Key("subscriptionWidget.subscriptionsSection.title")
  String subscriptionWidget_subscriptionsSection_title();

  /**
   * Translated "Subscription Details".
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

  /**
   * Translated "New Subscription".
   * 
   * @return translated "New Subscription"
   */
  @DefaultMessage("New Subscription")
  @Key("subscriptionWidget.winModal.title")
  String subscriptionWidget_winModal_title();

  /**
   * Translated "Create a new Subscription".
   * 
   * @return translated "Create a new Subscription"
   */
  @DefaultMessage("Create a new Subscription")
  @Key("subscriptionWidget.formTitle.title")
  String subscriptionWidget_formTitle_title();

  /**
   * Translated "Name".
   * 
   * @return translated "Name"
   */
  @DefaultMessage("Name")
  @Key("subscriptionWidget.nameItem.title")
  String subscriptionWidget_nameItem_title();

  /**
   * Translated "Nb Max Msgs".
   * 
   * @return translated "Nb Max Msgs"
   */
  @DefaultMessage("Nb Max Msgs")
  @Key("subscriptionWidget.nbMaxMsgsItem.title")
  String subscriptionWidget_nbMaxMsgsItem_title();

  /**
   * Translated "Context Id".
   * 
   * @return translated "Context Id"
   */
  @DefaultMessage("Context Id")
  @Key("subscriptionWidget.contextIdItem.title")
  String subscriptionWidget_contextIdItem_title();

  /**
   * Translated "Selector".
   * 
   * @return translated "Selector"
   */
  @DefaultMessage("Selector")
  @Key("subscriptionWidget.selectorItem.title")
  String subscriptionWidget_selectorItem_title();

  /**
   * Translated "SubRequest Id".
   * 
   * @return translated "SubRequest Id"
   */
  @DefaultMessage("SubRequest Id")
  @Key("subscriptionWidget.subRequestIdItem.title")
  String subscriptionWidget_subRequestIdItem_title();

  /**
   * Translated "Active".
   * 
   * @return translated "Active"
   */
  @DefaultMessage("Active")
  @Key("subscriptionWidget.activeItem.title")
  String subscriptionWidget_activeItem_title();

  /**
   * Translated "Durable".
   * 
   * @return translated "Durable"
   */
  @DefaultMessage("Durable")
  @Key("subscriptionWidget.durableItem.title")
  String subscriptionWidget_durableItem_title();

  /**
   * Translated "Create Subscription".
   * 
   * @return translated "Create Subscription"
   */
  @DefaultMessage("Create Subscription")
  @Key("subscriptionWidget.validateButton.titleCreate")
  String subscriptionWidget_validateButton_titleCreate();

  /**
   * Translated "Update Subscription".
   * 
   * @return translated "Update Subscription"
   */
  @DefaultMessage("Update Subscription")
  @Key("subscriptionWidget.validateButton.titleEdit")
  String subscriptionWidget_validateButton_titleEdit();

  /**
   * Translated "Cancel".
   * 
   * @return translated "Cancel"
   */
  @DefaultMessage("Cancel")
  @Key("subscriptionWidget.cancelButton.title")
  String subscriptionWidget_cancelButton_title();

  /**
   * Translated "Do you really want to delete this subscription?".
   * 
   * @return translated "Do you really want to delete this subscription?"
   */
  @DefaultMessage("Do you really want to delete this subscription?")
  @Key("subscriptionWidget.confirmDelete")
  String subscriptionWidget_confirmDelete();

  /**
   * // * Translated "New Subscription".
   * 
   * @return translated "New Subscription"
   */
  @DefaultMessage("New Subscription")
  @Key("subscriptionWidget.buttonNewSubscription.title")
  String subscriptionWidget_buttonNewSubscription_title();

  /**
   * Translated "Click to create a new subscription".
   * 
   * @return translated "Click to create a new subscription"
   */
  @DefaultMessage("Click to create a new subscription")
  @Key("subscriptionWidget.buttonNewSubscription.prompt")
  String subscriptionWidget_buttonNewSubscription_prompt();

  /**
   * Translated "Delete".
   * 
   * @return translated "Delete"
   */
  @DefaultMessage("Delete")
  @Key("subscriptionWidget.buttonDelete.title")
  String subscriptionWidget_buttonDelete_title();

  /**
   * Translated "Click to delete the subscription on JORAM".
   * 
   * @return translated "Click to delete the subscription on JORAM"
   */
  @DefaultMessage("Action not yet implemented.")
  @Key("subscriptionWidget.buttonDelete.prompt")
  String subscriptionWidget_buttonDelete_prompt();

  /**
   * Translated "Edit".
   * 
   * @return translated "Edit"
   */
  @DefaultMessage("Edit")
  @Key("subscriptionWidget.buttonEdit.title")
  String subscriptionWidget_buttonEdit_title();

  /**
   * Translated "Click to edit the subscription.
   * 
   * @return translated "Click to edit the subscription"
   */
  @DefaultMessage("Click to edit the subscription")
  @Key("subscriptionWidget.buttonEdit.prompt")
  String subscriptionWidget_buttonEdit_prompt();

  /**
   * Translated "Delete".
   * 
   * @return translated "Delete"
   */
  @DefaultMessage("Delete")
  @Key("subscriptionWidget.deleteFieldL.title")
  String subscriptionWidget_deleteFieldL_title();

  /**
   * Translated "Edit".
   * 
   * @return translated "Edit"
   */
  @DefaultMessage("Edit")
  @Key("subscriptionWidget.editFieldL.title")
  String subscriptionWidget_editFieldL_title();

  /** SUBSCRIPTION DETAIL WIDGET **/

  /**
   * Translated "Subscription Details".
   * 
   * @return translated "Subscription Details"
   */
  @DefaultMessage("Subscription Details")
  @Key("subscriptionDetailWidget.subscriptionDetailsSection.title")
  String subscriptionDetailWidget_subscriptionDetailsSection_title();

  /**
   * Translated "Messages".
   * 
   * @return translated "Messages"
   */
  @DefaultMessage("Messages")
  @Key("subscriptionDetailWidget.messagesSection.title")
  String subscriptionDetailWidget_messagesSection_title();

  /**
   * Translated "Message Details".
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

  /**
   * Translated "Username".
   * 
   * @return translated "Username"
   */
  @DefaultMessage("Username")
  @Key("loginWidget.usernameField.title")
  String loginWidget_usernameField_title();

  /**
   * Translated "Password".
   * 
   * @return translated "Password"
   */
  @DefaultMessage("Password")
  @Key("loginWidget.passwordField.title")
  String loginWidget_passwordField_title();

  /**
   * Translated "Password".
   * 
   * @return translated "Password"
   */
  @DefaultMessage("Login")
  @Key("loginWidget.loginButton.title")
  String loginWidget_loginButton_title();

  /** SERVER WIDGET **/

  /**
   * Translated "Queues".
   * 
   * @return translated "Queues"
   */
  @DefaultMessage("Queues")
  @Key("serverWidget.queues")
  String serverWidget_queues();

  /**
   * Translated "Topics".
   * 
   * @return translated "Topics"
   */
  @DefaultMessage("Topics")
  @Key("serverWidget.topics")
  String serverWidget_topics();

  /**
   * Translated "Users".
   * 
   * @return translated "Users"
   */
  @DefaultMessage("Users")
  @Key("serverWidget.users")
  String serverWidget_users();

  /**
   * Translated "Subscriptions".
   * 
   * @return translated "Subscriptions"
   */
  @DefaultMessage("Subscriptions")
  @Key("serverWidget.subscriptions")
  String serverWidget_subscriptions();

  /**
   * Translated "Count"
   * 
   * @return translated "Count"
   */
  @DefaultMessage("Count")
  @Key("serverWidget.count")
  String serverWidget_count();

  /**
   * Translated "Engine"
   * 
   * @return translated "Engine"
   */
  @DefaultMessage("Engine")
  @Key("serverWidget.engine")
  String serverWidget_engine();

  /**
   * Translated "Network"
   * 
   * @return translated "Network"
   */
  @DefaultMessage("Network")
  @Key("serverWidget.network")
  String serverWidget_network();

  /**
   * Translated "Avg 1 min"
   * 
   * @return translated "Avg 1 min"
   */
  @DefaultMessage("Avg 1 min")
  @Key("serverWidget.avg1min")
  String serverWidget_avg1min();

  /**
   * Translated "Avg 5 min"
   * 
   * @return translated "Avg 5 min"
   */
  @DefaultMessage("Avg 5 min")
  @Key("serverWidget.avg5min")
  String serverWidget_avg5min();

  /**
   * Translated "Avg 15 min"
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
   * Translated "Received".
   * 
   * @return translated "Received"
   */
  @DefaultMessage("Received")
  @Key("common_received")
  String common_received();

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

  /**
   * Translated "true".
   * 
   * @return translated "true"
   */
  @DefaultMessage("true")
  @Key("main_true")
  String main_true();

  /**
   * Translated "false".
   * 
   * @return translated "false"
   */
  @DefaultMessage("false")
  @Key("main_false")
  String main_false();

}
