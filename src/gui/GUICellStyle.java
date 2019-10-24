package gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.SwingConstants;

import factory.FontFactory;

public class GUICellStyle {
	private Integer style, size, alignment;
	private Color fgColor;
	
	public GUICellStyle(){
		alignment = SwingConstants.LEFT;
		fgColor = Color.BLACK;
		style = Font.PLAIN;
	}
	
	public GUICellStyle(int alignment){
		this.alignment = alignment;
		fgColor = Color.BLACK;
		style = Font.PLAIN;
	}
	
	public Font getCombinedFont(Font font){
		int newStyle = font.getStyle();
		if (style != null) newStyle = style;
		int newSize = font.getSize();
		if (size != null) newSize = size;
		return new Font(FontFactory.getFontName(), newStyle, newSize);
	}
	
	public void setStyle(int style){
		this.style = style;
	}
	
	public void setSize(int size){
		this.size = size;
	}
	
	public void setFGColor(Color color){
		fgColor = color;
	}
	
	public Font getFont(){
		return new Font(FontFactory.getFontName(), style, size);
	}
	
	public int getAlignment(){
		return alignment;
	}
	
	public Color getFGColor(){
		return fgColor;
	}
}
