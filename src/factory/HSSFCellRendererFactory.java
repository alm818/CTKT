package factory;

import java.util.Calendar;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;

import utility.Utility;

public class HSSFCellRendererFactory {
	private HSSFCellStyleFactory factory;
	
	public HSSFCellRendererFactory(HSSFCellStyleFactory factory){
		this.factory = factory;
	}
	
	public abstract class CellRenderer{
		protected HSSFCellStyle cellStyle;
		
		public CellRenderer(HSSFCellStyle cellStyle){
			this.cellStyle = cellStyle;
		}
		
		public abstract void render(HSSFCell cell, Object value);
	}
	
	public CalendarRenderer getCalendarRenderer(){
		return new CalendarRenderer();
	}
	
	public StringRenderer getStringRenderer(){
		return new StringRenderer();
	}
	
	public PriceRenderer getPriceRenderer(){
		return new PriceRenderer();
	}
	
	public QuantityRenderer getQuantityRenderer(){
		return new QuantityRenderer();
	}
	
	public PercentRenderer getPercentRenderer(){
		return new PercentRenderer();
	}
	
	public SignedPriceRenderer getSignedPriceRenderer(){
		return new SignedPriceRenderer();
	}
	
	public CalendarRenderer getCalendarRenderer(HSSFCellStyle cellStyle){
		return new CalendarRenderer(cellStyle);
	}
	
	public StringRenderer getStringRenderer(HSSFCellStyle cellStyle){
		return new StringRenderer(cellStyle);
	}
	
	public PriceRenderer getPriceRenderer(HSSFCellStyle cellStyle){
		return new PriceRenderer(cellStyle);
	}
	
	public QuantityRenderer getQuantityRenderer(HSSFCellStyle cellStyle){
		return new QuantityRenderer(cellStyle);
	}
	
	public PercentRenderer getPercentRenderer(HSSFCellStyle cellStyle){
		return new PercentRenderer(cellStyle);
	}
	
	public SignedPriceRenderer getSignedPriceRenderer(HSSFCellStyle cellStyle){
		return new SignedPriceRenderer(cellStyle);
	}
	
	private class CalendarRenderer extends CellRenderer{
		public CalendarRenderer() {
			super(factory.getBorderedCenterStyle());
		}
		
		public CalendarRenderer(HSSFCellStyle cellStyle) {
			super(cellStyle);
		}

		@Override
		public void render(HSSFCell cell, Object value) {
			Calendar cal = (Calendar) value;
			cell.setCellStyle(cellStyle);
			cell.setCellValue(FormatFactory.formatCalendar(cal));
		}
	}
	
	private class StringRenderer extends CellRenderer{
		public StringRenderer() {
			super(factory.getLeftStyle());
		}
		
		public StringRenderer(HSSFCellStyle cellStyle) {
			super(cellStyle);
		}

		@Override
		public void render(HSSFCell cell, Object value) {
			String string = (String) value;
			cell.setCellStyle(cellStyle);
			cell.setCellValue(string);
		}
	}
	
	public class PriceRenderer extends CellRenderer{
		public PriceRenderer() {
			super(factory.getPriceStyle());
		}
		
		public PriceRenderer(HSSFCellStyle cellStyle) {
			super(cellStyle);
		}
		
		@Override
		public void render(HSSFCell cell, Object value) {
			Long price = Utility.getWholeValue(value);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(FormatFactory.formatPrice(price));
		}
	}
	
	public class QuantityRenderer extends CellRenderer{
		public QuantityRenderer() {
			super(factory.getRightStyle());
		}
		
		public QuantityRenderer(HSSFCellStyle cellStyle) {
			super(cellStyle);
		}
		
		@Override
		public void render(HSSFCell cell, Object value) {
			Double val = Double.valueOf(value.toString());
			cell.setCellStyle(cellStyle);
			cell.setCellValue(FormatFactory.formatQuantity(val));
		}
	}
	
	private class PercentRenderer extends CellRenderer{
		public PercentRenderer() {
			super(factory.getRightStyle());
		}
		
		public PercentRenderer(HSSFCellStyle cellStyle) {
			super(cellStyle);
		}
		
		@Override
		public void render(HSSFCell cell, Object value) {
			Double val = Double.valueOf(value.toString());
			cell.setCellStyle(cellStyle);
			cell.setCellValue(FormatFactory.formatPercent(val));
		}
	}
	
	private class SignedPriceRenderer extends CellRenderer{
		public SignedPriceRenderer() {
			super(null);
		}
		
		public SignedPriceRenderer(HSSFCellStyle cellStyle) {
			super(cellStyle);
		}
		
		@Override
		public void render(HSSFCell cell, Object value) {
			Long price = Utility.getWholeValue(value);
			if (price == null) price = 0L;
			if (price == 0)
				cellStyle = factory.getZeroStyle();
			else if (price < 0)
				cellStyle = factory.getNegativeStyle();
			else
				cellStyle = factory.getPositiveStyle();
			cell.setCellStyle(cellStyle);
			cell.setCellValue(FormatFactory.formatSignedPrice(price));
		}
	}
}
