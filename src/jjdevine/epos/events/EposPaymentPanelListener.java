package jjdevine.epos.events;

import jjdevine.epos.beans.EposPaymentViewBean;

public interface EposPaymentPanelListener 
{
	public void cancelPayment(EposPaymentViewBean eposPaymentViewBean);
}
