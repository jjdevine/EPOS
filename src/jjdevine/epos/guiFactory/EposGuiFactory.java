package jjdevine.epos.guiFactory;

import jjdevine.epos.gui.EposTransactionGui;
import jjdevine.epos.guiImpl.ModifyPurchasesMenu;
import jjdevine.epos.guiImpl.SimpleEposTransactionGui;

public class EposGuiFactory 
{
	
	public static EposTransactionGui createEposTransactionGui()
	{
		return new SimpleEposTransactionGui();
	}
	
	public static ModifyPurchasesMenu createModifyPurchasesMenu(int[] affectedPurchases)
	{
		return new ModifyPurchasesMenu(affectedPurchases);
	}
	
}
