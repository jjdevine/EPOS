package jjdevine.epos.events;

import jjdevine.epos.beans.EposPaymentReturnedItemViewBean;

public interface EposGarmentReturnPanelListener 
{
	public void cancelReturn(EposPaymentReturnedItemViewBean eposPaymentReturnedItemViewBean);
}
