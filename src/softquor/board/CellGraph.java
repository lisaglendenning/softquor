/*
 * Created on Apr 29, 2004
 *
 */
package softquor.board;
import java.util.Vector;
import java.util.ListIterator;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import org._3pq.jgrapht.graph.ListenableUndirectedGraph;
import org._3pq.jgrapht.Edge;
import org._3pq.jgrapht.edge.UndirectedEdge;
import org._3pq.jgrapht.alg.ConnectivityInspector;
import org._3pq.jgrapht.alg.DijkstraShortestPath;

/**
 * @author Lisa Glendenning
 *
 */
public class CellGraph {
	
	private ListenableUndirectedGraph graph;
	private Vector walledges;
	private CellMatrix cells;
	
	public CellGraph(CellMatrix cells) {
		this.cells = cells;
		graph = new ListenableUndirectedGraph();
		graph.addAllVertices(cells);
		
		Vector edges = new Vector();
		for( int x=0;  x<cells.rows();  x++ ) {
			for( int y=0;  y<cells.cols();  y++ ) {
				Cell current = cells.get(x, y);
				Vector neighbors = cells.neighbors(current);
				ListIterator lit = neighbors.listIterator();
				while(lit.hasNext()) {
					Cell nextCell = (Cell)lit.next();
					edges.add(new UndirectedEdge(current, nextCell));
				}
			}
		}	

		graph.addAllEdges(edges);
		
		walledges = new Vector();	
	}
	
	public void addWall(Wall wall) {
		Cell nw = cells.get(wall.northwest().x(), wall.northwest().y());
		Cell ne = cells.neighbor(CellMatrix.EAST, nw);	
		Cell sw = cells.neighbor(CellMatrix.SOUTH, nw);	
		Cell se = cells.neighbor(CellMatrix.EAST, sw);			
		if(wall.horizontal()) {
			walledges.add(graph.getEdge(nw, sw));
			walledges.add(graph.getEdge(ne, se));			
		}
		else {
			walledges.add(graph.getEdge(nw, ne));
			walledges.add(graph.getEdge(sw, se));	
		}
	}
	
	public void removeWall(Wall wall) {
		Cell nw = cells.get(wall.northwest().x(), wall.northwest().y());
		Cell ne = cells.neighbor(CellMatrix.EAST, nw);	
		Cell sw = cells.neighbor(CellMatrix.SOUTH, nw);	
		Cell se = cells.neighbor(CellMatrix.EAST, sw);			
		if(wall.horizontal()) {
			walledges.remove(graph.getEdge(nw, sw));
			walledges.remove(graph.getEdge(ne, se));			
		}
		else {
			walledges.remove(graph.getEdge(nw, ne));
			walledges.remove(graph.getEdge(sw, se));	
		}
	}	
	
	public void clear() {
		walledges.clear();
	}
	
	public int degree(Cell vertex) {
		remove();
		int degree = graph.degreeOf(cells.get(vertex.x(), vertex.y()));
		retrieve();
		return degree;
	}

	public void setWalls(Vector walls) {
		clear();
		ListIterator lit = walls.listIterator();
		while(lit.hasNext()) {
			addWall((Wall)lit.next());
		}
	}
	
	public boolean edge(Cell from, Cell to) {
		remove();
		Cell f = cells.get(from.x(), from.y());
		Cell t = cells.get(to.x(), to.y());
		boolean edge = graph.containsEdge(f, t);
		retrieve();
		return edge;
	}
	
	public List shortestPath(Cell from, Cell to, Vector playerPositions) {
		remove();
		// temporarily remove player positions from graph
		Vector removedVertices = new Vector();
		Vector removedEdges = new Vector();
		Vector addedEdges = new Vector();
		ListIterator lit = playerPositions.listIterator();
		while(lit.hasNext()) {
			Cell next = (Cell)lit.next();
			Cell player = cells.get(next.x(), next.y());
			removedVertices.add(player);
			List playerEdges = graph.edgesOf(player);
			removedEdges.addAll(playerEdges);
			Vector neighbors = new Vector();
			ListIterator edgeslit = playerEdges.listIterator();
			while(edgeslit.hasNext()) {
				Edge edge = (Edge)edgeslit.next();
				neighbors.add(edge.oppositeVertex(player));
			}
			graph.removeVertex(player);
			for( int i=0;  i<neighbors.size();  i++ ) {
				for( int j=1;  j<neighbors.size();  j++ ) {
					if(i == j) {
						continue;
					}
					Cell n1 = (Cell)neighbors.get(i);
					Cell n2 = (Cell)neighbors.get(j);
					if(cells.direction(n1, n2) != CellMatrix.NONE) {
						if(!graph.containsEdge(n1, n2)) {
							UndirectedEdge edge = new UndirectedEdge(n1, n2);
							graph.addEdge(edge);
							addedEdges.add(edge);
						}
					}
				}
			}
		}		

		Cell f = cells.get(from.x(), from.y());
		Cell t = cells.get(to.x(), to.y());		
		List path = DijkstraShortestPath.findPathBetween(graph, f, t);
		graph.removeAllEdges(addedEdges);
		graph.addAllVertices(removedVertices);
		graph.addAllEdges(removedEdges);		
		retrieve();
		return path;
	}
	
	public HashMap shortestPath(Cell from, List to, Vector playerPositions) {
		remove();
		// temporarily remove player positions from graph
		Vector removedVertices = new Vector();
		Vector removedEdges = new Vector();
		Vector addedEdges = new Vector();
		ListIterator lit = playerPositions.listIterator();
		while(lit.hasNext()) {
			Cell next = (Cell)lit.next();
			Cell player = cells.get(next.x(), next.y());
			removedVertices.add(player);
			List playerEdges = graph.edgesOf(player);
			removedEdges.addAll(playerEdges);
			Vector neighbors = new Vector();
			ListIterator edgeslit = playerEdges.listIterator();
			while(edgeslit.hasNext()) {
				Edge edge = (Edge)edgeslit.next();
				neighbors.add(edge.oppositeVertex(player));
			}
			graph.removeVertex(player);
			for( int i=0;  i<neighbors.size();  i++ ) {
				for( int j=1;  j<neighbors.size();  j++ ) {
					if(i == j) {
						continue;
					}
					Cell n1 = (Cell)neighbors.get(i);
					Cell n2 = (Cell)neighbors.get(j);
					if(cells.direction(n1, n2) != CellMatrix.NONE) {
						if(!graph.containsEdge(n1, n2)) {
							UndirectedEdge edge = new UndirectedEdge(n1, n2);
							graph.addEdge(edge);
							addedEdges.add(edge);
						}
					}
					else {
						if(neighbors.size() < 4) {
							if(!graph.containsEdge(n1, n2)) {
								UndirectedEdge edge = new UndirectedEdge(n1, n2);
								graph.addEdge(edge);
								addedEdges.add(edge);
							}
						}
					}
				}
			}
		}
		
		Cell f = cells.get(from.x(), from.y());
		Vector t = new Vector();
		lit = to.listIterator();
		while(lit.hasNext()) {
			Cell next = (Cell)lit.next();
			t.add(cells.get(next.x(), next.y()));
		}
		HashMap path = DijkstraShortestPath.findPathBetween(graph, f, t);
		graph.removeAllEdges(addedEdges);
		graph.addAllVertices(removedVertices);
		graph.addAllEdges(removedEdges);
		retrieve();
		return path;
	}
	
	public boolean pathExists(Cell from, Cell to) {
		remove();
		Cell f = cells.get(from.x(), from.y());
		Cell t = cells.get(to.x(), to.y());			
		ConnectivityInspector inspector = new ConnectivityInspector(graph);
		boolean exists = inspector.pathExists(f, t);
		retrieve();
		return exists;
	}	
	
	public Set connectedSetOf(Cell from) {
		remove();
		Cell f = cells.get(from.x(), from.y());		
		ConnectivityInspector inspector = new ConnectivityInspector(graph);
		Set connected = inspector.connectedSetOf(f);
		retrieve();
		return connected;
	}
	
	public Vector neighbors(Cell vertex) {
		remove();
		Cell v = cells.get(vertex.x(), vertex.y());			
		Vector neighbors = new Vector();
		List edges = graph.edgesOf(v);
		ListIterator lit = edges.listIterator();
		while(lit.hasNext()) {
			Edge next = (Edge)lit.next();
			Cell neighbor = (Cell)next.oppositeVertex(v);
			neighbors.add(neighbor);
		}
		retrieve();
		return neighbors;
	}
	
	public String toString() {
		String str = "(CellGraph[walledges=" + walledges.toString() + "][graph=" + graph.toString() + "])";
		return str;
	}
	
	private void remove() {
		graph.removeAllEdges(walledges);		
	}
	
	private void retrieve() {
		graph.addAllEdges(walledges);
	}
}
