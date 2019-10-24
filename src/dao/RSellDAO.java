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
import misc.Pair;
import misc.ThreadedStatement;
import transferObject.Bill;
import transferObject.RStatus;
import transferObject.ResSet;
import utility.CalendarUtility;

public class RSellDAO extends DAO{
	private ThreadedStatement insert, insertProduct;
	private static HashMap<String, Integer> idMap = new HashMap<String, Integer>();
	private static PreparedStatement getID, getDiary, getBills, getProfit, getHistory, setLast, getLast;
	static{
		String getIDString = "SELECT id FROM APP.RSell WHERE code=?";
		String getDiaryString = "SELECT code FROM APP.RSell JOIN APP.RSell_Product ON APP.RSell.id=rsell_id WHERE product_id=? AND day>=? AND day<=?";
		String getBillsString = "SELECT code FROM APP.RSell WHERE day>=? AND day<=?";
		String getProfitString = "SELECT q, sp, bp FROM APPDATE.RSell_Profit WHERE rsell_id=?";
		String getHistoryString = "SELECT sell_id, q FROM APPDATE.RSell_Sell WHERE rsell_id=?";
		String setLastString = "UPDATE APP.RSell_Product"
				+ " SET last_p=?, last_code=? WHERE rsell_id=? AND product_id=?";
		String getLastString = "SELECT last_p, last_code FROM APP.RSell_Product WHERE id=?";
		try {
			getID = conn.prepareStatement(getIDString);
			getDiary = conn.prepareStatement(getDiaryString);
			getBills = conn.prepareStatement(getBillsString);
			getProfit = conn.prepareStatement(getProfitString);
			getHistory = conn.prepareStatement(getHistoryString);
			setLast = conn.prepareStatement(setLastString);
			getLast = conn.prepareStatement(getLastString);
			psAll.add(getID);
			psAll.add(getDiary);
			psAll.add(getBills);
			psAll.add(getProfit);
			psAll.add(getHistory);
			psAll.add(setLast);
			psAll.add(getLast);
		} catch (SQLException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public RSellDAO() throws SQLException{
		super();
	}
	
	public void inserts(String code, Date day, String codeCustomer) throws SQLException{
		insert.addData(code, day, codeCustomer);
	}
	
	public void insertsProduct(String codeRSell, String nameProduct, double q, double pq, int cost) throws SQLException{
		insertProduct.addData(codeRSell, nameProduct, q, pq, cost);
	}

	@Override
	protected void initThreadedStatement() throws SQLException {
		String insertString = "INSERT INTO APP.RSell (code, day, customer_id)"
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
		
		String insertProductString = "INSERT INTO APP.RSell_Product (rsell_id, product_id, q, pq, cost)" 
				+ " VALUES (?, ?, ?, ?, ?)";
		FunctionalStatement insertProductFs = (PreparedStatement stm, Object[] objects) -> {
			String codeRSell = (String) objects[0];
			String nameProduct = (String) objects[1];
			double q = (double) objects[2];
			double pq = (double) objects[3];
			int cost = (int) objects[4];
			
			stm.setInt(1, RSellDAO.getID(codeRSell));
			stm.setInt(2, ProductDAO.getID(nameProduct));
			stm.setDouble(3, q);
			stm.setDouble(4, pq);
			stm.setInt(5, cost);
			stm.addBatch();
		};
		insertProduct = new ThreadedStatement(conn, insertProductString, insertProductFs);
		tsAll.add(insertProduct);
	}
	
	public synchronized static Integer getID(String codeRSell) throws SQLException {
		if (idMap.containsKey(codeRSell)) return idMap.get(codeRSell);
		getID.setString(1, codeRSell);
		ResultSet results = getID.executeQuery();
		Integer id = null;
		while (results.next())
			id = results.getInt("id");
		results.close();
		idMap.put(codeRSell, id);
		return id;
	}
	
	public synchronized static void setLast(String codeRSell, String nameProduct, int last_p, String last_code) throws SQLException{
		setLast.setInt(1, last_p);
		setLast.setString(2, last_code);
		setLast.setInt(3, RSellDAO.getID(codeRSell));
		setLast.setInt(4, ProductDAO.getID(nameProduct));
		setLast.executeUpdate();
	}
	
	public static Pair<Integer, String> getLast(int id) throws SQLException{
		getLast.setInt(1, id);
		ResultSet result = getLast.executeQuery();
		while (result.next()){
			return new Pair<Integer, String>(result.getInt("last_p"), result.getString("last_code"));
		}
		return null;
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
	
	public static ArrayList<Bill> getBills(Calendar from, Calendar to) throws SQLException{
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
	
	public static ResSet getProfit(int rsell_id) throws SQLException{
		getProfit.setInt(1, rsell_id);
		ResultSet result = getProfit.executeQuery();
		while (result.next()){
			ResSet resSet = new ResSet();
			resSet.put("q", result.getDouble("q"));
			resSet.put("sp", result.getInt("sp"));
			resSet.put("bp", result.getInt("bp"));
			return resSet;
		}
		return null;
	}
	
	public static ArrayList<RStatus> getHistory(int rsell_id) throws SQLException{
		getHistory.setInt(1, rsell_id);
		ResultSet results = getHistory.executeQuery();
		ArrayList<RStatus> history = new ArrayList<RStatus>();
		while (results.next()){
			int sell_id = results.getInt("sell_id");
			double q = results.getDouble("q");
			Bill bill = SellDAO.getBill(sell_id);
			history.add(new RStatus(bill, q));
		}
		results.close();
		history.sort(ComparatorFactory.getRStatusComparator());
		return history;
	}
}
