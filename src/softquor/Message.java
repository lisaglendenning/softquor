/*
 * Created on Apr 27, 2004
 *
 */
package softquor;
import java.util.StringTokenizer;

/**
 * @author Lisa Glendenning
 *
 */
public abstract class Message {
	public int from;
	public int to;
	
	protected Message() {
		from = 0;
		to = 0;
	}
	
	public static Message createMessage(String str) {
		try {
			Message message = null;

			StringTokenizer tok = new StringTokenizer(str);
			int from = Integer.valueOf(tok.nextToken()).intValue();
			int to = Integer.valueOf(tok.nextToken()).intValue();
			String type = tok.nextToken();
		
			if(type.equals("connect")) {
				message = new ConnectMessage();
			}
			else if(type.equals("exception")) {
				message = new ExceptionMessage();			
			}
			else if(type.equals("hello")) {
				message = new HelloMessage();			
			}
			else if(type.equals("init")) {	
				message = new InitMessage();
			}
			else if(type.equals("move")) {
				message = new MoveMessage();
			}
			else if(type.equals("quit")) {
				message = new QuitMessage();
			}
			else if(type.equals("register")) {
				message = new RegisterMessage();
			}
			else if(type.equals("start")) {
				message = new StartMessage();
			}
			else if(type.equals("talk")) {
				message = new TalkMessage();
			}
			else if(type.equals("turn")) {
				message = new TurnMessage();
			}
			else if(type.equals("win")) {
				message = new WinMessage();
			}
		
			message.from = from;
			message.to = to;
			message.parse(tok);
			return message;
		}
		catch(Exception e) {
			return null;
		}
	}
	
	public String protocol() {
		return (from + " " + to + " " + protocolString()); 
	}
	
	protected abstract String protocolString();
	
	public abstract void parse(StringTokenizer rest) throws Exception;
}
