package transferObject;

import java.io.Serializable;
import java.util.HashMap;

import dao.ProductDAO;
import dao.StaffDAO;
import dao.SupplierDAO;
import misc.BiMap;

public class StatStorage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, Staff> staffList;
	private HashMap<String, Supplier> supplierList;
	private BiMap<String, String> taxConnection;
	private long targetRevenue;
	private double percentRevenue;
	
	public StatStorage(long targetRevenue, double percentRevenue){
		staffList = StaffDAO.getStaffList();
		supplierList = SupplierDAO.getSupplierList();
		taxConnection = ProductDAO.getTaxConnection();
		this.targetRevenue = targetRevenue;
		this.percentRevenue = percentRevenue;
	}
	
	public long getTargetRevenue() {
		return targetRevenue;
	}
	
	public double getPercentRevenue() {
		return percentRevenue;
	}
	
	public HashMap<String, Staff> getStaffList(){
		return staffList;
	}
	
	public HashMap<String, Supplier> getSupplierList(){
		return supplierList;
	}
	
	public BiMap<String, String> getTaxConnection(){
		return taxConnection;
	}
}