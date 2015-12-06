import java.io.File;
import java.io.IOException;

public class Main {
	private static String FILE_PATH;

	@SuppressWarnings("unchecked")
	public static void main(String[] arg) throws IOException {
		String trainFile = arg[0];
		String model = arg[1];
		String seedSet = arg[2];
		String monteCarlo = arg[3];

		File _f = new File("Main.java");
		try {
			FILE_PATH = _f.getCanonicalFile().getParentFile().getCanonicalPath();
		} catch (Exception e) {
		}
		_f = new File(FILE_PATH + "/datasets");
		if (!_f.exists()) {
			_f.mkdirs();
		}

		GraphGeneration graphGene = new GraphGeneration(trainFile);

		String[] args = new String[7];
		args[0] = seedSet;
		args[1] = graphGene.getEdgeFile();
		args[2] = graphGene.getAdoProbFile();
		args[3] = graphGene.getAvgRateFile();
		args[4] = graphGene.getMovieRateFile();
		args[5] = trainFile;
		args[6] = monteCarlo;

		switch (model) {
		case "LTC": {
			LTC.main(args);
			break;
		}
		case "LTClassical": {
			LTClassical.main(args);
			break;
		}
		case "LTRatings": {
			LTRatings.main(args);
			break;
		}
		case "LTTattle": {
			LTTattle.main(args);
			break;
		}
		default:
			break;
		}
	}
}