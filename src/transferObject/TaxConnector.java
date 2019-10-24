package transferObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import misc.BiMap;

public class TaxConnector {
	private HashMap<String, ResSet> taxMap, productMap;
	private BiMap<String, String> trueMap, falseMap;
	private HashMap<Integer, HashSet<String>> customerMap;
	private Calendar from, to;
	
	public TaxConnector(HashMap<String, ResSet> taxMap, HashMap<String, ResSet> productMap, Calendar from, Calendar to){
		this.taxMap = taxMap;
		this.productMap = productMap;
		this.from = from;
		this.to = to;
		trueMap = new BiMap<String, String>();
		falseMap = new BiMap<String, String>();
		customerMap = new HashMap<Integer, HashSet<String>>();
	}
	
	public void addCustomer(int groupID, String nameCustomer){
		if (!customerMap.containsKey(groupID)) customerMap.put(groupID, new HashSet<String>());
		customerMap.get(groupID).add(nameCustomer);
	}
	
	public ArrayList<String> getCustomers(int groupID){
		return new ArrayList<String>(customerMap.get(groupID));
	}
	
	public Calendar getFrom(){
		return from;
	}
	
	public Calendar getTo(){
		return to;
	}
	
	public void addTrue(String productName, String taxName){
		trueMap.add(productName, taxName);
	}
	
	public void addFalse(String productName, String taxName){
		falseMap.add(productName, taxName);
	}
	
	public HashMap<String, ResSet> getTaxMap(){
		return taxMap;
	}
	
	public HashMap<String, ResSet> getProductMap(){
		return productMap;
	}
	
	public BiMap<String, String> getTrueMap(){
		return trueMap;
	}
	
	public BiMap<String, String> getFalseMap(){
		return falseMap;
	}
}
