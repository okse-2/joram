/**
 * (c)2010 Scalagent Distributed Technologies
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
import com.scalagent.appli.client.presenter.TopicListPresenter;
import com.scalagent.appli.client.widget.handler.queue.RefreshAllClickHandler;
import com.scalagent.appli.client.widget.handler.topic.NewTopicClickHandler;
import com.scalagent.appli.client.widget.handler.topic.TopicDeleteClickHandler;
import com.scalagent.appli.client.widget.handler.topic.TopicEditClickHandler;
import com.scalagent.appli.client.widget.record.QueueListRecord;
import com.scalagent.appli.client.widget.record.TopicListRecord;
import com.scalagent.appli.shared.TopicWTO;
import com.scalagent.engine.client.widget.BaseWidget;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.Alignment;
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
public class TopicListWidget extends BaseWidget<TopicListPresenter> {

	int chartWidth;
	boolean redrawChart = false;

	boolean showRecieved = true;
	boolean showDelivered = true;
	boolean showSentDMQ = true;

	HashMap<String, String> etat=new HashMap<String, String>();

	SectionStackSection buttonSection;
	HLayout hl;
	IButton refreshButton;
	IButton newTopicButton;

	SectionStackSection listStackSection;
	ListGrid topicList;

	SectionStackSection viewSectionSection;
	HLayout	topicView;
	DetailViewer topicDetail;
	VLayout topicChart;
	AnnotatedTimeLine chart;	
	DynamicForm columnForm;
	CheckboxItem showRecievedBox;
	CheckboxItem showDeliveredBox;
	CheckboxItem showSentDMQBox;
	
	Window winModal = new Window(); 

	public TopicListWidget(TopicListPresenter topicPresenter) {
		super(topicPresenter);
		etat.put("true", Application.baseMessages.main_true());
		etat.put("false", Application.baseMessages.main_false());
	}
	
	public IButton getRefreshButton() {
		return refreshButton;
	}

	@Override
	public Widget asWidget() {
		
		

		SectionStack topicStack = new SectionStack();
		topicStack.setVisibilityMode(VisibilityMode.MULTIPLE);
		topicStack.setWidth100();
		topicStack.setHeight100();


		refreshButton = new IButton();  
		refreshButton.setAutoFit(true);
		refreshButton.setIcon("refresh.gif");  
		refreshButton.setTitle(Application.messages.queueWidget_buttonRefresh_title());
		refreshButton.setPrompt(Application.messages.queueWidget_buttonRefresh_prompt());
		refreshButton.addClickHandler(new RefreshAllClickHandler(presenter)); 

		newTopicButton = new IButton(); 
		newTopicButton.setMargin(0);
		newTopicButton.setAutoFit(true);
		newTopicButton.setIcon("new.png");  
		newTopicButton.setTitle(Application.messages.topicWidget_buttonNewTopic_title());
		newTopicButton.setPrompt(Application.messages.topicWidget_buttonNewTopic_prompt());
		newTopicButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) { drawForm(null); }  
		}); 		

		hl = new HLayout();
		hl.setHeight(22);
		hl.setPadding(5);
		hl.setMembersMargin(5);
		hl.addMember(refreshButton);
		hl.addMember(newTopicButton);

		buttonSection = new SectionStackSection(Application.messages.topicWidget_buttonSection_title());
		buttonSection.setExpanded(true);
		buttonSection.addItem(hl);



		topicList = new ListGrid() {

			@Override  
			protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {  

				String fieldName = this.getFieldName(colNum);  

				if (fieldName.equals("deleteField")) {

					IButton buttonDelete = new IButton();  
					buttonDelete.setAutoFit(true);
					buttonDelete.setHeight(20); 
					buttonDelete.setIconSize(13);
					buttonDelete.setIcon("remove.png");  
					buttonDelete.setTitle(Application.messages.topicWidget_buttonDelete_title());
					buttonDelete.setPrompt(Application.messages.topicWidget_buttonDelete_prompt());
					buttonDelete.addClickHandler(new TopicDeleteClickHandler(presenter, (TopicListRecord) record));

					return buttonDelete;

				} else if (fieldName.equals("editField")) {

					IButton buttonEdit = new IButton();  
					buttonEdit.setAutoFit(true);
					buttonEdit.setHeight(20); 
					buttonEdit.setIconSize(13);
					buttonEdit.setIcon("pencil.png");  
					buttonEdit.setTitle(Application.messages.topicWidget_buttonEdit_title());
					buttonEdit.setPrompt(Application.messages.topicWidget_buttonEdit_prompt());
					buttonEdit.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) { drawForm((TopicListRecord) record); }  
					}); 		
					return buttonEdit;

				} else {  
					return null;                     
				}  	    	   
			}	
		};
		
		
		topicList.setRecordComponentPoolingMode("viewport");
		topicList.setShowGridSummary(true);   
		topicList.setAlternateRecordStyles(true);
		topicList.setShowRecordComponents(true);          
		topicList.setShowRecordComponentsByCell(true);





		ListGridField nameFieldL = new ListGridField(TopicListRecord.ATTRIBUTE_NAME, Application.messages.topicWidget_nameFieldL_title());		
		ListGridField nbMsgsDeliverSinceCreationFieldL = new ListGridField(TopicListRecord.ATTRIBUTE_NBMSGSDELIVERSINCECREATION, Application.messages.topicWidget_nbMsgsDeliverSinceCreationFieldL_title());
		ListGridField nbMsgsReceiveSinceCreationFieldL = new ListGridField(TopicListRecord.ATTRIBUTE_NBMSGSRECEIVESINCECREATION, Application.messages.topicWidget_nbMsgsRecievesSinceCreationFieldD_title());
		ListGridField nbMsgsSentToDMQSinceCreationFieldL = new ListGridField(TopicListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, Application.messages.topicWidget_nbMsgsSentSinceCreationFieldL_title());
		ListGridField freeReadingFieldL = new ListGridField(TopicListRecord.ATTRIBUTE_FREEREADING, Application.messages.topicWidget_freeReadingFieldL_title());   
		ListGridField freeWritingFieldL = new ListGridField(TopicListRecord.ATTRIBUTE_FREEWRITING, Application.messages.topicWidget_freeWritingFieldL_title());   
		ListGridField deleteFieldL = new ListGridField("deleteField", Application.messages.topicWidget_deleteFieldL_title(), 110);
		deleteFieldL.setAlign(Alignment.CENTER);  
		ListGridField editFieldL = new ListGridField("editField", Application.messages.topicWidget_editFieldL_title(), 110);
		editFieldL.setAlign(Alignment.CENTER);  
		freeReadingFieldL.setValueMap(etat);
		freeWritingFieldL.setValueMap(etat);

		topicList.setFields(nameFieldL, 
				nbMsgsDeliverSinceCreationFieldL,
				nbMsgsReceiveSinceCreationFieldL,
				nbMsgsSentToDMQSinceCreationFieldL,
				freeReadingFieldL,
				freeWritingFieldL,
				editFieldL,
				deleteFieldL);

		topicList.addRecordClickHandler(new RecordClickHandler() {
			public void onRecordClick(RecordClickEvent event) {
				topicDetail.setData(new Record[]{event.getRecord()});
				redrawChart(true);
			}
		});


		topicDetail = new DetailViewer();
		topicDetail.setMargin(2);
		topicDetail.setWidth("50%");
		topicDetail.setEmptyMessage(Application.messages.topicWidget_topicDetail_emptyMessage());

		DetailViewerField nameFieldD = new DetailViewerField(TopicListRecord.ATTRIBUTE_NAME, Application.messages.topicWidget_nameFieldD_title());
		DetailViewerField creationDateFieldD = new DetailViewerField(TopicListRecord.ATTRIBUTE_CREATIONDATE, Application.messages.topicWidget_creationDateFieldD_title());
		DetailViewerField subscriberIdsFieldD = new DetailViewerField(TopicListRecord.ATTRIBUTE_SUBSCRIBERIDS, Application.messages.topicWidget_subscriberIdsFieldD_title());
		DetailViewerField DMQIdFieldD = new DetailViewerField(TopicListRecord.ATTRIBUTE_DMQID, Application.messages.topicWidget_DMQIdFieldD_title());
		DetailViewerField destinationIdFieldD = new DetailViewerField(TopicListRecord.ATTRIBUTE_DESTINATIONID, Application.messages.topicWidget_destinationIdFieldD_title());
		DetailViewerField nbMsgsDeliverSinceCreationFieldD = new DetailViewerField(TopicListRecord.ATTRIBUTE_NBMSGSDELIVERSINCECREATION, Application.messages.topicWidget_nbMsgsDeliverSinceCreationFieldD_title());
		DetailViewerField nbMsgsReceiveSinceCreationFieldD = new DetailViewerField(TopicListRecord.ATTRIBUTE_NBMSGSRECEIVESINCECREATION, Application.messages.topicWidget_nbMsgsRecievesSinceCreationFieldD_title());
		DetailViewerField nbMsgsSentToDMQSinceCreationFieldD = new DetailViewerField(TopicListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, Application.messages.topicWidget_nbMsgsSentSinceCreationFieldD_title());
		DetailViewerField periodFieldD = new DetailViewerField(TopicListRecord.ATTRIBUTE_PERIOD, Application.messages.topicWidget_periodFieldD_title());
		DetailViewerField rightsFieldD = new DetailViewerField(TopicListRecord.ATTRIBUTE_RIGHTS, Application.messages.topicWidget_rightsFieldD_title());
		DetailViewerField freeReadingFieldD = new DetailViewerField(TopicListRecord.ATTRIBUTE_FREEREADING, Application.messages.topicWidget_freeReadingFieldD_title());
		DetailViewerField freeWritingFieldD= new DetailViewerField(TopicListRecord.ATTRIBUTE_FREEWRITING, Application.messages.topicWidget_freeWritingFieldD_title());
		freeReadingFieldD.setValueMap(etat);
		freeWritingFieldD.setValueMap(etat);

		topicDetail.setFields(nameFieldD, 
				creationDateFieldD, 
				subscriberIdsFieldD, 
				DMQIdFieldD, 
				destinationIdFieldD, 
				nbMsgsDeliverSinceCreationFieldD, 
				nbMsgsReceiveSinceCreationFieldD, 
				nbMsgsSentToDMQSinceCreationFieldD, 
				periodFieldD, 
				rightsFieldD, 
				freeReadingFieldD, 
				freeWritingFieldD);

		chartWidth = (com.google.gwt.user.client.Window.getClientWidth()/2)-45;
		chart = new AnnotatedTimeLine(createTable(), createOptions(true), ""+chartWidth, "200");
		
		
		columnForm = new DynamicForm();
		columnForm.setNumCols(6);

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



		columnForm.setFields(showRecievedBox, showDeliveredBox, showSentDMQBox);

		topicChart = new VLayout();
		topicChart.setMargin(2);
		topicChart.setPadding(5);
		topicChart.setWidth("50%");
		topicChart.setHeight(220);
		topicChart.setAlign(Alignment.CENTER);
		topicChart.setAlign(VerticalAlignment.TOP);
		topicChart.setShowEdges(true);
		topicChart.setEdgeSize(1);
		topicChart.addMember(columnForm);
		topicChart.addMember(chart);
		topicChart.addDrawHandler(new DrawHandler() {
			@Override
			public void onDraw(DrawEvent event) {
				redrawChart = true;
			}
		});


		topicView = new HLayout();
		topicView.setMargin(2);
		topicView.setPadding(2);
		topicView.addMember(topicDetail);
		topicView.addMember(topicChart);


		// Section stack of the topic list
		listStackSection = new SectionStackSection(Application.messages.topicWidget_listStackSection_title());
		listStackSection.setExpanded(true);
		listStackSection.addItem(topicList);


		// Section stack of the view (details & buttons)
		viewSectionSection = new SectionStackSection(Application.messages.topicWidget_viewSectionSection_title());
		viewSectionSection.setExpanded(true);
		viewSectionSection.addItem(topicView);
		viewSectionSection.setCanReorder(true);

		topicStack.addSection(buttonSection);
		topicStack.addSection(listStackSection);
		topicStack.addSection(viewSectionSection);
		topicStack.setCanResizeSections(true);
		

		return topicStack;


	}

	public void setData(List<TopicWTO> data) {

		TopicListRecord[] topicListRecords = new TopicListRecord[data.size()];
		for (int i=0;i<data.size();i++) {
			topicListRecords[i] = new TopicListRecord(data.get(i));
		}

		topicList.setData(topicListRecords);
	}

	public void updateTopic(TopicWTO topic) {
		TopicListRecord topicListRecords = (TopicListRecord)topicList.getRecordList().find(TopicListRecord.ATTRIBUTE_NAME, topic.getName());
		if(topicListRecords != null)  {
			topicListRecords.setAttribute(TopicListRecord.ATTRIBUTE_NAME, topic.getName()); 
			topicListRecords.setAttribute(TopicListRecord.ATTRIBUTE_CREATIONDATE, topic.getCreationDate()); 
			topicListRecords.setAttribute(TopicListRecord.ATTRIBUTE_SUBSCRIBERIDS, topic.getSubscriberIds()); 
			topicListRecords.setAttribute(TopicListRecord.ATTRIBUTE_DMQID, topic.getDMQId()); 
			topicListRecords.setAttribute(TopicListRecord.ATTRIBUTE_DESTINATIONID, topic.getDestinationId()); 
			topicListRecords.setAttribute(TopicListRecord.ATTRIBUTE_NBMSGSDELIVERSINCECREATION, topic.getNbMsgsDeliverSinceCreation()); 
			topicListRecords.setAttribute(TopicListRecord.ATTRIBUTE_NBMSGSRECEIVESINCECREATION, topic.getNbMsgsReceiveSinceCreation()); 
			topicListRecords.setAttribute(TopicListRecord.ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, topic.getNbMsgsSentToDMQSinceCreation()); 
			topicListRecords.setAttribute(TopicListRecord.ATTRIBUTE_PERIOD, topic.getPeriod()); 
			topicListRecords.setAttribute(TopicListRecord.ATTRIBUTE_RIGHTS, topic.getRights()); 
			topicListRecords.setAttribute(TopicListRecord.ATTRIBUTE_FREEREADING, topic.isFreeReading()); 
			topicListRecords.setAttribute(TopicListRecord.ATTRIBUTE_FREEWRITING, topic.isFreeWriting()); 

			topicListRecords.setTopic(topic);
			topicList.markForRedraw();

		}

		topicDetail.setData(new Record[]{topicList.getSelectedRecord()});

	}

	public void addTopic(TopicListRecord topic) {

		ListGridRecord[] records = topicList.getRecords();
		ListGridRecord[] newRecords = new ListGridRecord[records.length+1];
		for (int i=0;i<records.length;i++) {
			newRecords[i] = records[i];
		}
		newRecords[records.length] = topic;
		topicList.setData(newRecords);
		topicList.markForRedraw();


	}

	public void removeTopic(TopicListRecord topic) {

		RecordList list = topicList.getDataAsRecordList();
		TopicListRecord toRemove = (TopicListRecord)list.find(TopicListRecord.ATTRIBUTE_NAME, topic.getName());
		topicList.removeData(toRemove);
		topicList.markForRedraw();

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
		if(showRecieved) data.addColumn(ColumnType.NUMBER, Application.messages.common_recieved()); 
		if(showDelivered) data.addColumn(ColumnType.NUMBER, Application.messages.common_delivered()); 
		if(showSentDMQ)	data.addColumn(ColumnType.NUMBER, Application.messages.common_sentDMQ()); 


		Record selectedRecord = topicList.getSelectedRecord();
		if(selectedRecord != null) {
			SortedMap<Date, int[]> history = presenter.getTopicHistory(selectedRecord.getAttributeAsString(QueueListRecord.ATTRIBUTE_NAME));
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
		if(!showDelivered && !showSentDMQ) {
			showRecievedBox.disable();
		}
		else if(!showRecieved && !showSentDMQ) {
			showDeliveredBox.disable();
		}
		else if(!showRecieved && !showDelivered){
			showSentDMQBox.disable();
		}
		else {
			showRecievedBox.enable();
			showDeliveredBox.enable();
			showSentDMQBox.enable();
		}
	}
	
	private void drawForm(TopicListRecord tlr) {

		winModal = new Window(); 
		winModal.setHeight(350);
		winModal.setWidth(400);
		if(tlr == null) winModal.setTitle(Application.messages.topicWidget_winModal_title());  
		else winModal.setTitle("Topic \""+tlr.getAttributeAsString(TopicListRecord.ATTRIBUTE_NAME)+"\"");  
		winModal.setShowMinimizeButton(false);  
		winModal.setIsModal(true);  
		winModal.setShowModalMask(true);  
		winModal.centerInPage();  
		winModal.addCloseClickHandler(new CloseClickHandler() {  
			public void onCloseClick(CloseClientEvent event) {  
				winModal.destroy();  
			}  
		});  


		Label formTitle = new Label();
		if(tlr == null) formTitle.setContents(Application.messages.topicWidget_formTitle_title());  
		else formTitle.setContents("Edit \""+tlr.getAttributeAsString(TopicListRecord.ATTRIBUTE_NAME)+"\"");  
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
		nameItem.setTitle(Application.messages.topicWidget_nameItem_title()); 
		nameItem.setName("nameItem");
		nameItem.setRequired(true);

		TextItem DMQItem = new TextItem();  
		DMQItem.setTitle(Application.messages.topicWidget_DMQItem_title()); 
		DMQItem.setName("DMQItem");
		DMQItem.setRequired(true);

		TextItem destinationItem = new TextItem();  
		destinationItem.setTitle(Application.messages.topicWidget_destinationItem_title());
		destinationItem.setName("destinationItem");
		destinationItem.setRequired(true);

		TextItem periodItem = new TextItem();  
		periodItem.setTitle(Application.messages.topicWidget_periodItem_title());
		periodItem.setName("periodItem");
		periodItem.setRequired(true);
		periodItem.setValidators(integerValidator); 

		CheckboxItem freeReadingItem = new CheckboxItem();  
		freeReadingItem.setTitle(Application.messages.topicWidget_freeReadingItem_title());
		freeReadingItem.setName("freeReadingItem");

		CheckboxItem freeWritingItem = new CheckboxItem();  
		freeWritingItem.setTitle(Application.messages.topicWidget_freeWritingItem_title());
		freeWritingItem.setName("freeWritingItem");

		if(tlr != null) {
			nameItem.setValue(tlr.getAttributeAsString(TopicListRecord.ATTRIBUTE_NAME));
			nameItem.setDisabled(true);
			DMQItem.setValue(tlr.getAttributeAsString(TopicListRecord.ATTRIBUTE_DMQID));
			destinationItem.setValue(tlr.getAttributeAsString(TopicListRecord.ATTRIBUTE_DESTINATIONID));
			periodItem.setValue(tlr.getAttributeAsString(TopicListRecord.ATTRIBUTE_PERIOD));
			freeReadingItem.setValue(tlr.getAttributeAsBoolean(TopicListRecord.ATTRIBUTE_FREEREADING));
			freeWritingItem.setValue(tlr.getAttributeAsBoolean(TopicListRecord.ATTRIBUTE_FREEWRITING));
		}

		form.setFields(nameItem, 
				DMQItem, 
				destinationItem, 
				periodItem, 
				freeReadingItem, 
				freeWritingItem);

		IButton validateButton = new IButton();  
		if(tlr == null) {
			validateButton.setTitle(Application.messages.topicWidget_validateButton_titleCreate());
			validateButton.setIcon("add.png");
			validateButton.addClickHandler(new NewTopicClickHandler(presenter, form));
		}
		else {
			validateButton.setTitle(Application.messages.topicWidget_validateButton_titleEdit());  
			validateButton.setIcon("accept.png");
			validateButton.addClickHandler(new TopicEditClickHandler(presenter, form));
		}
		validateButton.setAutoFit(true);
		validateButton.setLayoutAlign(VerticalAlignment.TOP);  
		validateButton.setLayoutAlign(Alignment.CENTER);

		IButton cancelButton = new IButton();  
		cancelButton.setTitle(Application.messages.topicWidget_cancelButton_title());
		cancelButton.setIcon("cancel.png");
		cancelButton.setAutoFit(true);
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