/*
 * Created on Sep 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package softquor.ai;

/**
 * @author lglenden
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FeatureVector {
	private double[] features;
	
	public FeatureVector() {
		features = null;
	}
	
	public void features(double[] f) {
		features = f;
	}
	
	public double[] features() {
		return features;
	}
	
	public void mult(double gamma) {
		if(features != null) {
			for( int i=0;  i<features.length;  i++ ) {
				features[i] *= gamma;
			}
		}		
	}
	
	public void add(FeatureVector fv) {
		if(features != null) {
			add(features, fv.features());
		}
	}
	
	private void add(double[] sum, double[] addends) {			
		for( int i=0;  i<sum.length;  i++ ) {
			sum[i] += addends[i];
		}	
	}
		
	// assumes wv has equal length
	public double apply(WeightVector wv) {
		double sum = 0;
		double[] weights = wv.weights();
		if(weights != null && features != null) {
			for( int i=0;  i<weights.length;  i++ ) {
				sum += weights[i]*features[i];
			}
		}
		return sum;
	}
	
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("(FeatureVector[features=");
		for( int i=0;  i<features.length;  i++ ) {
			str.append(features[i]);
			if(i < features.length-1) {
				str.append(",");
			}
		}
		str.append("])");
		return str.toString();
	}
		
}
