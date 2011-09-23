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
import com.scalagent.appli.client.presenter.SubscriptionListPresenter;
import com.scalagent.appli.client.widget.handler.queue.RefreshAllClickHandler;
import com.scalagent.appli.client.widget.handler.subscription.NewSubscriptionClickHandler;
import com.scalagent.appli.client.widget.handler.subscription.SubscriptionDeleteClickHandler;
import com.scalagent.appli.client.widget.handler.subscription.SubscriptionDetailsClickHandler;
import com.scalagent.appli.client.widget.handler.subscription.SubscriptionEditClickHandler;
import com.scalagent.appli.client.widget.record.QueueListRecord;
import com.scalagent.appli.client.widget.record.SubscriptionListRecord;
import com.scalagent.appli.shared.SubscriptionWTO;
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
public class SubscriptionListWidget extends BaseWidget<SubscriptionListPresenter> {

  int chartWidth;
  boolean redrawChart = false;
  Options chartOptions;
  int lastFill = 5;

  boolean showDelivered = true;
  boolean showSentDMQ = true;
  boolean showPending = true;

  SectionStack subStack;

  SectionStackSection buttonSection;
  HLayout hl;
  IButton refreshButton;
//  IButton newSubButton;

  SectionStackSection listStackSection;
  ListGrid subList;

  SectionStackSection viewSection;
  HLayout subView;
  DetailViewer subDetailLeft;
  DetailViewer subDetailRight;
  VLayout subChart;

  AnnotatedTimeLine chart;
  DynamicForm columnForm;
  CheckboxItem showDeliveredBox;
  CheckboxItem showSentDMQBox;
  CheckboxItem showPendingBox;

  Window winModal;

  public SubscriptionListWidget(SubscriptionListPresenter subscriptionPresenter) {
    super(subscriptionPresenter);
  }

  public IButton getRefreshButton() {
    return refreshButton;
  }

  @Override
  public Widget asWidget() {

    subStack = new SectionStack();
    subStack.setVisibilityMode(VisibilityMode.MULTIPLE);
    subStack.setWidth100();
    subStack.setHeight100();

    refreshButton = new IButton();
    refreshButton.setAutoFit(Boolean.TRUE);
    refreshButton.setIcon("refresh.gif");
    refreshButton.setTitle(Application.messages.queueWidget_buttonRefresh_title());
    refreshButton.setPrompt(Application.messages.queueWidget_buttonRefresh_prompt());
    refreshButton.addClickHandler(new RefreshAllClickHandler(presenter));

//    newSubButton = new IButton();
//    newSubButton.setMargin(0);
//    newSubButton.setAutoFit(Boolean.TRUE);
//    newSubButton.setIcon("new.png");
//    newSubButton.setTitle(Application.messages.subscriptionWidget_buttonNewSubscription_title());
//    newSubButton.setPrompt(Application.messages.subscriptionWidget_buttonNewSubscription_prompt());
//    newSubButton.addClickHandler(new ClickHandler() {
//      public void onClick(ClickEvent event) {
//        drawForm(null);
//      }
//    });

    hl = new HLayout();
    hl.setHeight(22);
    hl.setPadding(5);
    hl.setMembersMargin(5);
    hl.addMember(refreshButton);
//    hl.addMember(newSubButton);

    buttonSection = new SectionStackSection(Application.messages.subscriptionWidget_actionsSection_title());
    buttonSection.setExpanded(Boolean.TRUE);
    buttonSection.addItem(hl);

    subList = new ListGrid() {

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
          buttonBrowse.addClickHandler(new SubscriptionDetailsClickHandler(presenter,
              (SubscriptionListRecord) record));

          return buttonBrowse;

        } else if (fieldName.equals("deleteField")) {

          IButton buttonDelete = new IButton();
          buttonDelete.setAutoFit(Boolean.TRUE);
          buttonDelete.setHeight(20);
          buttonDelete.setIconSize(13);
          buttonDelete.setIcon("remove.png");
          buttonDelete.setTitle(Application.messages.subscriptionWidget_buttonDelete_title());
          buttonDelete.setPrompt(Application.messages.subscriptionWidget_buttonDelete_prompt());
          buttonDelete.addClickHandler(new SubscriptionDeleteClickHandler(presenter,
              (SubscriptionListRecord) record));
          buttonDelete.setDisabled(true);

          return buttonDelete;

        } else if (fieldName.equals("editField")) {

          IButton buttonEdit = new IButton();
          buttonEdit.setAutoFit(Boolean.TRUE);
          buttonEdit.setHeight(20);
          buttonEdit.setIconSize(13);
          buttonEdit.setIcon("pencil.png");
          buttonEdit.setTitle(Application.messages.subscriptionWidget_buttonEdit_title());
          buttonEdit.setPrompt(Application.messages.subscriptionWidget_buttonEdit_prompt());
          buttonEdit.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
              drawForm((SubscriptionListRecord) record);
            }
          });
          return buttonEdit;

        } else {
          return null;
        }
      }
    };
    subList.setRecordComponentPoolingMode(RecordComponentPoolingMode.VIEWPORT);
    subList.setAlternateRecordStyles(Boolean.TRUE);
    subList.setShowRecordComponents(Boolean.TRUE);
    subList.setShowRecordComponentsByCell(Boolean.TRUE);

    ListGridField nameFieldL = new ListGridField(SubscriptionListRecord.ATTRIBUTE_NAME,
        Application.messages.subscriptionWidget_nameFieldL_title());
    ListGridField activeFieldL = new ListGridField(SubscriptionListRecord.ATTRIBUTE_ACTIVE,
        Application.messages.subscriptionWidget_activeFieldL_title());
    ListGridField nbMsgsDeliveredSinceCreationFieldL = new ListGridField(
        SubscriptionListRecord.ATTRIBUTE_NBMSGSDELIVEREDSINCECREATION,
        Application.messages.subscriptionWidget_msgsDeliveredFieldL_title());
    ListGridField nbMsgsSentToDMQSinceCreationFieldL = new ListGridField(
        SubscriptionListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION,
        Application.messages.subscriptionWidget_msgsSentFieldL_title());
    ListGridField pendingCountFieldL = new ListGridField(
        SubscriptionListRecord.ATTRIBUTE_PENDINGMESSAGECOUNT,
        Application.messages.subscriptionWidget_pendingFieldL_title());
    ListGridField browseField = new ListGridField("browseField",
        Application.messages.queueWidget_browseFieldL_title(), 110);
    browseField.setAlign(Alignment.CENTER);
    ListGridField editField = new ListGridField("editField",
        Application.messages.subscriptionWidget_editFieldL_title(), 110);
    editField.setAlign(Alignment.CENTER);
    ListGridField deleteField = new ListGridField("deleteField",
        Application.messages.subscriptionWidget_deleteFieldL_title(), 110);
    deleteField.setAlign(Alignment.CENTER);

    subList.setFields(nameFieldL, activeFieldL, nbMsgsDeliveredSinceCreationFieldL,
        nbMsgsSentToDMQSinceCreationFieldL, pendingCountFieldL, browseField, editField, deleteField);

    subList.addRecordClickHandler(new RecordClickHandler() {

      @Override
      public void onRecordClick(RecordClickEvent event) {
        subDetailLeft.setData(new Record[] { event.getRecord() });
        subDetailRight.setData(new Record[] { event.getRecord() });
        redrawChart(false);
      }
    });

    DetailViewerField nameFieldD = new DetailViewerField(SubscriptionListRecord.ATTRIBUTE_NAME,
        Application.messages.subscriptionWidget_nameFieldD_title());
    DetailViewerField activeFieldD = new DetailViewerField(SubscriptionListRecord.ATTRIBUTE_ACTIVE,
        Application.messages.subscriptionWidget_activeFieldD_title());
    DetailViewerField nbMaxMsgFieldD = new DetailViewerField(SubscriptionListRecord.ATTRIBUTE_NBMAXMSG,
        Application.messages.subscriptionWidget_nbMaxMsgsFieldD_title());
    DetailViewerField contextIDFieldD = new DetailViewerField(SubscriptionListRecord.ATTRIBUTE_CONTEXTID,
        Application.messages.subscriptionWidget_contextIdFieldD_title());
    DetailViewerField nbMsgsDeliveredSinceCreationFieldD = new DetailViewerField(
        SubscriptionListRecord.ATTRIBUTE_NBMSGSDELIVEREDSINCECREATION,
        Application.messages.subscriptionWidget_msgsDeliveredFieldD_title());
    DetailViewerField nbMsgsSentToDMQSinceCreationFieldD = new DetailViewerField(
        SubscriptionListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION,
        Application.messages.subscriptionWidget_msgsSentFieldD_title());
    DetailViewerField pendingMessageCountFieldD = new DetailViewerField(
        SubscriptionListRecord.ATTRIBUTE_PENDINGMESSAGECOUNT,
        Application.messages.subscriptionWidget_pendingFieldD_title());
    DetailViewerField selectorFieldD = new DetailViewerField(SubscriptionListRecord.ATTRIBUTE_SELECTOR,
        Application.messages.subscriptionWidget_selectorFieldD_title());
    DetailViewerField subRequestIdFieldD = new DetailViewerField(
        SubscriptionListRecord.ATTRIBUTE_SUBREQUESTID,
        Application.messages.subscriptionWidget_subRequestFieldD_title());

    subDetailLeft = new DetailViewer();
    subDetailLeft.setMargin(2);
    subDetailLeft.setWidth("25%");
    subDetailLeft.setLabelSuffix("");
    subDetailLeft.setEmptyMessage(Application.messages.subscriptionWidget_subscriptionDetail_emptyMessage());
    subDetailLeft.setFields(nameFieldD, activeFieldD, nbMaxMsgFieldD, contextIDFieldD,
        nbMsgsDeliveredSinceCreationFieldD);

    subDetailRight = new DetailViewer();
    subDetailRight.setMargin(2);
    subDetailRight.setWidth("25%");
    subDetailRight.setLabelSuffix("");
    subDetailRight.setEmptyMessage(Application.messages.subscriptionWidget_subscriptionDetail_emptyMessage());
    subDetailRight.setFields(nbMsgsSentToDMQSinceCreationFieldD, pendingMessageCountFieldD, selectorFieldD,
        subRequestIdFieldD);

    chartWidth = (com.google.gwt.user.client.Window.getClientWidth() / 2) - 45;
    chart = new AnnotatedTimeLine(createTable(), createOptions(true), chartWidth + "px", "200px");

    columnForm = new DynamicForm();
    columnForm.setNumCols(6);

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
          chart.showDataColumns(0);
        } else {
          chart.hideDataColumns(0);
        }
        enableDisableCheckbox();
      }
    });

    columnForm.setFields(showDeliveredBox, showSentDMQBox, showPendingBox);

    subChart = new VLayout();
    subChart.setMargin(2);
    subChart.setPadding(5);
    subChart.setWidth("50%");
    subChart.setHeight(220);
    subChart.setAlign(Alignment.CENTER);
    subChart.setAlign(VerticalAlignment.TOP);
    subChart.setShowEdges(Boolean.TRUE);
    subChart.setEdgeSize(1);
    subChart.addMember(columnForm);
    subChart.addMember(chart);
    subChart.addDrawHandler(new DrawHandler() {
      @Override
      public void onDraw(DrawEvent event) {
        redrawChart = true;
      }
    });
    subView = new HLayout();
    subView.setMargin(2);
    subView.setPadding(2);
    subView.addMember(subDetailLeft);
    subView.addMember(subDetailRight);
    subView.addMember(subChart);

    //		 Section stack of the queue list
    listStackSection = new SectionStackSection(
        Application.messages.subscriptionWidget_subscriptionsSection_title());
    listStackSection.setExpanded(Boolean.TRUE);
    listStackSection.addItem(subList);

    // Section stack of the view (details & buttons)
    viewSection = new SectionStackSection(
        Application.messages.subscriptionWidget_subscriptionDetailsSection_title());
    viewSection.setExpanded(Boolean.TRUE);
    viewSection.addItem(subView);
    viewSection.setCanReorder(Boolean.TRUE);

    subStack.addSection(buttonSection);
    subStack.addSection(listStackSection);
    subStack.addSection(viewSection);
    subStack.setCanResizeSections(Boolean.TRUE);

    return subStack;
  }

  public void setData(List<SubscriptionWTO> data) {

    SubscriptionListRecord[] subListRecord = new SubscriptionListRecord[data.size()];
    for (int i = 0; i < data.size(); i++) {
      subListRecord[i] = new SubscriptionListRecord(data.get(i));
    }

    subList.setData(subListRecord);
  }

  public void addSubscription(SubscriptionListRecord subRecord) {
    subList.addData(subRecord);
    subList.markForRedraw();
  }

  public void removeSubscription(SubscriptionListRecord subRecord) {
    RecordList list = subList.getDataAsRecordList();
    SubscriptionListRecord toRemove = (SubscriptionListRecord) list.find(
        SubscriptionListRecord.ATTRIBUTE_NAME, subRecord.getName());
    subList.removeData(toRemove);
    subList.markForRedraw();
  }

  public void updateSubscription(SubscriptionWTO sub) {
    SubscriptionListRecord subListRecords = (SubscriptionListRecord) subList.getRecordList().find(
        SubscriptionListRecord.ATTRIBUTE_NAME, sub.getId());
    if (subListRecords != null) {

      subListRecords.setAttribute(SubscriptionListRecord.ATTRIBUTE_NAME, sub.getId());
      subListRecords.setAttribute(SubscriptionListRecord.ATTRIBUTE_ACTIVE, sub.isActive());
      subListRecords.setAttribute(SubscriptionListRecord.ATTRIBUTE_NBMAXMSG, sub.getNbMaxMsg());
      subListRecords.setAttribute(SubscriptionListRecord.ATTRIBUTE_CONTEXTID, sub.getContextId());
      subListRecords.setAttribute(SubscriptionListRecord.ATTRIBUTE_NBMSGSDELIVEREDSINCECREATION,
          sub.getNbMsgsDeliveredSinceCreation());
      subListRecords.setAttribute(SubscriptionListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION,
          sub.getNbMsgsSentToDMQSinceCreation());
      subListRecords.setAttribute(SubscriptionListRecord.ATTRIBUTE_PENDINGMESSAGECOUNT,
          sub.getPendingMessageCount());
      subListRecords.setAttribute(SubscriptionListRecord.ATTRIBUTE_SELECTOR, sub.getSelector());
      subListRecords.setAttribute(SubscriptionListRecord.ATTRIBUTE_SUBREQUESTID, sub.getSubRequestId());

      subListRecords.setSubscription(sub);
      subList.markForRedraw();
    }

    subDetailLeft.setData(new Record[] { subList.getSelectedRecord() });
    subDetailRight.setData(new Record[] { subList.getSelectedRecord() });
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
    chartOptions.setFill(5);
    chartOptions.setLegendPosition(AnnotatedLegendPosition.NEW_ROW);
    chartOptions.setWindowMode(WindowMode.TRANSPARENT);

    return chartOptions;
  }

  private AbstractDataTable createTable() {
    DataTable data = DataTable.create();

    data.addColumn(ColumnType.DATETIME, Application.messages.common_time());
    data.addColumn(ColumnType.NUMBER, Application.messages.common_pending());
    data.addColumn(ColumnType.NUMBER, Application.messages.common_delivered());
    data.addColumn(ColumnType.NUMBER, Application.messages.common_sentDMQ());

    Record selectedRecord = subList.getSelectedRecord();
    if (selectedRecord != null) {
      List<HistoryData> history = presenter.getSubHistory(selectedRecord
          .getAttributeAsString(QueueListRecord.ATTRIBUTE_NAME));
      if (history != null) {
        data.addRows(history.size());
        for (int i = 0; i < history.size(); i++) {
          HistoryData hdata = history.get(i);
          data.setValue(i, 0, hdata.time);
          data.setValue(i, 1, hdata.data[0]);
          data.setValue(i, 2, hdata.data[1]);
          data.setValue(i, 3, hdata.data[2]);
        }
      }
    }
    return data;
  }

  public void redrawChart(boolean reuseChart) {
    if (redrawChart) {
      chart.draw(createTable(), createOptions(reuseChart));
      if (!reuseChart) {
        if (!showDelivered) chart.hideDataColumns(1);
        if (!showSentDMQ) chart.hideDataColumns(2);
        if (!showPending) chart.hideDataColumns(3);
      }
    }
  }

  private void enableDisableCheckbox() {
    if (!showSentDMQ && !showPending) {
      showDeliveredBox.disable();
    } else if (!showDelivered && !showPending) {
      showSentDMQBox.disable();
    } else if (!showDelivered && !showSentDMQ) {
      showPendingBox.disable();
    } else {
      showDeliveredBox.enable();
      showSentDMQBox.enable();
      showPendingBox.enable();
    }
  }

  private void drawForm(SubscriptionListRecord slr) {

    winModal = new Window();
    winModal.setHeight(350);
    winModal.setWidth(400);
    if (slr == null)
      winModal.setTitle(Application.messages.subscriptionWidget_winModal_title());
    else
      winModal.setTitle("Subscription \"" + slr.getAttributeAsString(SubscriptionListRecord.ATTRIBUTE_NAME)
          + "\"");
    winModal.setShowMinimizeButton(Boolean.FALSE);
    winModal.setIsModal(Boolean.TRUE);
    winModal.setShowModalMask(Boolean.TRUE);
    winModal.centerInPage();
    winModal.addCloseClickHandler(new CloseClickHandler() {
      public void onCloseClick(CloseClickEvent event) {
        winModal.destroy();
      }
    });

    Label formTitle = new Label();
    if (slr == null)
      formTitle.setContents(Application.messages.subscriptionWidget_formTitle_title());
    else
      formTitle.setContents("Edit \"" + slr.getAttributeAsString(SubscriptionListRecord.ATTRIBUTE_NAME)
          + "\"");
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

    TextItem nameItem = new TextItem();
    nameItem.setTitle(Application.messages.subscriptionWidget_nameItem_title());
    nameItem.setName("nameItem");
    nameItem.setRequired(Boolean.TRUE);

    TextItem nbMaxMsgItem = new TextItem();
    nbMaxMsgItem.setTitle(Application.messages.subscriptionWidget_nbMaxMsgsItem_title());
    nbMaxMsgItem.setName("nbMaxMsgItem");
    nbMaxMsgItem.setRequired(Boolean.TRUE);
    nbMaxMsgItem.setValidators(integerValidator);

    TextItem contextIdItem = new TextItem();
    contextIdItem.setTitle(Application.messages.subscriptionWidget_contextIdItem_title());
    contextIdItem.setName("contextIdItem");
    contextIdItem.setValidators(integerValidator);
    contextIdItem.setDisabled(Boolean.TRUE);

    TextItem selectorItem = new TextItem();
    selectorItem.setTitle(Application.messages.subscriptionWidget_selectorItem_title());
    selectorItem.setName("selectorItem");
    selectorItem.setDisabled(Boolean.TRUE);

    TextItem subRequestIdItem = new TextItem();
    subRequestIdItem.setTitle(Application.messages.subscriptionWidget_subRequestIdItem_title());
    subRequestIdItem.setName("subRequestIdItem");
    subRequestIdItem.setValidators(integerValidator);
    subRequestIdItem.setDisabled(Boolean.TRUE);

    CheckboxItem activeItem = new CheckboxItem();
    activeItem.setTitle(Application.messages.subscriptionWidget_activeItem_title());
    activeItem.setName("activeItem");
    activeItem.setDisabled(Boolean.TRUE);

    CheckboxItem durableItem = new CheckboxItem();
    durableItem.setTitle(Application.messages.subscriptionWidget_durableItem_title());
    durableItem.setName("durableItem");
    durableItem.setDisabled(Boolean.TRUE);

    if (slr != null) {
      nameItem.setValue(slr.getAttributeAsString(SubscriptionListRecord.ATTRIBUTE_NAME));
      nameItem.setDisabled(Boolean.TRUE);
      nbMaxMsgItem.setValue(slr.getAttributeAsString(SubscriptionListRecord.ATTRIBUTE_NBMAXMSG));
      contextIdItem.setValue(slr.getAttributeAsString(SubscriptionListRecord.ATTRIBUTE_CONTEXTID));
      selectorItem.setValue(slr.getAttributeAsString(SubscriptionListRecord.ATTRIBUTE_SELECTOR));
      subRequestIdItem.setValue(slr.getAttributeAsString(SubscriptionListRecord.ATTRIBUTE_SUBREQUESTID));
      activeItem.setValue(slr.getAttributeAsBoolean(SubscriptionListRecord.ATTRIBUTE_ACTIVE));
      durableItem.setValue(slr.getAttributeAsBoolean(SubscriptionListRecord.ATTRIBUTE_DURABLE));
    }

    form.setFields(nameItem, nbMaxMsgItem, contextIdItem, selectorItem, subRequestIdItem, activeItem,
        durableItem);

    IButton validateButton = new IButton();
    if (slr == null) {
      validateButton.setTitle(Application.messages.subscriptionWidget_validateButton_titleCreate());
      validateButton.setIcon("add.png");
      validateButton.addClickHandler(new NewSubscriptionClickHandler(presenter, form));
    } else {
      validateButton.setTitle(Application.messages.subscriptionWidget_validateButton_titleEdit());
      validateButton.setIcon("accept.png");
      validateButton.addClickHandler(new SubscriptionEditClickHandler(presenter, form));
    }
    validateButton.setAutoFit(Boolean.TRUE);
    validateButton.setLayoutAlign(VerticalAlignment.TOP);
    validateButton.setLayoutAlign(Alignment.CENTER);

    IButton cancelButton = new IButton();
    cancelButton.setTitle(Application.messages.subscriptionWidget_cancelButton_title());
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