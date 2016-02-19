package jjdevine.epos.processors.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jjdevine.csv.svc.CSVService;
import jjdevine.csv.svc.beans.CSVData;
import jjdevine.csv.svc.beans.CSVModel;
import jjdevine.csv.svc.beans.CSVRequest;
import jjdevine.epos.EposContext;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.beans.Discount;
import jjdevine.epos.common.beans.EposPayment;
import jjdevine.epos.common.beans.HistoricEposDiscount;
import jjdevine.epos.common.beans.HistoricEposPurchase;
import jjdevine.epos.common.beans.HistoricEposTransaction;
import jjdevine.epos.common.beans.EposPayment.Type;
import jjdevine.epos.discountdefintion.data.svc.DiscountDefinitionDataService;
import jjdevine.epos.discountdefintion.model.beans.CustomerInfo;
import jjdevine.epos.discountdefintion.model.beans.DiscountPolicy;
import jjdevine.epos.processors.ShowTransactionDetailsReportProcessor;
import jjdevine.epos.reports.transactiondetails.view.beans.TransactionDetailsReportDiscount;
import jjdevine.epos.reports.transactiondetails.view.beans.TransactionDetailsReportPayment;
import jjdevine.epos.reports.transactiondetails.view.beans.TransactionDetailsReportPurchase;
import jjdevine.epos.reports.transactiondetails.view.beans.TransactionDetailsReportViewBean;
import jjdevine.epos.reports.transactiondetails.view.events.TransactionDetailsReportViewEvent;
import jjdevine.epos.reports.transactiondetails.view.events.TransactionDetailsReportViewListener;
import jjdevine.epos.reports.transactiondetails.view.gui.TransactionDetailsReportView;
import jjdevine.epos.transaction.data.svc.TransactionDataService;
import jjdevine.epos.utils.Utils;

public class ShowTransactionDetailsReportProcessorImpl implements ShowTransactionDetailsReportProcessor, TransactionDetailsReportViewListener 
{
	private TransactionDataService transactionDataService;
	private DiscountDefinitionDataService discountDefinitionDataService;
	private CSVService csvService;
	private TransactionDetailsReportView view;

	@Override
	public void showTransactionDetailsReport(long transactionId) 
	{
		HistoricEposTransaction transaction = transactionDataService.getTransactionById(transactionId);
		
		if(transaction == null)
		{
			//TODO: show error
			return;
		}
		
		TransactionDetailsReportViewBean viewBean = new TransactionDetailsReportViewBean();
		
		viewBean.setTransactionId(transactionId);
		setPurchases(viewBean, transaction);
		setPayments(viewBean, transaction);
		setDiscounts(viewBean, transaction);
		setSummary(viewBean, transaction);
		
		closeView();
		view = EposContext.getTransactionDetailsReportView();
		view.addListener(this);
		view.showReport(viewBean);
		//TODO: time being lost from date
	}
	
	private void setSummary(TransactionDetailsReportViewBean viewBean, HistoricEposTransaction transaction) 
	{
		CustomerInfo custInfo = transaction.getCustomerInfo();
		if(custInfo != null)
		{
			viewBean.setCustomer(
					(custInfo.getFirstName() == null ? "" : custInfo.getFirstName() + " ") +
					(custInfo.getLastName() == null ? "" : custInfo.getLastName() + " ") +
					"(" + custInfo.getCustomerId() + ")"
			);
		}
		
		viewBean.setTransactionDate(transaction.getTransactionDate());
		
		double totalDiscounts = 0;
		double totalPaid = 0;
		double totalPurchaseValue = 0;
		double subtotal = 0;
		
		for(HistoricEposPurchase purchase: transaction.getPurchases())
		{
			totalPurchaseValue += purchase.getRetailPrice();
			if(purchase.getDiscount() != null)
			{
				totalDiscounts += purchase.getDiscount().getValue();
			}
		}
		
		for(EposPayment payment: transaction.getPayments())
		{
			totalPaid += payment.getAmount();
		}
		
		for(HistoricEposDiscount discount: transaction.getDiscounts())
		{
			totalDiscounts += discount.getDiscountValue();
		} 
		
		subtotal = totalPurchaseValue - totalDiscounts;
		
		viewBean.setTotalDiscounts(totalDiscounts);
		viewBean.setTotalPaid(totalPaid);
		viewBean.setTotalPurchaseValue(totalPurchaseValue);
		viewBean.setSubtotal(subtotal);
	}

	private void setDiscounts(TransactionDetailsReportViewBean viewBean, HistoricEposTransaction transaction) 
	{
		List<TransactionDetailsReportDiscount> discounts = new ArrayList<TransactionDetailsReportDiscount>();
		viewBean.setDiscounts(discounts);
		
		TransactionDetailsReportDiscount discount = null;
		for(HistoricEposDiscount txDiscount: transaction.getDiscounts())
		{
			discount = new TransactionDetailsReportDiscount();
			
			discount.setAmount(txDiscount.getDiscountValue());
			DiscountPolicy discountPolicy = discountDefinitionDataService.getDiscountPolicy(txDiscount.getDiscountPolicyId());
			if(discountPolicy != null)
			{
				discount.setDiscountPolicy(discountPolicy.getPolicyName());
			}
			
			discounts.add(discount);
		}
	}

	private void setPayments(TransactionDetailsReportViewBean viewBean,
			HistoricEposTransaction transaction) 
	{
		List<TransactionDetailsReportPayment> payments = new ArrayList<TransactionDetailsReportPayment>();
		viewBean.setPayments(payments);
		
		TransactionDetailsReportPayment payment = null;
		for(EposPayment txPayment: transaction.getPayments())
		{
			payment = new TransactionDetailsReportPayment();
			
			payment.setAmount(txPayment.getAmount());
			payment.setType(txPayment.getType().toString());
			
			if(txPayment.getType() == Type.CREDIT_NOTE)
			{
				payment.setCreditNoteId(txPayment.getCreditNote().getId());
			}
			
			payments.add(payment);
		}
	}

	private void setPurchases(TransactionDetailsReportViewBean viewBean,HistoricEposTransaction transaction) 
	{
		List<TransactionDetailsReportPurchase> purchases = new ArrayList<TransactionDetailsReportPurchase>();
		viewBean.setPurchases(purchases);
		
		TransactionDetailsReportPurchase purchase = null;
		Discount discount = null;
		for(HistoricEposPurchase txPurchase: transaction.getPurchases())
		{
			if(txPurchase.isPurchaseVoid())
			{
				continue;
			}
			
			purchase = new TransactionDetailsReportPurchase();
			purchases.add(purchase);
			
			discount = txPurchase.getDiscount();
			
			purchase.setColour(txPurchase.getGarmentData().getColourDesc());
			if(discount != null)
			{
				purchase.setDiscount(txPurchase.getDiscount().getValue());
				purchase.setDiscountReason(txPurchase.getDiscount().getDescription());
			}
			purchase.setGarmentCode(txPurchase.getGarmentData().getGarmentCode());
			purchase.setPricePaid(txPurchase.getPricePaid());
			purchase.setRetailPrice(txPurchase.getRetailPrice());
			purchase.setSize(txPurchase.getGarmentData().getSize1());
		}
	}

	private void closeView()
	{
		if(view != null)
		{
			view.close();
			view = null;
		}
	}
	
	@Override
	public void onTransactionDetailsReportViewEvent(TransactionDetailsReportViewEvent evt) 
	{
		switch(evt.getType())
		{
		case CLOSE:
			closeView();
			break;
			
		case EXPORT_CSV:
			exportCSV(evt.getViewBean());
			break;
		}
		
	}

	private void exportCSV(TransactionDetailsReportViewBean viewBean) 
	{
		/*
		 * get file name to export to
		 */
		
		File file = Utils.getCSVFileFromUser();
		
		if(file == null)
		{
			return;
		}

		/*
		 * output csv
		 */

		CSVRequest csvReq = new CSVRequest();
		csvReq.setFile(file);
		
		CSVModel csvModel = new CSVModel();
		csvReq.setModel(csvModel);
		
		/*
		 * Header
		 */
		
		CSVData csvData = new CSVData(0,0);
		csvData.setValue("Transaction #" + viewBean.getTransactionId() + " Details Report");
		csvModel.addData(csvData);
		
		/*
		 * Purchases
		 */
		
		csvData = new CSVData(2,0);
		csvData.setValue("Purchases");
		csvModel.addData(csvData);
		
		csvData = new CSVData(4,0);
		csvData.setValue("Garment Code");
		csvModel.addData(csvData);
		
		csvData = new CSVData(4,1);
		csvData.setValue("Colour");
		csvModel.addData(csvData);
		
		csvData = new CSVData(4,2);
		csvData.setValue("Size");
		csvModel.addData(csvData);
		
		csvData = new CSVData(4,3);
		csvData.setValue("Retail Price");
		csvModel.addData(csvData);
		
		csvData = new CSVData(4,4);
		csvData.setValue("Price Paid");
		csvModel.addData(csvData);
		
		csvData = new CSVData(4,5);
		csvData.setValue("Discount");
		csvModel.addData(csvData);
		
		csvData = new CSVData(4,6);
		csvData.setValue("Discount Reason");
		csvModel.addData(csvData);
		
		int row=5; //current row, as need to keep track of it from now on
		
		for(TransactionDetailsReportPurchase purchase: viewBean.getPurchases())
		{
			csvData = new CSVData(row,0);
			csvData.setValue(purchase.getGarmentCode());
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,1);
			csvData.setValue(purchase.getColour());
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,2);
			csvData.setValue(purchase.getSize());
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,3);
			csvData.setValue(EposUtils.formatCurrencyNoCommas(purchase.getRetailPrice()));
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,4);
			csvData.setValue(EposUtils.formatCurrencyNoCommas(purchase.getPricePaid()));
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,5);
			csvData.setValue(EposUtils.formatCurrencyNoCommas(purchase.getDiscount()));
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,6);
			csvData.setValue(purchase.getDiscountReason());
			csvModel.addData(csvData);
			
			row++;
		}
		
		/*
		 * Payments
		 */
		
		//leave gap from last section
		row+=2;
		
		csvData = new CSVData(row,0);
		csvData.setValue("Payments");
		csvModel.addData(csvData);
		
		row+=2;
		
		csvData = new CSVData(row,0);
		csvData.setValue("Type");
		csvModel.addData(csvData);
		
		csvData = new CSVData(row,1);
		csvData.setValue("Amount");
		csvModel.addData(csvData);
		
		csvData = new CSVData(row,2);
		csvData.setValue("Credit Note Id");
		csvModel.addData(csvData);
		
		row++;
		
		for(TransactionDetailsReportPayment payment: viewBean.getPayments())
		{
			csvData = new CSVData(row,0);
			csvData.setValue(payment.getType());
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,1);
			csvData.setValue(EposUtils.formatCurrencyNoCommas(payment.getAmount()));
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,2);
			csvData.setValue(payment.getCreditNoteId()+"");
			csvModel.addData(csvData);
			
			row++;
		}

		/*
		 * discounts
		 */
		
		int discountsRow = row+2;
		
		csvData = new CSVData(discountsRow,0);
		csvData.setValue("Discounts");
		csvModel.addData(csvData);
		
		discountsRow+=2;
		
		csvData = new CSVData(discountsRow,0);
		csvData.setValue("Amount");
		csvModel.addData(csvData);
		
		csvData = new CSVData(discountsRow,1);
		csvData.setValue("Discount Policy");
		csvModel.addData(csvData);
		
		discountsRow++;
		
		for(TransactionDetailsReportDiscount discount : viewBean.getDiscounts())
		{
			csvData = new CSVData(discountsRow,0);
			csvData.setValue(EposUtils.formatCurrencyNoCommas(discount.getAmount()));
			csvModel.addData(csvData);
			
			csvData = new CSVData(discountsRow,1);
			csvData.setValue(discount.getDiscountPolicy());
			csvModel.addData(csvData);
			
			discountsRow++;
		}
		
		/*
		 * Summary
		 */
		
		int summaryRow = row+2;
		
		csvData = new CSVData(summaryRow,3);
		csvData.setValue("Summary");
		csvModel.addData(csvData);
		
		summaryRow+=2;
		
		csvData = new CSVData(summaryRow,3);
		csvData.setValue("Information");
		csvModel.addData(csvData);
		
		csvData = new CSVData(summaryRow,4);
		csvData.setValue("Value");
		csvModel.addData(csvData);
		
		summaryRow++;
		
		csvData = new CSVData(summaryRow,3);
		csvData.setValue("Customer");
		csvModel.addData(csvData);
		
		csvData = new CSVData(summaryRow,4);
		csvData.setValue(viewBean.getCustomer());
		csvModel.addData(csvData);
		
		summaryRow++;
		
		csvData = new CSVData(summaryRow,3);
		csvData.setValue("Transaction Date");
		csvModel.addData(csvData);
		
		csvData = new CSVData(summaryRow,4);
		csvData.setValue(EposUtils.formatDate(viewBean.getTransactionDate()));
		csvModel.addData(csvData);
		
		summaryRow++;
		
		csvData = new CSVData(summaryRow,3);
		csvData.setValue("Total Purchase Value");
		csvModel.addData(csvData);
		
		csvData = new CSVData(summaryRow,4);
		csvData.setValue(EposUtils.formatCurrencyNoCommas(viewBean.getTotalPurchaseValue()));
		csvModel.addData(csvData);
		
		summaryRow++;
		
		csvData = new CSVData(summaryRow,3);
		csvData.setValue("Total Discounts");
		csvModel.addData(csvData);
		
		csvData = new CSVData(summaryRow,4);
		csvData.setValue(EposUtils.formatCurrencyNoCommas(viewBean.getTotalDiscounts()));
		csvModel.addData(csvData);
		
		summaryRow++;
		
		csvData = new CSVData(summaryRow,3);
		csvData.setValue("Subtotal");
		csvModel.addData(csvData);
		
		csvData = new CSVData(summaryRow,4);
		csvData.setValue(EposUtils.formatCurrencyNoCommas(viewBean.getSubtotal()));
		csvModel.addData(csvData);
		
		summaryRow++;
		
		csvData = new CSVData(summaryRow,3);
		csvData.setValue("Total Payments");
		csvModel.addData(csvData);
		
		csvData = new CSVData(summaryRow,4);
		csvData.setValue(EposUtils.formatCurrencyNoCommas(viewBean.getTotalPaid()));
		csvModel.addData(csvData);
		
		/*
		 * create CSV
		 */
		
		csvService.createCSV(csvReq);
		
		view.showMessage("CSV Created: " + file.getPath());
		closeView();
	}

	public void setTransactionDataService(
			TransactionDataService transactionDataService) {
		this.transactionDataService = transactionDataService;
	}

	public void setDiscountDefinitionDataService(
			DiscountDefinitionDataService discountDefinitionDataService) {
		this.discountDefinitionDataService = discountDefinitionDataService;
	}

	public void setCsvService(CSVService csvService) {
		this.csvService = csvService;
	}

}