package jjdevine.epos.listeners;

import jjdevine.epos.events.TransactionEvent;

public interface TransactionEventListener 
{
	public void onTransactionEvent(TransactionEvent evt);
}
