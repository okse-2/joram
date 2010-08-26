/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.client.widget;

import java.util.Date;
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
import com.scalagent.appli.client.presenter.ServerPresenter;
import com.scalagent.engine.client.widget.BaseWidget;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.DrawEvent;
import com.smartgwt.client.widgets.events.DrawHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class ServerWidget extends BaseWidget<ServerPresenter> {

	boolean redrawChart = false;
	boolean showQueue = true;
	boolean showTopic = true;
	boolean showUser = true;
	boolean showSub = true;
	
	boolean showEngine1 = true;
	boolean showEngine2 = true;
	boolean showEngine3 = true;
	
	boolean showNetwork1 = true;
	boolean showNetwork2 = true;
	boolean showNetwork3 = true;



	VLayout	vl;
	HLayout h1;
	VLayout vCount;
	
	HLayout h2;
	VLayout vEngine;
	VLayout vNetwork;

	IButton refreshButton;

	Label countLabel;
	DynamicForm countForm;
	CheckboxItem showQueueBox;
	CheckboxItem showTopicBox;
	CheckboxItem showUserBox;
	CheckboxItem showSubBox;
	AnnotatedTimeLine countChart;
	
	Label engineLabel;
	DynamicForm engineForm;
	CheckboxItem showEngine1Box;
	CheckboxItem showEngine2Box;
	CheckboxItem showEngine3Box;
	AnnotatedTimeLine engineChart;
	
	Label networkLabel;
	DynamicForm networkForm;
	CheckboxItem showNetwork1Box;
	CheckboxItem showNetwork2Box;
	CheckboxItem showNetwork3Box;
	AnnotatedTimeLine networkChart;


	public ServerWidget(ServerPresenter serverPresenter) {
		super(serverPresenter);

	}

	@Override
	public Widget asWidget() {
		
		int pageWidth = com.google.gwt.user.client.Window.getClientWidth();
		refreshButton = new IButton();
		refreshButton.setMargin(0); 
		refreshButton.setAutoFit(true);
		refreshButton.setIcon("refresh.gif");  
		refreshButton.setTitle(Application.messages.queueWidget_buttonRefresh_title());
		refreshButton.setPrompt(Application.messages.queueWidget_buttonRefresh_prompt());
		refreshButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.getCache().retrieveQueue(true);
				presenter.getCache().retrieveTopic(true);
				presenter.getCache().retrieveUser(true);
				presenter.getCache().retrieveSubscription(true);
				redrawChart(true, true, true);

			}
		}); 
		
		countLabel = new Label(Application.messages.serverWidget_count());
		countLabel.setHeight(20);
		countLabel.setStyleName("title1");
		countChart = new AnnotatedTimeLine(createTableCount(), createOptions(true), Integer.toString(pageWidth-150), "200");
		
		countForm = new DynamicForm();
		countForm.setNumCols(8);

		showQueueBox = new CheckboxItem();  
		showQueueBox.setTitle(Application.messages.serverWidget_queues());
		showQueueBox.setValue(true);
		showQueueBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {

				showQueue = showQueueBox.getValueAsBoolean();
				enableDisableCheckboxCount();
				redrawChart(false, true, true);
				redrawChart(true, true, true);
			}
		});

		showTopicBox = new CheckboxItem();  
		showTopicBox.setTitle(Application.messages.serverWidget_topics());
		showTopicBox.setValue(true);
		showTopicBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showTopic = showTopicBox.getValueAsBoolean();
				enableDisableCheckboxCount();
				redrawChart(false, true, true);
				redrawChart(true, true, true);
			}
		});

		showUserBox = new CheckboxItem();  
		showUserBox.setTitle(Application.messages.serverWidget_users());
		showUserBox.setValue(true);
		showUserBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showUser = showUserBox.getValueAsBoolean();
				enableDisableCheckboxCount();
				redrawChart(false, true, true);
				redrawChart(true, true, true);
			}
		});
		
		showSubBox = new CheckboxItem();  
		showSubBox.setTitle(Application.messages.serverWidget_subscriptions());
		showSubBox.setValue(true);
		showSubBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showSub = showSubBox.getValueAsBoolean();
				enableDisableCheckboxCount();
				redrawChart(false, true, true);
				redrawChart(true, true, true);
			}
		});
		
		countForm.setFields(showQueueBox, showTopicBox, showUserBox, showSubBox);
		
		vCount = new VLayout();
		vCount.addMember(countLabel);
		vCount.addMember(countForm);
		vCount.addMember(countChart);


		
		engineLabel = new Label(Application.messages.serverWidget_engine());
		engineLabel.setHeight(20);
		engineLabel.setStyleName("title1");
		engineChart = new AnnotatedTimeLine(createTableEngine(), createOptions(true), Integer.toString(pageWidth/2-50), "200");
		
		engineForm = new DynamicForm();
		engineForm.setNumCols(6);

		showEngine1Box = new CheckboxItem();  
		showEngine1Box.setTitle(Application.messages.serverWidget_avg1min());
		showEngine1Box.setValue(true);
		showEngine1Box.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showEngine1 = showEngine1Box.getValueAsBoolean();
				enableDisableCheckboxEngine();
				redrawChart(true, false, true);
				redrawChart(true, true, true);
			}
		});

		showEngine2Box = new CheckboxItem();  
		showEngine2Box.setTitle(Application.messages.serverWidget_avg5min());
		showEngine2Box.setValue(true);
		showEngine2Box.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showEngine2 = showEngine2Box.getValueAsBoolean();
				enableDisableCheckboxEngine();
				redrawChart(true, false, true);
				redrawChart(true, true, true);
			}
		});

		showEngine3Box = new CheckboxItem();  
		showEngine3Box.setTitle(Application.messages.serverWidget_avg15min());
		showEngine3Box.setValue(true);
		showEngine3Box.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showEngine3 = showEngine3Box.getValueAsBoolean();
				enableDisableCheckboxEngine();
				redrawChart(true, false, true);
				redrawChart(true, true, true);
			}
		});
		
		engineForm.setFields(showEngine1Box, showEngine2Box, showEngine3Box);
		
		vEngine = new VLayout();
		vEngine.addMember(engineLabel);
		vEngine.addMember(engineForm);
		vEngine.addMember(engineChart);

		
		networkLabel = new Label(Application.messages.serverWidget_network());
		networkLabel.setHeight(20);
		networkLabel.setStyleName("title1");
		networkChart = new AnnotatedTimeLine(createTableNetwork(), createOptions(true), Integer.toString(pageWidth/2-50), "200");
		
		networkForm = new DynamicForm();
		networkForm.setNumCols(6);

		showNetwork1Box = new CheckboxItem();  
		showNetwork1Box.setTitle(Application.messages.serverWidget_avg1min());
		showNetwork1Box.setValue(true);
		showNetwork1Box.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showNetwork1 = showNetwork1Box.getValueAsBoolean();
				enableDisableCheckboxNetwork();
				redrawChart(true, true, false);
				redrawChart(true, true, true);
			}
		});

		showNetwork2Box = new CheckboxItem();  
		showNetwork2Box.setTitle(Application.messages.serverWidget_avg5min());
		showNetwork2Box.setValue(true);
		showNetwork2Box.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showNetwork2 = showNetwork2Box.getValueAsBoolean();
				enableDisableCheckboxNetwork();
				redrawChart(true, true, false);
				redrawChart(true, true, true);
			}
		});

		showNetwork3Box = new CheckboxItem();  
		showNetwork3Box.setTitle(Application.messages.serverWidget_avg15min());
		showNetwork3Box.setValue(true);
		showNetwork3Box.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showNetwork3 = showNetwork3Box.getValueAsBoolean();
				enableDisableCheckboxNetwork();
				redrawChart(true, true, false);
				redrawChart(true, true, true);
			}
		});
		
		networkForm.setFields(showNetwork1Box, showNetwork2Box, showNetwork3Box);
		
		vNetwork = new VLayout();
		vNetwork.addMember(networkLabel);
		vNetwork.addMember(networkForm);
		vNetwork.addMember(networkChart);
		

		h1 = new HLayout();
		h1.addMember(vCount);
		
		h2 = new HLayout();
		h2.addMember(vEngine);
		h2.addMember(vNetwork);

		vl = new VLayout();
		vl.setWidth100();
		vl.setHeight100();
		vl.setPadding(2);
		vl.addDrawHandler(new DrawHandler() {
			public void onDraw(DrawEvent event) {
				redrawChart = true;
			}
		});
		vl.addMember(refreshButton);
		vl.addMember(h1);
		vl.addMember(h2);

		return vl;
	}
	
	public void redrawChart(boolean reuseCount, boolean reuseEngine, boolean reuseNetwork) {
		if(redrawChart) {
			countChart.draw(createTableCount(), createOptions(reuseCount));
			engineChart.draw(createTableEngine(), createOptions(reuseEngine));
			networkChart.draw(createTableNetwork(), createOptions(reuseNetwork));
		}
	}

	private void enableDisableCheckboxCount() {
		if(!showTopic && !showUser && !showSub) {
			showQueueBox.disable();
		}
		else if(!showQueue && !showUser && !showSub){
			showTopicBox.disable();
		}
		else if(!showQueue && !showTopic && !showSub) {
			showUserBox.disable();
		}
		else if(!showQueue && !showTopic && !showUser) {
			showSubBox.disable();
		}
		else {
			showQueueBox.enable();
			showTopicBox.enable();
			showUserBox.enable();
			showSubBox.enable();
		}
	}
	
	private void enableDisableCheckboxEngine() {
		if(!showEngine2 && !showEngine3) {
			showEngine1Box.disable();
		}
		else if(!showEngine1 && !showEngine3){
			showEngine2Box.disable();
		}
		else if(!showEngine1 && !showEngine2) {
			showEngine3Box.disable();
		}
		else {
			showEngine1Box.enable();
			showEngine2Box.enable();
			showEngine3Box.enable();
		}
	}
	
	private void enableDisableCheckboxNetwork() {
		if(!showNetwork2 && !showNetwork3) {
			showNetwork1Box.disable();
		}
		else if(!showNetwork1 && !showNetwork3){
			showNetwork2Box.disable();
		}
		else if(!showNetwork1 && !showNetwork2) {
			showNetwork3Box.disable();
		}
		else {
			showNetwork1Box.enable();
			showNetwork2Box.enable();
			showNetwork3Box.enable();
		}
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

	private AbstractDataTable createTableCount() {
		DataTable data = DataTable.create();

		data.addColumn(ColumnType.DATETIME, Application.messages.common_time());
		if(showQueue) data.addColumn(ColumnType.NUMBER, Application.messages.serverWidget_queues()); 
		if(showTopic) data.addColumn(ColumnType.NUMBER, Application.messages.serverWidget_topics()); 
		if(showUser) data.addColumn(ColumnType.NUMBER, Application.messages.serverWidget_users()); 
		if(showSub)	data.addColumn(ColumnType.NUMBER, Application.messages.serverWidget_subscriptions()); 

		SortedMap<Date,Integer> queuesHistory = presenter.getQueuesHistory();
		SortedMap<Date,Integer> topicsHistory = presenter.getTopicsHistory();
		SortedMap<Date,Integer> usersHistory = presenter.getUsersHistory();
		SortedMap<Date,Integer> subsHistory = presenter.getSubsHistory();
		
			data.addRows(queuesHistory.size());

			int i=0;
			for(Date d : queuesHistory.keySet()) {
				if(d!=null) {
					int j=1;
					data.setValue(i, 0, d);
					if(showQueue){ data.setValue(i, j, queuesHistory.get(d)); j++; }
					if(showTopic) { data.setValue(i, j, topicsHistory.get(d)); j++; }
					if(showUser) { data.setValue(i, j, usersHistory.get(d)); j++; }
					if(showSub) { data.setValue(i, j, subsHistory.get(d)); j++; }
					i++;
					j=1;
				}
		}
		return data;
	}
	
	private AbstractDataTable createTableEngine() {
		DataTable data = DataTable.create();

		data.addColumn(ColumnType.DATETIME, Application.messages.common_time());
		if(showEngine1) data.addColumn(ColumnType.NUMBER, Application.messages.serverWidget_avg1min()); 
		if(showEngine2) data.addColumn(ColumnType.NUMBER, Application.messages.serverWidget_avg5min()); 
		if(showEngine3) data.addColumn(ColumnType.NUMBER, Application.messages.serverWidget_avg15min()); 

		SortedMap<Date, float[]> engineHistory = presenter.getEngineHistory();
		
			data.addRows(engineHistory.size());

			int i=0;
			for(Date d : engineHistory.keySet()) {
				if(d!=null) {
					int j=1;
					data.setValue(i, 0, d);
					if(showEngine1){ data.setValue(i, j, engineHistory.get(d)[0]); j++; }
					if(showEngine2) { data.setValue(i, j, engineHistory.get(d)[1]); j++; }
					if(showEngine3) { data.setValue(i, j, engineHistory.get(d)[2]); j++; }
					i++;
					j=1;
				}
		}
		return data;
	}
	
	private AbstractDataTable createTableNetwork() {
		DataTable data = DataTable.create();

		data.addColumn(ColumnType.DATETIME, Application.messages.common_time());
		if(showNetwork1) data.addColumn(ColumnType.NUMBER, Application.messages.serverWidget_avg1min()); 
		if(showNetwork2) data.addColumn(ColumnType.NUMBER, Application.messages.serverWidget_avg5min()); 
		if(showNetwork3) data.addColumn(ColumnType.NUMBER, Application.messages.serverWidget_avg15min()); 

		SortedMap<Date, float[]> networkHistory = presenter.getNetworkHistory();
		
			data.addRows(networkHistory.size());

			int i=0;
			for(Date d : networkHistory.keySet()) {
				if(d!=null) {
					int j=1;
					data.setValue(i, 0, d);
					if(showNetwork1){ data.setValue(i, j, networkHistory.get(d)[0]); j++; }
					if(showNetwork2) { data.setValue(i, j, networkHistory.get(d)[1]); j++; }
					if(showNetwork3) { data.setValue(i, j, networkHistory.get(d)[2]); j++; }
					i++;
					j=1;
				}
		}
		return data;
	}


}
