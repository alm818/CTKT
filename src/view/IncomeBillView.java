package view;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Vector;
import java.util.logging.Logger;

import dao.BillDAO;
import dao.CustomerDAO;
import dao.ProductDAO;
import dao.SellDAO;
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
import transferObject.Status;
import utility.Utility;

public class IncomeBillView extends SearchTableView{
	protected final static Logger LOGGER = Logger.getLogger(IncomeBillView.class.getName());
	private static final String TITLE = "CHI TIẾT LỢI NHUẬN TỪNG ĐƠN HÀNG THEO NHÀ CUNG CẤP";
	private static final String[] COLUMN_TITLE = { 
			"<html><center>Ngày<br>hóa đơn</center></br></html>", 
			"<html><center>Mã<br>hóa đơn</center></br></html>", 
			"Tên khách hàng", "Mã mặt hàng", "Tên mặt hàng", 
			"<html><center>Đơn giá<br>mua</center></br></html>", 
			"<html><center>Đơn giá<br>bán</center></br></html>", 
			"<html><center>Số lượng<br>bán</center></br></html>", 
			"<html><center>Số lượng<br>trả về</center></br></html>", 
			"<html><center>Thành tiền<br>chênh lệch</center></br></html>", 
			"<html><center>Phần trăm<br>lợi nhuận</center></br></html>"};
	private static final Class<?>[] COLUMN_CLASS = {Calendar.class, String.class, String.class, String.class, String.class, Long.class, Long.class, Double.class, Double.class, Long.class, Double.class};
	private static final Integer[] COLUMN_EDITABLE = {};

	public IncomeBillView() {
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
						bill = BillDAO.getBill(codeSell);
					} catch (SQLException err) {
						LOGGER.severe(err.getMessage());
						err.printStackTrace();
					}
					ArrayList<PostView> views = new ArrayList<PostView>();
		    		int sp_id = bill.getProduct(nameProduct).getID();
		    		try {
						ArrayList<Status> history = SellDAO.getHistory(sp_id);
						for (Status status : history){
							Bill billBuy = status.getBill();
							String codeBuy = billBuy.getCodeBill();
							if (codeBuy.equals(AttributesFactory.CODE) || codeBuy.equals(AttributesFactory.PROMO_CODE)) continue;
							String buyTitle = String.format("CHI TIẾT HÓA ĐƠN %s VÀO NGÀY %s - %s", codeBuy, FormatFactory.formatCalendar(billBuy.getDay()), billBuy.getTarget());
				    		String buyName = String.format("Hóa đơn %s", codeBuy);
							BuyView buyView = new BuyView(buyTitle, buyName, codeBuy, nameProduct);
							views.add(buyView);
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
		    		String historyTitle = String.format("CHI TIẾT HÓA ĐƠN MUA - %s", _nameProduct);
		    		String historyName = "Hóa đơn mua hàng";
		    		HistoryView historyView = new HistoryView(historyTitle, historyName, codeSell, nameProduct);
		    		views.add(historyView);
		    		String nameCustomer = CustomerDAO.getCustomerList().get(bill.getTarget()).getName();
		    		String day = FormatFactory.formatCalendar(bill.getDay());
		    		String sellTitle = String.format("CHI TIẾT HÓA ĐƠN %s VÀO NGÀY %s - %s", codeSell, day, nameCustomer);
		    		String sellName = String.format("Hóa đơn %s", codeSell);
		    		SellView sellView = new SellView(sellTitle, sellName, codeSell, nameProduct);
		    		views.add(sellView);
		    		while (true){
		    			boolean isDone = true;
		    			for (PostView view : views){
		    				if (!view.isDone()){
		    					isDone = false;
		    					break;
		    				}
		    			}
		    			if (isDone){
		    				String title = String.format("%s - %s - %s - %s", day, codeSell, nameCustomer, _nameProduct);
							@SuppressWarnings("unused")
							MultipleFrame frame = new MultipleFrame(title, views);
		    				break;
		    			}
		    		}
		    	}
		    }
		});
		table.getTableHeader().setPreferredSize(new Dimension(table.getColumnModel().getTotalColumnWidth(), 40));
		table.getColumnModel().getColumn(0).setCellRenderer(GUICellRendererFactory.getCalendarRenderer(GUICellStyleFactory.getCenter()));
		table.getColumnModel().getColumn(5).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightItalic()));
		table.getColumnModel().getColumn(6).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightItalic()));
		table.getColumnModel().getColumn(7).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(8).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(9).setCellRenderer(GUICellRendererFactory.getSignedPriceRenderer(GUICellStyleFactory.getRightBoldItalic()));
		table.getColumnModel().getColumn(10).setCellRenderer(GUICellRendererFactory.getPerRenderer(GUICellStyleFactory.getRight()));
	}

	@Override
	protected void getParameters(Object... objects) throws SQLException {
		String nameSupplier = (String) objects[0];
		Calendar from = (Calendar) objects[1];
		Calendar to = (Calendar) objects[2];
		Vector<Object[]> data = new Vector<Object[]>();
		ArrayList<Bill> billList = SellDAO.getBills(from, to);
		if (nameSupplier.equals(AttributesFactory.ALL)){
			for (Bill bill : billList){
				for (String nameProduct : bill.getProductList()){
					double q = bill.getQ(nameProduct);
					if (q == 0) continue;
					ResSet resSet = SellDAO.getProfit(bill.getProduct(nameProduct).getID());
					if (resSet == null) continue;
					String nameCustomer = CustomerDAO.getCustomerList().get(bill.getTarget()).getName();
					String codeProduct = ProductDAO.getProductList().get(nameProduct).getCode();
					String _codeProduct = codeProduct, _nameProduct = nameProduct;
					//GET-NEW
					if (ProductDAO.isOld(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.getNew(codeProduct, nameProduct);
						_codeProduct = pair.getFirst();
						_nameProduct = pair.getSecond();
					}
					
					Double rq = (Double) resSet.get("rq");
					rq = Math.min(rq, q);
					Integer sp = (Integer) resSet.get("sp");
					Integer bp = (Integer) resSet.get("bp");
					if (bp == 0) continue;
					Integer dif = (int) ((sp - bp) * (q - rq));
					double percent = (sp - bp) / (double) bp;
					Object[] thisRow = {
							bill.getDay(), bill.getCodeBill(), nameCustomer, _codeProduct, _nameProduct,
							bp, sp, q, rq, dif, percent
					};
					data.add(thisRow);
				}
			}
		} else{
			HashSet<String> products = SupplierDAO.getSupplierList().get(nameSupplier).getProducts();
			for (Bill bill : billList){
				for (String nameProduct : bill.getProductList()){
					if (!products.contains(nameProduct)) continue;
					double q = bill.getQ(nameProduct);
					if (q == 0) continue;
					ResSet resSet = SellDAO.getProfit(bill.getProduct(nameProduct).getID());
					if (resSet == null) continue;
					String nameCustomer = CustomerDAO.getCustomerList().get(bill.getTarget()).getName();
					String codeProduct = ProductDAO.getProductList().get(nameProduct).getCode();
					String _codeProduct = codeProduct, _nameProduct = nameProduct;
					//GET-NEW
					if (ProductDAO.isOld(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.getNew(codeProduct, nameProduct);
						_codeProduct = pair.getFirst();
						_nameProduct = pair.getSecond();
					}
					
					Double rq = (Double) resSet.get("rq");
					rq = Math.min(rq, q);
					Integer sp = (Integer) resSet.get("sp");
					Integer bp = (Integer) resSet.get("bp");
					if (bp == 0) continue;
					Integer dif = (int) ((sp - bp) * (q - rq));
					double percent = (sp - bp) / (double) bp;
					Object[] thisRow = {
							bill.getDay(), bill.getCodeBill(), nameCustomer, _codeProduct, _nameProduct,
							bp, sp, q, rq, dif, percent
					};
					data.add(thisRow);
				}
			}
		}
		tableModel.setData(data);
	}

}
