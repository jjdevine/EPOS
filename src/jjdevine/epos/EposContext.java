package jjdevine.epos;

import java.util.List;

import jjdevine.epos.beans.ActiveDiscountPolicies;
import jjdevine.epos.beans.Preferences;
import jjdevine.epos.common.EposResources;
import jjdevine.epos.controllers.TransactionController;
import jjdevine.epos.creditnote.data.svc.CreditNoteDataService;
import jjdevine.epos.creditnote.view.gui.CreditNoteView;
import jjdevine.epos.customerinfo.data.svc.CustomerDataService;
import jjdevine.epos.customerinfo.view.gui.CustomerInfoView;
import jjdevine.epos.customersearch.view.CustomerSearchView;
import jjdevine.epos.discountdefinition.view.gui.DiscountDefinitionView;
import jjdevine.epos.discountdefinition.view.gui.DiscountPolicyDefinitionView;
import jjdevine.epos.discountdefintion.data.svc.DiscountDefinitionDataService;
import jjdevine.epos.guiImpl.ReportsSelectionMenu;
import jjdevine.epos.mediators.CustomerSearchMediator;
import jjdevine.epos.preferences.data.svc.PreferencesDataService;
import jjdevine.epos.preferences.view.gui.PreferencesView;
import jjdevine.epos.processors.AddCustomerProcessor;
import jjdevine.epos.processors.CompletedTransactionProcessor;
import jjdevine.epos.processors.CreateDiscountDefinitionProcessor;
import jjdevine.epos.processors.CreateDiscountPolicyProcessor;
import jjdevine.epos.processors.CreateReceiptSetupProcessor;
import jjdevine.epos.processors.CreateUserProcessor;
import jjdevine.epos.processors.EditCustomerProcessor;
import jjdevine.epos.processors.EditPreferencesProcessor;
import jjdevine.epos.processors.EditUserProcessor;
import jjdevine.epos.processors.IssueCreditNoteProcessor;
import jjdevine.epos.processors.ReceiptPrintingProcessor;
import jjdevine.epos.processors.StockAdjustmentProcessor;
import jjdevine.epos.processors.TransactionReceiptFormatter;
import jjdevine.epos.receiptsetup.data.svc.ReceiptSetupDataService;
import jjdevine.epos.receiptsetup.view.beans.ReceiptSetup;
import jjdevine.epos.receiptsetup.view.gui.ReceiptSetupView;
import jjdevine.epos.receiptsetup.view.gui.ReceiptSetupViewImpl;
import jjdevine.epos.reports.endofday.view.gui.EndOfDayReportView;
import jjdevine.epos.reports.genericreporttemplate1.view.gui.GenericReportTemplateView1;
import jjdevine.epos.reports.pricehistory.view.gui.PriceHistoryReportView;
import jjdevine.epos.reports.transactiondetails.view.gui.TransactionDetailsReportView;
import jjdevine.epos.services.searchskucode.SearchSKUCodeService;
import jjdevine.epos.transaction.data.svc.TransactionDataService;
import jjdevine.epos.usermanagement.data.svc.UserManagementDataService;
import jjdevine.epos.usermanagement.view.gui.UserManagementView;
import jjdevine.epos.view.stockadjustment.gui.StockAdjustmentView;
import jjdevine.printing.PrintPreviewService;
import jjdevine.windowregistry.WindowManagementService;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class EposContext 
{
	private static ApplicationContext context;
	private static Preferences preferences;
	
	static
	{
		context = new ClassPathXmlApplicationContext("EposContext.xml");
	}
	
	/**
	 * get the epos application context
	 * @return
	 */
	public static ApplicationContext getContext()
	{
		return context;
	}
	
	public static AddCustomerProcessor getAddCustomerProcessor()
	{
		return (AddCustomerProcessor)context.getBean("addCustomerProcessor");
	}
	
	public static EditCustomerProcessor getEditCustomerProcessor() 
	{
		return (EditCustomerProcessor)context.getBean("editCustomerProcessor");	
	}
	
	public static CustomerInfoView getCustomerInfoView()
	{
		return (CustomerInfoView)context.getBean("customerInfoView");
	}
	
	public static CustomerDataService getCustomerDataService()
	{
		return (CustomerDataService)context.getBean("customerDao");
	}
	
	public static SearchSKUCodeService getSearchSKUService()
	{
		return (SearchSKUCodeService)context.getBean("searchSKUCodeService");
	}
	
	public static WindowManagementService getWindowManagementService()
	{
		return EposResources.getWindowManagementService();
	}
	
	public static ReceiptSetupView getReceiptSetupView()
	{
		return (ReceiptSetupView) context.getBean("receiptSetupView");
	}
	
	public static ReceiptSetupView getReceiptSetupView(ReceiptSetup setupViewBean) 
	{
		// TODO is there a Spring way of doing this?
		return new ReceiptSetupViewImpl(setupViewBean);
	}
	
	public static PreferencesDataService getPreferencesDataService()
	{
		return (PreferencesDataService) context.getBean("preferencesDao");
	}
	
	public static ReceiptSetupDataService getReceiptSetupDataService()
	{
		return (ReceiptSetupDataService) context.getBean("receiptDao");
	}
	
	public static DiscountDefinitionDataService getDiscountDataService()
	{
		return (DiscountDefinitionDataService) context.getBean("discountDao");
	}
	
	public static Preferences getPreferences()
	{
		if (preferences == null)
		{
			preferences = new Preferences();
			preferences.setPreferencesDataService(getPreferencesDataService());
			preferences.setReceiptSetupDataService(getReceiptSetupDataService());
			preferences.refresh();
		}
		
		//TODO: implement asynchronous paging pattern for preferences bean
		return preferences;
	}
	
	public static PrintPreviewService getPrintPreviewService()
	{
		return (PrintPreviewService) context.getBean("printPreviewService");
	}
	
	public static CreateReceiptSetupProcessor getCreateReceiptSetupProcessor()
	{
		return (CreateReceiptSetupProcessor) context.getBean("createReceiptSetupProcessor");
	}
	
	public static ReceiptPrintingProcessor getReciptPrintingProcessor() 
	{
		return (ReceiptPrintingProcessor) context.getBean("receiptPrintingProcessor");
	}
	

	public static CompletedTransactionProcessor getCompletedTransactionProcessor() 
	{	
		return (CompletedTransactionProcessor)context.getBean("completedTransactionProcessor");
	}
	
	@SuppressWarnings("unchecked")
	public static TransactionReceiptFormatter getTransactionReceiptFormatter(int id)
	{
		List list = (List) context.getBean("transactionReceiptFormatters");

		//the index in the list must always be the ID minus 1
		
		return (TransactionReceiptFormatter)list.get(id-1);
	}
	
	public static CreateDiscountDefinitionProcessor getCreateDiscountDefinitionProcessor()
	{
		return (CreateDiscountDefinitionProcessor)context.getBean("createDiscountDefinitionProcessor");
	}
	
	public static DiscountDefinitionView getDiscountDefinitionView()
	{
		return (DiscountDefinitionView)context.getBean("discountDefinitionView");
	}
	
	public static CreateDiscountPolicyProcessor getCreateDiscountPolicyProcessor()
	{
		return (CreateDiscountPolicyProcessor)context.getBean("createDiscountPolicyProcessor");
	}
	
	public static StockAdjustmentProcessor getStockAdjustmentProcessor()
	{
		return (StockAdjustmentProcessor)context.getBean("stockAdjustmentProcessor");
	}
	
	public static DiscountPolicyDefinitionView getDiscountPolicyView()
	{
		return (DiscountPolicyDefinitionView)context.getBean("discountPolicyView");
	}
	
	public static CustomerSearchView getCustomerSearchView()
	{
		return (CustomerSearchView)context.getBean("customerSearchView");
	}

	public static CustomerSearchMediator getCustomerSearchMediator() 
	{
		return (CustomerSearchMediator)context.getBean("customerSearchMediator");	
	}
	
	public static CreditNoteView getCreditNoteView()
	{
		return (CreditNoteView)context.getBean("creditNoteView");
	}

	public static IssueCreditNoteProcessor getIssueCreditNoteProcessor() 
	{
		return (IssueCreditNoteProcessor)context.getBean("issueCreditNoteProcessor");
	}
	
	public static EditPreferencesProcessor getEditPreferencesProcessor() 
	{
		return (EditPreferencesProcessor)context.getBean("editPreferencesProcessor");
	}
	
	public static ActiveDiscountPolicies getActiveDiscountPolicies()
	{
		return (ActiveDiscountPolicies)context.getBean("activeDiscountPolicies");
	}
	
	public static CreditNoteDataService getCreditNoteDataService()
	{
		return (CreditNoteDataService)context.getBean("creditNoteDataService");
	}
	
	public static TransactionDataService getTransactionDataService()
	{
		return (TransactionDataService)context.getBean("transactionDataService");
	}
	
	public static EndOfDayReportView getEndOfDayReportView()
	{
		return (EndOfDayReportView)context.getBean("endOfDayReportView");
	}
	
	public static PriceHistoryReportView getPriceHistoryReportView()
	{
		return (PriceHistoryReportView)context.getBean("priceHistoryReportView");
	}
	
	public static ReportsSelectionMenu getReportsSelectionMenu()
	{
		return (ReportsSelectionMenu)context.getBean("reportsSelectionMenu");
	}
	
	public static GenericReportTemplateView1 getGenericReportTemplateView1()
	{
		return (GenericReportTemplateView1)context.getBean("genericReportTemplate1View");
	}

	public static UserManagementView getUserManagementView()
	{
		return (UserManagementView)context.getBean("userManagementView");
	}
	
	public static PreferencesView getPreferencesView()
	{
		return (PreferencesView)context.getBean("preferencesView");
	}
	
	public static CreateUserProcessor getCreateUserProcessor()
	{
		return (CreateUserProcessor)context.getBean("createUserProcessor");
	}
	
	public static EditUserProcessor getEditUserProcessor()
	{
		return (EditUserProcessor)context.getBean("editUserProcessor");
	}
	
	public static UserManagementDataService getUserManagementDataService()
	{
		return (UserManagementDataService)context.getBean("userManagementDataService");
	}
	
	public static TransactionController getTransactionController()
	{
		return (TransactionController)context.getBean("transactionController");
	}

	public static TransactionDetailsReportView getTransactionDetailsReportView() 
	{
		return (TransactionDetailsReportView)context.getBean("transactionDetailsReportView");
	}
	
	public static StockAdjustmentView getStockAdjustmentView()
	{
		return (StockAdjustmentView)context.getBean("stockAdjustmentView");
	}
}
