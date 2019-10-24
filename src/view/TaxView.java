package view;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;

import dao.ProductDAO;
import dao.SupplierDAO;
import factory.ComparatorFactory;
import factory.DAOFactory;
import factory.FormatFactory;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import fao.FileTaxBillExport;
import fao.FileTaxExport;
import gui.AutoComboBox;
import gui.DynamicTable;
import gui.DynamicTableModel;
import misc.BiMap;
import misc.GenericSwingWorker;
import misc.Pair;
import transferObject.ResSet;
import transferObject.Supplier;
import transferObject.TaxConnector;
import utility.Utility;

public class TaxView extends TableView{
	private static final String TITLE = "BẢNG CÂN ĐỐI HÓA ĐƠN";
	private static final String[] COLUMN_TITLE = { 
			"<html><center>Mã hàng<br>thuế</center></br></html>", 
			"Tên hàng thuế",
			"<html><center>Mã hàng<br>thực tế</center></br></html>",
			"Tên hàng thực tế",
			"<html><center>Tồn đầu<br>thuế</center></br></html>",
			"<html><center>Tồn đầu<br>thực tế</center></br></html>",
			"<html><center>SL nhập<br>thuế</center></br></html>",
			"<html><center>SL nhập<br>thực tế</center></br></html>",
			"<html><center>SL xuất<br>thuế</center></br></html>",
			"<html><center>SL xuất<br>thực tế</center></br></html>",
			"Ghi chú", "Chọn"};
	private static final Class<?>[] COLUMN_CLASS = {String.class, String.class, String.class, String.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, String.class, Boolean.class};
	private static final Integer[] COLUMN_EDITABLE = {2, 3, 11};
	public static final String[] comments = {
			"SL phải xuất HĐ: %.1f",
			"Hàng xuất lố, SL: %.1f",
			"Thiếu HĐ, SL: %.1f",
			"Thất thoát hàng, SL: %.1f",
			"Lấy hóa đơn"
	};
	private TaxConnector connector;
	
	public TaxView(TaxConnector connector) {
		super(String.format(TITLE + " TỪ NGÀY %s ĐẾN NGÀY %s", FormatFactory.formatCalendar(connector.getFrom()), FormatFactory.formatCalendar(connector.getTo())));
		this.connector = connector;
	}

	@Override
	protected JPanel getSouthJP() {
		JButton exportJB = new JButton("Xuất");
		exportJB.setEnabled(false);
		exportJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final class ExportWorker extends GenericSwingWorker{
					public ExportWorker(Container container, String title, boolean isDisposed) {
						super(container, title, isDisposed);
					}

					@Override
					public void process() {
						Vector<Object[]> data = tableModel.getData();
						BiMap<String, String> taxConnection = new BiMap<String, String>();
						for (Object[] row : data){
							if ((boolean) row[11]){
								String nameTax = (String) row[1];
								String _nameProduct = (String) row[3];
								String _codeProduct = (String) row[2];
								String codeProduct = _codeProduct, nameProduct = _nameProduct;
								//NEW-CONVERSE
								if (ProductDAO.isNew(codeProduct, nameProduct)){
									Pair<String, String> pair = ProductDAO.converseNew(codeProduct, nameProduct);
									codeProduct = pair.getFirst();
									nameProduct = pair.getSecond();
								}
								taxConnection.add(nameProduct, nameTax);
							}
						}

						try {
							Connection conn = DAOFactory.getConn();
							Statement removeTax = conn.createStatement();
							removeTax.execute("DELETE FROM APP.Tax_Connection WHERE 1=1");
							removeTax.close();
							conn.commit();
							for (Entry<String, String> entry : taxConnection.getLeftEntry()){
								String nameProduct = entry.getKey();
								String nameTax = entry.getValue();
								ProductDAO.insertTaxConnection(nameProduct, nameTax);
							}
							conn.commit();
							ProductDAO.updateTaxConnection();
							FileTaxExport.export(connector, data);
							FileTaxBillExport.export(connector, data);
						} catch (SQLException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				ExportWorker task = new ExportWorker(null, "Đang xuất file...", false);
				task.execute();
			}
		});
		exportJB.setSize(80, 60);
		JPanel exportJP = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		exportJP.add(exportJB);
		return exportJP;
	}

	@SuppressWarnings("serial")
	@Override
	protected void setTable() {
		tableModel = new DynamicTableModel(Utility.getVector(COLUMN_TITLE));
		tableModel.setClassColumn(COLUMN_CLASS);
		tableModel.setEditableColumn(COLUMN_EDITABLE);
		table = new DynamicTable(tableModel){
			public void setValueAt(Object value, int row, int column){
				super.setValueAt(value, row, column);
				int thisRow = table.convertRowIndexToModel(row);
				if (column == 2){
					String newCode = (String) tableModel.getValueAt(thisRow, 2);
					String newName;
					if (ProductDAO.isNewCode(newCode))
						newName = ProductDAO.getNewName(newCode);
					else
						newName = ProductDAO.getProductName(newCode);
					tableModel.setValueAt(newName, row, 3);
					
					String codeProduct = newCode, nameProduct = newName;
					//NEW-CONVERSE
					if (ProductDAO.isNew(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.converseNew(codeProduct, nameProduct);
						codeProduct = pair.getFirst();
						nameProduct = pair.getSecond();
					}
					ResSet resProduct = connector.getProductMap().get(nameProduct);
					double buyQ = (double) resProduct.get("buyQ");
					double sellQ = (double) resProduct.get("sellQ");
					tableModel.setValueAt(buyQ, row, 5);
					tableModel.setValueAt(sellQ, row, 7);
				} else if (column == 3){
					String newName = (String) tableModel.getValueAt(thisRow, 3);
					String newCode;
					if (ProductDAO.isNewName(newName))
						newCode = ProductDAO.getNewCode(newName);
					else
						newCode = ProductDAO.getProductList().get(newName).getCode();
					tableModel.setValueAt(newCode, row, 2);
					
					String codeProduct = newCode, nameProduct = newName;
					//NEW-CONVERSE
					if (ProductDAO.isNew(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.converseNew(codeProduct, nameProduct);
						codeProduct = pair.getFirst();
						nameProduct = pair.getSecond();
					}
					ResSet resProduct = connector.getProductMap().get(nameProduct);
					double buyQ = (double) resProduct.get("buyQ");
					double sellQ = (double) resProduct.get("sellQ");
					tableModel.setValueAt(buyQ, row, 5);
					tableModel.setValueAt(sellQ, row, 7);
				}
				if (column == 2 || column == 3 || column == 11){
					boolean isChoosed = (boolean) tableModel.getValueAt(thisRow, 11);
					String comment = "";
					if (isChoosed){
						String newName = (String) tableModel.getValueAt(thisRow, 3);
						String newCode = (String) tableModel.getValueAt(thisRow, 2);
						
						String codeProduct = newCode, nameProduct = newName;
						//NEW-CONVERSE
						if (ProductDAO.isNew(codeProduct, nameProduct)){
							Pair<String, String> pair = ProductDAO.converseNew(codeProduct, nameProduct);
							codeProduct = pair.getFirst();
							nameProduct = pair.getSecond();
						}
						double startQTax = (double) tableModel.getValueAt(thisRow, 4);
						double startQ = (double) tableModel.getValueAt(thisRow, 5);
						double buyQTax = (double) tableModel.getValueAt(thisRow, 6);
						double buyQ = (double) tableModel.getValueAt(thisRow, 7);
						double sellQTax = (double) tableModel.getValueAt(thisRow, 8);
						double sellQ = (double) tableModel.getValueAt(thisRow, 9);
						Supplier supplier = SupplierDAO.getSupplier(nameProduct);
						if (supplier.isFull()){
							if (startQTax + buyQTax == startQ + buyQ){
								if (sellQTax == sellQ)
									comment = "";
								else if (sellQTax < sellQ)
									comment = String.format(comments[0], sellQ - sellQTax);
								else
									comment = String.format(comments[1], sellQTax - sellQ);
							} else{
								if (startQTax + buyQTax < startQ + buyQ)
									comment = String.format(comments[2], startQ + buyQ - buyQTax - startQTax);
								else
									comment = String.format(comments[3], startQTax + buyQTax - buyQ - startQ);
							}
						} else{
							double lastQ = startQ + buyQ - sellQ;
							double lastQTax = startQTax + buyQTax - sellQTax;
							if (lastQ > 3 * lastQTax)
								comment = comments[4];
							else if (lastQ < 5 * lastQTax)
								comment = String.format(comments[0], 2 * lastQTax);
						}
					}
					table.setValueAt(comment, row, 10);
				}
			}
		};
		table.getTableHeader().setPreferredSize(new Dimension(table.getColumnModel().getTotalColumnWidth(), 40));
		table.getColumnModel().getColumn(4).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(5).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(6).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(7).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
	}

	@Override
	protected void getParameters(Object... objects) throws SQLException {
		ArrayList<String> codeTerms = new ArrayList<String>();
		ArrayList<String> nameTerms = new ArrayList<String>();
		for (ResSet resSet : connector.getProductMap().values()){
			String _code = (String) resSet.get("newCode");
			String _name = (String) resSet.get("newName");
			codeTerms.add(_code);
			nameTerms.add(_name);
		}
		codeTerms.sort(ComparatorFactory.getNameComparator());
		nameTerms.sort(ComparatorFactory.getNameComparator());
        AutoComboBox codeJCB = new AutoComboBox(codeTerms);
        codeJCB.setDataList(codeTerms);
        AutoComboBox nameJCB = new AutoComboBox(nameTerms);
        nameJCB.setDataList(nameTerms);
		table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(codeJCB));
		table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(nameJCB));
		Vector<Object[]> data = new Vector<Object[]>();
		for (Entry<String, String> entry : connector.getFalseMap().getLeftEntry()){
			String nameProduct = entry.getKey();
			String nameTax = entry.getValue();
			ResSet resProduct = connector.getProductMap().get(nameProduct);
			ResSet resTax = connector.getTaxMap().get(nameTax);
			String codeTax = (String) resTax.get("code");
			String _nameProduct = (String) resProduct.get("newName");
			String _codeProduct = (String) resProduct.get("newCode");
			double buyQTax = (double) resTax.get("buyQ"), sellQTax = (double) resTax.get("sellQ");
			double buyQ = (double) resProduct.get("buyQ"), sellQ = (double) resProduct.get("sellQ");
			double startQ = (double) resProduct.get("startQ"), startQTax = (double) resTax.get("startQ");
			String comment = "";
			Object[] thisRow = { codeTax, nameTax, _codeProduct, _nameProduct, startQTax, startQ, buyQTax, buyQ, sellQTax, sellQ, comment, false};
			data.add(thisRow);
		}
		for (Entry<String, String> entry : connector.getTrueMap().getLeftEntry()){
			String nameProduct = entry.getKey();
			String nameTax = entry.getValue();
			ResSet resProduct = connector.getProductMap().get(nameProduct);
			ResSet resTax = connector.getTaxMap().get(nameTax);
			String codeTax = (String) resTax.get("code");
			String _nameProduct = (String) resProduct.get("newName");
			String _codeProduct = (String) resProduct.get("newCode");
			double buyQTax = (double) resTax.get("buyQ"), sellQTax = (double) resTax.get("sellQ");
			double buyQ = (double) resProduct.get("buyQ"), sellQ = (double) resProduct.get("sellQ");
			double startQ = (double) resProduct.get("startQ"), startQTax = (double) resTax.get("startQ");
			String comment = "";
			Supplier supplier = SupplierDAO.getSupplier(nameProduct);
			if (supplier == null) continue;
			if (supplier.isFull()){
				if (startQTax + buyQTax == startQ + buyQ){
					if (sellQTax == sellQ)
						comment = "";
					else if (sellQTax < sellQ)
						comment = String.format(comments[0], sellQ - sellQTax);
					else
						comment = String.format(comments[1], sellQTax - sellQ);
				} else{
					if (startQTax + buyQTax < startQ + buyQ)
						comment = String.format(comments[2], startQ + buyQ - buyQTax - startQTax);
					else
						comment = String.format(comments[3], startQTax + buyQTax - buyQ - startQ);
				}
			} else{
				double lastQ = startQ + buyQ - sellQ;
				double lastQTax = startQTax + buyQTax - sellQTax;
				if (lastQ > 3 * lastQTax)
					comment = comments[4];
				else if (lastQ < 5 * lastQTax)
					comment = String.format(comments[0], 2 * lastQTax);
			}
			Object[] thisRow = { codeTax, nameTax, _codeProduct, _nameProduct, startQTax, startQ, buyQTax, buyQ, sellQTax, sellQ, comment, true};
			data.add(thisRow);
		}
		tableModel.setData(data);
	}
}
