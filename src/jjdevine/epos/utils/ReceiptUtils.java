package jjdevine.epos.utils;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import jjdevine.epos.receiptsetup.data.svc.beans.ReceiptLineDTO;
import jjdevine.epos.receiptsetup.data.svc.beans.ReceiptSectionDTO;
import jjdevine.epos.receiptsetup.data.svc.beans.ReceiptSetupDTO;
import jjdevine.epos.receiptsetup.data.svc.beans.TransactionStyleDTO;
import jjdevine.epos.receiptsetup.view.beans.ReceiptLine;
import jjdevine.epos.receiptsetup.view.beans.ReceiptSection;
import jjdevine.epos.receiptsetup.view.beans.ReceiptSetup;
import jjdevine.epos.receiptsetup.view.beans.TransactionStyle;

public class ReceiptUtils 
{
	private int nextSetupId, nextSectionId, nextLineId;
	
	public ReceiptSetupDTO copyToReceiptSetupDTO(ReceiptSetup viewBean)
	{
		ReceiptSetupDTO dto = new ReceiptSetupDTO();
		
		dto.setId(nextSetupId++);
		dto.setName(viewBean.getName());
		dto.setTopSection(copyToReceiptSectionDTO(viewBean.getTopSection()));
		dto.setBottomSection(copyToReceiptSectionDTO(viewBean.getBottomSection()));
		//TODO: do this properly:
		dto.setTransactionStyle(TransactionStyleDTO.DEFAULT_STYLE);
		
		return dto;
	}
	
	public ReceiptSectionDTO copyToReceiptSectionDTO(ReceiptSection receiptSection)
	{
		ReceiptSectionDTO dto = new ReceiptSectionDTO();
		
		dto.setId(nextSectionId++);
		List<ReceiptLineDTO> lines = new ArrayList<ReceiptLineDTO>();
		dto.setLines(lines);
		
		for(ReceiptLine line: receiptSection.getLines())
		{
			lines.add(copyToReceiptLineDTO(line));
		}
		
		return dto;
	}
	
	public ReceiptLineDTO copyToReceiptLineDTO(ReceiptLine receiptLine)
	{
		ReceiptLineDTO dto = new ReceiptLineDTO();
		
		dto.setId(nextLineId++);
		
		switch(receiptLine.getType())
		{
			case BLANK:
				dto.setType(ReceiptLineDTO.Type.BLANK);
				break;
			case DIVIDER:
				dto.setType(ReceiptLineDTO.Type.DIVIDER);
				break;
			case TEXT:
				dto.setType(ReceiptLineDTO.Type.TEXT);
				
				switch(receiptLine.getAlignment())
				{
					case LEFT:
						dto.setAlignment(ReceiptLineDTO.Alignment.LEFT);
						break;
					case CENTRE:
						dto.setAlignment(ReceiptLineDTO.Alignment.CENTRE);
						break;
					case RIGHT:
						dto.setAlignment(ReceiptLineDTO.Alignment.RIGHT);
						break;
				}
				
				dto.setText(receiptLine.getText());
				dto.setFont(new Font(receiptLine.getFontName(), receiptLine.getFontType(), receiptLine.getFontSize()));
				break;
		}
		
		return dto;
	}
	
	public static ReceiptSetup copyToReceiptSetupViewBean(ReceiptSetupDTO dto)
	{
		ReceiptSetup setup = new ReceiptSetup();
		
		setup.setName(dto.getName());
		//TODO: do this properly:
		setup.setTransactionStyle(TransactionStyle.DEFAULT_STYLE);
		setup.setTopSection(copyToReceiptSectionViewBean(dto.getTopSection()));
		setup.setBottomSection(copyToReceiptSectionViewBean(dto.getBottomSection()));
		
		return setup;
	}
	
	public static ReceiptSection copyToReceiptSectionViewBean(ReceiptSectionDTO dto)
	{
		ReceiptSection section = new ReceiptSection();
		
		List<ReceiptLine> lines = new ArrayList<ReceiptLine>();
		section.setLines(lines);
		
		for(ReceiptLineDTO line: dto.getLines())
		{
			lines.add(copyToReceiptLineViewBean(line));
		}
		
		return section;
	}
	
	public static ReceiptLine copyToReceiptLineViewBean(ReceiptLineDTO dto)
	{
		ReceiptLine line = new ReceiptLine();

		switch(dto.getType())
		{
			case BLANK:
				line.setType(ReceiptLine.Type.BLANK);
				break;
			case DIVIDER:
				line.setType(ReceiptLine.Type.DIVIDER);
				break;
			case TEXT:
				line.setType(ReceiptLine.Type.TEXT);
				line.setFontName(dto.getFont().getName());
				line.setFontSize(dto.getFont().getSize());
				line.setFontType(dto.getFont().getStyle());
				line.setText(dto.getText());
				
				switch(dto.getAlignment())
				{
					case LEFT:
						line.setAlignment(ReceiptLine.Alignment.LEFT);
						break;
					case CENTRE:
						line.setAlignment(ReceiptLine.Alignment.CENTRE);
						break;
					case RIGHT:
						line.setAlignment(ReceiptLine.Alignment.RIGHT);
						break;
				}
				break;
		}
		
		return line;
	}

	public void setNextSetupId(int nextSetupId) {
		this.nextSetupId = nextSetupId;
	}

	public void setNextSectionId(int nextSectionId) {
		this.nextSectionId = nextSectionId;
	}

	public void setNextLineId(int nextLineId) {
		this.nextLineId = nextLineId;
	}
}
