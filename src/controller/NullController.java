package controller;

import javax.swing.JPanel;

import view.NullView;
import view.View;

public class NullController extends Controller{
	
	public NullController(NullView view){
		super(view);
		view.setController(this);
	}
	
	public NullController(View view) {
		super(view);
	}

	@Override
	protected JPanel getControlPanel() {
		this.requestView();
		return new JPanel();
	}

}
