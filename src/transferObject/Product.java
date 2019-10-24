package transferObject;

import java.util.ArrayList;

public class Product {
	private Integer id, init_p, last_p;
	private String code, name, last_code;
	private ArrayList<Status> statuses;
	private Double init_q, init_pq;
	
	public Product(int id, String code, String name, double init_q, double init_pq, Integer init_p, Integer last_p, String last_code, ArrayList<Status> statuses){
		this.id = id;
		this.code = code;
		this.name = name;
		this.init_q = init_q;
		this.init_pq = init_pq;
		this.init_p = init_p;
		this.last_p = last_p;
		this.last_code = last_code;
		this.statuses = statuses;
	}
	
	public double getInitQ(){
		return init_q;
	}
	
	public double getInitPQ(){
		return init_pq;
	}
	
	public Integer getInitP(){
		return init_p;
	}
	
	public Integer getLastP(){
		return last_p;
	}
	
	public String getLastCode(){
		return last_code;
	}
	
	public void setLast(int last_p, String last_code){
		this.last_p = last_p;
		this.last_code = last_code;
	}
	
	public Integer getID(){
		return id;
	}
	
	public double getPQ(){
		double res = 0;
		for (Status status : statuses)
			res += status.getPQ(name);
		return res;
	}
	
	public double getQ(){
		double res = 0;
		for (Status status : statuses)
			res += status.getQ(name);
		return res;
	}
	
	public long getCost(){
		long res = 0;
		for (Status status : statuses)
			res += status.getCost(name);
		return res;
	}
	
	public String getCode(){
		return code;
	}
	
	public String getName(){
		return name;
	}
	
	public ArrayList<Status> getStatuses(){
		return statuses;
	}
	
	public String toString(){
		String s = String.format("PRODUCT code: %s, name: %s", code, name) + "\nStatuses:\n";
		for (Status status : statuses)
			s += "\t" + status.getBill().getCodeBill() + "\t" + status.getBill().getProduct(name) + "\t\t" + status.getQ() + "\t\t" + status.getPQ() + '\n';
		return s;
	}
}
