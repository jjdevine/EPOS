package jjdevine.epos.guiImpl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import jjdevine.epos.beans.DiscountViewBean;
import jjdevine.epos.common.CommonComponentFactory;
import jjdevine.epos.common.EposUtils;

public class DiscountPanel extends JPanel 
{
	private static final long serialVersionUID = -7208637482600366924L;
	private static final Color colAmount = new Color(0,150,0);
	private JPanel panelHeader, panelInfo, panelAmount;
	
	public DiscountPanel(DiscountViewBean discountViewBean)
	{
		//TODO: update receipt printing for global discounts
		setBorder(new LineBorder(Color.BLACK));
		setPreferredSize(new Dimension(706,51));
		
		EposUtils.setFlowLayoutGaps(this, 1, 1);
		
		/*
		 * create components
		 */
		
		Font paymentFont = new Font("Arial", Font.BOLD, 22);
		
		panelHeader = CommonComponentFactory.createHeadingJPanel("Discount", new Font("Arial", Font.BOLD, 18), Color.RED);
		panelHeader.setPreferredSize(new Dimension(100,47));
		panelInfo = CommonComponentFactory.createHeadingJPanel(discountViewBean.getDescription(), 
				new Font("Comic Sans MS", Font.BOLD, 18), Color.BLUE);
		panelInfo.setPreferredSize(new Dimension(398,47));
		panelAmount = CommonComponentFactory.createHeadingJPanel(
				"-" + EposUtils.formatCurrency(discountViewBean.getValue()), 
				paymentFont, colAmount);
		panelAmount.setPreferredSize(new Dimension(202,47));
		
		/*
		 * add subpanels to container
		 */
		
		EposUtils.setFlowLayoutGaps(panelInfo, 1, 1);
		
		add(panelHeader);
		add(panelInfo);
		add(panelAmount);
			
	}

}
