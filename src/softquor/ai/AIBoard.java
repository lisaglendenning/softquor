/*
 * Created on Apr 29, 2004
 *
 */
package softquor.ai;

import java.util.ListIterator;
import java.util.Vector;
import java.util.List;
import Jama.Matrix;
import softquor.GameException;
import softquor.board.*;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.text.DecimalFormat;

/**
 * @author Lisa Glendenning
 *
 */
public class AIBoard extends Board {
	
	public AIBoard() {
		super();
	}
	
	public AIBoard(int nrows, int ncols, int nwalls) throws GameException {
		super(nrows, ncols, nwalls);
	}
	
	public int nwalls(int playerid) {
		Player player = player(playerid);
		return player.nwalls;		
	}
	
	public int shortestPath(int playerid) {		
		Player player = player(playerid);
		Cell position = cells.get(player.position.x(), player.position.y());
		Vector playerPositions = opponentPositions(playerid);
		HashMap map = graph.shortestPath(position, player.goal, playerPositions);
		Collection paths = map.values();
		Iterator it = paths.iterator();
		int shortest = -1;		
		while(it.hasNext()) {
			List path = (List)it.next();
			if(path != null) {
				int length = path.size();
				if(shortest==-1  ||  length<shortest) {
					shortest = length;
				}
			}
		}
		return shortest;
	}
	
	public int manhatten(int playerid) {
		Player player = player(playerid);
		Cell g1 = (Cell)player.goal.get(0);
		Cell g2 = (Cell)player.goal.get(1);
		// going horizontal?
		int dist = 0;
		if(g1.x() == g2.x()) {
			dist = Math.abs(player.position.x() - g1.x());
		}
		// going vertical
		else {
			dist = Math.abs(player.position.y() - g1.y());			
		}
		return dist;
	}

	// sparse matrices
	public double markovChain(int playerid) {
		Player player = player(playerid);	
		Vector goal = player.goal;
		Cell position = cells.get(player.position.x(), player.position.y());
		
		Set connected = graph.connectedSetOf(player.position);
		Vector interior = new Vector(cells.rows()*cells.cols());
		Vector border = new Vector(cells.rows()*2);
		Iterator it = connected.iterator();
		int interiorInd = 0;
		int borderInd = 0;
		HashMap interiorIndices = new HashMap(cells.rows()*cells.cols());
		HashMap borderIndices = new HashMap(cells.rows());
		while(it.hasNext()) {
			Object next = it.next();
			if(goal.contains(next)) {
				border.add(next);
				borderIndices.put(next, new Integer(borderInd));
				borderInd++;
			}
			else {
				interior.add(next);
				interiorIndices.put(next, new Integer(interiorInd));
				interiorInd++;
			}
		}
		int n = borderInd;
		int m = interiorInd;	
		
		// create artificial source
		Vector source = source(playerid);
		ListIterator lit = source.listIterator();
		while(lit.hasNext()) {
			Object next = lit.next();
			if(interior.contains(next)) {
				border.add(next);
				borderIndices.put(next, new Integer(borderInd));
				borderInd++;
			}
		}	
		
		Matrix R = new Matrix(m, borderInd);
		Matrix Q = new Matrix(m, m);
		lit = interior.listIterator();
		while(lit.hasNext()) {
			Cell vertex = (Cell)lit.next();
			int vertexIndex = ((Integer)interiorIndices.get(vertex)).intValue();
			boolean sourceCell = source.contains(vertex);
			double prob = probability(vertex, sourceCell);
			//System.out.print(vertex + " " + vertexIndex + " " + prob);
			Vector neighbors = graph.neighbors(vertex);
			ListIterator nlit = neighbors.listIterator();
			while(nlit.hasNext()) {
				Cell next = (Cell)nlit.next();
				//System.out.print(" " + next);
				if(goal.contains(next)) {
					int borderIndex = ((Integer)borderIndices.get(next)).intValue();
					//System.out.print(" goal " + borderIndex);
					R.set(vertexIndex, borderIndex, prob);
				}
				else {
					int interiorIndex = ((Integer)interiorIndices.get(next)).intValue();
					//System.out.print(" interior " + interiorIndex);
					Q.set(vertexIndex, interiorIndex, prob);
				}
			}
			if(sourceCell) {
				int sourceIndex = ((Integer)borderIndices.get(vertex)).intValue();
				//System.out.print(" source " + sourceIndex);
				R.set(vertexIndex, sourceIndex, prob);
			}
			//System.out.println();
		}
		//R.print(5, 2);
		//Q.print(5, 2);
		
		// N = inverse(I-Q)
		Matrix I = Matrix.identity(m, m);		
		Matrix N = (I.minus(Q)).inverse();
		
		// B = N*R
		Matrix B = N.times(R);
		
		// create fB
		Matrix fB = new Matrix(border.size(), 1);
		for( int i=0;  i<n;  i++ ) {
			fB.set(i, 0, (double)1);
		}
		
		// fD = B*fB
		Matrix fD = B.times(fB);
		
		Integer positionIndex = (Integer)interiorIndices.get(position);
		if(positionIndex == null) {
			positionIndex = (Integer)borderIndices.get(position);
			return fB.get(positionIndex.intValue(), 0);
		}
		return fD.get(positionIndex.intValue(), 0);
	}	
	
	// probability of moving from a
	private double probability(Cell a, boolean source) {
		double degree = (double)graph.degree(a);
		if(source) {
			degree++;
		}
		return ((double)1)/degree;
	}
	
	public double relaxation(int playerid, int iterations) {
		Player player = player(playerid);	
		Vector goal = player.goal;
		int n = cells.cols();
		int m = cells.rows();

		double[][] prob = new double[n+1][m+1];
			
		Cell g1 = (Cell)goal.get(0);
		Cell g2 = (Cell)goal.get(1);
		if(g1.x() == g2.x()) {
			if(g1.x() == 0) {
				// initialize values;
				for( int x=0;  x<n+1;  x++ ) {
					for( int y=0;  y<m+1;  y++) {
						if(x == 0) {
							prob[x][y] = 1;
						}
						else {
							prob[x][y] = 0;
						}
					}
				}
				
				// relax interior points
				for( int i=0;  i<iterations;  i++ ) {
					for( int x=1;  x<n;  x++ ) {
						for( int y=0;  y<m;  y++ ) {
							boolean boundary = (x==(n-1)) ? true : false;
							prob[x][y] = neighborAvg(prob, x, 0, y, 0, boundary);
						}
					}
				}
				//printProbabilities(prob, 0, n-1, 0, m-1);
				return prob[player.position.x()][player.position.y()];
			}
			
			// initialize values;
			for( int x=0;  x<n+1;  x++ ) {
				for( int y=0;  y<m+1;  y++) {
					if(x == n) {
						prob[x][y] = 1;
					}
					else {
						prob[x][y] = 0;
					}
				}
			}
				
			// relax interior points
			for( int i=0;  i<iterations;  i++ ) {
				for( int x=n-1;  x>0;  x-- ) {
					for( int y=0;  y<m;  y++ ) {
						boolean boundary = (x==1) ? true : false;
						prob[x][y] = neighborAvg(prob, x, 1, y, 0, boundary);
					}
				}
			}
			//printProbabilities(prob, 1, n, 0, m-1);
			return prob[player.position.x()+1][player.position.y()];
		}
		
		if(g1.y() == 0) {
			// initialize values;
			for( int x=0;  x<n+1;  x++ ) {
				for( int y=0;  y<m+1;  y++) {
					if(y == 0) {
						prob[x][y] = 1;
					}
					else {
						prob[x][y] = 0;
					}
				}
			}
			
			// relax interior points
			for( int i=0;  i<iterations;  i++ ) {
				for( int y=1;  y<m;  y++ ) {
					for( int x=0;  x<n;  x++ ) {
						boolean boundary = (y==(m-1)) ? true : false;
						prob[x][y] = neighborAvg(prob, x, 0, y, 0, boundary);
					}
				}
			}
			//printProbabilities(prob, 0, n-1, 0, m-1);
			return prob[player.position.x()][player.position.y()];
		}
		
		// initialize values;
		for( int x=0;  x<n+1;  x++ ) {
			for( int y=0;  y<m+1;  y++) {
				if(y == m) {
					prob[x][y] = 1;
				}
				else {
					prob[x][y] = 0;
				}
			}
		}
		
		// relax interior points
		for( int i=0;  i<iterations;  i++ ) {
			for( int y=m-1;  y>0;  y-- ) {
				for( int x=0;  x<n;  x++ ) {
					boolean boundary = (y==1) ? true : false;
					prob[x][y] = neighborAvg(prob, x, 0, y, 1, boundary);
				}
			}
		}
		//printProbabilities(prob, 0, n-1, 1, m);
		return prob[player.position.x()][player.position.y()+1];
	}

	private double neighborAvg(double[][] prob, int x, int modx, int y, int mody, boolean boundary) {
		Cell vertex = cells.get(x-modx, y-mody);
		Vector neighbors = graph.neighbors(vertex);
		ListIterator nlit = neighbors.listIterator();
		double nsum = (double)0;
		while(nlit.hasNext()) {
			Cell neighbor = (Cell)nlit.next();
			nsum += prob[neighbor.x()+modx][neighbor.y()+mody];
		}
		double average = boundary ? (nsum/(double)(neighbors.size() + 1))
					: (nsum/(double)neighbors.size());
		return average;
	}
	
	private void printProbabilities(double[][] prob, int startx, int endx, int starty, int endy) {
		DecimalFormat formatter = new DecimalFormat("0.00");
		for( int j=starty;  j<=endy;  j++ ) {
			for( int i=startx;  i<=endx;  i++ ) {
				System.out.print(formatter.format(prob[i][j]) + ", ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public Vector source(int playerid) {
		Vector goal = player(playerid).goal;
		Cell g0 = (Cell)goal.get(0);
		Cell g1 = (Cell)goal.get(1);
		if(g0.x() == g1.x()) {
			if(g0.x() == 0) {
				return cells.edge(CellMatrix.EAST);
			}
			return cells.edge(CellMatrix.WEST);
		}
		if(g0.y() == 0) {
			return cells.edge(CellMatrix.SOUTH);
		}
		return cells.edge(CellMatrix.NORTH);
	}
	
	public int pawnDistance() {
		Player p1 = (Player)state.players.get(0);
		Player p2 = (Player)state.players.get(1);	
		List path = graph.shortestPath(p1.position, p2.position, new Vector());
		if(path == null) {
			return 0;
		}
		return path.size();
	}
	
	public int goalSide(int playerid) {
		Player player = player(playerid);	
		int dist = manhatten(playerid);
		Cell g1 = (Cell)player.goal.get(0);
		Cell g2 = (Cell)player.goal.get(1);
		// going horizontal?
		if(g1.x() == g2.x()) {
			if(dist < (cells.cols()/2)) {
				return 1;
			}
			return 0;
		}
		// going vertical
		else {
			if(dist < (cells.rows()/2)) {
				return 1;
			}
		}
		return 0;
	}
	
}
