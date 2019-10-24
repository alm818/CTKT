package factory;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FormatFactory {
	private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");
	private static final SimpleDateFormat SDF2 = new SimpleDateFormat("dd-MM-yyyy");
	private static final DecimalFormat PRICE = new DecimalFormat("#,###");
	private static final DecimalFormat PERCENT = new DecimalFormat("0.000");
	private static final DecimalFormat QUANTITY = new DecimalFormat("#,##0.0");
	private static final DecimalFormat INFO = new DecimalFormat("#,###.#");
	
	public static Calendar parseDate(String input) throws ParseException{
		Calendar cal = Calendar.getInstance();
		cal.setTime(SDF.parse(input));
		return cal;
	}
	
	public static String formatCalendar(Calendar cal){
		if (cal == null) return "";
		return SDF.format(cal.getTime());
	}
	
	public static String formatCalendarFile(Calendar cal){
		if (cal == null) return "";
		return SDF2.format(cal.getTime());
	}
	
	public static String formatPrice(Long price){
		if (price == null) return "";
		return PRICE.format(price);
	}
	
	public static String formatSignedPrice(Long price){
		if (price == null) return "";
		String num = PRICE.format(Math.abs(price));
		if (price == 0) return "0";
		else if (price < 0) return "-" + num;
		else return "+" + num;
	}
	
	public static String formatPercent(Double percent){
		if (percent == null) return "";
		return PERCENT.format(percent * 100) + "%";
	}
	
	public static String formatQuantity(Double quantity){
		if (quantity == null) return "";
		return QUANTITY.format(quantity);
	}
	
	public static String formatSignedQuantity(Double quantity){
		if (quantity == null) return "";
		String num = QUANTITY.format(Math.abs(quantity));
		if (quantity == 0) return "0";
		else if (quantity < 0) return "-" + num;
		else return "+" + num;
	}
	
	public static String formatInfo(double val){
		return INFO.format(val);
	}
}
