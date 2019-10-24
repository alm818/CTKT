package factory;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.SwingConstants;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import gui.GUICellStyle;

public class HSSFCellStyleFactory {
	private static final HashMap<Color, Short> colorMap;
	private static final HashMap<Integer, HorizontalAlignment> alignMap;
	static{
		colorMap = new HashMap<Color, Short>();
		colorMap.put(Color.BLACK, HSSFColor.BLACK.index);
		colorMap.put(Color.RED, HSSFColor.RED.index);
		colorMap.put(Color.BLUE, HSSFColor.BLUE.index);
		alignMap = new HashMap<Integer, HorizontalAlignment>();
		alignMap.put(SwingConstants.LEFT, HorizontalAlignment.LEFT);
		alignMap.put(SwingConstants.CENTER, HorizontalAlignment.CENTER);
		alignMap.put(SwingConstants.RIGHT, HorizontalAlignment.RIGHT);
	}
	private HSSFWorkbook wb;
	private HSSFCellStyle headerStyle, borderedCenterStyle, leftStyle, priceStyle, 
		rightStyle, negativeStyle, zeroStyle, positiveStyle, titleStyle,
		companyStyle, staffStyle, wageHeaderStyle, sumWageCenterStyle,
		sumWageStyle, realWageStyle, closingStyle, dayStyle, centerStyle;
	
	public HSSFCellStyleFactory(HSSFWorkbook wb){
		this.wb = wb;
		headerStyle = makeBorderedCellStyle(GUICellStyleFactory.getCenterBold12());
		headerStyle.setWrapText(true);
		borderedCenterStyle = makeBorderedCellStyle(GUICellStyleFactory.getCenter11());
		leftStyle = makeBorderedCellStyle(GUICellStyleFactory.getLeft11());
		priceStyle = makeBorderedCellStyle(GUICellStyleFactory.getRightItalic11());
		rightStyle = makeBorderedCellStyle(GUICellStyleFactory.getRight11());
		negativeStyle = makeBorderedCellStyle(GUICellStyleFactory.getRightBoldRed11());
		zeroStyle = makeBorderedCellStyle(GUICellStyleFactory.getRightBold11());
		positiveStyle = makeBorderedCellStyle(GUICellStyleFactory.getRightBoldBlue11());
		titleStyle = makeCellStyle(GUICellStyleFactory.getCenterBold16());
		companyStyle = makeCellStyle(GUICellStyleFactory.getCenterBold14());
		staffStyle = makeCellStyle(GUICellStyleFactory.getCenter12());
		dayStyle = makeCellStyle(GUICellStyleFactory.getLeft11());
		wageHeaderStyle = makeCellStyle(GUICellStyleFactory.getCenterBold11());
		centerStyle = makeCellStyle(GUICellStyleFactory.getCenter11());
		sumWageStyle = makeCellStyle(GUICellStyleFactory.getLeftBold12());
		sumWageCenterStyle = makeCellStyle(GUICellStyleFactory.getCenterBold12());
		realWageStyle = makeCellStyle(GUICellStyleFactory.getLeftBold11());
		closingStyle = makeCellStyle(GUICellStyleFactory.getCenterItalic11());
	}
	public HSSFCellStyle getSumWageCenterStyle(){
		return sumWageCenterStyle;
	}
	
	public HSSFCellStyle getCenterStyle(){
		return centerStyle;
	}
	
	public HSSFCellStyle getDayStyle(){
		return dayStyle;
	}
	
	public HSSFCellStyle getClosingStyle(){
		return closingStyle;
	}
	
	public HSSFCellStyle getRealWageStyle(){
		return realWageStyle;
	}
	
	public HSSFCellStyle getSumWageStyle(){
		return sumWageStyle;
	}
	
	public HSSFCellStyle getWageHeaderStyle(){
		return wageHeaderStyle;
	}
	
	public HSSFCellStyle getStaffStyle(){
		return staffStyle;
	}
	
	public HSSFCellStyle getCompanyStyle(){
		return companyStyle;
	}
	
	public HSSFCellStyle getTitleStyle(){
		return titleStyle;
	}
	
	public HSSFCellStyle getPositiveStyle(){
		return positiveStyle;
	}
	
	public HSSFCellStyle getZeroStyle(){
		return zeroStyle;
	}
	
	public HSSFCellStyle getNegativeStyle(){
		return negativeStyle;
	}
	
	public HSSFCellStyle getRightStyle(){
		return rightStyle;
	}
	
	public HSSFCellStyle getPriceStyle(){
		return priceStyle;
	}
	
	public HSSFCellStyle getLeftStyle(){
		return leftStyle;
	}
	
	public HSSFCellStyle getBorderedCenterStyle(){
		return borderedCenterStyle;
	}
	
	public HSSFCellStyle getHeaderStyle(){
		return headerStyle;
	}
	
	private HSSFCellStyle makeBorderedCellStyle(GUICellStyle GUIcs){
		HSSFFont font = wb.createFont();
		Font GUIFont = GUIcs.getFont();
		font.setBold(GUIFont.isBold());
		font.setItalic(GUIFont.isItalic());
		font.setFontName(GUIFont.getName());
		font.setColor(colorMap.get(GUIcs.getFGColor()));
		font.setFontHeightInPoints((short) GUIFont.getSize());
		HSSFCellStyle cs = wb.createCellStyle();
		cs.setFont(font);
		cs.setAlignment(alignMap.get(GUIcs.getAlignment()));
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBottomBorderColor(HSSFColor.BLACK.index);
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setLeftBorderColor(HSSFColor.BLACK.index);
        cs.setBorderRight(BorderStyle.THIN);
        cs.setRightBorderColor(HSSFColor.BLACK.index);
        cs.setBorderTop(BorderStyle.THIN);
        cs.setTopBorderColor(HSSFColor.BLACK.index);
		return cs;
	}
	
	private HSSFCellStyle makeCellStyle(GUICellStyle GUIcs){
		HSSFFont font = wb.createFont();
		Font GUIFont = GUIcs.getFont();
		font.setBold(GUIFont.isBold());
		font.setItalic(GUIFont.isItalic());
		font.setFontName(GUIFont.getName());
		font.setColor(colorMap.get(GUIcs.getFGColor()));
		font.setFontHeightInPoints((short) GUIFont.getSize());
		HSSFCellStyle cs = wb.createCellStyle();
		cs.setFont(font);
		cs.setAlignment(alignMap.get(GUIcs.getAlignment()));
		return cs;
	}
}
