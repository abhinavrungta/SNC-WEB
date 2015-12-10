import java.io.IOException;

public class Main {
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
		for (String model : models) {
			System.out.println("Model is " + model);
			switch (model) {
			case "color": {
				args[6] = monteCarlo;
				LTC.main(args);
				break;
			}
			case "classic": {
				args[6] = "1";
				LTClassical.main(args);
				break;
			}
			case "ratings": {
				args[6] = "1";
				LTRatings.main(args);
				break;
			}
			case "tattle": {
				args[6] = monteCarlo;
				LTTattle.main(args);
				break;
			}
			case "tim": {
				RIS.main(args);
				break;
			}
			default:
				break;
			}
		}

	}
}