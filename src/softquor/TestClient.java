/*
 * Created on May 1, 2004
 *
 */
package softquor;
import softquor.ai.Agent;
import softquor.ai.FeatureEvaluator;
import softquor.ai.ABStrategy;
import softquor.ai.WeightVector;

/**
 * @author Lisa Glendenning
 *
 */
public class TestClient implements ClientListener {
	
	public String host;
	public int port;
	public AgentClient client;
	
	public TestClient(String dir, int id, boolean debug) {
		// build agent
		FeatureEvaluator eval = new FeatureEvaluator();
		ABStrategy strat = new ABStrategy(debug);
		WeightVector wv = new WeightVector(dir + "/" + id + ".weights");
		Agent agent = new Agent(id, eval, strat, wv);
		this.client = new AgentClient(agent, debug);
		client.addListener(this);
	}
	
	private void agentTest(String host, int port) 
		throws GameException {
		this.host = host;
		this.port = port;

		client.connect(host, port);
	}
	
	public void listenEvent(ClientEvent e) {
		if(e.getCommand().equals("disconnect")) {
			client.connect(host, port);
		}
	}
	
	public static void main(String[] args) throws GameException {
		TestClient tester = new TestClient(args[2], 
				Integer.valueOf(args[3]).intValue(), false);

		tester.agentTest(args[0], Integer.valueOf(args[1]).intValue());
	}
}
