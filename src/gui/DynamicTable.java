package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import factory.AttributesFactory;
import factory.FontFactory;
import factory.FormatFactory;
import utility.StringUtility;
import utility.Utility;

@SuppressWarnings("serial")
public class DynamicTable extends JTable{
	protected JTextField filterField;
	protected JLabel sumJL, countJL, averageJL;
	protected JComboBox<String> columnChooser;
	protected DynamicTableModel tableModel;
	protected TableRowSorter<DynamicTableModel> sorter;
	protected JButton export;
	protected HashMap<Integer, Color> coloredRows, coloredColumns;
	
	public DynamicTable(DynamicTableModel tableModel){
		super(tableModel);
		coloredRows = new HashMap<Integer, Color>();
		coloredColumns = new HashMap<Integer, Color>();
		this.tableModel = tableModel;
		this.getTableHeader().setFont(FontFactory.getTableHeader());
		this.getTableHeader().setPreferredSize(new Dimension(0, 30));
		this.setRowHeight(this.getRowHeight() + 5);
		sorter = new TableRowSorter<DynamicTableModel>(tableModel);
		this.setRowSorter(sorter);
		this.getTableHeader().setReorderingAllowed(false);
	}
	
	public void setRowColor(int row, Color c){
		coloredRows.put(row, c);
	}
	
	public void setColColor(int col, Color c){
		coloredColumns.put(col, c);
	}
	
	public void setUnsorted(final int col){
		sorter = new TableRowSorter<DynamicTableModel>(tableModel){
			public void toggleSortOrder(int column) {
				if (col == column){
			        List<? extends SortKey> sortKeys = getSortKeys();
			        if (sortKeys.size() > 0) {
			            if (sortKeys.get(0).getSortOrder() == SortOrder.DESCENDING) {
			                setSortKeys(null);
			                return;
			            }
			        }
				}
				super.toggleSortOrder(column);
		    }
		};
		this.setRowSorter(sorter);
	}
	
    @Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
	    Component component = super.prepareRenderer(renderer, row, column);
		int rowModel = convertRowIndexToModel(row);
		if (coloredColumns.size() == 0){
			if (!isRowSelected(rowModel) && !isRowSelected(row)){
				if (coloredRows.containsKey(rowModel))
					component.setBackground(coloredRows.get(rowModel));
				else
					component.setBackground(null);
			}
		}
		if (coloredRows.size() == 0){
			if (!isColumnSelected(column)){
				if (coloredColumns.containsKey(column))
					component.setBackground(coloredColumns.get(column));
				else
					component.setBackground(null);
			}
		}
	    int rendererWidth = component.getPreferredSize().width;
	    TableColumn tableColumn = getColumnModel().getColumn(column);
	    tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
	    return component;
	 };
	
	public void setDisplay(int column, int[] rows){
		int count = rows.length;
		int minus = 0;
		double sum = 0;
		for (int i = 0; i < count; i ++){
			int row = this.convertRowIndexToModel(rows[i]);
			Object val = tableModel.getValueAt(row, column);
			if (val == null) minus ++;
			else sum += Utility.getNumericValue(val);
		}
		count -= minus;
		if (count == 0) return;
		double average = sum / count;
		if (tableModel.getColumnClass(column) == Integer.class){
			sumJL.setText("Tổng: " + FormatFactory.formatInfo((long) sum));
			averageJL.setText("Trung bình: " + FormatFactory.formatInfo((long) average));
		}
		else {
			sumJL.setText("Tổng: " + FormatFactory.formatInfo(sum));
			averageJL.setText("Trung bình: " + FormatFactory.formatInfo(average));
		}
		countJL.setText("Đếm: " + FormatFactory.formatInfo(count));
	}
	
	public void resetDisplay(){
		sumJL.setText(null);
		averageJL.setText(null);
		countJL.setText(null);
	}
	
	private boolean accentInsensitiveFilter(Object val, String query){
		if (val == null) return false;
		String input;
		if (val instanceof Calendar){
			Calendar cal = (Calendar) val;
			input = FormatFactory.formatCalendar(cal);
		} else input = val.toString();
		input = StringUtility.removeAccent(input);
		Pattern pattern = Pattern.compile(query);
		Matcher matcher = pattern.matcher(input);
		return matcher.find();
	}
	
	protected void filter(final String query){
		final DynamicTable table = this;
		final int index = columnChooser.getSelectedIndex();
		RowFilter<DynamicTableModel, Integer> rowFilter = new RowFilter<DynamicTableModel, Integer>(){
			@Override
			public boolean include(Entry<? extends DynamicTableModel, ? extends Integer> entry) {
				if (index == 0){
					boolean res = false;
					for (int i = 0; i < table.getColumnCount(); i ++){
						Object val = entry.getValue(i);
						res |= accentInsensitiveFilter(val, query);
					}
					return res;
				} else{
					Object val = entry.getValue(index - 1);
					return accentInsensitiveFilter(val, query);
				}
			}
		};
		sorter.setRowFilter(rowFilter);
	}
	
	public void setDisplay(JLabel sumJL, JLabel countJL, JLabel averageJL){
		this.sumJL = sumJL;
		this.countJL = countJL;
		this.averageJL = averageJL;
		this.sumJL.setFont(FontFactory.getInfo());
		this.averageJL.setFont(FontFactory.getInfo());
		this.countJL.setFont(FontFactory.getInfo());
		@SuppressWarnings("unused")
		DynamicListSecListener listSecListener = new DynamicListSecListener(this);
	}
	
	public void setColumnChooser(JComboBox<String> columnChooser){
		this.columnChooser = columnChooser;
		this.columnChooser.addItem(AttributesFactory.ALL);
		for (int i = 0; i < tableModel.getColumnCount(); i ++){
			String columnName = StringUtility.removeTags(tableModel.getColumnName(i));
			this.columnChooser.addItem(columnName);
		}
		this.columnChooser.setSelectedIndex(0);
	}
	
	public void setFilterField(JTextField field){
		filterField = field;
		filterField.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent e){
				filter("(?i)" + StringUtility.removeAccent(filterField.getText()));
			}
		});
	}
}
