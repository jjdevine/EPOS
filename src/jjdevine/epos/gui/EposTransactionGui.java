package jjdevine.epos.gui;

import jjdevine.epos.beans.TransactionViewBean;
import jjdevine.epos.listeners.TransactionEventListener;

public interface EposTransactionGui 
{
	public void resetSelectedItems();
	
	/**
	 * add listeners to the form to notify of events 
	 * @param listener
	 */
	public void addListener(TransactionEventListener listener);
	
	public void renderTransaction(TransactionViewBean transactionViewBean);

	public void close();
	
	public void showError(String message);
	
	public void setUserDisplay(String displayName);

	
}
