import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class MarginalGain {
	int nodeId;
	float gain;

	public MarginalGain(int nodeId, float gain) {
		this.nodeId = nodeId;
		this.gain = gain;
	}
}

class GainComparator implements Comparator<MarginalGain> {

	@Override
	public int compare(MarginalGain o1, MarginalGain o2) {
		if (o1.gain > o2.gain)
			return -1;
		else if (o1.gain < o2.gain)
			return 1;
		else
			return 0;

	}

}

public class LTC {
	ConcurrentHashMap<Integer, Node> usersList;
	ConcurrentHashMap<Integer, Float> userRatings;
	ConcurrentHashMap<Integer, Float> movieRatings;
	static float rMin = 1.0f;
	static float rMax = 5.0f;
	int noOfMonteCarloSims = 1000;
	Set<Integer> seedSet;
	Random rand;
	ExecutorService executorService;
	File fout;
	FileOutputStream fos;
	BufferedWriter bw;
	int productId;

	public LTC(){
		int cores = 16;
		if (System.getProperty("cores") != null)
			cores = Integer.parseInt(System.getProperty("cores"));
		executorService = Executors.newFixedThreadPool(cores);
		productId = 0;
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		LTC app = new LTC();
		app.fout = new File("output_ltc.txt");
		app.fos = new FileOutputStream(app.fout);
		app.bw = new BufferedWriter(new OutputStreamWriter(app.fos));
		System.out.println(System.currentTimeMillis());
		app.noOfMonteCarloSims = Integer.parseInt(args[6]);
		app.loadGraph(args[1]);
		app.loadProbabilities(args[2]);
		app.loadRatings(args[3], args[4], args[5]);
		app.productId = app.movieRatings.keySet().iterator().next();
		app.seedSet = app.runCELF(Integer.parseInt(args[0]));
		Iterator<Integer> itr = app.seedSet.iterator();
		while (itr.hasNext()) {
			System.out.print(itr.next() + ", ");
		}
		app.executorService.shutdown();
		System.out.println(System.currentTimeMillis());
		app.serializeMap("graph_new.ser");
	}

	public void loadGraph(String edgeList) throws FileNotFoundException {
		File inputFile = new File(edgeList);
		usersList = new ConcurrentHashMap<Integer, Node>();
		Scanner stdin = new Scanner(inputFile);
		while (stdin.hasNext()) {
			String[] data = stdin.nextLine().split("\t");
			int user1 = Integer.parseInt(data[0]);
			int user2 = Integer.parseInt(data[1]);
			float weight = Float.parseFloat(data[2]);

			// add to user list if not already present.
			if (!usersList.containsKey(user1)) {
				usersList.put(user1, new Node(user1));
			}
			if (!usersList.containsKey(user2)) {
				usersList.put(user2, new Node(user2));
			}

			usersList.get(user1).addToOutLinkList(user2);
			// add a pair of outgoing edge and weight for user1.
			usersList.get(user2).addToInLinkList(user1, weight);

		}
		stdin.close();
	}

	public void loadRatings(String userRatingsFile, String movieRatingsFile, String inputFile)
			throws FileNotFoundException {
		// load ratings matrix.
		userRatings = new ConcurrentHashMap<Integer, Float>(usersList.size());
		movieRatings = new ConcurrentHashMap<Integer, Float>();

		File ratingsFile = new File(userRatingsFile);
		Scanner stdin = new Scanner(ratingsFile);
		while (stdin.hasNext()) {
			String[] data = stdin.nextLine().split("\t");
			int userId = Integer.parseInt(data[0]);
			float rating = Float.parseFloat(data[1]);
			userRatings.put(userId, rating);
		}
		stdin.close();

		ratingsFile = new File(movieRatingsFile);
		stdin = new Scanner(ratingsFile);
		while (stdin.hasNext()) {
			String[] data = stdin.nextLine().split("\t");
			int movieId = Integer.parseInt(data[0]);
			float rating = Float.parseFloat(data[1]);
			movieRatings.put(movieId, rating);
		}
		stdin.close();

		ratingsFile = new File(inputFile);
		stdin = new Scanner(ratingsFile);
		while (stdin.hasNext()) {
			String[] data = stdin.nextLine().split(",");
			int userId = Integer.parseInt(data[0]);
			int movieId = Integer.parseInt(data[1]);
			float rating = Float.parseFloat(data[2]);
			userRatings.put(userId, rating);
			movieRatings.put(movieId, rating);
		}
		stdin.close();
	}

	public void loadValues(String values) throws FileNotFoundException {
		// load values for rMin and rMax.
		// File valuesFile = new File(values);
		// Scanner stdin = new Scanner(valuesFile);
		// rMax = Float.parseFloat(stdin.nextLine().split("\t")[1]);
		// rMin = Float.parseFloat(stdin.nextLine().split("\t")[1]);
		// stdin.close();
	}

	public void loadProbabilities(String adoptionProbFile) throws FileNotFoundException {

		File inputFile = new File(adoptionProbFile);
		Scanner stdin = new Scanner(inputFile);
		while (stdin.hasNext()) {
			String[] data = stdin.nextLine().split("\t");
			int userId = Integer.parseInt(data[0]);
			double adoption = Double.parseDouble(data[1]);
			float activation = Float.parseFloat(data[2]);
			double prohibit = Double.parseDouble(data[3]);
			if (usersList.containsKey(userId)) {
				Node node = usersList.get(userId);
				node.setActivationThreshold(activation);
				node.setAdoptionProbability(adoption);
				node.setPromotionProbability(prohibit);
			}
		}
		stdin.close();
	}

	public float getRating(int nodeId, int productId, State state) {
		float rating = 0.0f;
		switch (state) {
		case ADOPT:
			rating = (userRatings.get(nodeId) + movieRatings.get(productId)) / 2.0f;
			break;
		case PROMOTE:
			rating = rMax;
			break;
		case INHIBIT:
			rating = rMin;
			break;
		default:
			rating = (userRatings.get(nodeId) + movieRatings.get(productId)) / 2.0f;
			break;
		}
		return rating;
	}

	public ConcurrentHashMap<Integer, Node> getUsersList() {
		return usersList;
	}

	public Set<Integer> getCalculatedSeed() {
		return seedSet;
	}

	// calculate expected spread for a seed set by running monte carlo
	// simulations i.e. repeat coverage for 10K times where random behavior
	// decides if a node is getting into ADOPT, PROMOTE AND INHIBIT STATE.
	public float expectedSpread(Set<Integer> seed, int noOfMonteCarloRuns) {
		float expectedCoverage = 0.0f;
		Set<Callable<Integer>> callables = new HashSet<Callable<Integer>>();
		for (int i = 0; i < noOfMonteCarloRuns; i++)
			callables.add(new Executor(this, seed));
		List<Future<Integer>> futures;
		try {
			futures = executorService.invokeAll(callables);
			for (Future<Integer> future : futures) {
				expectedCoverage += (float) future.get();
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return expectedCoverage / noOfMonteCarloRuns;
	}

	public Set<Integer> runCELF(int k) throws IOException {
		PriorityQueue<MarginalGain> gains = new PriorityQueue<MarginalGain>(1, new GainComparator());
		Set<Integer> seed = new HashSet<Integer>();
		float totalCoverage = 0.0f;

		Iterator<Integer> itr = usersList.keySet().iterator();
		// calculate expected spread for {v} only.
		while (itr.hasNext()) {
			int nodeId = itr.next();
			System.out.println("Calculating Spread for " + nodeId);
			seed.add(nodeId);
			float marginalGain = expectedSpread(seed, noOfMonteCarloSims);
			gains.add(new MarginalGain(nodeId, marginalGain));
			seed.clear();
		}

		MarginalGain nodeGain = gains.poll();
		seed.add(nodeGain.nodeId);
		totalCoverage += nodeGain.gain;

		System.out.println("Seed Size\tExpected Spread\tNode Id");
		System.out.println("1\t" + totalCoverage + "\t" + nodeGain.nodeId);
		bw.write("Seed Size\tExpected Spread\tNode Id");
		bw.newLine();
		bw.write("1\t" + totalCoverage + "\t" + nodeGain.nodeId);

		while (seed.size() < k && gains.size() > 0) {
			MarginalGain tmpSeed = gains.poll();
			seed.add(tmpSeed.nodeId);
			float newCoverage = expectedSpread(seed, noOfMonteCarloSims);
			// if marginal gain is more than the gain of the next element
			if (gains.size() > 0 && newCoverage - totalCoverage >= gains.peek().gain) {
				totalCoverage = newCoverage;
				System.out.println(seed.size() + "\t" + totalCoverage + "\t" + tmpSeed.nodeId);
				bw.newLine();
				bw.write(seed.size() + "\t" + totalCoverage + "\t" + tmpSeed.nodeId);
			} else {
				seed.remove(tmpSeed.nodeId);
				tmpSeed.gain = newCoverage - totalCoverage;
				gains.add(tmpSeed);
			}
		}
		bw.close();
		return seed;
	}

	public void serializeMap(String fileName) {
		try {
			FileOutputStream fileOut = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(usersList);
			out.writeObject(seedSet);
			out.flush();
			out.close();
			fileOut.close();
			System.out.printf("Serialized data is saved in graph.ser");
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public void deSerializeMap(String fileName) {
		try {
			FileInputStream fileIn = new FileInputStream(fileName);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			usersList = (ConcurrentHashMap<Integer, Node>) in.readObject();
			seedSet = (Set<Integer>) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("class not found");
			c.printStackTrace();
			return;
		}
	}

}
