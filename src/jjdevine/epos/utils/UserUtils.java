package jjdevine.epos.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import jjdevine.epos.common.CommonComponentFactory;
import jjdevine.epos.usermanagement.data.svc.beans.User;
import jjdevine.epos.usermanagement.data.svc.beans.User.AccessLevel;
import jjdevine.epos.usermanagement.view.beans.UserManagementViewBean;
import jjdevine.epos.usermanagement.view.gui.UserManagementView;

public class UserUtils 
{
	public static User copyViewBeanToUser(UserManagementViewBean viewBean)
	{
		User user = new User();
		
		user.setUserId(viewBean.getUserId());
		user.setUsername(viewBean.getUsername());
		user.setFirstName(viewBean.getFirstName());
		user.setLastName(viewBean.getLastName());
		user.setUserActive(viewBean.isUserActive());
		user.setNotes(viewBean.getNotes());
		user.setAccessLevel(evaluateAccessLevel(viewBean.getAccessLevel()));
		
		return user;
	}
	
	public static UserManagementViewBean copyUserToViewBean(User user)
	{
		UserManagementViewBean viewBean = new UserManagementViewBean();
		
		viewBean.setUserId(user.getUserId());
		viewBean.setUsername(user.getUsername());
		viewBean.setFirstName(user.getFirstName());
		viewBean.setLastName(user.getLastName());
		viewBean.setUserActive(user.isUserActive());
		viewBean.setNotes(user.getNotes());
		viewBean.setAccessLevel(evaluateAccessLevel(user.getAccessLevel()));

		return viewBean;
	}
	
	private static jjdevine.epos.usermanagement.view.beans.UserManagementViewBean.AccessLevel evaluateAccessLevel(AccessLevel accessLevel) 
	{
		switch(accessLevel)
		{
			case ADMINISTRATOR:
				return jjdevine.epos.usermanagement.view.beans.UserManagementViewBean.AccessLevel.ADMINISTRATOR;
			case USER:
				return jjdevine.epos.usermanagement.view.beans.UserManagementViewBean.AccessLevel.USER;
			default:
				throw new IllegalStateException("Access Level not recognised or is not set");
		}
	}
	
	private static AccessLevel evaluateAccessLevel(jjdevine.epos.usermanagement.view.beans.UserManagementViewBean.AccessLevel accessLevel) 
	{
		switch(accessLevel)
		{
			case ADMINISTRATOR:
				return AccessLevel.ADMINISTRATOR;
			case USER:
				return AccessLevel.USER;
			default:
				throw new IllegalStateException("Access Level not recognised or is not set");
		}
	}
	
	public static String getPassword(UserManagementView view)
	{
		String password1 = getMaskedInputValue((Component)view, "New Password:");
		
		if(password1 == null || password1.length() == 0)
		{
			return null; //user pressed cancel or entered blank password
		}
		
		String password2 = null;
		
		while(true)
		{
			password2 = getMaskedInputValue((Component)view, "Confirm password:");
			if(password2 == null)
			{
				return null;
			}
			else
			{
				if(password2.equals(password1))
				{
					return password1;
				}
				else
				{
					view.showErrorMessage("Passwords do not match!");
				}
			}
		}
	}
	
	/**
	 * result[0] is the username and result [1] is the password
	 * @param parent
	 * @return
	 */
	public static String[] quickAuthenticate(Component parent)
	{
		String[] result = new String[2];
		
		JPanel panelInput = CommonComponentFactory.createUnborderedJPanel(new Dimension(200,62));
		
		JLabel lUsername = CommonComponentFactory.createCenteredJLabel("Username:", new Dimension(115,25));
		JTextField tfUsername = CommonComponentFactory.createJTextField(new Dimension(110,25));
		JLabel lPassword = CommonComponentFactory.createCenteredJLabel("Password", new Dimension(115,25));
		final JPasswordField jpf = new JPasswordField();
		jpf.setPreferredSize(new Dimension(110,25));
		
		panelInput.add(lUsername);
		panelInput.add(tfUsername);
		panelInput.add(lPassword);
		panelInput.add(jpf);
		
		JOptionPane jop = new JOptionPane(panelInput, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		
		JDialog dialog = jop.createDialog(parent, "Authentication");
		
		dialog.addComponentListener(
			new ComponentAdapter() 
			{
				
				@Override
				public void componentShown(ComponentEvent evt)
				{
					SwingUtilities.invokeLater(
						new Runnable() 
						{	
							@Override
							public void run() 
							{
								//TODO: this doesn't work, focus still goes to 'ok' button
								jpf.requestFocusInWindow();
							}
						}
					);
				}
			}
		);
		
		dialog.setVisible(true);
		int resultInt = jop.getValue() != null ? (Integer)jop.getValue() : -1;
		dialog.dispose();
		
		if(resultInt == JOptionPane.OK_OPTION)
		{
			result[0] = tfUsername.getText().trim();
			result[1] = new String(jpf.getPassword());
		}
		
		return result;
	}
	
	public static String getMaskedInputValue(Component parent, String message)
	{
		JPanel panelInput = CommonComponentFactory.createUnborderedJPanel(new Dimension(200,32));
		JLabel lMessage = CommonComponentFactory.createCenteredJLabel(message, new Dimension(115,25));
		final JPasswordField jpf = new JPasswordField();
		jpf.setPreferredSize(new Dimension(110,25));
		
		panelInput.add(lMessage);
		panelInput.add(jpf);
		
		JOptionPane jop = new JOptionPane(panelInput, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		
		JDialog dialog = jop.createDialog(parent, message);
		
		dialog.addComponentListener(
			new ComponentAdapter() 
			{
				
				@Override
				public void componentShown(ComponentEvent evt)
				{
					SwingUtilities.invokeLater(
						new Runnable() 
						{	
							@Override
							public void run() 
							{
								//TODO: this doesn't work, focus still goes to 'ok' button
								jpf.requestFocusInWindow();
							}
						}
					);
				}
			}
		);
		
		dialog.setVisible(true);
		int result = jop.getValue() != null ? (Integer)jop.getValue() : -1;
		dialog.dispose();
		
		String value = null;
		if(result == JOptionPane.OK_OPTION)
		{
			value = new String(jpf.getPassword());
		}
		
		return value;
	}
}
