/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package softquor;
import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.ListIterator;
import java.util.StringTokenizer;

/**
 * @author shade
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Server implements ClientListener {

	static public  int TIMEOUT = 5*60*1000; // 5 minutes	
	private boolean debug;
	private ServerSocket socket;
	private Vector groups[];
	private boolean listening;
		
	public Server(boolean debug) {
		this.debug = debug;
		socket = null;
		groups = new Vector[2];
		groups[Client.HUMAN] = new Vector();
		groups[Client.AGENT] = new Vector();
		listening = true;
	}
	
	public boolean listening() {
		return listening;
	}
	
	public void open(int port) {
        socket = null;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            System.exit(1);
        }		
	}
	
	public void listenEvent(ClientEvent event) {
		if(event.getCommand().equals("remove")) {
			groupRemove((ClientGroup)event.getSource());
		}
	}
	
	public void groupRemove(ClientGroup group) {
		Vector selectedGroup = groups[group.who()];
		synchronized(selectedGroup) {
			ClientGroup lastGroup = (ClientGroup)selectedGroup.lastElement();
			selectedGroup.remove(lastGroup.id());
			if(group.id != lastGroup.id) {
				selectedGroup.remove(group.id);
				selectedGroup.add(group.id, lastGroup);
				lastGroup.id(group.id);
			}			
		}
	}

	public void client(SocketIO socket, HelloMessage hello) {
		try {
			StringTokenizer tok = new StringTokenizer(hello.info);
			int who = Integer.valueOf(tok.nextToken()).intValue();
			int request = Integer.valueOf(tok.nextToken()).intValue();
			if(who == Client.HUMAN) {
				if(request == Client.AGENT) {
					synchronized(groups[Client.AGENT]) {
						ListIterator lit = groups[Client.AGENT].listIterator();
						while(lit.hasNext()) {
							ClientGroup group = (ClientGroup)lit.next();
							if(!group.full()) {
								group.client(socket);
								return;
							}
						}
						HelloMessage except = new HelloMessage("-1 no open Agents");
						socket.write(except.protocol());
						try {
							socket.close();
						}
						catch(IOException io) {
						}
					}					
				}
				else if(request==2 || request==4) {
					synchronized(groups[Client.HUMAN]) {
						ListIterator lit = groups[Client.HUMAN].listIterator();
						while(lit.hasNext()) {
							ClientGroup group = (ClientGroup)lit.next();
							if(!group.full() && group.numPlayers==request) {
								group.client(socket);
								return;
							}
						}
						int id = groups[Client.HUMAN].size();
						ClientGroup group = new ClientGroup(this, id, Client.HUMAN, request);
						group.addListener(this);
						groups[Client.HUMAN].add(group);
						group.client(socket);
					}
				}
				else {
					throw new Exception();
				}
			}
			else if(who == Client.AGENT) {
				synchronized(groups[Client.AGENT]) {
					int id = groups[Client.AGENT].size();
					ClientGroup group = new ClientGroup(this, id, Client.AGENT, request);
					group.addListener(this);
					groups[Client.AGENT].add(group);
					group.client(socket);
				}
			}
			else {
				throw new Exception();
			}
		}
		catch(Exception e) {
			HelloMessage except = new HelloMessage("-1 unrecognized HelloMessage");
			socket.write(except.protocol());
			try {
				socket.close();
			}
			catch(IOException io) {
			}
		}
	}
	
	public void listen() {
		ListenThread listen = new ListenThread(this, socket);
		listen.start();
	}
	
	public void close() throws IOException {		
		synchronized(groups) {
			for( int i=Client.HUMAN;  i<=Client.AGENT;  i++ ) {
				ListIterator lit = (groups[i]).listIterator();
				while(lit.hasNext()) {	
					ClientGroup group = (ClientGroup)lit.next();
					group.close();
				}
			}
		}
	}
	
    public static void main(String[] args) throws IOException {
    	int port = Integer.valueOf(args[0]).intValue();
    	Server server = new Server(false);
    	server.open(port);
    	server.listen();
    }
    
    class ClientGroup implements Client {
    		
    	private int id;
    	private Vector listeners;
       	private Vector clients;
       	private Quoridor quoridor;
       	private int numPlayers;
       	private boolean valid;
       	private int who;
       	
       	public ClientGroup(Server server, int id, int who, int numPlayers) {
       		this.id = id;
       		this.who = who;
       		this.listeners = new Vector();
       		this.clients = new Vector();
       		this.quoridor = new Quoridor();
       		this.numPlayers = numPlayers;
       		this.valid = true;
       	}
       	
       	public int who() {
       		return who;
       	}
       	
       	public boolean full() {
       		return (numPlayers == clients.size());
       	}
       	
       	public int id() {
       		return id;
       	}
       	
       	public int numPlayers() {
       		return numPlayers;
       	}
       	
       	public void id(int newid) {
       		id = newid;
       	}
       	
       	protected void notifyListeners(ClientEvent e) {
       		ListIterator lit = listeners.listIterator();
       		while(lit.hasNext()) {
       			ClientListener next = (ClientListener)lit.next();
       			next.listenEvent(e);
       		}
       	}
     
       	public void remove() {
       		notifyListeners(new ClientEvent(this, "remove"));
       	}
       	
       	public void close() {
       		if(clients != null) {
       			synchronized(clients) {
       				try {
       					ListIterator lit = clients.listIterator();
       					while(lit.hasNext()) {
       						SocketIO client = (SocketIO)lit.next();
       						client.close();
       					}
       					clients = null;
       				} 	   
       				catch(IOException e) {
       				}
       			}
       		}
       	}
       	
       	public void addListener(ClientListener listener) {
       		listeners.add(listener);
       	}
       	
       	public void socketMessage(Message message) {
       		if(debug) {
       			System.out.println("Message: " + message.protocol());
       		}
       		if(message instanceof InitMessage) {
       			ListIterator lit = clients.listIterator();
       			while(lit.hasNext()) {
   					SocketIO next = (SocketIO)lit.next();
   					next.cancelTimer();
   					next.startTimer(TIMEOUT);
       			}
       		}
       		else if(message instanceof StartMessage) {
       			ListIterator lit = clients.listIterator();
       			while(lit.hasNext()) {
   					SocketIO next = (SocketIO)lit.next();
   					next.cancelTimer();
       			}
       		}
       		else if(message instanceof MoveMessage) {
       			SocketIO client = (SocketIO)clients.get(message.from);
       			client.cancelTimer();
       		}
       		
   			if(message instanceof TalkMessage) {
   				send(message);
   				return;
   			}
   			
       		if(message instanceof QuitMessage) {
       			if(valid) {
       				valid = false;
       				message.from = Client.SERVER;
       				message.to = Client.BROADCAST;
       				send(message);
       				close();
       				remove();
       				return;
       			}
       		}

       		quoridor.tell(message);	
       		Message response = quoridor.query();
       		while(response != null) {
       			response.from = Client.SERVER;
       			response.to = Client.BROADCAST;
       			send(response);
       			response = quoridor.query();
       		}
       		if(message instanceof InitMessage) {
       			ListIterator lit = clients.listIterator();
       			while(lit.hasNext()) {
       				SocketIO next = (SocketIO)lit.next();
       				RegisterMessage register = new RegisterMessage(next.id);
       				register.to = Client.SERVER;
       				register.from = Client.SERVER;
       				socketMessage(register);
       			}
       		} 
       	}
       	
       	public void socketError() {
    		if(valid) {
    			socketMessage(new QuitMessage("Server socket error"));
    		}
       	}
       	
       	public void socketClose() {
       		if(valid) {
       			socketMessage(new QuitMessage("Connection closed"));
       		}
       	}
       	
       	public void client(SocketIO client) {
       		synchronized(clients) {
       			if(full()) {
       				return;
       			}
       			
       			int clientid = clients.size();
       			client.id = clientid;
       			clients.add(client);
       			SocketThread thread = new SocketThread(this, client);
       			thread.start();
       			
       			// tell new client hello
       			Message message = new HelloMessage("" + clientid);
       			message.from = Client.SERVER;
       			message.to = clientid;
       			send(message);
       			
       			// tell other clients there is a new connection
       			ListIterator lit = clients.listIterator();
       			while(lit.hasNext()) {
       				SocketIO next = (SocketIO)lit.next();
       				if(next.id != clientid) {
       					message = new ConnectMessage(clientid);
       					message.from = Client.SERVER;
       					message.to = next.id;
       					send(message);
       				}
       			}
       			
       			// tell new client about other clients
       			lit = clients.listIterator();
       			while(lit.hasNext()) {
       				SocketIO next = (SocketIO)lit.next();
       				if(next.id != clientid) {
       					message = new ConnectMessage(next.id);
       					message.from = Client.SERVER;
       					message.to = clientid;
       					send(message);
       				}
       			}
       			
       			if(full()) {
       				lit = clients.listIterator();
       				while(lit.hasNext()) {
       					SocketIO next = (SocketIO)lit.next();
       					next.startTimer(TIMEOUT);
       				}
       			}
       		}     		
       	}
       	
       	public void send(Message message) {
       		if(debug) {
       			System.out.println("Send: " + message.protocol());
       		}
       		if(message instanceof TurnMessage) {
       			TurnMessage turn = (TurnMessage)message;
       			SocketIO next = (SocketIO)clients.get(turn.next);
       			next.startTimer(TIMEOUT);
       		}
       		String out = message.protocol();
       		if(message.to == Client.BROADCAST) {
       			ListIterator lit = clients.listIterator();
       			while(lit.hasNext()) {
           			SocketIO client = (SocketIO)lit.next();
           			if(client.id != message.from) {
           				client.write(out);
           			}
       			}
       		}
       		else {
       			SocketIO client = (SocketIO)clients.get(message.to);
       			client.write(out);
       		}
       	}
       	
    }
    
    class HelloThread extends Thread {
		public Server server;
		public SocketIO client;
	
		public HelloThread(Server server, Socket socket) throws IOException {
			this.server = server;
			this.client = new SocketIO(-1, socket);
		}	
		
		public void run() {
			String inputLine;
			Message message;
			boolean hello = false;

			try {
				client.startTimer(TIMEOUT);
				while (!hello && (inputLine = client.read()) != null) {
					message = Message.createMessage(inputLine);
					if(message instanceof HelloMessage) {
						client.cancelTimer();
						hello = true;
						HelloMessage helloMessage = (HelloMessage)message;
						server.client(client, helloMessage);
					}
		    	}
		    }
			catch(SocketTimeoutException e) {
				message = new ExceptionMessage("HelloMessage Timeout: " + TIMEOUT + " ms");
				message.from = Client.SERVER;
				client.write(message.protocol());
				try {
					client.close();
				}
				catch(IOException io) {
				}				
			}
			catch(IOException e) {
				try {
					client.close();
				}
				catch(IOException io) {
				}
			}
		}    	
    }
    
    class ListenThread extends Thread {
        private ServerSocket socket;
        private Server server;
        
        public ListenThread(Server server, ServerSocket socket) {
        		super("ServerListenThread");
        		this.socket = socket;
        		this.server = server;
        }

        public void run() {
        		try {
        			Socket client = null;
        			while(server.listening()) {
        				client = socket.accept();
        				try {
        					HelloThread hello = new HelloThread(server, client);
        					hello.start();
        				}
        				catch(IOException e) {
        				} 
        			}
        		}
        		catch (IOException e) {
        			// do what?
        		}
        }
    		
    }
	
}
