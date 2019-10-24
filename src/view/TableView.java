package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import factory.FontFactory;
import gui.DynamicTable;
import gui.DynamicTableModel;

public abstract class TableView extends View{
	protected String title;
	protected DynamicTable table;
	protected DynamicTableModel tableModel;
	
	public TableView(String title){
		super();
		this.title = title;
		preparePanel();
	}
	
	protected void preparePanel(){
		setTable();
		table.getTableHeader().addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
		    	if (e.getClickCount() == 2){
		    		int col = table.columnAtPoint(e.getPoint());
		    		table.setColumnSelectionInterval(col, col);
			        table.setRowSelectionInterval(0, table.getRowCount() - 1);
		    	}
		    }
		});
		table.addMouseListener(new MouseAdapter(){
		    @Override
		    public void mouseClicked(MouseEvent e) {
		    	if (e.getClickCount() == 2)
		    		table.setColumnSelectionInterval(0, table.getColumnCount() - 1);
		    }
		});
		JLabel tableTitle = new JLabel(title);
		tableTitle.setFont(FontFactory.getTableTitle());
		JPanel centerJP = new JPanel(new BorderLayout(0, 10));
		JPanel labelJP = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		labelJP.add(tableTitle);
		centerJP.add(labelJP, BorderLayout.NORTH);
		centerJP.add(new JScrollPane(table), BorderLayout.CENTER);
		viewPanel.setLayout(new BorderLayout(0, 5));
		viewPanel.add(getSouthJP(), BorderLayout.SOUTH);
		viewPanel.add(centerJP, BorderLayout.CENTER);
		viewPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}
	
	protected abstract JPanel getSouthJP();
	protected abstract void setTable();
}
