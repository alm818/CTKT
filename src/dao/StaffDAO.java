package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import misc.BiMap;
import transferObject.Staff;

public class StaffDAO extends DAO{
	// IDENTIFIER: Name
	private static HashMap<String, Staff> staffList = new HashMap<String, Staff>();
	private static BiMap<String, Integer> idMap = new BiMap<String, Integer>();
	private static PreparedStatement insert, remove, get, getID;
	static{
		String insertString = "INSERT INTO APP.Staff (name, position) VALUES (?, ?)";
		String removeString = "DELETE FROM APP.Staff WHERE id=?";
		String getStaffString = "SELECT * FROM APP.Staff";
		String getIDString = "SELECT id FROM APP.Staff WHERE name=?";
		try {
			insert = conn.prepareStatement(insertString);
			remove = conn.prepareStatement(removeString);
			get = conn.prepareStatement(getStaffString);
			getID = conn.prepareStatement(getIDString);
			psAll.add(insert);
			psAll.add(remove);
			psAll.add(get);
			psAll.add(getID);
		} catch (SQLException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void insert(String nameStaff, String position) throws SQLException{
		insert.setString(1, nameStaff);
		insert.setString(2, position);
		insert.executeUpdate();
		Integer id = StaffDAO.getID(nameStaff);
		staffList.put(nameStaff, new Staff(id, nameStaff, position));
	}
	
	public static void remove(String nameStaff) throws SQLException{
		int id = StaffDAO.getID(nameStaff);
		remove.setInt(1, id);
		remove.executeUpdate();
		staffList.remove(nameStaff);
	}
	
	public static void updateStaffList() throws SQLException{
		ResultSet results = get.executeQuery();
		staffList = new HashMap<String, Staff>();
		idMap = new BiMap<String, Integer>();
		while (results.next()){
			int id = results.getInt("id");
			String name = results.getString("name");
			Staff staff = new Staff(results);
			staffList.put(name, staff);
			idMap.add(name, id);
		}
		results.close();
	}
	
	public static Integer getID(String nameStaff) throws SQLException {
		if (idMap.containsL(nameStaff)) return idMap.getR(nameStaff);
		getID.setString(1, nameStaff);
		ResultSet result = getID.executeQuery();
		Integer id = null;
		while (result.next())
			id = result.getInt("id");
		result.close();
		idMap.add(nameStaff, id);
		return id;
	}
	
	public static void write(HashMap<String, Staff> staffList) throws SQLException{
		PreparedStatement stm = conn.prepareStatement("UPDATE APP.Staff"
				+ " SET base_wage=?, sub_wage=?, base_eff=?, pob=?, insurance=?, imprest=?,"
				+ " day_off=?, holiday=? WHERE name=?");
		for (Staff staff : staffList.values()){
			stm.setInt(1, staff.getBaseWage());
			stm.setInt(2, staff.getSubWage());
			stm.setInt(3, staff.getBaseEff());
			stm.setInt(4, staff.getPOB());
			stm.setInt(5, staff.getInsurance());
			stm.setInt(6, staff.getImprest());
			stm.setDouble(7, staff.getDayOff());
			stm.setInt(8, staff.getHoliday());
			stm.setString(9, staff.getName());
			stm.addBatch();
		}
		stm.executeBatch();
		conn.commit();
		stm.close();
	}
	
	public static void write() throws SQLException{
		StaffDAO.write(staffList);
	}
	
	public static String getName(Integer id){
		return idMap.getL(id);
	}
	
	public static HashMap<String, Staff> getStaffList(){
		return staffList;
	}
	
	public static long getTotalEff() {
		long totalEff = 0;
		for (Staff staff : staffList.values()) 
			totalEff += staff.getBaseEff();
		return totalEff;
	}
	
	public StaffDAO() throws SQLException {
		super();
	}
	
	@Override
	protected void initThreadedStatement() throws SQLException {
	}
}
