/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2011 ScalAgent Distributed Technologies
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
package com.scalagent.appli.client.widget;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.AnnotatedLegendPosition;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.Options;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.WindowMode;
import com.scalagent.appli.client.Application;
import com.scalagent.appli.client.RPCServiceCacheClient.HistoryData;
import com.scalagent.appli.client.presenter.QueueDetailPresenter;
import com.scalagent.appli.client.widget.handler.message.MessageDeleteClickHandler;
import com.scalagent.appli.client.widget.handler.message.MessageEditClickHandler;
import com.scalagent.appli.client.widget.handler.message.NewMessageClickHandler;
import com.scalagent.appli.client.widget.handler.queue.RefreshAllClickHandler;
import com.scalagent.appli.client.widget.record.MessageListRecord;
import com.scalagent.appli.client.widget.record.QueueListRecord;
import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.engine.client.widget.BaseWidget;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.RecordComponentPoolingMode;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.DrawEvent;
import com.smartgwt.client.widgets.events.DrawHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.validator.MaskValidator;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.viewer.DetailViewer;
import com.smartgwt.client.widgets.viewer.DetailViewerField;

/**
 * @author Yohann CINTRE
 */
public class QueueDetailWidget extends BaseWidget<QueueDetailPresenter> {

  boolean redrawChart = false;
  Options chartOptions;

  int chartWidth;

  boolean showReceived = true;
  boolean showDelivered = true;
  boolean showSentDMQ = true;
  boolean showPending = true;

  SectionStack queueDetailStack;

  SectionStackSection buttonSection;
  VLayout vl;
  HLayout hl;
  IButton refreshButton;
//  IButton newQueueButton;
  HLayout hl2;

  DetailViewer queueDetailLeft = new DetailViewer();
  DetailViewer queueDetailRight = new DetailViewer();

  SectionStackSection listStackSection;
  ListGrid messageList;

  SectionStackSection viewSection;
  HLayout queueView;
  DetailViewer messageDetailLeft;
  DetailViewer messageDetailRight;
  VLayout queueChart;

  AnnotatedTimeLine chart;
  DynamicForm columnForm;
  CheckboxItem showReceivedBox;
  CheckboxItem showDeliveredBox;
  CheckboxItem showSentDMQBox;
  CheckboxItem showPendingBox;

  Window winModal;

  HashMap<String, String> etat = new HashMap<String, String>();

  public QueueDetailWidget(QueueDetailPresenter queueDetailsPresenter) {
    super(queueDetailsPresenter);
    etat.put("true", Application.messages.main_true());
    etat.put("false", Application.messages.main_false());
  }

  public IButton getRefreshButton() {
    return refreshButton;
  }

  @Override
  public Widget asWidget() {

    queueDetailStack = new SectionStack();
    queueDetailStack.setVisibilityMode(VisibilityMode.MULTIPLE);
    queueDetailStack.setWidth100();
    queueDetailStack.setHeight100();

    refreshButton = new IButton();
    refreshButton.setAutoFit(Boolean.TRUE);
    refreshButton.setIcon("refresh.gif");
    refreshButton.setTitle(Application.messages.queueWidget_buttonRefresh_title());
    refreshButton.setPrompt(Application.messages.queueWidget_buttonRefresh_prompt());
    refreshButton.addClickHandler(new RefreshAllClickHandler(presenter));

//    newQueueButton = new IButton();
//    newQueueButton.setMargin(0);
//    newQueueButton.setAutoFit(Boolean.TRUE);
//    newQueueButton.setIcon("new.png");
//    newQueueButton.setTitle(Application.messages.queueDetailWidget_buttonNewMessage_title());
//    newQueueButton.setPrompt(Application.messages.queueDetailWidget_buttonNewMessage_prompt());
//    newQueueButton.addClickHandler(new ClickHandler() {
//      public void onClick(ClickEvent event) {
//        drawForm(null);
//      }
//    });

    hl = new HLayout();
    hl.setHeight(22);
    hl.setPadding(5);
    hl.setMembersMargin(5);
    hl.addMember(refreshButton);
//    hl.addMember(newQueueButton);

    DetailViewerField nameFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_NAME,
        Application.messages.queueWidget_nameFieldD_title());
    DetailViewerField creationDateFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_CREATIONDATE,
        Application.messages.queueWidget_creationDateFieldD_title());
    DetailViewerField DMQIdFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_DMQID,
        Application.messages.queueWidget_DMQIdFieldD_title());
    DetailViewerField destinationIdFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_DESTINATIONID,
        Application.messages.queueWidget_destinationIdFieldD_title());
    DetailViewerField nbMsgsDeliverSinceCreationFieldD = new DetailViewerField(
        QueueListRecord.ATTRIBUTE_NBMSGSDELIVERSINCECREATION,
        Application.messages.queueWidget_nbMsgsDeliverSinceCreationFieldD_title());
    DetailViewerField nbMsgsReceiveSinceCreationFieldD = new DetailViewerField(
        QueueListRecord.ATTRIBUTE_NBMSGSRECEIVESINCECREATION,
        Application.messages.queueWidget_nbMsgsReceiveSinceCreationFieldD_title());
    DetailViewerField nbMsgsSentToDMQSinceCreationFieldD = new DetailViewerField(
        QueueListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION,
        Application.messages.queueWidget_nbMsgsSentToDMQSinceCreationFieldD_title());
    DetailViewerField periodFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_PERIOD,
        Application.messages.queueWidget_periodFieldD_title());
    DetailViewerField rightsFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_RIGHTS,
        Application.messages.queueWidget_RightsFieldD_title());
    DetailViewerField freeReadingFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_FREEREADING,
        Application.messages.queueWidget_freeReadingFieldD_title());
    DetailViewerField freeWritingFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_FREEWRITING,
        Application.messages.queueWidget_freeWritingFieldD_title());
    DetailViewerField thresholdFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_THRESHOLD,
        Application.messages.queueWidget_thresholdFieldD_title());
    DetailViewerField waitingRequestCountFieldD = new DetailViewerField(
        QueueListRecord.ATTRIBUTE_WAITINGREQUESTCOUNT,
        Application.messages.queueWidget_waitingRequestCountFieldD_title());
    DetailViewerField pendingMessageCountFieldD = new DetailViewerField(
        QueueListRecord.ATTRIBUTE_PENDINGMESSAGECOUNT,
        Application.messages.queueWidget_pendingMessageCountFieldD_title());
    DetailViewerField deliveredMessagecountFieldD = new DetailViewerField(
        QueueListRecord.ATTRIBUTE_DELIVEREDMESSAGECOUNT,
        Application.messages.queueWidget_deliveredMessageCountFieldD_title());
    DetailViewerField nbMaxMessFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_NBMAXMSG,
        Application.messages.queueWidget_nbMaxMessFieldD_title());
    freeReadingFieldD.setValueMap(etat);
    freeWritingFieldD.setValueMap(etat);

    queueDetailLeft = new DetailViewer();
    queueDetailLeft.setMargin(2);
    queueDetailLeft.setWidth("25%");
    queueDetailLeft.setLabelSuffix("");
    queueDetailLeft.setEmptyMessage(Application.messages.queueWidget_queueDetail_emptyMessage());
    queueDetailLeft.setFields(nameFieldD, creationDateFieldD, DMQIdFieldD, destinationIdFieldD,
        nbMsgsDeliverSinceCreationFieldD, nbMsgsReceiveSinceCreationFieldD,
        nbMsgsSentToDMQSinceCreationFieldD, periodFieldD);

    queueDetailRight = new DetailViewer();
    queueDetailRight.setMargin(2);
    queueDetailRight.setWidth("25%");
    queueDetailRight.setLabelSuffix("");
    queueDetailRight.setEmptyMessage(Application.messages.queueWidget_queueDetail_emptyMessage());
    queueDetailRight.setFields(rightsFieldD, freeReadingFieldD, freeWritingFieldD, thresholdFieldD,
        waitingRequestCountFieldD, pendingMessageCountFieldD, deliveredMessagecountFieldD, nbMaxMessFieldD);

    queueDetailRight.setData(new Record[] { new QueueListRecord(presenter.getQueue()) });
    queueDetailLeft.setData(new Record[] { new QueueListRecord(presenter.getQueue()) });

    chartWidth = (com.google.gwt.user.client.Window.getClientWidth() / 2) - 35;
    chart = new AnnotatedTimeLine(createTable(), createOptions(), chartWidth + "px", "170px");

    columnForm = new DynamicForm();
    columnForm.setNumCols(8);

    showReceivedBox = new CheckboxItem();
    showReceivedBox.setTitle(Application.messages.common_received());
    showReceivedBox.setValue(true);
    showReceivedBox.addChangedHandler(new ChangedHandler() {
      public void onChanged(ChangedEvent event) {
        showReceived = showReceivedBox.getValueAsBoolean().booleanValue();
        if (showReceived) {
          chart.showDataColumns(0);
        } else {
          chart.hideDataColumns(0);
        }
        enableDisableCheckbox();
      }
    });

    showDeliveredBox = new CheckboxItem();
    showDeliveredBox.setTitle(Application.messages.common_delivered());
    showDeliveredBox.setValue(true);
    showDeliveredBox.addChangedHandler(new ChangedHandler() {
      public void onChanged(ChangedEvent event) {
        showDelivered = showDeliveredBox.getValueAsBoolean().booleanValue();
        if (showDelivered) {
          chart.showDataColumns(1);
        } else {
          chart.hideDataColumns(1);
        }
        enableDisableCheckbox();
      }
    });

    showSentDMQBox = new CheckboxItem();
    showSentDMQBox.setTitle(Application.messages.common_sentDMQ());
    showSentDMQBox.setValue(true);
    showSentDMQBox.addChangedHandler(new ChangedHandler() {
      public void onChanged(ChangedEvent event) {
        showSentDMQ = showSentDMQBox.getValueAsBoolean().booleanValue();
        if (showSentDMQ) {
          chart.showDataColumns(2);
        } else {
          chart.hideDataColumns(2);
        }
        enableDisableCheckbox();
      }
    });

    showPendingBox = new CheckboxItem();
    showPendingBox.setTitle(Application.messages.common_pending());
    showPendingBox.setValue(true);
    showPendingBox.addChangedHandler(new ChangedHandler() {
      public void onChanged(ChangedEvent event) {
        showPending = showPendingBox.getValueAsBoolean().booleanValue();
        if (showPending) {
          chart.showDataColumns(3);
        } else {
          chart.hideDataColumns(3);
        }
        enableDisableCheckbox();
      }
    });

    columnForm.setFields(showReceivedBox, showDeliveredBox, showSentDMQBox, showPendingBox);

    queueChart = new VLayout();
    queueChart.setMargin(2);
    queueChart.setPadding(5);
    queueChart.setWidth("50%");
    queueChart.setHeight(175);
    queueChart.setAlign(Alignment.CENTER);
    queueChart.setAlign(VerticalAlignment.TOP);
    queueChart.setShowEdges(Boolean.TRUE);
    queueChart.setEdgeSize(1);
    queueChart.addMember(columnForm);
    queueChart.addMember(chart);
    queueChart.addDrawHandler(new DrawHandler() {
      public void onDraw(DrawEvent event) {
        redrawChart = true;
      }
    });

    hl2 = new HLayout();
    hl2.setMargin(2);
    hl2.setPadding(2);
    hl2.addMember(queueDetailLeft);
    hl2.addMember(queueDetailRight);
    hl2.addMember(queueChart);

    vl = new VLayout();
    vl.setPadding(0);
    vl.addMember(hl);
    vl.addMember(hl2);

    buttonSection = new SectionStackSection(Application.messages.queueDetailWidget_buttonSection_title());
    buttonSection.setExpanded(Boolean.TRUE);
    buttonSection.addItem(vl);

    // Liste

    messageList = new ListGrid() {

      @Override
      protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {

        String fieldName = this.getFieldName(colNum);

        if (fieldName.equals("deleteField")) {

          IButton buttonDelete = new IButton();
          buttonDelete.setAutoFit(Boolean.TRUE);
          buttonDelete.setHeight(20);
          buttonDelete.setIcon("remove.png");
          buttonDelete.setTitle(Application.messages.queueDetailWidget_buttonDelete_title());
          buttonDelete.setPrompt(Application.messages.queueDetailWidget_buttonDelete_prompt());
          buttonDelete.addClickHandler(new MessageDeleteClickHandler(presenter, (MessageListRecord) record));
          buttonDelete.setDisabled(true);

          return buttonDelete;

        } else if (fieldName.equals("editField")) {

          IButton buttonEdit = new IButton();
          buttonEdit.setAutoFit(Boolean.TRUE);
          buttonEdit.setHeight(20);
          buttonEdit.setIconSize(13);
          buttonEdit.setIcon("pencil.png");
          buttonEdit.setTitle(Application.messages.queueDetailWidget_buttonEdit_title());
          buttonEdit.setPrompt(Application.messages.queueDetailWidget_buttonEdit_prompt());
          buttonEdit.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
              drawForm((MessageListRecord) record);
            }
          });
          buttonEdit.setDisabled(true);
          return buttonEdit;

        } else {
          return null;
        }
      }
    };

    messageList.setRecordComponentPoolingMode(RecordComponentPoolingMode.VIEWPORT);
    messageList.setAlternateRecordStyles(Boolean.TRUE);
    messageList.setShowRecordComponents(Boolean.TRUE);
    messageList.setShowRecordComponentsByCell(Boolean.TRUE);

    ListGridField idSFieldL = new ListGridField(MessageListRecord.ATTRIBUTE_IDS,
        Application.messages.queueDetailWidget_idFieldL_title());
    ListGridField deliverycountFieldL = new ListGridField(MessageListRecord.ATTRIBUTE_DELIVERYCOUNT,
        Application.messages.queueDetailWidget_deliveyCountFieldL_title());
    ListGridField priorityFieldL = new ListGridField(MessageListRecord.ATTRIBUTE_PRIORITY,
        Application.messages.queueDetailWidget_priorityFieldL_title());
    ListGridField typeFieldL = new ListGridField(MessageListRecord.ATTRIBUTE_TYPE,
        Application.messages.queueDetailWidget_typeFieldL_title());
    ListGridField deleteField = new ListGridField("deleteField",
        Application.messages.queueDetailWidget_deleteFieldL_title(), 114);
    deleteField.setAlign(Alignment.CENTER);
    ListGridField editField = new ListGridField("editField",
        Application.messages.queueDetailWidget_editFieldL_title(), 114);
    editField.setAlign(Alignment.CENTER);

    messageList.setFields(idSFieldL, deliverycountFieldL, priorityFieldL, typeFieldL, editField, deleteField);
    messageList.addRecordClickHandler(new RecordClickHandler() {
      public void onRecordClick(RecordClickEvent event) {
        messageDetailLeft.setData(new Record[] { event.getRecord() });
        messageDetailRight.setData(new Record[] { event.getRecord() });
      }
    });

    DetailViewerField idSFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_IDS,
        Application.messages.queueDetailWidget_idFieldD_title());
    DetailViewerField expirationFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_EXPIRATION,
        Application.messages.queueDetailWidget_expirationFieldD_title());
    DetailViewerField timestampFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_TIMESTAMP,
        Application.messages.queueDetailWidget_timestampFieldD_title());
    DetailViewerField deliverycountFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_DELIVERYCOUNT,
        Application.messages.queueDetailWidget_deliveyCountFieldD_title());
    DetailViewerField priorityFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_PRIORITY,
        Application.messages.queueDetailWidget_priorityFieldD_title());
    DetailViewerField typeFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_TYPE,
        Application.messages.queueDetailWidget_typeFieldD_title());
    DetailViewerField textFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_TEXT,
        Application.messages.queueDetailWidget_textFieldD_title());
    DetailViewerField propertiesFieldD = new DetailViewerField(MessageListRecord.ATTRIBUTE_PROPERTIES,
        Application.messages.queueDetailWidget_propertiesFieldD_title());

    messageDetailLeft = new DetailViewer();
    messageDetailLeft.setMargin(2);
    messageDetailLeft.setWidth("50%");
    messageDetailLeft.setLabelSuffix("");
    messageDetailLeft.setEmptyMessage(Application.messages.queueDetailWidget_messageDetail_emptyMessage());
    messageDetailLeft.setFields(idSFieldD, expirationFieldD, timestampFieldD, deliverycountFieldD,
        priorityFieldD);

    messageDetailRight = new DetailViewer();
    messageDetailRight.setMargin(2);
    messageDetailRight.setWidth("50%");
    messageDetailRight.setLabelSuffix("");
    messageDetailRight.setEmptyMessage(Application.messages.queueDetailWidget_messageDetail_emptyMessage());
    messageDetailRight.setFields(typeFieldD, textFieldD, propertiesFieldD);

    //		queueChart = new VLayout();
    //		queueChart.setMargin(2);
    //		queueChart.setWidth("33%");
    //		queueChart.setShowEdges(Boolean.TRUE);
    //		queueChart.setAlign(Alignment.CENTER);
    //		queueChart.setAlign(VerticalAlignment.CENTER);

    queueView = new HLayout();
    queueView.setMargin(0);
    queueView.setPadding(2);
    queueView.addMember(messageDetailLeft);
    queueView.addMember(messageDetailRight);
    //		queueView.addMember(queueChart);

    // Section stack of the queue list
    listStackSection = new SectionStackSection(Application.messages.queueDetailWidget_listSection_title());
    listStackSection.setExpanded(Boolean.TRUE);
    listStackSection.addItem(messageList);

    // Section stack of the view (details & buttons)
    viewSection = new SectionStackSection(Application.messages.queueDetailWidget_detailsSection_title());
    viewSection.setExpanded(Boolean.TRUE);
    viewSection.addItem(queueView);
    viewSection.setCanReorder(Boolean.TRUE);

    queueDetailStack.addSection(buttonSection);
    queueDetailStack.addSection(listStackSection);
    queueDetailStack.addSection(viewSection);
    queueDetailStack.setCanResizeSections(Boolean.TRUE);

    presenter.initList();

    return queueDetailStack;

  }

  public void setData(List<MessageWTO> data) {

    MessageListRecord[] messageListRecord = new MessageListRecord[data.size()];
    for (int i = 0; i < data.size(); i++) {
      messageListRecord[i] = new MessageListRecord(data.get(i));
    }

    messageList.setData(messageListRecord);
    messageList.setShowAllRecords(Boolean.TRUE);
  }

  public void updateMessage(MessageWTO message) {
    MessageListRecord messageListRecords = (MessageListRecord) messageList.getRecordList().find(
        MessageListRecord.ATTRIBUTE_IDS, message.getId());
    if (messageListRecords != null) {

      messageListRecords.setIdS(message.getId());
      messageListRecords.setExpiration(message.getExpiration());
      messageListRecords.setTimestamp(new Date(message.getTimestamp()));
      messageListRecords.setDeliveryCount(message.getDeliveryCount());
      messageListRecords.setPriority(message.getPriority());
      messageListRecords.setText(message.getText());
      messageListRecords.setType(message.getType());

      messageListRecords.setMessage(message);
      messageList.markForRedraw();

    }

    messageDetailLeft.setData(new Record[] { messageList.getSelectedRecord() });
    messageDetailRight.setData(new Record[] { messageList.getSelectedRecord() });

  }

  public void addMessage(MessageListRecord message) {
    messageList.addData(message);
    messageList.markForRedraw();
  }

  public void removeMessage(MessageListRecord message) {
    RecordList list = messageList.getDataAsRecordList();
    MessageListRecord toRemove = (MessageListRecord) list.find(MessageListRecord.ATTRIBUTE_IDS,
        message.getIdS());
    if (toRemove != null)
      messageList.removeData(toRemove);
    messageList.markForRedraw();
  }

  public void updateQueue() {
    queueDetailLeft.setData(new Record[] { new QueueListRecord(presenter.getQueue()) });
    queueDetailRight.setData(new Record[] { new QueueListRecord(presenter.getQueue()) });
  }

  public void redrawChart() {
    if (redrawChart) {
      chart.draw(createTable(), createOptions());
    }
  }

  private Options createOptions() {
    if (chartOptions != null) {
      return chartOptions;
    }
    chartOptions = Options.create();
    chartOptions.setDisplayAnnotations(false);
    chartOptions.setDisplayAnnotationsFilter(false);
    chartOptions.setDisplayZoomButtons(true);
    chartOptions.setDisplayRangeSelector(false);
    chartOptions.setAllowRedraw(true);
    chartOptions.setDateFormat("dd MMM HH:mm:ss");
    chartOptions.setFill(5);
    chartOptions.setLegendPosition(AnnotatedLegendPosition.NEW_ROW);
    chartOptions.setWindowMode(WindowMode.TRANSPARENT);

    return chartOptions;
  }

  private AbstractDataTable createTable() {
    DataTable data = DataTable.create();

    data.addColumn(ColumnType.DATETIME, Application.messages.common_time());
    data.addColumn(ColumnType.NUMBER, Application.messages.common_received());
    data.addColumn(ColumnType.NUMBER, Application.messages.common_delivered());
    data.addColumn(ColumnType.NUMBER, Application.messages.common_sentDMQ());
    data.addColumn(ColumnType.NUMBER, Application.messages.common_pending());

    List<HistoryData> history = presenter.getQueueHistory();
    if (history != null) {
      data.addRows(history.size());
      for (int i = 0; i < history.size(); i++) {
        HistoryData hdata = history.get(i);
        data.setValue(i, 0, hdata.time);
        data.setValue(i, 1, hdata.data[0]);
        data.setValue(i, 2, hdata.data[1]);
        data.setValue(i, 3, hdata.data[2]);
        data.setValue(i, 4, hdata.data[3]);
      }
    }

    return data;
  }

  public void stopChart() {
    redrawChart = false;
  }

  private void enableDisableCheckbox() {
    if (!showDelivered && !showSentDMQ && !showPending) {
      showReceivedBox.disable();
    } else if (!showReceived && !showSentDMQ && !showPending) {
      showDeliveredBox.disable();
    } else if (!showReceived && !showDelivered && !showPending) {
      showSentDMQBox.disable();
    } else if (!showReceived && !showDelivered && !showSentDMQ) {
      showPendingBox.disable();
    } else {
      showReceivedBox.enable();
      showDeliveredBox.enable();
      showSentDMQBox.enable();
      showPendingBox.enable();
    }
  }

  private void drawForm(MessageListRecord mlr) {

    winModal = new Window();
    winModal.setHeight(350);
    winModal.setWidth(400);
    if (mlr == null)
      winModal.setTitle(Application.messages.queueDetailWidget_winModal_title());
    else
      winModal.setTitle("Message \"" + mlr.getAttributeAsString(MessageListRecord.ATTRIBUTE_IDS) + "\"");
    winModal.setShowMinimizeButton(false);
    winModal.setIsModal(Boolean.TRUE);
    winModal.setShowModalMask(Boolean.TRUE);
    winModal.centerInPage();
    winModal.addCloseClickHandler(new CloseClickHandler() {
      public void onCloseClick(CloseClickEvent event) {
        winModal.destroy();
      }
    });

    Label formTitle = new Label();
    if (mlr == null)
      formTitle.setContents(Application.messages.queueDetailWidget_formTitle_title());
    else
      formTitle.setContents("Edit \"" + mlr.getAttributeAsString(MessageListRecord.ATTRIBUTE_IDS) + "\"");
    formTitle.setWidth100();
    formTitle.setAutoHeight();
    formTitle.setMargin(5);
    formTitle.setStyleName("title2");
    formTitle.setLayoutAlign(VerticalAlignment.TOP);
    formTitle.setLayoutAlign(Alignment.CENTER);

    final DynamicForm form = new DynamicForm();
    form.setWidth100();
    form.setPadding(5);
    form.setMargin(10);
    form.setLayoutAlign(VerticalAlignment.TOP);
    form.setLayoutAlign(Alignment.CENTER);

    MaskValidator integerValidator = new MaskValidator();
    integerValidator.setMask("^-?[0-9]*$");

    Map<String, QueueWTO> mapQueues = presenter.getQueues();
    LinkedHashMap<String, String> mapNames = new LinkedHashMap<String, String>();

    for (String name : mapQueues.keySet()) {
      mapNames.put(name, name);
    }

    SelectItem queueNameItem = new SelectItem();
    queueNameItem.setTitle(Application.messages.queueDetailWidget_queueNameItem_title());
    queueNameItem.setName("queueNameItem");
    queueNameItem.setRequired(Boolean.TRUE);
    queueNameItem.setValueMap(mapNames);
    queueNameItem.setRequired(Boolean.TRUE);
    queueNameItem.setDefaultValue(presenter.getQueue().getId());

    TextItem idItem = new TextItem();
    idItem.setTitle(Application.messages.queueDetailWidget_idItem_title());
    idItem.setName("idItem");
    idItem.setRequired(Boolean.TRUE);

    TextItem expirationItem = new TextItem();
    expirationItem.setTitle(Application.messages.queueDetailWidget_expirationItem_title());
    expirationItem.setName("expirationItem");
    expirationItem.setRequired(Boolean.TRUE);
    expirationItem.setValidators(integerValidator);

    TextItem timestampItem = new TextItem();
    timestampItem.setTitle(Application.messages.queueDetailWidget_timestampItem_title());
    timestampItem.setName("timestampItem");
    timestampItem.setRequired(Boolean.TRUE);
    timestampItem.setValidators(integerValidator);

    TextItem priorityItem = new TextItem();
    priorityItem.setTitle(Application.messages.queueDetailWidget_priorityItem_title());
    priorityItem.setName("priorityItem");
    priorityItem.setRequired(Boolean.TRUE);
    priorityItem.setValidators(integerValidator);

    TextItem textItem = new TextItem();
    textItem.setTitle(Application.messages.queueDetailWidget_textItem_title());
    textItem.setName("textItem");
    textItem.setRequired(Boolean.TRUE);

    TextItem typeItem = new TextItem();
    typeItem.setTitle(Application.messages.queueDetailWidget_typeItem_title());
    typeItem.setName("typeItem");
    typeItem.setRequired(Boolean.TRUE);
    typeItem.setValidators(integerValidator);

    //		queueNameItem.setValue(presenter.getQueue().getName());

    if (mlr != null) {
      queueNameItem.setDisabled(Boolean.TRUE);
      idItem.setValue(mlr.getAttributeAsString(MessageListRecord.ATTRIBUTE_IDS));
      expirationItem.setValue(mlr.getAttributeAsString(MessageListRecord.ATTRIBUTE_EXPIRATION));
      timestampItem.setValue(mlr.getAttributeAsString(MessageListRecord.ATTRIBUTE_TIMESTAMP));
      priorityItem.setValue(mlr.getAttributeAsString(MessageListRecord.ATTRIBUTE_PRIORITY));
      textItem.setValue(mlr.getAttributeAsString(MessageListRecord.ATTRIBUTE_TEXT));
      typeItem.setValue(mlr.getAttributeAsString(MessageListRecord.ATTRIBUTE_TYPE));
    }

    form.setFields(queueNameItem, idItem, expirationItem, timestampItem, priorityItem, textItem, typeItem);

    IButton validateButton = new IButton();
    if (mlr == null) {
      validateButton.setTitle(Application.messages.queueDetailWidget_validateButton_titleCreate());
      validateButton.setIcon("add.png");
      validateButton.addClickHandler(new NewMessageClickHandler(presenter, form));
    } else {
      validateButton.setTitle(Application.messages.queueDetailWidget_validateButton_titleEdit());
      validateButton.setIcon("accept.png");
      validateButton.addClickHandler(new MessageEditClickHandler(presenter, form));
    }
    validateButton.setAutoFit(Boolean.TRUE);
    validateButton.setLayoutAlign(VerticalAlignment.TOP);
    validateButton.setLayoutAlign(Alignment.CENTER);

    IButton cancelButton = new IButton();
    cancelButton.setTitle(Application.messages.queueWidget_cancelButton_title());
    cancelButton.setIcon("cancel.png");
    cancelButton.setAutoFit(Boolean.TRUE);
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        destroyForm();
      }
    });
    cancelButton.setLayoutAlign(VerticalAlignment.TOP);
    cancelButton.setLayoutAlign(Alignment.CENTER);

    HLayout hl = new HLayout();
    hl.setWidth100();
    hl.setAlign(Alignment.CENTER);
    hl.setAlign(VerticalAlignment.CENTER);
    hl.setMembersMargin(5);
    hl.addMember(validateButton);
    hl.addMember(cancelButton);

    winModal.addItem(formTitle);
    winModal.addItem(form);
    winModal.addItem(hl);
    winModal.show();

  }

  public void destroyForm() {
    winModal.destroy();
  }
}
