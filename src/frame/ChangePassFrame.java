package frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import fao.FileDirection;
import utility.GUIUtility;

@SuppressWarnings("serial")
public class ChangePassFrame extends JFrame{
	private MainFrame mainFrame;
	private JPanel contentPane;
	private final int WIDTH = 310;
	private final int HEIGHT = 195;
	private final int HEIGHT_LINE = 25; 
	private final int WIDTH_JL = 140;
	private final int WIDTH_JTF = 100;
	
	public ChangePassFrame(MainFrame mainFrame){
		super("Cài đặt mật khẩu mới");
		this.mainFrame = mainFrame;
		mainFrame.setEnabled(false);
		initContentPane();
		this.setContentPane(contentPane);
		this.setVisible(true);
		this.setBounds(GUIUtility.getBound(WIDTH, HEIGHT));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	protected void initContentPane(){
		JLabel oldJL = new JLabel("Mật khẩu cũ:");
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
		JPanel northJP = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
		northJP.add(oldJL);
		northJP.add(oldJPF);
		
		JLabel passJL = new JLabel("Mật khẩu mới:");
		passJL.setPreferredSize(new Dimension(WIDTH_JL, HEIGHT_LINE));
		JPasswordField fieldJPF = new JPasswordField();
		fieldJPF.setEchoChar('•');
		fieldJPF.setPreferredSize(new Dimension(WIDTH_JTF, HEIGHT_LINE));
		JPanel passJP = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		passJP.add(passJL);
		passJP.add(fieldJPF);
		
		JLabel rPassJL = new JLabel("Nhập lại mật khẩu mới:");
		rPassJL.setPreferredSize(new Dimension(WIDTH_JL, HEIGHT_LINE));
		JPasswordField rFieldJPF = new JPasswordField();
		rFieldJPF.setEchoChar('•');
		rFieldJPF.setPreferredSize(new Dimension(WIDTH_JTF, HEIGHT_LINE));
		JPanel rPassJP = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		rPassJP.add(rPassJL);
		rPassJP.add(rFieldJPF);
		
		JPanel centerJP = new JPanel(new GridLayout(0, 1, 5, 5));
		centerJP.add(passJP);
		centerJP.add(rPassJP);
		
		JButton confirmJB = new JButton("Xác nhận");
		confirmJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				char[] password = fieldJPF.getPassword();
				char[] rPassword = rFieldJPF.getPassword();
				char[] oldPassword = oldJPF.getPassword();
				char[] thisPassword = null;
				thisPassword = FileDirection.getData().getPassword();
				if (password.length == 0 || rPassword.length == 0 || oldPassword.length == 0)
					JOptionPane.showConfirmDialog(ChangePassFrame.this, "Mật khẩu không thể bỏ trống", "Đổi mật khẩu", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
				else if (Arrays.equals(password, rPassword)){
					if (Arrays.equals(thisPassword, oldPassword)){
						FileDirection.getData().setPassword(password);
						ChangePassFrame.this.dispose();
					} else JOptionPane.showConfirmDialog(ChangePassFrame.this, "Mật khẩu cũ nhập sai", "Đổi mật khẩu", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
				} else JOptionPane.showConfirmDialog(ChangePassFrame.this, "Mật khẩu nhập không giống nhau", "Đổi mật khẩu", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
			}
		});
		JButton cancelJB = new JButton("Hủy bỏ");
		cancelJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				ChangePassFrame.this.dispose();
			}
		});
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosed(WindowEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						mainFrame.toFront();
						mainFrame.setEnabled(true);
					}
				});
			}
		});
		JPanel southJP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		southJP.add(confirmJB);
		southJP.add(cancelJB);
		
		contentPane = new JPanel(new BorderLayout(5, 5));
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentPane.add(northJP, BorderLayout.NORTH);
		contentPane.add(centerJP, BorderLayout.CENTER);
		contentPane.add(southJP, BorderLayout.SOUTH);
	}
}
