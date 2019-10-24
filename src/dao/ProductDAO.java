package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import factory.AttributesFactory;
import factory.ComparatorFactory;
import misc.BiMap;
import misc.FunctionalStatement;
import misc.Pair;
import misc.ThreadedStatement;
import transferObject.Bill;
import transferObject.Product;
import transferObject.Status;

public class ProductDAO extends DAO{
	private ThreadedStatement insertStatus, removeStatus;
	private static HashSet<String> nspSet = new HashSet<String>();
	private static HashMap<String, Product> productList = new HashMap<String, Product>();
	private static HashMap<String, String> codeToName = new HashMap<String, String>();
	// PPMap
	private static BiMap<Pair<String, String>, Pair<String, String>> ppMap, connectionMap;
	private static BiMap<String, String> newCodeToNewName, taxConnection;
	private static BiMap<String, Integer> idMap = new BiMap<String, Integer>();
	private static PreparedStatement insertPS, insertTax, updateP, updateLast, updateQ, updatePQ, updatePP, updateC, updateNSP, updateTax, getStatus, getProduct, getID;
	static{
		String insertString = "MERGE INTO APP.Product p"
				+ " USING SYSIBM.SYSDUMMY1"
				+ " ON p.name=?"
				+ " WHEN NOT MATCHED THEN INSERT (code, name) VALUES (?, ?)";
		String insertTaxString = "INSERT INTO APP.Tax_Connection (product_name, tax_name) VALUES (?, ?)";
		String updatePString = "UPDATE APP.Product"
				+ " SET init_p=? WHERE name=? AND init_p IS NULL";
		String updateLastString = "UPDATE APP.Product"
				+ " SET last_p=?, last_code=? WHERE name=?";
		String updateQString = "UPDATE APP.Product"
				+ " SET init_q=? WHERE name=?";
		String updatePQString = "UPDATE APP.Product"
				+ " SET init_pq=? WHERE name=?";
		String updatePPString = "SELECT code, name, promoCode, promoName FROM APP.PtoPP";
		String updateCString = "SELECT code, name, newCode, newName FROM APP.Conn";
		String updateNSPString = "SELECT name FROM APP.NonSplitProduct";
		String updateTaxString = "SELECT product_name, tax_name FROM APP.Tax_Connection";
		String getProductString = "SELECT * FROM APP.Product";
		String getStatusString = "SELECT code, q, pq FROM APPDATE.Product_Status"
				+ " WHERE product_id=?";
		String getIDString = "SELECT id FROM APP.Product WHERE name=?";
		try {
			insertPS = conn.prepareStatement(insertString);
			insertTax = conn.prepareStatement(insertTaxString);
			updateP = conn.prepareStatement(updatePString);
			updateLast = conn.prepareStatement(updateLastString);
			updateQ = conn.prepareStatement(updateQString);
			updatePQ = conn.prepareStatement(updatePQString);
			updatePP = conn.prepareStatement(updatePPString);
			updateC = conn.prepareStatement(updateCString);			
			updateNSP = conn.prepareStatement(updateNSPString);
			updateTax = conn.prepareStatement(updateTaxString);
			getProduct = conn.prepareStatement(getProductString);
			getStatus = conn.prepareStatement(getStatusString);
			getID = conn.prepareStatement(getIDString);
			psAll.add(insertPS);
			psAll.add(insertTax);
			psAll.add(updateP);
			psAll.add(updateLast);
			psAll.add(updateQ);
			psAll.add(updatePQ);
			psAll.add(updatePP);
			psAll.add(updateC);
			psAll.add(updateNSP);
			psAll.add(updateTax);
			psAll.add(getProduct);
			psAll.add(getStatus);
			psAll.add(getID);
		} catch (SQLException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	public static BiMap<String, String> getTaxConnection(){
		return taxConnection;
	}
	
	public static boolean isNewCode(String newCode){
		return newCodeToNewName.containsL(newCode);
	}
	
	public static boolean isNewName(String newName){
		return newCodeToNewName.containsR(newName);
	}
	
	public static String getNewCode(String newName){
		return newCodeToNewName.getL(newName);
	}
	
	public static String getNewName(String newCode){
		return newCodeToNewName.getR(newCode);
	}
	
	public static boolean isOld(String code, String name){
		return connectionMap.containsL(new Pair<String, String>(code, name));
	}
	
	public static boolean isNew(String code, String name){
		return connectionMap.containsR(new Pair<String, String>(code, name));
	}
	
	public static Pair<String, String> converseNew(String newCode, String newName){
		return connectionMap.getL(new Pair<String, String>(newCode, newName));
	}
	
	public static Pair<String, String> getNew(String code, String name){
		return connectionMap.getR(new Pair<String, String>(code, name));
	}
	
	public static boolean isPromo(String code, String name){
		return ppMap.containsR(new Pair<String, String>(code, name));
	}
	
	public static Pair<String, String> conversePromo(String codePromo, String namePromo){
		return ppMap.getL(new Pair<String, String>(codePromo, namePromo));
	}
	
	public static boolean isSplit(String nameProduct){
		return !nspSet.contains(nameProduct);
	}
	
	public static void insert(String codeProduct, String nameProduct) throws SQLException{
		if (codeProduct.length() == 0 || nameProduct.length() == 0)
			return;
		insertPS.setString(1, nameProduct);
		insertPS.setString(2, codeProduct);
		insertPS.setString(3, nameProduct);
		insertPS.executeUpdate();
	}
	
	public static void updateP(String nameProduct, int price) throws SQLException{
		updateP.setInt(1, price);
		updateP.setString(2, nameProduct);
		updateP.executeUpdate();
	}
	
	public static void updateLast(String nameProduct, int lastPrice, String lastCode) throws SQLException{
		updateLast.setInt(1, lastPrice);
		updateLast.setString(2, lastCode);
		updateLast.setString(3, nameProduct);
		updateLast.executeUpdate();
	}
	
	public static void updateInitQ(String nameProduct, double init_q) throws SQLException{
		updateQ.setDouble(1, init_q);
		updateQ.setString(2, nameProduct);
		updateQ.executeUpdate();
	}
	
	public static void updateInitPQ(String nameProduct, double init_pq) throws SQLException{
		updatePQ.setDouble(1, init_pq);
		updatePQ.setString(2, nameProduct);
		updatePQ.executeUpdate();
	}
	
	public static void initMaps() throws SQLException{
		ResultSet results = updatePP.executeQuery();
		ppMap = new BiMap<Pair<String, String>, Pair<String, String>>();
		while (results.next()){
			String code = results.getString("code");
			String name = results.getString("name");
			String promoCode = results.getString("promoCode");
			String promoName = results.getString("promoName");
			ppMap.add(new Pair<String, String>(code, name), new Pair<String, String>(promoCode, promoName));
		}
		results.close();
		results = updateC.executeQuery();
		connectionMap = new BiMap<Pair<String, String>, Pair<String, String>>();
		newCodeToNewName = new BiMap<String, String>();
		while (results.next()){
			String code = results.getString("code");
			String name = results.getString("name");
			String newCode = results.getString("newCode");
			String newName = results.getString("newName");
			newCodeToNewName.add(newCode, newName);
			connectionMap.add(new Pair<String, String>(code, name), new Pair<String, String>(newCode, newName));
		}
		results.close();
		results = updateNSP.executeQuery();
		nspSet = new HashSet<String>();
		while (results.next()){
			String name = results.getString("name");
			nspSet.add(name);
		}
		results.close();
	}
	
	public static void insertTaxConnection(String nameProduct, String nameTax) throws SQLException{
		insertTax.setString(1, nameProduct);
		insertTax.setString(2, nameTax);
		insertTax.executeUpdate();
	}
	
	public static void updateTaxConnection() throws SQLException{
		ResultSet results = updateTax.executeQuery();
		taxConnection = new BiMap<String, String>();
		while (results.next()){
			String nameProduct = results.getString("product_name");
			String nameTax = results.getString("tax_name");
			taxConnection.add(nameProduct, nameTax);
		}
		results.close();
	}
	
	private static void updateIDMap() throws SQLException{
		ResultSet results = getProduct.executeQuery();
		idMap = new BiMap<String, Integer>();
		while (results.next()){
			int id = results.getInt("id");
			String name = results.getString("name");
			idMap.add(name, id);
		}
		results.close();
	}
	
	public static void updateProductList() throws SQLException{
		ProductDAO.updateIDMap();
		ResultSet results = getProduct.executeQuery();
		productList = new HashMap<String, Product>();
		codeToName = new HashMap<String, String>();
		while (results.next()){
			int id = results.getInt("id");
			String code = results.getString("code");
			String name = results.getString("name");
			int init_p = results.getInt("init_p");
			int last_p = results.getInt("last_p");
			String last_code = results.getString("last_code");
			double init_q = results.getDouble("init_q");
			double init_pq = results.getDouble("init_pq");
			getStatus.setInt(1, id);
			ResultSet status_res = getStatus.executeQuery();
			ArrayList<Status> statuses = new ArrayList<Status>();
			if (init_q > 0){
				Bill bill = new Bill(AttributesFactory.CAL2016, AttributesFactory.CODE, AttributesFactory.CODE);
				bill.addElement(name, init_q, 0, (int) (init_q * init_p));
				statuses.add(new Status(bill, init_q, 0));
			}
			if (init_pq > 0){
				Bill bill = new Bill(AttributesFactory.CAL2016, AttributesFactory.PROMO_CODE, AttributesFactory.PROMO_CODE);
				bill.addElement(name, 0, init_pq, 0);
				statuses.add(new Status(bill, 0, init_pq));
			}
			while (status_res.next()){
				String codeBill = status_res.getString("code");
				Bill bill = BillDAO.getBill(codeBill);
				double q = status_res.getDouble("q");
				double pq = status_res.getDouble("pq");
				statuses.add(new Status(bill, q, pq));
			}
			status_res.close();
			statuses.sort(ComparatorFactory.getStatusComparator());
			if (Status.getNPIndex(name, statuses) == null){
				Bill bill = new Bill(AttributesFactory.CAL2016, AttributesFactory.CODE, AttributesFactory.CODE);
				bill.addElement(name, init_q, 0, (int) (init_q * init_p));
				statuses.add(new Status(bill, init_q, 0));
			}
			if (Status.getPIndex(name, statuses) == null){
				Bill bill = new Bill(AttributesFactory.CAL2016, AttributesFactory.PROMO_CODE, AttributesFactory.PROMO_CODE);
				bill.addElement(name, 0, init_pq == 0 ? -1 : init_pq, 0);
				statuses.add(new Status(bill, 0, init_pq));
			}
//			if (name.equals("THUC AN CHO-VI BO LON 400")) {
//				System.out.println("IMPORTANT");
//				for (Status status : statuses) {
//					System.out.println(status);
//				}
//				System.out.println(Status.getPIndex(name, statuses));
//				System.out.println(Status.getNPIndex(name, statuses));
//			}
			statuses.sort(ComparatorFactory.getStatusComparator());
			Product product = new Product(id, code, name, init_q, init_pq, init_p, last_p, last_code, statuses);
			productList.put(name, product);
			codeToName.put(code, name);
		}
		results.close();
	}
	
	public synchronized static Integer getID(String nameProduct) throws SQLException {
		if (idMap.containsL(nameProduct)) return idMap.getR(nameProduct);
		getID.setString(1, nameProduct);
		ResultSet result = getID.executeQuery();
		Integer id = null;
		while (result.next())
			id = result.getInt("id");
		result.close();
		idMap.add(nameProduct, id);
		return id;
	}
	
	public static String getName(Integer id){
		return idMap.getL(id);
	}
	
	public static HashMap<String, Product> getProductList(){
		return productList;
	}
	
	public static String getProductName(String codeProduct){
		return codeToName.get(codeProduct);
	}
	
	public void prepare() throws SQLException{
		for (Product product : productList.values()){
			String nameProduct = product.getName();
			ProductDAO.updateLast(nameProduct, product.getLastP(), product.getLastCode());
			removeStatus.addData(nameProduct);
			boolean isNP = false, isP = false;
			for (Status status : product.getStatuses()){
				String codeBill = status.getBill().getCodeBill();
				if (codeBill.equals(AttributesFactory.CODE)){
					ProductDAO.updateInitQ(nameProduct, status.getQ());
					isNP = true;
				} else if (codeBill.equals(AttributesFactory.PROMO_CODE)){
					ProductDAO.updateInitPQ(nameProduct, status.getPQ());
					isP = true;
				} else insertStatus.addData(nameProduct, codeBill, status.getQ(), status.getPQ());
			}
			if (!isNP) ProductDAO.updateInitQ(nameProduct, -1);
			if (!isP) ProductDAO.updateInitPQ(nameProduct, -1);
		}
	}
	
	public ProductDAO() throws SQLException{
		super();
	}

	@Override
	protected void initThreadedStatement() throws SQLException {
		String removeStatusString = "DELETE FROM APPDATE.Product_Status"
								+ " WHERE product_id=?";
		FunctionalStatement removeStatusFs = (PreparedStatement stm, Object[] objects) -> {
			String nameProduct = (String) objects[0];
			stm.setInt(1, ProductDAO.getID(nameProduct));
			stm.addBatch();
		};
		removeStatus = new ThreadedStatement(conn, removeStatusString, removeStatusFs);
		tsAll.add(removeStatus);
		
		String insertStatusString = "INSERT INTO APPDATE.Product_Status"
								+ " VALUES (?, ?, ?, ?)";
		FunctionalStatement insertStatusFs = (PreparedStatement stm, Object[] objects) -> {
			String nameProduct = (String) objects[0];
			String codeBill = (String) objects[1];
			double q = (double) objects[2];
			double pq = (double) objects[3];
			stm.setInt(1, ProductDAO.getID(nameProduct));
			stm.setString(2, codeBill);
			stm.setDouble(3, q);
			stm.setDouble(4, pq);
			stm.addBatch();
		};
		insertStatus = new ThreadedStatement(conn, insertStatusString, insertStatusFs);
		tsAll.add(insertStatus);
	}
}
