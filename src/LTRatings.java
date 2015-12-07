import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;

public class LTRatings extends LTC {

	public LTRatings() {
		super();
		noOfMonteCarloSims = 1;
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		LTRatings app = new LTRatings();
		app.fout = new File("output_ltratings.txt");
		app.fos = new FileOutputStream(app.fout);
		app.bw = new BufferedWriter(new OutputStreamWriter(app.fos));
		long start = System.currentTimeMillis();
		app.noOfMonteCarloSims = Integer.parseInt(args[6]);
		app.loadGraph(args[1]);
		app.loadProbabilities(args[2]);
		app.loadRatings(args[3], args[4], args[5]);
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
}
