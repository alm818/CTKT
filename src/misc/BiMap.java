package misc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class BiMap<L, R> implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected HashMap<L, R> LRMap;
	protected HashMap<R, L> RLMap;
	public BiMap(){
		LRMap = new HashMap<L, R>();
		RLMap = new HashMap<R, L>();
	}
	public Set<Entry<L, R>> getLeftEntry(){
		return LRMap.entrySet();
	}
	public Set<Entry<R, L>> getRightEntry(){
		return RLMap.entrySet();
	}
	public void add(L l, R r){
		LRMap.put(l, r);
		RLMap.put(r, l);
	}
	public void removeL(L l){
		if (!LRMap.containsKey(l)) return;
		R r = LRMap.get(l);
		LRMap.remove(l);
		RLMap.remove(r);
	}
	public void removeR(R r){
		if (!RLMap.containsKey(r)) return;
		L l = RLMap.get(r);
		LRMap.remove(l);
		RLMap.remove(r);
	}
	public R getR(L l){
		return LRMap.get(l);
	}
	public L getL(R r){
		return RLMap.get(r);
	}
	public boolean containsL(L l){
		return LRMap.containsKey(l);
	}
	public boolean containsR(R r){
		return RLMap.containsKey(r);
	}
}
