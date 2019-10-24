package transferObject;

public class Result {
	private String productName, taxName;
	private int leven, lcs;
	private double jaccard, jaro;
	
	public Result(String taxName, String productName, int leven, double jaccard){
		this.taxName = taxName;
		this.productName = productName;
		this.leven = leven;
		this.jaccard = jaccard;
	}
	
	public Result(String taxName, String productName, int leven, double jaccard, int lcs, double jaro){
		this(taxName, productName, leven, jaccard);
		this.lcs = lcs;
		this.jaro = jaro;
	}
	
	public double getScore(){
		return jaccard * jaro * lcs / (leven + 1);
	}
	
	public double getJaro(){
		return jaro;
	}
	
	public int getLCS(){
		return lcs;
	}
	
	public String getProductName(){
		return productName;
	}
	
	public String getTaxName(){
		return taxName;
	}
	
	public int getLeven(){
		return leven;
	}
	
	public double getJaccard(){
		return jaccard;
	}
}
