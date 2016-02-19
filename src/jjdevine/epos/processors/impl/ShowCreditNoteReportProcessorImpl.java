package jjdevine.epos.processors.impl;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jjdevine.csv.svc.CSVService;
import jjdevine.csv.svc.beans.CSVRequest;
import jjdevine.epos.EposContext;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.creditnote.data.svc.CreditNoteDataService;
import jjdevine.epos.discountdefintion.model.beans.CreditNote;
import jjdevine.epos.processors.ShowCreditNoteReportProcessor;
import jjdevine.epos.reports.genericreporttemplate1.view.beans.GenericReportTemplate1ViewBean;
import jjdevine.epos.reports.genericreporttemplate1.view.beans.SummaryInformation;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Event;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Listener;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Event.Type;
import jjdevine.epos.reports.genericreporttemplate1.view.gui.GenericReportTemplateView1;
import jjdevine.epos.utils.Utils;

public class ShowCreditNoteReportProcessorImpl implements ShowCreditNoteReportProcessor, GenericReportTemplateView1Listener 
{	
	private GenericReportTemplateView1 view;
	private CreditNoteDataService creditNoteDataService;
	private CSVService csvService;

	@Override
	public void showCreditNoteReportForAll() 
	{
		GenericReportTemplate1ViewBean viewBean = new GenericReportTemplate1ViewBean();
		
		viewBean.setReportTitle("Credit Note Report - All");
		
		List<CreditNote> creditNotes = creditNoteDataService.findAllCreditNotes();
		
		List<SummaryInformation> summaryInfo1 = getSummaryInfo1(creditNotes);
		List<SummaryInformation> summaryInfo2 = getSummaryInfo2(creditNotes);
		
		viewBean.setSummaryInformation1(summaryInfo1);
		viewBean.setSummaryInformation2(summaryInfo2);
		
		viewBean.setDetailedInformationHeadings(getReportDetailHeadings());
		viewBean.setDetailedInformationRows(getReportDetailRows(creditNotes));
		
		closeView(); //close report if already open
		view = EposContext.getGenericReportTemplateView1();
		view.showReport(viewBean);
		view.addListener(this);
	}
	
	@Override
	public void showCreditNoteReportForIssueDate(Date dateFrom, Date dateTo) 
	{
		GenericReportTemplate1ViewBean viewBean = new GenericReportTemplate1ViewBean();
		
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		
		viewBean.setReportTitle("Credit Note Report - " + df.format(dateFrom) + " Until " + df.format(dateTo));
		
		List<CreditNote> creditNotes = creditNoteDataService.findCreditNotesByIssueDate(dateFrom, dateTo);
		
		List<SummaryInformation> summaryInfo1 = getSummaryInfo1(creditNotes);
		List<SummaryInformation> summaryInfo2 = getSummaryInfo2(creditNotes);
		
		viewBean.setSummaryInformation1(summaryInfo1);
		viewBean.setSummaryInformation2(summaryInfo2);
		
		viewBean.setDetailedInformationHeadings(getReportDetailHeadings());
		viewBean.setDetailedInformationRows(getReportDetailRows(creditNotes));
		
		closeView(); //close report if already open
		view = EposContext.getGenericReportTemplateView1();
		view.showReport(viewBean);
		view.addListener(this);
	}
	
	//TODO - time being lost from issue date

	@Override
	public void showCreditNoteReportForName(String name) 
	{
		GenericReportTemplate1ViewBean viewBean = new GenericReportTemplate1ViewBean();
		
		viewBean.setReportTitle("Credit Note Report - Issued to '"+name.replaceAll("%", "")+"'");
		
		List<CreditNote> creditNotes = creditNoteDataService.findCreditNotesIssuedToName(name);
		
		List<SummaryInformation> summaryInfo1 = getSummaryInfo1(creditNotes);
		List<SummaryInformation> summaryInfo2 = getSummaryInfo2(creditNotes);
		
		viewBean.setSummaryInformation1(summaryInfo1);
		viewBean.setSummaryInformation2(summaryInfo2);
		
		viewBean.setDetailedInformationHeadings(getReportDetailHeadings());
		viewBean.setDetailedInformationRows(getReportDetailRows(creditNotes));
		
		closeView(); //close report if already open
		view = EposContext.getGenericReportTemplateView1();
		view.showReport(viewBean);
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
	
	private List<SummaryInformation> getSummaryInfo1(List<CreditNote> creditNotes) 
	{
		List<SummaryInformation> listSummaryInfo = new ArrayList<SummaryInformation>();
		
		SummaryInformation summaryInfo = new SummaryInformation();
		summaryInfo.setInformation("Total Credit Notes Issued");
		summaryInfo.setValue(""+creditNotes.size());
		listSummaryInfo.add(summaryInfo);
		
		double total = 0;
		
		for(CreditNote creditNote: creditNotes)
		{
			total += creditNote.getTotalValue();
		}
		
		summaryInfo = new SummaryInformation();
		summaryInfo.setInformation("Total Value");
		summaryInfo.setValue(EposUtils.formatCurrencyNoCommas(total));
		listSummaryInfo.add(summaryInfo);
		
		return listSummaryInfo;
	}
	
	private List<SummaryInformation> getSummaryInfo2(List<CreditNote> creditNotes) 
	{
		List<SummaryInformation> listSummaryInfo = new ArrayList<SummaryInformation>();
		
		int numUnredeemed = 0;
		double totValUnredeemed = 0;
		
		for(CreditNote creditNote: creditNotes)
		{
			if(creditNote.getValueRemaining() > 0)
			{
				numUnredeemed++;
				totValUnredeemed += creditNote.getValueRemaining();
			}
		}
		
		SummaryInformation summaryInfo = new SummaryInformation();
		summaryInfo.setInformation("Number Unredeemed");
		summaryInfo.setValue(""+numUnredeemed);
		listSummaryInfo.add(summaryInfo);
		
		summaryInfo = new SummaryInformation();
		summaryInfo.setInformation("Unredeemed Value");
		summaryInfo.setValue(EposUtils.formatCurrencyNoCommas(totValUnredeemed));
		listSummaryInfo.add(summaryInfo);
		
		return listSummaryInfo;
	}
	
	private String[] getReportDetailHeadings() 
	{
		String[] headings = {
			"Credit Note ID",
			"Total Value",
			"Value Remaining",
			"Name Issued To",
			"Reason For Issue",
			"Payment Type",
			"Issue Date"
		};

		return headings;
	}

	private List<String[]> getReportDetailRows(List<CreditNote> creditNotes) 
	{
		List<String[]> rows = new ArrayList<String[]>();
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		
		String[] row;
		
		for(CreditNote creditNote: creditNotes)
		{
			row = new String[7];
			
			row[0] = creditNote.getId() +"";
			row[1] = EposUtils.formatCurrencyNoCommas(creditNote.getTotalValue());
			row[2] = EposUtils.formatCurrencyNoCommas(creditNote.getValueRemaining());
			row[3] = creditNote.getNameIssuedTo();
			row[4] = creditNote.getReasonForIssue().getDescription();
			row[5] = creditNote.getPaymentType() == null ? "" : creditNote.getPaymentType().toString();
			row[6] = df.format(creditNote.getIssueDate());
			
			rows.add(row);
		}
		
		return rows;
	}
	
	@Override
	public void onGenericReportTemplateView1Event(GenericReportTemplateView1Event evt) 
	{
		if(evt.getType() == Type.EXPORT_CSV)
		{
			/*
			 * get file name to export to
			 */
			
			File file = Utils.getCSVFileFromUser();
			
			if(file == null)
			{
				return;
			}
			
			String path = file.getPath();
			
			if(path.length() > 4 && !path.substring(path.length()-4).equalsIgnoreCase(".csv"))
			{
				file = new File(path + ".csv");
			}
	
			/*
			 * output csv
			 */
	
			CSVRequest csvReq = new CSVRequest();
			csvReq.setFile(file);
			csvReq.setModel(Utils.convertGenericReportTemplate1ViewBeanToCSVModel(evt.getViewBean()));
			
			csvService.createCSV(csvReq);
			
			view.showMessage("CSV Created: " + file.getPath());
			closeView();
		}
	}

	public CreditNoteDataService getCreditNoteDataService() {
		return creditNoteDataService;
	}

	public void setCreditNoteDataService(CreditNoteDataService creditNoteDataService) {
		this.creditNoteDataService = creditNoteDataService;
	}

	public CSVService getCsvService() {
		return csvService;
	}

	public void setCsvService(CSVService csvService) {
		this.csvService = csvService;
	}



}
