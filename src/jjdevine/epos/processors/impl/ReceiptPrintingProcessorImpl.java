package jjdevine.epos.processors.impl;

import java.util.List;

import jjdevine.epos.EposContext;
import jjdevine.epos.beans.Preferences;
import jjdevine.epos.common.Constants;
import jjdevine.epos.processors.ReceiptPrintingProcessor;
import jjdevine.epos.processors.TransactionReceiptFormatter;
import jjdevine.epos.receiptsetup.view.beans.ReceiptSetup;
import jjdevine.epos.transaction.EposTransaction;
import jjdevine.epos.utils.ReceiptUtils;
import jjdevine.epos.utils.Utils;
import jjdevine.printing.PrintingService;
import jjdevine.printing.beans.PrintingOutputLine;
import jjdevine.printing.beans.PrintingRequest;

public class ReceiptPrintingProcessorImpl implements ReceiptPrintingProcessor 
{
	private PrintingService printingService;
	private Preferences preferences;
	
	@Override
	public int printReceipt(EposTransaction transaction) 
	{
		this.preferences = EposContext.getPreferences();
		
		ReceiptSetup receiptSetup = ReceiptUtils.copyToReceiptSetupViewBean(preferences.getActiveReceiptSetup());
		TransactionReceiptFormatter transactionReceiptFormatter = EposContext.getTransactionReceiptFormatter(
				receiptSetup.getTransactionStyle().getId());
		
		List<PrintingOutputLine> printingOutputLines = Utils.convertReceiptSectionToPrintingRequest(receiptSetup.getTopSection());
		printingOutputLines.addAll(transactionReceiptFormatter
				.convertEposTransactionToPrintingRequest(transaction));
		printingOutputLines.addAll(Utils.convertReceiptSectionToPrintingRequest(receiptSetup.getBottomSection()));
		
		PrintingRequest printReq = new PrintingRequest();
		printReq.setPrintingOutputLines(printingOutputLines);
		
		printingService.doPrinting(printReq);
		
		return Constants.PROCESS_OK;
	}
	
	public void setPrintingService(PrintingService printingService) {
		this.printingService = printingService;
	}

}
