package jjdevine.epos.processors.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jjdevine.csv.svc.CSVService;
import jjdevine.csv.svc.beans.CSVRequest;
import jjdevine.epos.EposContext;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.garment.data.svc.GarmentDataService;
import jjdevine.epos.processors.ShowStockAdjustmentReportProcessor;
import jjdevine.epos.reports.genericreporttemplate1.view.beans.GenericReportTemplate1ViewBean;
import jjdevine.epos.reports.genericreporttemplate1.view.beans.SummaryInformation;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Event;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Listener;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Event.Type;
import jjdevine.epos.reports.genericreporttemplate1.view.gui.GenericReportTemplateView1;
import jjdevine.epos.stockadjustment.model.beans.StockAdjustment;
import jjdevine.epos.utils.Utils;

public class ShowStockAdjustmentReportProcessorImpl implements ShowStockAdjustmentReportProcessor, GenericReportTemplateView1Listener
{
	private GarmentDataService garmentDataService;
	private GenericReportTemplateView1 view;
	private CSVService csvService;

	@Override
	public void showStockAdjustmentReport() 
	{
		List<StockAdjustment> adjustments = garmentDataService.getStockAdjustments();
		
		GenericReportTemplate1ViewBean viewBean = new GenericReportTemplate1ViewBean();
		
		//report title
		viewBean.setReportTitle("Stock Adjustment Report");
		
		setupViewBean(viewBean, adjustments);
		
		closeView();
		view = EposContext.getGenericReportTemplateView1();
		view.showReport(viewBean);
		view.addListener(this);
	}

	@Override
	public void showStockAdjustmentReport(String garmentCode) 
	{
		List<StockAdjustment> adjustments = garmentDataService.getStockAdjustmentsByGarmentCode(garmentCode);
		
		GenericReportTemplate1ViewBean viewBean = new GenericReportTemplate1ViewBean();
		
		//report title
		viewBean.setReportTitle("Stock Adjustment Report for Garment " + garmentCode);
		
		setupViewBean(viewBean, adjustments);
		
		closeView();
		view = EposContext.getGenericReportTemplateView1();
		view.showReport(viewBean);
		view.addListener(this);
	}
	
	private void setupViewBean(GenericReportTemplate1ViewBean viewBean, List<StockAdjustment> adjustments)
	{
		//details headings
		viewBean.setDetailedInformationHeadings(new String[]{
				"Garment Code",
				"Size",
				"Colour",
				"New Quantity",
				"Old Quantity",
				"Date Adjusted"
			});
		
		List<String[]> details = new ArrayList<String[]>();
		viewBean.setDetailedInformationRows(details);
		
		long itemsAddedTotal = 0;
		long itemsRemovedTotal = 0;
		
		for(StockAdjustment adjustment: adjustments)
		{
			int change = adjustment.getNewQty() - adjustment.getOldQty();
			
			if(change < 0)  //items removed
			{
				itemsRemovedTotal += Math.abs(change);
			}
			else	//items added
			{
				itemsAddedTotal += change;
			}
			
			details.add(new String[]{
					adjustment.getGarmentCode(),
					adjustment.getSizeDescription(),
					adjustment.getColourDescription(),
					adjustment.getNewQty()+"",
					adjustment.getOldQty()+"",
					EposUtils.formatDate(adjustment.getTimestamp())
			});
		}
		List<SummaryInformation> summaryInfo1 = new ArrayList<SummaryInformation>();
		List<SummaryInformation> summaryInfo2 = new ArrayList<SummaryInformation>();		
		
		SummaryInformation summaryInfo = new SummaryInformation("Adjustment Total", adjustments.size()+"");
		summaryInfo1.add(summaryInfo);
		
		summaryInfo = new SummaryInformation("Garments Added", itemsAddedTotal+"");
		summaryInfo2.add(summaryInfo);
		summaryInfo = new SummaryInformation("Garments Removed", itemsRemovedTotal+"");
		summaryInfo2.add(summaryInfo);
		
		viewBean.setSummaryInformation1(summaryInfo1);
		viewBean.setSummaryInformation2(summaryInfo2);
	}
	
	private void closeView()
	{
		if(view != null)
		{
			view.close();
			view = null;
		}
	}

	public void setGarmentDataService(GarmentDataService garmentDataService) {
		this.garmentDataService = garmentDataService;
	}

	public void setCsvService(CSVService csvService) {
		this.csvService = csvService;
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
	
}

