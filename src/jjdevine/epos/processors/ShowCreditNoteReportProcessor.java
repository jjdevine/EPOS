package jjdevine.epos.processors;

import java.util.Date;

public interface ShowCreditNoteReportProcessor 
{
	public void showCreditNoteReportForAll();
	
	public void showCreditNoteReportForName(String name);
	
	public void showCreditNoteReportForIssueDate(Date dateFrom, Date dateTo);
}
