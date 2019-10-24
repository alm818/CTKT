package frame;

import java.awt.EventQueue;

import javax.swing.JFrame;

import utility.GUIUtility;
import view.PostView;

@SuppressWarnings("serial")
public class SingleFrame extends JFrame{
	private final int WIDTH = GUIUtility.getScreenWidth();
	public SingleFrame(PostView view){
		this(view.getTitle(), view);
	}
	
	public SingleFrame(String title, PostView view){
		super(title);
		int HEIGHT = 200 + view.getRowHeight() * view.getRowCount();
		int maxHeight = GUIUtility.getScreenHeight();
		if (HEIGHT > maxHeight) HEIGHT = maxHeight;
		this.setBounds(GUIUtility.getBound(WIDTH, HEIGHT));
		this.setVisible(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setContentPane(view.getViewPanel());
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				SingleFrame.this.toFront();
			}
		});
	}
}
