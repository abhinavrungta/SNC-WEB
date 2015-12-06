import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.distribution.BetaDistribution;

public class GraphGeneration {
	private static String edgeFilePath;
	private static String adoProbFilePath;
	private static String avgRateFilePath;
	private static String movieRateFilePath;

	public static String getEdgeFile() {
		return edgeFilePath;
	}

	public static String getAdoProbFile() {
		return adoProbFilePath;
	}

	public static String getAvgRateFile() {
		return avgRateFilePath;
	}

	public static String getMovieRateFile() {
		return movieRateFilePath;
	}

	public GraphGeneration(String trainingPath) throws IOException {
		File trainingFile = new File(trainingPath);
		readFromFile(trainingFile);
	}

	private static void readFromFile(File file) throws IOException {
		String line = null;
		Map<Integer, List<MovieRating>> userRatings = new HashMap<Integer, List<MovieRating>>();
		Map<Long, List<Double>> movieRatings = new HashMap<Long, List<Double>>();

		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		long count = 1;
		long sum = 0;
		while ((line = bufferedReader.readLine()) != null) {
			String[] attributes = line.split(",");
			int userId = Integer.parseInt(attributes[0]);
			long movieId = Long.parseLong(attributes[1]);
			double rating = Double.parseDouble(attributes[2]);
			long timeStamp = Long.parseLong(attributes[3]);
			MovieRating movieRating = new MovieRating(movieId, rating, timeStamp);

			List<MovieRating> movieList = userRatings.get(userId);
			if (movieList == null)
				movieList = new ArrayList<MovieRating>();
			movieList.add(movieRating);
			userRatings.put(userId, movieList);

			List<Double> ratingList = movieRatings.get(movieId);
			if (ratingList == null)
				ratingList = new ArrayList<Double>();
			ratingList.add(rating);
			movieRatings.put(movieId, ratingList);

			count++;
			sum += (long) rating;
		}

		double avgRating = (double) (sum / count);

		System.out.println("Average of all movie ratings is " + avgRating);
		System.out.println("Number of users is " + userRatings.size());
		System.out.println("Number of movies is " + movieRatings.size());

		calculateAvgMovieRating(movieRatings);
		System.out.println("Average of movie ratings done");

		calculateAdoptionProbabilties(userRatings, avgRating);
		System.out.println("Average of user ratings done");

		calculateEdges(userRatings);

		bufferedReader.close();
		fileReader.close();
	}

	private static void calculateEdges(Map<Integer, List<MovieRating>> userRatings) throws IOException {
		List<Integer> users = new ArrayList<Integer>(userRatings.keySet());
		Map<Integer, Map<Integer, Double>> edges = new HashMap<Integer, Map<Integer, Double>>();
		int size = users.size();
		int edgeCount = 0;
		for (int i = 0; i < size - 1; i++) {
			int userA = users.get(i);
			for (int j = i + 1; j < size; j++) {
				int userB = users.get(j);
				if (isEdgePresent(userRatings.get(userA), userRatings.get(userB))) {
					createEdge(edges, userA, userB, userRatings);
					edgeCount++;
				}
			}
		}
		System.out.println("Number of edges is " + edgeCount);

		edgeFilePath = "datasets/edge_weights.txt";
		FileWriter fileWriter = new FileWriter(edgeFilePath);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		for (int i = 0; i < size; i++) {
			int user = users.get(i);
			Map<Integer, Double> map = edges.get(user);
			if (map == null)
				continue;
			Set<Integer> neighbours = map.keySet();
			double sum = 0;
			for (int neighbour : neighbours)
				sum += map.get(neighbour);
			for (int neighbour : neighbours) {
				double weight = (sum == 0) ? 0 : (map.get(neighbour) / sum);
				bufferedWriter.write(neighbour + "\t" + user + "\t" + weight + "\n");
				// map.put(neighbour, weight);
			}
			// edges.put(user, map);
		}

		bufferedWriter.close();
		fileWriter.close();
		System.out.println("Edge weight calculation done");
	}

	private static void createEdge(Map<Integer, Map<Integer, Double>> edges, int userA, int userB,
			Map<Integer, List<MovieRating>> userRatings) {
		List<MovieRating> userARatings = userRatings.get(userA);
		List<MovieRating> userBRatings = userRatings.get(userB);
		int timesARatedLater = 0;
		int timesBRatedLater = 0;
		for (MovieRating userARating : userARatings) {
			for (MovieRating userBRating : userBRatings) {
				if (userARating.getMovieId() == userBRating.getMovieId()) {
					if (userARating.getRatingTime() > userBRating.getRatingTime())
						timesARatedLater++;
					else
						timesBRatedLater++;
				}
			}
		}
		addEdgeToNode(edges, userA, userB, timesARatedLater);
		addEdgeToNode(edges, userB, userA, timesBRatedLater);
	}

	private static void addEdgeToNode(Map<Integer, Map<Integer, Double>> edges, int userA, int userB, double weight) {
		Map<Integer, Double> edgesIntoA = edges.get(userA);
		if (edgesIntoA == null)
			edgesIntoA = new HashMap<Integer, Double>();
		edgesIntoA.put(userB, weight);
		edges.put(userA, edgesIntoA);
	}

	private static boolean isEdgePresent(List<MovieRating> A, List<MovieRating> B) {
		double threshold = 0.25;
		boolean result = false;
		Set<Long> moviesA = new HashSet<Long>();
		Set<Long> moviesB = new HashSet<Long>();
		Set<Long> AintB = new HashSet<Long>();
		Set<Long> AunionB = new HashSet<Long>();

		int sizeA = A.size();
		for (int i = 0; i < sizeA; i++) {
			long movieId = A.get(i).getMovieId();
			moviesA.add(movieId);
			AunionB.add(movieId);
		}

		int sizeB = B.size();
		for (int i = 0; i < sizeB; i++) {
			long movieId = B.get(i).getMovieId();
			moviesB.add(movieId);
			AunionB.add(movieId);
		}

		for (int i = 0; i < sizeA; i++) {
			long movieId = A.get(i).getMovieId();
			if (moviesB.contains(movieId))
				AintB.add(movieId);
		}

		int num = AintB.size();
		int den = AunionB.size();

		double similarity = ((double) num) / den;
		if (similarity >= 0.25)
			return true;
		else
			return false;
	}

	private static void calculateAvgMovieRating(Map<Long, List<Double>> movieRatings) throws IOException {
		Iterator iterator = movieRatings.entrySet().iterator();
		double sum;
		int count;

		movieRateFilePath = "datasets/average_movie_ratings.txt";
		FileWriter fileWriter = new FileWriter(movieRateFilePath);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			sum = 0;
			List<Double> list = (List<Double>) pair.getValue();
			count = list.size();
			for (int i = 0; i < count; i++)
				sum += list.get(i);
			double avg = sum / count;
			bufferedWriter.write(pair.getKey() + "\t" + avg + "\n");
		}

		bufferedWriter.close();
		fileWriter.close();
	}

	private static void calculateAdoptionProbabilties(Map<Integer, List<MovieRating>> userRatings, double avgRating)
			throws IOException {
		BetaDistribution mu = new BetaDistribution(0.001, 0.001);
		Iterator iterator = userRatings.entrySet().iterator();
		double sum;
		int count;
		int adoptCount = 0;

		avgRateFilePath = "datasets/average_user_ratings.txt";
		FileWriter fileWriter1 = new FileWriter(avgRateFilePath);
		BufferedWriter bufferedWriter1 = new BufferedWriter(fileWriter1);

		adoProbFilePath = "datasets/adoption_probabilities.txt";
		FileWriter fileWriter2 = new FileWriter(adoProbFilePath);
		BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2);

		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			int userId = (Integer) pair.getKey();
			sum = 0;
			List<MovieRating> list = (List<MovieRating>) pair.getValue();
			count = list.size();
			adoptCount = 0;
			for (int i = 0; i < count; i++) {
				double ratingValue = list.get(i).getRatingValue();
				sum += ratingValue;
				if (ratingValue >= avgRating)
					adoptCount++;
			}

			double avg = sum / count;
			bufferedWriter1.write(userId + "\t" + avg + "\n");

			double adoptProbability = ((double) adoptCount) / count;
			bufferedWriter2.write(userId + "\t" + adoptProbability + "\t" + ThreadLocalRandom.current().nextFloat()
					+ "\t" + mu.inverseCumulativeProbability(ThreadLocalRandom.current().nextDouble()) + "\n");
			// bufferedWriter2.write(userId + "\t" + adoptProbability + "\n");

		}

		bufferedWriter2.close();
		fileWriter2.close();
		bufferedWriter1.close();
		fileWriter1.close();
	}

}