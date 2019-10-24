package utility;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

public class GUIUtility {
	private static final Rectangle ACTUAL_DIM = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
	private static final int WIDTH = ACTUAL_DIM.width;
	private static final int HEIGHT = ACTUAL_DIM.height;
	
	public static Rectangle getScreenBound(){
		return new Rectangle(0, 0, WIDTH, HEIGHT);
	}
	
	public static Rectangle getBound(int width, int height){
		return new Rectangle((WIDTH - width) / 2, (HEIGHT - height) / 2, width, height);
	}
	
	public static int getScreenWidth(){ return WIDTH; }
	
	public static int getScreenHeight(){ return HEIGHT; }
}
