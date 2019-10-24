package gui;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class DynamicTabbedPane extends JTabbedPane{
	private DynamicTabComponent component;
	private Integer MAX_LENGTH;
	
	public DynamicTabbedPane(int MAX_LENGTH){
		super(JTabbedPane.TOP);
		setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		this.MAX_LENGTH = MAX_LENGTH;
	}
	
	public void addComponent(String title, JPanel panel){
		int index = this.getTabCount();
		this.addTab(title, panel);
		if (MAX_LENGTH != null) component = new DynamicTabComponent(this, MAX_LENGTH);
		else component = new DynamicTabComponent(this);
		component.setLabel(title);
		this.setTabComponentAt(index, component);
		this.setToolTipTextAt(index, title);
		this.setSelectedIndex(index);
	}
}
