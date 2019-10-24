package view;

import controller.Controller;

public abstract class NullView extends TableView{
	protected Controller controller;
	
	public NullView(String title) {
		super(title);
	}
	
	public void setController(Controller controller){
		this.controller = controller;
	}
}
