package factory;

import java.awt.Font;

public class FontFactory {
	private final static String FONT_NAME = "Calibri";
	private static Font tableTitle, info, tableHeader;
	static{
		tableTitle = new Font(FONT_NAME, Font.BOLD, 20);
		info = new Font(FONT_NAME, Font.ITALIC, 15);
		tableHeader = new Font(FONT_NAME, Font.BOLD, 16);
	}

	public static Font getTableTitle() {
		return tableTitle;
	}

	public static Font getInfo() {
		return info;
	}

	public static Font getTableHeader() {
		return tableHeader;
	}

	public static String getFontName() {
		return FONT_NAME;
	}
}
