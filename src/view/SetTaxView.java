package view;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import dao.SupplierDAO;
import factory.DAOFactory;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import gui.DynamicTable;
import gui.DynamicTableModel;
import transferObject.Supplier;
import utility.Utility;

public class SetTaxView extends TableView{
	private static final String TITLE = "TÙY CHỈNH THUẾ THEO NHÀ CUNG CẤP";
	private static final String[] COLUMN_TITLE = { "Mã nhà cung cấp", "Tên nhà cung cấp", "100% Thuế", "Nhóm", "% giá mua"};
	private static final Class<?>[] COLUMN_CLASS = {String.class, String.class, Boolean.class, Integer.class, Double.class};
	private static final Integer[] COLUMN_EDITABLE = {2, 3, 4};

	public SetTaxView() {
		super(TITLE);
	}

	@Override
	protected JPanel getSouthJP() {
		JButton saveJB = new JButton("Lưu");
		saveJB.setEnabled(false);
		saveJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Vector<Object[]> data = tableModel.getData();
				for (Object[] thisRow : data){
					String nameSupplier = (String) thisRow[1];
					boolean isFull = (boolean) thisRow[2];
					int groupID = (int) thisRow[3];
					double pricePercent = (double) thisRow[4];
					try {
						SupplierDAO.setTax(nameSupplier, isFull, groupID, pricePercent);
					} catch (SQLException e) {
						LOGGER.severe(e.getMessage());
						e.printStackTrace();
					}
				}
				saveJB.setEnabled(false);
				try {
					DAOFactory.getConn().commit();
					SupplierDAO.updateSupplierList();
				} catch (SQLException e) {
					LOGGER.severe(e.getMessage());
					e.printStackTrace();
				}
			}
		});
		tableModel.addTableModelListener(new TableModelListener(){
			@Override
			public void tableChanged(TableModelEvent arg0) {
				saveJB.setEnabled(true);
			}
		});
		saveJB.setSize(80, 60);
		JPanel saveJP = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		saveJP.add(saveJB);
		return saveJP;
	}

	@Override
	protected void setTable() {
		tableModel = new DynamicTableModel(Utility.getVector(COLUMN_TITLE));
		tableModel.setClassColumn(COLUMN_CLASS);
		tableModel.setEditableColumn(COLUMN_EDITABLE);
		table = new DynamicTable(tableModel);
		table.getColumnModel().getColumn(4).setCellRenderer(GUICellRendererFactory.getPerRenderer(GUICellStyleFactory.getRight()));
		table.getTableHeader().setResizingAllowed(false);
	}

	@Override
	protected void getParameters(Object... objects) throws SQLException {
		Vector<Object[]> data = new Vector<Object[]>();
		for (Supplier supplier : SupplierDAO.getSupplierList().values()){
			Object[] thisRow = { supplier.getCode(), supplier.getName(), supplier.isFull(), supplier.getGroupID(), supplier.getPricePercent() };
			data.add(thisRow);
		}
		tableModel.setData(data);
	}

}
