/*
 * Created on Apr 5, 2004
 *
 */
 
package softquor.board;
import java.util.ListIterator;
import java.util.Vector;
import java.util.Set;

import softquor.GameException;

/*
 * @author Lisa Glendenning
 *
 */
public class Board {
	protected final static int MAX_PLAYERS = 4;		

	protected int nwalls;
	protected BoardState state;
	protected CellMatrix cells;
	protected CellGraph graph;

	/* 
	 * calls Board(int nrows, int ncols, int nwalls) with default values
	 * nrows=9, ncols=9; nwalls=20
	 */
	public Board() {
		init(9, 9, 20);			
	}
	
	public Board(int nrows, int ncols, int nwalls) throws GameException {
		if( nrows<1 || ncols<1 || nwalls<0) {
			throw new GameException("Illegal Board parameters");
		}
		init(nrows, ncols, nwalls);
	}
	
	private void init(int nrows, int ncols, int nwalls) {
		this.nwalls = nwalls;
		this.state = new BoardState();
		this.cells = new CellMatrix(nrows, ncols);
		this.graph = new CellGraph(this.cells);
	}
	
	public int rows() {
		return cells.rows();
	}
	
	public int cols() {
		return cells.cols();
	}
	
	public int nwalls() {
		return nwalls;
	}
	
	/*
	 *  Register a new user before start() has been called
	 *  
	 */
	public void register(int userid) throws GameException {
		if(state.phase != BoardState.PREGAME) {
			throw new GameException("Can't register during game.");
		}
		if(state.players.size() == MAX_PLAYERS) {
			throw new GameException("Maximum players registered");
		}
		if(player(userid) != null) {
			throw new GameException("Player " + userid + " already registered");
		}
		state.players.add(new Player(userid));
	}
	
	/*
	 *  Once all the players have registered, start the game
	 */
	public void start(int firstPlayer) throws GameException {	
		if(state.phase != BoardState.PREGAME) {
			throw new GameException("Cannot start game while game is in progress");
		}
		
		int nplayers = state.players.size();
		if( nplayers!=2 && nplayers!=4 ) {
			throw new GameException("Illegal number of players");
		}
		int wallsPerPlayer = nwalls / nplayers;
		
		Vector northEdge = cells.edge(CellMatrix.NORTH);
		Vector southEdge = cells.edge(CellMatrix.SOUTH);
		Vector eastEdge = cells.edge(CellMatrix.EAST);
		Vector westEdge = cells.edge(CellMatrix.WEST);	
		
		Player next = (Player)state.players.get(0); 
		next.nwalls = wallsPerPlayer;
		next.position = (Cell)southEdge.get(southEdge.size()/2);
		next.goal = northEdge;
		
		next = (Player)state.players.get(1); 
		next.nwalls = wallsPerPlayer;
		next.position = (Cell)northEdge.get(northEdge.size()/2);
		next.goal = southEdge;
		
		if(nplayers == 4) {
			next = (Player)state.players.get(2); 
			next.nwalls = wallsPerPlayer;
			next.position = (Cell)eastEdge.get(eastEdge.size()/2);
			next.goal = westEdge;
		
			next = (Player)state.players.get(3); 
			next.nwalls = wallsPerPlayer;
			next.position = (Cell)westEdge.get(westEdge.size()/2);
			next.goal = eastEdge;
		}
		
		ListIterator lit = state.players.listIterator();
		int ind = 0;
		while(lit.hasNext()) {
			Player p = (Player)lit.next();
			if(p.id() == firstPlayer) {
				state.nextplayer = ind;
				break;
			}
			ind++;
		}
		
		state.phase = BoardState.GAME;
	}
	
	public boolean gameOver() {
		return (state.phase == BoardState.ENDGAME);
	}
	
	public int phase() {
		return state.phase;
	}
	
	public void restart(int firstPlayer) throws GameException {
		if(state.phase == BoardState.PREGAME) {
			throw new GameException("Cannot restart game yet");
		}
		
		int nplayers = state.players.size();
		int wallsPerPlayer = nwalls / nplayers;
		state.walls.clear();			
		graph.setWalls(state.walls);

		Vector northEdge = cells.edge(CellMatrix.NORTH);
		Vector southEdge = cells.edge(CellMatrix.SOUTH);
		Vector eastEdge = cells.edge(CellMatrix.EAST);
		Vector westEdge = cells.edge(CellMatrix.WEST);	
		
		Player next = (Player)state.players.get(0); 
		next.nwalls = wallsPerPlayer;
		next.position = (Cell)southEdge.get(southEdge.size()/2);
		
		next = (Player)state.players.get(1); 
		next.nwalls = wallsPerPlayer;
		next.position = (Cell)northEdge.get(northEdge.size()/2);
		
		if(nplayers == 4) {
			next = (Player)state.players.get(2); 
			next.nwalls = wallsPerPlayer;
			next.position = (Cell)eastEdge.get(eastEdge.size()/2);
			
			next = (Player)state.players.get(3); 
			next.nwalls = wallsPerPlayer;
			next.position = (Cell)westEdge.get(westEdge.size()/2);
		}
		
		ListIterator lit = state.players.listIterator();
		int ind = 0;
		while(lit.hasNext()) {
			Player p = (Player)lit.next();
			if(p.id() == firstPlayer) {
				state.nextplayer = ind;
				break;
			}
			ind++;
		}		
		
		state.phase = BoardState.GAME;
		state.turns = 0;
	}
	
	/*
	 *  Returns a deep copy of the Board's state
	 */
	public BoardState state() {
		return (BoardState)state.clone();
	}
	
	public void state(BoardState bstate) {
		state = (BoardState)bstate.clone();
		graph.setWalls(state.walls);
	}
	
	public boolean move(Move move) {
		state = state.child();
		if(move instanceof WalkMove) {
			WalkMove wm = (WalkMove)move;
			player(wm.userid).position = wm.to;
		}	
		else if(move instanceof WallMove) {
			WallMove wm = (WallMove)move;
			player(wm.userid).nwalls--;
			state.walls.add(wm.wall);
			graph.addWall(wm.wall);
		}
		state.turns++;
		return turn();
	}
	
	public int turns() {
		return state.turns;
	}
	
	public Vector legalMoves() {
		Player nextPlayer = (Player)state.players.get(state.nextplayer);
		Vector moves = legalWalkMoves(nextPlayer);
		moves.addAll(legalWallMoves(nextPlayer));
		return moves;
	}	
	
	public Vector legalWalkMoves(Player player) {
		Vector moves = new Vector();
		//System.out.println(player.position);
		Vector neighbors = graph.neighbors(player.position);
		//System.out.println(neighbors);
		ListIterator lit = neighbors.listIterator();
		while(lit.hasNext()) {
			Cell next = (Cell)lit.next();
			//System.out.println(nextmove);
			if(player(next) == null) {
				//System.out.println("legal");
				moves.add(new WalkMove(player.id(), player.position, next));
			}
			// possible jump
			else {
				Cell behindNext = cells.neighbor(cells.direction(player.position, next), next);
				if(behindNext==null || !graph.edge(next, behindNext)) {
					Vector nextNeighbors = graph.neighbors(next);
					ListIterator nextLit = nextNeighbors.listIterator();
					while(nextLit.hasNext()) {
						Cell nextNeighbor = (Cell)nextLit.next();
						if(player(nextNeighbor) == null) {
							moves.add(new WalkMove(player.id(), player.position, nextNeighbor));
						}
					}
				}
				else {
					moves.add(new WalkMove(player.id(), player.position, behindNext));
				}
			}
		}	
		return moves;
	}
	
	public Vector legalWallMoves(Player player) {
		Vector moves = new Vector();
		if(player.nwalls > 0) {			
			for( int x=0;  x<cells.cols();  x++ ) {
				for( int y=0; y<cells.rows(); y++ ) {
					Cell nw = new Cell(x, y);
					Wall next = new Wall(nw, true);
					WallMove nextmove = new WallMove(player.id(), next);
					if(legalWallMove(nextmove)) {
						moves.add(nextmove);
					}
					next = new Wall(nw, false);
					nextmove = new WallMove(player.id(), next);
					if(legalWallMove(nextmove)) {
						moves.add(nextmove);
					}					
				}
			}
		}
		return moves;		
	}	
	
	public boolean legal(Move move) {
		if(move.userid != nextPlayer()) {
			//System.out.println("wrong player");
			return false;
		}		
		if(move instanceof WalkMove) {
			return legalWalkMove((WalkMove)move);
		}
		if(move instanceof WallMove) {
			return legalWallMove((WallMove)move);
		}
		return false;	
	}
	
	protected boolean legalWallMove(WallMove move) {
		if(player(move.userid).nwalls < 1) {
			//System.out.println("not enough walls");
			return false;
		}
		if(!cells.legal(move.wall.northwest())) {
			//System.out.println("northwest not on board");
			return false;
		}
		Cell ne = cells.neighbor(CellMatrix.EAST, move.wall.northwest());
		if( ne==null ||
			cells.neighbor(CellMatrix.SOUTH, move.wall.northwest())==null ||
			cells.neighbor(CellMatrix.SOUTH, ne)==null) {
			//System.out.println("not on board");
			return false;
		}
		
		//wall conflict?
		Vector neighbors = graph.neighbors(move.wall.northwest());
		ListIterator lit = state.walls.listIterator();
		while(lit.hasNext()) {
			Wall next = (Wall)lit.next();
			if(next.northwest().equals(move.wall.northwest())) {
				//System.out.println("nortwest conflict");
				return false;
			}
			if(next.horizontal() == move.wall.horizontal()) {
				ListIterator nlit = neighbors.listIterator();
				while(nlit.hasNext()) {
					Cell neighbor = (Cell)nlit.next();
					if(neighbor.equals(next.northwest())) {
						int dir = cells.direction(move.wall.northwest(), neighbor);
						if(next.horizontal()) {
							if(dir==CellMatrix.EAST || dir==CellMatrix.WEST) {
								//System.out.println("east west conflict");
								return false;
							}
						}
						else {
							if(dir==CellMatrix.NORTH || dir==CellMatrix.SOUTH) {
								//System.out.println("east west conflict");
								return false;
							}
						}
					}	
				}
			}
		}
		
		
		// block path?
		graph.addWall(move.wall);
		boolean reachGoal = reachGoal();
		graph.removeWall(move.wall);
		if(!reachGoal) {
			//System.out.println("blocks opponent");
			return false;
		}
		return true;
	}
	
	protected boolean legalWalkMove(WalkMove move) {
		if(!player(move.userid).position.equals(move.from)) {
			//System.out.println("player not in from cell");
			return false;
		}
		if(player(move.to) != null) {
			//System.out.println("player in to cell");
			return false;
		}
		if(!cells.legal(move.to)) {
			//System.out.println("to cell off board");
			return false;
		}
		if(graph.edge(move.to, move.from)) {
			return true;
		}
		
		// jumping
		// straight line jump?
		int dir = cells.direction(move.from, move.to);
		if(dir != CellMatrix.NONE) {
			Cell intermediate = cells.neighbor(dir, move.from);
			// pawn must be present
			if(player(intermediate) == null) {
				//System.out.println("no intermediate pawn for straight jump");
				return false;
			}
			// check for walls
			if(!graph.edge(move.from, intermediate) || !graph.edge(intermediate, move.to)) {
				//System.out.println("can't jump over walls");
				return false;
			}
		}
		
		// must be a wall jump
		// get intermediate cells
		Vector fromNeighbors = graph.neighbors(move.from);
		Vector toNeighbors = graph.neighbors(move.to);
		Vector sharedNeighbors = new Vector();
		ListIterator lit = fromNeighbors.listIterator();
		while(lit.hasNext()) {
			Cell next = (Cell)lit.next();
			if(toNeighbors.contains(next)) {
				sharedNeighbors.add(next);
			}
		}
		
		// find the intermediate cell with a player
		lit = sharedNeighbors.listIterator();
		while(lit.hasNext()) {
			Cell next = (Cell)lit.next();
			if(player(next) != null) {
				return true;
			}
		}		
		//System.out.println("no intermediate pawn for wall jump");
		return false;
	}
	
	public int numPlayers() {
		return state.players.size();
	}
	
	public int nextPlayer() {
		return ((Player)state.players.get(state.nextplayer)).id();
	}
	
	public int prevPlayer() {
		int prevIndex = state.nextplayer - 1;
		if(prevIndex >= 0) {
			return ((Player)state.players.get(prevIndex)).id();
		}
		return ((Player)state.players.lastElement()).id();
	}
	
	public int getOpponent(int playerid) {
		ListIterator lit = state.players.listIterator();
		while(lit.hasNext()) {
			Player player = (Player)lit.next();
			if(player.id() != playerid) {
				return player.id();
			}
		}
		return playerid;
	}
			
	protected boolean turn() {
		if(endgame()) {
			state.phase = BoardState.ENDGAME;
			return true;
		}
		state.nextplayer++;
		if(state.nextplayer >= state.players.size()) {
			state.nextplayer = 0;
		}
		return false;
	}
	
	public String toString() {
		String str = "(Board[cells=" + cells.toString() + "][graph=" + graph.toString() + "])";
		return str;
	}
	
	public String toText(int printdepth) {
		StringBuffer out = new StringBuffer();
		for( int y=0;  y<cells.rows();  y++ ) {
			for(int i=0;  i<printdepth;  i++) {
				out.append(" ");
			}
			if(y == 0) {
				out.append("  ");
				for( int x=0;  x<cells.cols();  x++ ) {
					out.append(x + " ");	
				}				
				out.	append("\n");
				for(int i=0;  i<printdepth;  i++) {
					out.append(" ");
				}
			}	
			for( int x=0;  x<cells.cols();  x++ ) {
				if(x == 0) {
					out.append(y + " ");
				}
				Cell cell = cells.get(x, y);
				Player p = player(cell);
				if(p == null) {
					out.append("#");		
				}
				else {
					out.append(p.id());
				}
				if(x < (cells.cols()-1)) {
					Cell next = cells.get(x+1, y);
					if(graph.edge(cell, next)) {
						out.append(" ");
					}
					else {
						out.append("|");
					}
				}
			}
			out.append("\n");
			for(int i=0;  i<printdepth;  i++) {
				out.append(" ");
			}
			if(y < (cells.rows()-1)) {
				out.append("  ");
				for( int x=0;  x<cells.rows();  x++ ) {
					Cell next = cells.get(x, y);
					Cell cell = cells.get(x, y+1);
					if(graph.edge(cell, next)) {
						out.append(" ");
					}
					else {
						out.append("-");
					}
					out.append(" ");
				}			
			}
			out.append("\n");
		}
		return out.toString();
	}
	
	// checks if player that just went reached the goal
	private boolean endgame() {
		Player nextplayer = (Player)state.players.get(state.nextplayer);
		ListIterator goals = nextplayer.goal.listIterator();
		while(goals.hasNext()) {
			Cell goal = (Cell)goals.next();
			if(nextplayer.position.equals(goal)) {
				return true;
			}
		}
		return false;		
	}	
	
	public int nwalls(int playerid) {
		Player player = player(playerid);
		return player.nwalls;		
	}
		
	public Player player(int userid) {
		ListIterator lit = state.players.listIterator();
		while(lit.hasNext()) {
			Player p = (Player)lit.next();
			if(p.id() == userid) {
				return p;
			}
		}
		return null;
	}	
	
	public Player player(Cell position) {
		ListIterator lit = state.players.listIterator();
		while(lit.hasNext()) {
			Player p = (Player)lit.next();
			if(p.position.equals(position)) {
				return p;
			}
		}
		return null;
	}	
	
	public CellMatrix cells() {
		return cells;
	}
	
	public CellGraph graph() {
		return graph;
	}
	
	protected boolean reachGoal() {
		ListIterator lit = state.players.listIterator();
		while(lit.hasNext()) {
			Player player = (Player)lit.next();
			Set connected = graph.connectedSetOf(player.position);
			boolean reachGoal = false;
			ListIterator goalit = player.goal.listIterator();
			while(goalit.hasNext() && !reachGoal) {
				Cell next = (Cell)goalit.next();
				if(connected.contains(next)) {
					reachGoal = true;
				}
			}
			if(!reachGoal) {
				return false;
			}
		}
		return true;
	}
	
	protected Vector opponentPositions(int playerid) {
		Vector positions = new Vector();
		ListIterator lit = state.players.listIterator();
		while(lit.hasNext()) {
			Player next = (Player)lit.next();
			if(next.id() != playerid) {
				positions.add(next.position);
			}
		}
		return positions;
	}

}
