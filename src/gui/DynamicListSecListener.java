package gui;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class DynamicListSecListener implements ListSelectionListener{
	private DynamicTable table;
	public DynamicListSecListener(DynamicTable table){
		this.table = table;
		setListener();
	}
	private void setListener(){
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setCellSelectionEnabled(true);
		ListSelectionModel rowSelMod = table.getSelectionModel();
		rowSelMod.addListSelectionListener(this);
	}
	@Override
	public void valueChanged(ListSelectionEvent e) {
		int[] rows = table.getSelectedRows();
		int[] cols = table.getSelectedColumns();
		if (cols.length == 1){
			Class<?> colClass = table.getModel().getColumnClass(cols[0]);
			if (colClass == Long.class || colClass == Integer.class || colClass == Double.class) table.setDisplay(cols[0], rows);
			else table.resetDisplay();
		}else table.resetDisplay();
	}
}
