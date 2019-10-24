package utility;

import java.sql.Date;
import java.util.Calendar;
import java.util.Locale;

public class CalendarUtility {
	private static final Locale LOCALE = new Locale("vi", "VN");
	
	public static Locale getLocale(){ 
		return LOCALE; 
	}
	
	public static void startCal(Calendar cal){
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
	}
	
	public static void endCal(Calendar cal){
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
	}
	
	public static Calendar BuyCal(Calendar cal){
		cal.set(Calendar.HOUR_OF_DAY, 5);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal;
	}
	
	public static Calendar SellCal(Calendar cal){
		cal.set(Calendar.HOUR_OF_DAY, 10);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal;
	}
	
	public static Calendar RSellCal(Calendar cal){
		cal.set(Calendar.HOUR_OF_DAY, 15);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal;
	}
	
	public static Calendar RBuyCal(Calendar cal){
		cal.set(Calendar.HOUR_OF_DAY, 20);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal;
	}
	
	public static Calendar toCalendar(Date day){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(day.getTime());
		return cal;
	}
	
	public static Date toDate(Calendar cal){
		Date date = new Date(cal.getTimeInMillis());
		return date;
	}
	
	public static int getTotalDay(Calendar from, Calendar to){
		Calendar localFrom = Calendar.getInstance();
		localFrom.setTimeInMillis(from.getTimeInMillis());
		CalendarUtility.startCal(localFrom);
		Calendar localTo = Calendar.getInstance();
		localTo.setTimeInMillis(to.getTimeInMillis());
		CalendarUtility.endCal(localTo);
		int totalDay = 0, sunday = 0;
		for (Calendar i = localFrom; i.before(to); i.add(Calendar.DAY_OF_MONTH, 1)){
			totalDay ++;
			if (i.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
				sunday ++;
		}
		totalDay -= sunday;
		return totalDay;
	}
}
