package frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fao.FileDirection;
import transferObject.DataStorage;
import utility.GUIUtility;

@SuppressWarnings("serial")
public class WageSettingFrame extends JFrame{
	private JPanel contentPane;
	private int WIDTH = 320, HEIGHT = 180;
	private final int WIDTH_JTF = 120;
	private final int HEIGHT_LINE = 35; 
	
	public WageSettingFrame(){
		super("Điều chỉnh hệ số lương");
		initContentPane();
		this.setContentPane(contentPane);
		this.setVisible(true);
		this.setBounds(GUIUtility.getBound(WIDTH, HEIGHT));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				WageSettingFrame.this.toFront();
			}
		});
	}
	private void initContentPane(){
		DataStorage data = FileDirection.getData();
		JLabel targetRevenueJL = new JLabel("Doanh số tiêu chuẩn:");
		JTextField targetRevenueJTF = new JTextField();
		targetRevenueJTF.setPreferredSize((new Dimension(WIDTH_JTF, HEIGHT_LINE)));
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				targetRevenueJTF.grabFocus();
				targetRevenueJTF.requestFocus();
			}
		});
		targetRevenueJTF.setText(Long.toString(data.getTargetRevenue()));
		JPanel targetRevenueJP = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		targetRevenueJP.add(targetRevenueJL);
		targetRevenueJP.add(targetRevenueJTF);
		
		JLabel percentRevenueJL = new JLabel("Phần trăm doanh thu:");
		JTextField percentRevenueJTF = new JTextField();
		percentRevenueJTF.setPreferredSize((new Dimension(WIDTH_JTF, HEIGHT_LINE)));
		percentRevenueJTF.setText(Double.toString(data.getPercentRevenue()));
		JPanel percentRevenueJP = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		percentRevenueJP.add(percentRevenueJL);
		percentRevenueJP.add(percentRevenueJTF);
		
		JPanel centerJP = new JPanel(new GridLayout(0, 1));
		centerJP.add(targetRevenueJP);
		centerJP.add(percentRevenueJP);
		
		JButton addJB = new JButton("Lưu");
		addJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					long targetRevenue = Long.parseLong(targetRevenueJTF.getText());
					double percentRevenue = Double.parseDouble(percentRevenueJTF.getText());
					data.setRevenue(targetRevenue, percentRevenue);
					WageSettingFrame.this.dispose();
				}
				catch (NumberFormatException err) {
					JOptionPane.showConfirmDialog(WageSettingFrame.this, "Không phải dạng số", "Lưu hệ số", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		JButton cancelJB = new JButton("Bỏ");
		cancelJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				WageSettingFrame.this.dispose();
			}
		});
		JPanel southJB = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		southJB.add(addJB);
		southJB.add(cancelJB);
		
		contentPane = new JPanel(new BorderLayout(5, 5));
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentPane.add(centerJP, BorderLayout.CENTER);
		contentPane.add(southJB, BorderLayout.SOUTH);
	}
}
