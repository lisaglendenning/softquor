/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package softquor;

/**
 * @author shade
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface Client {
	public static final int HUMAN = 0;
	public static final int AGENT = 1;
	public static final int AGENT_PLAYERS = 2;
	public static final int SERVER = 5;
	public static final int BROADCAST = 6;
	
	public abstract void socketMessage(Message message);
	
	public abstract void socketError();
	
	public abstract void socketClose();
	
	public abstract void addListener(ClientListener listener);
}
