package jjdevine.epos.processors;

import java.util.Date;

public interface ShowOrdersReportProcessor 
{
	public void showOrdersReport(String garmentCode, Date dateFrom, Date dateTo);
}
