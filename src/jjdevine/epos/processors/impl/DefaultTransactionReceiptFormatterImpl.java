package jjdevine.epos.processors.impl;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jjdevine.epos.common.Constants;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.beans.CustomerInfo;
import jjdevine.epos.common.beans.Discount;
import jjdevine.epos.common.beans.EposPayment;
import jjdevine.epos.common.beans.EposPurchase;
import jjdevine.epos.common.beans.GarmentData;
import jjdevine.epos.processors.TransactionReceiptFormatter;
import jjdevine.epos.transaction.EposTransaction;
import jjdevine.printing.beans.PrintingOutputLine;

public class DefaultTransactionReceiptFormatterImpl implements
		TransactionReceiptFormatter {

	@Override
	public List<PrintingOutputLine> convertEposTransactionToPrintingRequest(
			EposTransaction transaction) 
	{
		List<PrintingOutputLine> lines = new ArrayList<PrintingOutputLine>();
		PrintingOutputLine line;
		
		CustomerInfo customerInfo = transaction.getCustomerInfo();
		Font defaultFont = new Font("Courier new", Font.PLAIN, 15);
		Font boldFont = new Font("Courier new", Font.BOLD, 15);
		Font smallFont = new Font("Courier new", Font.PLAIN, 13);
		
		line = new PrintingOutputLine();
		line.setType(PrintingOutputLine.Type.TEXT);
		line.setCentreAlignedText("Date: " + EposUtils.formatDate(new Date()));
		line.setFont(smallFont);
		lines.add(line);
		
		line = new PrintingOutputLine();
		line.setType(PrintingOutputLine.Type.TEXT);
		line.setCentreAlignedText("Tx Ref: " + EposUtils.leftPad(""+transaction.getTransactionId(), 6, '0'));
		line.setFont(smallFont);
		lines.add(line);
		
		if(customerInfo != null)
		{
			line = new PrintingOutputLine();
			line.setType(PrintingOutputLine.Type.TEXT);
			line.setCentreAlignedText("CUSTOMER: " + customerInfo.getCustomerId());
			line.setFont(smallFont);
			lines.add(line);
		}
		
		line = new PrintingOutputLine();
		line.setType(PrintingOutputLine.Type.BLANK);
		lines.add(line);
		
		for(EposPurchase purchase: transaction.getPurchases())
		{
			GarmentData garment = purchase.getGarment();
			
			String brandToDisplay = garment.getBrandAbbr()==null ? garment.getBrand() : garment.getBrandAbbr();
			
			line = new PrintingOutputLine();
			line.setType(PrintingOutputLine.Type.TEXT);
			line.setLeftAlignedText(brandToDisplay.toUpperCase() + " " + garment.getStyle().toUpperCase());
			line.setFont(defaultFont);
			lines.add(line);
			
			switch(purchase.getMode())
			{
				case NORMAL:
					line.setRightAlignedText(EposUtils.formatCurrency(purchase.getSalePrice()));
					break;
				case NORMAL_DISCOUNTED:
					line.setRightAlignedText("WAS " + EposUtils.formatCurrency(garment.getRetailPrice()));
					line = new PrintingOutputLine();
					line.setType(PrintingOutputLine.Type.TEXT);
					line.setFont(boldFont);
					line.setRightAlignedText("NOW " + EposUtils.formatCurrency(purchase.getSalePrice()));
					lines.add(line);
					break;
				case PURCHASE_VOID:
					line.setRightAlignedText("VOID");
					line.setStrikeThroughText(true);
					break;
				default:
					throw new RuntimeException(purchase.getMode() + " is not supported");
			}
		}
		
		//add blank line if any discounts applied
		if(transaction.getDiscounts() != null && transaction.getDiscounts().size() > 0)
		{
			line = new PrintingOutputLine();
			line.setType(PrintingOutputLine.Type.BLANK);
			lines.add(line);
		}
		
		for(Discount discount: transaction.getDiscounts())
		{
			line = new PrintingOutputLine();
			line.setType(PrintingOutputLine.Type.TEXT);
			line.setLeftAlignedText("DISCOUNT :");
			line.setRightAlignedText("-" + EposUtils.formatCurrency(
					EposUtils.calculateValueOfTransactionDiscount(transaction, discount)));
			line.setFont(defaultFont);
			lines.add(line);
		}
		
		/*
		 * Subtotal section
		 */
		
		line = new PrintingOutputLine();
		line.setType(PrintingOutputLine.Type.BLANK);
		lines.add(line);
		
		line = new PrintingOutputLine();
		line.setType(PrintingOutputLine.Type.RIGHT_HALF_DIVIDER);
		lines.add(line);
		
		line = new PrintingOutputLine();
		line.setType(PrintingOutputLine.Type.TEXT);
		line.setFont(boldFont);
		line.setLeftAlignedText("SUBTOTAL");
		line.setRightAlignedText(EposUtils.formatCurrency(
				EposUtils.calculatePurchaseSubtotal(transaction)-EposUtils.calculateValueOfTransactionDiscounts(transaction)));
		lines.add(line);
		
		line = new PrintingOutputLine();
		line.setType(PrintingOutputLine.Type.BLANK);
		lines.add(line);
		
		line = new PrintingOutputLine();
		line.setType(PrintingOutputLine.Type.TEXT);
		line.setFont(boldFont);
		line.setLeftAlignedText("PAYMENTS");
		lines.add(line);

		for(EposPayment payment: transaction.getPayments())
		{
			line = new PrintingOutputLine();
			line.setType(PrintingOutputLine.Type.TEXT);
			line.setFont(defaultFont);
			lines.add(line);
			
			switch(payment.getType())
			{
				case CARD:					
				case CASH:
				case CHEQUE:
				case COUPON:
					if(!payment.isCancelled())
					{
						line.setRightAlignedText(payment.getType() + " " + EposUtils.formatCurrency(payment.getAmount()));
					}
					else
					{
						line.setRightAlignedText(payment.getType() + " £0"); 
						line.setStrikeThroughText(true);
					}
					
					break;
				case RETURNED_ITEM:
					if(!payment.isCancelled())
					{
						line.setRightAlignedText("RETURNED ITEM " + EposUtils.formatCurrency(payment.getAmount()));
					}
					else
					{
						line.setRightAlignedText("RETURNED ITEM £0");
						line.setStrikeThroughText(true);
					}
					break;
				case CREDIT_NOTE:
					if(!payment.isCancelled())
					{
						if(payment.getCreditNote().getReasonForIssue().getId() == Constants.CREDIT_NOTE_GIFT_VOUCHER_ID)
						{
							line.setRightAlignedText("GIFT VOUCHER " + EposUtils.formatCurrency(payment.getAmount()));
						}
						else
						{
							line.setRightAlignedText("CREDIT NOTE " + EposUtils.formatCurrency(payment.getAmount()));
						}
					}
					else
					{
						if(payment.getCreditNote().getReasonForIssue().getId() == Constants.CREDIT_NOTE_GIFT_VOUCHER_ID)
						{
							line.setRightAlignedText("GIFT VOUCHER £0");
							line.setStrikeThroughText(true);
						}
						else
						{
							line.setRightAlignedText("CREDIT NOTE £0");
							line.setStrikeThroughText(true);
						}
					}
					break;
				default:
					throw new RuntimeException(payment.getType() + " is not supported");
			}
		}
		
		line = new PrintingOutputLine();
		line.setType(PrintingOutputLine.Type.BLANK);
		lines.add(line);
		
		line = new PrintingOutputLine();
		line.setType(PrintingOutputLine.Type.TEXT);
		line.setFont(boldFont);
		line.setLeftAlignedText("CHANGE DUE");
		line.setRightAlignedText(EposUtils.formatCurrency(Math.abs(EposUtils.calculateSubtotal(transaction))));
		lines.add(line);
		
		line = new PrintingOutputLine();
		line.setType(PrintingOutputLine.Type.BLANK);
		lines.add(line);
		
		return lines;
	}

}
