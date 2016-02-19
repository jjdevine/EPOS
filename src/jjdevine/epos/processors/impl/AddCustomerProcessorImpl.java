package jjdevine.epos.processors.impl;

import jjdevine.epos.BeanCopyUtils;
import jjdevine.epos.EposContext;
import jjdevine.epos.customerinfo.data.svc.CustomerDataService;
import jjdevine.epos.customerinfo.view.beans.CustomerInfoViewBean;
import jjdevine.epos.customerinfo.view.events.CustomerInfoViewEvent;
import jjdevine.epos.customerinfo.view.events.CustomerInfoViewListener;
import jjdevine.epos.customerinfo.view.gui.CustomerInfoView;
import jjdevine.epos.processors.AddCustomerProcessor;

public class AddCustomerProcessorImpl implements AddCustomerProcessor, CustomerInfoViewListener
{
	private CustomerInfoView customerInfoView;

	@Override
	public void addCustomer() 
	{
		CustomerInfoViewBean custInfo = new CustomerInfoViewBean();
		
		int nextId = EposContext.getCustomerDataService().getNextAvailableId();
		custInfo.setCustomerId(nextId);

		customerInfoView = EposContext.getCustomerInfoView();
		customerInfoView.setCustomerInfo(custInfo);
		
		customerInfoView.addListener(this);
	}

	@Override
	public void onCustomerInfoViewEvent(CustomerInfoViewEvent evt) 
	{
		switch(evt.getType())
		{
			case SAVE_DETAILS:
				saveCustomer(evt.getCustomerInfo());
				
		}		
	}
	
	private void saveCustomer(CustomerInfoViewBean custInfo)
	{
		CustomerDataService dataSvc = EposContext.getCustomerDataService();
		
		boolean success = dataSvc.addCustomer(BeanCopyUtils.copyCustomerInfoViewBeanToModel(custInfo));
		
		if(!success)
		{
			customerInfoView.showErrorMessage("Unable to save customer, please check the logs");
		}
		else
		{
			customerInfoView.showInformationMessage("Customer saved successfully!");
		}
	}
	
	

}
