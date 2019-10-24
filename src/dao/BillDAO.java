package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;

import factory.AttributesFactory;
import misc.Pair;
import transferObject.Bill;
import transferObject.Product;
import utility.CalendarUtility;
import utility.Utility;

public class BillDAO extends DAO{
	private static HashMap<String, Bill> buyMap = new HashMap<String, Bill>();
	private static HashMap<String, Bill> sellMap = new HashMap<String, Bill>();
	private static HashMap<String, Bill> rSellMap = new HashMap<String, Bill>();
	private static HashMap<String, Bill> rBuyMap = new HashMap<String, Bill>();
	private static PreparedStatement getBuy, getSell, getRSell, getRBuy,
		getProductBuy, getProductSell, getProductRSell, getProductRBuy,
		getStatusBuy, getStatusSell, getStatusRSell, getStatusRBuy;
	static {
		String getBuyString = "SELECT day, supplier_id FROM APP.Buy WHERE APP.Buy.code=? ORDER BY day DESC";
		String getSellString = "SELECT day, customer_id FROM APP.Sell WHERE APP.Sell.code=? ORDER BY day DESC";
		String getRSellString = "SELECT day, customer_id FROM APP.RSell WHERE APP.RSell.code=? ORDER BY day DESC";
		String getRBuyString = "SELECT day, supplier_id FROM APP.RBuy WHERE APP.RBuy.code=? ORDER BY day DESC";
		String getProductBuyString = "SELECT id, product_id, q, pq, cost FROM APP.Buy_Product WHERE buy_id=?";
		String getProductSellString = "SELECT id, product_id, q, pq, cost FROM APP.Sell_Product WHERE sell_id=?";
		String getProductRSellString = "SELECT id, product_id, q, pq, cost FROM APP.RSell_Product WHERE rsell_id=?";
		String getProductRBuyString = "SELECT id, product_id, q, pq, cost FROM APP.RBuy_Product WHERE rbuy_id=?";
		String getStatusBuyString = "SELECT q, pq FROM APPDATE.Buy_Status WHERE buy_id=?";
		String getStatusSellString = "SELECT q, pq FROM APPDATE.Sell_Status WHERE sell_id=?";
		String getStatusRSellString = "SELECT q, pq FROM APPDATE.RSell_Status WHERE rsell_id=?";
		String getStatusRBuyString = "SELECT q, pq FROM APPDATE.RBuy_Status WHERE rbuy_id=?";
		try {
			getBuy = conn.prepareStatement(getBuyString);
			getSell = conn.prepareStatement(getSellString);
			getRSell = conn.prepareStatement(getRSellString);
			getRBuy = conn.prepareStatement(getRBuyString);
			getProductBuy = conn.prepareStatement(getProductBuyString);
			getProductSell = conn.prepareStatement(getProductSellString);
			getProductRSell = conn.prepareStatement(getProductRSellString);
			getProductRBuy = conn.prepareStatement(getProductRBuyString);
			getStatusBuy = conn.prepareStatement(getStatusBuyString);
			getStatusSell = conn.prepareStatement(getStatusSellString);
			getStatusRSell = conn.prepareStatement(getStatusRSellString);
			getStatusRBuy = conn.prepareStatement(getStatusRBuyString);
			psAll.add(getBuy);
			psAll.add(getSell);
			psAll.add(getRSell);
			psAll.add(getRBuy);
			psAll.add(getProductBuy);
			psAll.add(getProductSell);
			psAll.add(getProductRSell);
			psAll.add(getProductRBuy);
			psAll.add(getStatusBuy);
			psAll.add(getStatusSell);
			psAll.add(getStatusRSell);
			psAll.add(getStatusRBuy);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateBill(){
		Bill npBill = new Bill(AttributesFactory.CAL2016, AttributesFactory.CODE, AttributesFactory.NAME);
		Bill pBill = new Bill(AttributesFactory.CAL2016, AttributesFactory.PROMO_CODE, AttributesFactory.PROMO_NAME);
		for (Product product : ProductDAO.getProductList().values()){
			int cost = (int) (product.getInitQ() * product.getInitP());
			npBill.addElement(product.getName(), product.getInitQ(), 0, cost);
			pBill.addElement(product.getName(), 0, product.getInitPQ() == 0 ? -1 : product.getInitPQ(), 0);
		}
		buyMap.put(AttributesFactory.CODE, npBill);
		buyMap.put(AttributesFactory.PROMO_CODE, pBill);
	}
	
	public static Pair<Double, Double> getStatus(String codeBill, String nameProduct) throws SQLException{
		PreparedStatement getStatus;
		Integer id = BillDAO.getBill(codeBill).getProduct(nameProduct).getID();
		switch (Utility.getBillType(codeBill)){
			case BUY:
				getStatus = getStatusBuy;
				break;
			case SELL:
				getStatus = getStatusSell;
				break;
			case RSELL:
				getStatus = getStatusRSell;
				break;
			case RBUY:
				getStatus = getStatusRBuy;
				break;
			default:
				throw new SQLException("INVALID CODE " + codeBill);
		}
		getStatus.setInt(1, id);
		ResultSet result = getStatus.executeQuery();
		Pair<Double, Double> pair = null;
		while (result.next()){
			double q = result.getDouble("q");
			double pq = result.getDouble("pq");
			pair = new Pair<Double, Double>(q, pq);
			break;
		}
		result.close();
		return pair;
	}
	
	public static Bill getBill(String codeBill) throws SQLException{
		switch (Utility.getBillType(codeBill)){
			case INITNP:
			case INITP:
				return buyMap.get(codeBill);
			case BUY:
				if (buyMap.containsKey(codeBill))
					return buyMap.get(codeBill);
				return BillDAO.getBuyBill(codeBill);
			case SELL:
				if (sellMap.containsKey(codeBill))
					return sellMap.get(codeBill);
				return BillDAO.getSellBill(codeBill);
			case RSELL:
				if (rSellMap.containsKey(codeBill))
					return rSellMap.get(codeBill);
				return BillDAO.getRSellBill(codeBill);
			case RBUY:
				if (rBuyMap.containsKey(codeBill))
					return rBuyMap.get(codeBill);
				return BillDAO.getRBuyBill(codeBill);
			default:
				throw new SQLException("INVALID CODE " + codeBill);
		}
	}
	
	private synchronized static Bill getBuyBill(String codeBill) throws SQLException{
		Integer id = BuyDAO.getID(codeBill);
		if (id == null) return null;
		getBuy.setString(1, codeBill);
		ResultSet result = getBuy.executeQuery();
		Bill bill = null;
		while (result.next()){
			Calendar day = CalendarUtility.toCalendar(result.getDate(1));
			int target_id = result.getInt(2);
			String target;
			day = CalendarUtility.BuyCal(day);
			target = SupplierDAO.getName(target_id);
			bill = new Bill(day, codeBill, target);
			break;
		}
		result.close();
		getProductBuy.setInt(1, id);
		ResultSet results = getProductBuy.executeQuery();
		while (results.next()){
			int e_id = results.getInt("id");
			String nameProduct = ProductDAO.getName(results.getInt("product_id"));
			double q = results.getDouble("q");
			double pq = results.getDouble("pq");
			Integer cost = results.getInt("cost");
			bill.addElement(nameProduct, e_id, q, pq, cost);
		}
		results.close();
		buyMap.put(codeBill, bill);
		return bill;
	}
	
	private synchronized static Bill getSellBill(String codeBill) throws SQLException{
		Integer id = SellDAO.getID(codeBill);
		if (id == null) return null;
		getSell.setString(1, codeBill);
		ResultSet result = getSell.executeQuery();
		Bill bill = null;
		while (result.next()){
			Calendar day = CalendarUtility.toCalendar(result.getDate(1));
			int target_id = result.getInt(2);
			String target;
			day = CalendarUtility.SellCal(day);
			target = CustomerDAO.getCode(target_id);
			bill = new Bill(day, codeBill, target);
			break;
		}
		result.close();
		if (bill == null) return null;
		getProductSell.setInt(1, id);
		ResultSet results = getProductSell.executeQuery();
		while (results.next()){
			int e_id = results.getInt("id");
			String nameProduct = ProductDAO.getName(results.getInt("product_id"));
			double q = results.getDouble("q");
			double pq = results.getDouble("pq");
			Integer cost = results.getInt("cost");
			bill.addElement(nameProduct, e_id, q, pq, cost);
		}
		results.close();
		sellMap.put(codeBill, bill);
		return bill;
	}
	
	private synchronized static Bill getRSellBill(String codeBill) throws SQLException{
		Integer id = RSellDAO.getID(codeBill);
		if (id == null) return null;
		getRSell.setString(1, codeBill);
		ResultSet result = getRSell.executeQuery();
		Bill bill = null;
		while (result.next()){
			Calendar day = CalendarUtility.toCalendar(result.getDate(1));
			int target_id = result.getInt(2);
			String target;
			day = CalendarUtility.RSellCal(day);
			target = CustomerDAO.getCode(target_id);
			bill = new Bill(day, codeBill, target);
			break;
		}
		result.close();
		if (bill == null) return null;
		getProductRSell.setInt(1, id);
		ResultSet results = getProductRSell.executeQuery();
		while (results.next()){
			int e_id = results.getInt("id");
			String nameProduct = ProductDAO.getName(results.getInt("product_id"));
			double q = results.getDouble("q");
			double pq = results.getDouble("pq");
			Integer cost = results.getInt("cost");
			bill.addElement(nameProduct, e_id, q, pq, cost);
		}
		results.close();
		rSellMap.put(codeBill, bill);
		return bill;
	}
	
	private synchronized static Bill getRBuyBill(String codeBill) throws SQLException{
		Integer id = RBuyDAO.getID(codeBill);
		if (id == null) return null;
		getRBuy.setString(1, codeBill);
		ResultSet result = getRBuy.executeQuery();
		Bill bill = null;
		while (result.next()){
			Calendar day = CalendarUtility.toCalendar(result.getDate(1));
			int target_id = result.getInt(2);
			String target;
			day = CalendarUtility.RBuyCal(day);
			target = SupplierDAO.getName(target_id);
			bill = new Bill(day, codeBill, target);
			break;
		}
		result.close();
		if (bill == null) return null;
		getProductRBuy.setInt(1, id);
		ResultSet results = getProductRBuy.executeQuery();
		while (results.next()){
			int e_id = results.getInt("id");
			String nameProduct = ProductDAO.getName(results.getInt("product_id"));
			double q = results.getDouble("q");
			double pq = results.getDouble("pq");
			Integer cost = results.getInt("cost");
			bill.addElement(nameProduct, e_id, q, pq, cost);
		}
		results.close();
		rBuyMap.put(codeBill, bill);
		return bill;
	}
	
	public BillDAO() throws SQLException{
		super();
	}
	
	@Override
	protected void initThreadedStatement() throws SQLException {
	}
}
