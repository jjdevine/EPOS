package jjdevine.epos.utils;

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import jjdevine.csv.svc.beans.CSVData;
import jjdevine.csv.svc.beans.CSVModel;
import jjdevine.epos.beans.EposPaymentViewBean;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.beans.AdHocDiscount;
import jjdevine.epos.common.beans.CustomerInfo;
import jjdevine.epos.common.beans.Discount;
import jjdevine.epos.common.beans.EposPayment;
import jjdevine.epos.common.beans.EposPurchase;
import jjdevine.epos.common.beans.GarmentData;
import jjdevine.epos.common.beans.TransactionItem;
import jjdevine.epos.common.beans.EposPayment.Type;
import jjdevine.epos.processors.impl.DefaultTransactionReceiptFormatterImpl;
import jjdevine.epos.receiptsetup.view.beans.ReceiptLine;
import jjdevine.epos.receiptsetup.view.beans.ReceiptSection;
import jjdevine.epos.reports.genericreporttemplate1.view.beans.GenericReportTemplate1ViewBean;
import jjdevine.epos.reports.genericreporttemplate1.view.beans.SummaryInformation;
import jjdevine.epos.transaction.EposTransaction;
import jjdevine.printing.beans.PrintingOutputLine;

public class Utils 
{
	public static List<PrintingOutputLine> convertReceiptSectionToPrintingRequest(ReceiptSection receiptSection)
	{
		List<PrintingOutputLine> lines = new ArrayList<PrintingOutputLine>();
		
		for(ReceiptLine receiptLine: receiptSection.getLines())
		{
			PrintingOutputLine line = new PrintingOutputLine();
			
			switch(receiptLine.getType())
			{
				case BLANK:
					line.setType(PrintingOutputLine.Type.BLANK);
					break;
				case DIVIDER:
					line.setType(PrintingOutputLine.Type.DIVIDER);
					break;
				case TEXT:
					line.setType(PrintingOutputLine.Type.TEXT);
					line.setFont(new Font(receiptLine.getFontName(), receiptLine.getFontType(), receiptLine.getFontSize()));
				
					switch(receiptLine.getAlignment())
					{
						case LEFT:
							line.setLeftAlignedText(receiptLine.getText());
							break;
						case CENTRE:
							line.setCentreAlignedText(receiptLine.getText());
							break;
						case RIGHT:
							line.setRightAlignedText(receiptLine.getText());
							break;
					}
					
					break;
			}
			lines.add(line);
		}
		
		return lines;
	}
	
	public static List<PrintingOutputLine> convertEposTransactionToPrintingRequest(EposTransaction transaction)
	{
		return new DefaultTransactionReceiptFormatterImpl().convertEposTransactionToPrintingRequest(transaction);
	}
	
	public static EposTransaction getTestEposTransaction()
	{
		EposTransaction eposTransaction = new EposTransaction();
		
		/*
		 * status
		 */
		eposTransaction.setStatus(EposTransaction.Status.COMPLETE);
		
		/*
		 * customer info
		 */
		CustomerInfo customerInfo = new CustomerInfo();
		customerInfo.setCustomerId(123);
		eposTransaction.setCustomerInfo(customerInfo);
		
		/*
		 * purchases
		 */
		List<TransactionItem> transactionItems = new ArrayList<TransactionItem>();
		eposTransaction.setTransactionItems(transactionItems);
		
		
		
		AdHocDiscount discount = new AdHocDiscount();
		discount.setType(Discount.Type.FLAT_DISCOUNT);
		discount.setValue(10);

		GarmentData garment1 = new GarmentData();
		garment1.setBrand("Nike");
		garment1.setStyle("Trainers");
		garment1.setRetailPrice(25);
		
		GarmentData garment2 = new GarmentData();
		garment2.setBrand("Burberry");
		garment2.setStyle("Cap");
		garment2.setRetailPrice(30);
		
		GarmentData garment3 = new GarmentData();
		garment3.setBrand("Gucci");
		garment3.setStyle("Dress");
		garment3.setRetailPrice(110);
		
		GarmentData garment4 = new GarmentData();
		garment4.setBrand("Bench");
		garment4.setStyle("Shirt");
		garment4.setRetailPrice(30);
			
		EposPurchase purchase1 = new EposPurchase();
		purchase1.setGarment(garment1);
		purchase1.setMode(EposPurchase.Mode.NORMAL);
		transactionItems.add(purchase1);
		
		EposPurchase purchase2 = new EposPurchase();
		purchase2.setGarment(garment2);
		purchase2.setMode(EposPurchase.Mode.NORMAL_DISCOUNTED);
		purchase2.setDiscount(discount);
		transactionItems.add(purchase2);
		
		EposPurchase purchase3 = new EposPurchase();
		purchase3.setGarment(garment3);
		purchase3.setMode(EposPurchase.Mode.PURCHASE_VOID);
		transactionItems.add(purchase3);
		
		EposPurchase purchase4 = new EposPurchase();
		purchase4.setGarment(garment4);
		purchase4.setMode(EposPurchase.Mode.NORMAL);
		transactionItems.add(purchase4);
		
		/*
		 * discounts
		 */
		
		eposTransaction.setDiscounts(new ArrayList<Discount>());
		
		/*
		 * payments
		 */
		
		EposPayment payment1 = new EposPayment();
		payment1.setType(EposPayment.Type.CASH);
		payment1.setAmount(30);
		transactionItems.add(payment1);
		
		EposPayment payment2 = new EposPayment();
		payment2.setType(EposPayment.Type.CARD);
		payment2.setAmount(35);
		transactionItems.add(payment2);
		
		return eposTransaction;
	}
	
	/**
	 * returns a customer friendly String representation of the payment type
	 * @param eposPaymentViewBean
	 * @return
	 */
	public static String getPaymentTypeAsString(EposPaymentViewBean eposPaymentViewBean)
	{
		switch (eposPaymentViewBean.getType())
		{
			case CARD:
				return "Card";		
			case CASH:
				return "Cash";		
			case CHEQUE:
				return "Cheque";				
			case COUPON:
				return "Coupon";
			case RETURNED_ITEM:
				return "Returned Item";
			case CREDIT_NOTE:
				return "Credit Note";
			default:			
				throw new IllegalStateException("No EposPurchase object defined");
		}
	}
	
	public static boolean creditNoteExistsInTransaction(int creditNoteId, EposTransaction transaction)
	{
		boolean result = false;
		
		List<EposPayment> payments = transaction.getPayments();
		
		for(EposPayment payment: payments)
		{
			if(payment.getType() == Type.CREDIT_NOTE)
			{
				if(creditNoteId == payment.getCreditNote().getId() && !payment.isCancelled())
				{
					return true;
				}
			}
		}
		
		return result;
	}
	
	public static File getCSVFileFromUser()
	{
		File result = null;
		
		JFileChooser jfcSave = new JFileChooser();	//create new file chooser
		jfcSave.setApproveButtonText("Save");	//set confirm button text
		jfcSave.setDialogTitle("Choose location to export CSV");	//dialog heading
		int returnVal = jfcSave.showOpenDialog(new JFrame());	//get file chosen

		if(returnVal == JFileChooser.APPROVE_OPTION)	//if file chosen is ok
  		{
    			result = jfcSave.getSelectedFile();	//store chosen file
		}
		
		if(result != null)
		{
			String path = result.getPath();
			
			if(path.length() > 4 && !path.substring(path.length()-4).equalsIgnoreCase(".csv"))
			{
				result = new File(path + ".csv");
			}
			
		}
		
		
		return result;
	}
	
	public static CSVModel convertGenericReportTemplate1ViewBeanToCSVModel(GenericReportTemplate1ViewBean viewBean)
	{
		CSVModel csvModel = new CSVModel();
		
		CSVData csvData = new CSVData(0,0);
		csvData.setValue(viewBean.getReportTitle());
		csvModel.addData(csvData);
		
		csvData = new CSVData(2,0);
		csvData.setValue("Summary Information");
		csvModel.addData(csvData);
		
		csvData = new CSVData(4,0);
		csvData.setValue("Information");
		csvModel.addData(csvData);
		
		csvData = new CSVData(4,1);
		csvData.setValue("Value");
		csvModel.addData(csvData);
		
		int summInfo1Row = 5;
		
		for(SummaryInformation info1: viewBean.getSummaryInformation1())
		{
			csvData = new CSVData(summInfo1Row,0);
			csvData.setValue(info1.getInformation());
			csvModel.addData(csvData);
			
			csvData = new CSVData(summInfo1Row++,1);
			csvData.setValue(info1.getValue());
			csvModel.addData(csvData);
		}
		
		int summInfo2Row = 5;
		
		for(SummaryInformation info2: viewBean.getSummaryInformation2())
		{
			csvData = new CSVData(summInfo2Row,3);
			csvData.setValue(info2.getInformation());
			csvModel.addData(csvData);
			
			csvData = new CSVData(summInfo2Row++,4);
			csvData.setValue(info2.getValue());
			csvModel.addData(csvData);
		}
		
		//lowest row currently used:
		int currentRow = summInfo1Row>summInfo2Row ? summInfo1Row: summInfo2Row;
		
		currentRow += 2;
		int column = 0;
		
		/*
		 * detailed section headings
		 */
		
		for(String heading: viewBean.getDetailedInformationHeadings())
		{
			csvData = new CSVData(currentRow,column++);
			csvData.setValue(heading);
			csvModel.addData(csvData);
		}
		
		//detailed section rows
		
		for(String[] row: viewBean.getDetailedInformationRows())
		{
			currentRow++;
			column = 0;
			
			for(String cell: row)
			{
				csvData = new CSVData(currentRow,column++);
				csvData.setValue(cell);
				csvModel.addData(csvData);
			}
		}
		
		return csvModel;
	}
	
	public static void closeWindows()
	{
		EposUtils.getWindowManagementService().closeAll();
	}
	
}
