package jjdevine.epos.guiImpl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import jjdevine.epos.EposContext;
import jjdevine.epos.beans.DiscountViewBean;
import jjdevine.epos.beans.EposCreditNotePaymentViewBean;
import jjdevine.epos.beans.EposPaymentReturnedItemViewBean;
import jjdevine.epos.beans.EposPaymentViewBean;
import jjdevine.epos.beans.EposPurchaseViewBean;
import jjdevine.epos.beans.Preferences;
import jjdevine.epos.beans.TransactionItemViewBean;
import jjdevine.epos.beans.TransactionViewBean;
import jjdevine.epos.common.CommonComponentFactory;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.customComponents.AppendableJPanel;
import jjdevine.epos.common.customComponents.HeadingJPanel;
import jjdevine.epos.common.customComponents.ManagedJFrame;
import jjdevine.epos.events.EposGarmentReturnPanelListener;
import jjdevine.epos.events.EposPaymentPanelListener;
import jjdevine.epos.events.TransactionEvent;
import jjdevine.epos.gui.EposTransactionGui;
import jjdevine.epos.listeners.TransactionEventListener;
import jjdevine.epos.utils.Utils;

import org.apache.log4j.Logger;

public class SimpleEposTransactionGui extends ManagedJFrame implements EposTransactionGui, ActionListener, EposPaymentPanelListener, EposGarmentReturnPanelListener
{
	private static final long serialVersionUID = -3518744137292306470L;
	
	private List<TransactionEventListener> listeners = new ArrayList<TransactionEventListener>();
	private List<SelectedItemPanel> itemPanels = new ArrayList<SelectedItemPanel>();
	
	private JPanel panelTop, panelTransaction, panelMenu;
	private JPanel panelUserName, panelLogo, panelDate;
	private JPanel panelTotal;
	private AppendableJPanel panelItemList;
	private JPanel panelSubTotalLabel;
	private HeadingJPanel panelNumItems, panelSubTotal;
	private JButton bSelectItem, bPayCash, bPayCard, bPayCoupon, bPayCheque;
	private JButton bModItems, bCreditNote, bReturnItem, bManagement, bLogOut, bAbout;
	private JCheckBox toggleAllCheckBox;
	private JScrollPane jspItemList;
	private JLabel lUsername, lDisplayName;	
	private Logger logger = Logger.getLogger(this.getClass());
	
	public SimpleEposTransactionGui()
	{
		super("Transaction Screen");	//form heading
		//create container to place components in:
		Container container = getContentPane();
		container.setLayout(new BorderLayout());	//set flow layout
		
		/*
		 * create main jpanels
		 */
		
		panelTop = CommonComponentFactory.createBorderedJPanel();
		panelTransaction = CommonComponentFactory.createBorderedJPanel();
		panelMenu = CommonComponentFactory.createBorderedJPanel();
		
		panelTop.setPreferredSize(new Dimension(0,100));
		panelMenu.setPreferredSize(new Dimension(282,0));
		
		/*
		 * create top panel
		 */
		
		panelTop.setLayout(new BorderLayout());
		
		panelUserName = CommonComponentFactory.createBorderedJPanel();
		lUsername = CommonComponentFactory.createCenteredJLabel("Cashier:", new Dimension(90, 30));
		lUsername.setFont(new Font("Arial", Font.BOLD, 14));
		lDisplayName = CommonComponentFactory.createCenteredJLabel("", new Dimension(90, 20));
		lDisplayName.setFont(new Font("Arial", Font.ITALIC, 14));
		bAbout = CommonComponentFactory.createJButton("About...", new Dimension(90,25));
		bAbout.addActionListener(this);
		
		panelUserName.add(lUsername);
		panelUserName.add(lDisplayName);
		panelUserName.add(bAbout);
		
		Preferences prefs = EposContext.getPreferences();
		boolean headerSet = false;
		
		try 
		{
			switch(prefs.getTillHeadingMode())
			{
				case Preferences.TILL_HEADING_MODE_TEXT:
					panelLogo = CommonComponentFactory.createHeadingJPanel(
							prefs.getTillHeading(), new Font("Comic Sans MS", Font.BOLD, 22));
					headerSet = true;
					break;
				case Preferences.TILL_HEADING_MODE_IMAGE:
					panelLogo = CommonComponentFactory.createImagePanel(new File(prefs.getTillHeadingImage()));
					headerSet = true;
					break;
			}
		} catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
		}
		
		if(!headerSet)
		{
			//default heading
			panelLogo = CommonComponentFactory.createHeadingJPanel(
					"ePOS", new Font("Comic Sans MS", Font.BOLD, 22));
		}
		
		//get and format the date
		Date date = new Date();
		DateFormat df = DateFormat.getDateInstance();
		panelDate = CommonComponentFactory.createHeadingJPanel(df.format(date));
		
		panelUserName.setPreferredSize(new Dimension(100,0));
		panelDate.setPreferredSize(new Dimension(100,0));

		panelTop.add(panelUserName, BorderLayout.LINE_START);
		panelTop.add(panelLogo, BorderLayout.CENTER);
		panelTop.add(panelDate, BorderLayout.LINE_END);
		
		/*
		 * transaction panel
		 */
		
		FlowLayout panelTransactionLayout = new FlowLayout();
		panelTransactionLayout.setVgap(0);
		panelTransaction.setLayout(panelTransactionLayout);
		
		panelItemList = CommonComponentFactory.createBorderedAppendableJPanel();
		panelItemList.setWidth(732);
		
		jspItemList = new JScrollPane(panelItemList);
		jspItemList.setPreferredSize(new Dimension(732,453));
		jspItemList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jspItemList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jspItemList.setBorder(new LineBorder(Color.BLACK));
		panelTotal = CommonComponentFactory.createBorderedJPanel();
		
		JPanel panelListSelect = CommonComponentFactory.createBorderedJPanel();
		EposUtils.setFlowLayoutGaps(panelListSelect, 1, 1);
		panelListSelect.setPreferredSize(new Dimension(732,50));
		panelListSelect.setBackground(Color.CYAN);
		
		JPanel panelCheckbox = CommonComponentFactory.createBorderedJPanel(new Dimension(69,46));
		EposUtils.setFlowLayoutGaps(panelCheckbox, 5, 12);
		toggleAllCheckBox = new JCheckBox();
		toggleAllCheckBox.setBackground(Color.CYAN);
		toggleAllCheckBox.addActionListener(this);
		panelCheckbox.add(toggleAllCheckBox);
		panelCheckbox.setBackground(Color.CYAN);
		
		JPanel panelOptions = CommonComponentFactory.createBorderedJPanel(new Dimension(656,46));
		bModItems = CommonComponentFactory.createJButton("Modify selected items", new Dimension(200,34));
		bModItems.addActionListener(this);
		panelOptions.add(bModItems);
		panelOptions.setBackground(Color.CYAN);
		
		panelListSelect.add(panelCheckbox);
		panelListSelect.add(panelOptions);
				
		panelItemList.setBackground(Color.WHITE);
		
		panelTotal.setPreferredSize(new Dimension(732,100));
		
		panelTransaction.add(panelListSelect);
		panelTransaction.add(jspItemList);
		panelTransaction.add(panelTotal);
		
		//subtotals panel
		
		GridBagConstraints subTotalConstraints = new GridBagConstraints();
		
		panelTotal.setLayout(new GridBagLayout());
		
		//gaps round the edges
		subTotalConstraints.insets = new Insets(1,2,1,2);
		//attach to page start
		subTotalConstraints.anchor = GridBagConstraints.PAGE_START;
		//default buttons to fill grid horizontally
		subTotalConstraints.fill = GridBagConstraints.HORIZONTAL;
		//make sure items vertically fill the space:
		subTotalConstraints.ipady = 100;
		
		panelSubTotalLabel = CommonComponentFactory.createHeadingJPanel("Subtotal",new Font("Arial", Font.BOLD, 24));
		panelNumItems = CommonComponentFactory.createHeadingJPanel("0 Items",new Font("Arial", Font.BOLD, 18));
		panelSubTotal = CommonComponentFactory.createHeadingJPanel("£0.00", new Font("Arial", Font.BOLD, 28), new Color(0,150,0));
		
		subTotalConstraints.weightx=1;
		subTotalConstraints.weighty=1;
		
		//start from 0,0
		subTotalConstraints.gridx = 0;
		subTotalConstraints.gridy = 0;
		
		panelTotal.add(panelSubTotalLabel, subTotalConstraints);
		subTotalConstraints.gridx++;
		panelTotal.add(panelNumItems, subTotalConstraints);
		subTotalConstraints.gridx++;
		panelTotal.add(panelSubTotal, subTotalConstraints);
		
		/*
		 * menu panel
		 */
		
		panelMenu.setLayout(new GridBagLayout());
		GridBagConstraints menuConstraints = new GridBagConstraints();
		
		//gaps round the edges
		menuConstraints.insets = new Insets(1,2,1,2);
		//attach to page start
		menuConstraints.anchor = GridBagConstraints.PAGE_START;
		//default buttons to fill grid horizontally
		menuConstraints.fill = GridBagConstraints.HORIZONTAL;
		//make sure items vertically fill the space:
		menuConstraints.ipady = 40;
		
		//not sure what these too affect default component sizes???
		menuConstraints.weightx = 1;
		menuConstraints.weighty = 1;
		
		//first row - select item button
		menuConstraints.gridy = 0;
		
		menuConstraints.gridx = 0;//first column
		bSelectItem = new JButton("Select Item");
		bSelectItem.setPreferredSize(new Dimension(0,50));
		panelMenu.add(bSelectItem, menuConstraints);
		
		
		menuConstraints.gridy++;//next row - payment options header
		
		JPanel panelFuncHeadings = CommonComponentFactory.createHeadingJPanel("Payment Functions");	
		panelMenu.add(panelFuncHeadings, menuConstraints);
		
		menuConstraints.gridy++;//next row - payment options cash
		
		bPayCash = new JButton("Cash");
		panelMenu.add(bPayCash, menuConstraints);	
		
		menuConstraints.gridy++;//next row - payment options card
		
		bPayCard = new JButton("Card");
		panelMenu.add(bPayCard, menuConstraints);
		
		menuConstraints.gridy++;//next row - payment options coupon
		
		bPayCoupon = new JButton("Coupon");
		panelMenu.add(bPayCoupon, menuConstraints);
		
		menuConstraints.gridy++;//next row - payment options cheque
		
		bPayCheque = new JButton("Cheque");
		panelMenu.add(bPayCheque, menuConstraints);
		
		menuConstraints.gridy++;//next row - discount card button
		
		bCreditNote = new JButton("Credit Note/Gift Voucher");
		panelMenu.add(bCreditNote, menuConstraints);
		
		menuConstraints.gridy++;//next row - other options header
		
		JPanel panelOtherOptsHeadings = CommonComponentFactory.createHeadingJPanel("Other Functions");	
		panelMenu.add(panelOtherOptsHeadings, menuConstraints);
		
		menuConstraints.gridy++;//next row - return item button
		
		bReturnItem= new JButton("Returned Item");
		panelMenu.add(bReturnItem, menuConstraints);
		
		menuConstraints.gridy++;//next row - management button
		
		bManagement = new JButton("Management Functions");
		panelMenu.add(bManagement, menuConstraints);
		
		menuConstraints.gridy++;//next row - management button
		
		bLogOut = new JButton("Logout");
		panelMenu.add(bLogOut, menuConstraints);
		
		/*
		 * add listeners
		 */
		
		bSelectItem.addActionListener(this);
		bPayCash.addActionListener(this);
		bPayCard.addActionListener(this);
		bPayCoupon.addActionListener(this);
		bPayCheque.addActionListener(this);
		bCreditNote.addActionListener(this);
		bReturnItem.addActionListener(this);
		bManagement.addActionListener(this);
		bLogOut.addActionListener(this);
		
		/*
		 * add main panels to form
		 */
		
		container.add(panelTop, BorderLayout.PAGE_START);
		container.add(panelTransaction, BorderLayout.CENTER);
		container.add(panelMenu, BorderLayout.LINE_END);
		
		/*
		 * rendering
		 */
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);		
		//Dimension d = Toolkit.getDefaultToolkit().getScreenSize();	//get screen resolution

		int sWidth = 1024, sHeight = 738;
		setSize(sWidth,sHeight);
		//my resolution is 1280 x 800
		//garys resolution is 1024x768
		//best resolution to use is screen width x screen height-30

		//put in horizontal centre:
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();	//get screen resolution
		setLocation((d.width-sWidth)/2, 0);	//centre form
		
		if(d.height>=800)
		{
			setLocation((d.width-sWidth)/2, (d.height-sHeight)/2);	//centre form
		}
		
		setVisible(true);//display screen
	}



	@Override
	public void addListener(TransactionEventListener listener) 
	{
		listeners.add(listener);		
	}
	
	private void notifyListeners(TransactionEvent event)
	{
		for(TransactionEventListener tel: listeners)
		{
			tel.onTransactionEvent(event);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		//identify event and wrap in appropriate TransactionEvent
		if(e.getSource() == bSelectItem)
		{
			TransactionEvent newEvt = new TransactionEvent();
			newEvt.setType(TransactionEvent.Type.SELECT_ITEM);
			notifyListeners(newEvt);
		}
		else if(e.getSource() == bPayCash)
		{
			TransactionEvent newEvt = new TransactionEvent();
			newEvt.setType(TransactionEvent.Type.PAYMENT);
			newEvt.setPaymentMethod(TransactionEvent.PaymentMethod.CASH);
			notifyListeners(newEvt);
		}
		else if(e.getSource() == bPayCard)
		{
			TransactionEvent newEvt = new TransactionEvent();
			newEvt.setType(TransactionEvent.Type.PAYMENT);
			newEvt.setPaymentMethod(TransactionEvent.PaymentMethod.CARD);
			notifyListeners(newEvt);
		}
		else if(e.getSource() == bPayCoupon)
		{
			TransactionEvent newEvt = new TransactionEvent();
			newEvt.setType(TransactionEvent.Type.PAYMENT);
			newEvt.setPaymentMethod(TransactionEvent.PaymentMethod.COUPON);
			notifyListeners(newEvt);
		}
		else if(e.getSource() == bPayCheque)
		{
			TransactionEvent newEvt = new TransactionEvent();
			newEvt.setType(TransactionEvent.Type.PAYMENT);
			newEvt.setPaymentMethod(TransactionEvent.PaymentMethod.CHEQUE);
			notifyListeners(newEvt);
		}
		else if(e.getSource() == bCreditNote)
		{
			TransactionEvent newEvt = new TransactionEvent();
			newEvt.setType(TransactionEvent.Type.CREDIT_NOTE);
			notifyListeners(newEvt);
		}
		else if(e.getSource() == bReturnItem)
		{
			TransactionEvent newEvt = new TransactionEvent();
			newEvt.setType(TransactionEvent.Type.RETURN_ITEM);
			notifyListeners(newEvt);
		}
		else if(e.getSource() == bManagement)
		{
			TransactionEvent newEvt = new TransactionEvent();
			newEvt.setType(TransactionEvent.Type.MANAGEMENT_MENU);
			notifyListeners(newEvt);
		}
		else if(e.getSource() == bLogOut)
		{
			TransactionEvent newEvt = new TransactionEvent();
			newEvt.setType(TransactionEvent.Type.LOG_OUT);
			notifyListeners(newEvt);
		}
		else if(e.getSource() == bModItems)
		{
			/*
			 * get all selected indices
			 */
			List<Integer> selectedItems = new ArrayList<Integer>();
			
			for(SelectedItemPanel selectedItemPanel: itemPanels)
			{
				if(selectedItemPanel.isSelected())
				{
					selectedItems.add(selectedItemPanel.getEposPurchaseViewBean().getTransactionIndex());
				}
			}
			
			/*
			 * convert the list to an int[]
			 */
			int[] selectedItemsArray = new int[selectedItems.size()];
			
			int count=0;
			for(Integer index: selectedItems)
			{
				selectedItemsArray[count++] = index;
			}
			
			TransactionEvent newEvt = new TransactionEvent();
			newEvt.setType(TransactionEvent.Type.MODIFY_ITEMS);
			newEvt.setTransactionIndexArray(selectedItemsArray);
			notifyListeners(newEvt);
		}		
		else if(e.getSource() == toggleAllCheckBox)
		{
			toggleAll(toggleAllCheckBox.isSelected());
		}
		else if(e.getSource() == bAbout)
		{
			JOptionPane.showMessageDialog(this, 
					"ePOS v1.0 © Jonathan Devine and Gary Needham 2011.", "About ePOS...", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * toggles whether all items are selected or not, ignores void items
	 */
	private void toggleAll(boolean flag) 
	{

		for(SelectedItemPanel panel: itemPanels)
		{
			if(panel.getEposPurchaseViewBean().getMode() != EposPurchaseViewBean.Mode.PURCHASE_VOID)
			{
				panel.setSelected(flag);
			}		
		}
	}

	@Override
	public void close() 
	{
		dispose();	
	}
	
	@Override
	public void cancelPayment(EposPaymentViewBean eposPaymentViewBean) 
	{
		int result = JOptionPane.showConfirmDialog(this, 
				"Cancel this " + Utils.getPaymentTypeAsString(eposPaymentViewBean) + " payment for " + EposUtils.formatCurrency(eposPaymentViewBean.getValue()) + "?", 
				"Confirm", 
				JOptionPane.OK_CANCEL_OPTION);
		
		if(result == JOptionPane.OK_OPTION)
		{
			TransactionEvent evt = new TransactionEvent();
			evt.setType(TransactionEvent.Type.CANCEL_PAYMENT);
			evt.setTransactionIndex(eposPaymentViewBean.getTransactionIndex());
			notifyListeners(evt);
		}
		
	}

	private void refreshSubtotalDisplay(int numItems, double subtotal) 
	{
		String newText = null;
		if(numItems == 1)
		{
			newText = "1 Item";
		}
		else
		{
			newText = numItems + " Items";
		}

		panelNumItems.getLabel().setText(newText);
		
		/*
		 * set new subtotal text
		 */
		
		panelSubTotal.getLabel().setText(EposUtils.formatCurrency(subtotal));		
	}

	@Override
	public void resetSelectedItems() 
	{
		toggleAllCheckBox.setSelected(false);

		for(SelectedItemPanel panel: itemPanels)
		{
			panel.setSelected(false);
		}
	}

	private void resetDisplay() 
	{
		refreshSubtotalDisplay(0, 0);
		panelItemList.clear();
		
		if(EposContext.getPreferences().isTestMode())
		{
			panelItemList.append(getTestModePanel());
		}
		
		itemPanels = new ArrayList<SelectedItemPanel>();
	}

	@Override
	public void renderTransaction(TransactionViewBean transactionViewBean) 
	{
		resetDisplay();
		
		for(TransactionItemViewBean transactionItemViewBean: transactionViewBean.getTransactionItemViewBeans())
		{
			if(transactionItemViewBean instanceof EposPurchaseViewBean)
			{
				renderPurchase((EposPurchaseViewBean)transactionItemViewBean);
			}
			else if(transactionItemViewBean instanceof EposPaymentViewBean)
			{
				renderPayment((EposPaymentViewBean)transactionItemViewBean);
			}
		}
		
		for(DiscountViewBean discount: transactionViewBean.getDiscountViewBeans())
		{
			renderDiscount(discount);
		}
		
		/*
		 * update subtotals
		 */
		
		refreshSubtotalDisplay(transactionViewBean.getItemsInTransaction(), transactionViewBean.getSubtotal());
	}

	private void renderPurchase(EposPurchaseViewBean eposPurchaseViewBean)
	{
		SelectedItemPanel newPanel = new SelectedItemPanel(eposPurchaseViewBean);
		panelItemList.append(newPanel);
		itemPanels.add(newPanel);
	}
	
	private void renderPayment(EposPaymentViewBean eposPaymentViewBean)
	{
		switch(eposPaymentViewBean.getType())
		{
			case CARD:
			case CHEQUE:
			case CASH:
			case COUPON:
				EposPaymentPanel newPanel = new EposPaymentPanel(eposPaymentViewBean);
				newPanel.setListener(this);
				panelItemList.append(newPanel);
				break;
			case RETURNED_ITEM:
				EposGarmentReturnPanel newReturnPanel = new EposGarmentReturnPanel((EposPaymentReturnedItemViewBean)eposPaymentViewBean);
				panelItemList.append(newReturnPanel);
				newReturnPanel.addListener(this);
				break;
			case CREDIT_NOTE:
				EposCreditNotePanel newCreditNotePanel = new EposCreditNotePanel((EposCreditNotePaymentViewBean)eposPaymentViewBean);
				panelItemList.append(newCreditNotePanel);
				newCreditNotePanel.setListener(this);
				break;
			default:
				throw new IllegalStateException("Invalid Payment Type");
		}
	}
	
	private void renderDiscount(DiscountViewBean discount) 
	{
		panelItemList.append(new DiscountPanel(discount));
	}
	
	@Override
	public void cancelReturn(EposPaymentReturnedItemViewBean eposPaymentReturnedItemViewBean) 
	{
		TransactionEvent evt = new TransactionEvent();
		evt.setType(TransactionEvent.Type.CANCEL_PAYMENT);
		evt.setTransactionIndex(eposPaymentReturnedItemViewBean.getTransactionIndex());
		notifyListeners(evt);		
	}

	@Override
	public void showError(String message) 
	{
		JOptionPane.showMessageDialog(this, message, "Error!", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void setUserDisplay(String displayName) 
	{
		lDisplayName.setText(displayName);
	}
	
	private JPanel getTestModePanel()
	{
		JPanel panel = CommonComponentFactory.createHeadingJPanel("TEST MODE", new Font("Comic Sans MS", Font.BOLD, 18), Color.black);
		panel.setBackground(Color.GREEN);
		panel.setPreferredSize(new Dimension(707,50));
		
		return panel;
	}
}



