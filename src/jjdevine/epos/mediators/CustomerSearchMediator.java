package jjdevine.epos.mediators;

public interface CustomerSearchMediator 
{
	public void addListener(CustomerSearchMediatorListener listener);
	
	public void searchForCustomer();
}