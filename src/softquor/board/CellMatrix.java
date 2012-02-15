/*
 * Created on Apr 29, 2004
 *
 */
package softquor.board;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.ListIterator;

/**
 * @author Lisa Glendenning
 *
 */
public class CellMatrix extends AbstractSet {

	public final static int NORTH = 0;
	public final static int SOUTH = 1;
	public final static int EAST = 2;
	public final static int WEST = 3;
	public final static int NONE = 4;

	private int nrows;
	private int ncols;
	private Vector cells;
	
	// x = row
	// y = col
	public CellMatrix(int nrows, int ncols) {
		this.nrows = nrows;
		this.ncols = ncols;
		cells = new Vector(nrows*ncols);
		for( int x=0;  x<nrows;  x++ ) {
			for( int y=0;  y<ncols;  y++ ) {
				cells.add(new Cell(x,y));
			}
		}
	}
	
	public Vector get() {
		return cells;
	}
	
	public int rows() {
		return nrows;
	}
	
	public int cols() {
		return ncols;
	}
	
	public Cell get(int row, int col) {
		return get(index(row, col));
	}

	public Vector neighbors(Cell c) {
		Vector neighbors = new Vector();
		Cell next = neighbor(NORTH, c);
		if(next != null) {
			neighbors.add(next);
		}
		next = neighbor(SOUTH, c);
		if(next != null) {
			neighbors.add(next);
		}
		next = neighbor(EAST, c);
		if(next != null) {
			neighbors.add(next);
		}
		next = neighbor(WEST, c);
		if(next != null) {
			neighbors.add(next);
		}

		return neighbors;		
	}
	
	public Vector edge(int dir) {
		Vector edges = new Vector();
		
		Cell start;
		int direction;

		if(dir == NORTH) {
			start = get(0, 0);
			direction = EAST;
		}
		else if(dir == SOUTH) {
			start = get(0, nrows-1);
			direction = EAST;
		}		
		else if(dir == EAST) {
			start = get(ncols-1, 0);
			direction = SOUTH;
		}		
		else {
			start = get(0, 0);
			direction = SOUTH;
		}
		edges.add(start);
		Cell next = neighbor(direction, start);
		while(next != null) {		
			edges.add(next);
			next = neighbor(direction, next);
		}
		return edges;
	}	
	
	public boolean legal(Cell c) {
		return (c!=null && c.x()>=0 && c.x()<nrows && c.y()>=0 && c.y()<ncols);
	}	 	
	
	public boolean edge(int dir, Cell n) {
		if(dir == NORTH) {
			return (n.y() == 0);
		}
		if(dir == SOUTH) {
			return (n.y() == (nrows-1));
		}
		if(dir == EAST) {
			return (n.x() == (ncols-1));
		}
		return (n.x() == 0);
	}	 
	

	public int direction(Cell from, Cell to) {
		if(from.x() == to.x()) {
			if(from.y() < to.y()) {
				return SOUTH;
			}
			if(from.y() > to.y()) {
				return NORTH;
			}
		}
		if(from.y() == to.y()) {
			if(from.x() < to.x()) {
				return EAST;
			}
			if(from.x() > to.x()) {
				return WEST;
			}
		}
		return NONE;
	}	
	
	/* 
	 * @see java.util.Collection#size()
	 */
	public int size() {
		return nrows*ncols;
	}

	/* 
	 * @see java.util.Collection#iterator()
	 */
	public Iterator iterator() {
		return cells.iterator();
	}
	
	public Cell neighbor(int dir, Cell c) {
		if(edge(dir, c)) {
			return null;
		}
		if(dir == NORTH) {
			return get(c.x(), c.y()-1);
		}
		if(dir == SOUTH) {
			return get(c.x(), c.y()+1); 
		}
		if(dir == EAST) {
			return get(c.x()+1, c.y()); 
		}
		return get(c.x()-1, c.y()); 
	}		
	
	public String toString() {
		String str = "(CellMatrix[nrows=" + nrows + "][ncols=" + ncols + "][cells=";
		ListIterator lit = cells.listIterator();
		while(lit.hasNext()) {
			str += "[" + lit.next().toString() + "]";
		}
		str += ")";
		return str;
	}
	
	private Cell get(int index) {
		return (Cell)cells.get(index);
	}
	
	private int index(int row, int col) {
		return (row*ncols + col);
	}

}
