package frame;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import gui.DynamicTabbedPane;
import utility.GUIUtility;
import view.PostView;

@SuppressWarnings("serial")
public class MultipleFrame extends JFrame{
	private final int WIDTH = GUIUtility.getScreenWidth();
	public MultipleFrame(String title, PostView... views){
		this(title, Arrays.asList(views));
	}
	public MultipleFrame(String title, List<PostView> views){
		super(title);
		int HEIGHT = 200;
		DynamicTabbedPane tabbedPane = new DynamicTabbedPane(50);
		if (views != null)
			for (PostView view : views){
				HEIGHT = Math.max(HEIGHT, 200 + view.getRowHeight() * view.getRowCount());
				tabbedPane.addComponent(view.getName(), view.getViewPanel());
			}
		int maxHeight = GUIUtility.getScreenHeight();
		if (HEIGHT > maxHeight) HEIGHT = maxHeight;
		this.setBounds(GUIUtility.getBound(WIDTH, HEIGHT));
		this.setVisible(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setContentPane(tabbedPane);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				MultipleFrame.this.toFront();
			}
		});
	}
}
