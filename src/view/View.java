package view;

import java.awt.Container;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.swing.JPanel;

import misc.GenericSwingWorker;

public abstract class View {
	protected final static Logger LOGGER = Logger.getLogger(View.class.getName());
	protected JPanel viewPanel;
	protected GenericSwingWorker task;
	
	public View(){
		viewPanel = new JPanel();
	}
	
	public JPanel getViewPanel(){
		return viewPanel;
	}
	
	public boolean isDone(){
		return task.isDone();
	}
	
	public void load(Object...objects){
		final class ViewWorker extends GenericSwingWorker{
			public ViewWorker(Container container, String title, boolean isDisposed) {
				super(container, title, isDisposed);
			}
			@Override
			public void process() {
				try {
					getParameters(objects);
				} catch (SQLException e) {
					LOGGER.severe(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		task = new ViewWorker(viewPanel, "Đang lấy dữ liệu...", false);
		task.execute();
	}
	
	protected abstract void preparePanel();
	
	protected abstract void getParameters(Object...objects) throws SQLException;
}
