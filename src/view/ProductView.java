package view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import dao.ProductDAO;
import dao.SupplierDAO;
import factory.AttributesFactory;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import frame.SingleFrame;
import gui.DynamicTable;
import gui.DynamicTableModel;
import misc.Pair;
import transferObject.Product;
import transferObject.Supplier;
import utility.Utility;

public class ProductView extends SearchTableView{
	private static final String TITLE = "THÔNG TIN CÁC MẶT HÀNG THEO NHÀ CUNG CẤP";
	private static final String[] COLUMN_TITLE = { "Mã mặt hàng", "Tên mặt hàng", "Tồn không KM", "Tồn KM", "Tồn thực tế", "Giá trị tồn"};
	private static final Class<?>[] COLUMN_CLASS = {String.class, String.class, Double.class, Double.class, Double.class, Long.class};
	private static final Integer[] COLUMN_EDITABLE = {};
	
	public ProductView() {
		super(TITLE);
	}
	
	@Override
	protected void setTable() {
		tableModel = new DynamicTableModel(Utility.getVector(COLUMN_TITLE));
		tableModel.setClassColumn(COLUMN_CLASS);
		tableModel.setEditableColumn(COLUMN_EDITABLE);
		getParameters(AttributesFactory.ALL);
		table = new DynamicTable(tableModel);
		table.addMouseListener(new MouseAdapter(){
		    @Override
		    public void mouseClicked(MouseEvent e) {
		    	if (e.getClickCount() == 2){
		    		int row = table.rowAtPoint(e.getPoint());
		    		int rowModel = table.convertRowIndexToModel(row);
		    		String _codeProduct = (String) tableModel.getValueAt(rowModel, 0);
		    		String _nameProduct = (String) tableModel.getValueAt(rowModel, 1);
		    		String nameProduct = _nameProduct;
					//NEW-CONVERSE
					if (ProductDAO.isNew(_codeProduct, _nameProduct)){
						Pair<String, String> pair = ProductDAO.converseNew(_codeProduct, _nameProduct);
						nameProduct = pair.getSecond();
					}
		    		String title = String.format("CHI TIẾT HÓA ĐƠN TỒN KHO - %s", _nameProduct);
		    		StatusView view = new StatusView(title, null, nameProduct);
		    		while (true){
		    			if (view.isDone()){
		    				@SuppressWarnings("unused")
							SingleFrame singleFrame = new SingleFrame(view);
		    				break;
		    			}
		    		}
		    	}
		    }
		});
		table.getColumnModel().getColumn(2).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(3).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(4).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRightBold()));
		table.getColumnModel().getColumn(5).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightItalic()));
	}
	@Override
	public void getParameters(Object... objects) {
		String nameSupplier = (String) objects[0];
		Vector<Object[]> data = new Vector<Object[]>();
		if (nameSupplier.equals(AttributesFactory.ALL)){
			for (Product product : ProductDAO.getProductList().values()){
				double q = product.getQ();
				double pq = product.getPQ();
				String codeProduct = product.getCode();
				String nameProduct = product.getName();
				//GET-NEW
				if (ProductDAO.isOld(codeProduct, nameProduct)){
					Pair<String, String> pair = ProductDAO.getNew(codeProduct, nameProduct);
					codeProduct = pair.getFirst();
					nameProduct = pair.getSecond();
				}
				Object[] thisRow = { codeProduct, nameProduct, q, pq, q + pq, product.getCost()};
				data.add(thisRow);
			}
		} else{
			Supplier supplier = SupplierDAO.getSupplierList().get(nameSupplier);
			for (String nameProduct : supplier.getProducts()){
				Product product = ProductDAO.getProductList().get(nameProduct);
				double q = product.getQ();
				double pq = product.getPQ();
				String codeProduct = product.getCode();
				//GET-NEW
				if (ProductDAO.isOld(codeProduct, nameProduct)){
					Pair<String, String> pair = ProductDAO.getNew(codeProduct, nameProduct);
					codeProduct = pair.getFirst();
					nameProduct = pair.getSecond();
				}
				Object[] thisRow = { codeProduct, nameProduct, q, pq, q + pq, product.getCost()};
				data.add(thisRow);
			}
		}
		tableModel.setData(data);
	}
}
