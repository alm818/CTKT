package controller;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.toedter.calendar.JDateChooser;

import dao.SupplierDAO;
import factory.AttributesFactory;
import factory.ComparatorFactory;
import transferObject.Supplier;
import utility.CalendarUtility;
import view.View;

public class QueryController extends Controller{
	private Calendar from, to;
	public QueryController(View view){
		super(view);
	}
	
	protected JPanel getControlPanel(){
		QueryController self = this;
		JPanel supplierJP = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JLabel supplierJL = new JLabel("Nhà cung cấp:");
		JComboBox<String> supplierChooser = new JComboBox<String>();
		supplierChooser.addItem(AttributesFactory.ALL);
		ArrayList<String> supplierNames= new ArrayList<String>();
		for (Supplier supplier : SupplierDAO.getSupplierList().values())
			if (supplier.isMain())
				supplierNames.add(supplier.getName());
		supplierNames.sort(ComparatorFactory.getStringComparator());
		for (String name : supplierNames)
			supplierChooser.addItem(name);
		supplierJP.add(supplierJL);
		supplierJP.add(supplierChooser);
		
		from = AttributesFactory.getFrom();
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
	    
	    JButton queryJB = new JButton("Chọn");
	    queryJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				from = fromDate.getCalendar();
				CalendarUtility.startCal(from);
				to = toDate.getCalendar();
				CalendarUtility.endCal(to);
				if (from.after(to)) JOptionPane.showMessageDialog(contentPane, "Ngày bắt đầu không thể sau ngày kết thúc", "Lỗi đầu vào", JOptionPane.ERROR_MESSAGE);
				else{
					AttributesFactory.setCalendar(from, to);
					self.requestView(supplierChooser.getSelectedItem(), from, to);
				}
			}
	    });
	    
	    JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
	    controlPanel.add(supplierJP);
	    controlPanel.add(fromJP);
	    controlPanel.add(toJP);
	    controlPanel.add(queryJB);
	    return controlPanel;
	}
}
