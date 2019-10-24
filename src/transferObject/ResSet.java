package transferObject;

import java.util.HashMap;

public class ResSet {
	private HashMap<String, Object> resMap;
	
	public ResSet(){
		resMap = new HashMap<String, Object>();
	}
	
	public void put(String att, Object val){
		resMap.put(att.toLowerCase(), val);
	}
	
	public Object get(String att){
		// FOR double value, just cast in primitive type
		// FOR long and int value, cast primitive with Utility.getNumericValue()
		if (!resMap.containsKey(att.toLowerCase())) return 0.0;
		return resMap.get(att.toLowerCase());
	}
	
	public boolean contains(String att){
		return resMap.containsKey(att.toLowerCase());
	}
	
	public String toString(){
		String s = "";
		for (String att : resMap.keySet())
			s += att + "\t" + resMap.get(att) + "\n";
		return s;
	}
}
