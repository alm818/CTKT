package utility;

import java.sql.Date;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

public class StringUtility {
	private static final Pattern REMOVE_TAGS = Pattern.compile("<.+?>");
	private static final DataFormatter formatter = new DataFormatter();

	public static String removeAccent(String string) {
	    String str = Normalizer.normalize(string, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	    return str.replace('đ', 'd').replace('Đ', 'D');
	}
	
	public static String removeTags(String string) {
	    if (string == null || string.length() == 0) {
	        return string;
	    }
	    Matcher m = REMOVE_TAGS.matcher(string);
	    return m.replaceAll(" ").trim();
	}
	
	public static String getCellString(Cell cell){
		return formatter.formatCellValue(cell).trim().replaceAll(" +", " ");
	}
	
	public static String getCKString(String ck){
		if (ck.contains("("))
			ck = "-" + ck.substring(1, ck.length() - 1);
		return ck;
	}
	
	public static double getSum(String quantity){
		int plus = quantity.indexOf('+');
		double q = Double.parseDouble(quantity.substring(0, plus));
		double add = Double.parseDouble(quantity.substring(plus + 1));
		return q + add;
	}
	
	public static String hashDate(Date day){
		return hashDate(day.getTime());
	}
	
	public static String hashDate(Calendar cal){
		return hashDate(cal.getTimeInMillis());
	}
	
	private static String hashDate(long mili){
		int pos = (int) (mili % 676);
		char first = (char) ('a' + pos / 26);
		char second = (char) ('a' + pos % 26);
		return "_" + first + second;
	}
}
