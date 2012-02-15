/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package softquor;
import java.util.StringTokenizer;


/**
 * @author shade
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class InitMessage extends Message {
	public int rows;
	public int cols;
	public int walls;
	public int timelimit;

	public InitMessage() {}
	
	public InitMessage(int rows, int cols, int walls, int timelimit) {
		this.rows = rows;
		this.cols = cols;
		this.walls = walls;
		this.timelimit = timelimit;
	}
	
	public void parse(StringTokenizer tok) {
		rows = Integer.parseInt(tok.nextToken());
		cols = Integer.parseInt(tok.nextToken());
		walls = Integer.parseInt(tok.nextToken());
		timelimit = Integer.parseInt(tok.nextToken());
	}

	
	protected String protocolString() {
		return ("init " + rows + " " + cols + " " + walls + " " + timelimit);
	}
}
