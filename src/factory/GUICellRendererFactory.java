package factory;

import java.awt.Color;
import java.awt.Component;
import java.util.Calendar;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import gui.GUICellStyle;
import utility.Utility;

public class GUICellRendererFactory {
	public static DefaultTableCellRenderer getStringRenderer(GUICellStyle cellStyle){ return new StringRenderer(cellStyle); }
	public static DefaultTableCellRenderer getCalendarRenderer(GUICellStyle cellStyle){ return new CalendarRenderer(cellStyle); }
	public static DefaultTableCellRenderer getPriceRenderer(GUICellStyle cellStyle){ return new PriceRenderer(cellStyle); }
	public static DefaultTableCellRenderer getPerRenderer(GUICellStyle cellStyle){ return new PerRenderer(cellStyle); }
	public static DefaultTableCellRenderer getQuantityRenderer(GUICellStyle cellStyle){ return new QuantityRenderer(cellStyle); }
	public static DefaultTableCellRenderer getSignedQuantityRenderer(GUICellStyle cellStyle){ return new SignedQuantityRenderer(cellStyle); }
	public static DefaultTableCellRenderer getSignedPriceRenderer(GUICellStyle cellStyle){ return new SignedPriceRenderer(cellStyle); }

	@SuppressWarnings("serial")
	private static abstract class GenericRenderer extends DefaultTableCellRenderer{
		protected GUICellStyle cellStyle;
		
		public GenericRenderer(GUICellStyle cellStyle){
			super();
			this.cellStyle = cellStyle;
		}
		
		public Component getTableCellRendererComponent(JTable table, String value, boolean isSelected, boolean hasFocus, int row, int column) {
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value == null) return super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
			JLabel renderedLabel = getRenderedLabel(table, value, isSelected, hasFocus, row, column);
			setRenderedLabel(renderedLabel);
			return renderedLabel;
		}
		
		protected void setRenderedLabel(JLabel renderedLabel){
			renderedLabel.setForeground(cellStyle.getFGColor());
			renderedLabel.setHorizontalAlignment(cellStyle.getAlignment());
			renderedLabel.setFont(cellStyle.getCombinedFont(renderedLabel.getFont()));
		}
		
		protected abstract JLabel getRenderedLabel(JTable table, Object value, boolean isSelected, boolean hasFocus,int row, int column);
	}
	
	@SuppressWarnings("serial")
	private static class StringRenderer extends GenericRenderer{
		public StringRenderer(GUICellStyle cellStyle) {
			super(cellStyle);
		}
		
		@Override
		protected JLabel getRenderedLabel(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			return (JLabel) super.getTableCellRendererComponent(table, (String) value, isSelected, hasFocus, row, column);
		}
	}
	
	@SuppressWarnings("serial")
	private static class CalendarRenderer extends GenericRenderer{
		public CalendarRenderer(GUICellStyle cellStyle) {
			super(cellStyle);
		}
		
		@Override
		protected JLabel getRenderedLabel(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Calendar calendar = (Calendar)value;
			return (JLabel) super.getTableCellRendererComponent(table, FormatFactory.formatCalendar(calendar), isSelected, hasFocus, row, column);
		}
	}
	
	@SuppressWarnings("serial")
	private static class PriceRenderer extends GenericRenderer{
		public PriceRenderer(GUICellStyle cellStyle){
			super(cellStyle);
		}
		
		@Override
		protected JLabel getRenderedLabel(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Long val = Utility.getWholeValue(value);
			return (JLabel) super.getTableCellRendererComponent(table, FormatFactory.formatPrice(val), isSelected, hasFocus, row, column);
		}
	}
	
	@SuppressWarnings("serial")
	private static class PerRenderer extends GenericRenderer{
		public PerRenderer(GUICellStyle cellStyle){
			super(cellStyle);
		}
		
		@Override
		protected JLabel getRenderedLabel(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Double val = Double.valueOf(value.toString());
			return (JLabel) super.getTableCellRendererComponent(table, FormatFactory.formatPercent(val), isSelected, hasFocus, row, column);
		}
	}
	
	@SuppressWarnings("serial")
	private static class QuantityRenderer extends GenericRenderer{
		public QuantityRenderer(GUICellStyle cellStyle){
			super(cellStyle);
		}
		
		@Override
		protected JLabel getRenderedLabel(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Double val = Double.valueOf(value.toString());
			return (JLabel) super.getTableCellRendererComponent(table, FormatFactory.formatQuantity(val), isSelected, hasFocus, row, column);
		}
	}
	
	@SuppressWarnings("serial")
	private static class SignedQuantityRenderer extends GenericRenderer{
		public SignedQuantityRenderer(GUICellStyle cellStyle){
			super(cellStyle);
		}
		
		@Override
		protected JLabel getRenderedLabel(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Double val = Double.valueOf(value.toString());
			JLabel renderedLabel = (JLabel) super.getTableCellRendererComponent(table, FormatFactory.formatSignedQuantity(val), isSelected, hasFocus, row, column);
			if (val != null){
				if (val == 0)
					cellStyle.setFGColor(Color.BLACK);
				else if (val < 0)
					cellStyle.setFGColor(Color.RED);
				else
					cellStyle.setFGColor(Color.BLUE);
			}
			return renderedLabel;
		}
	}
	
	@SuppressWarnings("serial")
	private static class SignedPriceRenderer extends GenericRenderer{
		public SignedPriceRenderer(GUICellStyle cellStyle){
			super(cellStyle);
		}
		
		@Override
		protected JLabel getRenderedLabel(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Long val = Utility.getWholeValue(value);
			JLabel renderedLabel = (JLabel) super.getTableCellRendererComponent(table, FormatFactory.formatSignedPrice(val), isSelected, hasFocus, row, column);
			if (val != null){
				if (val == 0)
					cellStyle.setFGColor(Color.BLACK);
				else if (val < 0)
					cellStyle.setFGColor(Color.RED);
				else
					cellStyle.setFGColor(Color.BLUE);
			}
			return renderedLabel;
		}
	}
}
