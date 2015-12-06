import java.io.FileNotFoundException;
import java.util.Iterator;

public class LTTattle extends LTC {

	public LTTattle() {
		super();
	}

	public static void main(String[] args) throws FileNotFoundException {
		LTTattle app = new LTTattle();
		long start = System.currentTimeMillis();
		app.loadGraph(args[1]);
		app.loadProbabilities(args[2]);
		app.loadValues(args[3]);

		app.seedSet = app.runCELF(Integer.parseInt(args[0]));
		app.executorService.shutdown();
		long end = System.currentTimeMillis();
		System.out.println("Time taken " + (end-start));
	}

	public float getRating(int nodeId, int productId, State state) {
		float rating = 0.0f;
		switch (state) {
		case ADOPT:
			rating = rMax;
			break;
		case PROMOTE:
			rating = rMax;
			break;
		case INHIBIT:
			rating = rMin;
			break;
		default:
			rating = rMax;
			break;
		}
		return rating;
	}
}
