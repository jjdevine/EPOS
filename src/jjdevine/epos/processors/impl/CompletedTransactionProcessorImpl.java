package jjdevine.epos.processors.impl;

import java.util.Date;

import org.apache.log4j.Logger;

import jjdevine.epos.common.Constants;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.beans.AdHocDiscount;
import jjdevine.epos.common.beans.CustomerInfo;
import jjdevine.epos.common.beans.Discount;
import jjdevine.epos.common.beans.EposPayment;
import jjdevine.epos.common.beans.EposPurchase;
import jjdevine.epos.common.beans.PolicyDiscount;
import jjdevine.epos.common.beans.EposPayment.Type;
import jjdevine.epos.common.persistence.EposCommonDatasource;
import jjdevine.epos.common.persistence.EposCommonDatasourceFactory;
import jjdevine.epos.creditnote.data.svc.CreditNoteDataService;
import jjdevine.epos.discountdefintion.model.beans.CreditNote;
import jjdevine.epos.processors.CompletedTransactionProcessor;
import jjdevine.epos.transaction.EposTransaction;
import jjdevine.epos.transaction.data.svc.TransactionDataService;

public class CompletedTransactionProcessorImpl implements CompletedTransactionProcessor
{
	private CreditNoteDataService creditNoteDataService;
	private TransactionDataService transactionDataService;
	private Logger logger = Logger.getLogger(this.getClass());

	/**
	 * write a completed transaction to the database
	 */
	@Override
	public int processTransaction(EposTransaction transaction) 
	{		
		transaction.getDiscounts();
		/*
		 * check transaction is complete
		 */
		
		if(transaction.getStatus() != EposTransaction.Status.COMPLETE)
		{
			throw new IllegalStateException("Transaction is not complete");
		}
		
		/*
		 * Enter new record in transaction_history
		 */
		
		EposCommonDatasource commonDs = EposCommonDatasourceFactory.createEposDatasource();
		int transactionId = commonDs.getNextAvailableId(Constants.TRANSACTION_HISTORY);
		transaction.setTransactionId(transactionId);
		
		CustomerInfo customer = transaction.getCustomerInfo();
		int customerId = -1; //-1 is default for no customer
		if(customer != null)
		{
			customerId = transaction.getCustomerInfo().getCustomerId();
		}
		
		Date now = new Date();
		
		boolean success = transactionDataService.persistTransaction(transactionId, customerId, now, 
				transaction.getChangeGiven());
		
		if(!success)
		{
			return Constants.PROCESS_ERROR;
		}
		
		/*
		 * Enter new records in transaction_purchase + transaction_payment
		 * 
		 * if items are not void then deduct from stock
		 */
		
		int purchaseId = commonDs.getNextAvailableId(Constants.TRANSACTION_PURCHASE);
		boolean persistFailedFlag = false;
		
		for(EposPurchase purchase :transaction.getPurchases())
		{
			int discountReasonId = -1; //default - no discount reason
			int discountPolicyId = -1;
			int discountDefinitionId = -1;
			
			Discount discount = purchase.getDiscount();
			if(discount != null)
			{
				//TODO: fix this
				if(discount instanceof AdHocDiscount)
				{
					AdHocDiscount adHocDiscount = (AdHocDiscount)discount;
					discountReasonId = adHocDiscount.getDiscountReasonId();
				}
				else if(discount instanceof PolicyDiscount)
				{
					PolicyDiscount policyDiscount = (PolicyDiscount)discount;
					discountPolicyId = policyDiscount.getDiscountPolicyId();
					discountDefinitionId = policyDiscount.getDiscountDefinitionId();
				}
			}
			
			boolean purchaseVoid = (purchase.getMode()==EposPurchase.Mode.PURCHASE_VOID);
			double salePrice = -1;
			
			if(!purchaseVoid)
			{
				salePrice = purchase.getSalePrice();
			}
			
			//write to DB
			success = transactionDataService.persistTransactionPurchase(
					purchaseId++, 
					transactionId, 
					discountReasonId, 
					purchase.getGarment().getSkuId(), 
					salePrice, 
					purchase.getGarment().getRetailPrice(),
					purchaseVoid,
					discountPolicyId,
					discountDefinitionId);
			
			if(!success)
			{
				persistFailedFlag = true;
			}
			
			if(!purchaseVoid)
			{
				try
				{
					success = transactionDataService.deductGarmentFromStock(purchase.getGarment().getSkuId());
				}
				catch(IllegalStateException ex)
				{
					logger.error(ex.getMessage(), ex);
					success = false;
				}
			}
			
			if(!success)
			{
				persistFailedFlag = true;
			}
		}
		
		int paymentId = commonDs.getNextAvailableId(Constants.TRANSACTION_PAYMENT);
		for(EposPayment payment: transaction.getPayments())
		{
			if(!payment.isCancelled())
			{
				success = transactionDataService.persistTransactionPayment(paymentId++, transactionId, payment);
				
				if(payment.getType() == Type.CREDIT_NOTE)
				{
					//need to deduct balance from credit note
					CreditNote creditNote = payment.getCreditNote();
					creditNote.setValueRemaining(creditNote.getValueRemaining() - payment.getAmount());
					creditNoteDataService.updateCreditNote(creditNote);
				}
				
				if(!success)
				{
					persistFailedFlag = true;
				}
			}
		}
		
		/*
		 * persist transaction level discounts
		 */
		
		if(!(transaction.getDiscounts() == null))
		{
			for(Discount discount: transaction.getDiscounts())
			{
				if(!(discount instanceof PolicyDiscount))
				{
					throw new IllegalStateException("Only PolicyDiscounts are supported at transaction level");
				}
				
				PolicyDiscount pDiscount = (PolicyDiscount)discount; 
				
				int transactionDiscountId = transactionDataService.getNextAvailableTransactionDiscountId();
				double discountValue = EposUtils.calculateValueOfTransactionDiscount(transaction, discount);

				transactionDataService.persistTransactionDiscount(transactionDiscountId, transactionId, discountValue, 
						-1, pDiscount.getDiscountPolicyId(), pDiscount.getDiscountDefinitionId()); 
			}
				
		}
		
		
//TODO: sometimes getting db error at end of the transaction - try to isolate
		if(persistFailedFlag)
		{
			return Constants.PROCESS_ERROR;
		}
		else
		{
			return Constants.PROCESS_OK;
		}		
	}

	public CreditNoteDataService getCreditNoteDataService() {
		return creditNoteDataService;
	}

	public void setCreditNoteDataService(CreditNoteDataService creditNoteDataService) {
		this.creditNoteDataService = creditNoteDataService;
	}

	public TransactionDataService getTransactionDataService() {
		return transactionDataService;
	}

	public void setTransactionDataService(
			TransactionDataService transactionDataService) {
		this.transactionDataService = transactionDataService;
	}

}
