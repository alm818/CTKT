package view;

import java.awt.Color;
import java.sql.SQLException;
import java.util.Vector;

import dao.BillDAO;
import dao.ProductDAO;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import gui.DynamicTable;
import gui.DynamicTableModel;
import misc.Pair;
import transferObject.Bill;
import transferObject.Element;
import utility.Utility;

public class RSellView extends PostView{
	private static final String[] COLUMN_TITLE = { "Mã mặt hàng", "Tên mặt hàng", "BTL không KM", "BTL KM", "Tổng BTL", "Thành tiền", "Đơn giá"};
	private static final Class<?>[] COLUMN_CLASS = {String.class, String.class, Double.class, Double.class, Double.class, Long.class, Long.class};
	private static final Integer[] COLUMN_EDITABLE = {};
	private static final Color HIGHLIGHT = Color.RED;
	
	public RSellView(String title, String name, Object... objects) {
		super(title, name, objects);
	}

	@Override
	protected void setTable() {
		tableModel = new DynamicTableModel(Utility.getVector(COLUMN_TITLE));
		tableModel.setClassColumn(COLUMN_CLASS);
		tableModel.setEditableColumn(COLUMN_EDITABLE);
		table = new DynamicTable(tableModel);
		table.getColumnModel().getColumn(2).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(3).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(4).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRightBold()));
		table.getColumnModel().getColumn(5).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightItalic()));
		table.getColumnModel().getColumn(6).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightBold()));
	}

	@Override
	protected void getParameters(Object... objects) throws SQLException {
		String codeSell = (String) objects[0];
		String name = (String) objects[1];
		Bill bill = BillDAO.getBill(codeSell);
		Vector<Object[]> data = new Vector<Object[]>();
		for (String nameProduct : bill.getProductList()){
			if (name.equals(nameProduct)) table.setRowColor(data.size(), HIGHLIGHT);
			String codeProduct = ProductDAO.getProductList().get(nameProduct).getCode();
			String _codeProduct = codeProduct, _nameProduct = nameProduct;
			//GET-NEW
			if (ProductDAO.isOld(codeProduct, nameProduct)){
				Pair<String, String> pair = ProductDAO.getNew(codeProduct, nameProduct);
				_codeProduct = pair.getFirst();
				_nameProduct = pair.getSecond();
			}
			
			Element e = bill.getProduct(nameProduct);
			Object[] thisRow = { _codeProduct, _nameProduct, e.getQ(), e.getPQ(), e.getSumQ(), e.getCost(), bill.getP(nameProduct)};
			data.add(thisRow);
		}
		tableModel.setData(data);
	}
}
