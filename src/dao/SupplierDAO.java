package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import misc.BiMap;
import transferObject.Supplier;
import utility.Utility;

public class SupplierDAO extends DAO{
	private static HashMap<String, Supplier> supplierList = new HashMap<String, Supplier>();
	private static BiMap<String, Integer> idMap = new BiMap<String, Integer>();
	private static PreparedStatement insertPS, insertProduct, setPS, getS, getP, getID, update, setTax, deleteProduct;
	static{
		String insertString = "MERGE INTO APP.Supplier s"
				+ " USING SYSIBM.SYSDUMMY1"
				+ " ON s.name=?"
				+ " WHEN NOT MATCHED THEN INSERT (code, name) VALUES (?, ?)";
		String insertProductString = "INSERT INTO APP.Supplier_Product (supplier_id, product_id)"
				+ " VALUES (?, ?)";
		String setSupplierString = "UPDATE APP.Supplier"
				+ " SET lim=?, is_main=? WHERE id=?";
		String getSupplierString = "SELECT * FROM APP.Supplier";
		String getProductString = "SELECT name FROM APP.Supplier_Product JOIN APP.Product ON product_id=id"
				+ " WHERE supplier_id=?";
		String getIDString = "SELECT id FROM APP.Supplier WHERE name=?";
		String updateString = "UPDATE APP.Supplier"
				+ " SET code=? WHERE name=?";
		String setTaxString = "UPDATE APP.Supplier"
				+ " SET is_full=?, group_id=?, price_percent=? WHERE id=?";
		String removeProductString = "DELETE FROM APP.Supplier_Product"
				+ " WHERE supplier_id=? AND product_id=?";
		try {
			insertPS = conn.prepareStatement(insertString);
			insertProduct = conn.prepareStatement(insertProductString);
			setPS = conn.prepareStatement(setSupplierString);
			getS = conn.prepareStatement(getSupplierString);
			getP = conn.prepareStatement(getProductString);
			getID = conn.prepareStatement(getIDString);
			update = conn.prepareStatement(updateString);
			setTax = conn.prepareStatement(setTaxString);
			deleteProduct = conn.prepareStatement(removeProductString);
			psAll.add(insertPS);
			psAll.add(insertProduct);
			psAll.add(setPS);
			psAll.add(getS);
			psAll.add(getP);
			psAll.add(getID);
			psAll.add(update);
			psAll.add(setTax);
			psAll.add(deleteProduct);
		} catch (SQLException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public SupplierDAO() throws SQLException{
		super();
	}
	
	public static Supplier getSupplier(String nameProduct){
		ArrayList<Supplier> suppliers = new ArrayList<Supplier>();
		for (Supplier supplier : supplierList.values()){
			if (supplier.getProducts().contains(nameProduct))
				suppliers.add(supplier);
		}
		for (Supplier supplier : suppliers)
			if (supplier.isMain())
				return supplier;
		if (suppliers.size() > 0)
			return suppliers.get(0);
		return null;
	}
	
	public static void update(String codeSupplier, String nameSupplier) throws SQLException{
		update.setString(1, codeSupplier);
		update.setString(2, nameSupplier);
		update.executeUpdate();
		supplierList.get(nameSupplier).setCode(codeSupplier);
	}
	
	public static void insert(String codeSupplier, String nameSupplier) throws SQLException{
		insertPS.setString(1, nameSupplier);
		insertPS.setString(2, codeSupplier);
		insertPS.setString(3, nameSupplier);
		insertPS.executeUpdate();
		supplierList.put(nameSupplier, new Supplier(codeSupplier, nameSupplier));
	}
	
	public static void insertProduct(String nameSupplier, String nameProduct) throws SQLException{
		insertProduct.setInt(1, SupplierDAO.getID(nameSupplier));
		insertProduct.setInt(2, ProductDAO.getID(nameProduct));
		insertProduct.executeUpdate();
	}
	
	public static void deleteProduct(String nameSupplier, String nameProduct) throws SQLException{
		deleteProduct.setInt(1, SupplierDAO.getID(nameSupplier));
		deleteProduct.setInt(2, ProductDAO.getID(nameProduct));
		deleteProduct.executeUpdate();
	}
	
	public static void setSupplier(String nameSupplier, Double lim, boolean is_main) throws SQLException{
		Utility.setLim(setPS, 1, lim);
		setPS.setBoolean(2, is_main);
		setPS.setInt(3, SupplierDAO.getID(nameSupplier));
		setPS.executeUpdate();
	}
	
	public static void setTax(String nameSupplier, boolean isFull, int groupID, double pricePercent) throws SQLException{
		setTax.setBoolean(1, isFull);
		setTax.setInt(2, groupID);
		setTax.setDouble(3, pricePercent);
		setTax.setInt(4, SupplierDAO.getID(nameSupplier));
		setTax.executeUpdate();
	}
	
	public static void updateSupplierList() throws SQLException{
		ResultSet results = getS.executeQuery();
		supplierList = new HashMap<String, Supplier>();
		idMap = new BiMap<String, Integer>();
		while (results.next()){
			int id = results.getInt("id");
			String code = results.getString("code");
			String name = results.getString("name");
			boolean is_main = results.getBoolean("is_main");
			Double lim = results.getDouble("lim");
			if (lim == 0) lim = null;
			boolean is_full = results.getBoolean("is_full");
			int group_id = results.getInt("group_id");
			double price_percent = results.getDouble("price_percent");
			getP.setInt(1, id);
			ResultSet products = getP.executeQuery();
			HashSet<String> productList = new HashSet<String>();
			while (products.next()){
				String nameProduct = products.getString("name");
				productList.add(nameProduct);
			}
			products.close();
			supplierList.put(name, new Supplier(id, code, name, is_main, lim, productList, is_full, group_id, price_percent));
			idMap.add(name, id);
		}
		results.close();
	}
	
	public synchronized static Integer getID(String nameSupplier) throws SQLException {
		if (idMap.containsL(nameSupplier)) return idMap.getR(nameSupplier);
		getID.setString(1, nameSupplier);
		ResultSet result = getID.executeQuery();
		Integer id = null;
		while (result.next())
			id = result.getInt("id");
		result.close();
		idMap.add(nameSupplier, id);
		return id;
	}
	
	public static String getName(Integer id){
		return idMap.getL(id);
	}
	
	public static HashMap<String, Supplier> getSupplierList(){
		return supplierList;
	}

	@Override
	protected void initThreadedStatement() throws SQLException {
	}
}
