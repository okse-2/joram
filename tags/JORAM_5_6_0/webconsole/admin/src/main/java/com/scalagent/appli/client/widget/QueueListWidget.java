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
package com.scalagent.appli.client.widget;

import java.util.HashMap;
import java.util.List;

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
import com.scalagent.appli.client.presenter.QueueListPresenter;
import com.scalagent.appli.client.widget.handler.queue.ClearPendingMessageClickHandler;
import com.scalagent.appli.client.widget.handler.queue.ClearWaintingRequestClickHandler;
import com.scalagent.appli.client.widget.handler.queue.NewQueueClickHandler;
import com.scalagent.appli.client.widget.handler.queue.QueueDeleteClickHandler;
import com.scalagent.appli.client.widget.handler.queue.QueueDetailsClickHandler;
import com.scalagent.appli.client.widget.handler.queue.QueueEditClickHandler;
import com.scalagent.appli.client.widget.handler.queue.RefreshAllClickHandler;
import com.scalagent.appli.client.widget.record.QueueListRecord;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.engine.client.widget.BaseWidget;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Cursor;
import com.smartgwt.client.types.RecordComponentPoolingMode;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.events.DrawEvent;
import com.smartgwt.client.widgets.events.DrawHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.events.MouseOverEvent;
import com.smartgwt.client.widgets.events.MouseOverHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.validator.IntegerRangeValidator;
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
public class QueueListWidget extends BaseWidget<QueueListPresenter> {

  int chartWidth;
  boolean redrawChart = false;
  Options chartOptions;
  int lastFill = 5;

  boolean showReceived = true;
  boolean showDelivered = true;
  boolean showSentDMQ = true;
  boolean showPending = true;

  SectionStackSection buttonSection;
  HLayout hl;
  IButton refreshButton;
  IButton newQueueButton;

  SectionStackSection listStackSection;
  ListGrid queueList;

  SectionStackSection viewSection;
  HLayout queueView;
  DetailViewer queueDetailLeft;
  DetailViewer queueDetailRight;

  VLayout queueChart;

  AnnotatedTimeLine chart;
  DynamicForm columnForm;
  CheckboxItem showReceivedBox;
  CheckboxItem showDeliveredBox;
  CheckboxItem showSentDMQBox;
  CheckboxItem showPendingBox;

  Window winModal;

  HashMap<String, String> etat = new HashMap<String, String>();

  public QueueListWidget(QueueListPresenter queuePresenter) {
    super(queuePresenter);

    etat.put("true", Application.messages.main_true());
    etat.put("false", Application.messages.main_false());
  }

  public IButton getRefreshButton() {
    return refreshButton;
  }

  @Override
  public Widget asWidget() {

    SectionStack queueStack = new SectionStack();
    queueStack.setVisibilityMode(VisibilityMode.MULTIPLE);
    queueStack.setWidth100();
    queueStack.setHeight100();

    refreshButton = new IButton();
    refreshButton.setAutoFit(Boolean.TRUE);
    refreshButton.setIcon("refresh.gif");
    refreshButton.setTitle(Application.messages.queueWidget_buttonRefresh_title());
    refreshButton.setPrompt(Application.messages.queueWidget_buttonRefresh_prompt());
    refreshButton.addClickHandler(new RefreshAllClickHandler(presenter));

    newQueueButton = new IButton();
    newQueueButton.setMargin(0);
    newQueueButton.setAutoFit(Boolean.TRUE);
    newQueueButton.setIcon("new.png");
    newQueueButton.setTitle(Application.messages.queueWidget_buttonNewQueue_title());
    newQueueButton.setPrompt(Application.messages.queueWidget_buttonNewQueue_prompt());
    newQueueButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        drawForm(null);
      }
    });

    hl = new HLayout();
    hl.setHeight(22);
    hl.setPadding(5);
    hl.setMembersMargin(5);
    hl.addMember(refreshButton);
    hl.addMember(newQueueButton);

    buttonSection = new SectionStackSection(Application.messages.queueWidget_buttonSection_title());
    buttonSection.setExpanded(Boolean.TRUE);
    buttonSection.addItem(hl);

    // Liste
    queueList = new ListGrid() {

      @Override
      protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {

        String fieldName = this.getFieldName(colNum);

        if (fieldName.equals("browseField")) {

          IButton buttonBrowse = new IButton();
          buttonBrowse.setAutoFit(Boolean.TRUE);
          buttonBrowse.setHeight(20);
          buttonBrowse.setIconSize(13);
          buttonBrowse.setIcon("view_right_p.png");
          buttonBrowse.setTitle(Application.messages.queueWidget_buttonBrowse_title());
          buttonBrowse.setPrompt(Application.messages.queueWidget_buttonBrowse_prompt());
          buttonBrowse.addClickHandler(new QueueDetailsClickHandler(presenter, (QueueListRecord) record));

          return buttonBrowse;

        } else if (fieldName.equals("actionField")) {

          VLayout vl = new VLayout();
          vl.setWidth(130);
          vl.setHeight(30);
          vl.setAlign(Alignment.CENTER);

          Label lab1 = new Label(Application.messages.queueWidget_labelClearWaitingRequests_title());
          lab1.setTooltip(Application.messages.queueWidget_labelClearWaitingRequests_tooltip());
          lab1.addClickHandler(new ClearWaintingRequestClickHandler(presenter, (QueueListRecord) record));
          setAllParam(lab1);

          Label lab2 = new Label(Application.messages.queueWidget_labelClearPendingMessages_title());
          lab2.setTooltip(Application.messages.queueWidget_labelClearPendingMessages_tooltip());
          lab2.addClickHandler(new ClearPendingMessageClickHandler(presenter, (QueueListRecord) record));
          setAllParam(lab2);

          vl.addMember(lab1);
          vl.addMember(lab2);

          return vl;

        } else if (fieldName.equals("deleteField")) {

          IButton buttonDelete = new IButton();
          buttonDelete.setAutoFit(Boolean.TRUE);
          buttonDelete.setHeight(20);
          buttonDelete.setIconSize(13);
          buttonDelete.setIcon("remove.png");
          buttonDelete.setTitle(Application.messages.queueWidget_buttonDelete_title());
          buttonDelete.setPrompt(Application.messages.queueWidget_buttonDelete_prompt());
          buttonDelete.addClickHandler(new QueueDeleteClickHandler(presenter, (QueueListRecord) record));

          return buttonDelete;

        } else if (fieldName.equals("editField")) {

          IButton buttonEdit = new IButton();
          buttonEdit.setAutoFit(Boolean.TRUE);
          buttonEdit.setHeight(20);
          buttonEdit.setIconSize(13);
          buttonEdit.setIcon("pencil.png");
          buttonEdit.setTitle(Application.messages.queueWidget_buttonEdit_title());
          buttonEdit.setPrompt(Application.messages.queueWidget_buttonEdit_prompt());
          buttonEdit.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
              drawForm((QueueListRecord) record);
            }
          });
          return buttonEdit;

        } else {
          return null;
        }
      }
    };

    queueList.setRecordComponentPoolingMode(RecordComponentPoolingMode.VIEWPORT);
    queueList.setAlternateRecordStyles(Boolean.TRUE);
    queueList.setShowRecordComponents(Boolean.TRUE);
    queueList.setShowRecordComponentsByCell(Boolean.TRUE);
    queueList.setCellHeight(34);

    ListGridField nameFieldL = new ListGridField(QueueListRecord.ATTRIBUTE_NAME,
        Application.messages.queueWidget_nameFieldL_title(), 100);
    ListGridField nbMsgsDeliverSinceCreationFieldL = new ListGridField(
        QueueListRecord.ATTRIBUTE_NBMSGSDELIVERSINCECREATION,
        Application.messages.queueWidget_nbMsgsDeliverSinceCreationFieldL_title());
    ListGridField nbMsgsRecieveSinceCreationFieldL = new ListGridField(
        QueueListRecord.ATTRIBUTE_NBMSGSRECEIVESINCECREATION,
        Application.messages.queueWidget_nbMsgsReceiveSinceCreationFieldL_title());
    ListGridField nbMsgsSentToDMQSinceCreationFieldL = new ListGridField(
        QueueListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION,
        Application.messages.queueWidget_nbMsgsSentToDMQSinceCreationFieldL_title());
    ListGridField waitingRequestCountFieldL = new ListGridField(
        QueueListRecord.ATTRIBUTE_WAITINGREQUESTCOUNT,
        Application.messages.queueWidget_waitingRequestFieldL_title());
    ListGridField pendingMessageCountFieldL = new ListGridField(
        QueueListRecord.ATTRIBUTE_PENDINGMESSAGECOUNT,
        Application.messages.queueWidget_pendingMessageFieldL_title());
    ListGridField browseField = new ListGridField("browseField",
        Application.messages.queueWidget_browseFieldL_title(), 110);
    browseField.setAlign(Alignment.CENTER);
    ListGridField actionField = new ListGridField("actionField",
        Application.messages.queueWidget_actionFieldL_title(), 130);
    actionField.setAlign(Alignment.CENTER);
    ListGridField deleteField = new ListGridField("deleteField",
        Application.messages.queueWidget_deleteFieldL_title(), 114);
    deleteField.setAlign(Alignment.CENTER);
    ListGridField editField = new ListGridField("editField",
        Application.messages.queueWidget_editFieldL_title(), 60);
    editField.setAlign(Alignment.CENTER);

    queueList.setFields(nameFieldL, nbMsgsDeliverSinceCreationFieldL, nbMsgsRecieveSinceCreationFieldL,
        nbMsgsSentToDMQSinceCreationFieldL, waitingRequestCountFieldL, pendingMessageCountFieldL,
        browseField, actionField, editField, deleteField);
    queueList.addRecordClickHandler(new RecordClickHandler() {
      
      public void onRecordClick(RecordClickEvent event) {
        queueDetailLeft.setData(new Record[] { event.getRecord() });
        queueDetailRight.setData(new Record[] { event.getRecord() });
        redrawChart(false);
      }
    });

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

    chartWidth = (com.google.gwt.user.client.Window.getClientWidth() / 2) - 45;
    chart = new AnnotatedTimeLine(createTable(), createOptions(true), "" + chartWidth, "200");

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
    queueChart.setHeight(220);
    queueChart.setAlign(Alignment.CENTER);
    queueChart.setAlign(VerticalAlignment.TOP);
    queueChart.setShowEdges(Boolean.TRUE);
    queueChart.setEdgeSize(1);
    queueChart.addMember(columnForm);
    queueChart.addMember(chart);

    queueChart.addDrawHandler(new DrawHandler() {
      @Override
      public void onDraw(DrawEvent event) {
        redrawChart = true;
      }
    });

    queueView = new HLayout();
    queueView.setMargin(2);
    queueView.setPadding(2);
    queueView.addMember(queueDetailLeft);
    queueView.addMember(queueDetailRight);
    queueView.addMember(queueChart);

    // Section stack of the queue list
    listStackSection = new SectionStackSection(Application.messages.queueWidget_listStackSection_title());
    listStackSection.setExpanded(Boolean.TRUE);
    listStackSection.addItem(queueList);

    // Section stack of the view (details & buttons)
    viewSection = new SectionStackSection(Application.messages.queueWidget_viewSection_title());
    viewSection.setExpanded(Boolean.TRUE);
    viewSection.addItem(queueView);
    viewSection.setCanReorder(Boolean.TRUE);

    queueStack.addSection(buttonSection);
    queueStack.addSection(listStackSection);
    queueStack.addSection(viewSection);
    queueStack.setCanResizeSections(Boolean.TRUE);

    return queueStack;
  }

  public void setData(List<QueueWTO> data) {

    QueueListRecord[] queueListRecord = new QueueListRecord[data.size()];
    for (int i = 0; i < data.size(); i++) {
      queueListRecord[i] = new QueueListRecord(data.get(i));
    }

    queueList.setData(queueListRecord);
  }

  public void updateQueue(QueueWTO queue) {
    QueueListRecord queueListRecords = (QueueListRecord) queueList.getRecordList().find(
        QueueListRecord.ATTRIBUTE_NAME, queue.getId());
    if (queueListRecords != null) {

      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_NAME, queue.getId());
      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_CREATIONDATE, queue.getCreationDate());
      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_DMQID, queue.getDMQId());
      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_DESTINATIONID, queue.getDestinationId());
      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_NBMSGSDELIVERSINCECREATION,
          queue.getNbMsgsDeliverSinceCreation());
      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_NBMSGSRECEIVESINCECREATION,
          queue.getNbMsgsReceiveSinceCreation());
      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION,
          queue.getNbMsgsSentToDMQSinceCreation());
      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_PERIOD, queue.getPeriod());
      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_RIGHTS, queue.getRights());
      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_FREEREADING, queue.isFreeReading());
      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_FREEWRITING, queue.isFreeWriting());
      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_THRESHOLD, queue.getThreshold());
      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_WAITINGREQUESTCOUNT,
          queue.getWaitingRequestCount());
      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_PENDINGMESSAGECOUNT,
          queue.getPendingMessageCount());
      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_DELIVEREDMESSAGECOUNT,
          queue.getDeliveredMessageCount());
      queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_NBMAXMSG, queue.getNbMaxMsg());

      queueListRecords.setQueue(queue);
      queueList.markForRedraw();

    }

    queueDetailLeft.setData(new Record[] { queueList.getSelectedRecord() });
    queueDetailRight.setData(new Record[] { queueList.getSelectedRecord() });

  }

  public void addQueue(QueueListRecord queue) {
    queueList.addData(queue);
    queueList.markForRedraw();
  }

  public void removeQueue(QueueListRecord queue) {
    RecordList list = queueList.getDataAsRecordList();
    QueueListRecord toRemove = (QueueListRecord) list.find(QueueListRecord.ATTRIBUTE_NAME, queue.getName());
    queueList.removeData(toRemove);
    queueList.markForRedraw();
  }

  private void setAllParam(Label l) {
    l.setStyleName("listLink");
    l.setCursor(Cursor.HAND);
    l.setAlign(Alignment.CENTER);
    l.addMouseOverHandler(new MouseOverHandler() {
      public void onMouseOver(MouseOverEvent event) {
        ((Label) event.getSource()).setStyleName("listLinkHover");
      }
    });
    l.addMouseOutHandler(new MouseOutHandler() {
      public void onMouseOut(MouseOutEvent event) {
        ((Label) event.getSource()).setStyleName("listLink");
      }
    });
    l.setWidth(150);
    l.setHeight(15);
    l.setMargin(0);
    l.setPadding(0);
  }

  private Options createOptions(boolean reuseChart) {
    if (chartOptions != null) {
      /* The following is done to avoid a glitch when redrawing the chart: if
       * the new chart time frame starts later than previous one, old values
       * are kept on the chart. So we change an option to force a clean redraw. */
      if (!reuseChart) {
        if (lastFill == 5) {
          chartOptions.setFill(6);
          lastFill = 6;
        } else {
          chartOptions.setFill(5);
          lastFill = 5;
        }
      }
      return chartOptions;
    }
    chartOptions = Options.create();
    chartOptions.setDisplayAnnotations(false);
    chartOptions.setDisplayAnnotationsFilter(false);
    chartOptions.setDisplayZoomButtons(true);
    chartOptions.setDisplayRangeSelector(false);
    chartOptions.setAllowRedraw(true);
    chartOptions.setDateFormat("dd MMM HH:mm:ss");
    chartOptions.setFill(lastFill);
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

    Record selectedRecord = queueList.getSelectedRecord();
    if (selectedRecord != null) {
      List<HistoryData> history = presenter.getQueueHistory(selectedRecord
          .getAttributeAsString(QueueListRecord.ATTRIBUTE_NAME));

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
    }

    return data;
  }

  public void redrawChart(boolean reuseChart) {
    if (redrawChart) {
      chart.draw(createTable(), createOptions(reuseChart));
      if (!reuseChart) {
        if (!showReceived) chart.hideDataColumns(0);
        if (!showDelivered) chart.hideDataColumns(1);
        if (!showSentDMQ) chart.hideDataColumns(2);
        if (!showPending) chart.hideDataColumns(3);
      }
    }
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

  private void drawForm(QueueListRecord qlr) {

    winModal = new Window();
    winModal.setHeight(350);
    winModal.setWidth(400);
    if (qlr == null)
      winModal.setTitle(Application.messages.queueWidget_winModal_title());
    else
      winModal.setTitle("Queue \"" + qlr.getAttributeAsString(QueueListRecord.ATTRIBUTE_NAME) + "\"");
    winModal.setShowMinimizeButton(Boolean.FALSE);
    winModal.setIsModal(Boolean.TRUE);
    winModal.setShowModalMask(Boolean.TRUE);
    winModal.centerInPage();
    winModal.addCloseClickHandler(new CloseClickHandler() {
      public void onCloseClick(CloseClientEvent event) {
        winModal.destroy();
      }
    });

    Label formTitle = new Label();
    if (qlr == null)
      formTitle.setContents(Application.messages.queueWidget_formTitle_title());
    else
      formTitle.setContents("Edit \"" + qlr.getAttributeAsString(QueueListRecord.ATTRIBUTE_NAME) + "\"");
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

    IntegerRangeValidator integerRangeValidator = new IntegerRangeValidator();

    MaskValidator integerValidator = new MaskValidator();
    integerValidator.setMask("^-?[0-9]*$");

    TextItem nameItem = new TextItem();
    nameItem.setTitle(Application.messages.queueWidget_nameItem_title());
    nameItem.setName("nameItem");
    nameItem.setRequired(Boolean.TRUE);

    if (qlr != null) {
      TextItem DMQItem = new TextItem();
      DMQItem.setTitle(Application.messages.queueWidget_DMQItem_title());
      DMQItem.setName("DMQItem");
      DMQItem.setDisabled(Boolean.TRUE);

      TextItem destinationItem = new TextItem();
      destinationItem.setTitle(Application.messages.queueWidget_destinationItem_title());
      destinationItem.setName("destinationItem");
      destinationItem.setDisabled(Boolean.TRUE);

      TextItem periodItem = new TextItem();
      periodItem.setTitle(Application.messages.queueWidget_periodItem_title());
      periodItem.setName("periodItem");
      periodItem.setRequired(Boolean.TRUE);
      periodItem.setValidators(integerRangeValidator, integerValidator);

      TextItem thresholdItem = new TextItem();
      thresholdItem.setTitle(Application.messages.queueWidget_thresholdItem_title());
      thresholdItem.setName("thresholdItem");
      thresholdItem.setRequired(Boolean.TRUE);
      thresholdItem.setValidators(integerRangeValidator, integerValidator);

      TextItem nbMaxMsgItem = new TextItem();
      nbMaxMsgItem.setTitle(Application.messages.queueWidget_nbMaxMsgsItem_title());
      nbMaxMsgItem.setName("nbMaxMsgItem");
      nbMaxMsgItem.setRequired(Boolean.TRUE);
      nbMaxMsgItem.setValidators(integerRangeValidator, integerValidator);

      CheckboxItem freeReadingItem = new CheckboxItem();
      freeReadingItem.setTitle(Application.messages.queueWidget_freeReadingItem_title());
      freeReadingItem.setName("freeReadingItem");

      CheckboxItem freeWritingItem = new CheckboxItem();
      freeWritingItem.setTitle(Application.messages.queueWidget_freeWritingItem_title());
      freeWritingItem.setName("freeWritingItem");

      nameItem.setValue(qlr.getAttributeAsString(QueueListRecord.ATTRIBUTE_NAME));
      nameItem.setDisabled(Boolean.TRUE);
      DMQItem.setValue(qlr.getAttributeAsString(QueueListRecord.ATTRIBUTE_DMQID));
      destinationItem.setValue(qlr.getAttributeAsString(QueueListRecord.ATTRIBUTE_DESTINATIONID));
      periodItem.setValue(qlr.getAttributeAsString(QueueListRecord.ATTRIBUTE_PERIOD));
      thresholdItem.setValue(qlr.getAttributeAsString(QueueListRecord.ATTRIBUTE_THRESHOLD));
      nbMaxMsgItem.setValue(qlr.getAttributeAsString(QueueListRecord.ATTRIBUTE_NBMAXMSG));
      freeReadingItem.setValue(qlr.getAttributeAsBoolean(QueueListRecord.ATTRIBUTE_FREEREADING));
      freeWritingItem.setValue(qlr.getAttributeAsBoolean(QueueListRecord.ATTRIBUTE_FREEWRITING));

      form.setFields(nameItem, DMQItem, destinationItem, periodItem, thresholdItem, nbMaxMsgItem,
          freeReadingItem, freeWritingItem);

    } else {
      form.setFields(nameItem);
    }


    IButton validateButton = new IButton();
    if (qlr == null) {
      validateButton.setTitle(Application.messages.queueWidget_validateButton_titleCreate());
      validateButton.setIcon("add.png");
      validateButton.addClickHandler(new NewQueueClickHandler(presenter, form));
    } else {
      validateButton.setTitle(Application.messages.queueWidget_validateButton_titleEdit());
      validateButton.setIcon("accept.png");
      validateButton.addClickHandler(new QueueEditClickHandler(presenter, form));
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
