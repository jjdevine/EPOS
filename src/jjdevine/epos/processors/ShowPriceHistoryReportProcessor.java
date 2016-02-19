package jjdevine.epos.processors;

public interface ShowPriceHistoryReportProcessor 
{
	public void showPriceHistoryReportForSKUID(long skuId);
	
	public void showPriceHistoryReportForGarmentCode(String garmentCode);
}
