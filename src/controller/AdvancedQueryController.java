package controller;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.toedter.calendar.JDateChooser;

import dao.ProductDAO;
import dao.SupplierDAO;
import factory.AttributesFactory;
import factory.ComparatorFactory;
import gui.AutocompleteJComboBox;
import misc.Pair;
import transferObject.Supplier;
import utility.CalendarUtility;
import view.View;

public class AdvancedQueryController extends Controller{
	private Calendar from, to;
	private JComboBox<String> supplierChooser;
	private AutocompleteJComboBox cPJCB, nPJCB;
	public AdvancedQueryController(View view) {
		super(view);
	}
	
	private void setProductJCB(){
		if (supplierChooser.getSelectedIndex() == -1)
			return;
		String nameSupplier = (String) supplierChooser.getSelectedItem();
		Supplier supplier = SupplierDAO.getSupplierList().get(nameSupplier);
		ArrayList<String> code = new ArrayList<String>();
		ArrayList<String> name = new ArrayList<String>();
		for (String nameProduct : supplier.getProducts()){
			String codeProduct = ProductDAO.getProductList().get(nameProduct).getCode();
			
			//GET-NEW
			if (ProductDAO.isOld(codeProduct, nameProduct)){
				Pair<String, String> pair = ProductDAO.getNew(codeProduct, nameProduct);
				codeProduct = pair.getFirst();
				nameProduct = pair.getSecond();
			}
			code.add(codeProduct);
			name.add(nameProduct);
		}
		Collections.sort(code);
		Collections.sort(name);
		cPJCB.changeSearchable(code);
		nPJCB.changeSearchable(name);
	}
	
	@Override
	protected JPanel getControlPanel() {
		AdvancedQueryController self = this;
		JPanel supplierJP = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JLabel supplierJL = new JLabel("Nhà cung cấp:");
		supplierChooser = new JComboBox<String>();
		ArrayList<String> supplierNames= new ArrayList<String>();
		for (Supplier supplier : SupplierDAO.getSupplierList().values())
			if (supplier.isMain())
				supplierNames.add(supplier.getName());
		supplierNames.sort(ComparatorFactory.getStringComparator());
		for (String name : supplierNames)
			supplierChooser.addItem(name);
		if (supplierChooser.getItemCount() > 0) supplierChooser.setSelectedIndex(0);
		supplierChooser.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				setProductJCB();
			}
		});
		supplierJP.add(supplierJL);
		supplierJP.add(supplierChooser);
		
		from = AttributesFactory.CAL2016;
		JPanel fromJP = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JLabel fromJL = new JLabel("Từ ngày:");
		JDateChooser fromDate = new JDateChooser();
	    fromDate.setCalendar(from);
	    fromDate.setLocale(CalendarUtility.getLocale());
	    fromDate.setDateFormatString("dd/MM/yyyy");
	    fromJP.add(fromJL);
	    fromJP.add(fromDate);
	    
	    to = AttributesFactory.getTo();
	    JPanel toJP = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
	    JLabel toJL = new JLabel("Đến ngày:");
	    JDateChooser toDate = new JDateChooser();
	    toDate.setCalendar(to);
	    toDate.setLocale(CalendarUtility.getLocale());
	    toDate.setDateFormatString("dd/MM/yyyy");
	    toJP.add(toJL);
	    toJP.add(toDate);
	    
	    JPanel centerJP = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
	    centerJP.add(supplierJP);
	    centerJP.add(fromJP);
	    centerJP.add(toJP);
	    
	    JLabel cPJL = new JLabel("Mã mặt hàng:");
	    cPJCB = new AutocompleteJComboBox();
	    cPJCB.init();
	    JLabel nPJL = new JLabel("Tên mặt hàng:");
	    nPJCB = new AutocompleteJComboBox();
	    nPJCB.init();
		cPJCB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String newCode = (String) cPJCB.getSelectedItem();
				if (newCode == null || !cPJCB.contains(newCode)) return;
				String newName;
				if (ProductDAO.isNewCode(newCode))
					newName = ProductDAO.getNewName(newCode);
				else
					newName = ProductDAO.getProductName(newCode);
				nPJCB.setSelectedItem(newName);
			}
		});
		nPJCB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String newName = (String) nPJCB.getSelectedItem();
				if (newName == null || !nPJCB.contains(newName)) return;
				String newCode;
				if (ProductDAO.isNewName(newName))
					newCode = ProductDAO.getNewCode(newName);
				else
					newCode = ProductDAO.getProductList().get(newName).getCode();
				cPJCB.setSelectedItem(newCode);
			}
		});
		setProductJCB();
		
	    JPanel cPJP = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
	    cPJP.add(cPJL);
	    cPJP.add(cPJCB);
	    JPanel nPJP = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
	    nPJP.add(nPJL);
	    nPJP.add(nPJCB);
	    
	    JButton queryJB = new JButton("Chọn");
	    queryJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				from = fromDate.getCalendar();
				CalendarUtility.startCal(from);
				to = toDate.getCalendar();
				CalendarUtility.endCal(to);
				if (from.after(to)) JOptionPane.showMessageDialog(contentPane, "Ngày bắt đầu không thể sau ngày kết thúc", "Lỗi đầu vào", JOptionPane.ERROR_MESSAGE);
				else self.requestView(cPJCB.getSelectedItem(), nPJCB.getSelectedItem(), from, to);
			}
	    });
	    
	    JPanel southJP = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
	    southJP.add(cPJP);
	    southJP.add(nPJP);
	    southJP.add(queryJB);
	    
	    JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
	    controlPanel.add(centerJP, BorderLayout.CENTER);
	    controlPanel.add(southJP, BorderLayout.SOUTH);
	    return controlPanel;
	}

}
