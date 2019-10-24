package misc;

public class NextIndex {
    private int c;
    
	public NextIndex(){
		c = 0;
	}

    public synchronized int value() {
    	c ++;
    	return c - 1;
    }
}