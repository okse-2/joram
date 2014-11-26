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
import java.util.Vector;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.AnnotatedLegendPosition;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.Options;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.WindowMode;
import com.scalagent.appli.client.Application;
import com.scalagent.appli.client.RPCServiceCacheClient.FloatHistoryData;
import com.scalagent.appli.client.RPCServiceCacheClient.HistoryData;
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

  VLayout vl;

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

  Options chartOptions;

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
    refreshButton.setAutoFit(Boolean.TRUE);
    refreshButton.setIcon("refresh.gif");
    refreshButton.setTitle(Application.messages.queueWidget_buttonRefresh_title());
    refreshButton.setPrompt(Application.messages.queueWidget_buttonRefresh_prompt());
    refreshButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        presenter.refreshAll();
        redrawChart();
      }
    });

    countLabel = new Label(Application.messages.serverWidget_count());
    countLabel.setHeight(20);
    countLabel.setStyleName("title1");
    countChart = new AnnotatedTimeLine(createTableCount(), createOptions(), (pageWidth - 50) + "px", "200px");

    countForm = new DynamicForm();
    countForm.setNumCols(8);

    showQueueBox = new CheckboxItem();
    showQueueBox.setTitle(Application.messages.serverWidget_queues());
    showQueueBox.setValue(true);
    showQueueBox.addChangedHandler(new ChangedHandler() {
      public void onChanged(ChangedEvent event) {
        showQueue = showQueueBox.getValueAsBoolean().booleanValue();
        if (showQueue) {
          countChart.showDataColumns(0);
        } else {
          countChart.hideDataColumns(0);
        }
        enableDisableCheckboxCount();
      }
    });

    showTopicBox = new CheckboxItem();
    showTopicBox.setTitle(Application.messages.serverWidget_topics());
    showTopicBox.setValue(true);
    showTopicBox.addChangedHandler(new ChangedHandler() {
      public void onChanged(ChangedEvent event) {
        showTopic = showTopicBox.getValueAsBoolean().booleanValue();
        if (showTopic) {
          countChart.showDataColumns(1);
        } else {
          countChart.hideDataColumns(1);
        }
        enableDisableCheckboxCount();
      }
    });

    showUserBox = new CheckboxItem();
    showUserBox.setTitle(Application.messages.serverWidget_users());
    showUserBox.setValue(true);
    showUserBox.addChangedHandler(new ChangedHandler() {
      public void onChanged(ChangedEvent event) {
        showUser = showUserBox.getValueAsBoolean().booleanValue();
        if (showUser) {
          countChart.showDataColumns(2);
        } else {
          countChart.hideDataColumns(2);
        }
        enableDisableCheckboxCount();
      }
    });

    showSubBox = new CheckboxItem();
    showSubBox.setTitle(Application.messages.serverWidget_subscriptions());
    showSubBox.setValue(true);
    showSubBox.addChangedHandler(new ChangedHandler() {
      public void onChanged(ChangedEvent event) {
        showSub = showSubBox.getValueAsBoolean().booleanValue();
        if (showSub) {
          countChart.showDataColumns(3);
        } else {
          countChart.hideDataColumns(3);
        }
        enableDisableCheckboxCount();
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
    serverChart = new AnnotatedTimeLine(createTableServer(), createOptions(), (pageWidth - 50) + "px",
        "200px");

    serverForm = new DynamicForm();
    serverForm.setNumCols(6);

    showEngineBox = new CheckboxItem();
    showEngineBox.setTitle("Engine");
    showEngineBox.setValue(true);
    showEngineBox.addChangedHandler(new ChangedHandler() {
      public void onChanged(ChangedEvent event) {
        showEngine = showEngineBox.getValueAsBoolean();
        enableDisableCheckboxEngine();
        redrawChart();
      }
    });

    for (int it = 0; it < vShowNetworkBox.size(); it++) {

      final CheckboxItem check = new CheckboxItem();
      check.setTitle("Network " + it);
      check.setValue(true);
      final int fit = it;
      check.addChangedHandler(new ChangedHandler() {
        public void onChanged(ChangedEvent event) {
          vShowNetwork.set(fit, check.getValueAsBoolean());
          enableDisableCheckboxEngine();
          redrawChart();
        }
      });
      vShowNetworkBox.setElementAt(check, it);
    }
    enableDisableCheckboxEngine();

    CheckboxItem[] arrNetwork = new CheckboxItem[vShowNetworkBox.size()];
    vShowNetworkBox.toArray(arrNetwork);

    CheckboxItem[] arrServer = new CheckboxItem[vShowNetworkBox.size() + 1];
    arrServer[0] = showEngineBox;
    for (int i = 1; i < arrServer.length; i++) {
      arrServer[i] = arrNetwork[i - 1];
    }
    serverForm.setFields(arrServer);

    vServer = new VLayout();
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

  public void redrawChart() {
    if (redrawChart && isInit) {
      countChart.draw(createTableCount(), createOptions());
      serverChart.draw(createTableServer(), createOptions());
    }
  }

  private void enableDisableCheckboxCount() {
    if (!showTopic && !showUser && !showSub) {
      showQueueBox.disable();
    } else if (!showQueue && !showUser && !showSub) {
      showTopicBox.disable();
    } else if (!showQueue && !showTopic && !showSub) {
      showUserBox.disable();
    } else if (!showQueue && !showTopic && !showUser) {
      showSubBox.disable();
    } else {
      showQueueBox.enable();
      showTopicBox.enable();
      showUserBox.enable();
      showSubBox.enable();
    }
  }

  private void enableDisableCheckboxEngine() {

    int cptTrue = (showEngine ? 1 : 0);

    for (boolean bool : vShowNetwork)
      cptTrue += (bool ? 1 : 0);

    if (cptTrue == 1) {
      if (showEngine)
        showEngineBox.disable();
      else
        vShowNetworkBox.get(vShowNetwork.indexOf(Boolean.TRUE)).disable();
    } else {
      showEngineBox.enable();
      for (CheckboxItem check : vShowNetworkBox)
        check.enable();
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

  private AbstractDataTable createTableCount() {
    DataTable data = DataTable.create();

    data.addColumn(ColumnType.DATETIME, Application.messages.common_time());
    data.addColumn(ColumnType.NUMBER, Application.messages.serverWidget_queues());
    data.addColumn(ColumnType.NUMBER, Application.messages.serverWidget_topics());
    data.addColumn(ColumnType.NUMBER, Application.messages.serverWidget_users());
    data.addColumn(ColumnType.NUMBER, Application.messages.serverWidget_subscriptions());

    List<HistoryData> history = presenter.getCountHistory();

    data.addRows(history.size());
    for (int i = 0; i < history.size(); i++) {
      HistoryData hdata = history.get(i);
      data.setValue(i, 0, hdata.time);
      data.setValue(i, 1, hdata.data[0]);
      data.setValue(i, 2, hdata.data[1]);
      data.setValue(i, 3, hdata.data[2]);
      data.setValue(i, 4, hdata.data[3]);
    }
    return data;
  }

  private AbstractDataTable createTableServer() {

    DataTable data = DataTable.create();

    data.addColumn(ColumnType.DATETIME, Application.messages.common_time());
    if (showEngine)
      data.addColumn(ColumnType.NUMBER, "Engine");

    for (int it = 0; it < vShowNetwork.size(); it++) {
      if (vShowNetwork.get(it))
        data.addColumn(ColumnType.NUMBER, "Network " + it);
    }
    List<FloatHistoryData> engineHistory = presenter.getServerHistory();

    data.addRows(engineHistory.size());

    for (int i = 0; i < engineHistory.size(); i++) {
      FloatHistoryData hdata = engineHistory.get(i);
      int j = 1;
      data.setValue(i, 0, hdata.time);
      if (showEngine) {
        data.setValue(i, j, hdata.data[0]);
        j++;
      }
      int k = 1;
      for (boolean showNet : vShowNetwork) {
        if (showNet) {
          data.setValue(i, j, hdata.data[k]);
          j++;
        }
        k++;
      }
      i++;
      j = 1;
    }
    return data;
  }

  public void initCharts(int size) {
    if (!isInit && size != 0) {

      for (int i = 0; i < size - 1; i++) {
        vShowNetwork.add(Boolean.TRUE);
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
          redrawChart();
        }
      });

      for (int it = 0; it < vShowNetworkBox.size(); it++) {

        final CheckboxItem check = new CheckboxItem();
        check.setTitle("Network " + it);
        check.setValue(true);
        final int fit = it;
        check.addChangedHandler(new ChangedHandler() {
          public void onChanged(ChangedEvent event) {
            vShowNetwork.set(fit, check.getValueAsBoolean());
            enableDisableCheckboxEngine();
            redrawChart();
          }
        });
        vShowNetworkBox.setElementAt(check, it);
      }

      CheckboxItem[] arrNetwork = new CheckboxItem[vShowNetworkBox.size()];
      vShowNetworkBox.toArray(arrNetwork);

      CheckboxItem[] arrServer = new CheckboxItem[vShowNetworkBox.size() + 1];
      arrServer[0] = showEngineBox;
      for (int i = 1; i < arrServer.length; i++) {
        arrServer[i] = arrNetwork[i - 1];
      }
      serverForm.setFields(arrServer);

      vServer.addMember(serverForm);

      isInit = true;
    }
  }
}