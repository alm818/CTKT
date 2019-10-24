package transferObject;

public class RStatus {
	private Bill bill;
	private double rq;
	
	public RStatus(Bill bill, double rq){
		this.bill = bill;
		this.rq = rq;
	}
	
	public double getRQ(){
		return rq;
	}
	
	public void setRQ(double rq){
		this.rq = rq;
	}
	
	public Bill getBill(){
		return bill;
	}
}
