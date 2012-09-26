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
import com.scalagent.appli.client.presenter.UserDetailPresenter;
import com.scalagent.appli.client.widget.handler.queue.RefreshAllClickHandler;
import com.scalagent.appli.client.widget.handler.subscription.NewSubscriptionClickHandler;
import com.scalagent.appli.client.widget.handler.subscription.SubscriptionDeleteClickHandler;
import com.scalagent.appli.client.widget.handler.subscription.SubscriptionEditClickHandler;
import com.scalagent.appli.client.widget.record.SubscriptionListRecord;
import com.scalagent.appli.client.widget.record.UserListRecord;
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
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
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
public class UserDetailWidget extends BaseWidget<UserDetailPresenter> {

  boolean redrawChart = false;
  AnnotatedTimeLine chartUser;
  AnnotatedTimeLine chartSub;
  int chartUserWidth;
  int chartSubWidth;
  Options chartOptions;

  boolean showSentDMQ = true;
  boolean showSubCount = true;

  SectionStack mainStack;

  SectionStackSection headerSection;
  VLayout vlHeader;
  HLayout hlHeader;
  IButton refreshButton;
//  IButton newSubButton;
  HLayout hlHeader2;
  DetailViewer userDetail;
  DynamicForm columnForm;
  CheckboxItem showSentDMQBox;
  CheckboxItem showSubCountBox;

  SectionStackSection listSection;
  ListGrid subscriptionList;

  SectionStackSection detailSection;
  HLayout hlDetail;
  DetailViewer subDetailLeft;
  DetailViewer subDetailRight;
  VLayout usrChart;
  VLayout subChart;

  Window winModal;

  private boolean active = true;

  public void setActive(boolean active) {
    this.active = active;
  }

  public UserDetailWidget(UserDetailPresenter userDetailPresenter) {
    super(userDetailPresenter);
  }

  public IButton getRefreshButton() {
    return refreshButton;
  }

  @Override
  public Widget asWidget() {

    mainStack = new SectionStack();
    mainStack.setVisibilityMode(VisibilityMode.MULTIPLE);
    mainStack.setWidth100();
    mainStack.setHeight100();

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

    hlHeader = new HLayout();
    hlHeader.setHeight(22);
    hlHeader.setPadding(5);
    hlHeader.setMembersMargin(5);
    hlHeader.addMember(refreshButton);
//    hlHeader.addMember(newSubButton);

    DetailViewerField nameFieldD = new DetailViewerField(UserListRecord.ATTRIBUTE_NAME,
        Application.messages.userWidget_nameFieldL_title());
    DetailViewerField periodFieldD = new DetailViewerField(UserListRecord.ATTRIBUTE_PERIOD,
        Application.messages.userWidget_periodFieldL_title());
    DetailViewerField nbMsgsSentToDMQSinceCreationFieldD = new DetailViewerField(
        UserListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION,
        Application.messages.userWidget_msgsSentFieldL_title());
    DetailViewerField subscriptionNamesFieldD = new DetailViewerField(
        UserListRecord.ATTRIBUTE_SUBSCRIPTIONNAMES,
        Application.messages.userWidget_subscriptionFieldL_title());

    userDetail = new DetailViewer();
    userDetail.setMargin(2);
    userDetail.setWidth("50%");
    userDetail.setFields(nameFieldD, periodFieldD, nbMsgsSentToDMQSinceCreationFieldD,
        subscriptionNamesFieldD);
    userDetail.setLabelSuffix("");

    userDetail.setData(new Record[] { new UserListRecord(presenter.getUser()) });

    chartUserWidth = (com.google.gwt.user.client.Window.getClientWidth() / 2) - 35;
    chartUser = new AnnotatedTimeLine(createUserTable(), createOptions(), "" + chartUserWidth, "170");

    columnForm = new DynamicForm();
    columnForm.setNumCols(4);

    showSentDMQBox = new CheckboxItem();
    showSentDMQBox.setTitle(Application.messages.common_sentDMQ());
    showSentDMQBox.setValue(true);
    showSentDMQBox.addChangedHandler(new ChangedHandler() {
      public void onChanged(ChangedEvent event) {
        showSentDMQ = showSentDMQBox.getValueAsBoolean().booleanValue();
        if (showSentDMQ) {
          chartUser.showDataColumns(0);
        } else {
          chartUser.hideDataColumns(0);
        }
        enableDisableCheckbox();
      }
    });

    showSubCountBox = new CheckboxItem();
    showSubCountBox.setTitle(Application.messages.common_subscription());
    showSubCountBox.setValue(true);
    showSubCountBox.addChangedHandler(new ChangedHandler() {
      public void onChanged(ChangedEvent event) {
        showSubCount = showSubCountBox.getValueAsBoolean().booleanValue();
        if (showSubCount) {
          chartUser.showDataColumns(1);
        } else {
          chartUser.hideDataColumns(1);
        }
        enableDisableCheckbox();
      }
    });

    columnForm.setFields(showSentDMQBox, showSubCountBox);

    usrChart = new VLayout();
    usrChart.setMargin(2);
    usrChart.setPadding(5);
    usrChart.setWidth("50%");
    usrChart.setHeight(175);
    usrChart.setAlign(Alignment.CENTER);
    usrChart.setAlign(VerticalAlignment.TOP);
    usrChart.setShowEdges(Boolean.TRUE);
    usrChart.setEdgeSize(1);
    usrChart.addMember(columnForm);
    usrChart.addMember(chartUser);
    usrChart.addDrawHandler(new DrawHandler() {
      public void onDraw(DrawEvent event) {
        redrawChart = true;
      }
    });

    hlHeader2 = new HLayout();
    hlHeader2.setMargin(0);
    hlHeader2.setPadding(2);
    hlHeader2.addMember(userDetail);
    hlHeader2.addMember(usrChart);

    vlHeader = new VLayout();
    vlHeader.setPadding(0);
    vlHeader.addMember(hlHeader);
    vlHeader.addMember(hlHeader2);

    headerSection = new SectionStackSection(Application.messages.userDetailsWidget_userDetailsSection_title());
    headerSection.setExpanded(Boolean.TRUE);
    headerSection.addItem(vlHeader);

    // Liste

    subscriptionList = new ListGrid() {

      @Override
      protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {

        String fieldName = this.getFieldName(colNum);

        if (fieldName.equals("deleteField")) {

          IButton buttonDelete = new IButton();
          buttonDelete.setAutoFit(Boolean.TRUE);
          buttonDelete.setHeight(20);
          buttonDelete.setIconSize(13);
          buttonDelete.setIcon("remove.png");
          buttonDelete.setTitle(Application.messages.subscriptionWidget_buttonDelete_title());
          buttonDelete.setPrompt(Application.messages.queueWidget_buttonDelete_prompt());
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

    subscriptionList.setRecordComponentPoolingMode(RecordComponentPoolingMode.VIEWPORT);
    subscriptionList.setAlternateRecordStyles(Boolean.TRUE);
    subscriptionList.setShowRecordComponents(Boolean.TRUE);
    subscriptionList.setShowRecordComponentsByCell(Boolean.TRUE);
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
    ListGridField editField = new ListGridField("editField",
        Application.messages.subscriptionWidget_editFieldL_title(), 110);
    editField.setAlign(Alignment.CENTER);
    ListGridField deleteField = new ListGridField("deleteField",
        Application.messages.subscriptionWidget_deleteFieldL_title(), 110);
    deleteField.setAlign(Alignment.CENTER);
    subscriptionList.setFields(nameFieldL, activeFieldL, nbMsgsDeliveredSinceCreationFieldL,
        nbMsgsSentToDMQSinceCreationFieldL, pendingCountFieldL, editField, deleteField);

    subscriptionList.addRecordClickHandler(new RecordClickHandler() {

      @Override
      public void onRecordClick(RecordClickEvent event) {
        subDetailLeft.setData(new Record[] { event.getRecord() });
        subDetailRight.setData(new Record[] { event.getRecord() });
        redrawChart();
      }
    });

    DetailViewerField nameFieldDSub = new DetailViewerField(SubscriptionListRecord.ATTRIBUTE_NAME,
        Application.messages.subscriptionWidget_nameFieldD_title());
    DetailViewerField activeFieldDSub = new DetailViewerField(SubscriptionListRecord.ATTRIBUTE_ACTIVE,
        Application.messages.subscriptionWidget_activeFieldD_title());
    DetailViewerField nbMaxMsgFieldDSub = new DetailViewerField(SubscriptionListRecord.ATTRIBUTE_NBMAXMSG,
        Application.messages.subscriptionWidget_nbMaxMsgsFieldD_title());
    DetailViewerField contextIDFieldDSub = new DetailViewerField(SubscriptionListRecord.ATTRIBUTE_CONTEXTID,
        Application.messages.subscriptionWidget_contextIdFieldD_title());
    DetailViewerField nbMsgsDeliveredSinceCreationFieldDSub = new DetailViewerField(
        SubscriptionListRecord.ATTRIBUTE_NBMSGSDELIVEREDSINCECREATION,
        Application.messages.subscriptionWidget_msgsDeliveredFieldD_title());
    DetailViewerField nbMsgsSentToDMQSinceCreationFieldDSub = new DetailViewerField(
        SubscriptionListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION,
        Application.messages.subscriptionWidget_msgsSentFieldD_title());
    DetailViewerField pendingMessageCountFieldDSub = new DetailViewerField(
        SubscriptionListRecord.ATTRIBUTE_PENDINGMESSAGECOUNT,
        Application.messages.subscriptionWidget_pendingFieldD_title());
    DetailViewerField selectorFieldDSub = new DetailViewerField(SubscriptionListRecord.ATTRIBUTE_SELECTOR,
        Application.messages.subscriptionWidget_selectorFieldD_title());
    DetailViewerField subRequestIdFieldDSub = new DetailViewerField(
        SubscriptionListRecord.ATTRIBUTE_SUBREQUESTID,
        Application.messages.subscriptionWidget_subRequestFieldD_title());

    subDetailLeft = new DetailViewer();
    subDetailLeft.setMargin(2);
    subDetailLeft.setWidth("25%");
    subDetailLeft.setLabelSuffix("");
    subDetailLeft.setEmptyMessage(Application.messages.userDetailWidget_messageDetail_emptyMessage());
    subDetailLeft.setFields(nameFieldDSub, activeFieldDSub, nbMaxMsgFieldDSub, contextIDFieldDSub,
        nbMsgsDeliveredSinceCreationFieldDSub);

    subDetailRight = new DetailViewer();
    subDetailRight.setMargin(2);
    subDetailRight.setWidth("25%");
    subDetailRight.setLabelSuffix("");
    subDetailRight.setEmptyMessage(Application.messages.userDetailWidget_messageDetail_emptyMessage());
    subDetailRight.setFields(nbMsgsSentToDMQSinceCreationFieldDSub, pendingMessageCountFieldDSub,
        selectorFieldDSub, subRequestIdFieldDSub);

    chartSubWidth = (com.google.gwt.user.client.Window.getClientWidth() / 2) - 35;
    chartSub = new AnnotatedTimeLine(createSubTable(), createOptions(), "" + chartSubWidth, "170");

    subChart = new VLayout();
    subChart.setMargin(2);
    subChart.setPadding(5);
    subChart.setWidth("50%");
    subChart.setHeight(175);
    subChart.setAlign(Alignment.CENTER);
    subChart.setAlign(VerticalAlignment.TOP);
    subChart.setShowEdges(Boolean.TRUE);
    subChart.setEdgeSize(1);
    subChart.addMember(chartSub);

    hlDetail = new HLayout();
    hlDetail.setMargin(0);
    hlDetail.setPadding(2);
    hlDetail.addMember(subDetailLeft);
    hlDetail.addMember(subDetailRight);
    hlDetail.addMember(subChart);

    // Section stack of the queue list
    listSection = new SectionStackSection(Application.messages.userDetailsWidget_subscriptionsSection_title());
    listSection.setExpanded(Boolean.TRUE);
    listSection.addItem(subscriptionList);

    // Section stack of the view (details & buttons)
    detailSection = new SectionStackSection(
        Application.messages.userDetailsWidget_subscriptionDetailsSection_title());
    detailSection.setExpanded(Boolean.TRUE);
    detailSection.addItem(hlDetail);
    detailSection.setCanReorder(Boolean.TRUE);

    mainStack.addSection(headerSection);
    mainStack.addSection(listSection);
    mainStack.addSection(detailSection);
    mainStack.setCanResizeSections(Boolean.TRUE);

    presenter.initList();

    return mainStack;

  }

  public void setData(List<SubscriptionWTO> data) {

    SubscriptionListRecord[] subListRecord = new SubscriptionListRecord[data.size()];
    for (int i = 0; i < data.size(); i++) {
      subListRecord[i] = new SubscriptionListRecord(data.get(i));
    }

    subscriptionList.setData(subListRecord);
  }

  public void updateSubscription(SubscriptionWTO sub) {
    if (active) {

      SubscriptionListRecord subListRecord = (SubscriptionListRecord) subscriptionList.getRecordList().find(
          SubscriptionListRecord.ATTRIBUTE_NAME, sub.getId());
      if (subListRecord != null) {

        subListRecord.setSubscription(sub);
        subListRecord.setSubscription(sub);
        subListRecord.setName(sub.getId());
        subListRecord.setActive(sub.isActive());
        subListRecord.setDurable(sub.isDurable());
        subListRecord.setNbMaxMsg(sub.getNbMaxMsg());
        subListRecord.setContextId(sub.getContextId());
        subListRecord.setNbMsgsDeliveredSinceCreation(sub.getNbMsgsDeliveredSinceCreation());
        subListRecord.setNbMsgsSentToDMQSinceCreation(sub.getNbMsgsSentToDMQSinceCreation());
        subListRecord.setPendingMessageCount(sub.getPendingMessageCount());
        subListRecord.setSelector(sub.getSelector());
        subListRecord.setSubRequestId(sub.getSubRequestId());

        subscriptionList.markForRedraw();
      }

      // Useful when a subscription is already in the cache but not draw on this tab
      else {
        addSubscription(new SubscriptionListRecord(sub));
      }

      subDetailLeft.setData(new Record[] { subscriptionList.getSelectedRecord() });
      subDetailRight.setData(new Record[] { subscriptionList.getSelectedRecord() });
    }
  }

  public void updateUser() {
    userDetail.setData(new Record[] { new UserListRecord(presenter.getUser()) });
  }

  public void addSubscription(SubscriptionListRecord subRec) {
    subscriptionList.addData(subRec);
    subscriptionList.markForRedraw();
  }

  public void removeSubscription(SubscriptionListRecord subRec) {
    RecordList list = subscriptionList.getDataAsRecordList();
    SubscriptionListRecord toRemove = (SubscriptionListRecord) list.find(
        SubscriptionListRecord.ATTRIBUTE_NAME, subRec.getName());
    if (toRemove != null)
      subscriptionList.removeData(toRemove);
    subscriptionList.markForRedraw();
  }

  public void enableRefreshButton() {
    if (active)
      refreshButton.enable();
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

  private AbstractDataTable createUserTable() {
    DataTable data = DataTable.create();

    data.addColumn(ColumnType.DATETIME, Application.messages.common_time());
    data.addColumn(ColumnType.NUMBER, Application.messages.common_sentDMQ());
    data.addColumn(ColumnType.NUMBER, Application.messages.common_subscription());

    List<HistoryData> history = presenter.getUserHistory();
    if (history != null) {
      data.addRows(history.size());
      for (int i = 0; i < history.size(); i++) {
        HistoryData hdata = history.get(i);
        data.setValue(i, 0, hdata.time);
        data.setValue(i, 1, hdata.data[0]);
        data.setValue(i, 2, hdata.data[1]);
      }
    }
    return data;
  }

  private AbstractDataTable createSubTable() {
    DataTable data = DataTable.create();
    data.addColumn(ColumnType.DATETIME, Application.messages.common_time());
    data.addColumn(ColumnType.NUMBER, Application.messages.common_pending());
    data.addColumn(ColumnType.NUMBER, Application.messages.common_delivered());
    data.addColumn(ColumnType.NUMBER, Application.messages.common_sentDMQ());

    Record selectedRecord = subscriptionList.getSelectedRecord();
    if (selectedRecord != null) {
      List<HistoryData> history = presenter.getSubHistory(selectedRecord
          .getAttributeAsString(SubscriptionListRecord.ATTRIBUTE_NAME));
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

  public void redrawChart() {
    if (redrawChart) {
      chartUser.draw(createUserTable(), createOptions());
      chartSub.draw(createSubTable(), createOptions());
    }
  }

  public void stopChart() {
    redrawChart = false;
  }

  private void enableDisableCheckbox() {
    if (!showSubCount) {
      showSentDMQBox.disable();
    } else if (!showSentDMQ) {
      showSubCountBox.disable();
    } else {
      showSentDMQBox.enable();
      showSubCountBox.enable();
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
      public void onCloseClick(CloseClientEvent event) {
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
