package jjdevine.epos.mediators;

import java.util.ArrayList;
import java.util.List;

import jjdevine.epos.BeanCopyUtils;
import jjdevine.epos.EposContext;
import jjdevine.epos.customerinfo.data.svc.CustomerDataService;
import jjdevine.epos.customerinfo.data.svc.beans.CustomerSearchCriteria;
import jjdevine.epos.customerinfo.view.beans.CustomerInfoViewBean;
import jjdevine.epos.customerinfo.view.events.CustomerInfoViewEvent;
import jjdevine.epos.customerinfo.view.events.CustomerInfoViewListener;
import jjdevine.epos.customerinfo.view.gui.CustomerInfoView;
import jjdevine.epos.customersearch.view.CustomerSearchView;
import jjdevine.epos.customersearch.view.events.CustomerSearchViewEvent;
import jjdevine.epos.customersearch.view.events.CustomerSearchViewListener;
import jjdevine.epos.discountdefintion.model.beans.CustomerInfo;
import jjdevine.epos.mediators.CustomerSearchMediatorEvent.Type;

public class CustomerSearchMediatorImpl implements CustomerSearchMediator, CustomerSearchViewListener, CustomerInfoViewListener
{
	private List<CustomerSearchMediatorListener> listeners;
	private CustomerSearchView customerSearchView;
	private CustomerInfoView customerInfoView;
	private CustomerDataService customerDataService;
	private SearchThread searchThread;

	@Override
	public void addListener(CustomerSearchMediatorListener listener) 
	{	
		if(listeners == null)
		{
			listeners = new ArrayList<CustomerSearchMediatorListener>();
		}
		
		listeners.add(listener);		
	}

	@Override
	public void searchForCustomer() 
	{
		endSearchesInProgress();
		
		customerSearchView = EposContext.getCustomerSearchView();
		customerSearchView.addListener(this);
		customerSearchView.setLastNames(customerDataService.getAllLastNames());
		customerSearchView.setPostCodes(customerDataService.getAllPostCodes());
	}
	
	@Override
	public void onCustomerSearchViewEvent(CustomerSearchViewEvent evt) 
	{
		switch(evt.getType())
		{
		case CLOSE:
			endSearchesInProgress();
			break;
			
		case CUSTOMER_SELECTED:
			CustomerSearchMediatorEvent newEvt = new CustomerSearchMediatorEvent();
			newEvt.setType(Type.CUSTOMER_SELECTED);
			newEvt.setCustomerId(evt.getCustomerId());
			notifyListeners(newEvt);
			closeCustomerInfoView();
			endSearchesInProgress();
			break;
			
		case DISPOSE:
			customerSearchView = null;
			//TODO: may need to stop search thread
			break;
			
		case DO_SEARCH:
			doSearch(BeanCopyUtils.copyCustomerSearchCriteriaViewBeanToModel(evt.getCustomerSearchCriteria()));
			break;
			
		case SHOW_CUSTOMER_INFO:
			CustomerInfo custInfo = customerDataService.getCustomer(evt.getCustomerId());
			CustomerInfoViewBean viewBean = BeanCopyUtils.mapCustomerInfoToViewBean(custInfo);
			
			closeCustomerInfoView();
			
			customerInfoView = EposContext.getCustomerInfoView();
			customerInfoView.addListener(this);
			customerInfoView.setCustomerInfo(viewBean);
			customerInfoView.setReadOnlyMode(true);
			
			break;
		}
		
	}
	
	private void notifyListeners(CustomerSearchMediatorEvent evt)
	{
		for(CustomerSearchMediatorListener listener: listeners)
		{
			listener.onCustomerSearchMediatorEvent(evt);
		}
	}
	
	private void endSearchesInProgress()
	{
		if(customerSearchView != null)
		{
			customerSearchView.close();
			customerSearchView = null;
		}
	}
	
	private void closeCustomerInfoView()
	{
		if(customerInfoView != null)
		{
			customerInfoView.close();
			customerInfoView = null;
		}
	}

	public CustomerSearchView getCustomerSearchView() {
		return customerSearchView;
	}

	public void setCustomerSearchView(CustomerSearchView customerSearchView) {
		this.customerSearchView = customerSearchView;
	}

	@Override
	public void onCustomerInfoViewEvent(CustomerInfoViewEvent evt) {
		//TODO: handle close?
	}

	public void setCustomerDataService(CustomerDataService customerDataService) {
		this.customerDataService = customerDataService;
	}
	
	
	private void doSearch(CustomerSearchCriteria searchCriteria)
	{
		killSearchThread();
		
		searchThread = new SearchThread();
		searchThread.setSearchCriteria(searchCriteria);
		searchThread.start();
	}
	
	private void killSearchThread()
	{
		if(searchThread == null)
		{
			return;
		}
		
		if(searchThread.isActive())
		{
			searchThread.setKillThread(true);
		}
	}

	private class SearchThread extends Thread
	{
		private boolean isActive = false;
		private boolean killThread = false;
		private CustomerSearchCriteria searchCriteria;
		
		@Override
		public void run() 
		{
			isActive = true;
			customerSearchView.clearResults();
			
			List<CustomerInfo> customers = customerDataService.findCustomers(searchCriteria);
			
			if(customers.size() == 0)
			{
				customerSearchView.showInformationMessage("No results found!");
			}
			
			for(CustomerInfo customer: customers)
			{
				if(killThread)
				{
					return;
				}
				
				customerSearchView.addToResults(BeanCopyUtils.mapCustomerInfoToSearchResultViewBean(customer));
			}
			isActive = false;
		}

		public boolean isActive()
		{
			return isActive;
		}
		
		public void setKillThread(boolean killThread) {
			this.killThread = killThread;
		}
		
		public void setSearchCriteria(CustomerSearchCriteria searchCriteria)
		{
			this.searchCriteria = searchCriteria;
		}
	}
	
}
