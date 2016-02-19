package jjdevine.epos.processors.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jjdevine.csv.svc.CSVService;
import jjdevine.csv.svc.beans.CSVRequest;
import jjdevine.epos.EposContext;
import jjdevine.epos.common.Constants;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.beans.GarmentData;
import jjdevine.epos.common.persistence.EposCommonDatasource;
import jjdevine.epos.garment.data.svc.GarmentDataService;
import jjdevine.epos.processors.ShowStockReportProcessor;
import jjdevine.epos.reports.genericreporttemplate1.view.beans.GenericReportTemplate1ViewBean;
import jjdevine.epos.reports.genericreporttemplate1.view.beans.SummaryInformation;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Event;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Listener;
import jjdevine.epos.reports.genericreporttemplate1.view.events.GenericReportTemplateView1Event.Type;
import jjdevine.epos.reports.genericreporttemplate1.view.gui.GenericReportTemplateView1;
import jjdevine.epos.services.manageorder.persistence.ManageOrderDatasource;
import jjdevine.epos.services.orders.common.beans.OrderLineItemBean;
import jjdevine.epos.utils.Utils;

public class ShowStockReportProcessorImpl implements ShowStockReportProcessor, GenericReportTemplateView1Listener 
{
	private GarmentDataService garmentDataService;
	private ManageOrderDatasource manageOrderDatasource;
	private EposCommonDatasource eposCommonDatasource;
	private GenericReportTemplateView1 view;
	private CSVService csvService;
	
	@Override
	public void showStockReport()
	{
		List<GarmentData> garments = eposCommonDatasource.searchForGarments(null, null, null, null, null, null);

		if(garments == null)
		{
			//TODO display error
			return;
		}
		
		/*
		 * need to create cost price map - best thing to do is create a map of garmentData lists, organised by garment code,
		 * and then pass each list into the createSkuIdCostPriceMap method and then merge all the results together at the end
		 */
		
		Map<String, List<GarmentData>> garmentsByCode = new HashMap<String, List<GarmentData>>();
		//loop variables
		List<GarmentData> garmentList;
		for(GarmentData garment: garments)
		{
			//check if this garmentCode has already been encountered
			garmentList = garmentsByCode.get(garment.getGarmentCode());
			
			if(garmentList == null)
			{
				//not encountered this garment code before, need to create new entry for it
				garmentList = new ArrayList<GarmentData>();
				garmentsByCode.put(garment.getGarmentCode(), garmentList);
			}
			
			garmentList.add(garment);
		}
		
		/*
		 * now all garments are organised in the Map, need to pass them into the createSkuIdCostPriceMap method and merge the outcomes
		 */
		
		Map<Long, Double> mergedCostPriceMap = new HashMap<Long, Double>();
		
		Set<String> garmentCodes = garmentsByCode.keySet();
		
		//loop variables
		Map<Long, Double> costPriceMap;
		for(String garmentCode: garmentCodes)
		{
			costPriceMap = createSkuIdCostPriceMap(garmentsByCode.get(garmentCode), garmentCode);
			mergedCostPriceMap.putAll(costPriceMap);
		}
		
		/*
		 * now calculate full report data
		 */

		GenericReportTemplate1ViewBean viewBean = new GenericReportTemplate1ViewBean();
		viewBean.setReportTitle("Stock Report for All Garments");
		
		processReportData(viewBean, mergedCostPriceMap, garments);
		
		closeView(); //close any views already open
		view = EposContext.getGenericReportTemplateView1();
		view.addListener(this);
		view.showReport(viewBean);
	}

	@Override
	public void showStockReportByGarmentCode(String garmentCode) 
	{	
		List<GarmentData> garments = garmentDataService.getGarmentDataByGarmentCode(garmentCode);
		
		if(garments == null)
		{
			//TODO display error
			return;
		}
		
		Map<Long, Double> costPriceMap = createSkuIdCostPriceMap(garments, garmentCode);
		
		GenericReportTemplate1ViewBean viewBean = new GenericReportTemplate1ViewBean();
		viewBean.setReportTitle("Stock Report for Garment " + garmentCode);
		
		processReportData(viewBean, costPriceMap, garments);
			
		closeView(); //close any views already open
		view = EposContext.getGenericReportTemplateView1();
		view.addListener(this);
		view.showReport(viewBean);
	}
	
	private void processReportData(GenericReportTemplate1ViewBean viewBean, Map<Long, Double> costPriceMap, List<GarmentData> garments)
	{
		int garmentsInStock = 0;
		double totalRetailValue = 0;
		double totalCostPrice = 0;
		int garmentsOnSale = 0;
		int garmentsNotOnSale = 0;
		
		List<String[]> detailRows = new ArrayList<String[]>();
		
		//loop variables
		Double costPrice;
		String[] row;
		for(GarmentData garment: garments)
		{
			costPrice = costPriceMap.get(garment.getSkuId());

			int qty = garment.getQuantity();
			//count total garments
			garmentsInStock += qty;
			
			//count retail value
			totalRetailValue += garment.getRetailPrice() * qty;
			
			//count cost price
			if(costPrice != null)
			{
				totalCostPrice += costPrice * qty;
			}
			
			//count not/on sale
			if(garment.getStatusId() == Constants.GARMENT_ON_SALE)
			{
				garmentsOnSale += qty;
			}
			else
			{
				garmentsNotOnSale += qty;
			}
			
			//row details
			row = new String[8];
			row[0] = garment.getGarmentCode();
			row[1] = garment.getColourDesc();
			row[2] = garment.getSize1();
			row[3] = EposUtils.formatCurrencyNoCommas(garment.getRetailPrice());
			row[4] = EposUtils.formatCurrencyNoCommas(garment.getMemberPrice());
			row[5] = costPrice == null ? "n/a" : EposUtils.formatCurrencyNoCommas(costPrice);
			row[6] = qty+"";
			row[7] = garment.getStatus();
			
			detailRows.add(row);
		}
		
		viewBean.setDetailedInformationHeadings(getDetailRowHeadings());
		viewBean.setDetailedInformationRows(detailRows);
		
		List<SummaryInformation> summaryInfo1 = new ArrayList<SummaryInformation>();
		List<SummaryInformation> summaryInfo2 = new ArrayList<SummaryInformation>();
		
		SummaryInformation summaryInfo = new SummaryInformation();
		summaryInfo.setInformation("Number of Garments in Stock");
		summaryInfo.setValue(garmentsInStock+"");
		summaryInfo1.add(summaryInfo);
		
		summaryInfo = new SummaryInformation();
		summaryInfo.setInformation("Total Retail Price");
		summaryInfo.setValue(EposUtils.formatCurrencyNoCommas(totalRetailValue));
		summaryInfo1.add(summaryInfo);
		
		summaryInfo = new SummaryInformation();
		summaryInfo.setInformation("Total Cost price");
		summaryInfo.setValue(EposUtils.formatCurrencyNoCommas(totalCostPrice));
		summaryInfo1.add(summaryInfo);
		
		viewBean.setSummaryInformation1(summaryInfo1);
		
		summaryInfo = new SummaryInformation();
		summaryInfo.setInformation("Number of Garments on Sale");
		summaryInfo.setValue(garmentsOnSale+"");
		summaryInfo2.add(summaryInfo);
		
		summaryInfo = new SummaryInformation();
		summaryInfo.setInformation("Number of Garments Not on Sale");
		summaryInfo.setValue(garmentsNotOnSale+"");
		summaryInfo2.add(summaryInfo);
		
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
	
	private String[] getDetailRowHeadings()
	{
		return new String[]{
			"Garment Code",
			"Colour",
			"Size",
			"Retail Price",
			"Member Price",
			"Cost Price",
			"Qty in Stock",
			"On Sale"
		};
	}
	
	private Map<Long, Double> createSkuIdCostPriceMap(List<GarmentData> garments, String garmentCode)
	{
		/*
		 * first step is to calculate for each sku id, the number of garments ordered and the total price paid
		 */
		
		List<OrderLineItemBean> lineItems = manageOrderDatasource.getAllOrderLineItemsForGarmentCode(garmentCode);
		
		//this will map <SkuId, [num_items_ordered, total_price_paid_pence]
		Map<Long, long[]> tally = new HashMap<Long, long[]>(); 
		//sku id
		//total num garments ordered 
		//total price paid
		
		for(OrderLineItemBean lineItem: lineItems)
		{
			long skuId = getSkuIdForLineItem(lineItem, garments);
			
			long[] totals = tally.get(skuId);
			
			if(totals == null)
			{
				//first time this skuId encountered
				totals = new long[2];
				totals[0] = lineItem.getQtyDelivered();
				totals[1] = (long)(lineItem.getCostPriceUnit() * lineItem.getQtyDelivered() * 100);
				
				tally.put(skuId, totals);
			}
			else
			{
				//skuId already exists in tally, add to existing totals
				totals[0] = totals[0] + lineItem.getQtyDelivered();
				totals[1] = totals[1] + ((long)(lineItem.getCostPriceUnit() * lineItem.getQtyDelivered() * 100));
			}
		}

		Map<Long, Double> costPriceMap = new HashMap<Long, Double>();
		
		Set<Long> keys = tally.keySet();
		
		for(Long key: keys)
		{
			long[] totals = tally.get(key);
			
			long costPrice = totals[0] != 0 ? totals[1] / totals[0] : 0;
			
			costPriceMap.put(key, EposUtils.roundToTwoPlaces(new Double(costPrice+"")/100));
		}
		
		return costPriceMap;
		
		//TODO note = may need to put some report generation in a Thread as it could take a while
	}

	private long getSkuIdForLineItem(OrderLineItemBean lineItem, List<GarmentData> garments) 
	{
		for(GarmentData data: garments)
		{
			if(lineItem.getColour().getColourID() == data.getColourId()
				&& lineItem.getSize().getSizeID() == data.getSizeId())
			{
				return data.getSkuId();
			}
		}
		return -1;
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

	public GarmentDataService getGarmentDataService() {
		return garmentDataService;
	}

	public void setGarmentDataService(GarmentDataService garmentDataService) {
		this.garmentDataService = garmentDataService;
	}

	public ManageOrderDatasource getManageOrderDatasource() {
		return manageOrderDatasource;
	}

	public void setManageOrderDatasource(ManageOrderDatasource manageOrderDatasource) {
		this.manageOrderDatasource = manageOrderDatasource;
	}

	public EposCommonDatasource getEposCommonDatasource() {
		return eposCommonDatasource;
	}

	public void setEposCommonDatasource(EposCommonDatasource eposCommonDatasource) {
		this.eposCommonDatasource = eposCommonDatasource;
	}

	public CSVService getCsvService() {
		return csvService;
	}

	public void setCsvService(CSVService csvService) {
		this.csvService = csvService;
	}

}
