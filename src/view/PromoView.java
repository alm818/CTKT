package view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

import dao.BillDAO;
import dao.CustomerDAO;
import dao.ProductDAO;
import dao.SellDAO;
import dao.SupplierDAO;
import factory.AttributesFactory;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import factory.FormatFactory;
import frame.SingleFrame;
import gui.DynamicTable;
import gui.DynamicTableModel;
import misc.Pair;
import transferObject.Bill;
import transferObject.Product;
import utility.Utility;

public class PromoView extends SearchTableView{
	private static final String TITLE = "CHI TIẾT HÓA ĐƠN XUẤT HÀNG KHUYẾN MÃI LẺ THEO NHÀ CUNG CẤP";
	private static final String[] COLUMN_TITLE = { "Ngày hóa đơn", "Mã hóa đơn", "Mã khách hàng", "Tên khách hàng", "Mã mặt hàng", "Tên mặt hàng", "Số lượng", "Đơn giá gốc", "Thành tiền" };
	private static final Class<?>[] COLUMN_CLASS = {Calendar.class, String.class, String.class, String.class, String.class, String.class, Double.class, Long.class, Long.class};
	private static final Integer[] COLUMN_EDITABLE = {};
	
	public PromoView() {
		super(TITLE);
	}

	@Override
	protected void setTable() {
		tableModel = new DynamicTableModel(Utility.getVector(COLUMN_TITLE));
		tableModel.setClassColumn(COLUMN_CLASS);
		tableModel.setEditableColumn(COLUMN_EDITABLE);
		table = new DynamicTable(tableModel);
		table.addMouseListener(new MouseAdapter(){
		    @Override
		    public void mouseClicked(MouseEvent e) {
		    	if (e.getClickCount() == 2){
		    		int row = table.rowAtPoint(e.getPoint());
		    		int rowModel = table.convertRowIndexToModel(row);
		    		String codeSell = (String) tableModel.getValueAt(rowModel, 1);
		    		String _codeProduct = (String) tableModel.getValueAt(rowModel, 4);
		    		String _nameProduct = (String) tableModel.getValueAt(rowModel, 5);
		    		String nameProduct = _nameProduct;
					//NEW-CONVERSE
					if (ProductDAO.isNew(_codeProduct, _nameProduct)){
						Pair<String, String> pair = ProductDAO.converseNew(_codeProduct, _nameProduct);
						nameProduct = pair.getSecond();
					}
		    		Bill bill = null;
					try {
						bill = BillDAO.getBill(codeSell);
					} catch (SQLException err) {
						LOGGER.severe(err.getMessage());
						err.printStackTrace();
					}
		    		String nameCustomer = CustomerDAO.getCustomerList().get(bill.getTarget()).getName();
		    		String day = FormatFactory.formatCalendar(bill.getDay());
		    		String sellTitle = String.format("CHI TIẾT HÓA ĐƠN %s VÀO NGÀY %s - %s", codeSell, day, nameCustomer);
		    		SellView sellView = new SellView(sellTitle, null, codeSell, nameProduct);
		    		while (true){
		    			if (sellView.isDone()){
		    				String title = String.format("%s - %s - %s - %s", day, codeSell, nameCustomer, _nameProduct);
		    				@SuppressWarnings("unused")
							SingleFrame frame = new SingleFrame(title, sellView);
		    				break;
		    			}
		    		}
		    	}
		    }
		});
		table.getColumnModel().getColumn(0).setCellRenderer(GUICellRendererFactory.getCalendarRenderer(GUICellStyleFactory.getCenter()));
		table.getColumnModel().getColumn(6).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRightBold()));
		table.getColumnModel().getColumn(7).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(8).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRight()));
	}

	@Override
	protected void getParameters(Object... objects) throws SQLException {
		String nameSupplier = (String) objects[0];
		Calendar from = (Calendar) objects[1];
		Calendar to = (Calendar) objects[2];
		Vector<Object[]> data = new Vector<Object[]>();
		ArrayList<String> nameList;
		if (nameSupplier.equals(AttributesFactory.ALL))
			nameList = new ArrayList<String>(ProductDAO.getProductList().keySet());
		else nameList = new ArrayList<String>(SupplierDAO.getSupplierList().get(nameSupplier).getProducts());
		for (String nameProduct : nameList){
			ArrayList<Bill> promoBills = SellDAO.getPromo(nameProduct, from, to);
			Product product = ProductDAO.getProductList().get(nameProduct);
			for (Bill bill : promoBills){
				String codeProduct = ProductDAO.getProductList().get(nameProduct).getCode();
				String _codeProduct = codeProduct, _nameProduct = nameProduct;
				
				//GET-NEW
				if (ProductDAO.isOld(codeProduct, nameProduct)){
					Pair<String, String> pair = ProductDAO.getNew(codeProduct, nameProduct);
					_codeProduct = pair.getFirst();
					_nameProduct = pair.getSecond();
				}
				
				String codeCustomer = bill.getTarget();
				String nameCustomer = CustomerDAO.getCustomerList().get(codeCustomer).getName();
				long totalPrice = (long) (product.getLastP() * bill.getPQ(nameProduct));
				Object[] thisRow = { bill.getDay(), bill.getCodeBill(), codeCustomer, nameCustomer, _codeProduct, _nameProduct, bill.getPQ(nameProduct), product.getLastP(), totalPrice};
				data.add(thisRow);
			}
		}
		tableModel.setData(data);
	}
}
