package jjdevine.epos.processors.impl;

import jjdevine.epos.BeanCopyUtils;
import jjdevine.epos.EposContext;
import jjdevine.epos.customerinfo.data.svc.CustomerDataService;
import jjdevine.epos.customerinfo.view.beans.CustomerInfoViewBean;
import jjdevine.epos.customerinfo.view.events.CustomerInfoViewEvent;
import jjdevine.epos.customerinfo.view.events.CustomerInfoViewListener;
import jjdevine.epos.customerinfo.view.gui.CustomerInfoView;
import jjdevine.epos.discountdefintion.model.beans.CustomerInfo;
import jjdevine.epos.processors.EditCustomerProcessor;

public class EditCustomerProcessorImpl implements EditCustomerProcessor, CustomerInfoViewListener 
{
	private CustomerDataService customerDataService;
	private CustomerInfoView customerInfoView;
	
	@Override
	public void editCustomer(int customerId) 
	{
		CustomerInfo custInfo =  customerDataService.getCustomer(customerId);
		
		if(custInfo == null)
		{
			throw new IllegalArgumentException(customerId + " is not a valid customer id");
		}
		
		CustomerInfoViewBean viewBean = BeanCopyUtils.mapCustomerInfoToViewBean(custInfo);

		customerInfoView = EposContext.getCustomerInfoView();
		customerInfoView.setCustomerInfo(viewBean);
		
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
		
		boolean success = dataSvc.updateCustomer(BeanCopyUtils.copyCustomerInfoViewBeanToModel(custInfo));
		
		if(!success)
		{
			customerInfoView.showErrorMessage("Unable to update customer, please check the logs");
		}
		else
		{
			customerInfoView.showInformationMessage("Customer updated successfully!");
		}
	}

	public void setCustomerDataService(CustomerDataService customerDataService) {
		this.customerDataService = customerDataService;
	}
}
