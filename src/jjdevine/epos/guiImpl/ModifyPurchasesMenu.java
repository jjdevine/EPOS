package jjdevine.epos.guiImpl;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import jjdevine.epos.EposContext;
import jjdevine.epos.common.CommonComponentFactory;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.beans.AdHocDiscount;
import jjdevine.epos.common.beans.Discount;
import jjdevine.epos.common.beans.DiscountReason;
import jjdevine.epos.common.customComponents.ManagedJFrame;
import jjdevine.epos.events.ModifyPurchasesEvent;
import jjdevine.epos.events.ModifyPurchasesEventListener;
import jjdevine.epos.transaction.data.svc.TransactionDataService;

public class ModifyPurchasesMenu extends ManagedJFrame implements ActionListener 
{
	private static final long serialVersionUID = -120002003927379846L;
	private int sWidth = 500, sHeight = 323;
	private int[] affectedPurchases;
	private JPanel panelHeader, panelNewPrice, panelDiscountPc, panelVoid, panelClose;
	private JLabel lNewPrice, lDiscountPc, lVoid, l£, lPc;
	private JTextField tNewPrice, tDiscountPc;
	private JRadioButton radioNewPrice, radioDiscountPc, radioVoid;
	private JButton bOK, bCancel;
	private List<ModifyPurchasesEventListener> listeners = new ArrayList<ModifyPurchasesEventListener>();

	public ModifyPurchasesMenu(int[] affectedPurchases)
	{
		super("Modify Purchases");	//form heading
		//create container to place components in:
		Container container = getContentPane();
		container.setLayout(new FlowLayout());	//set flow layout
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);	
		
		this.affectedPurchases = affectedPurchases;
		Dimension panelDim = new Dimension(485, 50);
		
		/*
		 * header panel
		 */
		
		panelHeader = CommonComponentFactory.createHeadingJPanel("Modify Purchases", new Font("Comic Sans MS", Font.BOLD, 20));
		panelHeader.setPreferredSize(new Dimension(485, 60));
		
		/*
		 * new price panel
		 */
		
		panelNewPrice = CommonComponentFactory.createBorderedJPanel(panelDim);
		FlowLayout panelLayout = new FlowLayout();
		panelLayout.setHgap(1);
		panelNewPrice.setLayout(panelLayout);
		
		radioNewPrice = CommonComponentFactory.createJRadioButton();
		lNewPrice = CommonComponentFactory.createCenteredJLabel("Set new price :", new Dimension(200,40));
		l£ = CommonComponentFactory.createJLabel("£");
		tNewPrice = CommonComponentFactory.createJTextField(new Dimension(100,30));
		
		panelNewPrice.add(radioNewPrice);
		panelNewPrice.add(lNewPrice);
		panelNewPrice.add(l£);
		panelNewPrice.add(tNewPrice);
		
		/*
		 * Discount % panel
		 */
		
		panelDiscountPc = CommonComponentFactory.createBorderedJPanel(panelDim);
		panelLayout = new FlowLayout();
		panelLayout.setHgap(1);
		panelDiscountPc.setLayout(panelLayout);
		
		radioDiscountPc = CommonComponentFactory.createJRadioButton();
		lDiscountPc = CommonComponentFactory.createCenteredJLabel("Discount by :", new Dimension(200,40));	
		tDiscountPc = CommonComponentFactory.createJTextField(new Dimension(100,30));
		lPc = CommonComponentFactory.createJLabel("%");
		
		panelDiscountPc.add(radioDiscountPc);
		panelDiscountPc.add(lDiscountPc);
		panelDiscountPc.add(tDiscountPc);
		panelDiscountPc.add(lPc);
		
		/*
		 * void item(s) panel
		 */
		
		panelVoid = CommonComponentFactory.createBorderedJPanel(panelDim);
		panelLayout = new FlowLayout();
		panelLayout.setHgap(1);
		panelVoid.setLayout(panelLayout);
		
		radioVoid = CommonComponentFactory.createJRadioButton();
		lVoid = CommonComponentFactory.createCenteredJLabel("Void Item(s)", new Dimension(200,40));
		
		panelVoid.add(radioVoid);
		panelVoid.add(lVoid);
		panelVoid.add(CommonComponentFactory.createJLabel("", new Dimension(110,30)));
		
		/*
		 * panel close
		 */
		
		panelClose = CommonComponentFactory.createBorderedJPanel(panelDim);
		
		Dimension dButton = new Dimension(235,38);
		bOK = CommonComponentFactory.createJButton("OK", dButton);
		bCancel = CommonComponentFactory.createJButton("Cancel", dButton);
		
		panelClose.add(bOK);
		panelClose.add(bCancel);
		
		/*
		 * add panels to container
		 */
		
		container.add(panelHeader);
		container.add(panelNewPrice);
		container.add(panelDiscountPc);
		container.add(panelVoid);
		container.add(panelClose);
		
		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(radioNewPrice);
		radioGroup.add(radioDiscountPc);
		radioGroup.add(radioVoid);
		
		bOK.addActionListener(this);
		bCancel.addActionListener(this);
		radioNewPrice.addActionListener(this);
		radioDiscountPc.addActionListener(this);
		radioVoid.addActionListener(this);
		
		//start with all options disabled..
		disableNewPricePanel();
		disableDiscountPcPanel();
		disableVoidPanel();
		
		/*
		 * render frame
		 */
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
			/*
			 * ascertain action type required
			 */
			
			if(radioNewPrice.isSelected())
			{
				//new flat price for item(s)
				String input = tNewPrice.getText().trim();
				double value = -1;
				try
				{
					value = Double.parseDouble(input);
				}
				catch(NumberFormatException nfe)
				{
					JOptionPane.showMessageDialog(this, input + " is not a valid price", "Invalid value", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				value = EposUtils.roundToTwoPlaces(value);
				
				AdHocDiscount discount = new AdHocDiscount();
				discount.setType(Discount.Type.FLAT_PRICE);
				discount.setValue(value);
				
				DiscountReason discountReason = getReasonForDiscount();	
				discount.setDescription(discountReason.getDescription());
				discount.setDiscountReasonId(discountReason.getId());

				/*
				 * notify listeners of change
				 */
				
				ModifyPurchasesEvent modEvt = new ModifyPurchasesEvent();
				modEvt.setAffectedPurchases(affectedPurchases);
				modEvt.setDiscount(discount);
				modEvt.setType(ModifyPurchasesEvent.Type.DISCOUNT);
				
				notifyListeners(modEvt);
				
			}
			else if(radioDiscountPc.isSelected())
			{
				//discount item by %
				String input = tDiscountPc.getText().trim();
				double value = -1;
				try
				{
					value = Double.parseDouble(input);
				}
				catch(NumberFormatException nfe)
				{
					JOptionPane.showMessageDialog(this, input + " is not a valid percentage", "Invalid value", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				AdHocDiscount discount = new AdHocDiscount();
				discount.setType(Discount.Type.PERCENTAGE);
				discount.setValue(value);
				
				DiscountReason discountReason = getReasonForDiscount();	
				discount.setDescription(discountReason.getDescription());
				discount.setDiscountReasonId(discountReason.getId());
							
				/*
				 * notify listeners of change
				 */
				
				ModifyPurchasesEvent modEvt = new ModifyPurchasesEvent();
				modEvt.setAffectedPurchases(affectedPurchases);
				modEvt.setDiscount(discount);
				modEvt.setType(ModifyPurchasesEvent.Type.DISCOUNT);
				
				notifyListeners(modEvt);
			}
			else if(radioVoid.isSelected())
			{
				/*
				 * notify listeners of change
				 */
				
				ModifyPurchasesEvent modEvt = new ModifyPurchasesEvent();
				modEvt.setAffectedPurchases(affectedPurchases);
				modEvt.setType(ModifyPurchasesEvent.Type.VOID);
				
				notifyListeners(modEvt);
			}
			else
			{
				//nothing selected
				JOptionPane.showMessageDialog(this, "Select an option or press Cancel", "Invalid Operation", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			dispose();
		} 
		else if(evt.getSource() == bCancel)
		{
			dispose();
		}
		else if(radioNewPrice.isSelected())
		{
			lNewPrice.setForeground(Color.BLACK);
			l£.setForeground(Color.BLACK);
			tNewPrice.setEnabled(true);
			
			disableDiscountPcPanel();
			disableVoidPanel();
		}
		else if(radioDiscountPc.isSelected())
		{
			lDiscountPc.setForeground(Color.BLACK);
			lPc.setForeground(Color.BLACK);
			tDiscountPc.setEnabled(true);
			
			disableNewPricePanel();
			disableVoidPanel();
		}
		else if(radioVoid.isSelected())
		{
			lVoid.setForeground(Color.BLACK);
			
			disableNewPricePanel();
			disableDiscountPcPanel();
		}	
	}
	
	private void disableNewPricePanel()
	{
		lNewPrice.setForeground(Color.GRAY);
		l£.setForeground(Color.GRAY);
		tNewPrice.setEnabled(false);
	}
	
	private void disableDiscountPcPanel()
	{
		lDiscountPc.setForeground(Color.GRAY);
		lPc.setForeground(Color.GRAY);
		tDiscountPc.setEnabled(false);
	}
	
	private void disableVoidPanel()
	{
		lVoid.setForeground(Color.GRAY);
	}
	
	public void addListener(ModifyPurchasesEventListener listener)
	{
		listeners.add(listener);
	}
	
	private void notifyListeners(ModifyPurchasesEvent evt)
	{
		for (ModifyPurchasesEventListener listener: listeners)
		{
			listener.onModifyPurchasesEvent(evt);
		}
	}
	
	private DiscountReason getReasonForDiscount()
	{
		TransactionDataService txDS = EposContext.getTransactionDataService();
		List<DiscountReason> discountReasons = txDS.getDiscountReasons();
		
		if (discountReasons.size() == 0)
		{
			return null;
		}
		
		String[] options = new String[discountReasons.size()];
		
		int index = 0;
		for(DiscountReason reason: discountReasons)
		{
			options[index++] = reason.getDescription();
		}
		
		String result = (String)JOptionPane.showInputDialog(null,
			    "Please enter the reason for price modification",
			    "Price modification",
			    JOptionPane.QUESTION_MESSAGE,
			    null,     //do not use a custom Icon
			    options,  //the titles of buttons
			    options[0]); //default button title
		
		if(result == null)
		{
			return null;
		}
		
		for(DiscountReason reason: discountReasons)
		{
			if (result.equals(reason.getDescription()))
			{
				return reason;
			}
		}
		
		return null;
	}

}

