package transferObject;

import java.io.Serializable;
import java.util.Calendar;

import factory.AttributesFactory;

public class DataStorage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String installAddress, taxAddress;
	private char[] password;
	private Calendar today;
	private Boolean isAdmin;
	private long targetRevenue;
	private double percentRevenue;
	
	public DataStorage(){
		this.taxAddress = null;
		this.installAddress = null;
		this.password = "admin".toCharArray();
		this.today = null;
		this.isAdmin = true;
		this.targetRevenue = AttributesFactory.DEFAULT_TARGET_REVENUE;
		this.percentRevenue = AttributesFactory.DEFAULT_PERCENT_REVENUE;
	}
	
	public long getTargetRevenue() {
		return targetRevenue;
	}
	
	public double getPercentRevenue() {
		return percentRevenue;
	}
	
	public void setRevenue(long targetRevenue, double percentRevenue) {
		this.targetRevenue = targetRevenue;
		this.percentRevenue = percentRevenue;
	}
	
	public boolean isInstalled(){
		return installAddress != null;
	}
	
	public void setInstallAddress(String installAddress){
		this.installAddress = installAddress;
	}
	
	public void setTaxAddress(String taxAddress){
		this.taxAddress = taxAddress;
	}
	
	public String getInstallAddress(){ 
		return installAddress; 
	}
	
	public String getTaxAddress(){
		return taxAddress;
	}
	
	public void setToday(Calendar today){
		this.today = today;
	}
	
	public Calendar getToday(){
		return today;
	}
	
	public char[] getPassword(){
		return password;
	}
	
	public void setPassword(char[] password){
		this.password = password;
	}
	
	public boolean isAdmin(){
		return isAdmin;
	}
	
	public void setAdmin(boolean isAdmin){
		this.isAdmin = isAdmin;
	}
}