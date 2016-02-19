package jjdevine.epos.processors;

import java.util.List;

import jjdevine.epos.transaction.EposTransaction;
import jjdevine.printing.beans.PrintingOutputLine;

public interface TransactionReceiptFormatter 
{
	public List<PrintingOutputLine> convertEposTransactionToPrintingRequest(EposTransaction transaction);
}
