/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.AnnotatedLegendPosition;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.Options;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.WindowMode;
import com.scalagent.appli.client.Application;
import com.scalagent.appli.client.presenter.QueueListPresenter;
import com.scalagent.appli.client.widget.handler.queue.ClearPendingMessageClickHandler;
import com.scalagent.appli.client.widget.handler.queue.ClearWaintingRequestClickHandler;
import com.scalagent.appli.client.widget.handler.queue.QueueDeleteClickHandler;
import com.scalagent.appli.client.widget.handler.queue.QueueDetailsClickHandler;
import com.scalagent.appli.client.widget.handler.queue.RefreshAllClickHandler;
import com.scalagent.appli.client.widget.record.QueueListRecord;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.engine.client.widget.BaseWidget;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Cursor;
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


public class QueueListWidget extends BaseWidget<QueueListPresenter> {

	int chartWidth;
	boolean redrawChart = false;

	boolean showRecieved = true;
	boolean showDelivered = true;
	boolean showSentDMQ = true;
	boolean showPending = true;

	SectionStack queueStack;

	SectionStackSection buttonSection;
	HLayout hl;
	IButton refreshButton;
	IButton newQueueButton;

	SectionStackSection listStackSection;
	ListGrid queueList;

	SectionStackSection viewSection;
	HLayout	queueView;
	DetailViewer queueDetailLeft;
	DetailViewer queueDetailRight;

	VLayout queueChart;

	AnnotatedTimeLine chart;
	DynamicForm columnForm;
	CheckboxItem showRecievedBox;
	CheckboxItem showDeliveredBox;
	CheckboxItem showSentDMQBox;
	CheckboxItem showPendingBox;

	HashMap<String, String> etat=new HashMap<String, String>();

	public QueueListWidget(QueueListPresenter queuePresenter) {
		super(queuePresenter);

		etat.put("true", Application.baseMessages.main_true());
		etat.put("false", Application.baseMessages.main_false());
	}

	public IButton getRefreshButton() {
		return refreshButton;
	}



	@Override
	public Widget asWidget() {

		queueStack = new SectionStack();
		queueStack.setVisibilityMode(VisibilityMode.MULTIPLE);
		queueStack.setWidth100();
		queueStack.setHeight100();

		refreshButton = new IButton();
		refreshButton.setMargin(0); 
		refreshButton.setAutoFit(true);
		refreshButton.setIcon("refresh.gif");  
		refreshButton.setTitle(Application.messages.queueWidget_buttonRefresh_title());
		refreshButton.setPrompt(Application.messages.queueWidget_buttonRefresh_prompt());
		refreshButton.addClickHandler(new RefreshAllClickHandler(presenter)); 

		newQueueButton = new IButton(); 
		newQueueButton.setMargin(0);
		newQueueButton.setAutoFit(true);
		newQueueButton.setIcon("new.png");  
		newQueueButton.setTitle("New Queue");
		newQueueButton.setPrompt(Application.messages.queueWidget_buttonRefresh_prompt());
		newQueueButton.addClickHandler(new ClickHandler() {


			@Override
			public void onClick(ClickEvent event) {  
				final Window winModal = new Window();  
				winModal.setWidth(360);  
				winModal.setHeight(400);  
				winModal.setTitle("New Queue");  
				winModal.setShowMinimizeButton(false);  
				winModal.setIsModal(true);  
				winModal.setShowModalMask(true);  
				winModal.centerInPage();  
				winModal.addCloseClickHandler(new CloseClickHandler() {  
					public void onCloseClick(CloseClientEvent event) {  
						winModal.destroy();  
					}  
				});  

				final DynamicForm form = new DynamicForm();  
				form.setWidth100();  
				form.setPadding(5);  
				form.setLayoutAlign(VerticalAlignment.BOTTOM);  

				IntegerRangeValidator integerRangeValidator = new IntegerRangeValidator(); 
				integerRangeValidator.setMin(0);
				integerRangeValidator.setMax(10);

				MaskValidator integerValidator = new MaskValidator();  
				integerValidator.setMask("^[0-9]*$");  

				TextItem nameItem = new TextItem();  
				nameItem.setTitle("Name"); 
				nameItem.setName("nameItem");
				nameItem.setRequired(true);

				TextItem DMQItem = new TextItem();  
				DMQItem.setTitle("DMQ Id"); 
				DMQItem.setName("DMQItem");
				DMQItem.setRequired(true);

				TextItem destinationItem = new TextItem();  
				destinationItem.setTitle("Destination Id");
				destinationItem.setName("destinationItem");
				destinationItem.setRequired(true);

				TextItem periodItem = new TextItem();  
				periodItem.setTitle("Period");
				periodItem.setName("periodItem");
				periodItem.setRequired(true);
				periodItem.setValidators(integerRangeValidator, integerValidator); 

				CheckboxItem freeReadingItem = new CheckboxItem();  
				freeReadingItem.setTitle("Free Reading");
				freeReadingItem.setName("freeReadingItem");

				CheckboxItem freeWritingItem = new CheckboxItem();  
				freeWritingItem.setTitle("Free Writing");
				freeWritingItem.setName("freeWritingItem");


				TextItem thresholdItem = new TextItem();  
				thresholdItem.setTitle("Threshold");
				thresholdItem.setName("thresholdItem");
				thresholdItem.setRequired(true);
				thresholdItem.setValidators(integerRangeValidator, integerValidator);  

				TextItem nbMaxMsgItem = new TextItem();
				nbMaxMsgItem.setTitle("Nb Max Msgs");
				nbMaxMsgItem.setName("nbMaxMsgItem");
				nbMaxMsgItem.setRequired(true);
				nbMaxMsgItem.setValidators(integerRangeValidator, integerValidator);  

				form.setFields(nameItem, 
						DMQItem, 
						destinationItem, 
						periodItem, 
						freeReadingItem, 
						freeWritingItem,
						thresholdItem,
						nbMaxMsgItem);


				IButton validateButton = new IButton();  
				validateButton.setTitle("Validate");  
				validateButton.addClickHandler(new ClickHandler() {  
					public void onClick(ClickEvent event) {  
						try {
							if(form.validate())
							{
								int max = Integer.parseInt((String)(form.getValue("maxmsg")));
								System.out.println("max:"+max);
							}  
							else {
								System.out.println("error! validation");
							}

						} catch (Exception e) {
							System.out.println("error! integer");
						}

					}
				});  
				form.setShowEdges(true);

				winModal.addItem(form);  
				winModal.addItem(validateButton); 
				winModal.show();
			}  
		}); 		

		hl = new HLayout();
		hl.setHeight(22);
		hl.setPadding(5);
		hl.setMembersMargin(5);
		hl.addMember(refreshButton);
		hl.addMember(newQueueButton);

		buttonSection = new SectionStackSection(Application.messages.queueWidget_buttonSection_title());
		buttonSection.setExpanded(true);
		buttonSection.addItem(hl);


		// Liste
		queueList = new ListGrid() {

			@Override  
			protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {  

				String fieldName = this.getFieldName(colNum);  

				if (fieldName.equals("browseField")) { 

					IButton buttonBrowse = new IButton();  
					buttonBrowse.setAutoFit(true);
					buttonBrowse.setHeight(20);
					buttonBrowse.setIconSize(13);
					buttonBrowse.setIcon("view_right_p.png");  
					buttonBrowse.setTitle(Application.messages.queueWidget_buttonBrowse_title());
					buttonBrowse.setPrompt(Application.messages.queueWidget_buttonBrowse_prompt());
					buttonBrowse.addClickHandler(new QueueDetailsClickHandler(presenter, (QueueListRecord) record));

					return buttonBrowse;

				} else if (fieldName.equals("actionField")) { 

					VLayout vl = new VLayout();
					vl.setWidth(130); vl.setHeight(30);
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
					buttonDelete.setAutoFit(true);
					buttonDelete.setHeight(20); 
					buttonDelete.setIconSize(13);
					buttonDelete.setIcon("remove.png");  
					buttonDelete.setTitle(Application.messages.queueWidget_buttonDelete_title());
					buttonDelete.setPrompt(Application.messages.queueWidget_buttonDelete_prompt());
					buttonDelete.addClickHandler(new QueueDeleteClickHandler(presenter, (QueueListRecord) record));

					return buttonDelete;

				} else {  
					return null;                     
				}  	   
			}	
		};


		queueList.setRecordComponentPoolingMode("viewport");
		queueList.setAlternateRecordStyles(true);
		queueList.setShowRecordComponents(true);          
		queueList.setShowRecordComponentsByCell(true);
		queueList.setCellHeight(34);


		ListGridField nameFieldL = new ListGridField(QueueListRecord.ATTRIBUTE_NAME, Application.messages.queueWidget_nameFieldL_title(), 100);		
		ListGridField nbMsgsDeliverSinceCreationFieldL = new ListGridField(QueueListRecord.ATTRIBUTE_NBMSGSDELIVERSINCECREATION, Application.messages.queueWidget_nbMsgsDeliverSinceCreationFieldL_title());
		ListGridField nbMsgsRecieveSinceCreationFieldL = new ListGridField(QueueListRecord.ATTRIBUTE_NBMSGSRECEIVESINCECREATION, Application.messages.queueWidget_nbMsgsRecieveSinceCreationFieldL_title());
		ListGridField nbMsgsSentToDMQSinceCreationFieldL = new ListGridField(QueueListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, Application.messages.queueWidget_nbMsgsSentToDMQSinceCreationFieldL_title());
		ListGridField waitingRequestCountFieldL = new ListGridField(QueueListRecord.ATTRIBUTE_WAITINGREQUESTCOUNT, Application.messages.queueWidget_waitingRequestFieldL_title());
		ListGridField pendingMessageCountFieldL = new ListGridField(QueueListRecord.ATTRIBUTE_PENDINGMESSAGECOUNT, Application.messages.queueWidget_pendingMessageFieldL_title());
		ListGridField browseField = new ListGridField("browseField", Application.messages.queueWidget_browseFieldL_title(), 110);
		browseField.setAlign(Alignment.CENTER);  
		ListGridField actionField = new ListGridField("actionField", Application.messages.queueWidget_actionFieldL_title(), 130);
		actionField.setAlign(Alignment.CENTER);  
		ListGridField deleteField = new ListGridField("deleteField", Application.messages.queueWidget_deleteFieldL_title(), 114);
		deleteField.setAlign(Alignment.CENTER);  

		queueList.setFields(
				nameFieldL, 
				nbMsgsDeliverSinceCreationFieldL, 
				nbMsgsRecieveSinceCreationFieldL, 
				nbMsgsSentToDMQSinceCreationFieldL, 
				waitingRequestCountFieldL,
				pendingMessageCountFieldL,
				browseField, 
				actionField, 
				deleteField);
		queueList.addRecordClickHandler(new RecordClickHandler() {
			public void onRecordClick(RecordClickEvent event) {
				queueDetailLeft.setData(new Record[]{event.getRecord()});
				queueDetailRight.setData(new Record[]{event.getRecord()});
				redrawChart(false);
			}
		});

		DetailViewerField nameFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_NAME, Application.messages.queueWidget_nameFieldD_title());
		DetailViewerField creationDateFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_CREATIONDATE, Application.messages.queueWidget_creationDateFieldD_title());
		DetailViewerField DMQIdFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_DMQID, Application.messages.queueWidget_DMQIdFieldD_title());
		DetailViewerField destinationIdFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_DESTINATIONID, Application.messages.queueWidget_destinationIdFieldD_title());
		DetailViewerField nbMsgsDeliverSinceCreationFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_NBMSGSDELIVERSINCECREATION, Application.messages.queueWidget_nbMsgsDeliverSinceCreationFieldD_title());
		DetailViewerField nbMsgsReceiveSinceCreationFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_NBMSGSRECEIVESINCECREATION, Application.messages.queueWidget_nbMsgsRecieveSinceCreationFieldD_title());
		DetailViewerField nbMsgsSentToDMQSinceCreationFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, Application.messages.queueWidget_nbMsgsSentToDMQSinceCreationFieldD_title());
		DetailViewerField periodFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_PERIOD, Application.messages.queueWidget_periodFieldD_title());
		DetailViewerField rightsFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_RIGHTS, Application.messages.queueWidget_RightsFieldD_title());
		DetailViewerField freeReadingFieldD = new DetailViewerField(QueueListRecord.ATTRIBUTE_FREEREADING, Application.messages.queueWidget_freeReadingFieldD_title());
		DetailViewerField freeWritingFieldD= new DetailViewerField(QueueListRecord.ATTRIBUTE_FREEWRITING, Application.messages.queueWidget_freeWritingFieldD_title());
		DetailViewerField thresholdFieldD= new DetailViewerField(QueueListRecord.ATTRIBUTE_THRESHOLD, Application.messages.queueWidget_thresholdFieldD_title());
		DetailViewerField waitingRequestCountFieldD= new DetailViewerField(QueueListRecord.ATTRIBUTE_WAITINGREQUESTCOUNT, Application.messages.queueWidget_waitingRequestCountFieldD_title());
		DetailViewerField pendingMessageCountFieldD= new DetailViewerField(QueueListRecord.ATTRIBUTE_PENDINGMESSAGECOUNT, Application.messages.queueWidget_pendingMessageCountFieldD_title());
		DetailViewerField deliveredMessagecountFieldD= new DetailViewerField(QueueListRecord.ATTRIBUTE_DELIVEREDMESSAGECOUNT, Application.messages.queueWidget_deliveredMessageCountFieldD_title());
		DetailViewerField nbMaxMessFieldD= new DetailViewerField(QueueListRecord.ATTRIBUTE_NBMAXMSG, Application.messages.queueWidget_nbMaxMessFieldD_title());
		freeReadingFieldD.setValueMap(etat);
		freeWritingFieldD.setValueMap(etat);


		queueDetailLeft = new DetailViewer();
		queueDetailLeft.setMargin(2);
		queueDetailLeft.setWidth("25%");
		queueDetailLeft.setEmptyMessage(Application.messages.queueWidget_queueDetail_emptyMessage());
		queueDetailLeft.setFields(nameFieldD, creationDateFieldD, DMQIdFieldD, destinationIdFieldD, nbMsgsDeliverSinceCreationFieldD, nbMsgsReceiveSinceCreationFieldD, nbMsgsSentToDMQSinceCreationFieldD, periodFieldD);

		queueDetailRight = new DetailViewer();
		queueDetailRight.setMargin(2);
		queueDetailRight.setWidth("25%");
		queueDetailRight.setEmptyMessage(Application.messages.queueWidget_queueDetail_emptyMessage());
		queueDetailRight.setFields(rightsFieldD, freeReadingFieldD, freeWritingFieldD, thresholdFieldD, waitingRequestCountFieldD, pendingMessageCountFieldD, deliveredMessagecountFieldD, nbMaxMessFieldD);


		chartWidth = (com.google.gwt.user.client.Window.getClientWidth()/2)-45;
		chart = new AnnotatedTimeLine(createTable(), createOptions(true), ""+chartWidth, "200");

		columnForm = new DynamicForm();
		columnForm.setNumCols(8);

		showRecievedBox = new CheckboxItem();  
		showRecievedBox.setTitle(Application.messages.common_recieved());
		showRecievedBox.setValue(true);
		showRecievedBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showRecieved = showRecievedBox.getValueAsBoolean();
				enableDisableCheckbox();
				redrawChart(false);
			}
		});

		showDeliveredBox = new CheckboxItem();  
		showDeliveredBox.setTitle(Application.messages.common_delivered());
		showDeliveredBox.setValue(true);
		showDeliveredBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {

				showDelivered = showDeliveredBox.getValueAsBoolean();
				enableDisableCheckbox();
				redrawChart(false);
			}
		});

		showSentDMQBox = new CheckboxItem();  
		showSentDMQBox.setTitle(Application.messages.common_sentDMQ());
		showSentDMQBox.setValue(true);
		showSentDMQBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showSentDMQ = showSentDMQBox.getValueAsBoolean();
				enableDisableCheckbox();
				redrawChart(false);
			}
		});

		showPendingBox = new CheckboxItem();  
		showPendingBox.setTitle(Application.messages.common_pending());
		showPendingBox.setValue(true);
		showPendingBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showPending = showPendingBox.getValueAsBoolean();
				enableDisableCheckbox();
				redrawChart(false);
			}
		});


		columnForm.setFields(showRecievedBox, showDeliveredBox, showSentDMQBox, showPendingBox);


		queueChart = new VLayout();
		queueChart.setMargin(2);
		queueChart.setPadding(5);
		queueChart.setWidth("50%");
		queueChart.setHeight(220);
		queueChart.setAlign(Alignment.CENTER);
		queueChart.setAlign(VerticalAlignment.TOP);
		queueChart.setShowEdges(true);
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
		listStackSection.setExpanded(true);
		listStackSection.addItem(queueList);


		// Section stack of the view (details & buttons)
		viewSection = new SectionStackSection(Application.messages.queueWidget_viewSection_title());
		viewSection.setExpanded(true);
		viewSection.addItem(queueView);
		viewSection.setCanReorder(true);

		queueStack.addSection(buttonSection);
		queueStack.addSection(listStackSection);
		queueStack.addSection(viewSection);
		queueStack.setCanResizeSections(true);

		return queueStack;
	}

	public void setData(List<QueueWTO> data) {

		QueueListRecord[] queueListRecord = new QueueListRecord[data.size()];
		for (int i=0;i<data.size();i++) {
			queueListRecord[i] = new QueueListRecord(data.get(i));
		}

		queueList.setData(queueListRecord);
	}

	public void updateQueue(QueueWTO queue) {
		QueueListRecord queueListRecords = (QueueListRecord)queueList.getRecordList().find(QueueListRecord.ATTRIBUTE_NAME, queue.getName());
		if(queueListRecords != null)  {


			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_NAME, queue.getName()); 
			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_CREATIONDATE, queue.getCreationDate()); 
			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_DMQID, queue.getDMQId()); 
			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_DESTINATIONID, queue.getDestinationId()); 
			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_NBMSGSDELIVERSINCECREATION, queue.getNbMsgsDeliverSinceCreation()); 
			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_NBMSGSRECEIVESINCECREATION, queue.getNbMsgsReceiveSinceCreation()); 
			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, queue.getNbMsgsSentToDMQSinceCreation()); 
			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_PERIOD, queue.getPeriod()); 
			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_RIGHTS, queue.getRights()); 
			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_FREEREADING, queue.isFreeReading()); 
			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_FREEWRITING, queue.isFreeWriting()); 
			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_THRESHOLD, queue.getThreshold()); 
			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_WAITINGREQUESTCOUNT, queue.getWaitingRequestCount()); 
			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_PENDINGMESSAGECOUNT, queue.getPendingMessageCount()); 
			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_DELIVEREDMESSAGECOUNT, queue.getDeliveredMessageCount()); 
			queueListRecords.setAttribute(QueueListRecord.ATTRIBUTE_NBMAXMSG, queue.getNbMaxMsg()); 


			queueListRecords.setQueue(queue);
			queueList.markForRedraw();

		}

		queueDetailLeft.setData(new Record[]{queueList.getSelectedRecord()});
		queueDetailRight.setData(new Record[]{queueList.getSelectedRecord()});

	}

	public void addQueue(QueueListRecord queue) {
		queueList.addData(queue);
		queueList.markForRedraw();
	}

	public void removeQueue(QueueListRecord queue) {
		RecordList list = queueList.getDataAsRecordList();
		QueueListRecord toRemove = (QueueListRecord)list.find(QueueListRecord.ATTRIBUTE_NAME, queue.getName());
		queueList.removeData(toRemove);
		queueList.markForRedraw();
	}

	private void setAllParam(Label l) {
		l.setStyleName("listLink");
		l.setCursor(Cursor.HAND);
		l.setAlign(Alignment.CENTER);
		l.addMouseOverHandler(new MouseOverHandler() { 
			public void onMouseOver(MouseOverEvent event) {
				((Label)event.getSource()).setStyleName("listLinkHover"); 
			} 
		});
		l.addMouseOutHandler(new MouseOutHandler() { 
			public void onMouseOut(MouseOutEvent event) { 
				((Label)event.getSource()).setStyleName("listLink"); 
			} 
		});
		l.setWidth(150); 
		l.setHeight(15); 
		l.setMargin(0); 
		l.setPadding(0);
	}

	private Options createOptions(boolean reuseChart) {
		Options options = Options.create();
		options.setDisplayAnnotations(false);
		options.setDisplayAnnotationsFilter(false);
		options.setDisplayZoomButtons(true);
		options.setDisplayRangeSelector(false);
		options.setAllowRedraw(reuseChart);
		options.setDateFormat("dd MMM HH:mm:ss");
		options.setFill(5);
		options.setLegendPosition(AnnotatedLegendPosition.NEW_ROW);
		options.setWindowMode(WindowMode.TRANSPARENT);

		return options;
	}

	private AbstractDataTable createTable() {
		DataTable data = DataTable.create();

		data.addColumn(ColumnType.DATETIME, Application.messages.common_time());
		if(showRecieved)	data.addColumn(ColumnType.NUMBER, Application.messages.common_recieved()); 
		if(showDelivered)	data.addColumn(ColumnType.NUMBER, Application.messages.common_delivered()); 
		if(showSentDMQ)	data.addColumn(ColumnType.NUMBER, Application.messages.common_sentDMQ()); 
		if(showPending)	data.addColumn(ColumnType.NUMBER, Application.messages.common_pending()); 

		Record selectedRecord = queueList.getSelectedRecord();
		if(selectedRecord != null) {
			SortedMap<Date, int[]> history = presenter.getQueueHistory(selectedRecord.getAttributeAsString(QueueListRecord.ATTRIBUTE_NAME));
			if(history != null) {
				data.addRows(history.size());

				int i=0;
				for(Date d : history.keySet()) {
					if(d!=null) {
						int j=1;
						data.setValue(i, 0, d);
						if(showRecieved) { data.setValue(i, j, history.get(d)[0]); j++; }
						if(showDelivered){ data.setValue(i, j, history.get(d)[1]); j++; }
						if(showSentDMQ) { data.setValue(i, j, history.get(d)[2]); j++; }
						if(showPending) { data.setValue(i, j, history.get(d)[3]); j++; }
						i++;
						j=1;
					}
				}
			}
		}
		return data;
	}

	public void redrawChart(boolean reuseChart) {
		if(redrawChart) {
			chart.draw(createTable(), createOptions(reuseChart));
		}
	}

	private void enableDisableCheckbox() {
		if(!showDelivered && !showSentDMQ && !showPending) {
			showRecievedBox.disable();
		}
		else if(!showRecieved && !showSentDMQ && !showPending) {
			showDeliveredBox.disable();
		}
		else if(!showRecieved && !showDelivered && !showPending){
			showSentDMQBox.disable();
		}
		else if(!showRecieved && !showDelivered && !showSentDMQ) {
			showPendingBox.disable();
		}
		else {
			showRecievedBox.enable();
			showDeliveredBox.enable();
			showSentDMQBox.enable();
			showPendingBox.enable();
		}
	}
}