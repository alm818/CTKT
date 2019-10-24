package transferObject;

import java.io.Serializable;
import java.util.HashSet;

import dao.SupplierDAO;
import factory.AttributesFactory;

public class Supplier implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id, groupID;
	private String code, name;
	private boolean isMain = false, isFull;
	private Double lim = null, pricePercent;
	private HashSet<String> rawProducts, products;
	
	public Supplier(int id, String code, String name, boolean isMain, Double lim, HashSet<String> products, boolean isFull, int groupID, double pricePercent){
		this.code = code;
		this.name = name;
		this.id = id;
		this.isMain = isMain;
		this.lim = lim;
		this.rawProducts = products;
		this.isFull = isFull;
		this.groupID = groupID;
		this.pricePercent = pricePercent;
	}
	
	public Supplier(String code, String name){
		this.code = code;
		this.name = name;
	}
	
	public boolean isFull(){
		return isFull;
	}
	
	public int getGroupID(){
		return groupID;
	}
	
	public double getPricePercent(){
		return pricePercent;
	}
	
	public void setFull(boolean isFull){
		this.isFull = isFull;
	}
	
	public void setGroupID(int groupID){
		this.groupID = groupID;
	}
	
	public void setPricePercent(double pricePercent){
		this.pricePercent = pricePercent;
	}
	
	public HashSet<String> getProducts(){
		if (name.equals(AttributesFactory.DKSH)){
			if (products == null){
				HashSet<String> minus = SupplierDAO.getSupplierList().get(AttributesFactory.JOHNSON).rawProducts;
				products = new HashSet<String>(rawProducts);
				products.removeAll(minus);
			}
			return products;
		}
		return rawProducts;
	}
	
	public HashSet<String> getRawProducts(){
		return rawProducts;
	}
	
	public int getID(){
		return id;
	}
	
	public void setCode(String code){
		this.code = code;
	}
	
	public String getCode(){
		return code;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean isMain(){
		return isMain;
	}
	
	public Double getLim(){
		return lim;
	}
	
	public void setMain(boolean isMain){
		this.isMain = isMain;
	}
	
	public void setLim(Double lim){
		this.lim = lim;
	}
	
	public String toString(){
		return String.format("SUPPLIER ID: %s, code: %s, name: %s, main: %s, lim: %s", id, code, name, isMain, lim);
	}
}
