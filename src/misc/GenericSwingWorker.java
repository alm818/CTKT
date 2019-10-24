package misc;

import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

import frame.ProgressFrame;

public abstract class GenericSwingWorker extends SwingWorker<Void, Void>{
	private Container container;
	private String title;
	private ProgressFrame progressBar;
	private boolean isDisposed;
	
	public GenericSwingWorker(Container container, String title, boolean isDisposed){
		super();
		this.container = container;
		this.title = title;
		this.isDisposed = isDisposed;
	}
	
	private void setEnabled(Container container, boolean isEnabled){
		for (Component child : container.getComponents()){
			if (child instanceof Container) setEnabled((Container) child, isEnabled);
			child.setEnabled(isEnabled);
		}
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		if (container != null) setEnabled(container, false);
		progressBar = new ProgressFrame(container, title);
		process();
		return null;
	}
	
	@Override
    public void done() {
		try {
			super.get();
			if (isDisposed && container != null && container instanceof JFrame){
				JFrame frame = (JFrame) container;
				frame.dispose();
			} else if (container != null) setEnabled(container, true);
			progressBar.dispose();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
    }
	
	abstract public void process();
}
