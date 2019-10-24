package controller;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import utility.CalendarUtility;
import view.WageView;

public class WageController extends Controller{
	private static final int months = 6;

	public WageController(WageView view) {
		super(view);
		view.setController(this);
	}

	@Override
	protected JPanel getControlPanel() {
		JLabel monthJL = new JLabel("Bảng lương tháng:");
		JComboBox<String>monthChooser = new JComboBox<String>();
		monthChooser.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String item = (String) monthChooser.getSelectedItem();
				int month = Integer.valueOf(item.substring(0, 2)) - 1;
				int year = Integer.valueOf(item.substring(3));
				Calendar from = Calendar.getInstance();
				from.set(year, month, 1);
				CalendarUtility.startCal(from);
				Calendar to = Calendar.getInstance();
				to.set(year, month, from.getActualMaximum(Calendar.DATE));
				CalendarUtility.endCal(to);
				WageController.this.requestView(from, to);
			}
		});
		Calendar now = Calendar.getInstance();
		for (int i = 0; i < months; i ++){
			int month = now.get(Calendar.MONTH) + 1;
			int year = now.get(Calendar.YEAR);
			monthChooser.addItem(String.format("%02d/%d", month, year));
			now.add(Calendar.MONTH, -1);
		}
		now = Calendar.getInstance();
		Calendar half = Calendar.getInstance();
		half.set(Calendar.DAY_OF_MONTH, now.getActualMaximum(Calendar.DAY_OF_MONTH) / 2);
		if (now.after(half))monthChooser.setSelectedIndex(0);
		else monthChooser.setSelectedIndex(1);
		
	    JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
	    controlPanel.add(monthJL);
	    controlPanel.add(monthChooser);
	    return controlPanel;
	}

}
