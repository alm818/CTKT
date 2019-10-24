package gui;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class DynamicTableModel extends AbstractTableModel{
	protected Vector<Object[]> data;
	protected Vector<String> columnTitle;
	protected Class<?>[] columnClass;
	protected Boolean[] columnEditable;
	
	public DynamicTableModel(Vector<String> columnTitle){
		this.columnTitle = columnTitle;
		data = new Vector<Object[]>();
		columnClass = new Class<?>[columnTitle.size()];
		columnEditable = new Boolean[columnTitle.size()];
		for (int i = 0; i < columnTitle.size(); i ++)
			columnEditable[i] = false;
	}
	
	public DynamicTableModel(Vector<Object[]> data, Vector<String> columnTitle){
		this(columnTitle);
		this.data = data;
	}
	
	public Vector<Object[]> getData(){ 
		return data; 
	}
	
	public void setClassColumn(Class<?>... columnClass){
		this.columnClass = columnClass;
	}
	
	public void setEditableColumn(Integer... columnList){
		for (Integer i : columnList) setEditableColumn(i, true);
	}
	
	public void setEditableColumn(int column, boolean isEditable){
		columnEditable[column] = isEditable;
	}
	
	public void setValueAt(Object value, int row, int column){
		data.elementAt(row)[column] = value;
		fireTableCellUpdated(row, column);
	}
	
	public Class<?> getColumnClass(int column){
		return columnClass[column];
	}
	
	public String getColumnName(int column){
		return columnTitle.elementAt(column);
	}
	
	public boolean isCellEditable(int row, int column){
		return columnEditable[column];
	}
	
	@Override
	public int getColumnCount() {
		return columnTitle.size();
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		return data.elementAt(row)[column];
	}
	
	public void setData(Vector<Object[]> data){
		this.data = data;
		this.fireTableDataChanged();
	}
}
