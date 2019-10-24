package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import misc.BiMap;
import transferObject.Customer;
public class CustomerDAO extends DAO{
	// IDENTIFIER: Code
	private static HashMap<String, Customer> customerList = new HashMap<String, Customer>();
	private static BiMap<String, Integer> idMap = new BiMap<String, Integer>();
	private static PreparedStatement insert, getPS, getID;
	static{
		String insertString = "MERGE INTO APP.Customer c"
				+ " USING SYSIBM.SYSDUMMY1"
				+ " ON c.code=? AND c.name=?"
				+ " WHEN NOT MATCHED THEN INSERT (code, name) VALUES (?, ?)";
		String getCustomerString = "SELECT * FROM APP.Customer";
		String getIDString = "SELECT id FROM APP.Customer WHERE code=?";
		try {
			insert = conn.prepareStatement(insertString);
			getPS = conn.prepareStatement(getCustomerString);
			getID = conn.prepareStatement(getIDString);
			psAll.add(insert);
			psAll.add(getPS);
			psAll.add(getID);
		} catch (SQLException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void insert(String codeCustomer, String nameCustomer) throws SQLException{
		if (codeCustomer.length() == 0 && nameCustomer.length() == 0)
			return;
		insert.setString(1, codeCustomer);
		insert.setString(2, nameCustomer);
		insert.setString(3, codeCustomer);
		insert.setString(4, nameCustomer);
		insert.executeUpdate();
	}
	
	public static void updateCustomerList() throws SQLException{
		ResultSet results = getPS.executeQuery();
		customerList = new HashMap<String, Customer>();
		idMap = new BiMap<String, Integer>();
		while (results.next()){
			int id = results.getInt("id");
			String code = results.getString("code");
			String name = results.getString("name");
			customerList.put(code, new Customer(id, code, name));
			idMap.add(code, id);
		}
		results.close();
	}
	
	public synchronized static Integer getID(String codeCustomer) throws SQLException {
		if (idMap.containsL(codeCustomer)) return idMap.getR(codeCustomer);
		getID.setString(1, codeCustomer);
		ResultSet result = getID.executeQuery();
		Integer id = null;
		while (result.next())
			id = result.getInt("id");
		result.close();
		idMap.add(codeCustomer, id);
		return id;
	}
	
	public static String getCode(Integer id){
		return idMap.getL(id);
	}
	
	public static HashMap<String, Customer> getCustomerList(){
		return customerList;
	}
	
	public CustomerDAO() throws SQLException{
		super();
	}

	@Override
	protected void initThreadedStatement() throws SQLException {
	}
}