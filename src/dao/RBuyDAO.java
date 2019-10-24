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

public class RBuyDAO extends DAO{
	private ThreadedStatement insert, insertProduct;
	private static HashMap<String, Integer> idMap = new HashMap<String, Integer>();
	private static PreparedStatement getID, getDiary, getBills;
	static{
		String getIDString = "SELECT id FROM APP.RBuy WHERE code=?";
		String getDiaryString = "SELECT code FROM APP.RBuy JOIN APP.RBuy_Product ON APP.RBuy.id=rbuy_id WHERE product_id=? AND day>=? AND day<=?";
		String getBillsString = "SELECT code FROM APP.RBuy WHERE day>=? AND day<=?";
		try {
			getID = conn.prepareStatement(getIDString);
			getDiary = conn.prepareStatement(getDiaryString);
			getBills = conn.prepareStatement(getBillsString);
			psAll.add(getID);
			psAll.add(getDiary);
			psAll.add(getBills);
		} catch (SQLException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public RBuyDAO() throws SQLException{
		super();
	}
	
	public void inserts(String code, Date day, String nameSupplier) throws SQLException{
		insert.addData(code, day, nameSupplier);
	}
	
	public void insertsProduct(String codeRBuy, String nameProduct, double q, double pq, int cost) throws SQLException{
		insertProduct.addData(codeRBuy, nameProduct, q, pq, cost);
	}

	@Override
	protected void initThreadedStatement() throws SQLException {
		String insertRBuyString = "INSERT INTO APP.RBuy (code, day, supplier_id)"
				+ " VALUES (?, ?, ?)";
		FunctionalStatement insertRBuyFs = (PreparedStatement stm, Object[] objects) -> {
			String code = (String) objects[0];
			Date day = (Date) objects[1];
			String nameSupplier = (String) objects[2];
			stm.setString(1, code);
			stm.setDate(2, day);
			stm.setInt(3, SupplierDAO.getID(nameSupplier));
			stm.addBatch();
		};
		insert = new ThreadedStatement(conn, insertRBuyString, insertRBuyFs);
		tsAll.add(insert);
		
		String insertRBuyProductString = "INSERT INTO APP.RBuy_Product (rbuy_id, product_id, q, pq, cost)" 
				+ " VALUES (?, ?, ?, ?, ?)";
		FunctionalStatement insertRBuyProductFs = (PreparedStatement stm, Object[] objects) -> {
			String codeRBuy = (String) objects[0];
			String nameProduct = (String) objects[1];
			double q = (double) objects[2];
			double pq = (double) objects[3];
			int cost = (int) objects[4];
			stm.setInt(1, RBuyDAO.getID(codeRBuy));
			stm.setInt(2, ProductDAO.getID(nameProduct));
			stm.setDouble(3, q);
			stm.setDouble(4, pq);
			stm.setInt(5, cost);
			stm.addBatch();
		};
		insertProduct = new ThreadedStatement(conn, insertRBuyProductString, insertRBuyProductFs);
		tsAll.add(insertProduct);
	}
	
	public synchronized static Integer getID(String codeRBuy) throws SQLException {
		if (idMap.containsKey(codeRBuy)) return idMap.get(codeRBuy);
		getID.setString(1, codeRBuy);
		ResultSet results = getID.executeQuery();
		Integer id = null;
		while (results.next())
			id = results.getInt("id");
		results.close();
		idMap.put(codeRBuy, id);
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
