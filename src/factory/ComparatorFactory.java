package factory;

import java.util.Calendar;
import java.util.Comparator;

import transferObject.Bill;
import transferObject.RStatus;
import transferObject.Result;
import transferObject.Staff;
import transferObject.Status;
import utility.StringUtility;

public class ComparatorFactory {
	public static BillComparator getBillComparator(){
		return new BillComparator();
	}
	
	public static RStatusComparator getRStatusComparator(){
		return new RStatusComparator();
	}
	
	public static StatusComparator getStatusComparator(){
		return new StatusComparator();
	}
	
	public static StringComparator getStringComparator(){
		return new StringComparator();
	}
	
	public static RowComparator getRowComparator(){
		return new RowComparator();
	}
	
	public static CommentComparator getCommentComparator(){
		return new CommentComparator();
	}
	
	public static StaffComparator getStaffComparator(){
		return new StaffComparator();
	}
	
	public static NameComparator getNameComparator(){
		return new NameComparator();
	}
	
	public static FilterComparator getFilterComparator(){
		return new FilterComparator();
	}
	
	public static ResultComparator getResultComparator(){
		return new ResultComparator();
	}
	
	public static ExtendedFilterComparator getExtendedFilterComparator(){
		return new ExtendedFilterComparator();
	}
	
	public static ScoreComparator getScoreComparator(){
		return new ScoreComparator();
	}
	
	private static class ScoreComparator implements Comparator<Result>{
		@Override
		public int compare(Result o1, Result o2) {
			return Double.compare(o2.getScore(), o1.getScore());
		}
	}
	
	private static class ExtendedFilterComparator implements Comparator<Result>{
		@Override
		public int compare(Result o1, Result o2) {
			if (o1.getJaccard() == o2.getJaccard()){
				if (o1.getLCS() == o2.getLCS()){
					if (o1.getLeven() == o2.getLeven())
						Double.compare(o2.getJaro(), o1.getJaro());
					return Integer.compare(o1.getLeven(), o2.getLeven());
				}
				return Integer.compare(o2.getLCS(), o1.getLCS());
			}
			return Double.compare(o2.getJaccard(), o1.getJaccard());
		}
	}
	
	private static class ResultComparator implements Comparator<Result>{
		@Override
		public int compare(Result o1, Result o2) {
			return Integer.compare(o1.getLeven(), o2.getLeven());
		}
	}
	
	private static class FilterComparator implements Comparator<Result>{
		@Override
		public int compare(Result o1, Result o2) {
			if (o1.getJaccard() == o2.getJaccard())
				Integer.compare(o1.getLeven(), o2.getLeven());
			return Double.compare(o2.getJaccard(), o1.getJaccard());
		}
	}
	
	private static class NameComparator implements Comparator<String>{
		@Override
		public int compare(String o1, String o2) {
			return StringUtility.removeAccent(o1).toLowerCase().compareTo(StringUtility.removeAccent(o2).toLowerCase());
		}
	}
	
	private static class StringComparator implements Comparator<String>{
		@Override
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	}
	
	private static class StatusComparator implements Comparator<Status>{
		@Override
		public int compare(Status s1, Status s2) {
			BillComparator billComparator = new BillComparator();
			return billComparator.compare(s1.getBill(), s2.getBill());
		}
	}
	
	private static class RStatusComparator implements Comparator<RStatus>{
		@Override
		public int compare(RStatus o1, RStatus o2) {
			BillComparator billComparator = new BillComparator();
			return billComparator.compare(o1.getBill(), o2.getBill());
		}
		
	}
	
	private static class BillComparator implements Comparator<Bill>{
		@Override
		public int compare(Bill b1, Bill b2) {
			Calendar day1 = b1.getDay();
			Calendar day2 = b2.getDay();
			int res = day1.compareTo(day2);
			if (res == 0)
				return b1.getCodeBill().compareTo(b2.getCodeBill());
			return res;
		}
	}
	
	private static class RowComparator implements Comparator<Object[]>{
		@Override
		public int compare(Object[] o1, Object[] o2) {
			double percent1 = (double) o1[9];
			double percent2 = (double) o2[9];
			return Double.compare(percent1, percent2);
		}
	}
	
	private static class CommentComparator implements Comparator<Object[]>{
		@Override
		public int compare(Object[] o1, Object[] o2) {
			String comment1 = (String) o1[10];
			String comment2 = (String) o2[10];
			return comment1.compareTo(comment2);
		}
	}
	
	private static class StaffComparator implements Comparator<Staff>{
		@Override
		public int compare(Staff o1, Staff o2) {
			if (o1.getPosition().equals(o2.getPosition()))
				return o1.getName().compareTo(o2.getName());
			else return o1.getPosition().compareTo(o2.getPosition());
		}
	}
}
