/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.client.widget;

import java.util.Date;
import java.util.SortedMap;
import java.util.Vector;

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

/**
 * @author Yohann CINTRE
 */
public class ServerWidget extends BaseWidget<ServerPresenter> {

	boolean isInit = false;
	boolean redrawChart = false;
	boolean showQueue = true;
	boolean showTopic = true;
	boolean showUser = true;
	boolean showSub = true;

	boolean showEngine = true;
	Vector<Boolean> vShowNetwork = new Vector<Boolean>();

	VLayout	vl;

	HLayout h1;
	VLayout vCount;

	HLayout h2;
	VLayout vServer;

	IButton refreshButton;

	Label countLabel;
	DynamicForm countForm;
	CheckboxItem showQueueBox;
	CheckboxItem showTopicBox;
	CheckboxItem showUserBox;
	CheckboxItem showSubBox;
	AnnotatedTimeLine countChart;

	Label serverLabel;
	DynamicForm serverForm;
	CheckboxItem showEngineBox;
	Vector<CheckboxItem> vShowNetworkBox = new Vector<CheckboxItem>();
	AnnotatedTimeLine serverChart;


	public ServerWidget(ServerPresenter serverPresenter) {
		super(serverPresenter);
	}

	@Override
	public Widget asWidget() {
		vShowNetwork.trimToSize();
		vShowNetworkBox.trimToSize();

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
				presenter.refreshAll();
				redrawChart(true, true);
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
				redrawChart(false, true);
				redrawChart(true, true);
			}
		});

		showTopicBox = new CheckboxItem();  
		showTopicBox.setTitle(Application.messages.serverWidget_topics());
		showTopicBox.setValue(true);
		showTopicBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showTopic = showTopicBox.getValueAsBoolean();
				enableDisableCheckboxCount();
				redrawChart(false, true);
				redrawChart(true, true);
			}
		});

		showUserBox = new CheckboxItem();  
		showUserBox.setTitle(Application.messages.serverWidget_users());
		showUserBox.setValue(true);
		showUserBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showUser = showUserBox.getValueAsBoolean();
				enableDisableCheckboxCount();
				redrawChart(false, true);
				redrawChart(true, true);
			}
		});

		showSubBox = new CheckboxItem();  
		showSubBox.setTitle(Application.messages.serverWidget_subscriptions());
		showSubBox.setValue(true);
		showSubBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showSub = showSubBox.getValueAsBoolean();
				enableDisableCheckboxCount();
				redrawChart(false, true);
				redrawChart(true, true);
			}
		});

		countForm.setFields(showQueueBox, showTopicBox, showUserBox, showSubBox);

		vCount = new VLayout();
		vCount.addMember(countLabel);
		vCount.addMember(countForm);
		vCount.addMember(countChart);

		serverLabel = new Label(Application.messages.serverWidget_engine());
		serverLabel.setHeight(20);
		serverLabel.setStyleName("title1");
		serverChart = new AnnotatedTimeLine(createTableServer(), createOptions(true), Integer.toString(pageWidth-150), "200");

		serverForm = new DynamicForm();
		serverForm.setNumCols(6);

		showEngineBox = new CheckboxItem();  
		showEngineBox.setTitle("Engine");
		showEngineBox.setValue(true);
		showEngineBox.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {
				showEngine = showEngineBox.getValueAsBoolean();
				enableDisableCheckboxEngine();
				redrawChart(true, false);
				redrawChart(true, true);
			}
		});

		for(int it=0; it<vShowNetworkBox.size(); it++) {

			final CheckboxItem check = new CheckboxItem();  
			check.setTitle("Network "+it);
			check.setValue(true);
			final int fit = it;
			check.addChangedHandler(new ChangedHandler() {
				public void onChanged(ChangedEvent event) {
					vShowNetwork.set(fit, check.getValueAsBoolean());
					enableDisableCheckboxEngine();
					redrawChart(true, false);
					redrawChart(true, true);
				}
			});
			vShowNetworkBox.setElementAt(check, it);
		}

		CheckboxItem[] arrNetwork = new CheckboxItem[vShowNetworkBox.size()];
		vShowNetworkBox.toArray(arrNetwork);

		CheckboxItem[] arrServer = new CheckboxItem[vShowNetworkBox.size()+1];
		arrServer[0] = showEngineBox;
		for(int i=1; i<arrServer.length; i++) {
			arrServer[i] = arrNetwork[i-1];
		}
		serverForm.setFields(arrServer);

		vServer= new VLayout();
		vServer.addMember(serverLabel);
		vServer.addMember(serverChart);
		vServer.addMember(serverForm);

		h1 = new HLayout();
		h1.addMember(vCount);

		h2 = new HLayout();
		h2.addMember(vServer);

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

	public void redrawChart(boolean reuseCount, boolean reuseServer) {
		if(redrawChart && isInit) {
			countChart.draw(createTableCount(), createOptions(reuseCount));
			serverChart.draw(createTableServer(), createOptions(reuseServer));
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

		int cptTrue = (showEngine?1:0);

		for(boolean bool : vShowNetwork)
			cptTrue+=(bool?1:0);

		if(cptTrue==1) {
			if(showEngine)
				showEngineBox.disable();
			else
				vShowNetworkBox.get(vShowNetwork.indexOf(true)).disable();
		}
		else {
			showEngineBox.enable();
			for(CheckboxItem check : vShowNetworkBox)
				check.enable();
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

	private AbstractDataTable createTableServer() {

		DataTable data = DataTable.create();

		data.addColumn(ColumnType.DATETIME, Application.messages.common_time());
		if(showEngine) data.addColumn(ColumnType.NUMBER, "Engine"); 

		for(int it=0; it<vShowNetwork.size(); it++) {
			if(vShowNetwork.get(it)) data.addColumn(ColumnType.NUMBER, "Network "+it); 
		}
		SortedMap<Date, float[]> engineHistory = presenter.getServerHistory();

		data.addRows(engineHistory.size());

		int i=0;
		for(Date d : engineHistory.keySet()) {
			if(d!=null) {
				int j=1;
				data.setValue(i, 0, d);
				if(showEngine){ data.setValue(i, j, engineHistory.get(d)[0]); j++; }
				int k=1;
				for(boolean showNet : vShowNetwork) {
					if(showNet) { data.setValue(i, j, engineHistory.get(d)[k]); j++; }
					k++;
				}
				i++;
				j=1;
			}
		}

		return data;
	}

	public void initCharts(int size) {
		if(!isInit && size != 0) {

			for(int i=0; i<size-1; i++) {
				vShowNetwork.add(true);
				vShowNetworkBox.add(new CheckboxItem());
			}

			vServer.removeMember(serverForm);

			serverForm = new DynamicForm();
			serverForm.setNumCols(6);

			showEngineBox = new CheckboxItem();  
			showEngineBox.setTitle("Engine");
			showEngineBox.setValue(true);
			showEngineBox.addChangedHandler(new ChangedHandler() {
				public void onChanged(ChangedEvent event) {
					showEngine = showEngineBox.getValueAsBoolean();
					enableDisableCheckboxEngine();
					redrawChart(true, false);
					redrawChart(true, true);
				}
			});

			for(int it=0; it<vShowNetworkBox.size(); it++) {

				final CheckboxItem check = new CheckboxItem();  
				check.setTitle("Network "+it);
				check.setValue(true);
				final int fit = it;
				check.addChangedHandler(new ChangedHandler() {
					public void onChanged(ChangedEvent event) {
						vShowNetwork.set(fit, check.getValueAsBoolean());
						enableDisableCheckboxEngine();
						redrawChart(true, false);
						redrawChart(true, true);
					}
				});
				vShowNetworkBox.setElementAt(check, it);
			}

			CheckboxItem[] arrNetwork = new CheckboxItem[vShowNetworkBox.size()];
			vShowNetworkBox.toArray(arrNetwork);

			CheckboxItem[] arrServer = new CheckboxItem[vShowNetworkBox.size()+1];
			arrServer[0] = showEngineBox;
			for(int i=1; i<arrServer.length; i++) {
				arrServer[i] = arrNetwork[i-1];
			}
			serverForm.setFields(arrServer);

			vServer.addMember(serverForm);

			isInit = true;
		}
	}
}