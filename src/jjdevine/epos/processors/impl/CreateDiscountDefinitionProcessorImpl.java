package jjdevine.epos.processors.impl;

import java.awt.Component;

import javax.swing.JOptionPane;

import jjdevine.epos.BeanCopyUtils;
import jjdevine.epos.EposContext;
import jjdevine.epos.discountdefinition.view.events.DiscountDefinitionViewEvent;
import jjdevine.epos.discountdefinition.view.events.DiscountDefinitionViewListener;
import jjdevine.epos.discountdefinition.view.gui.DiscountDefinitionView;
import jjdevine.epos.discountdefintion.data.svc.DiscountDefinitionDataService;
import jjdevine.epos.discountdefintion.model.beans.DiscountDefinition;
import jjdevine.epos.discountdefintion.model.beans.DiscountDefinition.DiscountType;
import jjdevine.epos.processors.CreateDiscountDefinitionProcessor;

public class CreateDiscountDefinitionProcessorImpl implements CreateDiscountDefinitionProcessor, DiscountDefinitionViewListener
{
	private DiscountDefinitionDataService discountDefinitionDataService;
	private DiscountDefinitionView discountDefinitionView;
	
	@Override
	public void createDiscountDefinition() 
	{
		//TODO: if window is already open do a null check on view object?
		discountDefinitionView = EposContext.getDiscountDefinitionView();
		discountDefinitionView.addListener(this);
	}

	@Override
	public void onDiscountDefinitionViewEvent(DiscountDefinitionViewEvent evt) 
	{
		switch(evt.getType())
		{
			case SAVE:
				DiscountDefinition discountDefinition = 
					BeanCopyUtils.copyDiscountDefinitionViewBeanToModel(evt.getDiscountDefinitionViewBean());
				
				boolean nameExists = discountDefinitionDataService.discountNameExists(discountDefinition.getDiscountName());
				
				if(nameExists)
				{
					discountDefinitionView.showErrorMessage("This Discount Name already exists. Please choose another.");
					return;
				}
				
				if(discountDefinition.getDiscountType() == DiscountType.FLAT_DISCOUNT)
				{
					int result = JOptionPane.showConfirmDialog((Component)discountDefinitionView, "Flat discount will sell any qualifying " +
							"garment at the provided price - this may cause large discounts to be applied to expensive items - " +
							"are you sure you want to use this type of discount?", "Confirm Discount Type", JOptionPane.YES_NO_OPTION);
					if(result != JOptionPane.YES_OPTION)
					{
						//cancel discount
						return;
					}
				}
				
				discountDefinition.setDiscountId(discountDefinitionDataService.getNextAvailableDiscountDefinitionId());
				
				boolean success;
				
				success = discountDefinitionDataService.saveDiscountDefinition(discountDefinition);
				
				if(!success)
				{
					discountDefinitionView.showErrorMessage("Unable to save discount - please check the logs.");
					return;
				}
				discountDefinitionView.close();
				discountDefinitionView = null;
				break;
				
			//user uses gui close button
			case CLOSE:
				discountDefinitionView.close();
				discountDefinitionView = null;
				break;
				
			//user uses window 'x' to close directly
			case DISPOSE:
				discountDefinitionView = null;
				break;
		}
		
	}

	public void setDiscountDefinitionDataService(
			DiscountDefinitionDataService discountDefinitionDataService) {
		this.discountDefinitionDataService = discountDefinitionDataService;
	}

	public void setDiscountDefinitionView(
			DiscountDefinitionView discountDefinitionView) {
		this.discountDefinitionView = discountDefinitionView;
	}

}
