package controller;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dao.SupplierDAO;
import factory.AttributesFactory;
import factory.ComparatorFactory;
import transferObject.Supplier;
import view.View;

public class SupplierController extends Controller{

	public SupplierController(View view) {
		super(view);
	}

	@Override
	protected JPanel getControlPanel() {
		SupplierController self = this;
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
		supplierChooser.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				self.requestView(supplierChooser.getSelectedItem());
			}	
		});
		supplierJP.add(supplierJL);
		supplierJP.add(supplierChooser);
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
		controlPanel.add(supplierJP);
		return controlPanel;
	}

}
