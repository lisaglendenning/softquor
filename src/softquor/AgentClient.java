/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package softquor;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.ListIterator;
import softquor.ai.*;
import softquor.board.BoardState;

/**
 * @author shade
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AgentClient implements Client {

	private int id;
	private Agent agent;
	private AIQuoridor quoridor;
	private SocketIO socket;
	private SocketThread thread;
	private boolean debug;
	private Vector listeners;
	
	public AgentClient(Agent agent, boolean debug) {
		this.id = -1;
		this.agent = agent;
		this.quoridor = new AIQuoridor();
		this.socket = null;
		this.thread = null;
		this.debug = debug;
		this.listeners = new Vector();
	}
	
	public void addListener(ClientListener listener) {
		listeners.add(listener);
	}
	
	protected void notifyListeners(Message message) {
		ListIterator lit = listeners.listIterator();
		while(lit.hasNext()) {
			ClientListener next = (ClientListener)lit.next();
			next.listenEvent(new ClientEvent(this, "disconnect"));
		}
	}
	
	public void command(Message message) {
		if(debug) {
			System.out.println("Command: " + message.protocol());
		}
		message.from = id;
		if(message instanceof TalkMessage) {
			message.to = BROADCAST;
		}
		else {
			message.to = SERVER;
		}
		if(socket != null) {
			socket.write(message.protocol());
		}
	}
	
	public synchronized void socketMessage(Message message) {
		if(debug) {
			System.out.println("Message: " + message.protocol());
		}
		if(message instanceof ConnectMessage) {
			connect((ConnectMessage)message);
		}
		if(message instanceof ExceptionMessage) {
			exception((ExceptionMessage)message);
		}
		else if(message instanceof HelloMessage) {
			hello((HelloMessage)message);
		}
		else if(message instanceof InitMessage) {
			init((InitMessage)message);
		}
		else if(message instanceof MoveMessage) {
			move((MoveMessage)message);		
		}
		else if(message instanceof QuitMessage) {
			quit((QuitMessage)message);
		}
		else if(message instanceof RegisterMessage) {
			register((RegisterMessage)message);
		}
		else if(message instanceof StartMessage) {
			start((StartMessage)message);
		}
		else if(message instanceof TalkMessage) {
			talk((TalkMessage)message);
		}
		else if(message instanceof TurnMessage) {
			turn((TurnMessage)message);
		}		
		else if(message instanceof WinMessage) {
			win((WinMessage)message);
		}	
	}

	
	public void connect(String host, int port) {
		try {
			Socket sock = new Socket(host, port);
			socket = new SocketIO(SERVER, sock);
			thread = new SocketThread(this, socket);
			thread.start();
			agent.restart();
			command(new HelloMessage(AGENT + " " + AGENT_PLAYERS));
		} 
		catch (UnknownHostException e) {
			System.err.println("Can't find host.");
			System.exit(1);
		} 
		catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection");
			System.exit(1);
		}
	}
	
	public void socketError() {
		close();
		QuitMessage message = new QuitMessage("Client socket error");
		socketMessage(message);
		notifyListeners(message);
	}
	
	public void socketClose() {
		close();
		QuitMessage message = new QuitMessage("Client socket close");
		socketMessage(message);
		notifyListeners(message);
	}
	
	public void close() {
		agent.stop();
		if(socket != null) {
			try {
				socket.close();
			}
			catch(IOException e) {
			}
			socket = null;
		}
		thread = null;
	}
	
	protected void connect(ConnectMessage message) {
		
	}
	
	protected void exception(ExceptionMessage message) {
		agent.logError(message.exception);
	}
	
	protected void hello(HelloMessage message) {
		StringTokenizer tok = new StringTokenizer(message.info);		
		id = Integer.valueOf(tok.nextToken()).intValue();
		if(id < 0) {
			StringBuffer buff = new StringBuffer();
			while(tok.hasMoreTokens()) {
				buff.append(tok.nextToken() + " ");
			}
			buff.deleteCharAt(buff.length()-1);
			System.err.println("Unable to connect: " + buff.toString());
			close();
		}
	}
	
	protected void init(InitMessage message) {
		quoridor.tell(message);
		while(quoridor.query() != null) {}
	}

	protected void move(MoveMessage message) {
		quoridor.tell(message);
		while(quoridor.query() != null) {}
	}
	
	protected void quit(QuitMessage message) {
		
	}	
	
	protected void register(RegisterMessage message) {
		quoridor.tell(message);
		while(quoridor.query() != null) {}
	}
	
	protected void start(StartMessage message) {
		if(quoridor.board().phase() != BoardState.PREGAME) {
			agent.restart();
		}
		quoridor.tell(message);
		while(quoridor.query() != null) {}
	}
	
	protected void talk(TalkMessage message) {
		command(new TalkMessage("I am Agent " + agent.id()));
	}
		
	protected void turn(TurnMessage message) {
		if(id == message.next) {
			MoveMessage move = new MoveMessage(agent.turn((AIBoard)quoridor.board(), message.timelimit)); 
			move.move.userid = id;
			command(move);
		}
	}
	
	protected void win(WinMessage message) {
		boolean win = (message.winner == id)? true : false;
	}	
	
}
