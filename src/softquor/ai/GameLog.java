/*
 * Created on Sep 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package softquor.ai;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Vector;
import java.io.IOException;

/**
 * @author lglenden
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class GameLog {
	
	Vector features;
	
	public GameLog() {
		features = new Vector();
	}
	
	public void feature(FeatureVector fv) {
		features.add(fv);		
	}
	
	public Vector features() {
		return features;
	}
	
	public void restart() {
		features.clear();
	}
	
	public void output(String file, String message) throws IOException {
		FileWriter fwriter = new FileWriter(file, true);
		BufferedWriter writer = new BufferedWriter(fwriter);
		writer.write(message.toCharArray());
		writer.close();
		fwriter.close();
	}
}
