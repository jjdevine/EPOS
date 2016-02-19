package jjdevine.epos.processors.impl;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import jjdevine.csv.svc.CSVService;
import jjdevine.csv.svc.beans.CSVRequest;
import jjdevine.epos.EposContext;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.beans.GarmentCommonInfo;
import jjdevine.epos.common.beans.GarmentData;
import jjdevine.epos.common.beans.HistoricEposPurchase;
import jjdevine.epos.common.persistence.EposCommonDatasource;
import jjdevine.epos.garment.data.svc.GarmentDataService;
import jjdevine.epos.processors.ShowGarmentSalesProcessor;
import jjdevine.epos.processors.ShowTransactionDetailsReportProcessor;
import jjdevine.epos.reports.genericreporttemplate1.view.beans.GenericReportTemplate1ViewBean;
import jjdevine.epos.reports.genericreporttemplate1.view.beans.SummaryInformation;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Event;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Listener;
import jjdevine.epos.reports.genericreporttemplate1.view.gui.GenericReportTemplateView1;
import jjdevine.epos.transaction.data.svc.TransactionDataService;
import jjdevine.epos.utils.Utils;

public class ShowGarmentSalesProcessorImpl implements ShowGarmentSalesProcessor, GenericReportTemplateView1Listener 
{
	private GenericReportTemplateView1 view;
	private GarmentDataService garmentDataService;
	private EposCommonDatasource eposCommonDatasource;
	private TransactionDataService transactionDataService;
	private ShowTransactionDetailsReportProcessor transactionDetailsReportProcessor;
	private CSVService csvService;
	
	@Override
	public void showGarmentSalesByGarmentCode(String garmentCode) 
	{
		//TODO inject this with spring
		GarmentCommonInfo garmentCommonInfo = eposCommonDatasource.getGarmentCommonInfoByCode(garmentCode);
		
		if(garmentCommonInfo == null)
		{
			//TODO: show error - no data found
			return;
		}
		
		GenericReportTemplate1ViewBean viewBean = new GenericReportTemplate1ViewBean();
		
		viewBean.setReportTitle("Garment Sales Report - Garment Code " + garmentCode);
		
		viewBean.setSummaryInformation1(getSummaryInfo1(garmentCommonInfo));
		viewBean.setSummaryInformation2(getSummaryInfo2(garmentCommonInfo));
		/*
		 * get detailed info
		 */
		
		viewBean.setDetailedInformationHeadings(getRowHeadings());
		
		List<HistoricEposPurchase> purchases = transactionDataService.getPurchasesByGarmentCode(garmentCode);
		
		if(purchases == null)
		{
			//TODO: display error
			return;
		}
		
		List<String[]> rows = new ArrayList<String[]>();
		viewBean.setDetailedInformationRows(rows);
		
		for(HistoricEposPurchase purchase: purchases)
		{
			rows.add(convertPurchaseToRow(purchase));
		}
		
		//TODO: issues below
		//1. date is losing hh:mm
		
		closeView(); //close any views already open
		view = EposContext.getGenericReportTemplateView1();
		view.addListener(this);
		view.showReport(viewBean);
	}

	@Override
	public void showGarmentSalesBySKUId(long skuId) 
	{
		GarmentData garmentData = garmentDataService.getGarmentDataBySKUId(skuId);
		
		if(garmentData == null)
		{
			//TODO: show error
			return;
		}
		GarmentCommonInfo garmentCommonInfo = eposCommonDatasource.getGarmentCommonInfoByCode(garmentData.getGarmentCode());
		
		if(garmentCommonInfo == null)
		{
			//TODO: show error
			return;
		}
		
		GenericReportTemplate1ViewBean viewBean = new GenericReportTemplate1ViewBean();
		
		viewBean.setReportTitle("Garment Sales Report - SKU ID " + skuId);
		
		viewBean.setSummaryInformation1(getSummaryInfo1(garmentCommonInfo));
		viewBean.setSummaryInformation2(getSummaryInfo2(garmentCommonInfo));
		
		/*
		 * get detailed info
		 */
		
		viewBean.setDetailedInformationHeadings(getRowHeadings());
		
		List<HistoricEposPurchase> purchases = transactionDataService.getPurchasesBySKUId(skuId);
		
		if(purchases == null)
		{
			//TODO: display error
			return;
		}
		
		List<String[]> rows = new ArrayList<String[]>();
		viewBean.setDetailedInformationRows(rows);
		
		for(HistoricEposPurchase purchase: purchases)
		{
			rows.add(convertPurchaseToRow(purchase));
		}
		
		closeView(); //close any views already open
		view = EposContext.getGenericReportTemplateView1();
		view.addListener(this);
		view.showReport(viewBean);
	}

	public GarmentDataService getGarmentDataService() {
		return garmentDataService;
	}

	public void setGarmentDataService(GarmentDataService garmentDataService) {
		this.garmentDataService = garmentDataService;
	}

	public EposCommonDatasource getEposCommonDatasource() {
		return eposCommonDatasource;
	}

	public void setEposCommonDatasource(EposCommonDatasource eposCommonDatasource) {
		this.eposCommonDatasource = eposCommonDatasource;
	}

	public TransactionDataService getTransactionDataService() {
		return transactionDataService;
	}

	public void setTransactionDataService(
			TransactionDataService transactionDataService) {
		this.transactionDataService = transactionDataService;
	}
	
	public void setTransactionDetailsReportProcessor(
			ShowTransactionDetailsReportProcessor transactionDetailsReportProcessor) {
		this.transactionDetailsReportProcessor = transactionDetailsReportProcessor;
	}

	public CSVService getCsvService() {
		return csvService;
	}

	public void setCsvService(CSVService csvService) {
		this.csvService = csvService;
	}

	private String[] convertPurchaseToRow(HistoricEposPurchase purchase)
	{
		String[] result = new String[7];
		
		result[0] = ""+purchase.getTransactionId();
		result[1] = ""+purchase.getGarmentData().getSkuId();
		result[2] = purchase.getGarmentData().getColourDesc();
		result[3] = purchase.getGarmentData().getSize1();
		result[4] = EposUtils.formatCurrencyNoCommas(purchase.getRetailPrice());
		result[5] = EposUtils.formatCurrencyNoCommas(purchase.getPricePaid());
		result[6] = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(purchase.getPurchaseDate());
		
		return result;
	}

	private String[] getRowHeadings()
	{
		String[] result = new String[7];
		
		result[0] = "Transaction ID";
		result[1] = "SKU ID";
		result[2] = "Colour";
		result[3] = "Size";
		result[4] = "RRP";
		result[5] = "Sale Price";
		result[6] = "Date";
		
		return result;
	}
	
	private List<SummaryInformation> getSummaryInfo1(GarmentCommonInfo garmentCommonInfo)
	{
		List<SummaryInformation> summaryInfo1List = new ArrayList<SummaryInformation>();
		
		summaryInfo1List.add(new SummaryInformation("Garment Code", garmentCommonInfo.getGarmentCode()));
		summaryInfo1List.add(new SummaryInformation("Brand", garmentCommonInfo.getBrand()));
		summaryInfo1List.add(new SummaryInformation("Description", garmentCommonInfo.getDescription()));
		summaryInfo1List.add(new SummaryInformation("Style", garmentCommonInfo.getStyle()));
		summaryInfo1List.add(new SummaryInformation("Status", garmentCommonInfo.getStatus()));
		
		return summaryInfo1List;
	}
	
	private List<SummaryInformation> getSummaryInfo2(GarmentCommonInfo garmentCommonInfo)
	{
		List<SummaryInformation> summaryInfo2List = new ArrayList<SummaryInformation>();
		
		summaryInfo2List.add(new SummaryInformation("Gender", garmentCommonInfo.getGender()));
		summaryInfo2List.add(new SummaryInformation("Colour Matrix", garmentCommonInfo.getColourMatrix()));
		summaryInfo2List.add(new SummaryInformation("Size Matrix", garmentCommonInfo.getSizeMatrix()));
		summaryInfo2List.add(new SummaryInformation("Year", garmentCommonInfo.getYear()));
		summaryInfo2List.add(new SummaryInformation("Season", garmentCommonInfo.getSeason()));
		
		return summaryInfo2List;
	}
	
	private void closeView()
	{
		if(view != null)
		{
			view.close();
			view = null;
		}
	}
	
	@Override
	public void onGenericReportTemplateView1Event(GenericReportTemplateView1Event evt) 
	{
		switch(evt.getType())
		{
			case EXPORT_CSV:
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
				break;
				
			case DATA_SELECT:
				long transactionId = Long.valueOf(((String[])evt.getDataSelected())[0]); 
				int result = view.showConfirmMessage("Show details for transaction #" + transactionId + " ?");
				
				if(result == JOptionPane.YES_OPTION)
				{
					transactionDetailsReportProcessor.showTransactionDetailsReport(transactionId);
				}
				break;
				
		}

	}
}
