import java.io.IOException;

public class Main {
	private static String FILE_PATH;

	@SuppressWarnings("unchecked")
	public static void main(String[] arg) throws IOException {
		String trainFile = arg[0];
		String[] models = arg[1].split(",");
		String seedSet = arg[2];
		String monteCarlo = arg[3];

		GraphGeneration graphGene = new GraphGeneration(trainFile);

		String[] args = new String[7];
		args[0] = seedSet;
		args[1] = graphGene.getEdgeFile();
		args[2] = graphGene.getAdoProbFile();
		args[3] = graphGene.getAvgRateFile();
		args[4] = graphGene.getMovieRateFile();
		args[5] = trainFile;
		args[6] = monteCarlo;
		for (String model : models) {
			System.out.println("Model is " + model);
			switch (model) {
			case "color": {
				LTC.main(args);
				break;
			}
			case "classic": {
				LTClassical.main(args);
				break;
			}
			case "ratings": {
				LTRatings.main(args);
				break;
			}
			case "tattle": {
				LTTattle.main(args);
				break;
			}
			default:
				break;
			}
		}

	}
}