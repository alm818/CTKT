package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public abstract class SearchTableView extends TableView{

	public SearchTableView(String title) {
		super(title);
	}

	@Override
	protected JPanel getSouthJP() {
		JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		JComboBox<String> columnChooser = new JComboBox<String>();
		JTextField filter = new JTextField();
		filter.setPreferredSize(new Dimension(500, columnChooser.getPreferredSize().height));
		JLabel columnJL = new JLabel("Tìm kiếm theo cột:");
		search.add(columnJL);
		search.add(columnChooser);
		search.add(filter);
		JPanel info = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
		JLabel averageJL = new JLabel();
		JLabel countJL = new JLabel();
		JLabel sumJL = new JLabel();
		info.add(averageJL);
		info.add(countJL);
		info.add(sumJL);
		table.setFilterField(filter);
		table.setColumnChooser(columnChooser);
		table.setDisplay(sumJL, countJL, averageJL);
		JPanel southJP = new JPanel(new BorderLayout(5, 5));
		southJP.add(info, BorderLayout.NORTH);
		southJP.add(search, BorderLayout.CENTER);
		return southJP;
	}
}
