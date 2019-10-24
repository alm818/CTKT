package controller;

import java.awt.BorderLayout;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import view.View;

public abstract class Controller {
	protected final static Logger LOGGER = Logger.getLogger(Controller.class.getName());
	protected JPanel contentPane;
	private View view;
	
	public Controller(View view){
		contentPane = new JPanel();
		this.view = view;
	}
	
	public JPanel getContentPane(){
		contentPane.setLayout(new BorderLayout(0, 5));
		contentPane.add(getControlPanel(), BorderLayout.NORTH);
		contentPane.add(view.getViewPanel(), BorderLayout.CENTER);
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return contentPane;
	}
	
	public void requestView(Object...objects){
		view.load(objects);
		contentPane.repaint();
	}
	
	protected abstract JPanel getControlPanel();
}
