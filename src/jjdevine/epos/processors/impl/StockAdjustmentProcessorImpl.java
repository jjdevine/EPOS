package jjdevine.epos.processors.impl;

import java.util.List;

import org.apache.log4j.Logger;

import jjdevine.epos.EposContext;
import jjdevine.epos.common.beans.ColourInfo;
import jjdevine.epos.common.beans.ColourMatrixInfo;
import jjdevine.epos.common.beans.GarmentCommonInfo;
import jjdevine.epos.common.beans.GarmentData;
import jjdevine.epos.common.beans.SizeMatrixInfo;
import jjdevine.epos.common.beans.SizeMatrixSize;
import jjdevine.epos.common.persistence.EposCommonDatasource;
import jjdevine.epos.common.persistence.EposCommonDatasourceFactory;
import jjdevine.epos.garment.data.svc.GarmentDataService;
import jjdevine.epos.processors.StockAdjustmentProcessor;
import jjdevine.epos.view.stockadjustment.beans.StockAdjustmentViewBean;
import jjdevine.epos.view.stockadjustment.events.StockAdjustmentViewEvent;
import jjdevine.epos.view.stockadjustment.events.StockAdjustmentViewListener;
import jjdevine.epos.view.stockadjustment.gui.StockAdjustmentView;

public class StockAdjustmentProcessorImpl implements StockAdjustmentProcessor, StockAdjustmentViewListener
{
	private EposCommonDatasource datasource = EposCommonDatasourceFactory.createEposDatasource();
	private GarmentDataService garmentDataService;
	private StockAdjustmentView view;
	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void adjustStock(String garmentCode) 
	{
		GarmentCommonInfo commonInfo =  datasource.getGarmentCommonInfoByCode(garmentCode);
		SizeMatrixInfo sizeInfo = datasource.getSizeMatrixByDescription(commonInfo.getSizeMatrix());
		ColourMatrixInfo colourInfo = datasource.getColourMatrixByDescription(commonInfo.getColourMatrix());
		
		List<GarmentData> garments = garmentDataService.getGarmentDataByGarmentCode(garmentCode);

		StockAdjustmentViewBean viewBean = new StockAdjustmentViewBean();
		viewBean.setBrand(commonInfo.getBrand());
		viewBean.setColourMatrix(commonInfo.getColourMatrix());
		viewBean.setDescription(commonInfo.getDescription());
		viewBean.setGarmentCode(garmentCode);
		viewBean.setSizeMatrix(commonInfo.getSizeMatrix());
		viewBean.setStyle(commonInfo.getStyle());
		
		viewBean.setSizes(sizeInfo.getSizes());
		viewBean.setColours(colourInfo.getColours());
		
		int[][] qtyInStock = getQty(garments, sizeInfo.getSizes(), colourInfo.getColours());
		viewBean.setQtyInStock(qtyInStock);
		
		//TODO close view if open
		
		view = EposContext.getStockAdjustmentView();
		view.setViewBean(viewBean);
		view.addListener(this);
		
		
		// TODO Auto-generated method stub
		
	}

	private int[][] getQty(List<GarmentData> garments, List<SizeMatrixSize> sizes, List<ColourInfo> colours) 
	{
		//[sizes][colours]
		
		int[][] qtyInStock = new int[sizes.size()][colours.size()];
		
		for(GarmentData garment: garments)
		{
			int sizeId = garment.getSizeId();
			int colourId = garment.getColourId();
			
			int sizeIndex = -1;
			int colourIndex = -1;
			
			for(SizeMatrixSize size: sizes)
			{
				sizeIndex++;
				if(size.getSizeId() == sizeId)
				{
					break;
				}
			}
			
			for(ColourInfo colour: colours)
			{
				colourIndex++;
				if(colour.getColourID() == colourId)
				{
					break;
				}
			}
			
			qtyInStock[sizeIndex][colourIndex] = garment.getQuantity();
		}
		
		return qtyInStock;
	}

	@Override
	public void onStockAdjustmentViewEvent(StockAdjustmentViewEvent evt) 
	{
		switch(evt.getType())
		{
			case CLOSE:
				closeView();
				break;
			case SAVE:

				int x = evt.getOldValues().getQtyInStock().length;
				int y = evt.getOldValues().getQtyInStock()[0].length;
				boolean success = false;
				for(int iX=0; iX<x; iX++)
				{
					for(int iY=0; iY<y; iY++)
					{
						int oldQty = evt.getOldValues().getQtyInStock()[iX][iY];
						int newQty = evt.getNewValues().getQtyInStock()[iX][iY];
						
						if(oldQty != newQty)  //if quantity has been changed by user
						{
							String garmentCode = evt.getNewValues().getGarmentCode();
							int sizeId = evt.getNewValues().getSizes().get(iX).getSizeId();
							int colourId = evt.getNewValues().getColours().get(iY).getColourID();
							
							//update
							success = garmentDataService.updateQuantityInStock(
									garmentCode, 
									sizeId,
									colourId,
									newQty);
							
							if(!success)
							{
								logger.error("Could not update stock for <gamentCode=" + garmentCode + "><size=" + sizeId
										+ "><colourId = " + colourId + ">");
								
								view.showErrorMessage("Could not complete update, please check the logs");
								return;
							}
							
							success = garmentDataService.auditQuantityChange(garmentCode, sizeId, colourId, oldQty, newQty);
							
							if(!success)
							{
								logger.error("Could not audit stock change for <gamentCode=" + garmentCode + "><size=" + sizeId
										+ "><colourId = " + colourId + "><newQty=" + newQty + "><oldQty=" + oldQty + ">");
								
								view.showErrorMessage("Could not complete update, please check the logs");
								return;
							}
						}
					}					
				}
				
				view.showInformationMessage("Changes Saved Successfully!");
				closeView();
		}
		
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

}
