package jjdevine.epos.utils;

import java.util.ArrayList;
import java.util.List;

import jjdevine.epos.beans.DiscountViewBean;
import jjdevine.epos.beans.EposCreditNotePaymentViewBean;
import jjdevine.epos.beans.EposPaymentReturnedItemViewBean;
import jjdevine.epos.beans.EposPaymentViewBean;
import jjdevine.epos.beans.EposPurchaseViewBean;
import jjdevine.epos.beans.TransactionItemViewBean;
import jjdevine.epos.beans.TransactionViewBean;
import jjdevine.epos.common.Constants;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.beans.Discount;
import jjdevine.epos.common.beans.EposPayment;
import jjdevine.epos.common.beans.EposPurchase;
import jjdevine.epos.common.beans.PolicyDiscount;
import jjdevine.epos.common.beans.TransactionItem;
import jjdevine.epos.transaction.EposTransaction;

public class ViewBeanMapper 
{
	public static TransactionViewBean convertTransactionToViewBean(EposTransaction transaction)
	{
		TransactionViewBean transactionViewBean = new TransactionViewBean();
		
		List<TransactionItemViewBean> transactionItemViewBeans = new ArrayList<TransactionItemViewBean>();
		transactionViewBean.setTransactionItemViewBeans(transactionItemViewBeans);
		
		/*
		 * first convert transaction items to view beans
		 */
		for(TransactionItem transactionItem: transaction.getTransactionItems())
		{
			if(transactionItem instanceof EposPurchase)
			{
				EposPurchase purchase = (EposPurchase)transactionItem;
				EposPurchaseViewBean purchaseViewBean = new EposPurchaseViewBean();

				switch(purchase.getMode())
				{
					case NORMAL:
						purchaseViewBean.setMode(EposPurchaseViewBean.Mode.NORMAL);
						break;
					case NORMAL_DISCOUNTED:
						purchaseViewBean.setMode(EposPurchaseViewBean.Mode.NORMAL_DISCOUNTED);
						break;
					case MEMBER:
						purchaseViewBean.setMode(EposPurchaseViewBean.Mode.MEMBER);
						break;
					case MEMBER_DISCOUNTED:
						purchaseViewBean.setMode(EposPurchaseViewBean.Mode.MEMBER_DISCOUNTED);
						break;
					case PURCHASE_VOID:
						purchaseViewBean.setMode(EposPurchaseViewBean.Mode.PURCHASE_VOID);
						break;
				}
				
				purchaseViewBean.setBrand(purchase.getGarment().getBrandAbbr());
				purchaseViewBean.setColour(purchase.getGarment().getColourDesc());
				purchaseViewBean.setGarmentCode(purchase.getGarment().getGarmentCode());
				purchaseViewBean.setSize(purchase.getGarment().getSize1());
				purchaseViewBean.setStyle(purchase.getGarment().getStyle());
				
				purchaseViewBean.setRetailPrice(purchase.getGarment().getRetailPrice());
				purchaseViewBean.setMemberPrice(purchase.getGarment().getMemberPrice());
				
				Discount discount = purchase.getDiscount();
				
				if(discount != null)
				{
					purchaseViewBean.setMessage(discount.getDescription());
				}
				
				if(purchase.getMode() != EposPurchase.Mode.PURCHASE_VOID)
				{
					purchaseViewBean.setSalePrice(purchase.getSalePrice());
				}
				
				purchaseViewBean.setTransactionIndex(purchase.getTransactionIndex());
				
				transactionItemViewBeans.add(purchaseViewBean);
			}
			else if(transactionItem instanceof EposPayment)
			{
				EposPayment payment = (EposPayment)transactionItem;
				EposPaymentViewBean paymentViewBean = null;
				
				switch(payment.getType())
				{
					case CARD:
						paymentViewBean = new EposPaymentViewBean();
						paymentViewBean.setType(EposPaymentViewBean.Type.CARD);
						break;
					case CASH:
						paymentViewBean = new EposPaymentViewBean();
						paymentViewBean.setType(EposPaymentViewBean.Type.CASH);
						break;
					case COUPON:
						paymentViewBean = new EposPaymentViewBean();
						paymentViewBean.setType(EposPaymentViewBean.Type.COUPON);
						break;
					case CHEQUE:
						paymentViewBean = new EposPaymentViewBean();
						paymentViewBean.setType(EposPaymentViewBean.Type.CHEQUE);
						break;
					case RETURNED_ITEM:
						EposPaymentReturnedItemViewBean returnedItemViewBean = new EposPaymentReturnedItemViewBean();
						returnedItemViewBean.setGarmentCode(payment.getGarmentData().getGarmentCode());
						returnedItemViewBean.setBrand(payment.getGarmentData().getBrandAbbr());
						returnedItemViewBean.setColour(payment.getGarmentData().getColourDesc());
						returnedItemViewBean.setSize(payment.getGarmentData().getSize1());
						returnedItemViewBean.setStyle(payment.getGarmentData().getStyle());
						
						paymentViewBean = returnedItemViewBean;
						paymentViewBean.setType(EposPaymentViewBean.Type.RETURNED_ITEM);
						break;
					case CREDIT_NOTE:
						EposCreditNotePaymentViewBean creditNotePaymentViewBean = new EposCreditNotePaymentViewBean();
						creditNotePaymentViewBean.setReasonForIssue(payment.getCreditNote().getReasonForIssue().getDescription());
						creditNotePaymentViewBean.setGiftVoucher(
								payment.getCreditNote().getReasonForIssue().getId() == Constants.CREDIT_NOTE_GIFT_VOUCHER_ID ? true : false);
						
						paymentViewBean = creditNotePaymentViewBean;
						paymentViewBean.setType(EposPaymentViewBean.Type.CREDIT_NOTE);
						break;
				}
				
				paymentViewBean.setCancelled(payment.isCancelled());
				paymentViewBean.setValue(payment.getAmount());
				paymentViewBean.setTransactionIndex(payment.getTransactionIndex());
				
				transactionItemViewBeans.add(paymentViewBean);
			}
		}
		
		/*
		 * process discounts
		 */
		
		List<DiscountViewBean> discountViewBeans = new ArrayList<DiscountViewBean>();
		transactionViewBean.setDiscountViewBeans(discountViewBeans);
		
		for(Discount discount: transaction.getDiscounts())
		{
			DiscountViewBean discountViewBean = new DiscountViewBean();
			
			discountViewBean.setDescription(discount.getDescription());
			discountViewBean.setValue(EposUtils.calculateValueOfTransactionDiscount(transaction, discount));
			
			if(discount instanceof PolicyDiscount)
			{
				PolicyDiscount policyDiscount = (PolicyDiscount)discount;
				discountViewBean.setDiscountId(policyDiscount.getDiscountDefinitionId());
				discountViewBean.setDiscountPolicyId(policyDiscount.getDiscountPolicyId());
			}
			
			discountViewBeans.add(discountViewBean);
		}
		
		/*
		 * calculate subtotals
		 */
		
		transactionViewBean.setItemsInTransaction(transaction.getActivePurchasesCount());
		transactionViewBean.setSubtotal(EposUtils.calculateSubtotal(transaction));
		
		return transactionViewBean;
	}
}
