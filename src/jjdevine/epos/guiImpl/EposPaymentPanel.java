package jjdevine.epos.guiImpl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import jjdevine.epos.beans.EposPaymentViewBean;
import jjdevine.epos.common.CommonComponentFactory;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.customComponents.HeadingJPanel;
import jjdevine.epos.events.EposPaymentPanelListener;
import jjdevine.epos.utils.Utils;

public class EposPaymentPanel extends JPanel implements ActionListener 
{
	private static final long serialVersionUID = 6922998512834426727L;
	private EposPaymentViewBean eposPaymentViewBean;
	private JButton bCancel;
	private HeadingJPanel panelHeader, panelInfo, panelValue;
	private static final Color colAmount = new Color(0,150,0);
	private EposPaymentPanelListener listener;
	
	public EposPaymentPanel(EposPaymentViewBean eposPaymentViewBean) 
	{
		this.eposPaymentViewBean = eposPaymentViewBean;

		setBorder(new LineBorder(Color.BLACK));
		setPreferredSize(new Dimension(706,41));
		
		EposUtils.setFlowLayoutGaps(this, 5, 2);
		
		/*
		 * create components
		 */
		
		int componentHeight = 35;
		Font labelFont = new Font("Arial", Font.BOLD, 16);
		
		java.net.URL imageURLcross = EposPaymentPanel.class.getResource("/resources/red_cross.GIF");
		bCancel = CommonComponentFactory.createJButton(imageURLcross);
		bCancel.setPreferredSize(new Dimension(35, componentHeight));
		bCancel.addActionListener(this);
		
		panelHeader = CommonComponentFactory.createHeadingJPanel("Payment Received", labelFont);
		panelHeader.setPreferredSize(new Dimension(248, componentHeight));
		
		String infoText = "Type : " + Utils.getPaymentTypeAsString(eposPaymentViewBean);
		
		panelInfo = CommonComponentFactory.createHeadingJPanel(infoText , labelFont);
		panelInfo.setPreferredSize(new Dimension(200, componentHeight));
		
		panelValue = CommonComponentFactory.createHeadingJPanel(
				"-" + EposUtils.formatCurrency(eposPaymentViewBean.getValue()), 
				new Font("Arial", Font.BOLD, 22), 
				colAmount);
		panelValue.setPreferredSize(new Dimension(200, componentHeight));
		
		if(eposPaymentViewBean.isCancelled())
		{
			panelHeader.getLabel().setText("Payment Cancelled");
			panelValue.getLabel().setText("£0");
			bCancel.setEnabled(false);
		}
		
		/*
		 * add components to panel
		 */
		
		add(bCancel);
		add(panelHeader);
		add(panelInfo);
		add(panelValue);
	}

	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		if (evt.getSource() == bCancel)
		{		
			listener.cancelPayment(eposPaymentViewBean);
		}
	}

	public EposPaymentPanelListener getListener() {
		return listener;
	}

	public void setListener(EposPaymentPanelListener listener) {
		this.listener = listener;
	}

	public EposPaymentViewBean getEposPaymentViewBean() {
		return eposPaymentViewBean;
	}

	public void setEposPaymentViewBean(EposPaymentViewBean eposPaymentViewBean) {
		this.eposPaymentViewBean = eposPaymentViewBean;
	}
	
}
