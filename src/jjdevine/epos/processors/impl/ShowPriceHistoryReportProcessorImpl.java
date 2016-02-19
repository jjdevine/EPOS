package jjdevine.epos.processors.impl;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import jjdevine.csv.svc.CSVService;
import jjdevine.csv.svc.beans.CSVData;
import jjdevine.csv.svc.beans.CSVModel;
import jjdevine.csv.svc.beans.CSVRequest;
import jjdevine.epos.EposContext;
import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.beans.GarmentCommonInfo;
import jjdevine.epos.common.beans.GarmentData;
import jjdevine.epos.common.persistence.EposCommonDatasource;
import jjdevine.epos.common.persistence.EposCommonDatasourceFactory;
import jjdevine.epos.garment.data.svc.GarmentDataService;
import jjdevine.epos.pricehistory.model.beans.PriceChangeBean;
import jjdevine.epos.processors.ShowPriceHistoryReportProcessor;
import jjdevine.epos.reports.pricehistory.view.beans.PriceHistoryReportItemDataBean;
import jjdevine.epos.reports.pricehistory.view.beans.PriceHistoryReportViewBean;
import jjdevine.epos.reports.pricehistory.view.events.PriceHistoryReportViewEvent;
import jjdevine.epos.reports.pricehistory.view.events.PriceHistoryReportViewListener;
import jjdevine.epos.reports.pricehistory.view.gui.PriceHistoryReportView;
import jjdevine.epos.utils.Utils;

public class ShowPriceHistoryReportProcessorImpl implements
		ShowPriceHistoryReportProcessor, PriceHistoryReportViewListener 
{
	private GarmentDataService garmentDataService;
	private EposCommonDatasource eposCommonDatasource;
	private PriceHistoryReportView view;
	private CSVService csvService;
	private Logger logger = Logger.getLogger(this.getClass());
	
	public ShowPriceHistoryReportProcessorImpl()
	{
		//TODO do this with Spring
		eposCommonDatasource = EposCommonDatasourceFactory.createEposDatasource();
	}
	
	@Override
	public void showPriceHistoryReportForGarmentCode(String garmentCode) 
	{
		List<PriceChangeBean> priceChangeList = garmentDataService.getPriceChangesByGarmentCode(garmentCode);
		List<GarmentData> garmentDataList = garmentDataService.getGarmentDataByGarmentCode(garmentCode);
		GarmentCommonInfo commonInfo = eposCommonDatasource.getGarmentCommonInfoByCode(garmentCode);
		
		if(priceChangeList == null)
		{
			//TODO show error
			return;
		}
		
		if(garmentDataList == null || garmentDataList.size() == 0)
		{
			//TODO show error
			return;
		}
		
		PriceHistoryReportViewBean viewBean = new PriceHistoryReportViewBean();
		
		viewBean.setBrand(commonInfo.getBrand());
		viewBean.setColourMatrix(commonInfo.getColourMatrix());
		viewBean.setDescription(commonInfo.getDescription());
		viewBean.setGarmentCode(garmentCode);
		viewBean.setGender(commonInfo.getGender());
		viewBean.setSeason(commonInfo.getSeason());
		viewBean.setSizeMatrix(commonInfo.getSizeMatrix());
		viewBean.setStatus(commonInfo.getStatus());
		viewBean.setStyle(commonInfo.getStyle());
		viewBean.setYear(commonInfo.getYear());
		
		/*
		 * now get details
		 */
		
		List<PriceHistoryReportItemDataBean> dataBeans = new ArrayList<PriceHistoryReportItemDataBean>();
		viewBean.setItems(dataBeans);
		
		for(GarmentData garmentData: garmentDataList)
		{
			List<PriceHistoryReportItemDataBean> changesForThisGarment = getChangesForThisGarment(priceChangeList, garmentData);
			dataBeans.addAll(changesForThisGarment);
		}
		
		closeView();
		view = EposContext.getPriceHistoryReportView();
		view.addListener(this);
		view.showEndOfDayReport(viewBean);
	}
	
	@Override
	public void showPriceHistoryReportForSKUID(long skuId) 
	{
		List<PriceChangeBean> priceChangeList = garmentDataService.getPriceChangesBySKUID(skuId);
		GarmentData garmentData = garmentDataService.getGarmentDataBySKUId(skuId);
		
		if(priceChangeList == null)
		{
			//TODO: show error
			return;
		}
		
		if(garmentData == null)
		{
			//TODO: show error
			return;
		}
		
		PriceHistoryReportViewBean viewBean = new PriceHistoryReportViewBean();
		
		viewBean.setBrand(garmentData.getBrand());
		viewBean.setColourMatrix(garmentData.getColourMatrix());
		viewBean.setDescription(garmentData.getDescription());
		viewBean.setGarmentCode(garmentData.getGarmentCode());
		viewBean.setGender(garmentData.getGender());
		viewBean.setSeason(garmentData.getSeason());
		viewBean.setSizeMatrix(garmentData.getSizeMatrix());
		viewBean.setStatus(garmentData.getStatus());
		viewBean.setStyle(garmentData.getStyle());
		viewBean.setYear(garmentData.getYear());
		
		List<PriceHistoryReportItemDataBean> dataBeans = new ArrayList<PriceHistoryReportItemDataBean>();
		viewBean.setItems(dataBeans);

		List<PriceHistoryReportItemDataBean> changesForThisGarment = getChangesForThisGarment(priceChangeList, garmentData);
		dataBeans.addAll(changesForThisGarment);

		closeView();
		view = EposContext.getPriceHistoryReportView();
		view.addListener(this);
		view.showEndOfDayReport(viewBean);
	}
	
	private List<PriceHistoryReportItemDataBean> getChangesForThisGarment(List<PriceChangeBean> priceChangeList, GarmentData garmentData) 
	{
		List<PriceHistoryReportItemDataBean> result = new ArrayList<PriceHistoryReportItemDataBean>();
		long skuId = garmentData.getSkuId();
		
		PriceChangeBean previousPriceChangeBean = null;
		PriceHistoryReportItemDataBean currentBean = null;
		boolean firstItem = true;
		
		//1. set the 'date to' with current bean
		//2. 'date from' should be the 'date to' of previous bean
		
		for(PriceChangeBean priceChangeBean: priceChangeList)
		{
			if(priceChangeBean.getSkuId() != skuId)
			{
				continue;
			}
			
			currentBean = new PriceHistoryReportItemDataBean();
			
			if(firstItem)
			{
				firstItem=false;
				
			}
			else
			{
				currentBean.setDateFrom(previousPriceChangeBean.getDateChanged());
			}
			
			currentBean.setDateTo(priceChangeBean.getDateChanged());
			
			currentBean.setColour(garmentData.getColourDesc());
			currentBean.setMemberPrice(priceChangeBean.getOldMemberPrice());
			currentBean.setRetailPrice(priceChangeBean.getOldRetailPrice());
			currentBean.setRrp(priceChangeBean.getOldRRP());
			currentBean.setSize(garmentData.getSize1());
			currentBean.setSkuId(priceChangeBean.getSkuId());
			
			result.add(currentBean);
			
			//for next iteration:;
			previousPriceChangeBean = priceChangeBean;
		}
		
		/*
		 * add current price data
		 */
		
		PriceHistoryReportItemDataBean currentPriceBean = new PriceHistoryReportItemDataBean();
		currentPriceBean.setColour(garmentData.getColourDesc());
		currentPriceBean.setMemberPrice(garmentData.getMemberPrice());
		currentPriceBean.setRetailPrice(garmentData.getRetailPrice());
		currentPriceBean.setRrp(garmentData.getRrp());
		currentPriceBean.setSize(garmentData.getSize1());
		currentPriceBean.setSkuId(garmentData.getSkuId());
		if(previousPriceChangeBean != null)
		{
			currentPriceBean.setDateFrom(previousPriceChangeBean.getDateChanged());
		}
		
		result.add(currentPriceBean);
		
		return result;
	}

	public GarmentDataService getGarmentDataService() {
		return garmentDataService;
	}

	public void setGarmentDataService(GarmentDataService garmentDataService) {
		this.garmentDataService = garmentDataService;
	}

	public CSVService getCsvService() {
		return csvService;
	}

	public void setCsvService(CSVService csvService) {
		this.csvService = csvService;
	}

	@Override
	public void onPriceHistoryReportViewEvent(PriceHistoryReportViewEvent evt) 
	{
		switch(evt.getType())
		{
			case EXPORT_CSV:
				try
				{
					exportCSV(evt.getPriceHistoryReportViewBean());
				}
				catch(Exception ex)
				{
					view.showErrorMessage("Could not export CSV file: " + ex.getMessage());
					logger.error(ex.getMessage(), ex);
				}
				break;
		}
	}

	private void exportCSV(PriceHistoryReportViewBean priceHistoryReportViewBean) 
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
		 * format to csv model
		 */
		
		
		CSVModel csvModel = new CSVModel();
		
		CSVData csvData;
		
		csvData = new CSVData(0,0);
		csvData.setValue("Pricing History Report");
		csvModel.addData(csvData);
		
		csvData = new CSVData(2,0);
		csvData.setValue("Summary Information");
		csvModel.addData(csvData);
		
		csvData = new CSVData(4,0);
		csvData.setValue("Information");
		csvModel.addData(csvData);
		
		csvData = new CSVData(4,1);
		csvData.setValue("Value");
		csvModel.addData(csvData);
		
		csvData = new CSVData(5,0);
		csvData.setValue("Garment Code");
		csvModel.addData(csvData);
		
		csvData = new CSVData(5,1);
		csvData.setValue(priceHistoryReportViewBean.getGarmentCode());
		csvModel.addData(csvData);
		
		csvData = new CSVData(6,0);
		csvData.setValue("Brand");
		csvModel.addData(csvData);
		
		csvData = new CSVData(6,1);
		csvData.setValue(priceHistoryReportViewBean.getBrand());
		csvModel.addData(csvData);
		
		csvData = new CSVData(7,0);
		csvData.setValue("Description");
		csvModel.addData(csvData);
		
		csvData = new CSVData(7,1);
		csvData.setValue(priceHistoryReportViewBean.getDescription());
		csvModel.addData(csvData);
		
		csvData = new CSVData(8,0);
		csvData.setValue("Style");
		csvModel.addData(csvData);
		
		csvData = new CSVData(8,1);
		csvData.setValue(priceHistoryReportViewBean.getStyle());
		csvModel.addData(csvData);
		
		csvData = new CSVData(9,0);
		csvData.setValue("Status");
		csvModel.addData(csvData);
		
		csvData = new CSVData(9,1);
		csvData.setValue(priceHistoryReportViewBean.getStatus());
		csvModel.addData(csvData);
		
		csvData = new CSVData(4,3);
		csvData.setValue("Information");
		csvModel.addData(csvData);
		
		csvData = new CSVData(4,4);
		csvData.setValue("Value");
		csvModel.addData(csvData);
		
		csvData = new CSVData(5,3);
		csvData.setValue("Gender");
		csvModel.addData(csvData);
		
		csvData = new CSVData(5,4);
		csvData.setValue(priceHistoryReportViewBean.getGender());
		csvModel.addData(csvData);
		
		csvData = new CSVData(6,3);
		csvData.setValue("Colour Matrix");
		csvModel.addData(csvData);
		
		csvData = new CSVData(6,4);
		csvData.setValue(priceHistoryReportViewBean.getColourMatrix());
		csvModel.addData(csvData);
		
		csvData = new CSVData(7,3);
		csvData.setValue("Size Matrix");
		csvModel.addData(csvData);
		
		csvData = new CSVData(7,4);
		csvData.setValue(priceHistoryReportViewBean.getSizeMatrix());
		csvModel.addData(csvData);
		
		csvData = new CSVData(8,3);
		csvData.setValue("Year");
		csvModel.addData(csvData);
		
		csvData = new CSVData(8,4);
		csvData.setValue(priceHistoryReportViewBean.getYear());
		csvModel.addData(csvData);
		
		csvData = new CSVData(9,3);
		csvData.setValue("Season");
		csvModel.addData(csvData);
		
		csvData = new CSVData(9,4);
		csvData.setValue(priceHistoryReportViewBean.getSeason());
		csvModel.addData(csvData);
		
		/*
		 * price history section
		 */
		
		/*
		 * headings
		 */
		
		csvData = new CSVData(11,0);
		csvData.setValue("SKU ID");
		csvModel.addData(csvData);
		
		csvData = new CSVData(11,1);
		csvData.setValue("Colour");
		csvModel.addData(csvData);
		
		csvData = new CSVData(11,2);
		csvData.setValue("Size");
		csvModel.addData(csvData);
		
		csvData = new CSVData(11,3);
		csvData.setValue("Date From");
		csvModel.addData(csvData);
		
		csvData = new CSVData(11,4);
		csvData.setValue("Date To");
		csvModel.addData(csvData);
		
		csvData = new CSVData(11,5);
		csvData.setValue("Retail Price");
		csvModel.addData(csvData);
		
		csvData = new CSVData(11,6);
		csvData.setValue("RRP");
		csvModel.addData(csvData);
		
		csvData = new CSVData(11,7);
		csvData.setValue("Member Price");
		csvModel.addData(csvData);
		
		/*
		 * loop round details
		 */
		
		int row = 12;
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		
		for(PriceHistoryReportItemDataBean data: priceHistoryReportViewBean.getItems())
		{
			csvData = new CSVData(row,0);
			csvData.setValue(""+data.getSkuId());
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,1);
			csvData.setValue(data.getColour());
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,2);
			csvData.setValue(data.getSize());
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,3);
			csvData.setValue(data.getDateFrom()==null ? "" : df.format(data.getDateFrom()));
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,4);
			csvData.setValue(data.getDateTo()==null ? "" : df.format(data.getDateTo()));
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,5);
			csvData.setValue(EposUtils.formatCurrencyNoCommas(data.getRetailPrice()));
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,6);
			csvData.setValue(EposUtils.formatCurrencyNoCommas(data.getRrp()));
			csvModel.addData(csvData);
			
			csvData = new CSVData(row,7);
			csvData.setValue(EposUtils.formatCurrencyNoCommas(data.getMemberPrice()));
			csvModel.addData(csvData);
			
			row++;
		}	
		
		/*
		 * output csv
		 */

		CSVRequest csvReq = new CSVRequest();
		csvReq.setFile(file);
		csvReq.setModel(csvModel);
		
		csvService.createCSV(csvReq);
		
		view.showMessage("CSV Created: " + file.getPath());
		closeView();
	}
	
	private void closeView()
	{
		if(view != null)
		{
			view.close();
			view = null;
		}
	}
}
