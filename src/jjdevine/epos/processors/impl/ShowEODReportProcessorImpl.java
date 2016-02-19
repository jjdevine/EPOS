package jjdevine.epos.processors.impl;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jjdevine.csv.svc.CSVService;
import jjdevine.csv.svc.beans.CSVData;
import jjdevine.csv.svc.beans.CSVModel;
import jjdevine.csv.svc.beans.CSVRequest;
import jjdevine.epos.EposContext;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.beans.EposPayment;
import jjdevine.epos.common.beans.HistoricEposDiscount;
import jjdevine.epos.common.beans.HistoricEposPurchase;
import jjdevine.epos.common.beans.HistoricEposTransaction;
import jjdevine.epos.creditnote.data.svc.CreditNoteDataService;
import jjdevine.epos.discountdefintion.model.beans.CreditNote;
import jjdevine.epos.processors.ShowEODReportProcessor;
import jjdevine.epos.reports.endofday.view.beans.EndOfDayReportGarmentSalesDataBean;
import jjdevine.epos.reports.endofday.view.beans.EndOfDayReportViewBean;
import jjdevine.epos.reports.endofday.view.events.EndOfDayReportViewEvent;
import jjdevine.epos.reports.endofday.view.events.EndOfDayReportViewListener;
import jjdevine.epos.reports.endofday.view.gui.EndOfDayReportView;
import jjdevine.epos.services.manageorder.persistence.ManageOrderDatasource;
import jjdevine.epos.services.manageorder.persistence.ManageOrderDatasourceFactory;
import jjdevine.epos.services.orders.common.beans.OrderLineItemBean;
import jjdevine.epos.transaction.data.svc.TransactionDataService;
import jjdevine.epos.utils.Utils;

import org.apache.log4j.Logger;

public class ShowEODReportProcessorImpl implements ShowEODReportProcessor, EndOfDayReportViewListener 
{
	private TransactionDataService transactionDataService;
	private CreditNoteDataService creditNoteDataService;
	private ManageOrderDatasource manageOrderDatasource;
	private CSVService csvService;
	private String reportDateString;
	private EndOfDayReportView view;
	private Logger logger = Logger.getLogger(this.getClass());
	
	public ShowEODReportProcessorImpl()
	{
		//TODO: replace with spring
		manageOrderDatasource = ManageOrderDatasourceFactory.createManageOrderDatasource();
	}
	
	@Override
	public void showEODReportForDay(Date day) 
	{
		logger.debug("Showing EOD report for " + day);
		/*
		 * need to set start date to 00:00, and end date to 23:59
		 */
		
		Calendar startCal = Calendar.getInstance();
		startCal.setTimeInMillis(day.getTime());
		startCal.set(Calendar.HOUR_OF_DAY, 0);
		startCal.set(Calendar.MINUTE, 0);
		startCal.set(Calendar.SECOND, 0);
		startCal.set(Calendar.MILLISECOND, 0);
		
		Date startDate = startCal.getTime();
		
		startCal.roll(Calendar.DATE, true);

		Calendar endCal = Calendar.getInstance();
		endCal.setTimeInMillis(day.getTime());
		endCal.set(Calendar.HOUR_OF_DAY, 23);
		endCal.set(Calendar.MINUTE, 59);
		endCal.set(Calendar.SECOND, 59);
		endCal.set(Calendar.MILLISECOND, 999);
		
		Date endDate = endCal.getTime();
		
		reportDateString = DateFormat.getDateInstance().format(day);
		
		showEODReportForPeriod(startDate, endDate);
	}

	@Override
	public void showEODReportForPeriod(Date startDate, Date endDate) 
	{
		EndOfDayReportViewBean viewBean = new EndOfDayReportViewBean();
		
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		
		if(reportDateString == null) //won't be null if showEODReportForDay has called this method
		{
			reportDateString = df.format(startDate) + " - " + df.format(endDate);
		}
		
		/*
		 * number of transactions
		 */
		int numTransactions = transactionDataService.getTransactionCountForDates(startDate, endDate);
		if(numTransactions == -1)
		{
			logger.error("numTransactions is -1");
			//TODO: show error - static utility method to get transaction screen as parent?
			return;
		}
		
		/*
		 * get data needed for report
		 */
		
		List<HistoricEposTransaction> transactions = transactionDataService.getTransactionsForDates(startDate, endDate);
		if(transactions == null)
		{
			logger.error("numTransactions is -1");
			return;
		}
		
		List<CreditNote> creditNotes = creditNoteDataService.findCreditNotesByIssueDate(startDate, endDate);
		
		List<HistoricEposPurchase> purchases = new ArrayList<HistoricEposPurchase>();
		List<EposPayment> payments = new ArrayList<EposPayment>();
		double totalChange = 0;//keep track of change issued
		
		for(HistoricEposTransaction transaction: transactions)
		{
			purchases.addAll(transaction.getPurchases());
			payments.addAll(transaction.getPayments());
			totalChange += transaction.getChangeGiven();
		}
		
		/*
		 * number of items sold
		 */
		
		int numItemsSold = purchases.size();
		
		for(HistoricEposPurchase purchase: purchases)
		{
			if(purchase.isPurchaseVoid())
			{
				//don't count items that are void
				numItemsSold -= 1;
			}
		}
		
		/*
		 * number of credit notes issued
		 */		
		
		if(creditNotes == null)
		{
			logger.error("list of credit notes is null");
			return;
		}
		
		int numCreditNotesIssued = creditNotes.size();
		
		/*
		 * cash payments taken
		 */
		
		double cashTaken = 0;
		double cardPaymentsTaken = 0;
		double chequePaymentsTaken = 0;
		double couponPaymentsTaken = 0;
		double creditNotePaymentsTaken = 0;
		double returnedItemPaymentsTaken = 0;
		
		for(EposPayment payment: payments)
		{
			switch(payment.getType())
			{
				case CARD:
					cardPaymentsTaken += payment.getAmount();
					break;
				case CASH:
					cashTaken += payment.getAmount();
					break;
				case CHEQUE:
					chequePaymentsTaken += payment.getAmount();
					break;
				case COUPON:
					couponPaymentsTaken += payment.getAmount();
					break;
				case CREDIT_NOTE:
					creditNotePaymentsTaken += payment.getAmount();
					break;
				case RETURNED_ITEM:
					returnedItemPaymentsTaken += payment.getAmount();
					break;
			}
		}
		
		//need to minus change given from cash taken
		cashTaken -= totalChange;
		
		/*
		 * transaction level discounts
		 */
		
		double totalTransactionDiscounts = 0;
		
		for(HistoricEposTransaction transaction: transactions)
		{
			for(HistoricEposDiscount discount: transaction.getDiscounts())
			{
				totalTransactionDiscounts += discount.getDiscountValue();
			}
		}
		
		/*
		 * garments sold
		 */
		
		double totalCostPrice = 0;
		double totalSales = 0;
		
		List<EndOfDayReportGarmentSalesDataBean> salesDataList = new ArrayList<EndOfDayReportGarmentSalesDataBean>();
		
		for(HistoricEposPurchase purchase: purchases)
		{
			if(purchase.isPurchaseVoid())
			{
				//ignore this purchase
				continue;
			}
			long skuId = purchase.getGarmentData().getSkuId();
			
			EndOfDayReportGarmentSalesDataBean salesData = new EndOfDayReportGarmentSalesDataBean();
			
			double avgCostPrice = calculateAvgCostPriceForSkuId(skuId);
			
			salesData.setAvgCostPrice(avgCostPrice);
			salesData.setGarmentCode(purchase.getGarmentData().getGarmentCode());
			salesData.setRetailPrice(purchase.getGarmentData().getRetailPrice());
			salesData.setSoldPrice(purchase.getPricePaid());
			salesData.setColour(purchase.getGarmentData().getColourDesc());
			salesData.setSize(purchase.getGarmentData().getSize1());
			
			//check if a matching garment has been sold
			EndOfDayReportGarmentSalesDataBean existingSale = getMatchingEndOfDayReportGarmentSalesDataBean(
					purchase.getGarmentData().getGarmentCode(), purchase.getGarmentData().getColourDesc(), 
					purchase.getGarmentData().getSize1(), purchase.getPricePaid(), salesDataList);
			
			if(existingSale != null)
			{
				existingSale.setQtySold(existingSale.getQtySold()+1);
			}
			else
			{
				salesData.setQtySold(1);
				salesDataList.add(salesData);
			}
			
			//keep track of total sales
			totalSales += purchase.getPricePaid();
			//keep track of total cost price of all items sold
			totalCostPrice += avgCostPrice;
		}
		
		//need to take transaction level discounts into account
		totalSales -= totalTransactionDiscounts;

		viewBean.setDatePeriodText(reportDateString);
		reportDateString = null;//reset for next use of this class
		viewBean.setNumTransactions(numTransactions);
		viewBean.setItemsSold(numItemsSold);
		viewBean.setCreditNotesIssued(numCreditNotesIssued);
		viewBean.setCashTaken(cashTaken);
		viewBean.setCardPaymentsTaken(cardPaymentsTaken);
		viewBean.setChequePaymentsTaken(chequePaymentsTaken);
		viewBean.setCouponPaymentsTaken(couponPaymentsTaken);
		viewBean.setCreditNotePaymentsTaken(creditNotePaymentsTaken);
		viewBean.setTotalTaken(totalSales);
		viewBean.setGarmentSalesDataBeans(salesDataList);
		viewBean.setTotalProfit(totalSales-totalCostPrice);
		viewBean.setTotalTransactionDiscounts(totalTransactionDiscounts);
		
		closeView(); //close any views already open
		view = EposContext.getEndOfDayReportView();
		view.showEndOfDayReport(viewBean);
		view.addListener(this);
	}
	
	private void closeView()
	{
		if(view != null)
		{
			view.close();
			view = null;
		}
	}
	
	public static EndOfDayReportGarmentSalesDataBean getTestBean(String gCode, double soldPrice, int qty, double retailPrice, double avgCostPrice)
	{
		EndOfDayReportGarmentSalesDataBean bean = new EndOfDayReportGarmentSalesDataBean();
		bean.setGarmentCode(gCode);
		bean.setSoldPrice(soldPrice);
		bean.setQtySold(qty);
		bean.setRetailPrice(retailPrice);
		bean.setAvgCostPrice(avgCostPrice);
		
		return bean;
	}

	public TransactionDataService getTransactionDataService() {
		return transactionDataService;
	}

	public void setTransactionDataService(
			TransactionDataService transactionDataService) {
		this.transactionDataService = transactionDataService;
	}

	public CreditNoteDataService getCreditNoteDataService() {
		return creditNoteDataService;
	}

	public void setCreditNoteDataService(CreditNoteDataService creditNoteDataService) {
		this.creditNoteDataService = creditNoteDataService;
	}
	
	public CSVService getCsvService() {
		return csvService;
	}

	public void setCsvService(CSVService csvService) {
		this.csvService = csvService;
	}

	private double calculateAvgCostPriceForSkuId(long skuId)
	{
		double result = 0;
		
		List<OrderLineItemBean> orderLineItems = manageOrderDatasource.getAllOrderLineItemsForSKUId(skuId);
		
		long totalItems = 0;
		//total paid is the value in pence
		long totalPaid = 0;
		
		for(OrderLineItemBean orderLineItem: orderLineItems)
		{
			
			totalItems += orderLineItem.getQtyDelivered();
			double costPriceUnit = orderLineItem.getCostPriceUnit();
			
			long costPriceUnitInPence = EposUtils.convertCurrencyDoubleToLong(costPriceUnit);
			
			totalPaid += (costPriceUnitInPence*orderLineItem.getQtyDelivered());
		}
		
		if(totalItems > 0)
		{
			result = totalPaid/totalItems/100;
		}
		
		return result;
	}
	
	private EndOfDayReportGarmentSalesDataBean getMatchingEndOfDayReportGarmentSalesDataBean(String garmentCode, String colour, String size, double soldPrice, List<EndOfDayReportGarmentSalesDataBean> list)
	{
		for(EndOfDayReportGarmentSalesDataBean bean: list)
		{
			if(bean.getGarmentCode().equals(garmentCode)
					&& bean.getColour().equals(colour)
					&& bean.getSize().equals(size)
					&& bean.getSoldPrice() == soldPrice)
			{
				return bean;
			}
				
		}
		
		return null;
	}

	@Override
	public void onEndOfDayReportViewEvent(EndOfDayReportViewEvent evt) 
	{
		switch(evt.getType())
		{
			case EXPORT_CSV:
				try
				{
					exportCSV(evt.getViewBean());
				}
				catch(Exception ex)
				{
					view.showErrorMessage("Could not export CSV file: " + ex.getMessage());
					logger.error(ex.getMessage(), ex);
				}
				break;
		}
	}

	private void exportCSV(EndOfDayReportViewBean viewBean) 
	{
		/*
		 * get file name to export to
		 */
		
		File file = Utils.getCSVFileFromUser();
		
		if(file == null)
		{
			return;
		}
		
		String path = file.getPath();
		
		if(path.length() > 4 && !path.substring(path.length()-4).equalsIgnoreCase(".csv"))
		{
			file = new File(path + ".csv");
		}
		
		/*
		 * format to csv model
		 */
		
		
		CSVModel csvModel = new CSVModel();
		
		CSVData csvData;
		
		csvData = new CSVData(0,0);
		csvData.setValue("EOD Report for " + viewBean.getDatePeriodText());
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
	
		csvData = new CSVData(5,0);
		csvData.setValue("Number of Transactions");
		csvModel.addData(csvData);
		
		csvData = new CSVData(5,1);
		csvData.setValue(viewBean.getNumTransactions()+"");
		csvModel.addData(csvData);
		
		csvData = new CSVData(6,0);
		csvData.setValue("Number of Items Sold");
		csvModel.addData(csvData);
		
		csvData = new CSVData(6,1);
		csvData.setValue(viewBean.getItemsSold()+"");
		csvModel.addData(csvData);
		
		csvData = new CSVData(7,0);
		csvData.setValue("Credit Notes Issued");
		csvModel.addData(csvData);
		
		csvData = new CSVData(7,1);
		csvData.setValue(viewBean.getCreditNotesIssued()+"");
		csvModel.addData(csvData);
		
		csvData = new CSVData(8,0);
		csvData.setValue("Transaction Discounts");
		csvModel.addData(csvData);
		
		csvData = new CSVData(8,1);
		csvData.setValue(EposUtils.formatCurrencyNoCommas(viewBean.getTotalTransactionDiscounts()));
		csvModel.addData(csvData);
		
		csvData = new CSVData(9,0);
		csvData.setValue("Total Sales");
		csvModel.addData(csvData);
		
		csvData = new CSVData(9,1);
		csvData.setValue(EposUtils.formatCurrencyNoCommas(viewBean.getTotalTaken()));
		csvModel.addData(csvData);
		
		csvData = new CSVData(10,0);
		csvData.setValue("Total Profit");
		csvModel.addData(csvData);
		
		csvData = new CSVData(10,1);
		csvData.setValue(EposUtils.formatCurrencyNoCommas(viewBean.getTotalProfit()));
		csvModel.addData(csvData);
		
		csvData = new CSVData(4,3);
		csvData.setValue("Information");
		csvModel.addData(csvData);
		
		csvData = new CSVData(4,4);
		csvData.setValue("Value");
		csvModel.addData(csvData);
		
		csvData = new CSVData(5,3);
		csvData.setValue("Cash Payments Taken");
		csvModel.addData(csvData);
		
		csvData = new CSVData(5,4);
		csvData.setValue(EposUtils.formatCurrencyNoCommas(viewBean.getCashTaken()));
		csvModel.addData(csvData);
		
		csvData = new CSVData(6,3);
		csvData.setValue("Card Payments Taken");
		csvModel.addData(csvData);
		
		csvData = new CSVData(6,4);
		csvData.setValue(EposUtils.formatCurrencyNoCommas(viewBean.getCardPaymentsTaken()));
		csvModel.addData(csvData);
		
		csvData = new CSVData(7,3);
		csvData.setValue("Cheque Payments Taken");
		csvModel.addData(csvData);
		
		csvData = new CSVData(7,4);
		csvData.setValue(EposUtils.formatCurrencyNoCommas(viewBean.getChequePaymentsTaken()));
		csvModel.addData(csvData);
		
		csvData = new CSVData(8,3);
		csvData.setValue("Coupon Payments Taken");
		csvModel.addData(csvData);
		
		csvData = new CSVData(8,4);
		csvData.setValue(EposUtils.formatCurrencyNoCommas(viewBean.getCouponPaymentsTaken()));
		csvModel.addData(csvData);
		
		csvData = new CSVData(9,3);
		csvData.setValue("Credit Note Payments Taken");
		csvModel.addData(csvData);
		
		csvData = new CSVData(9,4);
		csvData.setValue(EposUtils.formatCurrencyNoCommas(viewBean.getCreditNotePaymentsTaken()));
		csvModel.addData(csvData);
		
		csvData = new CSVData(10,3);
		csvData.setValue("Returned Items Value");
		csvModel.addData(csvData);
		
		csvData = new CSVData(10,4);
		csvData.setValue(EposUtils.formatCurrencyNoCommas(viewBean.getReturnedItemPaymentsTaken()));
		csvModel.addData(csvData);
		
		/*
		 * garment sales section
		 */
		
		/*
		 * headings
		 */
		
		csvData = new CSVData(12,0);
		csvData.setValue("Garment Code");
		csvModel.addData(csvData);
		
		csvData = new CSVData(12,1);
		csvData.setValue("Size");
		csvModel.addData(csvData);
		
		csvData = new CSVData(12,2);
		csvData.setValue("Colour");
		csvModel.addData(csvData);
		
		csvData = new CSVData(12,3);
		csvData.setValue("Sold Price");
		csvModel.addData(csvData);
		
		csvData = new CSVData(12,4);
		csvData.setValue("Qty Sold");
		csvModel.addData(csvData);
		
		csvData = new CSVData(12,5);
		csvData.setValue("Retail Price");
		csvModel.addData(csvData);
		
		csvData = new CSVData(12,6);
		csvData.setValue("Avg Cost Price");
		csvModel.addData(csvData);
		
		/*
		 * loop round sales data
		 */
		
		int row = 13;
		
		for(EndOfDayReportGarmentSalesDataBean garmentBean: viewBean.getGarmentSalesDataBeans())
		{
			csvData = new CSVData(row,0);
			csvData.setValue(garmentBean.getGarmentCode());
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,1);
			csvData.setValue(garmentBean.getSize());
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,2);
			csvData.setValue(garmentBean.getColour());
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,3);
			csvData.setValue(EposUtils.formatCurrencyNoCommas(garmentBean.getSoldPrice()));
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,4);
			csvData.setValue(garmentBean.getQtySold()+"");
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,5);
			csvData.setValue(EposUtils.formatCurrencyNoCommas(garmentBean.getRetailPrice()));
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,6);
			csvData.setValue(EposUtils.formatCurrencyNoCommas(garmentBean.getAvgCostPrice()));
			csvModel.addData(csvData);

			row++;
		}
		
		/*
		 * output csv
		 */

		CSVRequest csvReq = new CSVRequest();
		csvReq.setFile(file);
		csvReq.setModel(csvModel);
		
		csvService.createCSV(csvReq);
		
		view.showMessage("CSV Created: " + file.getPath());
		closeView();
	}

}
