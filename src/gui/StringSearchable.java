package gui;

import java.util.ArrayList;
import java.util.HashSet;

import factory.ComparatorFactory;
import utility.StringUtility;

public class StringSearchable implements Searchable<String,String>{
	private HashSet<String> terms = new HashSet<String>();

	public StringSearchable(ArrayList<String> terms){
		this.terms.addAll(terms);
	}
	
	public boolean contains(String value){
		return terms.contains(value);
	}
	
	@Override
	public ArrayList<String> search(String value) {
		ArrayList<String> founds = new ArrayList<String>();
		String newValue = StringUtility.removeAccent(value).toLowerCase();
		for ( String s : terms ){
			if (StringUtility.removeAccent(s).toLowerCase().contains(newValue))
				founds.add(s);
		}
		founds.sort(ComparatorFactory.getNameComparator());
		return founds;
	}
}