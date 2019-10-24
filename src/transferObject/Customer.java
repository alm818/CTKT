package transferObject;

public class Customer {
	private String code, name;
	private Integer id;
	
	public Customer(int id, String code, String name){
		this.code = code;
		this.name = name;
		this.id = id;
	}
	
	public int getID(){
		return id;
	}
	
	public String getCode(){
		return code;
	}
	
	public String getName(){
		return name;
	}
	
	public String toString(){
		return String.format("SUPPLIER ID: %s, code: %s, name: %s", id, code, name);
	}
}
