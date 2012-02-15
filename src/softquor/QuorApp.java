package softquor;
import softquor.board.*;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.ListIterator;

import javax.swing.*;

/*
 * Created on Jan 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author shade
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class QuorApp extends JApplet implements ActionListener, Client {
	// GUI components
	private DisplayWidget displayWidget;
	private ConnectWidget connectWidget;
	private OptionWidget optionWidget;
	private PlayersWidget playersWidget;
	private CommandWidget commandWidget;
	private Vector icons;
	
	// game components
	private int id;
	private Quoridor quoridor;
	private boolean myTurn;
	private int numRequested;
	private Vector hotCells;
	
	// net components
	private int numConnected;
	private SocketIO socket;
	private SocketThread thread;
	
	public int id() {
		return id;
	}
	
	public boolean myTurn() {
		return myTurn;
	}
	
	public ImageIcon icon(int player) {
		return (ImageIcon)icons.get(player);
	}
	
	public void init() {
		id = -1;
		myTurn = false;
		numConnected = 0;
		numRequested = 0;
		hotCells = new Vector();
		quoridor = new Quoridor();
		socket = null;
		thread = null;
		
		try {
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					initGUI();
				}
			});
		} catch (Exception e) {
			System.err.println("initGUI didn't successfully complete");
		}  	
	}
	
	public void connect(String host, int port, int who) {
		try {
			Socket sock = new Socket(host, port);
			socket = new SocketIO(SERVER, sock);
			info("Welcome to Quoridor!");
			numRequested = (who == AGENT) ? 2 : who;
			thread = new SocketThread(this, socket);
			thread.start();
			command(new HelloMessage(HUMAN + " " + who));
		} 
		catch (UnknownHostException e) {
			info("Can't find host.");
		} 
		catch (IOException e) {
			info(e.toString());
		}		
	}

	public void command(Message message) {
		message.from = id;
		if(message instanceof MoveMessage) {
			MoveMessage move = (MoveMessage)message;
			if(!board().legal(move.move)) {
				info("Illegal move.");
				return;
			}
			myTurn = false;
			displayMove(move);
		}
		if(socket != null) {
			socket.write(message.protocol());		
		}
	}
	
	public void addListener(ClientListener listener) {}
	
	public void socketMessage(Message message) {
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
	
	protected void connect(ConnectMessage message) {
		info("Player " + message.who + " has connected");
		numConnected++;
	}

	protected void exception(ExceptionMessage message) {
		info("Error: " + message.exception);
	}
	
	protected void hello(HelloMessage message) {
		StringTokenizer tok = new StringTokenizer(message.info);		
		id = Integer.valueOf(tok.nextToken()).intValue();
		if(id >= 0) {
			info("You are connected as Player " + id);
			numConnected++;
		}
		else {
			StringBuffer buff = new StringBuffer();
			while(tok.hasMoreTokens()) {
				buff.append(tok.nextToken() + " ");
			}
			buff.deleteCharAt(buff.length()-1);
			info("Unable to connect: " + buff.toString());
			close();
		}
	}	
	
	protected void init(InitMessage message) {
		quoridor.tell(message);
		while(quoridor.query() != null) {}
		info("Board initialized");
		displayWidget.callInit(message.rows, message.cols);
	}
	
	protected void move(MoveMessage message) {
		quoridor.tell(message);
		while(quoridor.query() != null) {}
		if(message.move.userid != id) {
			displayMove(message);
		}
	}
	
	protected void displayMove(MoveMessage message) {
		if(message.move instanceof WallMove) {
			WallMove move = (WallMove)message.move;
			playersWidget.subWall(move.userid);
			displayWidget.wall(move.wall);
		}
		else {
			WalkMove move = (WalkMove)message.move;
			displayWidget.callWalk(move);
		}
	}
	
	protected void quit(QuitMessage message) {
		quoridor.tell(message);
		while(quoridor.query() != null) {}
		numConnected = 0;
		info(message.reason);
		getContentPane().remove(playersWidget);
		playersWidget = new PlayersWidget(this);
		getContentPane().add(playersWidget, BorderLayout.LINE_START);
		repaint();
		//close();
	}
	
	protected void register(RegisterMessage message) {
		quoridor.tell(message);
		while(quoridor.query() != null) {}
		info("Player " + message.gameid + " has registered");
	}
	
	protected void start(StartMessage message) {
		URL iconURLs[] = new URL[4];
		try {
			iconURLs[0] = new URL("http://www.cs.unm.edu/~lglenden/quoridor/black.png");	
			iconURLs[1] = new URL("http://www.cs.unm.edu/~lglenden/quoridor/white.png");	
			iconURLs[2] = new URL("http://www.cs.unm.edu/~lglenden/quoridor/red.png");	
			iconURLs[3] = new URL("http://www.cs.unm.edu/~lglenden/quoridor/blue.png");	
		}
		catch(MalformedURLException e) {}
		
		int phase = board().phase();
		quoridor.tell(message);
		while(quoridor.query() != null) {}
		info("Game started!");
		
		icons = new Vector();
		for( int i=0;  i<board().numPlayers();  i++ ) {
			icons.add(new ImageIcon(iconURLs[i]));
		}
		ImageIcon blackIcon = (ImageIcon)icons.get(0);
		icons.set(0, icons.get(message.first));
		icons.set(message.first, blackIcon);
		
		if(phase == BoardState.PREGAME) {				
			playersWidget.callStart();
			displayWidget.callStart();
		}
		else {
			getContentPane().remove(playersWidget);
			playersWidget = new PlayersWidget(this);
			playersWidget.callStart();
			getContentPane().add(playersWidget, BorderLayout.LINE_START);
			displayWidget.callRestart();
		}
	}
	
	protected void talk(TalkMessage message) {
		if(message.from == id) {
			info("You: " + message.message);
		}
		else {
			info("Player " + message.from + ": " + message.message);
		}
	}
	
	protected void turn(TurnMessage message) {
		if(message.next == id) {
			info("Your turn");
			myTurn = true;			
			setHot();
		}
		else {
			info("Player " + message.next + "'s turn");
		}
	}
	
	protected void win(WinMessage message) {
		info("Player " + message.winner + " won!");
	}
	
	public void socketError() {
		close();
		quit(new QuitMessage("socket error"));
	}
	
	public void socketClose() { 
		close();
		quit(new QuitMessage("Connection closed"));
	}
	
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		if(command.equals("init")) {
			if(socket == null) {
				info("Not connected to server.");
				return;
			}
			if(board() != null) {
				info("Board already initialized.");
				return;
			}
			if(numConnected != numRequested) {
				info("Waiting for more players to connect.");
				return;
			}
			command(new InitMessage(optionWidget.rows(), optionWidget.cols(), 
    				optionWidget.walls()*numRequested, optionWidget.timelimit()));
		}
		else if(command.equals("quit")) {
			if(socket == null) {
				info("Not connected to server.");
				return;
			}
			command(new QuitMessage("Player " + id + " quit."));
		}
		else if(command.equals("start")) {
			if(socket == null) {
				info("Not connected to server.");
				return;
			}
			if(board() == null) {
				info("Board not initialized.");
				return;
			}
			if(board().numPlayers() != numRequested) {
				info("Waiting for more players to register.");
				return;
			}
			if(optionWidget.black()) {
				command(new StartMessage(id));
			}
			else {
				command(new StartMessage(quoridor.board().getOpponent(id)));
			}
        }
		else if(command.equals("connect")) {
			if(socket != null) {
				info("Already connected.");
				return;
			}
			connect(connectWidget.host(), connectWidget.port(), connectWidget.who());
		}
		else {
			TalkMessage message = new TalkMessage(commandWidget.talk());
			commandWidget.clear();
			if(socket == null) {
				info("Not connected to server.");
				return;
			}
			message.to = commandWidget.playerTo();
			message.from = id();
			socketMessage(message);
			command(message);
		}
    }
	
	public void info(String str) {
		optionWidget.infoWidget.add(str);
	}
	
	public Board board() {
		synchronized(quoridor) {
			return quoridor.board();
		}
	}
	
	public boolean hot(int x, int y) {
		return hotCells.contains(new Cell(x, y));
	}
	
	private void setHot() {
		synchronized(quoridor) {
			synchronized(hotCells) {
				hotCells = new Vector();
				Player player = quoridor.board().player(id);
				Vector walks = quoridor.board().legalWalkMoves(player);
				ListIterator lit = walks.listIterator();
				while(lit.hasNext()) {
					WalkMove move = (WalkMove)lit.next();
					hotCells.add(move.to);
				}
			}
		}			
	}
	
	protected void initGUI() {	
		getContentPane().setLayout(new BorderLayout(10, 10));
		
		connectWidget = new ConnectWidget(this, "kappa.cs.unm.edu", 8080, 2);
		getContentPane().add(connectWidget, BorderLayout.PAGE_START);
		
		playersWidget = new PlayersWidget(this);
		getContentPane().add(playersWidget, BorderLayout.LINE_START);
		
		displayWidget = new DisplayWidget(this);
		getContentPane().add(displayWidget, BorderLayout.CENTER);
  	
		optionWidget = new OptionWidget(this, 9, 9, 10, 3000);
		getContentPane().add(optionWidget, BorderLayout.LINE_END);
 	
		commandWidget = new CommandWidget(this);
		getContentPane().add(commandWidget, BorderLayout.PAGE_END); 	
	}
  
	public void start() {
	}
  
	public void stop() {
	}
	
	class CommandWidget extends JPanel {
		
		public JTextField talkField;
		public JTextField toField;
		
		public CommandWidget(QuorApp app) {
			setBorder(BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.add(Box.createRigidArea(new Dimension(5, 0)));
			panel.add(new JLabel("Player"));
			panel.add(Box.createRigidArea(new Dimension(5, 0)));
			toField = new JTextField("", 2);
			toField.setMaximumSize(toField.getPreferredSize());
			panel.add(toField);
			panel.add(Box.createRigidArea(new Dimension(5, 0)));
			panel.add(new JLabel("Talk"));
			panel.add(Box.createRigidArea(new Dimension(5, 0)));
			talkField = new JTextField("", 40);
			talkField.setMaximumSize(talkField.getPreferredSize());
			talkField.addActionListener(app);
			panel.add(talkField);
			panel.add(Box.createHorizontalGlue());
			add(Box.createRigidArea(new Dimension(0, 5)));
			add(panel);
			add(Box.createRigidArea(new Dimension(0, 5)));
		}
		
		public void clear() {
			talkField.setText("");
		}
		
		public String talk() {
			return talkField.getText();
		}
		
		public int playerTo() {
			try {
				return Integer.valueOf(toField.getText()).intValue();		
			}
			catch(NumberFormatException e) {
				return BROADCAST;
			}
		}
	}
	
	class PlayersWidget extends JPanel {
		public QuorApp app;
		public Vector players;
		
		public PlayersWidget(QuorApp app) {
			this.app = app;
			this.players = new Vector();
			setBorder(BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		}
		
		public void callStart() {
			SwingUtilities.invokeLater(
					new PlayerStart(this));
		}
		
		class PlayerStart implements Runnable {
			public PlayersWidget widget;
			
			public PlayerStart(PlayersWidget widget) {
				this.widget = widget;
			}
			
			public void run() {
				widget.start();
			}
		}
		
		public void start() {
			int nplayers = app.board().numPlayers();
			int nwalls = app.board().nwalls(app.id());

			for( int i=0;  i<nplayers;  i++ ) {
				players.add(new PlayerWidget(i, nwalls, app.icon(i)));
			}
			add(Box.createRigidArea(new Dimension(0, 5)));
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.add(Box.createRigidArea(new Dimension(5, 0)));
			panel.add((Component)players.get(app.id()));
			panel.add(Box.createRigidArea(new Dimension(5, 0)));
			add(panel);
			for( int i=0;  i<nplayers;  i++) {
				if(i != app.id()) {
					add(Box.createRigidArea(new Dimension(0, 5)));
					panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
					panel.add(Box.createRigidArea(new Dimension(5, 0)));
					panel.add((Component)players.get(i));
					panel.add(Box.createRigidArea(new Dimension(5, 0)));
					add(panel);
				}
			}
			add(Box.createVerticalGlue());
		}
		
		public void subWall(int playerid) {
			((PlayerWidget)players.get(playerid)).subWall();
		}
		
		public int numWalls(int playerid) {
			return ((PlayerWidget)players.get(playerid)).numWalls();
		}
		
		class PlayerWidget extends JPanel {
			public JLabel wallLabel;
			public int id;
			public int nwalls;
			
			public PlayerWidget(int id, int nwalls, ImageIcon icon) {
				this.id = id;
				this.nwalls = nwalls;
				
				setBorder(BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
				setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
				add(new JLabel(icon));
				add(new JLabel("Player " + id));
				wallLabel = new JLabel("Walls: " + nwalls);
				add(wallLabel);
			}
			
			public void subWall() {
				nwalls--;
				wallLabel.setText("Walls: " + nwalls);
			}
			
			public int numWalls() {
				return nwalls;
			}
		}
	}
	
	class ConnectWidget extends JPanel {
		
		public JTextField hostField;
		public JTextField portField;
		public JComboBox whoField;
		public String[] whoStrings = { "2 Humans", "4 Humans", "Agent" };
		public int[] who = { 2, 4, AGENT};
		
		public ConnectWidget(QuorApp app, String host, int port, int nplayers) {
			setBorder(BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			add(Box.createRigidArea(new Dimension(0, 5)));
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.add(Box.createRigidArea(new Dimension(10, 0)));
			hostField = new JTextField(host, 20);
			hostField.setMaximumSize(hostField.getPreferredSize());
			portField = new JTextField("" + port, 5);
			portField.setMaximumSize(portField.getPreferredSize());
			whoField = new JComboBox(whoStrings);
			whoField.setSelectedIndex(0);
			whoField.setMaximumSize(whoField.getPreferredSize());
			panel.add(new JLabel("Hostname"));
			panel.add(Box.createRigidArea(new Dimension(5, 0)));
			panel.add(hostField);
			panel.add(Box.createRigidArea(new Dimension(10, 0)));
			panel.add(new JLabel("Port"));
			panel.add(Box.createRigidArea(new Dimension(5, 0)));
			panel.add(portField);
			panel.add(Box.createRigidArea(new Dimension(10, 0)));
			panel.add(new JLabel("Game"));
			panel.add(Box.createRigidArea(new Dimension(5, 0)));
			panel.add(whoField);
			panel.add(Box.createRigidArea(new Dimension(10, 0)));
			JButton connect = new JButton("Connect");
			connect.setActionCommand("connect");
			connect.addActionListener(app);
			panel.add(connect);
			panel.add(Box.createHorizontalGlue());
			add(panel);
			add(Box.createRigidArea(new Dimension(0, 5)));
		}
		
		public String host() {
			return hostField.getText();
		}
		
		public int port() {
			return Integer.valueOf(portField.getText()).intValue();
		}
		
		public int who() {
			String selected = (String)whoField.getSelectedItem();
			if(selected.equals(whoStrings[0])) {
				return who[0];
			}
			else if(selected.equals(whoStrings[1])) {
				return who[1];
			}
			return who[2];
		}
	}
	
	class OptionWidget extends JPanel {
	
		public JTextField rowsField;
		public JTextField colsField;
		public JTextField wallsField;
		public JCheckBox blackBox;
		public JTextField timeField;
		public InfoWidget infoWidget;
		
		public OptionWidget(QuorApp app, int rows, int cols, int walls, int timelimit) {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			JPanel initPanel = new JPanel();
			initPanel.setLayout(new BoxLayout(initPanel, BoxLayout.Y_AXIS));
			initPanel.setBorder(BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
			initPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			JPanel boardPanel = new JPanel();
			boardPanel.setLayout(new BoxLayout(boardPanel, BoxLayout.X_AXIS));
			boardPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			boardPanel.add(new JLabel("Rows"));
			boardPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			rowsField = new JTextField("" + rows, 2);
			rowsField.setMaximumSize(rowsField.getPreferredSize());
			boardPanel.add(rowsField);
			boardPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			boardPanel.add(new JLabel("Cols"));
			boardPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			colsField = new JTextField("" + cols, 2);
			colsField.setMaximumSize(colsField.getPreferredSize());
			boardPanel.add(colsField);
			boardPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			boardPanel.add(new JLabel("Walls"));
			boardPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			wallsField = new JTextField("" + walls, 2);
			wallsField.setMaximumSize(wallsField.getPreferredSize());
			boardPanel.add(wallsField);
			boardPanel.add(Box.createHorizontalGlue());
			initPanel.add(boardPanel);
			
			initPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			JPanel timePanel = new JPanel();
			timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
			timePanel.add(Box.createRigidArea(new Dimension(5, 0)));
			timePanel.add(new JLabel("Turn time limit (millis)"));
			timePanel.add(Box.createRigidArea(new Dimension(5, 0)));
			timeField = new JTextField("" + timelimit, 5);
			timeField.setMaximumSize(timeField.getPreferredSize());
			timePanel.add(timeField);
			timePanel.add(Box.createRigidArea(new Dimension(10, 0)));
			JButton init = new JButton("Initialize");
			init.setActionCommand("init");
			init.addActionListener(app);
			timePanel.add(init);
			timePanel.add(Box.createHorizontalGlue());
			initPanel.add(timePanel);
			initPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			add(initPanel);
			add(Box.createRigidArea(new Dimension(0, 5)));
			
			JPanel startPanel = new JPanel();
			startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.Y_AXIS));
			startPanel.setBorder(BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
			startPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			JPanel radioPanel = new JPanel();
			radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));
			blackBox = new JCheckBox("Black");
			blackBox.setSelected(true);	
			JButton start = new JButton("Start!");
			start.setActionCommand("start");
			start.addActionListener(app);
			JButton quit = new JButton("Quit");
			quit.setActionCommand("quit");
			quit.addActionListener(app);
			radioPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			radioPanel.add(blackBox);
			radioPanel.add(Box.createRigidArea(new Dimension(10, 0)));
			radioPanel.add(start);
			radioPanel.add(Box.createRigidArea(new Dimension(10, 0)));
			radioPanel.add(quit);
			radioPanel.add(Box.createHorizontalGlue());
			startPanel.add(radioPanel);
			startPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			add(startPanel);
			add(Box.createRigidArea(new Dimension(0, 5)));

			infoWidget = new InfoWidget();
			add(infoWidget);
		}
		
		public int rows() {
			return Integer.valueOf(rowsField.getText()).intValue();
		}

		public int cols() {
			return Integer.valueOf(colsField.getText()).intValue();
		}
		
		public int walls() {
			return Integer.valueOf(wallsField.getText()).intValue();
		}
		
		public int timelimit() {
			return Integer.valueOf(timeField.getText()).intValue();
		}
		
		public boolean black() {
			return blackBox.isSelected();
		}
	}

	class DisplayWidget extends JPanel implements MouseListener {
		
		public QuorApp app;
		public BoardWidget boardWidget;
		
		public DisplayWidget(QuorApp app) {
			this.app = app;
			setBorder(BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
			this.boardWidget = null;
		}
		
		public void mouseEntered(MouseEvent e) {
			if(!app.myTurn()) {
				return;
			}
			Component comp = e.getComponent();
			if(comp instanceof BoardWidget.CellWidget.CellCenter) {
				BoardWidget.CellWidget.CellCenter center = (BoardWidget.CellWidget.CellCenter)comp;
				if(app.hot(center.x, center.y)) {
					center.hover();
				}
			}
			else if(comp instanceof BoardWidget.CellWidget.CellWall) {
				if(playersWidget.numWalls(id) < 1) {
					return;
				}
				BoardWidget.CellWidget.CellWall wall = (BoardWidget.CellWidget.CellWall)comp;
				Vector walls = boardWidget.getWalls(wall);
				if(walls != null) {
					boolean ok = true;
					ListIterator lit = walls.listIterator();
					while(lit.hasNext()) {
						BoardWidget.CellWidget.CellWall next = (BoardWidget.CellWidget.CellWall)lit.next();
						if(next.isSet) {
							ok = false;
							break;
						}
					}
					
					if(ok) {
						lit = walls.listIterator();
						while(lit.hasNext()) {
							BoardWidget.CellWidget.CellWall next = (BoardWidget.CellWidget.CellWall)lit.next();
							next.hover();
						}
					}
				}
			}
	    }

		public void mouseExited(MouseEvent e) {
			if(!app.myTurn()) {
				return;
			}
			Component comp = e.getComponent();
			if(comp instanceof BoardWidget.CellWidget.CellCenter) {
				BoardWidget.CellWidget.CellCenter center = (BoardWidget.CellWidget.CellCenter)comp;
				if(app.hot(center.x, center.y)) {
					center.unhover();
				}
			}
			else if(comp instanceof BoardWidget.CellWidget.CellWall) {
				BoardWidget.CellWidget.CellWall wall = (BoardWidget.CellWidget.CellWall)comp;
				Vector walls = boardWidget.getWalls(wall);
				if(walls != null) {
					boolean ok = true;
					ListIterator lit = walls.listIterator();
					while(lit.hasNext()) {
						BoardWidget.CellWidget.CellWall next = (BoardWidget.CellWidget.CellWall)lit.next();
						if(next.isSet) {
							ok = false;
							break;
						}
					}
					
					if(ok) {
						lit = walls.listIterator();
						while(lit.hasNext()) {
							BoardWidget.CellWidget.CellWall next = (BoardWidget.CellWidget.CellWall)lit.next();
							next.unset();
						}
					}
				}
			}
		}
	     
		public void mouseClicked(MouseEvent e) {
			if(!app.myTurn()) {
				return;
			}
			Component comp = e.getComponent();
			if(comp instanceof BoardWidget.CellWidget.CellCenter) {
				BoardWidget.CellWidget.CellCenter center = (BoardWidget.CellWidget.CellCenter)comp;
				if(app.hot(center.x, center.y)) {
					center.unhover();
					int id = app.id();
					Cell from = app.board().player(id).position;
					app.command(new MoveMessage(new WalkMove(id, from, new Cell(center.x, center.y))));
				}
			}
			else if(comp instanceof BoardWidget.CellWidget.CellWall) {
				BoardWidget.CellWidget.CellWall w = (BoardWidget.CellWidget.CellWall)comp;
				Vector walls = boardWidget.getWalls(w);
				if(walls != null) {
					boolean ok = true;
					ListIterator lit = walls.listIterator();
					while(lit.hasNext()) {
						BoardWidget.CellWidget.CellWall next = (BoardWidget.CellWidget.CellWall)lit.next();
						if(next.isSet) {
							ok = false;
							break;
						}
					}
					
					if(ok) {
						Wall wall = boardWidget.getWall(w);
						if(wall != null) {
							app.command(new MoveMessage(new WallMove(app.id(), wall)));
						}
					}
				}
			}
		}

	    public void mousePressed(MouseEvent e) {
	    }

	    public void mouseReleased(MouseEvent e) {
	    }
	     

		
		public void callInit(int rows, int cols) {
			SwingUtilities.invokeLater(
					new BoardInit(this, rows, cols));
		}
		
		public void callStart() {
			SwingUtilities.invokeLater(
					new BoardStart(this));
					
		}
		
		public void callRestart() {
			SwingUtilities.invokeLater(
					new BoardRestart(this));
					
		}
		
		public void clear() {
			boardWidget.clear();
		}
		
		public void start() {
			for( int i=0;  i<app.board().numPlayers();  i++ ) {
				Cell position = app.board().player(i).position;
				boardWidget.pawn(position.x(), position.y(), app.icon(i));
			}
		}
		
		public void callWalk(WalkMove move) {
			SwingUtilities.invokeLater(
					new BoardWalk(this, move));
		}
		
		public void walk(WalkMove move) {
			boardWidget.removePawn(move.from.x(), move.from.y());
			boardWidget.pawn(move.to.x(), move.to.y(), app.icon(move.userid));
		}
		
		public void callWall(Wall wall) {
			SwingUtilities.invokeLater(
					new BoardWall(this, wall));
		}
		
		public void wall(Wall wall) {
			int sides[] = new int[4];
			if(wall.horizontal()) {
				sides[0] = BoardWidget.CellWidget.PAGE_END;
				sides[1] = BoardWidget.CellWidget.PAGE_END;
				sides[2] = BoardWidget.CellWidget.PAGE_START;
				sides[3] = BoardWidget.CellWidget.PAGE_START;
			}
			else {
				sides[0] = BoardWidget.CellWidget.LINE_END;
				sides[1] = BoardWidget.CellWidget.LINE_START;
				sides[2] = BoardWidget.CellWidget.LINE_END;
				sides[3] = BoardWidget.CellWidget.LINE_START;
			}
			boardWidget.setWall(wall.northwest().x(), 
					wall.northwest().y(), sides[0]);
			boardWidget.setWall(wall.northwest().x() + 1, 
					wall.northwest().y(), sides[1]);
			boardWidget.setWall(wall.northwest().x(), 
					wall.northwest().y() + 1, sides[2]);
			boardWidget.setWall(wall.northwest().x() + 1, 
					wall.northwest().y() + 1, sides[3]);
		}
		
		public void init(int rows, int cols) {
			if(boardWidget != null) {
				remove(boardWidget);
			}
			boardWidget = new BoardWidget(this, rows, cols);
			add(boardWidget);
			revalidate();
			repaint();
		}
		
		class BoardWidget extends JPanel {
			
			public CellWidget cells[][];
			public int rows;
			public int cols;
			
			public BoardWidget(MouseListener listener, int rows, int cols) {
				this.rows = rows;
				this.cols = cols;
				cells = new CellWidget[rows][cols];
				setLayout(new GridLayout(rows, cols));
				for( int y=0;  y<rows;  y++ ) {	
					for(int x=0;  x<cols;  x++ ) {
						cells[x][y] = new CellWidget(listener, x, y);
						add(cells[x][y]);
					}
				}
			}
			
			public void clear() {
				for( int x=0;  x<cols;  x++ ) {
					for( int y=0;  y<rows;  y++ ) {
						cells[x][y].clear();
					}
				}
			}
			
			public Wall getWall(CellWidget.CellWall wall) {
				int x = wall.x;
				int y = wall.y;
				boolean horizontal = true;
				if(wall.side == CellWidget.PAGE_START) {
					y = y-1;
				}
				else if(wall.side == CellWidget.LINE_START) {
					x = x-1;
					horizontal = false;
				}
				else if(wall.side == CellWidget.LINE_END) {
					horizontal = false;
				}
				if(!legal(x, y)) {
					return null;
				}
				return new Wall(new Cell(x, y), horizontal);
			}
			
			public Vector getWalls(CellWidget.CellWall wall) {
				Vector walls = new Vector();
				walls.add(wall);
				int nextx[] = new int[3];
				int nexty[] = new int[3];
				int nextside[] = new int[3];
				if(wall.side == CellWidget.PAGE_START) {
					nextx[0] = wall.x;
					nexty[0] = wall.y-1;
					nextside[0] = CellWidget.PAGE_END;
					
					nextx[1] = wall.x+1;
					nexty[1] = wall.y-1;
					nextside[1] = CellWidget.PAGE_END;
					
					nextx[2] = wall.x+1;
					nexty[2] = wall.y;
					nextside[2] = CellWidget.PAGE_START;
				}
				else if(wall.side == CellWidget.PAGE_END) {
					nextx[0] = wall.x;
					nexty[0] = wall.y+1;
					nextside[0] = CellWidget.PAGE_START;
					
					nextx[1] = wall.x+1;
					nexty[1] = wall.y+1;
					nextside[1] = CellWidget.PAGE_START;
					
					nextx[2] = wall.x+1;
					nexty[2] = wall.y;
					nextside[2] = CellWidget.PAGE_END;				
				}
				else if(wall.side == CellWidget.LINE_START) {
					nextx[0] = wall.x;
					nexty[0] = wall.y+1;
					nextside[0] = CellWidget.LINE_START;
					
					nextx[1] = wall.x-1;
					nexty[1] = wall.y+1;
					nextside[1] = CellWidget.LINE_END;
					
					nextx[2] = wall.x-1;
					nexty[2] = wall.y;
					nextside[2] = CellWidget.LINE_END;
				}
				else {
					nextx[0] = wall.x;
					nexty[0] = wall.y+1;
					nextside[0] = CellWidget.LINE_END;
					
					nextx[1] = wall.x+1;
					nexty[1] = wall.y+1;
					nextside[1] = CellWidget.LINE_START;
					
					nextx[2] = wall.x+1;
					nexty[2] = wall.y;
					nextside[2] = CellWidget.LINE_START;
				}
				for(int i=0;  i<3;  i++ ) {
					if(!legal(nextx[i], nexty[i])) {
						return null;
					}
					walls.add(cells[nextx[i]][nexty[i]].walls.get(nextside[i]));
				}
				return walls;
			}
			
			public boolean legal(int x, int y) {
				if(x<0 || y<0) {
					return false;
				}
				if(x>=cols || y>=rows) {
					return false;
				}
				return true;
			}
			
			public void hoverWall(int x, int y, int side) {
				cells[x][y].hoverWall(side);
			}
			
			public void setWall(int x, int y, int side) {
				cells[x][y].setWall(side);
			}
			
			public void unsetWall(int x, int y, int side) {
				cells[x][y].unsetWall(side);				
			}
			
			public void pawn(int x, int y, ImageIcon icon) {
				cells[x][y].pawn(icon);
			}
			
			public void removePawn(int x, int y) {
				cells[x][y].removePawn();
			}
			
			class CellWidget extends JPanel {
				public static final int PAGE_START = 0;
				public static final int PAGE_END = 1;
				public static final int LINE_START = 2;
				public static final int LINE_END = 3;
				
				public static final int HEIGHT = 40;
				public static final int WIDTH = 40;
				public static final int WALLHEIGHT = 2;
				public static final int WALLWIDTH = 36;
				
				public int x;
				public int y;
				public Vector walls;
				public CellCenter center;
				
				public CellWidget(MouseListener listener, int x, int y) {
					this.x = x;
					this.y = y;
					
					this.walls = new Vector();
					CellWall wall = new CellWall(x, y, PAGE_START, WALLWIDTH, WALLHEIGHT);
					wall.addMouseListener(listener);
					walls.add(wall);
					wall = new CellWall(x, y, PAGE_END, WALLWIDTH, WALLHEIGHT);
					wall.addMouseListener(listener);
					walls.add(wall);
					wall = new CellWall(x, y, LINE_START, WALLHEIGHT, WALLWIDTH);
					wall.addMouseListener(listener);
					walls.add(wall);
					wall = new CellWall(x, y, LINE_END, WALLHEIGHT, WALLWIDTH);
					wall.addMouseListener(listener);
					walls.add(wall);
					
					Dimension size = new Dimension(HEIGHT, WIDTH);
					setPreferredSize(size);
					setMinimumSize(size);
					setMaximumSize(size);
					
					setLayout(new BorderLayout());
					
					JPanel pageStart = new JPanel();
					pageStart.setLayout(new BoxLayout(pageStart, BoxLayout.LINE_AXIS));
					pageStart.add(Box.createRigidArea(
							new Dimension(WALLHEIGHT, WALLHEIGHT)));
					pageStart.add((CellWall)walls.get(PAGE_START));
					pageStart.add(Box.createRigidArea(
							new Dimension(WALLHEIGHT, WALLHEIGHT)));
					add(pageStart, BorderLayout.PAGE_START);
					
					JPanel pageEnd = new JPanel();
					pageEnd.setLayout(new BoxLayout(pageEnd, BoxLayout.LINE_AXIS));
					pageEnd.add(Box.createRigidArea(
							new Dimension(WALLHEIGHT, WALLHEIGHT)));
					pageEnd.add((CellWall)walls.get(PAGE_END));
					pageEnd.add(Box.createRigidArea(
							new Dimension(WALLHEIGHT, WALLHEIGHT)));
					add(pageEnd, BorderLayout.PAGE_END);
					
					JPanel lineStart = new JPanel();
					lineStart.setLayout(new BoxLayout(lineStart, BoxLayout.PAGE_AXIS));
					lineStart.add((CellWall)walls.get(LINE_START));
					add(lineStart, BorderLayout.LINE_START);
					
					JPanel lineEnd = new JPanel();
					lineEnd.setLayout(new BoxLayout(lineEnd, BoxLayout.PAGE_AXIS));
					lineEnd.add((CellWall)walls.get(LINE_END));
					add(lineEnd, BorderLayout.LINE_END);
					
					center = new CellCenter(x, y, WALLWIDTH, WALLWIDTH);
					center.addMouseListener(listener);
					add(center, BorderLayout.CENTER);
				}
				
				public void clear() {
					removePawn();
					for( int i=PAGE_START;  i<=LINE_END;  i++ ) {
						unsetWall(i);
					}
				}
				
				public void setWall(int side) {
					((CellWall)walls.get(side)).set();
				}
				
				public void hoverWall(int side) {
					((CellWall)walls.get(side)).hover();
				}
				
				public void unsetWall(int side) {
					((CellWall)walls.get(side)).unset();
				}			
				
				public void pawn(ImageIcon icon) {
					center.pawn(icon);
					revalidate();
					repaint();
				}
				
				public void removePawn() {
					center.removePawn();
					revalidate();
					repaint();
				}
				
				class CellCenter extends JPanel {
					
					public JPanel square;
					public JLabel icon;
					public static final int PADDING = 4;
					public int x;
					public int y;
					
					public CellCenter(int x, int y, int height, int width) {
						Dimension size = new Dimension(height, width);
						setPreferredSize(size);
						setMinimumSize(size);
						setMaximumSize(size);
						
						this.x = x;
						this.y = y;
						
						icon = null;
						
						square = new JPanel();
						square.setBorder(BorderFactory.createRaisedBevelBorder());
						square.setLayout(new BorderLayout());
						
						setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
						add(Box.createRigidArea(new Dimension(width, PADDING)));
						JPanel padded = new JPanel();
						padded.setLayout(new BoxLayout(padded, BoxLayout.LINE_AXIS));
						padded.add(Box.createRigidArea(new Dimension(PADDING, width-(PADDING*2))));
						padded.add(square);
						padded.add(Box.createRigidArea(new Dimension(PADDING, width-(PADDING*2))));
						add(padded);
						add(Box.createRigidArea(new Dimension(width, PADDING)));
					}
					
					public void hover() {
						square.setBorder(BorderFactory.createLoweredBevelBorder());
					}
					
					public void unhover() {
						square.setBorder(BorderFactory.createRaisedBevelBorder());
					}
					
					public void pawn(ImageIcon newIcon) {
						removePawn();
						if(newIcon != null) {
							icon = new JLabel(newIcon, JLabel.CENTER);
							square.add(icon, BorderLayout.CENTER);
						}
					}
					
					public void removePawn() {
						if(icon != null) {
							square.remove(icon);
							icon = null;
						}						
					}
				}
				
				class CellWall extends JPanel {
					
					public Color set;
					public Color hover;
					public int x;
					public int y;
					public int side;
					public boolean isSet;
					
					public CellWall(int x, int y, int side, int height, int width) {
						this.x = x;
						this.y = y;
						this.side = side;
						isSet = false;
						
						Dimension size = new Dimension(height, width);
						setPreferredSize(size);
						setMinimumSize(size);
						setMaximumSize(size);
						set = new Color(153, 153, 0);
						hover = set;
					}
					
					public void hover() {
						setBackground(hover);
					}
					
					public void set() {
						setBackground(set);
						isSet = true;
					}
					
					public void unset() {
						setBackground(getParent().getBackground());
						isSet = false;
					}
				}
			}
		}

		protected class BoardStart implements Runnable {
			public DisplayWidget widget;
			
			public BoardStart(DisplayWidget widget) {
				this.widget = widget;
			}
			
			public void run() { 
		    	widget.start();
		    }			
		}
		
		protected class BoardRestart implements Runnable {
			public DisplayWidget widget;
			
			public BoardRestart(DisplayWidget widget) {
				this.widget = widget;
			}
			
			public void run() { 
		    	widget.clear();
		    	widget.start();
		    }			
		}
		
		protected class BoardWall implements Runnable {
			public Wall wall;
			public DisplayWidget widget;
			
			public BoardWall(DisplayWidget widget, Wall wall) {
				this.widget = widget;
				this.wall = wall;
			}
			
			public void run() { 
		    	widget.wall(wall);
		    }			
		}
		
		protected class BoardWalk implements Runnable {
			public WalkMove move;
			public DisplayWidget widget;
			
			public BoardWalk(DisplayWidget widget, WalkMove move) {
				this.widget = widget;
				this.move = move;
			}
			
			public void run() { 
		    	widget.walk(move);
		    }			
		}
		
		protected class BoardInit implements Runnable {
			public int rows;
			public int cols;
			public DisplayWidget widget;
			
			public BoardInit(DisplayWidget widget, int rows, int cols) {
				this.widget = widget;
				this.rows = rows;
				this.cols = cols;
			}
			
			public void run() { 
		    	widget.init(rows, cols);
		    }
		}
	}

	
	class InfoWidget extends JScrollPane {
		public StringBuffer buffer;
		public JTextArea textArea;
		
		public InfoWidget() {
			this(new JTextArea());
		}
		
		protected InfoWidget(JTextArea textArea) {
			super(textArea);
			this.textArea = textArea;
			buffer = new StringBuffer();
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);			
			setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		}
		
		public synchronized void add(String str) {
			buffer.append(str);
			buffer.append("\n");
			textArea.setText(buffer.toString());
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
		thread = null;
	}
	
	public void destroy() {
		command(new QuitMessage("Player " + id + " quit."));
	}
	
}
