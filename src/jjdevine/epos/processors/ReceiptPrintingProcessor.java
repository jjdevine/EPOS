package jjdevine.epos.processors;

import jjdevine.epos.transaction.EposTransaction;

public interface ReceiptPrintingProcessor 
{
	public int printReceipt(EposTransaction transaction);
}
