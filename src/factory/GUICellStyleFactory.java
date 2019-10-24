package factory;

import java.awt.Color;
import java.awt.Font;

import javax.swing.SwingConstants;

import gui.GUICellStyle;

public class GUICellStyleFactory {
	private static GUICellStyle centerBold16, centerBold12, center11, left11, rightItalic11, right11, rightBoldRed11, rightBold11, rightBoldBlue11,
		centerBold14, center12, centerBold11, leftBold12, leftBold11, centerItalic11;
	static{
		centerBold16 = new GUICellStyle(SwingConstants.CENTER);
		centerBold16.setStyle(Font.BOLD);
		centerBold16.setSize(16);
		centerBold12 = new GUICellStyle(SwingConstants.CENTER);
		centerBold12.setStyle(Font.BOLD);
		centerBold12.setSize(12);
		center11 = new GUICellStyle(SwingConstants.CENTER);
		center11.setSize(11);
		left11 = new GUICellStyle(SwingConstants.LEFT);
		left11.setSize(11);
		rightItalic11 = new GUICellStyle(SwingConstants.RIGHT);
		rightItalic11.setStyle(Font.ITALIC);
		rightItalic11.setSize(11);
		right11 = new GUICellStyle(SwingConstants.LEFT);
		right11.setSize(11);
		rightBoldRed11 = new GUICellStyle(SwingConstants.RIGHT);
		rightBoldRed11.setStyle(Font.BOLD);
		rightBoldRed11.setFGColor(Color.RED);
		rightBoldRed11.setSize(11);
		rightBold11 = new GUICellStyle(SwingConstants.RIGHT);
		rightBold11.setStyle(Font.BOLD);
		rightBold11.setSize(11);
		rightBoldBlue11 = new GUICellStyle(SwingConstants.RIGHT);
		rightBoldBlue11.setStyle(Font.BOLD);
		rightBoldBlue11.setFGColor(Color.BLUE);
		rightBoldBlue11.setSize(11);
		centerBold14 = new GUICellStyle(SwingConstants.CENTER);
		centerBold14.setStyle(Font.BOLD);
		centerBold14.setSize(14);
		center12 = new GUICellStyle(SwingConstants.CENTER);
		center12.setSize(12);
		centerBold11 = new GUICellStyle(SwingConstants.CENTER);
		centerBold11.setStyle(Font.BOLD);
		centerBold11.setSize(11);
		leftBold12 = new GUICellStyle(SwingConstants.LEFT);
		leftBold12.setStyle(Font.BOLD);
		leftBold12.setSize(12);
		leftBold11 = new GUICellStyle(SwingConstants.LEFT);
		leftBold11.setStyle(Font.BOLD);
		leftBold11.setSize(11);
		centerItalic11 = new GUICellStyle(SwingConstants.CENTER);
		centerItalic11.setStyle(Font.ITALIC);
		centerItalic11.setSize(11);
	}
	public static GUICellStyle getCenterItalic11(){
		return centerItalic11;
	}
	
	public static GUICellStyle getLeftBold11(){
		return leftBold11;
	}
	
	public static GUICellStyle getLeftBold12(){
		return leftBold12;
	}
	
	public static GUICellStyle getCenterBold11(){
		return centerBold11;
	}
	
	public static GUICellStyle getCenter12(){
		return center12;
	}
	
	public static GUICellStyle getCenterBold14(){
		return centerBold14;
	}
	
	public static GUICellStyle getRightBoldBlue11(){
		return rightBoldBlue11;
	}
	
	public static GUICellStyle getRightBold11(){
		return rightBold11;
	}
	
	public static GUICellStyle getRightBoldRed11(){
		return rightBoldRed11;
	}
	
	public static GUICellStyle getRight11(){
		return right11;
	}
	
	public static GUICellStyle getRightItalic11(){
		return rightItalic11;
	}
	
	public static GUICellStyle getLeft11(){
		return left11;
	}
	
	public static GUICellStyle getCenter11(){
		return center11;
	}
	
	public static GUICellStyle getCenterBold12(){
		return centerBold12;
	}
	
	public static GUICellStyle getCenterBold16(){
		return centerBold16;
	}
	
	public static GUICellStyle getRight(){
		return new GUICellStyle(SwingConstants.RIGHT);
	}
	
	public static GUICellStyle getRightBold(){
		GUICellStyle rightBold = new GUICellStyle(SwingConstants.RIGHT);
		rightBold.setStyle(Font.BOLD);
		return rightBold;
	}
	
	public static GUICellStyle getRightBold16(){
		GUICellStyle rightBold16 = new GUICellStyle(SwingConstants.RIGHT);
		rightBold16.setStyle(Font.BOLD);
		rightBold16.setSize(16);
		return rightBold16;
	}
	
	public static GUICellStyle getRightBoldItalic(){
		GUICellStyle rightBoldItalic = new GUICellStyle(SwingConstants.RIGHT);
		rightBoldItalic.setStyle(Font.BOLD + Font.ITALIC);
		return rightBoldItalic;
	}
	
	public static GUICellStyle getRightItalic(){
		GUICellStyle rightItalic = new GUICellStyle(SwingConstants.RIGHT);
		rightItalic.setStyle(Font.ITALIC);
		return rightItalic;
	}
	
	public static GUICellStyle getCenter(){
		return new GUICellStyle(SwingConstants.CENTER);
	}
}
