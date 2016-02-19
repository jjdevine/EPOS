package jjdevine.epos.processors;

import java.util.Date;

public interface ShowTransactionSummaryReportProcessor 
{
	public void showTransactionSummaryReportByDate(Date from, Date to);
	
	public void showTransactionSummaryReportByCustomer(int customerId);
}
