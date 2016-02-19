package jjdevine.epos.processors.impl;

import jjdevine.epos.EposContext;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.processors.EditUserProcessor;
import jjdevine.epos.usermanagement.data.svc.UserManagementDataService;
import jjdevine.epos.usermanagement.data.svc.beans.User;
import jjdevine.epos.usermanagement.view.beans.UserManagementViewBean;
import jjdevine.epos.usermanagement.view.events.UserManagementViewEvent;
import jjdevine.epos.usermanagement.view.events.UserManagementViewEventListener;
import jjdevine.epos.usermanagement.view.gui.UserManagementView;
import jjdevine.epos.utils.UserUtils;

import org.apache.log4j.Logger;

public class EditUserProcessorImpl implements EditUserProcessor, UserManagementViewEventListener 
{
	private static Logger logger = Logger.getLogger(EditUserProcessorImpl.class);
	private UserManagementDataService userManagementDataService;
	private UserManagementView view;
	
	@Override
	public void editUser(String username) 
	{
		User user = userManagementDataService.getUserByUsername(username);
		
		if(user == null)
		{
			//TODO: show error
			return;
		}
		
		UserManagementViewBean viewBean = UserUtils.copyUserToViewBean(user);
		viewBean.setHeading("Edit User: " + user.getUsername());

		view = EposContext.getUserManagementView();
		view.setUser(viewBean);
		view.setEditMode(true);
		view.addListener(this);
	}
	
	@Override
	public void onUserManagementViewEvent(UserManagementViewEvent evt) 
	{
		switch(evt.getType())
		{
			case SAVE:
				UserManagementViewBean viewBean = evt.getViewBean();
				User user = UserUtils.copyViewBeanToUser(viewBean);
				
				boolean success = userManagementDataService.updateUser(user);
				
				if(!success)
				{
					view.showErrorMessage("Unable to update user, please check the logs");
				}
				else
				{
					view.showInformationMessage("User Details Saved Successfully!");
					closeView();
				}
				break;
				
			case CHANGE_PASSWORD:
				
				String password = UserUtils.getPassword(view);
				if(password == null)
				{
					return;
				}
				
				viewBean = evt.getViewBean();
				user = UserUtils.copyViewBeanToUser(viewBean);
				
				try
				{
					String salt = EposUtils.generateSalt();
					
					success = userManagementDataService.setPassword(user.getUserId(), EposUtils.encryptPassword(password+salt));
					
					if(!success)
					{
						throw new RuntimeException("Could not create password");
					}
					
					success = userManagementDataService.setSalt(user.getUserId(), salt);
					
					if(!success)
					{
						throw new RuntimeException("Could not create password");
					}
						
				}
				catch(Exception ex)
				{
					logger.error(ex.getMessage(), ex);
					view.showErrorMessage("Unable to create user, please check the logs");
					success = false;
				}
				
				if(success)
				{
					view.showInformationMessage("Password updated successfully!");
				}
				break;
				
			case EXIT:
				closeView();
				break;
		}
		
	}
	
	private void closeView()
	{
		if(view != null)
		{
			view.close();
			view = null;
		}
	}

	public void setUserManagementDataService(
			UserManagementDataService userManagementDataService) {
		this.userManagementDataService = userManagementDataService;
	}



}
