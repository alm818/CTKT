package factory;

import java.util.Calendar;

import javax.swing.JFrame;

public class AttributesFactory {
	private final static String MS2016 = "1483117200000"; //ONE YEAR ONE MONTH
	private final static long RETURN_PERIOD = Long.valueOf("34186698000"); // 1 year
	private final static long BILL_PERIOD = Long.valueOf("7889238000"); // 3 months
	public final static String JOHNSON = "CÔNG TY TNHH JOHNSON & JOHNSON (VIỆT NAM)";
	public final static String DKSH = "Công Ty TNHH DKSH Việt Nam";
	public final static String ALL = "Tất cả";
	public final static String CODE = "R2017";
	public final static String NAME = "Tồn đầu 2017";
	public final static String PROMO_CODE = "R2017_PROMO";
	public final static String PROMO_NAME = "Tồn KM đầu 2017";
	public final static String YEAR = "năm";
	public final static String MONTH = "tháng";
	public final static String FROM_DAY = "từ ngày";
	public final static String TO_DAY = "đến ngày";
	public final static String ADMIN = "Admin";
	public final static long DEFAULT_TARGET_REVENUE = 5100000000L;
	public final static double DEFAULT_PERCENT_REVENUE = 0.1;
	public final static double INTEREST_RATE = 0.045;
	public final static int BASED_YEAR = 2017;
	public final static int CONCURRENT_THREAD = 8 * Runtime.getRuntime().availableProcessors();
	public final static Calendar CAL2016 = Calendar.getInstance();
	static {
		CAL2016.setTimeInMillis(Long.parseLong(MS2016));
	}
	private static Calendar from;
	private static Calendar to;
	private static Calendar today;
	private static JFrame frame;
	static{
		frame = null;
		today = Calendar.getInstance();
		today.setTimeInMillis(0);
		from = Calendar.getInstance();
		from.add(Calendar.MONTH, -1);
		from.set(Calendar.DAY_OF_MONTH, 1);
		to = Calendar.getInstance();
		to.set(Calendar.DAY_OF_MONTH, to.getActualMaximum(Calendar.DAY_OF_MONTH));
	}
	
	public static Calendar getToday(){
		return today;
	}
	
	public static void setFrame(JFrame frame){
		AttributesFactory.frame = frame;
	}
	
	public static JFrame getFrame(){
		return frame;
	}
	
	public static Calendar getLimReturnDay(){
		Calendar res = Calendar.getInstance();
		res.setTimeInMillis(today.getTimeInMillis() - RETURN_PERIOD);
		return res;
	}
	
	public static Calendar getBillDay(Calendar today){
		Calendar res = Calendar.getInstance();
		res.setTimeInMillis(today.getTimeInMillis() - BILL_PERIOD);
		return res;
	}
	
	public static void updateToday(Calendar day){
		if (day == null) return;
		if (day.after(today))
			today.setTimeInMillis(day.getTimeInMillis());
	}
	
	public static void setCalendar(Calendar from, Calendar to){
		AttributesFactory.from = from;
		AttributesFactory.to = to;
	}
	
	public static Calendar getFrom(){
		return from;
	}
	
	public static Calendar getTo(){
		return to;
	}
}
