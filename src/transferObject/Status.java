package transferObject;

import java.util.ArrayList;

import dao.ProductDAO;

public class Status {
	public static Integer getNPIndex(String nameProduct, ArrayList<Status> statuses){
		for (int i = statuses.size() - 1; i >= 0; i --){
			Element e = statuses.get(i).getBill().getProduct(nameProduct);
			if (e != null && e.getType() != Element.P)
				return i;
		}

		return null;
	}
	
	public static Integer getPIndex(String nameProduct, ArrayList<Status> statuses){
		if (ProductDAO.isSplit(nameProduct)){
			for (int i = statuses.size() - 1; i >= 0; i --){
				Element e = statuses.get(i).getBill().getProduct(nameProduct);
				if (e != null && e.getType() != Element.NP)
					return i;
			}
		} else{
			for (int i = statuses.size() - 1; i >= 0; i --) {
				Element e = statuses.get(i).getBill().getProduct(nameProduct);
				if (e != null && e.getType() == Element.P)
					return i;
			}
		}
		return null;
	}
	
	private Bill bill;
	private double q, pq;
	
	public Status(Bill bill, double q, double pq){
		this.bill = bill;
		this.q = q;
		this.pq = pq;
	}
	
	public Bill getBill(){
		return bill;
	}
	
	public boolean isTrivial(){
		return (q == 0 && pq == 0);
	}
	
	//MACHINE-READABLE
	public double getQ(){
		return q;
	}
	
	//MACHINE-READABLE
	public double getPQ(){
		return pq;
	}
	
	public double getSumQ(){
		return q + pq;
	}
	
	//HUMAN-READABLE
	public double getQ(String nameProduct){
		return bill.getQ(nameProduct, q, pq);
	}
	
	//HUMAN-READABLE
	public double getPQ(String nameProduct){
		return bill.getPQ(nameProduct, pq);
	}
	
	//HUMAN-READABLE
	public int getP(String nameProduct){
		return bill.getP(nameProduct);
	}
	
	public int getCost(String nameProduct){
		return (int) (getQ(nameProduct) * bill.getP(nameProduct));
	}
	
	public void setQ(double q){
		this.q = q;
	}
	
	public void setPQ(double pq){
		this.pq = pq;
	}
	
	public String toString(){
		return String.format("Q left: %.1f\tPQ left: %.1f\n", q, pq) + bill;
	}
	
	public String toString(String nameProduct){
		return String.format("Q left: %.1f\tPQ left: %.1f\n%s\n", q, pq, bill.getCodeBill()) + bill.getProduct(nameProduct);
	}
}
