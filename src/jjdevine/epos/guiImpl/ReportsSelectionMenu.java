package jjdevine.epos.guiImpl;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jjdevine.epos.common.CommonComponentFactory;
import jjdevine.epos.common.Constants;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.customComponents.DateSelectionPanel;
import jjdevine.epos.common.customComponents.ManagedJFrame;
import jjdevine.epos.common.persistence.EposCommonDatasourceFactory;
import jjdevine.epos.mediators.CustomerSearchMediator;
import jjdevine.epos.mediators.CustomerSearchMediatorEvent;
import jjdevine.epos.mediators.CustomerSearchMediatorListener;
import jjdevine.epos.processors.ShowCreditNoteReportProcessor;
import jjdevine.epos.processors.ShowEODReportProcessor;
import jjdevine.epos.processors.ShowGarmentSalesProcessor;
import jjdevine.epos.processors.ShowOrdersReportProcessor;
import jjdevine.epos.processors.ShowPriceHistoryReportProcessor;
import jjdevine.epos.processors.ShowStockAdjustmentReportProcessor;
import jjdevine.epos.processors.ShowStockReportProcessor;
import jjdevine.epos.processors.ShowTransactionDetailsReportProcessor;
import jjdevine.epos.processors.ShowTransactionSummaryReportProcessor;
import jjdevine.epos.services.searchgarmentcode.SearchGarmentCodeService;
import jjdevine.epos.services.searchgarmentcode.events.GarmentCodeSearchEvent;
import jjdevine.epos.services.searchgarmentcode.events.GarmentCodeSearchEventListener;
import jjdevine.epos.services.searchskucode.SearchSKUCodeService;
import jjdevine.epos.services.searchskucode.events.SearchListener;
import jjdevine.epos.services.searchskucode.events.SearchResultEvent;
import jjdevine.epos.services.selectvalue.SelectExistingValueService;
import jjdevine.epos.services.selectvalue.events.SelectExistingValueEvent;
import jjdevine.epos.services.selectvalue.events.SelectExistingValueEventListener;

public class ReportsSelectionMenu extends ManagedJFrame implements ActionListener, ListSelectionListener, SearchListener, GarmentCodeSearchEventListener, CustomerSearchMediatorListener
{
	private static final long serialVersionUID = 1902664598309410128L;
	private JPanel panelHeader, panelDummyCriteria, panelButtons;
	private EndOfDayReportCriteriaPanel panelEODCriteria;
	private GarmentCodeOrSKUIdCriteriaPanel panelGarmentCodeOrSKUIdCriteria;
	private CreditNoteReportCriteriaPanel panelCreditNoteReportCriteria;
	private DateOrCustomerIdCriteriaPanel panelDateOrCustomerIdCriteria;
	private GarmentCodeOrAllCriteriaPanel panelGarmentCodeOrAllCriteria;
	private TransactionIdCriteriaPanel panelTransactionIdCriteria;
	private OrdersCriteriaPanel panelOrdersCriteria;
	private JList listReports;
	private JScrollPane jspListReports;
	private JButton bOK, bCancel;
	private int sWidth = 400, sHeight = 500;
	private Dimension dimCriteriaPanel = new Dimension(sWidth-20, 152);
	private ShowCreditNoteReportProcessor showCreditNoteReportProcessor;
	private ShowEODReportProcessor showEODReportProcessor;
	private ShowPriceHistoryReportProcessor showPriceHistoryReportProcessor;
	private ShowGarmentSalesProcessor showGarmentSalesProcessor;
	private ShowStockAdjustmentReportProcessor showStockAdjustmentReportProcessor;
	private ShowStockReportProcessor showStockReportProcessor;
	private ShowTransactionDetailsReportProcessor showTransactionDetailsReportProcessor;
	private ShowTransactionSummaryReportProcessor showTransactionSummaryReportProcessor;
	private ShowOrdersReportProcessor showOrdersReportProcessor;
	private SearchGarmentCodeService garmentCodeSvc;
	private CustomerSearchMediator customerSearchMediator;
	
	private static final String CREDIT_NOTE_REPORT = "Credit Note Report";
	private static final String END_OF_DAY_REPORT = "End of Day Report";
	private static final String GARMENT_SALES_REPORT = "Garment Sales Report";
	private static final String ORDERS_REPORT = "Orders Report";
	private static final String PRICE_HISTORY_REPORT = "Price History Report";
	private static final String STOCK_ADJUSTMENT_REPORT = "Stock Adjustment Report";
	private static final String STOCK_REPORT = "Stock Report";
	private static final String TRANSACTION_DETAILS_REPORT = "Transaction Details Report";
	private static final String TRANSACTION_SUMMARY_REPORT = "Transaction Summary Report";
	
	public ReportsSelectionMenu()
	{
		super("Select Report");	//form heading
		//create container to place components in:
		Container container = getContentPane();
		container.setLayout(new FlowLayout());	//set flow layout
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);	
		
		/*
		 * create panels
		 */
		
		panelHeader = CommonComponentFactory.createHeadingJPanel("Select Report", new Font("Comic Sans MS", Font.BOLD, 22));
		panelHeader.setPreferredSize(new Dimension(sWidth-20,40));
		panelDummyCriteria = CommonComponentFactory.createBorderedJPanel(dimCriteriaPanel);
		panelButtons = CommonComponentFactory.createBorderedJPanel(new Dimension(sWidth-20, 45));
		
		/*
		 * report list
		 */
		
		listReports = new JList(new String[]{
				CREDIT_NOTE_REPORT,
				END_OF_DAY_REPORT, 
				GARMENT_SALES_REPORT, 
				ORDERS_REPORT,
				PRICE_HISTORY_REPORT,
				STOCK_ADJUSTMENT_REPORT,
				STOCK_REPORT,
				TRANSACTION_DETAILS_REPORT,
				TRANSACTION_SUMMARY_REPORT});
		
		listReports.addListSelectionListener(this);
		jspListReports = CommonComponentFactory.createJScrollPane(listReports);
		jspListReports.setPreferredSize(new Dimension(sWidth-20, 200));
		
		/*
		 * criteria panels
		 */
		
		panelEODCriteria = new EndOfDayReportCriteriaPanel();
		panelEODCriteria.setVisible(false);
		
		panelGarmentCodeOrSKUIdCriteria = new GarmentCodeOrSKUIdCriteriaPanel();
		panelGarmentCodeOrSKUIdCriteria.setVisible(false);
		
		panelCreditNoteReportCriteria = new CreditNoteReportCriteriaPanel();
		panelCreditNoteReportCriteria.setVisible(false);
		
		panelDateOrCustomerIdCriteria = new DateOrCustomerIdCriteriaPanel();
		panelDateOrCustomerIdCriteria.setVisible(false);
		
		panelGarmentCodeOrAllCriteria = new GarmentCodeOrAllCriteriaPanel();
		panelGarmentCodeOrAllCriteria.setVisible(false);
		
		panelTransactionIdCriteria = new TransactionIdCriteriaPanel();
		panelTransactionIdCriteria.setVisible(false);
		
		panelOrdersCriteria = new OrdersCriteriaPanel();
		panelOrdersCriteria.setVisible(false);

		/*
		 * panel buttons
		 */
		
		Dimension dimButton = new Dimension(180,35);
		
		bOK = CommonComponentFactory.createJButton("OK", dimButton);
		bCancel = CommonComponentFactory.createJButton("Cancel", dimButton);
		
		bOK.addActionListener(this);
		bCancel.addActionListener(this);
		
		panelButtons.add(bOK);
		panelButtons.add(bCancel);
		
		/*
		 * add panels to screen
		 */
		
		add(panelHeader);
		add(jspListReports);
		add(panelDummyCriteria);
		add(panelEODCriteria);
		add(panelGarmentCodeOrSKUIdCriteria);
		add(panelCreditNoteReportCriteria);
		add(panelDateOrCustomerIdCriteria);
		add(panelGarmentCodeOrAllCriteria);
		add(panelTransactionIdCriteria);
		add(panelOrdersCriteria);
	
		add(panelButtons);
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();	//get screen resolution
		setLocation((d.width-sWidth)/2, (d.height-sHeight)/2);	//centre form
		
		setSize(sWidth, sHeight);	//set form size
		setVisible(true);//display screen
	}

	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		if(evt.getSource() == bOK)
		{	
			processSelection();
		}
		else if(evt.getSource() == bCancel)
		{
			dispose();
		}
	}

	private void processSelection() 
	{
		if(CREDIT_NOTE_REPORT.equals(listReports.getSelectedValue()))
		{
			int mode = panelCreditNoteReportCriteria.getMode();
			
			switch(mode)
			{
				case CreditNoteReportCriteriaPanel.MODE_ALL:
					showCreditNoteReportProcessor.showCreditNoteReportForAll();
					dispose();
					break;
					
				case CreditNoteReportCriteriaPanel.MODE_NAME:
					String name = panelCreditNoteReportCriteria.getName();
					if(name != null)
					{
						showCreditNoteReportProcessor.showCreditNoteReportForName(name);
						dispose();
					}
					break;
					
				case CreditNoteReportCriteriaPanel.MODE_DATE:
					Date dateFrom = panelCreditNoteReportCriteria.getDateFrom();
					if(dateFrom == null)
					{
						return;
					}
					
					Date dateTo = panelCreditNoteReportCriteria.getDateTo();
					if(dateTo == null)
					{
						return;
					}
					
					showCreditNoteReportProcessor.showCreditNoteReportForIssueDate(dateFrom, dateTo);
					dispose();
					break;
			}
		}
		else if(END_OF_DAY_REPORT.equals(listReports.getSelectedValue()))
		{
			int mode = panelEODCriteria.getMode();
			
			switch(mode)
			{
				case EndOfDayReportCriteriaPanel.ONE_DAY_MODE:
					Date day = panelEODCriteria.getDateOne();
					if(day != null)
					{
						showEODReportProcessor.showEODReportForDay(day);
						dispose();
					}
					break;
					
				case EndOfDayReportCriteriaPanel.CUSTOM_PERIOD_MODE:
					Date day1 = panelEODCriteria.getDateOne();
					Date day2 = panelEODCriteria.getDateTwo();
					if(day1 != null && day2 != null)
					{
						showEODReportProcessor.showEODReportForPeriod(day1, day2);
						dispose();
					}
					break;
				default:
					throw new IllegalStateException(mode + " is not a valid EOD report mode");
			}
		}
		else if(ORDERS_REPORT.equals(listReports.getSelectedValue()))
		{
			String garmentCode = panelOrdersCriteria.getGarmentCode()
			;
			Date dateFrom = panelOrdersCriteria.getDateFrom();
			if(panelOrdersCriteria.isError())
			{
				return;
			}
			
			Date dateTo = panelOrdersCriteria.getDateTo();
			if(panelOrdersCriteria.isError())
			{
				return;
			}
			
			showOrdersReportProcessor.showOrdersReport(garmentCode, dateFrom, dateTo);
			dispose();
		}
		else if(PRICE_HISTORY_REPORT.equals(listReports.getSelectedValue()))
		{
			int mode = panelGarmentCodeOrSKUIdCriteria.getMode();
			
			switch(mode)
			{
				case GarmentCodeOrSKUIdCriteriaPanel.SKU_MODE:
					SearchSKUCodeService searchSvc = SearchSKUCodeService.getInstance();
					searchSvc.setKey(PRICE_HISTORY_REPORT);
					searchSvc.addListener(this);
					searchSvc.performSearch(false);
					break;
				case GarmentCodeOrSKUIdCriteriaPanel.GARMENT_CODE_MODE:
					SearchGarmentCodeService garmentCodeService = getSearchGarmentCodeService();
					garmentCodeService.setKey(PRICE_HISTORY_REPORT);
					garmentCodeService.performSearch();
					break;
			}
		}
		else if(STOCK_REPORT.equals(listReports.getSelectedValue()))
		{
			int mode = panelGarmentCodeOrAllCriteria.getMode();
			
			switch(mode)
			{
				case GarmentCodeOrAllCriteriaPanel.MODE_ALL:
					showStockReportProcessor.showStockReport();
					dispose();
					break;
					
				case GarmentCodeOrAllCriteriaPanel.MODE_GARMENT_CODE:
					String garmentCode = panelGarmentCodeOrAllCriteria.getGarmentCode();
					
					if(garmentCode.length() == 0)
					{
						//perform a search
						SearchGarmentCodeService garmentCodeService = getSearchGarmentCodeService();
						garmentCodeService.setKey(STOCK_REPORT);
						garmentCodeService.performSearch();
					}
					else
					{
						showStockReportProcessor.showStockReportByGarmentCode(garmentCode);
						dispose();
					}
					break;
			}
		}
		else if(STOCK_ADJUSTMENT_REPORT.equals(listReports.getSelectedValue()))
		{
			int mode = panelGarmentCodeOrAllCriteria.getMode();
			
			switch(mode)
			{
				case GarmentCodeOrAllCriteriaPanel.MODE_ALL:
					showStockAdjustmentReportProcessor.showStockAdjustmentReport();
					dispose();
					break;
					
				case GarmentCodeOrAllCriteriaPanel.MODE_GARMENT_CODE:
					String garmentCode = panelGarmentCodeOrAllCriteria.getGarmentCode();
					
					if(garmentCode.length() == 0)
					{
						//perform a search
						SearchGarmentCodeService garmentCodeService = getSearchGarmentCodeService();
						garmentCodeService.setKey(STOCK_ADJUSTMENT_REPORT);
						garmentCodeService.performSearch();
					}
					else
					{
						showStockAdjustmentReportProcessor.showStockAdjustmentReport(garmentCode);
						dispose();
					}
					break;
			}
		}
		else if(GARMENT_SALES_REPORT.equals(listReports.getSelectedValue()))
		{
			int mode = panelGarmentCodeOrSKUIdCriteria.getMode();
			
			switch(mode)
			{
				case GarmentCodeOrSKUIdCriteriaPanel.SKU_MODE:
					SearchSKUCodeService searchSvc = SearchSKUCodeService.getInstance();
					searchSvc.addListener(this);
					searchSvc.setKey(GARMENT_SALES_REPORT);
					searchSvc.performSearch(false);
					break;
				case GarmentCodeOrSKUIdCriteriaPanel.GARMENT_CODE_MODE:
					SearchGarmentCodeService garmentCodeService = getSearchGarmentCodeService();
					garmentCodeService.setKey(GARMENT_SALES_REPORT);
					garmentCodeService.performSearch();
					//TODO - concurrency issues - may need new instance of search service each time
					break;
			}
		}
		else if(TRANSACTION_DETAILS_REPORT.equals(listReports.getSelectedValue()))
		{
			long txId = panelTransactionIdCriteria.getTransactionId();
			
			if(txId == -1)
			{
				JOptionPane.showMessageDialog(this, "Enter a valid transaction Id", "Error", JOptionPane.ERROR_MESSAGE);
			}
			
			showTransactionDetailsReportProcessor.showTransactionDetailsReport(txId);
			dispose();
		}
		else if(TRANSACTION_SUMMARY_REPORT.equals(listReports.getSelectedValue()))
		{
			int mode = panelDateOrCustomerIdCriteria.getMode();
			
			switch(mode)
			{
				case DateOrCustomerIdCriteriaPanel.MODE_CUSTOMER_ID:
					customerSearchMediator.addListener(this);
					customerSearchMediator.searchForCustomer();
					break;
					
				case DateOrCustomerIdCriteriaPanel.MODE_DATE:
					Date dateFrom = panelDateOrCustomerIdCriteria.getDateFrom();
					if(dateFrom == null)
					{
						return;
					}
					
					Date dateTo = panelDateOrCustomerIdCriteria.getDateTo();
					if(dateTo == null)
					{
						return;
					}
					
					showTransactionSummaryReportProcessor.showTransactionSummaryReportByDate(dateFrom, dateTo);
					dispose();
					break;	
			}
		}
		else
		{
			JOptionPane.showMessageDialog(this, "You must select a report to view!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent evt) 
	{
		if(CREDIT_NOTE_REPORT.equals(listReports.getSelectedValue()))
		{
			hideAllCriteriaPanels();
			panelCreditNoteReportCriteria.setVisible(true);
		}
		else if(END_OF_DAY_REPORT.equals(listReports.getSelectedValue()))
		{
			hideAllCriteriaPanels();
			panelEODCriteria.setVisible(true);
		}
		else if(ORDERS_REPORT.equals(listReports.getSelectedValue()))
		{
			hideAllCriteriaPanels();
			panelOrdersCriteria.setVisible(true);
		}
		else if(PRICE_HISTORY_REPORT.equals(listReports.getSelectedValue()))
		{
			hideAllCriteriaPanels();
			panelGarmentCodeOrSKUIdCriteria.setVisible(true);
		}
		else if(STOCK_REPORT.equals(listReports.getSelectedValue()))
		{
			hideAllCriteriaPanels();
			panelGarmentCodeOrAllCriteria.setVisible(true);
		}
		else if(STOCK_ADJUSTMENT_REPORT.equals(listReports.getSelectedValue()))
		{
			hideAllCriteriaPanels();
			panelGarmentCodeOrAllCriteria.setVisible(true);
		}
		else if(GARMENT_SALES_REPORT.equals(listReports.getSelectedValue()))
		{
			hideAllCriteriaPanels();
			panelGarmentCodeOrSKUIdCriteria.setVisible(true);
		}
		else if(TRANSACTION_DETAILS_REPORT.equals(listReports.getSelectedValue()))
		{
			hideAllCriteriaPanels();
			panelTransactionIdCriteria.setVisible(true);
		}
		else if(TRANSACTION_SUMMARY_REPORT.equals(listReports.getSelectedValue()))
		{
			hideAllCriteriaPanels();
			panelDateOrCustomerIdCriteria.setVisible(true);
		}
	}
	
	private void hideAllCriteriaPanels()
	{
		panelDummyCriteria.setVisible(false);
		panelEODCriteria.setVisible(false);
		panelGarmentCodeOrSKUIdCriteria.setVisible(false);
		panelCreditNoteReportCriteria.setVisible(false);
		panelDateOrCustomerIdCriteria.setVisible(false);
		panelGarmentCodeOrAllCriteria.setVisible(false);
		panelTransactionIdCriteria.setVisible(false);
		panelOrdersCriteria.setVisible(false);
	}
	
	private class GarmentCodeOrSKUIdCriteriaPanel extends JPanel
	{
		private static final long serialVersionUID = 5463472909762353786L;
		private JRadioButton rbSKU, rbGarmentCode;
		private static final int SKU_MODE = 0;
		private static final int GARMENT_CODE_MODE = 1;
		
		private GarmentCodeOrSKUIdCriteriaPanel()
		{
			setPreferredSize(dimCriteriaPanel);
			setBorder(new LineBorder(Color.BLACK));
			
			rbSKU = CommonComponentFactory.createJRadioButton("By SKU ID", true);
			rbSKU.setPreferredSize(new Dimension(320,20));
			rbGarmentCode = CommonComponentFactory.createJRadioButton("By Garment Code", false);
			rbGarmentCode.setPreferredSize(new Dimension(320,20));
			
			ButtonGroup rbGroup = new ButtonGroup();
			rbGroup.add(rbSKU);
			rbGroup.add(rbGarmentCode);
			
			add(rbSKU);
			add(rbGarmentCode);
		}
		
		public int getMode()
		{
			if(rbSKU.isSelected())
			{
				return SKU_MODE;
			}
			else
			{
				return GARMENT_CODE_MODE;
			}
		}
	}
	
	private class OrdersCriteriaPanel extends JPanel implements ActionListener, SelectExistingValueEventListener
	{
		private static final long serialVersionUID = 1609458303042728808L;
		private JPanel panelGarmentCode, panelDateFrom, panelDateTo;
		private DateSelectionPanel dspDateFrom, dspDateTo;
		private JLabel lGarmentCode, lDateFrom, lDateTo;
		private JTextField tfGarmentCode;
		private JButton bGarmentCode;
		boolean error = false;
		
		private OrdersCriteriaPanel()
		{
			setPreferredSize(dimCriteriaPanel);
			setBorder(new LineBorder(Color.BLACK));
			
			/*
			 * panel garment code
			 */
			
			panelGarmentCode = CommonComponentFactory.createBorderedJPanel(new Dimension(sWidth-30,40));
			lGarmentCode = CommonComponentFactory.createJLabel("Garment Code:", new Dimension(120,30));
			tfGarmentCode = CommonComponentFactory.createJTextField(new Dimension(170,30));
			bGarmentCode = CommonComponentFactory.createJButton("...", new Dimension(30, 30));

			bGarmentCode.addActionListener(this);
			
			panelGarmentCode.add(lGarmentCode);
			panelGarmentCode.add(tfGarmentCode);
			panelGarmentCode.add(bGarmentCode);
			
			/*
			 * panel date from
			 */
			
			panelDateFrom = CommonComponentFactory.createBorderedJPanel(new Dimension(sWidth-30, 39));
			EposUtils.setFlowLayoutGaps(panelDateFrom, 5, 1);
			
			lDateFrom = CommonComponentFactory.createJLabel("Date From:", new Dimension(100, 35));
			dspDateFrom = new DateSelectionPanel();
			dspDateFrom.setTimeMode(false);
			
			panelDateFrom.add(lDateFrom);
			panelDateFrom.add(dspDateFrom);
			
			/*
			 * panel date to
			 */
			
			panelDateTo = CommonComponentFactory.createBorderedJPanel(new Dimension(sWidth-30, 39));
			EposUtils.setFlowLayoutGaps(panelDateTo, 5, 1);
			
			lDateTo = CommonComponentFactory.createJLabel("Date To:", new Dimension(100, 35));
			dspDateTo = new DateSelectionPanel();
			dspDateTo.setTimeMode(false);
			
			panelDateTo.add(lDateTo);
			panelDateTo.add(dspDateTo);
			
			/*
			 * add sub panels
			 */
			add(panelGarmentCode);
			add(panelDateFrom);
			add(panelDateTo);
		}
		
		private boolean isError()
		{
			return error;
		}

		@Override
		public void actionPerformed(ActionEvent evt) 
		{
			if(evt.getSource() == bGarmentCode)
			{
				List<String> dbValues = EposCommonDatasourceFactory.createEposDatasource().getAllDistinctValues(Constants.GARMENT_CODE);
				SelectExistingValueService svc = SelectExistingValueService.getInstance();
				svc.setValues(dbValues);
				svc.getValue(this);
			}
		}
		
		public Date getDateFrom()
		{
			error = false;
			return getDate(dspDateFrom);
		}
		
		public Date getDateTo()
		{
			error = false;
			return getDate(dspDateTo);
		}
		
		public String getGarmentCode()
		{
			return tfGarmentCode.getText().trim().length() > 0 ? tfGarmentCode.getText().trim() : null; 
		}
		
		public Date getDate(DateSelectionPanel dateSelectionPanel)
		{
			if(dateSelectionPanel.noDateEntered())
			{
				return null;
			}
			
			int result = dateSelectionPanel.validateDate();
			
			switch(result)
			{
				case DateSelectionPanel.DAY_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Day entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					error = true;
					return null;
				case DateSelectionPanel.MONTH_NOT_VALID:
					error = true;
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Month entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.YEAR_NOT_VALID:
					error = true;
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Year entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.HOUR_NOT_VALID:
					error = true;
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Hour entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.MINUTE_NOT_VALID:
					error = true;
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Minute entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.DATE_OK:
					return dateSelectionPanel.getDate();
				default:
					error = true;
					throw new IllegalStateException("Unable to validate date, cannot recognise validation code");
			}
		}

		@Override
		public void onSelectExistingValueEvent(SelectExistingValueEvent evt) 
		{
			tfGarmentCode.setText(evt.getSelectedValue());
		}
	}
	
	private class GarmentCodeOrAllCriteriaPanel extends JPanel implements ActionListener
	{
		private static final long serialVersionUID = -170858384301118062L;
		private JPanel panelRadioButtons;
		private JRadioButton rbAll, rbGarmentCode;
		private JLabel lGarmentCode, lInfo;
		private JTextField tfGarmentCode;
		
		private static final int MODE_ALL = 0;
		private static final int MODE_GARMENT_CODE = 1;
		
		private GarmentCodeOrAllCriteriaPanel()
		{
			setPreferredSize(dimCriteriaPanel);
			setBorder(new LineBorder(Color.BLACK));
			
			/*
			 * panel radio buttons
			 */
			
			panelRadioButtons = CommonComponentFactory.createBorderedJPanel(new Dimension(sWidth-30, 30));
			
			add(panelRadioButtons);
			
			rbAll = CommonComponentFactory.createJRadioButton("All", true);
			rbAll.setPreferredSize(new Dimension(100,20));
			rbAll.addActionListener(this);
			rbAll.setSelected(true);
			rbGarmentCode = CommonComponentFactory.createJRadioButton("By Garment Code", false);
			rbGarmentCode.setPreferredSize(new Dimension(140,20));
			rbGarmentCode.addActionListener(this);
			
			ButtonGroup rbGroup = new ButtonGroup();
			rbGroup.add(rbAll);
			rbGroup.add(rbGarmentCode);

			panelRadioButtons.add(rbAll);
			panelRadioButtons.add(rbGarmentCode);
			
			/*
			 * garment code input
			 */
			
			lGarmentCode = CommonComponentFactory.createJLabel("Garment Code :", new Dimension(100,25));
			tfGarmentCode = CommonComponentFactory.createJTextField(new Dimension(100,25));
			lInfo = CommonComponentFactory.createJLabel("(Leave blank to perform a search)", new Dimension(200,25));
			
			lGarmentCode.setVisible(false);
			tfGarmentCode.setVisible(false);
			lInfo.setVisible(false);
			
			add(lGarmentCode);
			add(tfGarmentCode);
			add(lInfo);
		}
		
		public int getMode() 
		{
			if(rbGarmentCode.isSelected())
			{
				return MODE_GARMENT_CODE;
			}
			else
			{
				return MODE_ALL;
			}
		}

		@Override
		public void actionPerformed(ActionEvent evt) 
		{
			if(rbGarmentCode.isSelected())
			{
				lGarmentCode.setVisible(true);
				tfGarmentCode.setVisible(true);
				lInfo.setVisible(true);
			}
			else
			{
				lGarmentCode.setVisible(false);
				tfGarmentCode.setVisible(false);
				lInfo.setVisible(false);
			}
		}
		
		public String getGarmentCode()
		{
			return tfGarmentCode.getText().trim();
		}
		
	}
	
	private class DateOrCustomerIdCriteriaPanel extends JPanel implements ActionListener
	{
		private static final long serialVersionUID = 1545601710402075393L;
		private JPanel panelRadioButtons, panelDate;
		private JRadioButton rbDate, rbCustomerId;
		private DateSelectionPanel panelDateFrom, panelDateTo;
		private JLabel lDateFrom, lDateTo;
		
		private static final int MODE_DATE = 0;
		private static final int MODE_CUSTOMER_ID = 1;
		
		private DateOrCustomerIdCriteriaPanel()
		{
			setPreferredSize(dimCriteriaPanel);
			setBorder(new LineBorder(Color.BLACK));
			
			/*
			 * panel radio buttons
			 */
			
			panelRadioButtons = CommonComponentFactory.createBorderedJPanel(new Dimension(sWidth-30, 30));
			
			add(panelRadioButtons);
			
			rbDate = CommonComponentFactory.createJRadioButton("By Date", true);
			rbDate.setPreferredSize(new Dimension(100,20));
			rbDate.addActionListener(this);
			rbDate.setSelected(true);
			rbCustomerId = CommonComponentFactory.createJRadioButton("By Customer", false);
			rbCustomerId.setPreferredSize(new Dimension(100,20));
			rbCustomerId.addActionListener(this);
			
			ButtonGroup rbGroup = new ButtonGroup();
			rbGroup.add(rbDate);
			rbGroup.add(rbCustomerId);

			panelRadioButtons.add(rbDate);
			panelRadioButtons.add(rbCustomerId);
			
			/*
			 * panel date
			 */
			
			Dimension dimLabel = new Dimension(90, 35);
			
			lDateFrom = CommonComponentFactory.createJLabel("Date From:", dimLabel);
			lDateTo = CommonComponentFactory.createJLabel("Date To:", dimLabel);
			
			panelDate = CommonComponentFactory.createBorderedJPanel(new Dimension(sWidth-30, 87));
			add(panelDate);
			
			panelDateFrom = new DateSelectionPanel();
			panelDateFrom.setTimeMode(false);
			panelDateFrom.setTime(new Date(new Date().getTime()-86400000));//yesterday
			
			panelDateTo = new DateSelectionPanel();
			panelDateTo.setTimeMode(false);
			panelDateTo.setTime(new Date());
			
			panelDate.add(lDateFrom);
			panelDate.add(panelDateFrom);
			panelDate.add(lDateTo);
			panelDate.add(panelDateTo);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			if(rbDate.isSelected())
			{
				panelDate.setVisible(true);
			}
			else
			{
				panelDate.setVisible(false);
			}
		}
		
		public int getMode()
		{
			if(rbDate.isSelected())
			{
				return MODE_DATE;
			}
			else
			{
				return MODE_CUSTOMER_ID;
			}
		}
		
		public Date getDateFrom()
		{
			int result = panelDateFrom.validateDate();
			
			switch(result)
			{
				case DateSelectionPanel.DAY_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Day entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.MONTH_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Month entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.YEAR_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Year entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.HOUR_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Hour entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.MINUTE_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Minute entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.DATE_OK:
					return panelDateFrom.getDate();
				default:
					throw new IllegalStateException("Unable to validate date, cannot recognise validation code");
			}
		}
		
		public Date getDateTo()
		{
			int result = panelDateTo.validateDate();
			
			switch(result)
			{
				case DateSelectionPanel.DAY_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Day entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.MONTH_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Month entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.YEAR_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Year entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.HOUR_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Hour entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.MINUTE_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Minute entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.DATE_OK:
					return panelDateTo.getDate();
				default:
					throw new IllegalStateException("Unable to validate date, cannot recognise validation code");
			}
		}
	}
	
	private class CreditNoteReportCriteriaPanel extends JPanel implements ActionListener
	{
		private static final long serialVersionUID = 3011400842080184241L;
		private JPanel panelRadioButtons, panelDate, panelName;
		private DateSelectionPanel panelDateFrom, panelDateTo;
		private JRadioButton rbAll, rbName, rbDate;
		private JLabel lName, lDateFrom, lDateTo;
		private JTextField tfName;
		
		private static final int MODE_ALL = 0;
		private static final int MODE_NAME = 1;
		private static final int MODE_DATE = 2;
		
		private CreditNoteReportCriteriaPanel()
		{
			setPreferredSize(dimCriteriaPanel);
			setBorder(new LineBorder(Color.BLACK));
			
			/*
			 * panel radio buttons
			 */
			
			panelRadioButtons = CommonComponentFactory.createBorderedJPanel(new Dimension(sWidth-30, 30));
			
			add(panelRadioButtons);
			
			rbAll = CommonComponentFactory.createJRadioButton("All", true);
			rbAll.setPreferredSize(new Dimension(75,20));
			rbAll.addActionListener(this);
			rbName = CommonComponentFactory.createJRadioButton("By Name", false);
			rbName.setPreferredSize(new Dimension(85,20));
			rbName.addActionListener(this);
			rbDate = CommonComponentFactory.createJRadioButton("By Issue Date", false);
			rbDate.setPreferredSize(new Dimension(105,20));
			rbDate.addActionListener(this);
			
			ButtonGroup rbGroup = new ButtonGroup();
			rbGroup.add(rbAll);
			rbGroup.add(rbName);
			rbGroup.add(rbDate);
					
			panelRadioButtons.add(rbAll);
			panelRadioButtons.add(rbName);
			panelRadioButtons.add(rbDate);
			
			/*
			 * panel name
			 */
			
			panelName = CommonComponentFactory.createBorderedJPanel(new Dimension(sWidth-30, 40));
			panelName.setVisible(false);
			add(panelName);
			
			lName = CommonComponentFactory.createJLabel("Name Issued To :", new Dimension(100, 30));
			tfName = CommonComponentFactory.createJTextField(new Dimension(240, 30));
			
			panelName.add(lName);
			panelName.add(tfName);
			
			/*
			 * panel date
			 */
			
			Dimension dimLabel = new Dimension(90, 35);
			
			lDateFrom = CommonComponentFactory.createJLabel("Date From:", dimLabel);
			lDateTo = CommonComponentFactory.createJLabel("Date To:", dimLabel);
			
			panelDate = CommonComponentFactory.createBorderedJPanel(new Dimension(sWidth-30, 87));
			panelDate.setVisible(false);
			add(panelDate);
			
			panelDateFrom = new DateSelectionPanel();
			panelDateFrom.setTimeMode(false);
			panelDateFrom.setTime(new Date(new Date().getTime()-86400000));//yesterday
			
			panelDateTo = new DateSelectionPanel();
			panelDateTo.setTimeMode(false);
			panelDateTo.setTime(new Date());
			
			panelDate.add(lDateFrom);
			panelDate.add(panelDateFrom);
			panelDate.add(lDateTo);
			panelDate.add(panelDateTo);
		}
		
		public int getMode()
		{
			if(rbAll.isSelected())
			{
				return MODE_ALL;
			}
			else if(rbName.isSelected())
			{
				return MODE_NAME;
			}
			else if(rbDate.isSelected())
			{
				return MODE_DATE;
			}
			throw new IllegalStateException("No valid mode detected");
		}
		
		public String getName()
		{
			String strName = tfName.getText().trim();
			
			if(strName.length() == 0)
			{
				JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Please enter a name", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			
			return "%" + strName + "%"; //search as a wildcard
		}
		
		public Date getDateFrom()
		{
			int result = panelDateFrom.validateDate();
			
			switch(result)
			{
				case DateSelectionPanel.DAY_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Day entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.MONTH_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Month entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.YEAR_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Year entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.HOUR_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Hour entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.MINUTE_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Minute entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.DATE_OK:
					return panelDateFrom.getDate();
				default:
					throw new IllegalStateException("Unable to validate date, cannot recognise validation code");
			}
		}
		
		public Date getDateTo()
		{
			int result = panelDateTo.validateDate();
			
			switch(result)
			{
				case DateSelectionPanel.DAY_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Day entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.MONTH_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Month entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.YEAR_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Year entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.HOUR_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Hour entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.MINUTE_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Minute entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.DATE_OK:
					return panelDateTo.getDate();
				default:
					throw new IllegalStateException("Unable to validate date, cannot recognise validation code");
			}
		}

		@Override
		public void actionPerformed(ActionEvent evt) 
		{
			if(rbAll.isSelected())
			{
				panelName.setVisible(false);
				panelDate.setVisible(false);
			}
			else if(rbName.isSelected())
			{
				panelName.setVisible(true);
				panelDate.setVisible(false);
			}
			else if(rbDate.isSelected())
			{
				panelName.setVisible(false);
				panelDate.setVisible(true);
			}
			
		}
	}
	
	private class TransactionIdCriteriaPanel extends JPanel
	{
	
		private static final long serialVersionUID = 4087591024246699264L;
		private JLabel lTxId;
		private JTextField tfTxId;
		
		private TransactionIdCriteriaPanel()
		{
			setPreferredSize(dimCriteriaPanel);
			setBorder(new LineBorder(Color.BLACK));
			
			lTxId = CommonComponentFactory.createJLabel("Transaction Id", new Dimension(100, 30));
			tfTxId = CommonComponentFactory.createJTextField(new Dimension(250, 30));
			
			add(lTxId);
			add(tfTxId);
		}
		
		private long getTransactionId()
		{
			String input = tfTxId.getText().trim();
			
			if(input.length() == 0)
			{
				return -1;
			}
			
			try
			{
				long txId = Long.parseLong(input);
				if(txId > 0)
				{
					return txId;
				}
			}
			catch(NumberFormatException nfe)
			{
				
			}
			
			return -1;
		}
	}
	
	private class EndOfDayReportCriteriaPanel extends JPanel implements ActionListener
	{
		private static final long serialVersionUID = 322600622284279879L;
		private JRadioButton rbOneDay, rbCustom;
		private JPanel panelRadioButtons, panelDateOne, panelDateTwo;
		private DateSelectionPanel panelDateSelectionOne, panelDateSelectionTwo;
		private JLabel lDateOne, lDateTwo;
		
		public static final int ONE_DAY_MODE = 0;
		public static final int CUSTOM_PERIOD_MODE = 1;
		
		private EndOfDayReportCriteriaPanel()
		{
			setPreferredSize(dimCriteriaPanel);
			setBorder(new LineBorder(Color.BLACK));
			
			/*
			 * panel radio buttons
			 */
			
			panelRadioButtons = CommonComponentFactory.createBorderedJPanel(new Dimension(sWidth-30, 30));
			
			rbOneDay = CommonComponentFactory.createJRadioButton("One Day", true);
			rbOneDay.setPreferredSize(new Dimension(120,20));
			rbCustom = CommonComponentFactory.createJRadioButton("Custom Time Period", false);
			rbCustom.setPreferredSize(new Dimension(150,20));
			ButtonGroup rbGroup = new ButtonGroup();
			rbGroup.add(rbOneDay);
			rbGroup.add(rbCustom);
			rbOneDay.addActionListener(this);
			rbCustom.addActionListener(this);
			
			panelRadioButtons.add(rbOneDay);
			panelRadioButtons.add(rbCustom);
			
			/*
			 * panel date one
			 */
			
			panelDateOne = CommonComponentFactory.createBorderedJPanel(new Dimension(sWidth-30, 50));
			lDateOne = CommonComponentFactory.createJLabel("Date:", new Dimension(80,35));
			
			panelDateSelectionOne = new DateSelectionPanel();
			panelDateSelectionOne.setTimeMode(false);
			panelDateSelectionOne.setTime(new Date());
			
			panelDateOne.add(lDateOne);
			panelDateOne.add(panelDateSelectionOne);
			
			/*
			 * panel date two
			 */
			
			panelDateTwo = CommonComponentFactory.createBorderedJPanel(new Dimension(sWidth-30, 50));
			lDateTwo = CommonComponentFactory.createJLabel("Date To:", new Dimension(80,35));
			
			panelDateSelectionTwo = new DateSelectionPanel();
			
			//set default date to tomorrow
			Calendar cal = Calendar.getInstance();
			cal.roll(Calendar.DAY_OF_YEAR, 1);
			panelDateSelectionTwo.setTime(cal.getTime());
			
			panelDateTwo.add(lDateTwo);
			panelDateTwo.add(panelDateSelectionTwo);
			panelDateTwo.setVisible(false);
			
			/*
			 * add panels to parent panel
			 */
			
			add(panelRadioButtons);
			add(panelDateOne);
			add(panelDateTwo);
		}
		
		public int getMode()
		{
			if(rbOneDay.isSelected())
			{
				return ONE_DAY_MODE;
			}
			else
			{
				return CUSTOM_PERIOD_MODE;
			}
		}
		
		public Date getDateOne()
		{
			int result = panelDateSelectionOne.validateDate();
			
			switch(result)
			{
				case DateSelectionPanel.DAY_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Day entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.MONTH_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Month entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.YEAR_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Year entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.HOUR_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Hour entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.MINUTE_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Minute entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.DATE_OK:
					return panelDateSelectionOne.getDate();
				default:
					throw new IllegalStateException("Unable to validate date, cannot recognise validation code");
				
			}
		}
		
		public Date getDateTwo()
		{
			int result = panelDateSelectionTwo.validateDate();
			
			switch(result)
			{
				case DateSelectionPanel.DAY_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Day entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.MONTH_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Month entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.YEAR_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Year entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.HOUR_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Hour entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.MINUTE_NOT_VALID:
					JOptionPane.showMessageDialog(ReportsSelectionMenu.this, "Minute entered is not valid", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				case DateSelectionPanel.DATE_OK:
					return panelDateSelectionTwo.getDate();
				default:
					throw new IllegalStateException("Unable to validate date, cannot recognise validation code");
				
			}
		}

		@Override
		public void actionPerformed(ActionEvent evt) 
		{
			if(rbOneDay.isSelected())
			{
				lDateOne.setText("Date :");
				panelDateTwo.setVisible(false);
				panelDateSelectionOne.setTimeMode(false);
			}
			else
			{
				lDateOne.setText("Date From:");
				panelDateTwo.setVisible(true);
				panelDateSelectionOne.setTimeMode(true);
			}
			
		}
		
	}
	
	private SearchGarmentCodeService getSearchGarmentCodeService()
	{
		if(garmentCodeSvc == null)
		{
			garmentCodeSvc = SearchGarmentCodeService.getInstance();
			garmentCodeSvc.addListener(this);
		}

		return garmentCodeSvc;
	}

	public ShowEODReportProcessor getShowEODReportProcessor() {
		return showEODReportProcessor;
	}

	public void setShowEODReportProcessor(
			ShowEODReportProcessor showEODReportProcessor) {
		this.showEODReportProcessor = showEODReportProcessor;
	}

	public ShowPriceHistoryReportProcessor getShowPriceHistoryReportProcessor() {
		return showPriceHistoryReportProcessor;
	}

	public void setShowPriceHistoryReportProcessor(
			ShowPriceHistoryReportProcessor showPriceHistoryReportProcessor) {
		this.showPriceHistoryReportProcessor = showPriceHistoryReportProcessor;
	}

	public ShowGarmentSalesProcessor getShowGarmentSalesProcessor() {
		return showGarmentSalesProcessor;
	}

	public void setShowGarmentSalesProcessor(
			ShowGarmentSalesProcessor showGarmentSalesProcessor) {
		this.showGarmentSalesProcessor = showGarmentSalesProcessor;
	}

	public ShowCreditNoteReportProcessor getShowCreditNoteReportProcessor() {
		return showCreditNoteReportProcessor;
	}

	public void setShowCreditNoteReportProcessor(
			ShowCreditNoteReportProcessor showCreditNoteReportProcessor) {
		this.showCreditNoteReportProcessor = showCreditNoteReportProcessor;
	}

	public void setShowTransactionDetailsReportProcessor(
			ShowTransactionDetailsReportProcessor showTransactionDetailsReportProcessor) {
		this.showTransactionDetailsReportProcessor = showTransactionDetailsReportProcessor;
	}

	public ShowTransactionSummaryReportProcessor getShowTransactionSummaryReportProcessor() {
		return showTransactionSummaryReportProcessor;
	}

	public void setShowTransactionSummaryReportProcessor(
			ShowTransactionSummaryReportProcessor showTransactionSummaryReportProcessor) {
		this.showTransactionSummaryReportProcessor = showTransactionSummaryReportProcessor;
	}

	public ShowStockReportProcessor getShowStockReportProcessor() {
		return showStockReportProcessor;
	}

	public void setShowStockReportProcessor(
			ShowStockReportProcessor showStockReportProcessor) {
		this.showStockReportProcessor = showStockReportProcessor;
	}

	public void setShowOrdersReportProcessor(
			ShowOrdersReportProcessor showOrdersReportProcessor) {
		this.showOrdersReportProcessor = showOrdersReportProcessor;
	}

	public CustomerSearchMediator getCustomerSearchMediator() {
		return customerSearchMediator;
	}

	public void setCustomerSearchMediator(
			CustomerSearchMediator customerSearchMediator) {
		this.customerSearchMediator = customerSearchMediator;
	}

	public void setShowStockAdjustmentReportProcessor(
			ShowStockAdjustmentReportProcessor showStockAdjustmentReportProcessor) {
		this.showStockAdjustmentReportProcessor = showStockAdjustmentReportProcessor;
	}

	@Override
	public void onSearchResultEvent(SearchResultEvent evt) 
	{
		long skuId = evt.getGarmentData().getSkuId();
		if(PRICE_HISTORY_REPORT.equals(evt.getKey()))
		{
			showPriceHistoryReportProcessor.showPriceHistoryReportForSKUID(skuId);
		}
		else if(GARMENT_SALES_REPORT.equals(evt.getKey()))
		{
			showGarmentSalesProcessor.showGarmentSalesBySKUId(skuId);
		}
			
		dispose();
	}

	@Override
	public void onGarmentCodeSearchEvent(GarmentCodeSearchEvent evt) 
	{
		String garmentCode = evt.getInfo().getGarmentCode();
		
		if(PRICE_HISTORY_REPORT.equals(evt.getKey()))
		{
			showPriceHistoryReportProcessor.showPriceHistoryReportForGarmentCode(garmentCode);
		}
		else if(GARMENT_SALES_REPORT.equals(evt.getKey()))
		{
			showGarmentSalesProcessor.showGarmentSalesByGarmentCode(garmentCode);
		}
		else if(STOCK_REPORT.equals(evt.getKey()))
		{
			showStockReportProcessor.showStockReportByGarmentCode(garmentCode);
		}
		else if(STOCK_ADJUSTMENT_REPORT.equals(evt.getKey()))
		{
			showStockAdjustmentReportProcessor.showStockAdjustmentReport(garmentCode);
		}
		
		dispose();
	}

	@Override
	public void onCustomerSearchMediatorEvent(CustomerSearchMediatorEvent evt) 
	{
		switch(evt.getType())
		{
			case CUSTOMER_SELECTED:
				showTransactionSummaryReportProcessor.showTransactionSummaryReportByCustomer(evt.getCustomerId());
				dispose();
				break;
		}
	}
}
