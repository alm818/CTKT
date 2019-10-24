package transferObject;

public class Element {
	public static final int NP = 1;
	public static final int P = 2;
	public static final int NPP = 3;
	public static Element add(Element e1, Element e2){
		return new Element(e1.q + e2.q, e1.pq + e2.pq, e1.cost + e2.cost);
	}
	
	private double q, pq;
	private int cost;
	private Integer id;
	
	public Element(){
		q = 0;
		pq = 0;
		cost = 0;
		id = null;
	}
	
	public Element(Integer id, double q, double pq, int cost){
		this.id = id;
		this.q = q;
		this.pq = pq;
		this.cost = cost;
	}
	
	public Element(double q, double pq, int cost){
		this(null, q, pq, cost);
	}
	
	public Integer getID(){
		return id;
	}
	
	public double getQ(){
		return q;
	}
	
	public double getPQ(){
		return pq;
	}
	
	public double getSumQ(){
		return q + pq;
	}
	
	public int getCost(){
		return cost;
	}
	
	public int getType(){
		if (pq == 0) return NP;
		else if (q == 0) return P;
		else return NPP;
	}
	
	public String toString(){
		return String.format("ID:%d\tQ: %.1f\tPQ: %.1f\tCost: %d\n", id, q, pq, cost);
	}
}
