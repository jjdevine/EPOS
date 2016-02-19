package jjdevine.epos.guiImpl;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;

import jjdevine.epos.EposContext;
import jjdevine.epos.common.CommonComponentFactory;
import jjdevine.epos.common.Constants;
import jjdevine.epos.common.customComponents.ManagedJFrame;
import jjdevine.epos.common.persistence.EposCommonDatasourceFactory;
import jjdevine.epos.discountdefintion.data.svc.DiscountDefinitionDataService;
import jjdevine.epos.mediators.CustomerSearchMediator;
import jjdevine.epos.mediators.CustomerSearchMediatorEvent;
import jjdevine.epos.mediators.CustomerSearchMediatorListener;
import jjdevine.epos.mediators.CustomerSearchMediatorEvent.Type;
import jjdevine.epos.receiptsetup.data.svc.ReceiptSetupDataService;
import jjdevine.epos.services.createorder.CreateOrderService;
import jjdevine.epos.services.definenewgarment.DefineNewGarmentService;
import jjdevine.epos.services.editgarment.EditGarmentService;
import jjdevine.epos.services.manageorder.ManageOrderService;
import jjdevine.epos.services.searchgarmentcode.SearchGarmentCodeService;
import jjdevine.epos.services.searchgarmentcode.events.GarmentCodeSearchEvent;
import jjdevine.epos.services.searchgarmentcode.events.GarmentCodeSearchEventListener;
import jjdevine.epos.services.selectvalue.SelectExistingValueService;
import jjdevine.epos.services.selectvalue.events.SelectExistingValueEvent;
import jjdevine.epos.services.selectvalue.events.SelectExistingValueEventListener;
import jjdevine.epos.usermanagement.data.svc.UserManagementDataService;

public class EposManagementMenuGui extends ManagedJFrame implements ActionListener, GarmentCodeSearchEventListener, SelectExistingValueEventListener, CustomerSearchMediatorListener
{
	private static final long serialVersionUID = 6822583556158846834L;
	private int sWidth = 500, sHeight = 400;
	
	private JButton bNewGarment, bEditGarment, bCreateOrder, bManageOrders, bAddCustomer, bEditCustomer, bCreateReceiptSetup,
		bEditReceiptSetup, bCreateDiscount, bCreateDiscountPolicy, bEditDiscountPolicy, bIssueCreditNote, bCreateUser, bEditUser, 
		bStockAdjustment, bReports, bPreferences, bBack;
	private SearchGarmentCodeService garmentCodeSvc;
	private SelectExistingValueService selectSvc;
	private ReceiptSetupDataService receiptSetupDataService;
	private DiscountDefinitionDataService discountDefinitionDataService;
	private CustomerSearchMediator customerSearchMediator;
	private UserManagementDataService userManagementDataService;
	
	private static String EDIT_ORDER_KEY = "editOrders";
	private static String EDIT_RECEIPT_SETUP_KEY = "editReceiptSetup";
	private static String EDIT_DISCOUNT_POLICY_KEY = "editDiscountPolicy";
	private static String EDIT_USER_KEY = "editUser";
	private static String EDIT_GARMENT_KEY = "editGarment";
	private static String STOCK_ADJUSTMENT_KEY = "stockAdjustment";

	public EposManagementMenuGui()
	{
		super("Management Options");	//form heading
		//create container to place components in:
		Container container = getContentPane();
		container.setLayout(new GridBagLayout());	//set flow layout
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
		
		/*
		 * create buttons
		 */
		
		bNewGarment = CommonComponentFactory.createJButton("Define New Garment");
		bEditGarment = CommonComponentFactory.createJButton("Edit Garment");
		bCreateOrder = CommonComponentFactory.createJButton("Create Order");
		bManageOrders = CommonComponentFactory.createJButton("Manage Orders");
		bAddCustomer = CommonComponentFactory.createJButton("Add Customer");
		bEditCustomer = CommonComponentFactory.createJButton("Edit Customer");
		bCreateReceiptSetup = CommonComponentFactory.createJButton("Create New Receipt Setup");
		bEditReceiptSetup = CommonComponentFactory.createJButton("Edit Receipt Setup");
		bCreateDiscount = CommonComponentFactory.createJButton("Create Discount");
		bCreateDiscountPolicy = CommonComponentFactory.createJButton("Create Discount Policy");
		bEditDiscountPolicy = CommonComponentFactory.createJButton("Edit Discount Policy");
		bIssueCreditNote = CommonComponentFactory.createJButton("Issue Credit Note/Gift Voucher");
		bCreateUser = CommonComponentFactory.createJButton("Create User");
		bEditUser = CommonComponentFactory.createJButton("Edit User");
		bStockAdjustment = CommonComponentFactory.createJButton("Stock Adjustment");
		bReports = CommonComponentFactory.createJButton("View Reports");
		bPreferences = CommonComponentFactory.createJButton("Change Preferences");
		bBack = CommonComponentFactory.createJButton("Go Back");
		
		bNewGarment.addActionListener(this);
		bEditGarment.addActionListener(this);
		bCreateOrder.addActionListener(this);
		bManageOrders.addActionListener(this);
		bAddCustomer.addActionListener(this);
		bEditCustomer.addActionListener(this);
		bCreateReceiptSetup.addActionListener(this);
		bEditReceiptSetup.addActionListener(this);
		bCreateDiscount.addActionListener(this);
		bCreateDiscountPolicy.addActionListener(this);
		bEditDiscountPolicy.addActionListener(this);
		bIssueCreditNote.addActionListener(this);
		bCreateUser.addActionListener(this);
		bEditUser.addActionListener(this);
		bStockAdjustment.addActionListener(this);
		bReports.addActionListener(this);
		bPreferences.addActionListener(this);
		bBack.addActionListener(this);
		
		addAllComponents(container, 
				bNewGarment, bEditGarment,
				bCreateOrder, bManageOrders,
				bAddCustomer, bEditCustomer,
				bCreateReceiptSetup, bEditReceiptSetup,
				bCreateDiscount, bCreateDiscountPolicy, 
				bEditDiscountPolicy, bIssueCreditNote,
				bCreateUser, bEditUser,
				bStockAdjustment, bReports, 
				bPreferences, bBack);
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();	//get screen resolution
		setLocation((d.width-sWidth)/2, (d.height-sHeight)/2);	//centre form
		
		setSize(sWidth, sHeight);	//set form size
		setVisible(true);//display screen
		
		//set services
		userManagementDataService = EposContext.getUserManagementDataService();
	}
	
	/**
	 * use this to add the buttons without repeating code in the constructor
	 */
	private void addAllComponents(Container container, Component... components)
	{
		GridBagConstraints constraints = new GridBagConstraints();
		
		//gaps round the edges
		constraints.insets = new Insets(3,3,3,3);
		//attach to page start
		constraints.anchor = GridBagConstraints.PAGE_START;
		//default buttons to fill grid horizontally
		constraints.fill = GridBagConstraints.HORIZONTAL;
		//make sure items vertically fill the space:
		constraints.ipady = 100;
		
		constraints.weightx=1;
		constraints.weighty=1;
		
		//start from 0,0
		constraints.gridx = 0;
		constraints.gridy = 0;
		
		int index=0;
		int buttonsInRow = 2;
		
		while(index < components.length)
		{
			container.add(components[index], constraints);
			
			if(index % buttonsInRow == 0)
			{
				constraints.gridx++;
			}
			else
			{
				constraints.gridx=0;
				constraints.gridy++;
			}
			
			index++;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		//identify event source and wrap in appropriate event
		if(e.getSource() == bNewGarment)
		{
			//TODO: sort out structure of management menu
			DefineNewGarmentService svc = DefineNewGarmentService.getInstance();
			svc.defineGarment();
			dispose();
		}
		else if(e.getSource() == bEditGarment)
		{
			if(garmentCodeSvc == null)
			{
				garmentCodeSvc = SearchGarmentCodeService.getInstance();
				garmentCodeSvc.addListener(this);
			}
			garmentCodeSvc.setKey(EDIT_GARMENT_KEY);
			garmentCodeSvc.performSearch();	
			close();
		}
		else if(e.getSource() == bCreateOrder)
		{
			CreateOrderService svc = CreateOrderService.getInstance();
			svc.createOrder();
			dispose();
		}
		else if(e.getSource() == bManageOrders)
		{
			//TODO: edit all instances where svc.getValue(listener) method is used, this code is defective. use below code instead
			getSelectExistingValueService().setValues(EposCommonDatasourceFactory.createEposDatasource().getOrderList());
			getSelectExistingValueService().getValue(EDIT_ORDER_KEY);
			dispose();
		}
		else if(e.getSource() == bAddCustomer)
		{
			EposContext.getAddCustomerProcessor().addCustomer();
			dispose();
		}
		else if(e.getSource() == bEditCustomer)
		{
			if(customerSearchMediator == null)
			{
				customerSearchMediator = EposContext.getCustomerSearchMediator();
			}
			customerSearchMediator.addListener(this);
			customerSearchMediator.searchForCustomer();
			dispose();
		}
		else if(e.getSource() == bCreateReceiptSetup)
		{
			EposContext.getCreateReceiptSetupProcessor().createReceiptSetup();
			dispose();		
		}
		else if(e.getSource() == bEditReceiptSetup)
		{	
			if(receiptSetupDataService == null)
			{
				receiptSetupDataService = EposContext.getReceiptSetupDataService();
			}
			
			List<String> setupNames = receiptSetupDataService.getAllSetupNames();
			
			getSelectExistingValueService().setValues(setupNames);
			getSelectExistingValueService().getValue(EDIT_RECEIPT_SETUP_KEY);
			dispose();
		}
		else if(e.getSource() == bCreateDiscount)
		{
			EposContext.getCreateDiscountDefinitionProcessor().createDiscountDefinition();
			dispose();
		}
		else if(e.getSource() == bCreateDiscountPolicy)
		{
			EposContext.getCreateDiscountPolicyProcessor().createDiscountPolicy();
			dispose();
		}
		else if(e.getSource() == bEditDiscountPolicy)
		{
			//TODO inject service with spring, make management menu spring managed
			if(discountDefinitionDataService == null)
			{
				discountDefinitionDataService = EposContext.getDiscountDataService();
			}
			
			List<String> discountPolicyNames = discountDefinitionDataService.getAllDiscountPolicyNames();
			
			getSelectExistingValueService().setValues(discountPolicyNames);
			getSelectExistingValueService().getValue(EDIT_DISCOUNT_POLICY_KEY);
			dispose();
		}
		else if(e.getSource() == bIssueCreditNote)
		{
			EposContext.getIssueCreditNoteProcessor().issueCreditNote();
			dispose();
		}
		else if(e.getSource() == bCreateUser)
		{
			EposContext.getCreateUserProcessor().createUser();
			dispose();
		}
		else if(e.getSource() == bEditUser)
		{
			List<String> users = userManagementDataService.getAllUserNames();
			
			getSelectExistingValueService().setValues(users);
			getSelectExistingValueService().getValue(EDIT_USER_KEY);
			
			dispose();
		}
		else if(e.getSource() == bStockAdjustment)
		{
			if(garmentCodeSvc == null)
			{
				garmentCodeSvc = SearchGarmentCodeService.getInstance();
				garmentCodeSvc.addListener(this);
			}
			garmentCodeSvc.setKey(STOCK_ADJUSTMENT_KEY);
			garmentCodeSvc.performSearch();	
			close();
			dispose();
		}
		else if(e.getSource() == bReports)
		{
			EposContext.getReportsSelectionMenu();
			dispose();
		}
		else if(e.getSource() == bPreferences)
		{
			EposContext.getEditPreferencesProcessor().editPreferences();
			dispose();
		}
		else if(e.getSource() == bBack)
		{
			dispose();
		} 
		
	}

	public void close() 
	{
		dispose();		
	}
	
	

	@Override
	public void onGarmentCodeSearchEvent(GarmentCodeSearchEvent evt) 
	{
		if(evt.getKey() == EDIT_GARMENT_KEY)
		{
			EditGarmentService svc = EditGarmentService.getInstance(EposContext.getPreferences().isTestMode());
			svc.editGarment(evt.getInfo().getGarmentCode());
		}
		else if(evt.getKey() == STOCK_ADJUSTMENT_KEY)
		{
			EposContext.getStockAdjustmentProcessor().adjustStock(evt.getInfo().getGarmentCode());
		}
		dispose();
	}

	@Override
	public void onSelectExistingValueEvent(SelectExistingValueEvent evt) 
	{
		if(EDIT_ORDER_KEY.equals(evt.getKey()))
		{
			int orderId = EposCommonDatasourceFactory.createEposDatasource().getIdByDescription(
					evt.getSelectedValue(), Constants.ORDER);
			ManageOrderService svc = ManageOrderService.getInstance();
			svc.manageOrder(orderId);
			close();
		}
		else if(EDIT_RECEIPT_SETUP_KEY.equals(evt.getKey()))
		{
			EposContext.getCreateReceiptSetupProcessor().editReceiptSetup(evt.getSelectedValue());
			close();
		}
		else if(EDIT_DISCOUNT_POLICY_KEY.equals(evt.getKey()))
		{
			EposContext.getCreateDiscountPolicyProcessor().editDiscountPolicy(evt.getSelectedValue());
			close();
		}
		else if(EDIT_USER_KEY.equals(evt.getKey()))
		{
			EposContext.getEditUserProcessor().editUser(evt.getSelectedValue());
			close();
		}
	}
	
	private SelectExistingValueService getSelectExistingValueService()
	{
		if(selectSvc == null)
		{
			selectSvc = SelectExistingValueService.getInstance();
			selectSvc.addListener(this);
		}
		
		return selectSvc;
	}

	@Override
	public void onCustomerSearchMediatorEvent(CustomerSearchMediatorEvent evt) 
	{
		if(evt.getType() == Type.CUSTOMER_SELECTED)
		{
			EposContext.getEditCustomerProcessor().editCustomer(evt.getCustomerId());
		}
	}
}
