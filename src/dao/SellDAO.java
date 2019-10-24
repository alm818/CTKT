package dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import factory.AttributesFactory;
import factory.ComparatorFactory;
import fao.FileDirection;
import misc.FunctionalStatement;
import misc.ThreadedStatement;
import transferObject.Bill;
import transferObject.ResSet;
import transferObject.Status;
import utility.CalendarUtility;

public class SellDAO extends DAO{
	private static PreparedStatement getID, getDiary, getPromo, getBills, getProfit, getBill, getHistory;
	private static HashMap<String, Integer> idMap = new HashMap<String, Integer>();
	static{
		String getIDString = "SELECT id FROM APP.Sell WHERE code=?";
		String getDiaryString = "SELECT code FROM APP.Sell JOIN APP.Sell_Product ON APP.Sell.id=sell_id WHERE product_id=? AND day>=? AND day<=?";
		String getPromoString = "SELECT code FROM APP.Sell JOIN APP.Sell_Product ON APP.Sell.id=sell_id WHERE product_id=? AND day>=? AND day<=? AND q=0";
		String getBillsString = "SELECT code FROM APP.Sell WHERE day>=? AND day<=?";
		String getProfitString = "SELECT sp, bp, rq FROM APPDATE.Sell_Profit WHERE sell_id=?";
		String getBillString = "SELECT code FROM APP.Sell JOIN APP.Sell_Product ON APP.Sell.id=sell_id WHERE APP.Sell_Product.id=?";
		String getHistoryString = "SELECT code, q FROM APPDATE.Sell_History WHERE sell_id=?";
		try {
			getID = conn.prepareStatement(getIDString);
			getDiary = conn.prepareStatement(getDiaryString);
			getPromo = conn.prepareStatement(getPromoString);
			getBills = conn.prepareStatement(getBillsString);
			getProfit = conn.prepareStatement(getProfitString);
			getBill = conn.prepareStatement(getBillString);
			getHistory = conn.prepareStatement(getHistoryString);
			psAll.add(getID);
			psAll.add(getDiary);
			psAll.add(getPromo);
			psAll.add(getBills);
			psAll.add(getProfit);
			psAll.add(getBill);
			psAll.add(getHistory);
		} catch (SQLException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private ThreadedStatement insert, insertProduct;
	
	public SellDAO() throws SQLException{
		super();
	}
	
	public void inserts(String code, Date day, String codeCustomer) throws SQLException{
		insert.addData(code, day, codeCustomer);
	}
	
	public void insertsProduct(String codeSell, String nameProduct, double q, double pq, int cost) throws SQLException{
		insertProduct.addData(codeSell, nameProduct, q, pq, cost);
	}

	@Override
	protected void initThreadedStatement() throws SQLException {
		String insertString = "INSERT INTO APP.Sell (code, day, customer_id)"
				+ " VALUES (?, ?, ?)";
		FunctionalStatement insertFs = (PreparedStatement stm, Object[] objects) -> {
			String code = (String) objects[0];
			Date day = (Date) objects[1];
			String codeCustomer = (String) objects[2];
			stm.setString(1, code);
			stm.setDate(2, day);
			stm.setInt(3, CustomerDAO.getID(codeCustomer));
			stm.addBatch();
		};
		insert = new ThreadedStatement(conn, insertString, insertFs);
		tsAll.add(insert);
		
		String insertProductString = "INSERT INTO APP.Sell_Product (sell_id, product_id, q, pq, cost)" 
				+ " VALUES (?, ?, ?, ?, ?)";
		FunctionalStatement insertProductFs = (PreparedStatement stm, Object[] objects) -> {
			String codeSell = (String) objects[0];
			String nameProduct = (String) objects[1];
			double q = (double) objects[2];
			double pq = (double) objects[3];
			int cost = (int) objects[4];
			stm.setInt(1, SellDAO.getID(codeSell));
			stm.setInt(2, ProductDAO.getID(nameProduct));
			stm.setDouble(3, q);
			stm.setDouble(4, pq);
			stm.setInt(5, cost);
			stm.addBatch();
		};
		insertProduct = new ThreadedStatement(conn, insertProductString, insertProductFs);
		tsAll.add(insertProduct);
	}
	
	public synchronized static Integer getID(String codeSell) throws SQLException {
		if (idMap.containsKey(codeSell)) return idMap.get(codeSell);
		getID.setString(1, codeSell);
		ResultSet results = getID.executeQuery();
		Integer id = null;
		while (results.next())
			id = results.getInt("id");
		results.close();
		idMap.put(codeSell, id);
		return id;
	}
	
	public static HashSet<String> getCodeBills() throws SQLException{
		Calendar today = FileDirection.getData().getToday();
		if (today == null) return new HashSet<String>();
		getBills.setDate(1, CalendarUtility.toDate(AttributesFactory.getBillDay(today)));
		getBills.setDate(2, CalendarUtility.toDate(today));
		ResultSet results = getBills.executeQuery();
		HashSet<String> codeBills = new HashSet<String>();
		while (results.next()){
			String code = results.getString("code");
			codeBills.add(code);
		}
		return codeBills;
	}
	
	public static ArrayList<Bill> getDiary(String nameProduct, Calendar from, Calendar to) throws SQLException{
		getDiary.setInt(1, ProductDAO.getID(nameProduct));
		getDiary.setDate(2, CalendarUtility.toDate(from));
		getDiary.setDate(3, CalendarUtility.toDate(to));
		ResultSet results = getDiary.executeQuery();
		ArrayList<Bill> billList = new ArrayList<Bill>();
		while (results.next()){
			String code = results.getString("code");
			billList.add(BillDAO.getBill(code));
		}
		results.close();
		return billList;
	}
	
	public static ArrayList<Bill> getPromo(String nameProduct, Calendar from, Calendar to) throws SQLException{
		getPromo.setInt(1, ProductDAO.getID(nameProduct));
		getPromo.setDate(2, CalendarUtility.toDate(from));
		getPromo.setDate(3, CalendarUtility.toDate(to));
		ResultSet results = getPromo.executeQuery();
		ArrayList<Bill> billList = new ArrayList<Bill>();
		while (results.next()){
			String code = results.getString("code");
			billList.add(BillDAO.getBill(code));
		}
		results.close();
		return billList;
	}
	
	public synchronized static ArrayList<Bill> getBills(Calendar from, Calendar to) throws SQLException{
		getBills.setDate(1, CalendarUtility.toDate(from));
		getBills.setDate(2, CalendarUtility.toDate(to));
		ResultSet results = getBills.executeQuery();
		ArrayList<Bill> billList = new ArrayList<Bill>();
		while (results.next()){
			String code = results.getString("code");
			billList.add(BillDAO.getBill(code));
		}
		results.close();
		return billList;
	}
	
	public static ResSet getProfit(int sell_id) throws SQLException{
		getProfit.setInt(1, sell_id);
		ResultSet result = getProfit.executeQuery();
		while (result.next()){
			ResSet resSet = new ResSet();
			resSet.put("rq", result.getDouble("rq"));
			resSet.put("sp", result.getInt("sp"));
			resSet.put("bp", result.getInt("bp"));
			return resSet;
		}
		return null;
	}
	
	public static Bill getBill(int sp_id) throws SQLException{
		getBill.setInt(1, sp_id);
		ResultSet result = getBill.executeQuery();
		while (result.next()){
			String code = result.getString("code");
			return BillDAO.getBill(code);
		}
		return null;
	}
	
	public static ArrayList<Status> getHistory(int sp_id) throws SQLException{
		getHistory.setInt(1, sp_id);
		ResultSet results = getHistory.executeQuery();
		ArrayList<Status> statuses = new ArrayList<Status>();
		while (results.next()){
			String code = results.getString("code");
			double q = results.getDouble("q");
			Bill bill = BillDAO.getBill(code);
			statuses.add(new Status(bill, q, 0));
		}
		results.close();
		statuses.sort(ComparatorFactory.getStatusComparator());
		return statuses;
	}
	
	public static ArrayList<String> getCodeHistory(int sp_id) throws SQLException{
		getHistory.setInt(1, sp_id);
		ResultSet results = getHistory.executeQuery();
		ArrayList<String> codeList = new ArrayList<String>();
		while (results.next()){
			String code = results.getString("code");
			codeList.add(code);
		}
		results.close();
		return codeList;
	}
}
