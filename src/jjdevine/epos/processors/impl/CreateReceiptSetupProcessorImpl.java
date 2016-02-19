package jjdevine.epos.processors.impl;

import java.awt.Component;
import java.util.List;

import javax.swing.JOptionPane;

import jjdevine.epos.EposContext;
import jjdevine.epos.processors.CreateReceiptSetupProcessor;
import jjdevine.epos.processors.TransactionReceiptFormatter;
import jjdevine.epos.receiptsetup.data.svc.ReceiptSetupDataService;
import jjdevine.epos.receiptsetup.data.svc.beans.ReceiptSetupDTO;
import jjdevine.epos.receiptsetup.view.beans.ReceiptSetup;
import jjdevine.epos.receiptsetup.view.events.ReceiptSetupViewEvent;
import jjdevine.epos.receiptsetup.view.events.ReceiptSetupViewListener;
import jjdevine.epos.receiptsetup.view.gui.ReceiptSetupView;
import jjdevine.epos.utils.ReceiptUtils;
import jjdevine.epos.utils.Utils;
import jjdevine.printing.PrintPreviewService;
import jjdevine.printing.beans.PrintingOutputLine;
import jjdevine.printing.beans.PrintingRequest;

public class CreateReceiptSetupProcessorImpl implements
		CreateReceiptSetupProcessor, ReceiptSetupViewListener {

	private ReceiptSetupDataService receiptSetupDataService;
	private ReceiptSetupView receiptSetupView;
	private PrintPreviewService printPreviewService;
	
	
	@Override
	public void createReceiptSetup() 
	{
		receiptSetupView = EposContext.getReceiptSetupView();
		receiptSetupView.addListener(this);
	}
	
	@Override
	public void editReceiptSetup(String setupName) 
	{
		ReceiptSetupDTO setupDTO = receiptSetupDataService.getReceiptSetup(setupName);
		ReceiptSetup setupViewBean = ReceiptUtils.copyToReceiptSetupViewBean(setupDTO);
		
		receiptSetupView = EposContext.getReceiptSetupView(setupViewBean);
		receiptSetupView.addListener(this);
	}
	
	@Override
	public void onReceiptSetupViewEvent(ReceiptSetupViewEvent evt) 
	{
		switch(evt.getType())
		{
			case SAVE:
				save(evt.getReceiptSetup());
				break;
			case PRINT_PREVIEW:
				printPreview(evt.getReceiptSetup());
				break;
			case CREATE_TRANSACTION_STYLE:
				//not supported at present
				break;
		}
		
	}

	private void printPreview(ReceiptSetup receiptSetup) 
	{
		TransactionReceiptFormatter transactionReceiptFormatter = EposContext.getTransactionReceiptFormatter(
				receiptSetup.getTransactionStyle().getId());
		
		List<PrintingOutputLine> printingOutputLines = Utils.convertReceiptSectionToPrintingRequest(receiptSetup.getTopSection());
		printingOutputLines.addAll(transactionReceiptFormatter
				.convertEposTransactionToPrintingRequest(Utils.getTestEposTransaction()));
		printingOutputLines.addAll(Utils.convertReceiptSectionToPrintingRequest(receiptSetup.getBottomSection()));
		
		PrintingRequest printReq = new PrintingRequest();
		printReq.setPrintingOutputLines(printingOutputLines);
		
		printPreviewService.showPrintPreview(printReq);
		
		//TODO: implement printing at the end of transactions
	}

	private void save(ReceiptSetup receiptSetup) 
	{
		boolean success;
		
		if(receiptSetupDataService.setupNameExists(receiptSetup.getName()))
		{
			int option = JOptionPane.showConfirmDialog((Component)receiptSetupView, "Override existing setup with name '" + receiptSetup.getName() + "'?",
					"Confirm Overwrite", JOptionPane.YES_NO_OPTION);
			if(option != JOptionPane.OK_OPTION)
			{
				return;
			}		
			else
			{
				success = receiptSetupDataService.deleteReceiptSetup(receiptSetup.getName());
				
				if(!success)
				{
					JOptionPane.showMessageDialog((Component)receiptSetupView, "Unable to overwrite configuration!",
					"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
		ReceiptUtils receiptUtils = new ReceiptUtils();
		
		receiptUtils.setNextSetupId(receiptSetupDataService.getNextAvailableReceiptSetupId());
		receiptUtils.setNextSectionId(receiptSetupDataService.getNextAvailableReceiptSectionId());
		receiptUtils.setNextLineId(receiptSetupDataService.getNextAvailableReceiptLineId());
		
		ReceiptSetupDTO dto = receiptUtils.copyToReceiptSetupDTO(receiptSetup); 
		
		success = receiptSetupDataService.createReceiptSetup(dto);
		
		if(success)
		{
			JOptionPane.showMessageDialog((Component)receiptSetupView, "Configuration Saved!",
					"Save successful", JOptionPane.INFORMATION_MESSAGE);
			receiptSetupView.close();
			receiptSetupView = null;
		}
		else
		{
			JOptionPane.showMessageDialog((Component)receiptSetupView, "Unable to save new configuration, please check the logs.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void setReceiptSetupDataService(
			ReceiptSetupDataService receiptSetupDataService) {
		this.receiptSetupDataService = receiptSetupDataService;
	}

	public void setPrintPreviewService(PrintPreviewService printPreviewService) {
		this.printPreviewService = printPreviewService;
	}

}
