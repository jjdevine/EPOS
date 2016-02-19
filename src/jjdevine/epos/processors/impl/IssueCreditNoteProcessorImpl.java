package jjdevine.epos.processors.impl;

import java.awt.Font;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import jjdevine.epos.BeanCopyUtils;
import jjdevine.epos.EposContext;
import jjdevine.epos.beans.Preferences;
import jjdevine.epos.common.Constants;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.creditnote.data.svc.CreditNoteDataService;
import jjdevine.epos.creditnote.view.beans.CreditNoteReasonViewBean;
import jjdevine.epos.creditnote.view.events.CreditNoteViewEvent;
import jjdevine.epos.creditnote.view.events.CreditNoteViewListener;
import jjdevine.epos.creditnote.view.gui.CreditNoteView;
import jjdevine.epos.discountdefintion.model.beans.CreditNote;
import jjdevine.epos.discountdefintion.model.beans.CreditNoteReason;
import jjdevine.epos.processors.IssueCreditNoteProcessor;
import jjdevine.epos.receiptsetup.view.beans.ReceiptSetup;
import jjdevine.epos.utils.ReceiptUtils;
import jjdevine.epos.utils.Utils;
import jjdevine.printing.PrintingService;
import jjdevine.printing.beans.PrintingOutputLine;
import jjdevine.printing.beans.PrintingRequest;
import jjdevine.printing.beans.PrintingOutputLine.Type;

public class IssueCreditNoteProcessorImpl implements IssueCreditNoteProcessor, CreditNoteViewListener 
{
	private CreditNoteDataService creditNoteDataService;
	private CreditNoteView creditNoteView;
	private Preferences preferences;
	private PrintingService printingService;
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void issueCreditNote() 
	{
		if(creditNoteView != null)
		{
			closeView();
		}
		
		creditNoteView = EposContext.getCreditNoteView();
		creditNoteView.addListener(this);
		
		List<CreditNoteReasonViewBean> creditNoteReasonViewBeans = new ArrayList<CreditNoteReasonViewBean>();
		
		for(CreditNoteReason reason: creditNoteDataService.getAllCreditNoteReasons())
		{
			creditNoteReasonViewBeans.add(BeanCopyUtils.mapCreditNoteReasonToViewBean(reason));
		}
		
		creditNoteView.setCreditNoteReasons(creditNoteReasonViewBeans);
	}
	
	private void closeView() 
	{
		creditNoteView.close();
		creditNoteView = null;
	}
	
	@Override
	public void onCreditNoteViewEvent(CreditNoteViewEvent evt) 
	{
		switch(evt.getType())
		{
			case CLOSE:
				closeView();
				break;
			case DISPOSE:
				creditNoteView = null;
				break;
			case CREATE_CREDIT_NOTE:	
				boolean success;
				CreditNote creditNote = BeanCopyUtils.copyCreditNoteViewBeanToModel(evt.getCreditNoteViewBean());
				
				/*
				 * make entry in DB
				 */
				
				success = makeEntryInDB(creditNote);
				
				if(!success)
				{
					return;
				}
				
				/*
				 * print credit note
				 */
				
				try
				{
					printCreditNote(creditNote);
				}
				catch(Exception ex)
				{
					logger.error(ex.getMessage(), ex);
					creditNoteView.showErrorMessage("Unable to print credit note - please issue manually. Reference number in DB is '" +
							pad(creditNote.getId()) + "'");
					return;
				}
				
				closeView();
				
				break;
		}
	}

	private boolean makeEntryInDB(CreditNote creditNote) {
		boolean success;
		
		
		CreditNoteReason creditNoteReason = creditNote.getReasonForIssue();
		
		if(creditNoteReason.getId() == -1)//does not exist in DB
		{
			int nextReasonId = creditNoteDataService.getNextAvailableCreditNoteReasonId();
			
			if(nextReasonId == -1)
			{
				creditNoteView.showErrorMessage("Unable to create credit note in DB (1)");
				return false;
			}
			
			creditNoteReason.setId(nextReasonId);
			
			success = creditNoteDataService.createCreditNoteReason(creditNoteReason);
			
			if(!success)
			{
				creditNoteView.showErrorMessage("Unable to create credit note in DB (2)");
				return false;
			}
		}
		
		if(creditNote.getId() == -1)  //new credit note
		{
			int nextId = creditNoteDataService.getNextAvailableCreditNoteId();
			
			if(nextId == -1)
			{
				creditNoteView.showErrorMessage("Unable to create credit note in DB (3)");
				return false;
			}
			
			creditNote.setId(nextId);
			
			success = creditNoteDataService.createCreditNote(creditNote);
			
			if(!success)
			{
				creditNoteView.showErrorMessage("Unable to create credit note in DB (4)");
				return false;
			}
		}
		else
		{
			success = creditNoteDataService.updateCreditNote(creditNote);
			
			if(!success)
			{
				creditNoteView.showErrorMessage("Unable to create credit note in DB (5)");
				return false;
			}
		}
		
		return true;
	}
	
	private void printCreditNote(CreditNote creditNote) 
	{
		this.preferences = EposContext.getPreferences();
		
		ReceiptSetup receiptSetup = ReceiptUtils.copyToReceiptSetupViewBean(preferences.getActiveReceiptSetup());
		List<PrintingOutputLine> printingOutputLines = Utils.convertReceiptSectionToPrintingRequest(receiptSetup.getTopSection());

		PrintingOutputLine line = new PrintingOutputLine();
		line.setType(Type.TEXT);
		
		if(creditNote.getReasonForIssue().getId() == Constants.CREDIT_NOTE_GIFT_VOUCHER_ID)
		{
			line.setCentreAlignedText("GIFT VOUCHER");
		}
		else
		{
			line.setCentreAlignedText("CREDIT NOTE");
		}
	
		line.setFont(new Font("COURIER NEW", Font.BOLD, 16));
		printingOutputLines.add(line);
		
		line = new PrintingOutputLine();
		line.setType(Type.BLANK);
		printingOutputLines.add(line);
		
		line = new PrintingOutputLine();
		line.setType(Type.TEXT);
		line.setLeftAlignedText("Ref No. :");
		line.setRightAlignedText(pad(creditNote.getId()));
		printingOutputLines.add(line);
		
		line = new PrintingOutputLine();
		line.setType(Type.TEXT);
		line.setLeftAlignedText("Value :");
		line.setRightAlignedText(EposUtils.formatCurrency(creditNote.getTotalValue()));
		printingOutputLines.add(line);
		
		line = new PrintingOutputLine();
		line.setType(Type.TEXT);
		line.setLeftAlignedText("Issue Date :");
		line.setRightAlignedText(DateFormat.getDateInstance(DateFormat.SHORT).format(creditNote.getIssueDate()));
		printingOutputLines.add(line);
		
		line = new PrintingOutputLine();
		line.setType(Type.BLANK);
		printingOutputLines.add(line);
		
		printingOutputLines.addAll(Utils.convertReceiptSectionToPrintingRequest(receiptSetup.getBottomSection()));
		
		PrintingRequest printReq = new PrintingRequest();
		printReq.setPrintingOutputLines(printingOutputLines);
		printReq.setFont(Constants.DEFAULT_PRINTING_FONT);
		
		printingService.doPrinting(printReq);
	}

	public void setCreditNoteDataService(CreditNoteDataService creditNoteDataService) {
		this.creditNoteDataService = creditNoteDataService;
	}

	public void setPreferences(Preferences preferences) {
		this.preferences = preferences;
	}

	public void setPrintingService(PrintingService printingService) {
		this.printingService = printingService;
	}
	
	/**
	 * pad to six chars, eg '5' will become '000005'
	 * @param num
	 * @return
	 */
	private String pad(int num)
	{
		StringBuilder result = new StringBuilder(""+num);
		
		while(result.length() < 6)
		{
			result.insert(0, "0");
		}
		
		return result.toString();
	}
}
