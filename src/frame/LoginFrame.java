package frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import fao.FileDirection;
import main.Main;
import transferObject.DataStorage;
import utility.GUIUtility;

@SuppressWarnings("serial")
public class LoginFrame extends JFrame{
	private JPanel contentPane;
	private final int WIDTH = 260;
	private final int HEIGHT = 160;
	private final int HEIGHT_LINE = 25; 
	private final int WIDTH_JL = 65;
	private final int WIDTH_JTF = 100;
	public LoginFrame(){
		super("Đăng nhập");
		initContentPane();
		this.setContentPane(contentPane);
		this.setVisible(true);
		this.setBounds(GUIUtility.getBound(WIDTH, HEIGHT));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				LoginFrame.this.toFront();
			}
		});
	}
	private void initContentPane(){
		JLabel userJL = new JLabel("Người dùng:");
		JComboBox<String> userChooser = new JComboBox<String>();
		userChooser.addItem("Kế toán");
		userChooser.addItem("Admin");
		if (FileDirection.getData().isAdmin()) userChooser.setSelectedIndex(1);
		else userChooser.setSelectedIndex(0);
		JPanel northJP = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
		northJP.add(userJL);
		northJP.add(userChooser);
		
		JButton confirmJB = new JButton("Xác nhận");
		JLabel oldJL = new JLabel("Mật khẩu:");
		oldJL.setPreferredSize(new Dimension(WIDTH_JL, HEIGHT_LINE));
		JPasswordField oldJPF = new JPasswordField();
		oldJPF.setEchoChar('•');
		oldJPF.setPreferredSize(new Dimension(WIDTH_JTF, HEIGHT_LINE));
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				oldJPF.grabFocus();
				oldJPF.requestFocus();
			}
		});
		oldJPF.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					confirmJB.doClick();
			}
		});
		JPanel centerJP = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		centerJP.add(oldJL);
		centerJP.add(oldJPF);
		
		confirmJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent evt) {
				DataStorage data = FileDirection.getData();
				if (userChooser.getSelectedIndex() == 0){
					data.setAdmin(false);
					LoginFrame.this.dispose();
					Main.booting();
				} else{
					data.setAdmin(true);
					char[] oldPassword = data.getPassword();
					char[] thisPassword = oldJPF.getPassword();
					if (thisPassword.length == 0) JOptionPane.showConfirmDialog(LoginFrame.this, "Mật khẩu không thể bỏ trống", "Đăng nhập", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
					else if (Arrays.equals(oldPassword, thisPassword)){
						LoginFrame.this.dispose();
						Main.booting();
					} else{
						JOptionPane.showConfirmDialog(LoginFrame.this, "Mật khẩu nhập sai", "Đăng nhập", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
						oldJPF.setText("");
					}
				}
			}
		});
		JButton cancelJB = new JButton("Hủy bỏ");
		cancelJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				System.exit(0);
			}
		});
		JPanel southJP = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		southJP.add(confirmJB);
		southJP.add(cancelJB);
		
		contentPane = new JPanel(new BorderLayout(5, 5));
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentPane.add(northJP, BorderLayout.NORTH);
		contentPane.add(centerJP, BorderLayout.CENTER);
		contentPane.add(southJP, BorderLayout.SOUTH);
	}
}
