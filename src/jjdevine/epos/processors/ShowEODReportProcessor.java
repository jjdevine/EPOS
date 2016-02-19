package jjdevine.epos.processors;

import java.util.Date;

public interface ShowEODReportProcessor 
{
	public void showEODReportForDay(Date day);
	
	public void showEODReportForPeriod(Date startDate, Date endDate);
}
