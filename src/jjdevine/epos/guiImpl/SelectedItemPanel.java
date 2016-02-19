package jjdevine.epos.guiImpl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import jjdevine.epos.beans.EposPurchaseViewBean;
import jjdevine.epos.common.CommonComponentFactory;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.customComponents.HeadingJPanel;

public class SelectedItemPanel extends JPanel implements ActionListener 
{
	private static final long serialVersionUID = -7192443806742138875L;
	
	private EposPurchaseViewBean eposPurchaseViewBean;
	
	private JPanel checkboxPanel, panelDesc, panelPrice;
	private JCheckBox checkBox;
	private JLabel lGCode, lBrand, lStyle, lColour, lSize;
	private JLabel lGCodeVal, lBrandVal, lStyleVal, lColourVal, lSizeVal;
	private Color colGreen = new Color(0,150,0);
	private HeadingJPanel panelRetPriceVal, panelMemberPriceVal, panelSalePriceLabel, panelSalePriceVal;
	
	public SelectedItemPanel(EposPurchaseViewBean eposPurchaseViewBean) 
	{	
		this.eposPurchaseViewBean = eposPurchaseViewBean;
		
		setBorder(new LineBorder(Color.BLACK));
		setPreferredSize(new Dimension(706,100));
		
		EposUtils.setFlowLayoutGaps(this, 1, 1);

		
		/*
		 * sub panels
		 */
		
		checkboxPanel = CommonComponentFactory.createBorderedJPanel(new Dimension(56,96));
		panelDesc = CommonComponentFactory.createBorderedJPanel(new Dimension(320,96));
		panelPrice = CommonComponentFactory.createBorderedJPanel(new Dimension(320,96));
		
		add(checkboxPanel);
		add(panelDesc);
		add(panelPrice);
		
		/*
		 * checkbox panel
		 */
		
		FlowLayout layout = new FlowLayout();
		layout.setVgap(35);
		checkboxPanel.setLayout(layout);
		
		checkBox = CommonComponentFactory.createJCheckBox();
		checkboxPanel.add(checkBox);
		
		checkBox.addActionListener(this);
		
		/*
		 * desc panel
		 */
		
		JPanel column1 = CommonComponentFactory.createBorderedJPanel(new Dimension(100, 92));
		JPanel column2 = CommonComponentFactory.createBorderedJPanel(new Dimension(215, 92));
		Dimension dimLabel = new Dimension(94,17);

		lGCode = CommonComponentFactory.createJLabel("Garment Code: ", dimLabel);
		lBrand = CommonComponentFactory.createJLabel("Brand: ", dimLabel);
		lStyle = CommonComponentFactory.createJLabel("Style: ", dimLabel);
		lColour = CommonComponentFactory.createJLabel("Colour: ", dimLabel);
		lSize = CommonComponentFactory.createJLabel("Size: ", dimLabel);
		
		column1.add(lGCode);
		column1.add(lBrand);
		column1.add(lStyle);
		column1.add(lColour);
		column1.add(lSize);
		
		Dimension dimLabel2 = new Dimension(209,17);
		
		lGCodeVal = CommonComponentFactory.createJLabel(eposPurchaseViewBean.getGarmentCode(), dimLabel2);
		lBrandVal = CommonComponentFactory.createJLabel(eposPurchaseViewBean.getBrand(), dimLabel2);
		lStyleVal = CommonComponentFactory.createJLabel(eposPurchaseViewBean.getStyle(), dimLabel2);
		lColourVal = CommonComponentFactory.createJLabel(eposPurchaseViewBean.getColour(), dimLabel2);
		lSizeVal = CommonComponentFactory.createJLabel(eposPurchaseViewBean.getSize(), dimLabel2);
		
		column2.add(lGCodeVal);
		column2.add(lBrandVal);
		column2.add(lStyleVal);
		column2.add(lColourVal);
		column2.add(lSizeVal);
		
		panelDesc.add(column1);
		panelDesc.add(column2);
		
		EposUtils.setFlowLayoutGaps(column1, 2, 1);
		EposUtils.setFlowLayoutGaps(column2, 2, 1);
		EposUtils.setFlowLayoutGaps(panelDesc, 1, 1);
		
		/*
		 * price panel
		 */
		
		EposUtils.setFlowLayoutGaps(panelPrice, 1, 1);
		
		Dimension dimPricePanel = new Dimension(316,26);
		
		JPanel panelRetPrice = CommonComponentFactory.createBorderedJPanel(dimPricePanel);
		JPanel panelMemberPrice = CommonComponentFactory.createBorderedJPanel(dimPricePanel);
		JPanel panelSalePrice = CommonComponentFactory.createBorderedJPanel(new Dimension(316,38));
		
		panelPrice.add(panelRetPrice);
		panelPrice.add(panelMemberPrice);
		panelPrice.add(panelSalePrice);
		
		EposUtils.setFlowLayoutGaps(panelRetPrice, 1, 1);
		EposUtils.setFlowLayoutGaps(panelMemberPrice, 1, 1);
		EposUtils.setFlowLayoutGaps(panelSalePrice, 1, 1);
		
		/*
		 * panel ret price
		 */
		
		Font font = new Font("Arial", Font.BOLD, 12);
		
		JPanel panelRetPriceLabel = CommonComponentFactory.createHeadingJPanel("Retail Price", font);
		panelRetPriceLabel.setPreferredSize(new Dimension(220,22));
		
		panelRetPriceVal = CommonComponentFactory.createHeadingJPanel(
				EposUtils.formatCurrency(eposPurchaseViewBean.getRetailPrice()), font);
		panelRetPriceVal.setPreferredSize(new Dimension(91,22));
		
		panelRetPrice.add(panelRetPriceLabel);
		panelRetPrice.add(panelRetPriceVal);
		
		/*
		 * panel mem price
		 */
		
		JPanel panelMemberPriceLabel = CommonComponentFactory.createHeadingJPanel("Member Price", font);
		panelMemberPriceLabel.setPreferredSize(new Dimension(220,22));
		
		panelMemberPriceVal = CommonComponentFactory.createHeadingJPanel(
				EposUtils.formatCurrency(eposPurchaseViewBean.getMemberPrice()), font);
		panelMemberPriceVal.setPreferredSize(new Dimension(91,22));
		
		panelMemberPrice.add(panelMemberPriceLabel);
		panelMemberPrice.add(panelMemberPriceVal);
		
		/*
		 * panel sale price
		 */
		
		Font saleFont = new Font("Arial", Font.BOLD, 22);
		Font saleFont2 = new Font("Arial", Font.BOLD, 18);
		
		panelSalePriceLabel = CommonComponentFactory.createHeadingJPanel("", saleFont2, Color.RED);
		panelSalePriceLabel.setPreferredSize(new Dimension(220,34));
		
		panelSalePriceVal = CommonComponentFactory.createHeadingJPanel("", 
				saleFont, colGreen);
		panelSalePriceVal.setPreferredSize(new Dimension(91,34));
		
		panelSalePrice.add(panelSalePriceLabel);
		panelSalePrice.add(panelSalePriceVal);
		
		formatByPurchaseMode(eposPurchaseViewBean.getMessage());
	}


	public void formatByPurchaseMode(String displayMessage) 
	{	
		Color memberPriceColour = null;
		Color retPriceColour = null;
		String message = "";
		if(displayMessage == null)
		{
			displayMessage = "";
		}
		
		switch(eposPurchaseViewBean.getMode())
		{
			case NORMAL:
				retPriceColour = colGreen;
				memberPriceColour = Color.LIGHT_GRAY;
				break;
			case NORMAL_DISCOUNTED:
				message = displayMessage;
				retPriceColour = Color.LIGHT_GRAY;
				memberPriceColour = Color.LIGHT_GRAY;
				break;
				
			case MEMBER:
				retPriceColour = Color.LIGHT_GRAY;
				memberPriceColour = colGreen;
				break;
			case MEMBER_DISCOUNTED:
				memberPriceColour = Color.LIGHT_GRAY;
				retPriceColour = Color.LIGHT_GRAY;
				message = displayMessage;
				break;	

			case PURCHASE_VOID:
				checkBox.setEnabled(false);
				retPriceColour = Color.LIGHT_GRAY;
				memberPriceColour = Color.LIGHT_GRAY;
				message = "*VOID*";
				break;
		}
		
		panelRetPriceVal.getLabel().setForeground(retPriceColour);
		panelMemberPriceVal.getLabel().setForeground(memberPriceColour);
		panelSalePriceLabel.getLabel().setText(message);
		
		if(eposPurchaseViewBean.getMode() != EposPurchaseViewBean.Mode.PURCHASE_VOID)
		{
			panelSalePriceVal.getLabel().setText(EposUtils.formatCurrency(eposPurchaseViewBean.getSalePrice()));
		}
	}

	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		if(checkBox.isSelected()) //make sure selected value is accurate
		{
			eposPurchaseViewBean.setSelected(true);
		}
		else
		{
			eposPurchaseViewBean.setSelected(false);
		}
	}
	
	/**
	 * change value of item to selected or not
	 * @param selected
	 */
	public void setSelected(boolean selected)
	{
		checkBox.setSelected(selected);
		eposPurchaseViewBean.setSelected(selected);
	}
	
	public boolean isSelected()
	{
		return checkBox.isSelected();
	}


	public EposPurchaseViewBean getEposPurchaseViewBean() {
		return eposPurchaseViewBean;
	}


	public void setEposPurchaseViewBean(EposPurchaseViewBean eposPurchaseViewBean) {
		this.eposPurchaseViewBean = eposPurchaseViewBean;
	}


}

