package jjdevine.epos.beans;

import jjdevine.epos.common.EposUtils;
import jjdevine.epos.preferences.data.svc.PreferencesDataService;
import jjdevine.epos.receiptsetup.data.svc.ReceiptSetupDataService;
import jjdevine.epos.receiptsetup.data.svc.beans.ReceiptSetupDTO;

import org.springframework.dao.EmptyResultDataAccessException;

public class Preferences 
{
	public static final int TILL_HEADING_MODE_TEXT = 1;
	public static final int TILL_HEADING_MODE_IMAGE = 2;
	
	private PreferencesDataService preferencesDataService;
	private ReceiptSetupDataService receiptSetupDataService;
	
	//preference values:
	private ReceiptSetupDTO activeReceiptSetup;
	private String tillHeading;
	private boolean testMode;
	private boolean testModeSet = false;
	private String tillHeadingImage;
	private int tillHeadingMode;
	
	public ReceiptSetupDTO getActiveReceiptSetup() {
		return activeReceiptSetup;
	}

	public void setActiveReceiptSetup(ReceiptSetupDTO activeReceiptSetup) {
		this.activeReceiptSetup = activeReceiptSetup;
	}
	
	public String getTillHeading() {
		return tillHeading;
	}

	public void setTillHeading(String tillHeading) {
		this.tillHeading = tillHeading;
	}

	public boolean isTestMode() {
		return testMode;
	}

	public String getTillHeadingImage() {
		return tillHeadingImage;
	}

	public void setTillHeadingImage(String tillHeadingImage) {
		this.tillHeadingImage = tillHeadingImage;
	}

	public int getTillHeadingMode() {
		return tillHeadingMode;
	}

	public void setTillHeadingMode(int tillHeadingMode) {
		this.tillHeadingMode = tillHeadingMode;
	}

	public void refresh()
	{
		try
		{
			activeReceiptSetup = receiptSetupDataService.getReceiptSetup(
					preferencesDataService.getActiveReceiptSetupId());
			
			testMode = EposUtils.isTestMode();
			
			tillHeadingImage = preferencesDataService.getTillHeadingImage();
			tillHeadingMode = preferencesDataService.getTillHeadingMode();
			
			if(!testModeSet)   //do not allow test mode to be changed until restart or data could be corrupted
			{
				testModeSet = true;
				tillHeading = preferencesDataService.getTillHeading();
			}
		}
		catch(EmptyResultDataAccessException ex) //when application is first used this may not exist
		{
			activeReceiptSetup = null;
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
}	
