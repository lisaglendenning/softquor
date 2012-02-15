/*
 * Created on Sep 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package softquor.ai;
import softquor.GameException;
import softquor.board.Move;

import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileFilter;

/**
 * @author lglenden
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Breeder {
	
	private String wvdir;
	private Vector weights;
	private Strategy strategy;	
	private FeatureEvaluator evaluator;
	private AIBoard board;
	private Random generator;
	
	public static final int TURNCAP = 182;
	public static final int WIN = 0;
	public static final int LOSE = 1;
	public static final int DRAW = 2;
	
	public static final double MUTATION = 0.01;
	public static final double HASFEATURE = 0.3;
	public static final double LOSEFEATURE = 0.1;
	public static final double MUTFEATURE = 0.1;
	public static final double ELITISM = 0.2;
	
	public Breeder(String dir, Strategy s, FeatureEvaluator e) {
		wvdir = dir;
		weights = new Vector();
		strategy = s;
		evaluator = e;
		generator = new Random();
	}
	
	public void initWeights() {
		weights = readWeights(wvdir);
	}
	
	private Vector readWeights(String dir) {
		Vector w = new Vector();
		File directory = new File(dir);
		File[] files = directory.listFiles(new ExtensionFilter(".weights"));
		for( int i=0;  i<files.length;  i++ ) {
			WeightVector wv = new WeightVector(files[i].getAbsolutePath());
			w.add(wv);
		}
		return w;
	}
	
	public class ExtensionFilter implements FileFilter {
		public String extension;
		public ExtensionFilter(String extension) {
			this.extension = extension;
		}
		public boolean accept(File file) {
			return (file.getName().endsWith(extension));
		}
	}

	private File checkSuspend() {
		File directory = new File(wvdir);
		File[] files = directory.listFiles(new ExtensionFilter("suspend"));
		if(files.length > 0) {
			return files[0];
		}
		return null;
	}
	
	public void go(int testInterval, int timelimit) {
		boolean test = false;
		File suspend = checkSuspend();
		int nweights = weights.size();
		int[] fitness = new int[nweights];
		for( int i=0;  i<nweights;  i++ ) {
			fitness[i] = 0;
		}
		int rbegin = 0;
		int ibegin = 0;
		int jbegin = 0;
		if(suspend != null) {
			String input = "";
			try {
				FileReader freader = new FileReader("status");
				BufferedReader reader = new BufferedReader(freader);
				input = reader.readLine();
				reader.close();
				freader.close();
				suspend.delete();
				suspend = null;
				StringTokenizer token = new StringTokenizer(input);
				test = (token.nextToken().equals("t")) ? true : false;
				rbegin = Integer.parseInt(token.nextToken());
				ibegin = Integer.parseInt(token.nextToken());
				jbegin = Integer.parseInt(token.nextToken());
				if(test) {
					int popFitness = Integer.parseInt(token.nextToken());
					if(!test(rbegin, ibegin, jbegin, timelimit, popFitness)) {
						System.out.println("Preempted");
						return;
					}
					rbegin++;
					test = false;
					ibegin = 0;
					jbegin = 0;
				}
				else {
					for( int i=0;  i<nweights;  i++ ) {
						fitness[i] = Integer.parseInt(token.nextToken());
					}
				}
			}
			catch(Exception e) {

			}
		}

		for( int r=rbegin;  suspend==null;  r++ ) {
			for( int i=ibegin;  i<nweights;  i++ ) {
				for( int j=jbegin;  j<nweights;  j++ ) {
					if(i == j) {
						continue;
					}
					WeightVector wv1 = (WeightVector)weights.get(i);
					WeightVector wv2 = (WeightVector)weights.get(j);
					int outcome = play(wv1, wv2, r, timelimit);
					if(outcome == WIN) {
						fitness[wv1.id()]++; 
					}
					else if(outcome == LOSE) {
						fitness[wv2.id()]++;
					}
					
					// output status
					try {
						String output = "g " + r + " " + (i+1) + " " + (j+1);
						for( int f=0;  f<nweights;  f++ ) {
							output += " " + fitness[f];
						}
						FileWriter fwriter = new FileWriter(wvdir + "/status");
						BufferedWriter writer = new BufferedWriter(fwriter);
						writer.write(output.toCharArray());
						writer.close();
						fwriter.close();
					}
					catch(IOException e) {
						System.out.println(e.getMessage());
						return;
					}
					suspend = checkSuspend();
					if(suspend != null) {
						break;
					}
				}
				if(suspend != null) {
					break;
				}
			}
			if(suspend != null) {
				System.out.println("Preempted");
				return;
			}

			for( int f=0;  f<nweights;  f++ ) {
				WeightVector next = (WeightVector)weights.get(f);
				try {
					logWeight(next, fitness[next.id()], true);
				}
				catch(IOException e) {
					
				}
			}
			
			// clear variables
			mate(fitness);
			fitness = new int[nweights];
			for( int f=0;  f<nweights; f++ ) {
				fitness[f] = 0;
			}
			ibegin=0; 
			jbegin=0; 
			if((r%testInterval) == 0) {
				if(!test(r, 0, 0, timelimit, 0)) {
					System.out.println("Preempted");
					return;
				}
			}
		}
	}
	
	public void championship(int timelimit) {
		File suspend = checkSuspend();
		int nweights = weights.size();
		int[] fitness = new int[nweights];
		for( int i=0;  i<nweights;  i++ ) {
			fitness[i] = 0;
		}
		int ibegin = 0;
		int jbegin = 0;
		int gameLength = 0;
		if(suspend != null) {
			String input = "";
			try {
				FileReader freader = new FileReader("status");
				BufferedReader reader = new BufferedReader(freader);
				input = reader.readLine();
				reader.close();
				freader.close();
				suspend.delete();
				suspend = null;
				StringTokenizer token = new StringTokenizer(input);
				ibegin = Integer.parseInt(token.nextToken());
				jbegin = Integer.parseInt(token.nextToken());
				for( int i=0;  i<nweights;  i++ ) {
					fitness[i] = Integer.parseInt(token.nextToken());
				}
			}
			catch(Exception e) {

			}
		}

		for( int i=ibegin;  i<nweights;  i++ ) {
			for( int j=jbegin;  j<nweights;  j++ ) {
				if(i == j) {
					continue;
				}
				WeightVector wv1 = (WeightVector)weights.get(i);
				WeightVector wv2 = (WeightVector)weights.get(j);
				int outcome = play(wv1, wv2, 0, timelimit);
				gameLength += board.turns();
				if(outcome == WIN) {
					fitness[wv1.id()]++; 
				}
				else if(outcome == LOSE) {
					fitness[wv2.id()]++;
				}
					
				// output status
				try {
					String output = (i+1) + " " + (j+1);
					for( int f=0;  f<nweights;  f++ ) {
						output += " " + fitness[f];
					}
					FileWriter fwriter = new FileWriter(wvdir + "/status");
					BufferedWriter writer = new BufferedWriter(fwriter);
					writer.write(output.toCharArray());
					writer.close();
					fwriter.close();
				}
				catch(IOException e) {
					System.out.println(e.getMessage());
					return;
				}
				suspend = checkSuspend();
				if(suspend != null) {
					break;
				}
			}
			if(suspend != null) {
				System.out.println("Preempted!");
				return;
			}
		}

		for( int f=0;  f<nweights;  f++ ) {
			WeightVector next = (WeightVector)weights.get(f);
			try {
				logWeight(next, fitness[next.id()], true);
			}
			catch(IOException e) {
					
			}
		}
		
		System.out.println("ngames=" + (nweights*nweights-nweights) + 
			", gameLength=" + gameLength);
		strategy.printStats();
	}	
	
	private void mate(int[] fitness) {
		int noffspring = (int)Math.round(((double)weights.size())*(1.0-ELITISM));
		Vector offspring = new Vector(noffspring);
		int sum = 0;
		for( int i=0;  i<weights.size();  i++ ) {
			sum += fitness[i];
		}
		
		// create offspring
		for( int i=0;  i<noffspring;  i++ ) {
			// Select parents
			WeightVector[] parents = new WeightVector[2];
			for( int p=0;  p<2;  p++ ) {
				int rand = generator.nextInt(sum+1);
				//System.out.print(rand);
				int subsum = 0;
				for( int f=0;  f<weights.size();  f++ ) {
					WeightVector wv = (WeightVector)weights.get(f);
					subsum += fitness[wv.id()];
					if(subsum >= rand) {
						parents[p] = wv;
						//System.out.println(" => Parent " + f);
						//System.out.println(parents[p]);
						break;
					}
				}
			}
			
			double[] w = mate(parents);
			//System.out.print("mate=> ");
			//for( int j=0;  j<w.length;  j++ ) {
				//System.out.print("[" + j + ","+ w[j] + "]");
			//}
			//System.out.println();
			mutate(w);
			//System.out.print("mutate=> ");
			//for( int j=0;  j<w.length;  j++ ) {
				//System.out.print("[" + j + ","+ w[j] + "]");
			//}
			//System.out.println();
			offspring.add(new WeightVector(w));
		}
		
		// merge populations
		quicksort(weights, fitness, 0, weights.size()-1);
		//for( int i=0;  i<weights.size();  i++ ) {
			//System.out.println(((WeightVector)weights.get(i)).id() + " " + (WeightVector)weights.get(i));
		//}
		//System.out.println();
		for( int i=0;  i<noffspring;  i++ ) {
			WeightVector child = (WeightVector)offspring.get(i);
			WeightVector old = (WeightVector)weights.get(i);
			child.file(old.file());
			weights.set(i, child);
			try {
				child.output();
			}
			catch(IOException e) {
				System.err.println("Can't log child!");
			}
		}
	}
	
	private void quicksort(Vector a, int[] f, int lo0, int hi0) {
		//System.out.println("sort " + lo0 + " " + hi0);
		if(lo0 >= hi0) {
			//System.out.println("lo0<=hi0");
			return;
		}
        else if(lo0 == hi0-1) {
        	//System.out.println("lo0==hi0-1");
            if(f[lo0] > f[hi0]) {
                swap(a, f, lo0, hi0);
            }
            return;
        }
        int lo = lo0;
        int hi = hi0;
        int p = (lo + hi) / 2;
        int pivot = f[p];
		swap(a, f, p, hi);

		while(lo < hi) {
            while(f[lo]<=pivot && lo<hi) {
            	lo++;
            }
            
            while(pivot<=f[hi] && lo <hi) {
            	hi--;
            }

            if(lo < hi) {
            	swap(a, f, lo, hi);
            }
		}

		swap(a, f, hi0, hi);

		quicksort(a, f, lo0, lo-1);
		quicksort(a, f, hi+1, hi0);
    }
	
	private void swap(Vector a, int[] f, int i, int j) {
		//System.out.println("swapf " + i + " " + j);
		int temp = f[i];
        f[i] = f[j];
        f[j] = temp;
        
        swap(a, i, j);
	}
	
	private void swap(Vector a, int i, int j) {
    	//System.out.println("swapv " + i + " " + j);
        Object tempWV = a.get(i);
        a.set(i, a.get(j));
        a.set(j, tempWV);
	}
	
	private double[] mate(WeightVector[] parents) {
		int nweights = parents[0].weights().length;
		double[] w = new double[nweights];
		for( int i=0;  i<nweights;  i++ ) {
			if(generator.nextBoolean()) {
				w[i] = parents[0].weights()[i];
			}
			else {
				w[i] = parents[1].weights()[i];
			}
		}
		
		return w;
	}
	
	private void mutate(double[] w) {
		for( int i=0;  i<w.length;  i++ ) {
			if(generator.nextDouble() <= MUTATION) {
				//System.out.print("[mutate " + i);
				if(w[i] == 0.0) {
					//System.out.print(" zero");
					w[i] = (generator.nextDouble()-0.5)*2.0;
				}
				else {
					if(generator.nextDouble() <= LOSEFEATURE) {
						//System.out.print(" lose");
						w[i] = 0.0;
					}
					else {
						double amount = generator.nextDouble()*MUTFEATURE;
						//System.out.print(" amt=" + amount);
						if(generator.nextBoolean()) {
							//System.out.print(" +");
							w[i] += amount;
						}
						else {
							//System.out.print(" -");
							w[i] -= amount;
						}
					}
				}
				//System.out.print("]");
			}
		}
		//System.out.println();
	}
	
	
	public boolean test(int round, int ibegin, int jbegin, int timelimit, int fitness) {
		String testdir = wvdir + "/test";
		Vector testWeights = readWeights(testdir);
		int popFitness = fitness;
		int nweights = weights.size();
		int ntests = testWeights.size();
		File suspend = null;
		for( int i=ibegin;  i<nweights;  i++ ) {
			WeightVector nextWeight = (WeightVector)weights.get(i);
			for( int j=jbegin;  j<ntests;  j++ ) {
				WeightVector nextTest = (WeightVector)testWeights.get(j);
				int outcome = play(nextWeight, nextTest, 0, timelimit);
				if(outcome == WIN) {
					popFitness++; 
				}
				outcome = play(nextTest, nextWeight, 0, timelimit);
				if(outcome == LOSE) {
					popFitness++;
				}
				suspend = checkSuspend();
				if(suspend != null) {
					try {
						String output = "t " + round + " " + (i+1) + " " + (j+1) + " " + popFitness;
						FileWriter fwriter = new FileWriter(wvdir + "/status");
						BufferedWriter writer = new BufferedWriter(fwriter);
						writer.write(output.toCharArray());
						writer.close();
						fwriter.close();
					}
					catch(IOException e) {
						System.out.println(e.getMessage());
					}
					return false;
				}
			}
		}
		try {	
			FileWriter fwriter = new FileWriter(testdir + "/fitness", true);
			BufferedWriter writer = new BufferedWriter(fwriter);
			String output = round + " " + popFitness + "\n";
			writer.write(output.toCharArray());
			writer.close();
			fwriter.close();
		}
		catch(IOException e) {
			System.out.println(e.getMessage());
			return false;
		}
		return true;
	}
	
	private int play(WeightVector wv1, WeightVector wv2, int round, int timelimit) {
		Agent[] agents = new Agent[2];
		agents[0] = new Agent(wv1.id(), evaluator, strategy, wv1);
		agents[1] = new Agent(wv2.id(), evaluator, strategy, wv2);
		int outcome = DRAW;
		try {		
			board = new AIBoard();
			board.register(agents[0].id());
			board.register(agents[1].id());
			if(round%2 == 0) {
				board.start(agents[0].id());
			}
			else {
				board.start(agents[1].id());
			}
			//System.out.println("\nGame " + round + "!");
			//System.out.println(board.toText(0));
			int turns = 0;
			while(!board.gameOver() && turns<TURNCAP) {
				Agent next = nextPlayer(agents, board.nextPlayer());
				Move nextMove = next.turn(board, timelimit);
				board.move(nextMove);
				//System.out.println(board.toText(0));
				turns++;
			}
			// draw
			int winner;
			if(turns==TURNCAP && !board.gameOver()) {
				winner = -1;
			}
			else {
				winner = board.nextPlayer();
				if(winner == wv1.id()) {
					outcome = WIN;
				}
				else {
					outcome = LOSE;
				}
			}
			//agents[0].stats();
			//agents[1].stats();
			//System.out.println("Player " + winner + " won!");
			/*for( int i=0;  i<agents.length;  i++ ) {
				if(agents[i].id() == winner) {
					agents[i].logGame(board.getOpponent(agents[i].id()), true);
				}
				else {
					agents[i].logGame(board.getOpponent(agents[i].id()), false);
				}
			}*/
		}
		catch(GameException e) {
			agents[0].logError(e.getMessage());
			agents[1].logError(e.getMessage());
		}
		return outcome;
	}
	
	private Agent nextPlayer(Agent[] agents, int id) {
		for( int i=0;  i<agents.length;  i++ ) {
			if(agents[i].id() == id) {
				return agents[i];
			}
		}
		return null;
	}
	
	private void logWeight(WeightVector wv, int fitness, boolean append) throws IOException {
		String weightFile = wv.file();
		int extension = weightFile.lastIndexOf('.');
		String logFile = weightFile.substring(0, extension) + ".history";
		String message = "[" + wv + "][" + fitness + "]\n";
		FileWriter fwriter = new FileWriter(logFile, append);
		BufferedWriter writer = new BufferedWriter(fwriter);
		writer.write(message.toCharArray());
		writer.close();
		fwriter.close();
	}
	
	public void newPopulation(int numAgents, int numWeights, 
			double bound) throws IOException {
		for( int i=0;  i<numAgents;  i++ ) {
			double[] w = WeightVector.random(numWeights, bound);
			for( int j=0;  j<numWeights;  j++ ) {
				if(generator.nextDouble() > HASFEATURE) {
					w[j] = 0.0;
				}
			}
			WeightVector wv = new WeightVector(w);
			wv.file(wvdir + "/" + i + ".weights");
			wv.output();
			weights.add(wv);
		}
	}
	
	public static void main(String[] args) throws IOException {
		String dir = args[0];
		Strategy strategy = new ABStrategy(false);
		FeatureEvaluator eval = new FeatureEvaluator();
		Breeder breeder = new Breeder(dir, strategy, eval);
		if(args[1].equals("-i")) {
			int nweights = Integer.valueOf(args[2]).intValue();
			double bound = Double.valueOf(args[3]).doubleValue();
			breeder.newPopulation(nweights, FeatureEvaluator.NFEATURES, bound);
		}
		else if(args[1].equals("-c")) {
			int timelimit = Integer.valueOf(args[2]).intValue();
			breeder.initWeights();
			breeder.championship(timelimit);
		}
		else {
			int testInterval = Integer.valueOf(args[1]).intValue();
			int timelimit = Integer.valueOf(args[2]).intValue();
			breeder.initWeights();
			breeder.go(testInterval, timelimit);
		}
		System.out.println("Done!");
		System.exit(0);
	}
}
