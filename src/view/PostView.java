package view;

public abstract class PostView extends SearchTableView{
	private String name;
	
	public PostView(String title, String name, Object...objects){
		super(title);
		load(objects);
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public String getTitle(){
		return title;
	}
	
	public int getRowHeight(){
		return table.getRowHeight();
	}
	
	public int getRowCount(){
		return table.getRowCount();
	}
}
