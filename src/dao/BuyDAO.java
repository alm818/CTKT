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
import fao.FileDirection;
import misc.FunctionalStatement;
import misc.ThreadedStatement;
import transferObject.Bill;
import utility.CalendarUtility;

public class BuyDAO extends DAO{
	private ThreadedStatement insert, insertProduct;
	private static PreparedStatement getDiary, getID, getBills;
	private static HashMap<String, Integer> idMap = new HashMap<String, Integer>();
	static{
		String getDiaryString = "SELECT code FROM APP.Buy JOIN APP.Buy_Product ON APP.Buy.id=buy_id WHERE product_id=? AND day>=? AND day<=?";
		String getIDString = "SELECT id FROM APP.Buy WHERE code=?";
		String getBillsString = "SELECT code FROM APP.Buy WHERE day>=? AND day<=?";
		try {
			getDiary = conn.prepareStatement(getDiaryString);
			getID = conn.prepareStatement(getIDString);
			getBills = conn.prepareStatement(getBillsString);
			psAll.add(getDiary);
			psAll.add(getID);
			psAll.add(getBills);
		} catch (SQLException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public BuyDAO() throws SQLException{
		super();
	}
	
	public static Integer getLastestPrice(String nameProduct, Calendar last) throws SQLException{
		ArrayList<Bill> bills = BuyDAO.getDiary(nameProduct, AttributesFactory.CAL2016, last);
		Integer res = null;
		Calendar latest = AttributesFactory.CAL2016;
		for (Bill bill : bills){
			int price = bill.getP(nameProduct);
			if (price > 0)
				if (latest.before(bill.getDay())){
					latest = bill.getDay();
					res = price;
				}
		}
		if (res == null){
			res = ProductDAO.getProductList().get(nameProduct).getInitP();
			if (res == 0) res = null;
		}
		return res;
	}
	
	public void inserts(String code, Date day, String nameSupplier) throws SQLException{
		insert.addData(code, day, nameSupplier);
	}
	
	public void insertsProduct(String codeBuy, String nameProduct, double q, double pq, int cost) throws SQLException{
		insertProduct.addData(codeBuy, nameProduct, q, pq, cost);
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
	
	@Override
	protected void initThreadedStatement() throws SQLException {
		String insertBuyString = "INSERT INTO APP.Buy (code, day, supplier_id)"
				+ " VALUES (?, ?, ?)";
		FunctionalStatement insertBuyFs = (PreparedStatement stm, Object[] objects) -> {
			String code = (String) objects[0];
			Date day = (Date) objects[1];
			String nameSupplier = (String) objects[2];
			stm.setString(1, code);
			stm.setDate(2, day);
			stm.setInt(3, SupplierDAO.getID(nameSupplier));
			stm.addBatch();
		};
		insert = new ThreadedStatement(conn, insertBuyString, insertBuyFs);
		tsAll.add(insert);
		
		String insertBuyProductString = "INSERT INTO APP.Buy_Product (buy_id, product_id, q, pq, cost)" 
				+ " VALUES (?, ?, ?, ?, ?)";
		FunctionalStatement insertBuyProductFs = (PreparedStatement stm, Object[] objects) -> {
			String codeBuy = (String) objects[0];
			String nameProduct = (String) objects[1];
			double q = (double) objects[2];
			double pq = (double) objects[3];
			int cost = (int) objects[4];
			stm.setInt(1, BuyDAO.getID(codeBuy));
			stm.setInt(2, ProductDAO.getID(nameProduct));
			stm.setDouble(3, q);
			stm.setDouble(4, pq);
			stm.setInt(5, cost);
			stm.addBatch();
		};
		insertProduct = new ThreadedStatement(conn, insertBuyProductString, insertBuyProductFs);
		tsAll.add(insertProduct);
	}

	public synchronized static Integer getID(String codeBuy) throws SQLException {
		if (idMap.containsKey(codeBuy)) return idMap.get(codeBuy);
		getID.setString(1, codeBuy);
		ResultSet results = getID.executeQuery();
		Integer id = null;
		while (results.next())
			id = results.getInt("id");
		results.close();
		idMap.put(codeBuy, id);
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
}
