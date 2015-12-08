import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class LTTattle extends LTC {

	public LTTattle() {
		super();
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		LTTattle app = new LTTattle();
		app.fout = new File("/tmp/output_lttattle.txt");
		app.fos = new FileOutputStream(app.fout);
		app.bw = new BufferedWriter(new OutputStreamWriter(app.fos));
		long start = System.currentTimeMillis();
		app.noOfMonteCarloSims = Integer.parseInt(args[6]);
		app.loadGraph(args[1]);
		app.loadProbabilities(args[2]);

		app.seedSet = app.runCELF(Integer.parseInt(args[0]));
		app.executorService.shutdown();
		long end = System.currentTimeMillis();
		System.out.println("Time taken " + (end - start));
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