package utility;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;

import factory.AttributesFactory;
import misc.BillType;

public class Utility {
	private final static String[] BUY = {"NK"};
	private final static String[] SELL = {"BH", "PT", "NTTK", "N"};
	private final static String[] RSELL = {"BTL"};
	private final static String[] RBUY = {"MTL", "XK"};
	
	public static double generalizedJaccard(String left, String right){
		int[] l = new int[10];
		int[] r = new int[10];
		for (int i = 0; i < left.length(); i ++){
			int x = left.charAt(i) - '0';
			l[x] ++;
		}
		for (int i = 0; i < right.length(); i ++){
			int x = right.charAt(i) - '0';
			r[x] ++;
		}
		int up = 1, down = 1;
		for (int i = 0; i < 10; i ++){
			up += Math.min(l[i], r[i]);
			down += Math.max(l[i], r[i]);
		}
		return up / (double) down;
	}
	
	public static void setLim(PreparedStatement stm, int column, Double lim) throws SQLException{
		if (lim == null)
			stm.setNull(column, java.sql.Types.DOUBLE);
		else
			stm.setDouble(column, lim);
	}
	
	public static void runThreads(ArrayList<? extends Thread> threads) throws InterruptedException{
		for (Thread thread : threads){
			thread.start();
			thread.join();
		}
		for (Thread thread : threads)
			thread.join();
	}
	
	public static void runThreads(Thread...threads) throws InterruptedException{
		ArrayList<Thread> array = new ArrayList<Thread>(Arrays.asList(threads));
		runThreads(array);
	}
	
	public static BillType getBillType(String codeBill){
		for (String id : BUY)
			if (codeBill.contains(id))
				return BillType.BUY;
		for (String id : SELL)
			if (codeBill.contains(id))
				return BillType.SELL;
		for (String id : RSELL)
			if (codeBill.contains(id))
				return BillType.RSELL;
		for (String id : RBUY)
			if (codeBill.contains(id))
				return BillType.RBUY;
		if (codeBill.equals(AttributesFactory.CODE))
			return BillType.INITNP;
		if (codeBill.equals(AttributesFactory.PROMO_CODE))
			return BillType.INITP;
		return BillType.NULL;
	}
	
	public static double getNumericValue(Object val){
		if (val == null) return 0.0;
		if (val instanceof Long){
			Long res = (Long) val;
			return res.doubleValue();
		}
		if (val instanceof Integer){
			Integer res = (Integer) val;
			return res.doubleValue();
		}
		if (val instanceof Double) return (Double) val;
		return 0.0;
	}
	
	public static Long getWholeValue(Object val){
		if (val == null) return null;
		if (val instanceof Integer){
			Integer i = (Integer) val;
			return i.longValue();
		}
		if (val instanceof Long){
			Long l = (Long) val;
			return l;
		}
		return null;
	}
	
	public static Double reverseSign(Double val){
		if (val == null) return null;
		else return -val;
	}
	
	public static <T> Vector<T> getVector(T[] array){
		Vector<T> vector = new Vector<T>();
		for (int i = 0; i < array.length; i ++)
			vector.add(array[i]);
		return vector;
	}
	
	public static <T> boolean isAdding(HashSet<T> container, T check){
		if (!container.contains(check)){
			container.add(check);
			return true;
		}
		return false;
	}
}
