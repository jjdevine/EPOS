package jjdevine.epos.processors.impl;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import jjdevine.csv.svc.CSVService;
import jjdevine.csv.svc.beans.CSVRequest;
import jjdevine.epos.EposContext;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.beans.HistoricEposDiscount;
import jjdevine.epos.common.beans.HistoricEposPurchase;
import jjdevine.epos.common.beans.HistoricEposTransaction;
import jjdevine.epos.customerinfo.data.svc.CustomerDataService;
import jjdevine.epos.discountdefintion.model.beans.CustomerInfo;
import jjdevine.epos.processors.ShowTransactionDetailsReportProcessor;
import jjdevine.epos.processors.ShowTransactionSummaryReportProcessor;
import jjdevine.epos.reports.genericreporttemplate1.view.beans.GenericReportTemplate1ViewBean;
import jjdevine.epos.reports.genericreporttemplate1.view.beans.SummaryInformation;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Event;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Listener;
import jjdevine.epos.reports.genericreporttemplate1.view.gui.GenericReportTemplateView1;
import jjdevine.epos.transaction.data.svc.TransactionDataService;
import jjdevine.epos.utils.Utils;

public class ShowTransactionSummaryReportProcessorImpl implements ShowTransactionSummaryReportProcessor, GenericReportTemplateView1Listener 
{
	private TransactionDataService transactionDataService;
	private CustomerDataService customerDataService;
	private CSVService csvService;
	private GenericReportTemplateView1 view;
	private ShowTransactionDetailsReportProcessor transactionDetailsReportProcessor;
	private String[] rowHeadings = {
			"Transaction ID",
			"Items Purchased",
			"Sales Value",
			"Total Discounts",
			"Customer ID",
			"Transaction Date"
			};
	private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	
	@Override
	public void showTransactionSummaryReportByCustomer(int customerId) 
	{
		List<HistoricEposTransaction> transactions =  transactionDataService.getTransactionsForCustomer(customerId);
		
		CustomerInfo customer = customerDataService.getCustomer(customerId);
		
		if(transactions == null || customer == null)
		{
			//TODO display error
			return;
		}
		
		//TODO: times are being lost from transaction dates
		
		List<SummaryInformation> summaryInfo1 = getSummaryInfo1(transactions);
		List<SummaryInformation> summaryInfo2 = getSummaryInfo2(transactions);
		
		GenericReportTemplate1ViewBean viewBean = new GenericReportTemplate1ViewBean();
		viewBean.setSummaryInformation1(summaryInfo1);
		viewBean.setSummaryInformation2(summaryInfo2);
		
		viewBean.setDetailedInformationHeadings(rowHeadings);
		viewBean.setDetailedInformationRows(getRows(transactions));
		
		String customerName = "";
		customerName += customer.getFirstName() == null ? "" : customer.getFirstName() + " ";
		customerName += customer.getLastName() == null ? "" : customer.getLastName();
		
		viewBean.setReportTitle("Transaction Summary for Customer " + customerName);
		
		closeView(); //close any views already open
		view = EposContext.getGenericReportTemplateView1();
		view.addListener(this);
		view.showReport(viewBean);
	}

	@Override
	public void showTransactionSummaryReportByDate(Date from, Date to) 
	{
		List<HistoricEposTransaction> transactions =  transactionDataService.getTransactionsForDates(from, to);
		
		if(transactions == null)
		{
			//TODO display error
			return;
		}
		
		from = EposUtils.setHoursAndMinutes(from, 0, 0);
		to = EposUtils.setHoursAndMinutes(to, 23, 59);
		
		//TODO: need to reset time of day to 00:00 for 'from' date and to 23:59 for 'to' date
		
		List<SummaryInformation> summaryInfo1 = getSummaryInfo1(transactions);
		List<SummaryInformation> summaryInfo2 = getSummaryInfo2(transactions);
		
		GenericReportTemplate1ViewBean viewBean = new GenericReportTemplate1ViewBean();
		viewBean.setSummaryInformation1(summaryInfo1);
		viewBean.setSummaryInformation2(summaryInfo2);
		
		viewBean.setDetailedInformationHeadings(rowHeadings);
		viewBean.setDetailedInformationRows(getRows(transactions));
		
		viewBean.setReportTitle("Transaction Summary for " + dateFormat.format(from) + " to " + dateFormat.format(to));
		
		closeView(); //close any views already open
		view = EposContext.getGenericReportTemplateView1();
		view.addListener(this);
		view.showReport(viewBean);
	}
	
	private void closeView()
	{
		if(view != null)
		{
			view.close();
			view = null;
		}
	}

	private List<SummaryInformation> getSummaryInfo1(List<HistoricEposTransaction> transactions)
	{
		List<SummaryInformation> summaryInfo1 = new ArrayList<SummaryInformation>();
		
		SummaryInformation summaryInfo = new SummaryInformation();
		summaryInfo.setInformation("Number of Transactions");
		summaryInfo.setValue(transactions.size()+"");
		summaryInfo1.add(summaryInfo);
		
		int purchaseCount = 0;
		
		for(HistoricEposTransaction transaction: transactions)
		{
			purchaseCount += transaction.getPurchases().size();
		}
		
		summaryInfo = new SummaryInformation();
		summaryInfo.setInformation("Number of Items Purchased");
		summaryInfo.setValue(purchaseCount+"");
		summaryInfo1.add(summaryInfo);
		
		return summaryInfo1;
	}
	
	private List<SummaryInformation> getSummaryInfo2(List<HistoricEposTransaction> transactions)
	{
		List<SummaryInformation> summaryInfo2 = new ArrayList<SummaryInformation>();
		
		double totalSales = 0;
		double totalDiscounts = 0;
		
		for(HistoricEposTransaction transaction: transactions)
		{
			for(HistoricEposDiscount discount: transaction.getDiscounts())
			{
				totalDiscounts += discount.getDiscountValue();
			}
			
			for(HistoricEposPurchase purchase: transaction.getPurchases())
			{
				//TODO: only issue is this will count member prices as discounts if they are lower
				//need to store if purchase was by a member so can take this into account
				totalDiscounts += purchase.getRetailPrice() - purchase.getPricePaid();
				totalSales += purchase.getPricePaid();
			}
		}
		
		SummaryInformation summaryInfo = new SummaryInformation();
		summaryInfo.setInformation("Total Sales");
		summaryInfo.setValue(EposUtils.formatCurrencyNoCommas(totalSales));
		summaryInfo2.add(summaryInfo);
		
		summaryInfo = new SummaryInformation();
		summaryInfo.setInformation("Total Discounts");
		summaryInfo.setValue(EposUtils.formatCurrencyNoCommas(totalDiscounts));
		summaryInfo2.add(summaryInfo);
		
		return summaryInfo2;
	}
	
	private List<String[]> getRows(List<HistoricEposTransaction> transactions) 
	{
		List<String[]> rows = new ArrayList<String[]>();
		
		for(HistoricEposTransaction transaction: transactions)
		{
			String[] row = new String[6];
			
			row[0] = transaction.getTransactionId() + "";
			row[1] = transaction.getPurchases().size() + "";
			row[2] = EposUtils.formatCurrencyNoCommas(calculateSalesValue(transaction));
			row[3] = EposUtils.formatCurrencyNoCommas(calculateDiscounts(transaction));
			row[4] = transaction.getCustomerInfo() == null ? "" : transaction.getCustomerInfo().getCustomerId()+"";
			row[5] = dateFormat.format(transaction.getTransactionDate());
			
			rows.add(row);
		}
		
		return rows;
	}

	private double calculateSalesValue(HistoricEposTransaction transaction) 
	{
		double salesValue = 0;
		
		for(HistoricEposPurchase purchase: transaction.getPurchases())
		{
			salesValue += purchase.getPricePaid();
		}
		
		return salesValue;
	}

	private double calculateDiscounts(HistoricEposTransaction transaction) 
	{
		double totalDiscounts = 0;
		
		for(HistoricEposDiscount discount: transaction.getDiscounts())
		{
			totalDiscounts += discount.getDiscountValue();
		}
		
		for(HistoricEposPurchase purchase: transaction.getPurchases())
		{
			//TODO: doesn't take member purchases into account - suggest that if customer is a member
			//this should be recorded against the purchase
			totalDiscounts += purchase.getRetailPrice() - purchase.getPricePaid();
		}
		
		return totalDiscounts;
	}

	public TransactionDataService getTransactionDataService() {
		return transactionDataService;
	}

	public void setTransactionDataService(
			TransactionDataService transactionDataService) {
		this.transactionDataService = transactionDataService;
	}

	public CustomerDataService getCustomerDataService() {
		return customerDataService;
	}

	public void setCustomerDataService(CustomerDataService customerDataService) {
		this.customerDataService = customerDataService;
	}

	public CSVService getCsvService() {
		return csvService;
	}

	public void setCsvService(CSVService csvService) {
		this.csvService = csvService;
	}

	public void setTransactionDetailsReportProcessor(
			ShowTransactionDetailsReportProcessor transactionDetailsReportProcessor) {
		this.transactionDetailsReportProcessor = transactionDetailsReportProcessor;
	}

	@Override
	public void onGenericReportTemplateView1Event(GenericReportTemplateView1Event evt) 
	{
		switch(evt.getType())
		{
		case EXPORT_CSV:
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
			 * output csv
			 */
	
			CSVRequest csvReq = new CSVRequest();
			csvReq.setFile(file);
			csvReq.setModel(Utils.convertGenericReportTemplate1ViewBeanToCSVModel(evt.getViewBean()));
			
			csvService.createCSV(csvReq);
			
			view.showMessage("CSV Created: " + file.getPath());
			closeView();
			break;
			
		case DATA_SELECT:
			long transactionId = Long.valueOf(((String[])evt.getDataSelected())[0]); 
			int result = view.showConfirmMessage("Show details for transaction #" + transactionId + " ?");
			
			if(result == JOptionPane.YES_OPTION)
			{
				transactionDetailsReportProcessor.showTransactionDetailsReport(transactionId);
			}
			break;
		}

	}

}
