package view;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dao.ProductDAO;
import dao.SupplierDAO;
import factory.AttributesFactory;
import gui.DynamicTable;
import gui.DynamicTableModel;
import misc.Pair;
import transferObject.Supplier;
import utility.Utility;

public class JohnsonView extends NullView {
	private static final String TITLE = "DANH SÁCH MẶT HÀNG JOHNSON";
	private static final String[] COLUMN_TITLE = { "Mã hàng", "Tên hàng" };
	private static final Class<?>[] COLUMN_CLASS = { String.class, String.class };
	private static final Integer[] COLUMN_EDITABLE = {};
	
	public JohnsonView() {
		super(TITLE);
	}

	@Override
	protected JPanel getSouthJP() {
		JButton addJB = new JButton("Thêm");
		addJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String _code = JOptionPane.showInputDialog(null, "Nhập mã hàng Johnson:", "Thêm hàng Johnson", JOptionPane.QUESTION_MESSAGE);
				String oldCode = _code;
				if (_code == null)
					return;
				else if (_code.length() == 0)
					JOptionPane.showMessageDialog(null, "Mã hàng không thể bỏ trống", "Lỗi khi thêm mã hàng", JOptionPane.ERROR_MESSAGE);
				else {
					String _name;
					if (ProductDAO.isNewCode(_code))
						_name = ProductDAO.getNewName(_code);
					else
						_name = ProductDAO.getProductName(_code);
					if (_name == null){
						JOptionPane.showMessageDialog(null, "Mã hàng chưa tồn tại", "Lỗi khi thêm mã hàng", JOptionPane.ERROR_MESSAGE);
						return;
					}
					//NEW-CONVERSE
					if (ProductDAO.isNew(_code, _name)){
						Pair<String, String> pair = ProductDAO.converseNew(_code, _name);
						_code = pair.getFirst();
						_name = pair.getSecond();
					}
					Supplier supplier = SupplierDAO.getSupplierList().get(AttributesFactory.JOHNSON);
					if (supplier.getProducts().contains(_name)){
						JOptionPane.showMessageDialog(null, "Mã hàng đã được thêm từ trước", "Lỗi khi thêm mã hàng", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						SupplierDAO.insertProduct(AttributesFactory.JOHNSON, _name);
						SupplierDAO.updateSupplierList();
					} catch (SQLException err) {
						err.printStackTrace();
					}
					controller.requestView();
					JOptionPane.showMessageDialog(null, "Mã hàng " + oldCode + " đã được thêm", "Hoàn thành", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
		JButton deleteJB = new JButton("Xóa");
		deleteJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (table.getSelectedRowCount() == 1){
					int row = table.convertRowIndexToModel(table.getSelectedRow());
					String _code = (String) tableModel.getValueAt(row, 0);
					String _name = (String) tableModel.getValueAt(row, 1);
					String oldName = _name;
					int confirm = JOptionPane.showConfirmDialog(null, "Có chắc chắn xóa mặt hàng " + _name, "Xóa mặt hàng", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (confirm == JOptionPane.YES_OPTION){
						try {
							//NEW-CONVERSE
							if (ProductDAO.isNew(_code, _name)){
								Pair<String, String> pair = ProductDAO.converseNew(_code, _name);
								_name = pair.getSecond();
							}
							SupplierDAO.deleteProduct(AttributesFactory.JOHNSON, _name);
							SupplierDAO.updateSupplierList();
						} catch (SQLException err) {
							err.printStackTrace();
						}
						controller.requestView();
						JOptionPane.showMessageDialog(null, "Đã xóa mặt hàng " + oldName, "Hoàn thành", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});
		
		JPanel southJP = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
		southJP.add(addJB);
		southJP.add(deleteJB);
		return southJP;
	}

	@Override
	protected void setTable() {
		tableModel = new DynamicTableModel(Utility.getVector(COLUMN_TITLE));
		tableModel.setClassColumn(COLUMN_CLASS);
		tableModel.setEditableColumn(COLUMN_EDITABLE);
		table = new DynamicTable(tableModel);
	}

	@Override
	protected void getParameters(Object... objects) throws SQLException {
		Vector<Object[]> data = new Vector<Object[]>();
		Supplier supplier = SupplierDAO.getSupplierList().get(AttributesFactory.JOHNSON);
		for (String name : supplier.getProducts()){
			String code = ProductDAO.getProductList().get(name).getCode();
			//GET-NEW
			if (ProductDAO.isOld(code, name)){
				Pair<String, String> pair = ProductDAO.getNew(code, name);
				code = pair.getFirst();
				name = pair.getSecond();
			}
			data.add(new Object[]{ code, name });
		}
		tableModel.setData(data);
	}
}
