package view;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Vector;

import dao.BillDAO;
import dao.CustomerDAO;
import dao.ProductDAO;
import dao.RSellDAO;
import dao.SupplierDAO;
import factory.AttributesFactory;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import factory.FormatFactory;
import frame.MultipleFrame;
import gui.DynamicTable;
import gui.DynamicTableModel;
import misc.Pair;
import transferObject.Bill;
import transferObject.ResSet;
import utility.Utility;

public class ReturnView extends SearchTableView{
	private static final String TITLE = "CHI TIẾT HÓA ĐƠN BÁN TRẢ LẠI THEO NHÀ CUNG CẤP";
	private static final String[] COLUMN_TITLE = { 
			"<html><center>Ngày<br>hóa đơn</center></br></html>", 
			"<html><center>Mã<br>hóa đơn</center></br></html>", 
			"Tên khách hàng", "Mã mặt hàng", "Tên mặt hàng", "Số lượng", 
			"<html><center>Đơn giá<br>bán lúc đầu</center></br></html>", 
			"<html><center>Đơn giá<br>mua lúc sau</center></br></html>", 
			"<html><center>Đơn giá<br>chênh lệch</center></br></html>", 
			"<html><center>Thành tiền<br>chênh lệch</center></br></html>"};
	private static final Class<?>[] COLUMN_CLASS = {Calendar.class, String.class, String.class, String.class, String.class, Double.class, Long.class, Long.class, Long.class, Long.class};
	private static final Integer[] COLUMN_EDITABLE = {};
	
	public ReturnView() {
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
		    		String codeRSell = (String) tableModel.getValueAt(rowModel, 1);
		    		String _codeProduct = (String) tableModel.getValueAt(rowModel, 3);
		    		String _nameProduct = (String) tableModel.getValueAt(rowModel, 4);
		    		String nameProduct = _nameProduct;
					//NEW-CONVERSE
					if (ProductDAO.isNew(_codeProduct, _nameProduct)){
						Pair<String, String> pair = ProductDAO.converseNew(_codeProduct, _nameProduct);
						nameProduct = pair.getSecond();
					}
		    		Bill bill = null;
					try {
						bill = BillDAO.getBill(codeRSell);
					} catch (SQLException err) {
						LOGGER.severe(err.getMessage());
						err.printStackTrace();
					}
		    		String nameCustomer = CustomerDAO.getCustomerList().get(bill.getTarget()).getName();
		    		String day = FormatFactory.formatCalendar(bill.getDay());
		    		String rSellTitle = String.format("CHI TIẾT HÓA ĐƠN %s VÀO NGÀY %s - %s", codeRSell, day, nameCustomer);
		    		String rSellName = String.format("Hóa đơn %s", codeRSell);
		    		SellView rSellView = new SellView(rSellTitle, rSellName, codeRSell, nameProduct);
		    		String historyTitle = String.format("CHI TIẾT HÓA ĐƠN BÁN - %s", _nameProduct);
		    		String historyName = "Hóa đơn bán hàng";
		    		RHistoryView historyView = new RHistoryView(historyTitle, historyName, codeRSell, nameProduct);
		    		while (true){
		    			if (rSellView.isDone() && historyView.isDone()){
		    				String title = String.format("%s - %s - %s - %s", day, codeRSell, nameCustomer, _nameProduct);
							@SuppressWarnings("unused")
							MultipleFrame frame = new MultipleFrame(title, rSellView, historyView);
		    				break;
		    			}
		    		}
		    	}
		    }
		});
		table.getTableHeader().setPreferredSize(new Dimension(table.getColumnModel().getTotalColumnWidth(), 40));
		table.getColumnModel().getColumn(0).setCellRenderer(GUICellRendererFactory.getCalendarRenderer(GUICellStyleFactory.getCenter()));
		table.getColumnModel().getColumn(5).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(6).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(7).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(8).setCellRenderer(GUICellRendererFactory.getSignedPriceRenderer(GUICellStyleFactory.getRightBold()));
		table.getColumnModel().getColumn(9).setCellRenderer(GUICellRendererFactory.getSignedPriceRenderer(GUICellStyleFactory.getRightItalic()));
	}

	@Override
	protected void getParameters(Object... objects) throws SQLException {
		String nameSupplier = (String) objects[0];
		Calendar from = (Calendar) objects[1];
		Calendar to = (Calendar) objects[2];
		ArrayList<Bill> billList = RSellDAO.getBills(from, to);
		Vector<Object[]> data = new Vector<Object[]>();
		if (nameSupplier.equals(AttributesFactory.ALL)){
			for (Bill bill : billList)
				for (String nameProduct : bill.getProductList()){
					ResSet resSet = RSellDAO.getProfit(bill.getProduct(nameProduct).getID());
					if (resSet == null) continue;
					String nameCustomer = CustomerDAO.getCustomerList().get(bill.getTarget()).getName();
					String codeProduct = ProductDAO.getProductList().get(nameProduct).getCode();
					
					//GET-NEW
					if (ProductDAO.isOld(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.getNew(codeProduct, nameProduct);
						codeProduct = pair.getFirst();
						nameProduct = pair.getSecond();
					}
					
					Double q = (Double) resSet.get("q");
					Integer sp = (Integer) resSet.get("sp");
					Integer bp = (Integer) resSet.get("bp");
					Integer dif = sp - bp;
					Object[] row = {
							bill.getDay(), bill.getCodeBill(), nameCustomer,
							codeProduct, nameProduct, q, sp, bp, dif, (int) (dif * q)
					};
					data.add(row);
				}
		} else{
			HashSet<String> products = SupplierDAO.getSupplierList().get(nameSupplier).getProducts();
			for (Bill bill : billList)
				for (String nameProduct : bill.getProductList()){
					if (!products.contains(nameProduct)) continue;
					ResSet resSet = RSellDAO.getProfit(bill.getProduct(nameProduct).getID());
					if (resSet == null) continue;
					String nameCustomer = CustomerDAO.getCustomerList().get(bill.getTarget()).getName();
					String codeProduct = ProductDAO.getProductList().get(nameProduct).getCode();
					
					//GET-NEW
					if (ProductDAO.isOld(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.getNew(codeProduct, nameProduct);
						codeProduct = pair.getFirst();
						nameProduct = pair.getSecond();
					}
					
					Double q = (Double) resSet.get("q");
					Integer sp = (Integer) resSet.get("sp");
					Integer bp = (Integer) resSet.get("bp");
					Integer dif = sp - bp;
					Object[] row = {
							bill.getDay(), bill.getCodeBill(), nameCustomer,
							codeProduct, nameProduct, q, sp, bp, dif, (int) (dif * q)
					};
					data.add(row);
				}
		}
		tableModel.setData(data);
	}

}
