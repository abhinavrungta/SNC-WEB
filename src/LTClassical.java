import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LTClassical extends LTC {

	public LTClassical() {
		super();
		noOfMonteCarloSims = 1;
	}

	public static void main(String[] args) throws FileNotFoundException {
		LTClassical app = new LTClassical();
		long start = System.currentTimeMillis();
		app.noOfMonteCarloSims = Integer.parseInt(args[6]);
		app.loadGraph(args[1]);
		app.loadProbabilities(args[2]);
		app.seedSet = app.runCELF(Integer.parseInt(args[0]));
		app.executorService.shutdown();
		long end = System.currentTimeMillis();
		System.out.println("Time taken " + (end - start));
	}

	public void loadProbabilities(String adoptionProbFile) throws FileNotFoundException {
		File inputFile = new File(adoptionProbFile);
		Scanner stdin = new Scanner(inputFile);
		while (stdin.hasNext()) {
			String[] data = stdin.nextLine().split("\t");
			int userId = Integer.parseInt(data[0]);
			float activation = Float.parseFloat(data[2]);
			if (usersList.containsKey(userId)) {
				Node node = usersList.get(userId);
				node.setActivationThreshold(activation);
				node.setAdoptionProbability(1);
				node.setPromotionProbability(0);
			}
		}
		stdin.close();
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
