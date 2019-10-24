package fao;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import dao.ProductDAO;
import dao.StaffDAO;
import dao.SupplierDAO;
import factory.DAOFactory;
import misc.BiMap;
import transferObject.Staff;
import transferObject.StatStorage;
import transferObject.Supplier;
import utility.Utility;

public class FileStat {
	private final static Logger LOGGER = Logger.getLogger(FileStat.class.getName());
	private static Connection conn = DAOFactory.getConn();
	public static void update() throws ClassNotFoundException, IOException, SQLException{
		File statFile = new File(FileDirection.getStatFileName());
		System.gc();
		if (statFile.exists()){
			StatStorage stat = (StatStorage) FileProcess.readFile(statFile.getAbsolutePath());
			LOGGER.info("DELETE FILE: " + statFile.delete());
			HashMap<String, Supplier> supplierList = stat.getSupplierList();
			for (String nameSupplier : supplierList.keySet()){
				if (!SupplierDAO.getSupplierList().containsKey(nameSupplier))
					SupplierDAO.insert(supplierList.get(nameSupplier).getCode(), nameSupplier);
			}
			PreparedStatement stm = conn.prepareStatement("UPDATE APP.Supplier"
					+ " SET code=?, is_main=?, lim=?, is_full=?, group_id=?, price_percent=? WHERE name=?");
			for (String nameSupplier : supplierList.keySet()){
				Supplier supplier = supplierList.get(nameSupplier);
				stm.setString(1, supplier.getCode());
				stm.setBoolean(2, supplier.isMain());
				Utility.setLim(stm, 3, supplier.getLim());
				stm.setBoolean(4, supplier.isFull());
				stm.setInt(5, supplier.getGroupID());
				stm.setDouble(6, supplier.getPricePercent());
				stm.setString(7, nameSupplier);
				stm.addBatch();
			}
			stm.executeBatch();
			
			HashMap<String, Staff> staffList = stat.getStaffList();
			Statement dropTable = conn.createStatement();
			dropTable.execute("DROP TABLE APP.Staff");
			dropTable.close();
			Statement createTable = conn.createStatement();
			createTable.execute("CREATE TABLE APP.Staff("
				+ "id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1) PRIMARY KEY,"
				+ " name VARCHAR(100) NOT NULL,"
				+ " position VARCHAR(30) NOT NULL,"
				+ " base_wage INT NOT NULL DEFAULT 0,"
				+ " sub_wage INT NOT NULL DEFAULT 0,"
				+ " base_eff INT NOT NULL DEFAULT 0,"
				+ " pob INT NOT NULL DEFAULT 0,"
				+ " insurance INT NOT NULL DEFAULT 0,"
				+ " imprest INT NOT NULL DEFAULT 0,"
				+ " day_off FLOAT NOT NULL DEFAULT 0,"
				+ " holiday INT NOT NULL DEFAULT 0)"
			);
			createTable.close();
			for (String nameStaff : staffList.keySet()){
				Staff staff = staffList.get(nameStaff);
				StaffDAO.insert(nameStaff, staff.getPosition());
			}
			
			Statement removeTax = conn.createStatement();
			removeTax.execute("DELETE FROM APP.Tax_Connection WHERE 1=1");
			removeTax.close();
			conn.commit();
			StaffDAO.write(staffList);
			BiMap<String, String> taxConnection = stat.getTaxConnection();
			for (Entry<String, String> entry : taxConnection.getLeftEntry()){
				String nameProduct = entry.getKey();
				String nameTax = entry.getValue();
				ProductDAO.insertTaxConnection(nameProduct, nameTax);
			}
			conn.commit();
			
			FileDirection.getData().setRevenue(stat.getTargetRevenue(), stat.getPercentRevenue());
			
			LOGGER.info("Finished update FileStat");
		}
	}
}
