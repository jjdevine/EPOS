package jjdevine.epos.controllers;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import jjdevine.epos.EposContext;
import jjdevine.epos.beans.ActiveDiscountPolicies;
import jjdevine.epos.beans.TransactionViewBean;
import jjdevine.epos.common.Constants;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.beans.AdHocDiscount;
import jjdevine.epos.common.beans.Discount;
import jjdevine.epos.common.beans.EposPayment;
import jjdevine.epos.common.beans.EposPurchase;
import jjdevine.epos.common.beans.GarmentData;
import jjdevine.epos.common.beans.TransactionItem;
import jjdevine.epos.common.beans.EposPurchase.Mode;
import jjdevine.epos.common.persistence.EposCommonDatasource;
import jjdevine.epos.common.persistence.EposCommonDatasourceFactory;
import jjdevine.epos.creditnote.data.svc.CreditNoteDataService;
import jjdevine.epos.discountdefintion.model.beans.CreditNote;
import jjdevine.epos.discountdefintion.model.beans.DiscountPolicy;
import jjdevine.epos.events.ModifyPurchasesEvent;
import jjdevine.epos.events.ModifyPurchasesEventListener;
import jjdevine.epos.events.TransactionEvent;
import jjdevine.epos.gui.EposTransactionGui;
import jjdevine.epos.guiFactory.EposGuiFactory;
import jjdevine.epos.guiImpl.EposManagementMenuGui;
import jjdevine.epos.guiImpl.ModifyPurchasesMenu;
import jjdevine.epos.listeners.TransactionEventListener;
import jjdevine.epos.processors.CompletedTransactionProcessor;
import jjdevine.epos.processors.ReceiptPrintingProcessor;
import jjdevine.epos.services.authentication.AuthenticationService;
import jjdevine.epos.services.authentication.events.AuthenticationResultEvent;
import jjdevine.epos.services.authentication.listeners.AuthenticationServiceListener;
import jjdevine.epos.services.processpayment.PaymentService;
import jjdevine.epos.services.processpayment.events.PaymentEvent;
import jjdevine.epos.services.processpayment.events.PaymentServiceListener;
import jjdevine.epos.services.searchskucode.SearchSKUCodeService;
import jjdevine.epos.services.searchskucode.events.SearchListener;
import jjdevine.epos.services.searchskucode.events.SearchResultEvent;
import jjdevine.epos.transaction.EposTransaction;
import jjdevine.epos.transaction.EposTransactionFactory;
import jjdevine.epos.usermanagement.data.svc.beans.User;
import jjdevine.epos.usermanagement.data.svc.beans.User.AccessLevel;
import jjdevine.epos.utils.DiscountUtils;
import jjdevine.epos.utils.UserUtils;
import jjdevine.epos.utils.Utils;
import jjdevine.epos.utils.ViewBeanMapper;

import org.apache.log4j.Logger;

public class TransactionController implements AuthenticationServiceListener, TransactionEventListener, SearchListener, 
	ModifyPurchasesEventListener, PaymentServiceListener 
{
	private AuthenticationService authSvc;
	private EposTransactionGui eposTransactionGui;
	private EposTransaction transaction;
	//TODO: use spring to inject instances of these objects
	private CompletedTransactionProcessor completedTransactionProcessor;
	private ReceiptPrintingProcessor receiptPrintingProcessor;
	private ActiveDiscountPolicies activeDiscountPolicies;
	private CreditNoteDataService creditNoteDataService;
	private EposCommonDatasource commonDatasource;
	private User loggedInUser;
	private Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * used to track the index of the transaction items
	 */
	private int transactionIndex = 0;
	
	private static final String RETURNED_ITEM_KEY = "returns";
	
	public void init()
	{
		authSvc = AuthenticationService.getInstance();
		authSvc.setUserManagementDataService(EposContext.getUserManagementDataService());
		authSvc.addListener(this);
		authenticateUser();
		completedTransactionProcessor = EposContext.getCompletedTransactionProcessor();
		receiptPrintingProcessor = EposContext.getReciptPrintingProcessor();
		activeDiscountPolicies = EposContext.getActiveDiscountPolicies();
		creditNoteDataService = EposContext.getCreditNoteDataService();
		commonDatasource = EposCommonDatasourceFactory.createEposDatasource();
	}
	
	public void authenticateUser()
	{
		authSvc.authenticate();
	}

	@Override
	public void onAuthenticationResultEvent(AuthenticationResultEvent evt) 
	{
		switch (evt.getType()) 
		{	
			case SUCCESS_USER:
			case SUCCESS_ADMIN:	
				loggedInUser = evt.getUser();
				startTransactionScreen();			
				break;
			case FAIL:	
				JOptionPane.showMessageDialog(null, "Invalid logon details, please try again.", "Invalid logon", JOptionPane.ERROR_MESSAGE);
				break;
			case USER_DISABLED:
				JOptionPane.showMessageDialog(null, "This user account has been disabled", "Inactive User", JOptionPane.ERROR_MESSAGE);
				break;
			case CANCEL:
				System.exit(0);
				break;
		}
	}
	private void startTransactionScreen()
	{
		eposTransactionGui = EposGuiFactory.createEposTransactionGui();
		eposTransactionGui.addListener(this);
		
		String firstName = loggedInUser.getFirstName();
		eposTransactionGui.setUserDisplay((firstName==null || firstName.length()==0) ? loggedInUser.getUsername() : firstName);
		
		resetTransaction();
	}

	@Override
	public void onTransactionEvent(TransactionEvent evt) 
	{	
		SearchSKUCodeService searchSvc = null;
		switch (evt.getType()) 
		{
			case SELECT_ITEM:
				searchSvc = SearchSKUCodeService.getInstance();
				searchSvc.addListener(this);
				searchSvc.performSearchOnSaleOnly();
				break;
				
			case PAYMENT:
				processPayment(evt);
				break;
				
			case CREDIT_NOTE:
				String creditNoteIdStr = JOptionPane.showInputDialog((Component)eposTransactionGui, 
						"Please enter the reference number of the Credit Note or Gift Voucher:");
				
				if(creditNoteIdStr == null)
				{
					return;
				}
				
				int creditNoteId = -1;
				
				try
				{
					creditNoteId = Integer.parseInt(creditNoteIdStr);
				}
				catch(NumberFormatException nfe)
				{
					eposTransactionGui.showError(creditNoteIdStr + " is not a valid reference number");
					return;
				}
				
				CreditNote creditNote = creditNoteDataService.findCreditNote(creditNoteId);
				
				if(creditNote == null)
				{
					eposTransactionGui.showError(creditNoteIdStr + " - no credit note with this reference number found");
					return;
				}
				
				if(creditNote.getValueRemaining() == 0)
				{
					eposTransactionGui.showError(creditNoteIdStr + " - this credit note has been used up already");
					return;
				}
				
				if(Utils.creditNoteExistsInTransaction(creditNoteId, transaction))
				{
					eposTransactionGui.showError(creditNoteIdStr + " - this credit note is already in use in this transaction");
					return;
				}
				
				EposPayment creditNotePayment = new EposPayment();
				
				creditNotePayment.setType(EposPayment.Type.CREDIT_NOTE);
				creditNotePayment.setTransactionIndex(transactionIndex++);
				creditNotePayment.setCreditNote(creditNote);
				
				double subtotal = EposUtils.calculateSubtotal(transaction);
				
				if(subtotal > creditNote.getValueRemaining())
				{
					creditNotePayment.setAmount(creditNote.getValueRemaining());
				}
				else
				{
					creditNotePayment.setAmount(subtotal);
				}
				
				PaymentEvent creditNoteEvt = new PaymentEvent();
				creditNoteEvt.setPayment(creditNotePayment);
				
				onPaymentEvent(creditNoteEvt);
				break;
				
			case RETURN_ITEM:
				searchSvc = EposContext.getSearchSKUService();
				searchSvc.addListener(this);
				searchSvc.setKey(TransactionController.RETURNED_ITEM_KEY);
				searchSvc.performSearch(false);
				break;
				
			case MANAGEMENT_MENU:
				if(loggedInUser.getAccessLevel() != AccessLevel.ADMINISTRATOR)
				{
					String[] userLogin = UserUtils.quickAuthenticate((Component)eposTransactionGui);
					int result = -1;
					
					try 
					{
						result = commonDatasource.attemptLogon(userLogin[0], userLogin[1]);
					} 
					catch (Exception ex)
					{
						logger.error(ex.getMessage(), ex);
						eposTransactionGui.showError("An Error Occurred. Please check the logs.");
						return;
					}
					
					if(result != Constants.LOGON_SUCCESS_ADMIN)
					{
						eposTransactionGui.showError("You are not authorised to use this facility.");
						return;
					}
				}
				new EposManagementMenuGui();
				break;
				
			case MODIFY_ITEMS:
				if(evt.getTransactionIndexArray() != null && evt.getTransactionIndexArray().length>0)
				{
					ModifyPurchasesMenu menu = EposGuiFactory.createModifyPurchasesMenu(evt.getTransactionIndexArray());
					menu.addListener(this);
				}
				else
				{
					JOptionPane.showMessageDialog((Component)eposTransactionGui, 
							"You must select at least one item!", "Invalid Operation", 
							JOptionPane.ERROR_MESSAGE);
				}
				

				break;
				
			case CANCEL_PAYMENT:
				EposPayment payment = (EposPayment)transaction.getTransactionItemByIndex(evt.getTransactionIndex());
				
				if(payment != null)
				{
					payment.setCancelled(true);
					updateGui();
				}

				//TODO log warning if id not matched?
				break;
				
			case LOG_OUT:
				int result = JOptionPane.showConfirmDialog((Component)eposTransactionGui, "Are you sure you want to Logout?", "Logout", JOptionPane.OK_CANCEL_OPTION);
				
				if(result == JOptionPane.OK_OPTION)
				{
					eposTransactionGui.close();
					Utils.closeWindows();
					authenticateUser();
				}
				break;
		}
	}

	private void processPayment(TransactionEvent evt) 
	{
		if(EposUtils.calculateSubtotal(transaction) <= 0)
		{
			JOptionPane.showMessageDialog((Component)eposTransactionGui, 
					"" + "Subtotal must be greater than zero to process a payment", 
					"Invalid Operation", 
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		PaymentService svc = null;
		switch(evt.getPaymentMethod())
		{
			case CARD:
				svc = PaymentService.getCardInstance();
				svc.addListener(this);
				svc.processPayment();
				break;
			case CASH:
				svc = PaymentService.getCashInstance();
				svc.addListener(this);
				svc.processPayment();
				break;
			case CHEQUE:
				svc = PaymentService.getChequeInstance();
				svc.addListener(this);
				svc.processPayment();
				break;
			case COUPON:
				svc = PaymentService.getCouponInstance();
				svc.addListener(this);
				svc.processPayment();
				break;
		}	
	}

	@Override
	public void onSearchResultEvent(SearchResultEvent evt) 
	{
		switch(evt.getEventType())
		{
			case GARMENT:
				if(TransactionController.RETURNED_ITEM_KEY.equals(evt.getKey()))
				{
					handleReturn(evt.getGarmentData());
				}
				else   //new garment added to transaction
				{
					EposPurchase newPurchase = new EposPurchase();
					newPurchase.setTransactionIndex(transactionIndex++);
					newPurchase.setGarment(evt.getGarmentData());
					transaction.setStatus(EposTransaction.Status.IN_PROGRESS);
					newPurchase.setMode(EposPurchase.Mode.NORMAL);	//TODO: make this dynamic for members
					
					transaction.getTransactionItems().add(newPurchase);
					applyDiscountsForGarment(newPurchase);
					
					updateGui();
				}
				break;
		}	
	}

	private void handleReturn(GarmentData garmentData) 
	{
		EposPayment returnedItem = new EposPayment();
		returnedItem.setTransactionIndex(transactionIndex++);
		returnedItem.setGarmentData(garmentData);
		
		double returnValue = -1;
		
		while(returnValue == -1)
		{
		
			String returnValueStr = JOptionPane.showInputDialog((Component)eposTransactionGui, "Please confirm value of returned item " +
					"(current retail price is " + EposUtils.formatCurrency(garmentData.getRetailPrice()) + "):", garmentData.getRetailPrice());
			
			if(returnValueStr == null)
			{
				eposTransactionGui.showError("You must enter a value!");
				continue;
			}
			
			try
			{
				returnValue = Double.parseDouble(returnValueStr);
			}
			catch(NumberFormatException nfe)
			{
				eposTransactionGui.showError(returnValueStr + " is not a valid price!");
			}
			
			returnedItem.setAmount(returnValue);
		}
		
		returnedItem.setType(EposPayment.Type.RETURNED_ITEM);
		
		PaymentEvent evt = new PaymentEvent();
		evt.setPayment(returnedItem);
		
		onPaymentEvent(evt);
	}

	@Override
	public void onModifyPurchasesEvent(ModifyPurchasesEvent evt) 
	{
		switch(evt.getType())
		{
			case VOID:
				for(int index: evt.getAffectedPurchases())
				{
					TransactionItem transactionItem = transaction.getTransactionItemByIndex(index);
					
					if(transactionItem instanceof EposPurchase)
					{
						EposPurchase purchase = (EposPurchase)transactionItem;
						purchase.setMode(Mode.PURCHASE_VOID);
					}
													
				}
				updateGui();
				break;
			case DISCOUNT:
				for(int index: evt.getAffectedPurchases())
				{
					TransactionItem transactionItem = transaction.getTransactionItemByIndex(index);
					
					EposPurchase purchase = null;
					if(transactionItem instanceof EposPurchase)
					{
						purchase = (EposPurchase)transactionItem;
					}
					else
					{
						throw new IllegalStateException("Can only apply a discount to a purchase");
					}
					
					if(evt.getDiscount() instanceof AdHocDiscount)
					{
						AdHocDiscount adHocDiscount = (AdHocDiscount) evt.getDiscount();
						
						/*
						 * need to check if this new discount reduces the price further than any existing discount
						 */
						
						Discount existingDiscount = purchase.getDiscount();
						double existingPrice = purchase.getSalePrice();
						Mode existingMode = purchase.getMode();
						
						//now test with new discount
						purchase.setDiscount(adHocDiscount);
						purchase.setMode(EposPurchase.Mode.NORMAL_DISCOUNTED);
						double newPrice = purchase.getSalePrice();
						
						if(newPrice > existingPrice)
						{
							//TODO: show error messase
							//new discount does not reduce the price, so do not use it
							purchase.setDiscount(existingDiscount);
							purchase.setMode(existingMode);
						}
					}								
				}
				updateGui();
				break;
		}		
		eposTransactionGui.resetSelectedItems();
	}
	
	@Override
	public void onPaymentEvent(PaymentEvent evt) 
	{
		EposPayment payment = evt.getPayment();
		payment.setTransactionIndex(transactionIndex++);
		transaction.getTransactionItems().add(payment);
		updateGui();
		
		double subtotal = EposUtils.calculateSubtotal(transaction);
		if(subtotal <= 0)
		{
			finishTransaction();
		}
	}
	
	private void finishTransaction()
	{
		double subtotal = EposUtils.calculateSubtotal(transaction);
		
		//transaction is complete
		double change = Math.abs(subtotal);
		
		JOptionPane.showMessageDialog((Component)eposTransactionGui, 
				"Change due : " + EposUtils.formatCurrency(change), 
				"Transaction Complete", 
				JOptionPane.INFORMATION_MESSAGE);
		
		transaction.setChangeGiven(change);

		//process transaction and start new transaction for next customer
		transaction.setStatus(EposTransaction.Status.COMPLETE);
		int result = completedTransactionProcessor.processTransaction(transaction);
		if(result != Constants.PROCESS_OK)
		{
			JOptionPane.showMessageDialog((Component)eposTransactionGui, 
					"Error writing transaction details to database, please check the logs", 
					"Database Error", 
					JOptionPane.ERROR_MESSAGE);
		}
		receiptPrintingProcessor.printReceipt(transaction);
		
		resetTransaction();
	}
	
	/**
	 * starts a new transaction and refreshes the display to the user.
	 */
	private void resetTransaction() 
	{
		transaction = EposTransactionFactory.createNewTransaction();
		updateDiscountPolicies();
		updateGui();	
	}
	
	public void updateDiscountPolicies()
	{
		activeDiscountPolicies.refresh();
	}
	
	private void updateGui()
	{
		updateDiscountsForTransaction();
		TransactionViewBean viewBean = ViewBeanMapper.convertTransactionToViewBean(transaction);
		eposTransactionGui.renderTransaction(viewBean);
	}
	
	private void applyDiscountsForGarment(EposPurchase eposPurchase)
	{
		List<Discount> applicableDiscounts = new ArrayList<Discount>();
		Discount discount = null;
		
		for(DiscountPolicy discountPolicy: activeDiscountPolicies.getDiscountPolicies())
		{
			discount = DiscountUtils.getDiscountForPurchase(eposPurchase, discountPolicy);
			
			if(discount != null)
			{
				applicableDiscounts.add(discount);
			}
		}
		
		discount = DiscountUtils.getBestDiscount(eposPurchase, applicableDiscounts);
		eposPurchase.setDiscount(discount);
		
		if(discount != null)
		{
			if(eposPurchase.getMode() == EposPurchase.Mode.NORMAL)
			{
				eposPurchase.setMode(EposPurchase.Mode.NORMAL_DISCOUNTED);
			}
			else if(eposPurchase.getMode() == EposPurchase.Mode.MEMBER)
			{
				eposPurchase.setMode(EposPurchase.Mode.MEMBER_DISCOUNTED);
			}
		}
		
		//TODO: test different types of discount
	}
	
	private void updateDiscountsForTransaction()
	{
		//clear any currently applied discounts
		transaction.getDiscounts().clear();
		
		List<Discount> applicableDiscounts = new ArrayList<Discount>();
		Discount discount = null;
		
		for(DiscountPolicy discountPolicy: activeDiscountPolicies.getDiscountPolicies())
		{
			discount = DiscountUtils.getDiscountForTransaction(transaction, discountPolicy);
			
			if(discount != null)
			{
				applicableDiscounts.add(discount);
			}
		}
		
		discount = DiscountUtils.getBestDiscount(transaction, applicableDiscounts);
		
		if(discount != null)
		{
			transaction.getDiscounts().clear();
			transaction.getDiscounts().add(discount);
		}
		
	}

}
