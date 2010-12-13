package com.scalagent.engine.client.widget;

import com.google.gwt.user.client.ui.Widget;
import com.scalagent.engine.client.presenter.BasePresenter;
import com.smartgwt.client.widgets.Dialog;


public abstract class BaseWidget<P extends BasePresenter<?, ?, ?>>  {

  protected P presenter;
  
  public BaseWidget(P presenter) {
  	this.presenter = presenter;
  }
	
	public abstract Widget asWidget();
	
	public P getPresenter() {
		return presenter;
	}
	
  /**
   * @return a {@link Dialog} with the {@code showModalMask} property set to {@code true}
   */
  protected Dialog getModalMask(){
    Dialog dialog = new Dialog();
    dialog.setShowModalMask(Boolean.TRUE);
    return dialog;
  }
  
}
