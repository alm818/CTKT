package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class DynamicTabComponent extends JPanel {
	private int MAX_LENGTH = 20;
	private DynamicTabbedPane pane;
	private JLabel label;
	public DynamicTabComponent(DynamicTabbedPane pane) {
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		this.pane = pane;
		this.setOpaque(false);
		label = new JLabel();
		add(label);
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		add(new TabButton());
		setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
	}
	public DynamicTabComponent(DynamicTabbedPane pane, int MAX_LENGTH){
		this(pane);
		this.MAX_LENGTH = MAX_LENGTH;
	}
	public void setLabel(String title){
		if (title.length() > MAX_LENGTH) title = title.substring(0, MAX_LENGTH - 2) + "...";
		label.setText(title);
	}
	private int getIndex(){
		return pane.indexOfTabComponent(DynamicTabComponent.this);
	}
	private boolean isSelectedIndex(){
		return pane.getSelectedIndex() == getIndex();
	}
	
	private class TabButton extends JButton implements ActionListener {
		
		public TabButton() {
			int size = 15;
			setPreferredSize(new Dimension(size, size));
			setToolTipText("Đóng cửa sổ này");
			setFocusable(false);
			setRolloverEnabled(true);
			addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e) {
			removeTab(getIndex());
		}
		
		private void removeTab(int i){
			if (isSelectedIndex() && i == pane.getTabCount() - 1)
				pane.setSelectedIndex(i - 1);
			pane.remove(i);
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			this.setBorderPainted(false);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setStroke(new BasicStroke(2.75f));
			if (getModel().isRollover()) {
				this.setOpaque(true);
				this.setBackground(Color.RED);
				g2.setColor(Color.WHITE);
			} else{
				g2.setColor(Color.BLACK);
				this.setOpaque(false);			
				this.setBackground(Color.BLUE);
			}			
			int delta = 5;
			g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
			g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
			g2.dispose();
		}
	}
}
