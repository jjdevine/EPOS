package jjdevine.epos.processors.impl;

import java.util.ArrayList;
import java.util.List;

import jjdevine.epos.BeanCopyUtils;
import jjdevine.epos.EposContext;
import jjdevine.epos.discountdefinition.view.beans.DiscountDefinitionViewBean;
import jjdevine.epos.discountdefinition.view.beans.DiscountPolicyDefinitionViewBean;
import jjdevine.epos.discountdefinition.view.events.DiscountPolicyDefinitionViewEvent;
import jjdevine.epos.discountdefinition.view.events.DiscountPolicyDefinitionViewListener;
import jjdevine.epos.discountdefinition.view.gui.DiscountPolicyDefinitionView;
import jjdevine.epos.discountdefintion.data.svc.DiscountDefinitionDataService;
import jjdevine.epos.discountdefintion.model.beans.DiscountDefinition;
import jjdevine.epos.discountdefintion.model.beans.DiscountPolicy;
import jjdevine.epos.processors.CreateDiscountPolicyProcessor;

public class CreateDiscountPolicyProcessorImpl implements CreateDiscountPolicyProcessor, DiscountPolicyDefinitionViewListener 
{
	private DiscountDefinitionDataService discountDefinitionDataService;
	private DiscountPolicyDefinitionView discountPolicyDefinitionView;

	private void closeWindowIfOpen()
	{
		if (discountPolicyDefinitionView != null)
		{
			discountPolicyDefinitionView.close();
			discountPolicyDefinitionView = null;
		}
	}
	
	@Override
	public void createDiscountPolicy() 
	{
		closeWindowIfOpen();
		List<DiscountDefinition> discountDefinitions = discountDefinitionDataService.getAllDiscountDefinitions();
		List<DiscountDefinitionViewBean> discountDefinitionViewBeans = new ArrayList<DiscountDefinitionViewBean>();
		
		for(DiscountDefinition discountDefinition: discountDefinitions)
		{
			discountDefinitionViewBeans.add(BeanCopyUtils.mapDiscountDefinitionToViewBean(discountDefinition));
		}
		
		discountPolicyDefinitionView = EposContext.getDiscountPolicyView();
		discountPolicyDefinitionView.setAvailableDiscounts(discountDefinitionViewBeans);
		discountPolicyDefinitionView.setNewRecordMode(true);
		discountPolicyDefinitionView.addListener(this);
	}
	
	@Override
	public void editDiscountPolicy(String policyName) 
	{
		closeWindowIfOpen();
		DiscountPolicy discountPolicy = discountDefinitionDataService.getDiscountPolicy(policyName);
		DiscountPolicyDefinitionViewBean viewBean = BeanCopyUtils.mapDiscountPolicyToViewBean(discountPolicy);
		
		createDiscountPolicy();
		
		discountPolicyDefinitionView.setDiscountPolicyViewBean(viewBean);
		discountPolicyDefinitionView.setNewRecordMode(false);
	}

	@Override
	public void onDiscountPolicyDefinitionViewEvent(DiscountPolicyDefinitionViewEvent evt) 
	{
		switch(evt.getType())
		{
			//user uses gui close button
			case CLOSE:
				discountPolicyDefinitionView.close();
				discountPolicyDefinitionView = null;
				break;
				
			//user uses window 'x' to close directly
			case DISPOSE:
				discountPolicyDefinitionView = null;
				break;
				
			case SAVE:
				DiscountPolicyDefinitionViewBean viewBean = evt.getDiscountPolicyDefinitionViewBean();		
				DiscountPolicy discountPolicy = BeanCopyUtils.copyDiscountPolicyViewBeanToModel(viewBean);
				
				boolean success;
				
				if(discountPolicy.getPolicyId() == -1)
				{
					/*
					 * create a new policy
					 */
					
					boolean nameExists = discountDefinitionDataService.discountPolicyNameExists(discountPolicy.getPolicyName());
					
					if(nameExists)
					{
						discountPolicyDefinitionView.showErrorMessage("This Discount Policy Name already exists. Please choose another");
						return;
					}
					
					discountPolicy.setPolicyId(discountDefinitionDataService.getNextAvailableDiscountPolicyId());
					
					success = discountDefinitionDataService.saveDiscountPolicy(discountPolicy);
					
					if(!success)
					{
						discountPolicyDefinitionView.showErrorMessage("Unable to save discount policy - please check the logs.");
						return;
					}
					
					discountPolicyDefinitionView.showInformationMessage("Discount Policy saved successfully!");
					EposContext.getTransactionController().updateDiscountPolicies();
				}
				else
				{
					/*
					 * update an existing policy 
					 */
					
					success = discountDefinitionDataService.updateDiscountPolicy(discountPolicy);
					
					if(!success)
					{
						discountPolicyDefinitionView.showErrorMessage("Unable to update discount policy - please check the logs.");
						return;
					}
					
					discountPolicyDefinitionView.showInformationMessage("Discount Policy updated successfully!");
					EposContext.getTransactionController().updateDiscountPolicies();
				}
								
				discountPolicyDefinitionView.close();
				discountPolicyDefinitionView = null;
				break;
		}
		
	}

	public DiscountDefinitionDataService getDiscountDefinitionDataService() {
		return discountDefinitionDataService;
	}

	public void setDiscountDefinitionDataService(
			DiscountDefinitionDataService discountDefinitionDataService) {
		this.discountDefinitionDataService = discountDefinitionDataService;
	}
}
