package transferObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import dao.ProductDAO;

public class Bill {
	private String codeBill, target;
	private Calendar day;
	private HashMap<String, Element> productMap;
	
	public Bill(Calendar day, String codeBill, String target){
		this.day = day;
		this.codeBill = codeBill;
		this.target = target;
		productMap = new HashMap<String, Element>();
	}
	
	public boolean hasProduct(String nameProduct){
		return productMap.containsKey(nameProduct);
	}
	
	public Element getProduct(String nameProduct){
		return productMap.get(nameProduct);
	}
	
	public void addElement(String nameProduct, double q, double pq, int cost){
		if (!productMap.containsKey(nameProduct))
			productMap.put(nameProduct, new Element());
		productMap.put(nameProduct, Element.add(productMap.get(nameProduct), new Element(q, pq, cost)));
	}
	
	public void addElement(String nameProduct, int id, double q, double pq, int cost){
		productMap.put(nameProduct, new Element(id, q, pq, cost));
	}
	
	public int getP(String nameProduct){
		assert (productMap.containsKey(nameProduct));
		Element e = productMap.get(nameProduct);
		if (e.getType() == Element.P)
			return 0;
		if (ProductDAO.isSplit(nameProduct))
			return (int) (e.getCost() / e.getQ());
		else return (int) (e.getCost() / (e.getQ() + e.getPQ()));
	}
	
	public double getQ(String nameProduct, double eq, double epq){
		assert (productMap.containsKey(nameProduct));
		Element e = productMap.get(nameProduct);
		if (ProductDAO.isSplit(nameProduct))
			return eq;
		else{
			if (e.getType() == Element.NPP)
				return eq + epq;
			else return eq;
		}
	}
	
	public double getQ(String nameProduct){
		assert (productMap.containsKey(nameProduct));
		Element e = productMap.get(nameProduct);
		return getQ(nameProduct, e.getQ(), e.getPQ());
	}
	
	public double getPQ(String nameProduct, double epq){
		assert (productMap.containsKey(nameProduct));
		Element e = productMap.get(nameProduct);
		if (ProductDAO.isSplit(nameProduct))
			return epq;
		else{
			if (e.getType() == Element.NPP)
				return 0;
			else return epq;
		}
	}
	
	public double getPQ(String nameProduct){
		assert (productMap.containsKey(nameProduct));
		Element e = productMap.get(nameProduct);
		return getPQ(nameProduct, e.getPQ());
	}
	
	public String getCodeBill(){
		return codeBill;
	}
	
	public String getTarget(){
		return target;
	}
	
	public Calendar getDay(){
		return day;
	}
	
	public Set<String> getProductList(){
		return productMap.keySet();
	}
	
	public String toString(){
		String s = String.format("codeBill: %s\ttarget: %s\n", codeBill, target);
		for (String nameProduct : productMap.keySet()){
			s += "nameProduct: " + nameProduct + "\n";
			s += "\t" + productMap.get(nameProduct);
		}
		s += "\n";
		return s;
	}
}
