package jjdevine.epos.guiImpl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import jjdevine.epos.beans.EposPaymentReturnedItemViewBean;
import jjdevine.epos.common.CommonComponentFactory;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.events.EposGarmentReturnPanelListener;

public class EposGarmentReturnPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 3867072268216493751L;
	private EposPaymentReturnedItemViewBean eposPaymentReturnedItemViewBean;
	private JPanel panelButton, panelDetailsWrapper, panelDetails, panelValue;
	private JPanel panelDetailsLabels, panelDetailsValues;
	private JPanel panelValueMessage, panelValueValue;
	private JButton bCancel;
	private JLabel lGCode, lBrand, lStyle, lColour, lSize;
	private JLabel lGCodeVal, lBrandVal, lStyleVal, lColourVal, lSizeVal;
	private Color colGreen = new Color(0,150,0);
	private List<EposGarmentReturnPanelListener> listeners = new ArrayList<EposGarmentReturnPanelListener>(); 


	public EposGarmentReturnPanel(EposPaymentReturnedItemViewBean eposPaymentReturnedItemViewBean) 
	{
		this.eposPaymentReturnedItemViewBean = eposPaymentReturnedItemViewBean;
		
		setBorder(new LineBorder(Color.BLACK));
		setPreferredSize(new Dimension(706,100));
		EposUtils.setFlowLayoutGaps(this, 1, 1);
		
		/*
		 * sub-panels
		 */
		
		panelButton = CommonComponentFactory.createUnborderedJPanel(new Dimension(39,96));
		panelDetailsWrapper = CommonComponentFactory.createBorderedJPanel(new Dimension(660,96));
		panelDetails = CommonComponentFactory.createBorderedJPanel(new Dimension(400,92));
		panelValue = CommonComponentFactory.createBorderedJPanel(new Dimension(255,92));
		
		EposUtils.setFlowLayoutGaps(panelDetailsWrapper, 1, 1);
		
		/*
		 * panel button
		 */
		
		java.net.URL imageURLcross = EposPaymentPanel.class.getResource("/resources/red_cross.GIF");
		bCancel = CommonComponentFactory.createJButton(imageURLcross);
		bCancel.setPreferredSize(new Dimension(35, 35));
		bCancel.addActionListener(this);
		
		EposUtils.setFlowLayoutGaps(panelButton, 1, 28);
		panelButton.add(bCancel);
		
		/*
		 * panel details
		 */
		
		EposUtils.setFlowLayoutGaps(panelDetails, 1, 1);
		
		panelDetailsLabels = CommonComponentFactory.createBorderedJPanel(new Dimension(197,88));
		panelDetailsValues = CommonComponentFactory.createBorderedJPanel(new Dimension(198,88));
		
		panelDetails.add(panelDetailsLabels);
		panelDetails.add(panelDetailsValues);
		
		//panel details labels
		
		EposUtils.setFlowLayoutGaps(panelDetailsLabels, 1, 4);
		
		Dimension dimLabel = new Dimension(194,12);
		lGCode = CommonComponentFactory.createJLabel("Garment Code: ", dimLabel);
		lBrand = CommonComponentFactory.createJLabel("Brand: ", dimLabel);
		lStyle = CommonComponentFactory.createJLabel("Style: ", dimLabel);
		lColour = CommonComponentFactory.createJLabel("Colour: ", dimLabel);
		lSize = CommonComponentFactory.createJLabel("Size: ", dimLabel);
		
		panelDetailsLabels.add(lGCode);
		panelDetailsLabels.add(lBrand);
		panelDetailsLabels.add(lStyle);
		panelDetailsLabels.add(lColour);
		panelDetailsLabels.add(lSize);
		
		//panel details values
			
		EposUtils.setFlowLayoutGaps(panelDetailsValues, 1, 4);
		
		lGCodeVal = CommonComponentFactory.createJLabel(eposPaymentReturnedItemViewBean.getGarmentCode(), dimLabel);
		lBrandVal = CommonComponentFactory.createJLabel(eposPaymentReturnedItemViewBean.getBrand(), dimLabel);
		lStyleVal = CommonComponentFactory.createJLabel(eposPaymentReturnedItemViewBean.getStyle(), dimLabel);
		lColourVal = CommonComponentFactory.createJLabel(eposPaymentReturnedItemViewBean.getColour(), dimLabel);
		lSizeVal = CommonComponentFactory.createJLabel(eposPaymentReturnedItemViewBean.getSize(), dimLabel);
		
		panelDetailsValues.add(lGCodeVal);
		panelDetailsValues.add(lBrandVal);
		panelDetailsValues.add(lStyleVal);
		panelDetailsValues.add(lColourVal);
		panelDetailsValues.add(lSizeVal);
		
		/*
		 * panel value
		 */
		
		EposUtils.setFlowLayoutGaps(panelValue, 1, 1);
		
		panelValueMessage = CommonComponentFactory.createHeadingJPanel("*Returned Item*", 
				new Font("Arial", Font.BOLD, 18), Color.RED);
		panelValueMessage.setPreferredSize(new Dimension(251,44));
		
		if(eposPaymentReturnedItemViewBean.isCancelled())
		{
			bCancel.setEnabled(false);
			panelValueValue = CommonComponentFactory.createHeadingJPanel("*VOID*", new Font("Arial", Font.BOLD, 18), 
					Color.RED);
		}
		else
		{
			panelValueValue = CommonComponentFactory.createHeadingJPanel("-" + 
					EposUtils.formatCurrency(eposPaymentReturnedItemViewBean.getValue()), new Font("Arial", Font.BOLD, 22), 
					colGreen);
		}
		
		panelValueValue.setPreferredSize(new Dimension(251,43));
		panelValue.add(panelValueMessage);
		panelValue.add(panelValueValue);
		
		/*
		 * render panels
		 */
		
		add(panelButton);
		add(panelDetailsWrapper);
		panelDetailsWrapper.add(panelDetails);
		panelDetailsWrapper.add(panelValue);
	}

	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		for(EposGarmentReturnPanelListener listener: listeners)
		{
			listener.cancelReturn(eposPaymentReturnedItemViewBean);
		}
	}

	public EposPaymentReturnedItemViewBean getEposPaymentReturnedItemViewBean() {
		return eposPaymentReturnedItemViewBean;
	}

	public void setEposPaymentReturnedItemViewBean(
			EposPaymentReturnedItemViewBean eposPaymentReturnedItemViewBean) {
		this.eposPaymentReturnedItemViewBean = eposPaymentReturnedItemViewBean;
	}
	
	public void addListener(EposGarmentReturnPanelListener listener)
	{
		listeners.add(listener);
	}

}

