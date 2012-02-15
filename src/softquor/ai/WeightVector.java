/*
 * Created on Sep 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package softquor.ai;
import java.io.*;
import java.util.StringTokenizer;
import java.util.Random;

/**
 * @author lglenden
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WeightVector {
	private double[] weights;
	private String file;
	private int id;
	
	public WeightVector(double[] weights) {
		this.weights = weights;
		file = null;
		id = -1;
	}
	
	public WeightVector(String infile) {
		file = infile;
		setId();
		try {
			input();
		}
		catch(IOException e) {
			weights = null;
		}
	}
	
	private void setId() {
		File f = new File(file);
		String fileName = f.getName();
		int extension = fileName.lastIndexOf('.');
		id = Integer.valueOf(fileName.substring(0, extension)).intValue();
	}
	
	public int id() {
		return id;
	}
	
	public static double[] random(int numWeights, double bound) {
		Random generator = new Random();
		double[] weights = new double[numWeights];
		for( int i=0;  i<numWeights;  i++ ) {
			weights[i] = ((generator.nextDouble()-0.5)*2.0)*bound;
		}
		return weights;
	}
	
	public double[] weights() {
		return weights;
	}
	
	public void weights(double[] newWeights) {
		weights = newWeights;    	
	}
	
	public boolean[] eval() {
		boolean[] eval = new boolean[weights.length];
		for( int i=0;  i<weights.length;  i++ ) {
			eval[i] = (weights[i] != 0.0);
		}
		return eval;
	}
	
	public void add(double[] newWeights) {
		for( int i=0;  i<weights.length;  i++ ) {
			weights[i] += newWeights[i];
		}
	}
	
	public String file() {
		return file;
	}
	
	public void file(String file) {
		this.file = file;
		setId();
	}    
	
	public String toString() {
		StringBuffer output = new StringBuffer();
		if(weights != null) {
			for( int i=0;  i<weights.length;  i++ ) {
				output.append(String.valueOf(weights[i]));
				if(i<weights.length-1) {
					output.append(" ");
				}
			}
		}
		return output.toString();    	
	}
	
	public void output() throws IOException {
		FileWriter fwriter = new FileWriter(file);
		BufferedWriter writer = new BufferedWriter(fwriter);
		writer.write(toString().toCharArray());
		writer.close();
		fwriter.close();
	}
	
	private void input() throws IOException {
		FileReader freader = new FileReader(file);
		BufferedReader reader = new BufferedReader(freader);
		String input = reader.readLine();
		reader.close();
		freader.close();
		StringTokenizer token = new StringTokenizer(input);
		int length = token.countTokens();
		weights = new double[length];
		for( int i=0;  i<length;  i++ ) {
			weights[i] = Double.parseDouble(token.nextToken());
		}
	}
	
}
