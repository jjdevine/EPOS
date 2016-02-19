package jjdevine.epos.processors.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jjdevine.csv.svc.CSVService;
import jjdevine.csv.svc.beans.CSVRequest;
import jjdevine.epos.EposContext;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.ordersreporting.data.svc.OrdersReportingDataService;
import jjdevine.epos.ordersreporting.data.svc.beans.OrdersDetailsRowDTO;
import jjdevine.epos.processors.ShowOrdersReportProcessor;
import jjdevine.epos.reports.genericreporttemplate1.view.beans.GenericReportTemplate1ViewBean;
import jjdevine.epos.reports.genericreporttemplate1.view.beans.SummaryInformation;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Event;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Listener;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Event.Type;
import jjdevine.epos.reports.genericreporttemplate1.view.gui.GenericReportTemplateView1;
import jjdevine.epos.utils.Utils;

public class ShowOrdersReportProcessorImpl implements ShowOrdersReportProcessor, GenericReportTemplateView1Listener 
{
	private OrdersReportingDataService ordersReportingDataService;
	private GenericReportTemplateView1 view;
	private CSVService csvService;
	
	@Override
	public void showOrdersReport(String garmentCode, Date dateFrom, Date dateTo) 
	{
		List<OrdersDetailsRowDTO> data = ordersReportingDataService.getOrdersDetails(garmentCode, dateFrom, dateTo);
		
		if(data == null)
		{
			//TODO: display error
			return;
		}
		
		GenericReportTemplate1ViewBean viewBean = new GenericReportTemplate1ViewBean();
		
		String reportTitle = "Orders Report";
		if(garmentCode != null)
		{
			reportTitle += " Garment Code " + garmentCode;
		}
		if(dateFrom != null)
		{
			reportTitle += " From " + EposUtils.formatDate(dateFrom);
		}
		if(dateTo != null)
		{
			reportTitle += " To " + EposUtils.formatDate(dateTo);
		}
		viewBean.setReportTitle(reportTitle);
		
		viewBean.setDetailedInformationHeadings(new String[]{
				"Order Name",
				"Garment Code",
				"Size",
				"Colour",
				"Qty Ordered",
				"Qty Delivered",
				"Price/Unit",
				"Due Date"
		});
		
		List<String[]> detailsRows = new ArrayList<String[]>();
		List<SummaryInformation> summaryInfo1 = new ArrayList<SummaryInformation>();
		List<SummaryInformation> summaryInfo2 = new ArrayList<SummaryInformation>();
		
		int garmentsOrdered = 0;
		int garmentsDelivered = 0;
		long totCostPriceOrdered=0;
		long totCostPriceDelivered=0;
		
		for(OrdersDetailsRowDTO rowDTO: data)
		{
			String[] row = new String[]{
					rowDTO.getOrderName(),
					rowDTO.getGarmentCode(),
					rowDTO.getSize(),
					rowDTO.getColour(),
					rowDTO.getQtyOrdered()+"",
					rowDTO.getQtyDelivered()+"",
					EposUtils.formatCurrencyNoCommas(rowDTO.getPricePerUnit()),
					EposUtils.formatDate(rowDTO.getTargetDate())
			};
			
			garmentsOrdered += rowDTO.getQtyOrdered();
			garmentsDelivered += rowDTO.getQtyDelivered();
			totCostPriceOrdered += EposUtils.convertCurrencyDoubleToLong(rowDTO.getPricePerUnit())*rowDTO.getQtyOrdered();
			totCostPriceDelivered += EposUtils.convertCurrencyDoubleToLong(rowDTO.getPricePerUnit())*rowDTO.getQtyDelivered();
			
			detailsRows.add(row);
		}
		
		double valueOrdered = EposUtils.convertCurrencyLongToDouble(totCostPriceOrdered);
		double valueDelivered = EposUtils.convertCurrencyLongToDouble(totCostPriceDelivered);
		
		
		summaryInfo1.add(new SummaryInformation("Garments Ordered", ""+garmentsOrdered));
		summaryInfo1.add(new SummaryInformation("Garments Delivered", ""+garmentsDelivered));
		
		summaryInfo2.add(new SummaryInformation("Value Ordered", EposUtils.formatCurrencyNoCommas(valueOrdered)));
		summaryInfo2.add(new SummaryInformation("Value Delivered", EposUtils.formatCurrencyNoCommas(valueDelivered)));
		
		viewBean.setDetailedInformationRows(detailsRows);
		viewBean.setSummaryInformation1(summaryInfo1);
		viewBean.setSummaryInformation2(summaryInfo2);
		
		closeView();
		view = EposContext.getGenericReportTemplateView1();
		view.addListener(this);
		view.showReport(viewBean);
	}
	
	private void closeView()
	{
		if(view != null)
		{
			view.close();
			view = null;
		}
	}

	public void setOrdersReportingDataService(
			OrdersReportingDataService ordersReportingDataService) {
		this.ordersReportingDataService = ordersReportingDataService;
	}

	public OrdersReportingDataService getOrdersReportingDataService() {
		return ordersReportingDataService;
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
