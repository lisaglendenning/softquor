/*
 * Created on Nov 1, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package softquor;
import softquor.board.*;

import java.net.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.ListIterator;

/**
 * @author lglenden
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TextClient implements Client {
	
	private Quoridor quoridor;
	private BufferedReader in;
	private PrintWriter out;		
	private SocketIO socket;
	private SocketThread messageThread;
	private CommandThread commandThread;
	private int id;
	
	public TextClient() {
		out = new PrintWriter(System.out, true);
		in = new BufferedReader(new InputStreamReader(System.in));
		messageThread = null;
		commandThread = null;
		quoridor = new Quoridor();
		socket = null;
		id = 0;
	}
	
	public void addListener(ClientListener listener) {}
	
	public void socketMessage(Message message) {
		message(message);
	}
	
	public void socketError() {
		if(socket != null) {
			message(new QuitMessage("Socket error"));
		}
	}
	
	public void socketClose() {
		if(socket != null) {
			message(new QuitMessage("Connection closed"));
		}
	}
	
	public static void main(String[] args) {

		TextClient client = new TextClient();

		String host = args[0];
		int port = Integer.valueOf(args[1]).intValue();
		
		client.connect(host, port);
	}
	
	protected synchronized void message(Message message) {
		if(message instanceof ConnectMessage) {
			connect((ConnectMessage)message);
		}
		else if(message instanceof ExceptionMessage) {
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
			println("Welcome to Quoridor!\n" +
				"Type 'help'<enter> to see available commands.\n\n");
			messageThread = new SocketThread(this, socket);
			messageThread.start();
			commandThread = new CommandThread(this);
			commandThread.start();
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
	
	public void close() {
		if(socket != null) {
			try {
				socket.close();
			}
			catch(IOException e) {
			}
			socket = null;
		}	
		messageThread = null;
	}
	
	public Board board() {
		synchronized(quoridor) {
			return quoridor.board();
		}
	}
	
	public String read() throws IOException {
		return in.readLine();
	}
	
	public void prompt() {
		if(id > 0) {
			print("Player " + id);
		}
		print("> ");
		flush();
	}
	
	public void print(String str) {
		out.print(str);
	}
	
	public void flush() {
		out.flush();
	}

	public void println(String str) {
		out.println();
		out.println(str);
		prompt();
	}	
	
	public synchronized void command(Message message) {
		//System.err.println("Command!");
		message.to = SERVER;
		message.from = id;
		if(socket != null) {
			socket.write(message.protocol());
		}
	}
	
	class CommandThread extends Thread {

		public TextClient client;
		
		public CommandThread(TextClient client) {
			this.client = client;
		}
		
		public void run() {
			try {
				boolean reading = true;
				while(reading) {
					client.prompt();
					String command = client.read();
					StringTokenizer tok = new StringTokenizer(command);
					Vector words = new Vector();
					while(tok.hasMoreTokens()) {
						words.add(tok.nextToken());
					}
					
					if(words.size() < 1) {
						continue;
					}
					
					String firstword = (String)words.get(0);
					
					if(firstword.equals("quit")) {
						client.command(new QuitMessage("User request"));
						continue;
					}
					
					if(firstword.equals("hello")) {
						if(words.size() < 2) {
							userError();
							continue;
						}
						int who = Integer.valueOf((String)words.get(1)).intValue();
						client.command(new HelloMessage(HUMAN + " " + who));
					}
					
					else if(firstword.equals("init")) {
						if(words.size() < 5) {
							userError();
							continue;
						}
						int rows = Integer.valueOf((String)words.get(1)).intValue();
						int cols = Integer.valueOf((String)words.get(2)).intValue();
						int walls = Integer.valueOf((String)words.get(3)).intValue();
						int timelimit = Integer.valueOf((String)words.get(4)).intValue();
						client.command(new InitMessage(rows, cols, walls, timelimit));
					}
					
					else if(firstword.equals("help")) {
						doHelp();
					}		
					
					else if(firstword.equals("start")) {
						if(words.size() < 2) {
							userError();
							continue;
						}
						try {
							int firstplayer = Integer.valueOf((String)words.get(1)).intValue();
							client.command(new StartMessage(firstplayer));		
						}
						catch(NumberFormatException e) {
							userError();
							continue;
						}			
					}	
					
					else if(firstword.equals("legal")) {
						Vector movewords = new Vector();
						ListIterator lit = words.listIterator(1);
						while(lit.hasNext()) {
							movewords.add(lit.next());
						}
						Move move = parseMove(movewords);
						if(move != null) {
							if(client.board().legal(move)) {
								client.println("Legal move.");
							}
							else {
								client.println("Illegal move.");
							}
						}
						else {
							userError();
							continue;
						}			
					}	
					
					else {
						Move move = parseMove(words);
						if(move != null) {
							client.command(new MoveMessage(move));
						}
						else {
							userError();
						}
					}				
				}
			}
			catch(Exception e) {
				client.command(new QuitMessage("Reading error: " + e.getMessage()));
			}
		}

		private Move parseMove(Vector words) {
			if(words.get(0).equals("walk")) {
				if(words.size()==3) {
					try {
						int x = Integer.valueOf((String)words.get(1)).intValue();
						int y = Integer.valueOf((String)words.get(2)).intValue();
						Cell to = board().cells().get(x, y);
						Cell from = board().player(board().nextPlayer()).position;
						return new WalkMove(board().nextPlayer(), from, to);				
					}
					catch(NumberFormatException e) {
					}	
				}		
			}
			else if(words.get(0).equals("wall")) {
				if(words.size()==4) {
					try {
						int x = Integer.valueOf((String)words.get(1)).intValue();
						int y = Integer.valueOf((String)words.get(2)).intValue();
						boolean horizontal = words.get(3).equals("h") ? true : false;
						Wall wall = new Wall(new Cell(x, y), horizontal);
						return new WallMove(board().nextPlayer(), wall);
					}
					catch(NumberFormatException e) {
					}				
				}
			}
			return null;
		}

		private void userError() {
			client.println("Unknown command");
			client.flush();
		}

		private void doHelp() {
			client.println("\nCommands:\n" + "* init <rows> <cols> <walls> <timelimit>\n" +
				"* hello <who>\n" + "* start <first>\n" +
				"* walk <x> <y>\n" + "* wall <x> <y> <h/v>\n" + "* legal <walk or wall syntax>\n"
				+ "* help\n" + "* quit\n");
		}
		
	}
	
	protected void connect(ConnectMessage message) {
		println("Player " + message.who + " has connected.");		
	}
	
	protected void exception(ExceptionMessage message) {
		println("Error: " + message.exception);
	}
	
	protected void hello(HelloMessage message) {
		StringTokenizer tok = new StringTokenizer(message.info);		
		id = Integer.valueOf(tok.nextToken()).intValue();
		if(id >= 0) {
			println("You are connected as Player " + id);
		}
		else {
			StringBuffer buff = new StringBuffer();
			while(tok.hasMoreTokens()) {
				buff.append(tok.nextToken() + " ");
			}
			buff.deleteCharAt(buff.length()-1);
			println("Unable to connect: " + buff.toString());
			close();
		}
	}
	
	protected void init(InitMessage message) {
		quoridor.tell(message);
		while(quoridor.query() != null) {}
		println("Board initialized");
	}
	
	protected void move(MoveMessage message) {
		quoridor.tell(message);
		while(quoridor.query() != null) {}
		displayBoard();
	}
	
	protected void quit(QuitMessage message) {
		close();
		try {
			in.close();
		}
		catch(IOException e) {}
		commandThread = null;
		println("Game is terminated: " + message.reason);
	}
		
	protected void register(RegisterMessage message) {
		if(message.gameid == id) {
			println("You are registered as Player " + message.gameid);
		}
		else {
			println("Player " + message.gameid + " has registered");			
		}
		quoridor.tell(message);
		while(quoridor.query() != null) {}
	}
	
	protected void start(StartMessage message) {
		quoridor.tell(message);
		while(quoridor.query() != null) {}
		println("Game started!");
		displayBoard();
	}
	protected void talk(TalkMessage message) {		
		println("Player " + message.from + ": " + message.message);
	}

	protected void turn(TurnMessage message) {
		println("Player " + message.next + "'s turn.");
	}
	
	protected void win(WinMessage message) {
		if(message.winner == id) {
			println("You won!");
		}
		else {
			println("Game over.  Player " + message.winner + " won!");
		}
	}
	
	private void displayBoard() {
		println(board().toText(0));
	}
	
}
