package frame;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import utility.GUIUtility;

@SuppressWarnings("serial")
public class ProgressFrame extends JFrame{
	private JProgressBar progressBar;
	private JPanel panel;
	private int WIDTH = 255;
	private final int HEIGHT = 70;
	public ProgressFrame(Component component, String title){
		super(title);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setVisible(true);
		this.setBounds(GUIUtility.getBound(WIDTH, HEIGHT));
		initContentPane();
		this.setContentPane(panel);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ProgressFrame.this.toFront();
			}
		});
	}
	public void setProgress(int progress){
		progressBar.setValue(progress);
		this.repaint();
	}
	private void initContentPane(){
		progressBar = new JProgressBar(0, 100);
	    progressBar.setIndeterminate(true);
	    progressBar.setPreferredSize(new Dimension(WIDTH - 40, 15));
	    
	    panel = new JPanel();
	    panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
	    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    panel.add(progressBar);
	}
}
