package jjdevine.epos.processors.impl;

import java.util.Collections;
import java.util.List;

import jjdevine.epos.EposContext;
import jjdevine.epos.beans.Preferences;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.preferences.data.svc.PreferencesDataService;
import jjdevine.epos.preferences.view.beans.PreferencesViewBean;
import jjdevine.epos.preferences.view.beans.PreferencesViewBean.TillHeadingMode;
import jjdevine.epos.preferences.view.events.PreferencesViewEvent;
import jjdevine.epos.preferences.view.events.PreferencesViewEventListener;
import jjdevine.epos.preferences.view.gui.PreferencesView;
import jjdevine.epos.processors.EditPreferencesProcessor;
import jjdevine.epos.receiptsetup.data.svc.ReceiptSetupDataService;
import jjdevine.epos.receiptsetup.data.svc.beans.ReceiptSetupDTO;

public class EditPreferencesProcessorImpl implements EditPreferencesProcessor, PreferencesViewEventListener 
{
	private PreferencesView view;
	private PreferencesDataService preferencesDataService;
	private ReceiptSetupDataService receiptSetupDataService;
	
	private static final String TEST_MODE_ON = "YES";
	private static final String TEST_MODE_OFF = "NO";
	
	@Override
	public void editPreferences() 
	{
		PreferencesViewBean viewBean = new PreferencesViewBean();
		
		int receiptSetupId = preferencesDataService.getActiveReceiptSetupId();
		String tillHeading = preferencesDataService.getTillHeading();
		boolean testMode = EposUtils.isTestMode();
		int tillHeadingMode = preferencesDataService.getTillHeadingMode();
		String tillHeadingImage = preferencesDataService.getTillHeadingImage();
		
		ReceiptSetupDTO receiptSetup = receiptSetupDataService.getReceiptSetup(receiptSetupId);
		
		List<String> receiptSetupNames = receiptSetupDataService.getAllSetupNames();
		Collections.sort(receiptSetupNames);
		
		viewBean.setReceiptFormat(receiptSetupNames.toArray(new String[receiptSetupNames.size()]));
		viewBean.setReceiptFormatSelection(receiptSetup.getName());
		
		viewBean.setTillHeading(tillHeading);
		
		viewBean.setTestMode(new String[]{TEST_MODE_OFF, TEST_MODE_ON});
		viewBean.setTestModeSelection(testMode ? TEST_MODE_ON : TEST_MODE_OFF);
		
		viewBean.setTillHeadingImage(tillHeadingImage);
		
		switch(tillHeadingMode)
		{
			case Preferences.TILL_HEADING_MODE_TEXT:
				viewBean.setTillHeadingMode(TillHeadingMode.TEXT);
				break;
			case Preferences.TILL_HEADING_MODE_IMAGE:
				viewBean.setTillHeadingMode(TillHeadingMode.IMAGE);
				break;
		}
		
		//close view if already open
		closeView();
		
		view = EposContext.getPreferencesView();
		view.initialise(viewBean);
		view.addListener(this);
	}
	
	private void closeView()
	{
		if(view != null)
		{
			view.close();
			view = null;
		}
	}

	public void setPreferencesDataService(
			PreferencesDataService preferencesDataService) {
		this.preferencesDataService = preferencesDataService;
	}

	public void setReceiptSetupDataService(
			ReceiptSetupDataService receiptSetupDataService) {
		this.receiptSetupDataService = receiptSetupDataService;
	}

	@Override
	public void onPreferencesViewEvent(PreferencesViewEvent evt) 
	{
		switch(evt.getType())
		{
			case CLOSE:
				closeView();
				break;
			case UPDATE_PREFERENCES:
				updatePreferences(evt.getViewBean());
				break;
		}
	}

	private void updatePreferences(PreferencesViewBean viewBean) 
	{
		boolean success;
		success = EposUtils.setTestMode(TEST_MODE_ON.equals(viewBean.getTestModeSelection()));
		
		if(success)
		{
			success = preferencesDataService.setTillHeading(viewBean.getTillHeading());
		}
		
		if(success)
		{
			success = preferencesDataService.setActiveReceiptSetupId(receiptSetupDataService.getReceiptSetup(viewBean.getReceiptFormatSelection()).getId());
		}
		
		if(success)
		{
			success = preferencesDataService.setTillHeadingImage(viewBean.getTillHeadingImage());
		}
		
		if(success)
		{
			switch(viewBean.getTillHeadingMode())
			{
				case TEXT:
					success = preferencesDataService.setTillHeadingMode(Preferences.TILL_HEADING_MODE_TEXT);
					break;
				case IMAGE:
					success = preferencesDataService.setTillHeadingMode(Preferences.TILL_HEADING_MODE_IMAGE);
					break;
			}
		}
		
		if(success)
		{
			view.showInformationMessage("Prefences Updated Successfully - Restart EPOS to Apply New Settings!");
			closeView();
		}
		else
		{
			view.showErrorMessage("Unable to update all preferences. Please check the logs.");
		}
	}

}
