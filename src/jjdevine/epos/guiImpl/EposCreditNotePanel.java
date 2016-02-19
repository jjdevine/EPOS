package jjdevine.epos.guiImpl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import jjdevine.epos.beans.EposCreditNotePaymentViewBean;
import jjdevine.epos.common.CommonComponentFactory;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.customComponents.HeadingJPanel;
import jjdevine.epos.events.EposPaymentPanelListener;

public class EposCreditNotePanel extends JPanel implements ActionListener 
{
	private static final long serialVersionUID = 9114613255073300113L;
	private EposCreditNotePaymentViewBean viewBean;
	private JButton bCancel;
	private HeadingJPanel panelHeader, panelInfo, panelValue;
	private static final Color colAmount = new Color(0,150,0);
	private EposPaymentPanelListener listener;
	
	public EposCreditNotePanel(EposCreditNotePaymentViewBean viewBean)
	{
		this.viewBean = viewBean;
		
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

		String infoText = null;
		
		if(viewBean.isGiftVoucher())
		{
			infoText = "Gift Voucher";
		}
		else
		{
			infoText = "Credit Note";
		}
		
		panelInfo = CommonComponentFactory.createHeadingJPanel(infoText , labelFont);
		panelInfo.setPreferredSize(new Dimension(200, componentHeight));
		
		panelValue = CommonComponentFactory.createHeadingJPanel(
				"-" + EposUtils.formatCurrency(viewBean.getValue()), 
				new Font("Arial", Font.BOLD, 22), 
				colAmount);
		panelValue.setPreferredSize(new Dimension(200, componentHeight));
		
		if(viewBean.isCancelled())
		{
			panelHeader.getLabel().setText("Payment Cancelled");
			panelValue.getLabel().setText("£0");
			bCancel.setEnabled(false);
		}
		//TODO: continue testing payments with credit notes/gift vouchers
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
			listener.cancelPayment(viewBean);
		}
	}

	public EposPaymentPanelListener getListener() {
		return listener;
	}

	public void setListener(EposPaymentPanelListener listener) 
	{
		this.listener = listener;
	}
}
