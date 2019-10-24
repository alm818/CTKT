package frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.text.WordUtils;

import dao.StaffDAO;
import utility.GUIUtility;

@SuppressWarnings("serial")
public class StaffFrame extends JFrame{
	private final static Logger LOGGER = Logger.getLogger(StaffFrame.class.getName());
	private static final String[] positions = { "Kế toán", "Admin", "Tài xế", "Giao hàng"};
	
	private JPanel contentPane;
	private int WIDTH = 320, HEIGHT = 160;
	private final int WIDTH_JTF = 200;
	private final int HEIGHT_LINE = 30; 
	
	public StaffFrame(){
		super("Thêm nhân viên mới");
		initContentPane();
		this.setContentPane(contentPane);
		this.setVisible(true);
		this.setBounds(GUIUtility.getBound(WIDTH, HEIGHT));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				StaffFrame.this.toFront();
			}
		});
	}
	private void initContentPane(){
		JLabel nameJL = new JLabel("Họ và tên:");
		JTextField nameJTF = new JTextField();
		nameJTF.setPreferredSize((new Dimension(WIDTH_JTF, HEIGHT_LINE)));
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				nameJTF.grabFocus();
				nameJTF.requestFocus();
			}
		});
		JPanel nameJP = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		nameJP.add(nameJL);
		nameJP.add(nameJTF);
		
		JLabel posJL = new JLabel("Chức vụ:");
		JComboBox<String> posJCB = new JComboBox<String>(positions);
		posJCB.setSelectedIndex(0);
		JPanel posJP = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		posJP.add(posJL);
		posJP.add(posJCB);
		
		JPanel centerJP = new JPanel(new GridLayout(0, 1));
		centerJP.add(nameJP);
		centerJP.add(posJP);
		
		JButton addJB = new JButton("Thêm");
		addJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = WordUtils.capitalizeFully(nameJTF.getText().trim().replaceAll(" +", " "));
				if (name.length() == 0)
					JOptionPane.showConfirmDialog(StaffFrame.this, "Tên nhân viên không thể bỏ trống", "Thêm nhân viên mới", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
				else if (StaffDAO.getStaffList().containsKey(name))
					JOptionPane.showConfirmDialog(StaffFrame.this, "Tên nhân viên đã tồn tại", "Thêm nhân viên mới", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
				else {
					String position = (String) posJCB.getSelectedItem();
					try {
						StaffDAO.insert(name, position);
					} catch (SQLException err) {
						LOGGER.severe(err.getMessage());
						err.printStackTrace();
					}
					StaffFrame.this.dispose();
				}
			}
		});
		JButton cancelJB = new JButton("Bỏ");
		cancelJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				StaffFrame.this.dispose();
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
