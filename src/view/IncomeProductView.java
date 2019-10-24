package view;

import java.awt.Dimension;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import dao.BillDAO;
import dao.BuyDAO;
import dao.ProductDAO;
import dao.RBuyDAO;
import dao.RSellDAO;
import dao.SellDAO;
import dao.SupplierDAO;
import factory.AttributesFactory;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import gui.DynamicTable;
import gui.DynamicTableModel;
import misc.Pair;
import transferObject.Bill;
import transferObject.ResSet;
import utility.Utility;

public class IncomeProductView extends SearchTableView{
	private static final String TITLE = "CHI TIẾT LỢI NHUẬN TỪNG MẶT HÀNG THEO NHÀ CUNG CẤP";
	private static final String[] COLUMN_TITLE = { 
			"Mã mặt hàng", "Tên mặt hàng", 
			"Tồn đầu", "Tổng mua", "Tổng BTL", "Tổng bán", "Tổng MTL", "Tồn cuối", 
			"<html><center>Tổng<br>doanh thu</center></br></html>", 
			"<html><center>Tổng<br>lợi nhuận</center></br></html>", 
			"<html><center>Trung bình<br>% lợi nhuận</center></br></html>"
	};
	private static final Class<?>[] COLUMN_CLASS = { String.class, String.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Long.class, Long.class, Double.class};
	private static final Integer[] COLUMN_EDITABLE = {};
	
	public IncomeProductView() {
		super(TITLE);
	}

	@Override
	protected void setTable() {
		tableModel = new DynamicTableModel(Utility.getVector(COLUMN_TITLE));
		tableModel.setClassColumn(COLUMN_CLASS);
		tableModel.setEditableColumn(COLUMN_EDITABLE);
		table = new DynamicTable(tableModel);
		table.getTableHeader().setPreferredSize(new Dimension(table.getColumnModel().getTotalColumnWidth(), 40));
		table.getColumnModel().getColumn(2).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(3).setCellRenderer(GUICellRendererFactory.getSignedQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(4).setCellRenderer(GUICellRendererFactory.getSignedQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(5).setCellRenderer(GUICellRendererFactory.getSignedQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(6).setCellRenderer(GUICellRendererFactory.getSignedQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(7).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(8).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightItalic()));
		table.getColumnModel().getColumn(9).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightBold()));
		table.getColumnModel().getColumn(10).setCellRenderer(GUICellRendererFactory.getPerRenderer(GUICellStyleFactory.getRight()));
	}

	@Override
	protected void getParameters(Object... objects) throws SQLException {
		String nameSupplier = (String) objects[0];
		Calendar from = (Calendar) objects[1];
		Calendar to = (Calendar) objects[2];
		Vector<Object[]> data = new Vector<Object[]>();
		ArrayList<Bill> buyList = BuyDAO.getBills(from, to);
		ArrayList<Bill> sellList = SellDAO.getBills(from, to);
		ArrayList<Bill> rSellList = RSellDAO.getBills(from, to);
		ArrayList<Bill> rBuyList = RBuyDAO.getBills(from, to);
		HashMap<String, ResSet> rowSet = new HashMap<String, ResSet>();
		if (nameSupplier.equals(AttributesFactory.ALL)){
			for (Bill bill : buyList){
				for (String nameProduct : bill.getProductList()){
					if (!rowSet.containsKey(nameProduct)){
						rowSet.put(nameProduct, new ResSet());
						rowSet.get(nameProduct).put("day", Calendar.getInstance());
					}
					ResSet resSet = rowSet.get(nameProduct);
					Calendar day = (Calendar) resSet.get("day");
					if (day.after(bill.getDay())){
						resSet.put("day", bill.getDay());
						resSet.put("codeBill", bill.getCodeBill());
					}
					double buy = (double) resSet.get("buy") + bill.getProduct(nameProduct).getSumQ();
					resSet.put("buy", buy);
				}
			}
			for (Bill bill : sellList){
				for (String nameProduct : bill.getProductList()){
					if (!rowSet.containsKey(nameProduct)){
						rowSet.put(nameProduct, new ResSet());
						rowSet.get(nameProduct).put("day", Calendar.getInstance());
					}
					ResSet resSet = rowSet.get(nameProduct);
					Calendar day = (Calendar) resSet.get("day");
					if (day.after(bill.getDay())){
						resSet.put("day", bill.getDay());
						resSet.put("codeBill", bill.getCodeBill());
					}
					int sell_id = bill.getProduct(nameProduct).getID();
					ResSet sellProfit = SellDAO.getProfit(sell_id);
					if (sellProfit != null){
						double q = bill.getQ(nameProduct);
						double rq = (double) sellProfit.get("rq");
						int sp = (int) sellProfit.get("sp");
						int bp = (int) sellProfit.get("bp");
						long revenue = (long) Utility.getNumericValue(resSet.get("revenue")) + (long) (sp * (q - rq));
						long profit = (long) Utility.getNumericValue(resSet.get("profit")) + (long) ((sp - bp) * (q - rq));
						resSet.put("revenue", revenue);
						resSet.put("profit", profit);
					}
					double sell = (double) resSet.get("sell") + bill.getProduct(nameProduct).getSumQ();
					resSet.put("sell", sell);
				}
			}
			for (Bill bill : rSellList){
				for (String nameProduct : bill.getProductList()){
					if (!rowSet.containsKey(nameProduct)){
						rowSet.put(nameProduct, new ResSet());
						rowSet.get(nameProduct).put("day", Calendar.getInstance());
					}
					ResSet resSet = rowSet.get(nameProduct);
					Calendar day = (Calendar) resSet.get("day");
					if (day.after(bill.getDay())){
						resSet.put("day", bill.getDay());
						resSet.put("codeBill", bill.getCodeBill());
					}
					double rsell = (double) resSet.get("rsell") + bill.getProduct(nameProduct).getSumQ();
					resSet.put("rsell", rsell);
				}
			}
			for (Bill bill : rBuyList){
				for (String nameProduct : bill.getProductList()){
					if (!rowSet.containsKey(nameProduct)){
						rowSet.put(nameProduct, new ResSet());
						rowSet.get(nameProduct).put("day", Calendar.getInstance());
					}
					ResSet resSet = rowSet.get(nameProduct);
					Calendar day = (Calendar) resSet.get("day");
					if (day.after(bill.getDay())){
						resSet.put("day", bill.getDay());
						resSet.put("codeBill", bill.getCodeBill());
					}
					double rbuy = (double) resSet.get("rbuy") + bill.getProduct(nameProduct).getSumQ();
					resSet.put("rbuy", rbuy);
				}
			}
		} else{
			HashSet<String> products = SupplierDAO.getSupplierList().get(nameSupplier).getProducts();
			for (Bill bill : buyList){
				for (String nameProduct : bill.getProductList()){
					if (!products.contains(nameProduct)) continue;
					if (!rowSet.containsKey(nameProduct)){
						rowSet.put(nameProduct, new ResSet());
						rowSet.get(nameProduct).put("day", Calendar.getInstance());
					}
					ResSet resSet = rowSet.get(nameProduct);
					Calendar day = (Calendar) resSet.get("day");
					if (day.after(bill.getDay())){
						resSet.put("day", bill.getDay());
						resSet.put("codeBill", bill.getCodeBill());
					}
					double buy = (double) resSet.get("buy") + bill.getProduct(nameProduct).getSumQ();
					resSet.put("buy", buy);
				}
			}
			for (Bill bill : sellList){
				for (String nameProduct : bill.getProductList()){
					if (!products.contains(nameProduct)) continue;
					if (!rowSet.containsKey(nameProduct)){
						rowSet.put(nameProduct, new ResSet());
						rowSet.get(nameProduct).put("day", Calendar.getInstance());
					}
					ResSet resSet = rowSet.get(nameProduct);
					Calendar day = (Calendar) resSet.get("day");
					if (day.after(bill.getDay())){
						resSet.put("day", bill.getDay());
						resSet.put("codeBill", bill.getCodeBill());
					}
					int sell_id = bill.getProduct(nameProduct).getID();
					ResSet sellProfit = SellDAO.getProfit(sell_id);
					if (sellProfit != null){
						double q = bill.getQ(nameProduct);
						double rq = (double) sellProfit.get("rq");
						int sp = (int) sellProfit.get("sp");
						int bp = (int) sellProfit.get("bp");
						long revenue = (long) Utility.getNumericValue(resSet.get("revenue")) + (long) (sp * (q - rq));
						long profit = (long) Utility.getNumericValue(resSet.get("profit")) + (long) ((sp - bp) * (q - rq));
						resSet.put("revenue", revenue);
						resSet.put("profit", profit);
					}
					double sell = (double) resSet.get("sell") + bill.getProduct(nameProduct).getSumQ();
					resSet.put("sell", sell);
				}
			}
			for (Bill bill : rSellList){
				for (String nameProduct : bill.getProductList()){
					if (!products.contains(nameProduct)) continue;
					if (!rowSet.containsKey(nameProduct)){
						rowSet.put(nameProduct, new ResSet());
						rowSet.get(nameProduct).put("day", Calendar.getInstance());
					}
					ResSet resSet = rowSet.get(nameProduct);
					Calendar day = (Calendar) resSet.get("day");
					if (day.after(bill.getDay())){
						resSet.put("day", bill.getDay());
						resSet.put("codeBill", bill.getCodeBill());
					}
					double rsell = (double) resSet.get("rsell") + bill.getProduct(nameProduct).getSumQ();
					resSet.put("rsell", rsell);
				}
			}
			for (Bill bill : rBuyList){
				for (String nameProduct : bill.getProductList()){
					if (!products.contains(nameProduct)) continue;
					if (!rowSet.containsKey(nameProduct)){
						rowSet.put(nameProduct, new ResSet());
						rowSet.get(nameProduct).put("day", Calendar.getInstance());
					}
					ResSet resSet = rowSet.get(nameProduct);
					Calendar day = (Calendar) resSet.get("day");
					if (day.after(bill.getDay())){
						resSet.put("day", bill.getDay());
						resSet.put("codeBill", bill.getCodeBill());
					}
					double rbuy = (double) resSet.get("rbuy") + bill.getProduct(nameProduct).getSumQ();
					resSet.put("rbuy", rbuy);
				}
			}
		}
		for (String nameProduct : rowSet.keySet()){
			ResSet resSet = rowSet.get(nameProduct);
			if (!resSet.contains("codeBill")) continue;
			String codeProduct = ProductDAO.getProductList().get(nameProduct).getCode();
			String _codeProduct = codeProduct, _nameProduct = nameProduct;
			//GET-NEW
			if (ProductDAO.isOld(codeProduct, nameProduct)){
				Pair<String, String> pair = ProductDAO.getNew(codeProduct, nameProduct);
				_codeProduct = pair.getFirst();
				_nameProduct = pair.getSecond();
			}
			String codeBill = (String) resSet.get("codeBill");
			Pair<Double, Double> start = BillDAO.getStatus(codeBill, nameProduct);
			double init_q = start.getFirst() + start.getSecond();
			double buy = (double) resSet.get("buy");
			double sell = Utility.reverseSign((double) resSet.get("sell"));
			double rsell = (double) resSet.get("rsell");
			double rbuy = Utility.reverseSign((double) resSet.get("rbuy"));
			long revenue = (long) Utility.getNumericValue(resSet.get("revenue"));
			long profit = (long) Utility.getNumericValue(resSet.get("profit"));
			Double percent;
			if (revenue != 0)
				percent = profit / (double) revenue;
			else
				percent = null;
			Object[] thisRow = {
					_codeProduct, _nameProduct, init_q,
					buy, rsell, sell, rbuy, init_q + buy + rsell + sell + rbuy, 
					revenue, profit, percent
			};
			data.add(thisRow);
		}
		tableModel.setData(data);
	}

}
