package jjdevine.epos.processors;

public interface ShowGarmentSalesProcessor 
{
	public void showGarmentSalesBySKUId(long skuId);
	
	public void showGarmentSalesByGarmentCode(String garmentCode);
}
