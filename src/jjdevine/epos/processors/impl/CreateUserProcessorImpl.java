package jjdevine.epos.processors.impl;

import jjdevine.epos.EposContext;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.processors.CreateUserProcessor;
import jjdevine.epos.usermanagement.data.svc.UserManagementDataService;
import jjdevine.epos.usermanagement.data.svc.beans.User;
import jjdevine.epos.usermanagement.view.beans.UserManagementViewBean;
import jjdevine.epos.usermanagement.view.events.UserManagementViewEvent;
import jjdevine.epos.usermanagement.view.events.UserManagementViewEventListener;
import jjdevine.epos.usermanagement.view.gui.UserManagementView;
import jjdevine.epos.utils.UserUtils;

import org.apache.log4j.Logger;

public class CreateUserProcessorImpl implements CreateUserProcessor, UserManagementViewEventListener 
{
	private UserManagementDataService userManagementDataService;
	private UserManagementView view;
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void createUser() 
	{
		UserManagementViewBean viewBean = new UserManagementViewBean();
		
		long nextId = userManagementDataService.getNextAvailableUserId();
		if(nextId == -1)
		{
			logger.error("Could not get next available user ID");
			//TODO: show error
			return;
		}
		
		viewBean.setUserId(nextId);
		viewBean.setHeading("Create a New User");
		
		view = EposContext.getUserManagementView();
		view.setUser(viewBean);
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
				
				boolean success = false;

				if(userManagementDataService.userExists(user))
				{
					view.showErrorMessage("This username or id already exists. Please choose a new username.");
					return;
				}
				
				String password = UserUtils.getPassword(view);
				
				if(password == null)
				{
					return;
				}
				
				success = userManagementDataService.createUser(user);
				
				if(success)
				{
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
					}
				}
				
				

				if(success)
				{
					view.showInformationMessage("User Details Saved Successfully!");
					closeView();
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
