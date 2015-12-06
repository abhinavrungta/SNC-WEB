import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math3.util.CombinatoricsUtils;

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
	ConcurrentHashMap<Integer, Set<Integer>> inverseSet;
	Object[] userArray = null;
	long noOfEdges = 0;
	static float rMin = 1.0f;
	static float rMax = 5.0f;
	int noOfMonteCarloSims = 1000;
	Set<Integer> seedSet;
	ExecutorService executorService;

	public LTC() {
		int cores = 16;
		if (System.getProperty("cores") != null)
			cores = Integer.parseInt(System.getProperty("cores"));
		executorService = Executors.newFixedThreadPool(cores);
	}

	public static void main(String[] args) throws FileNotFoundException {
		LTC app = new LTC();
		app.loadGraph(args[1]);
		app.loadProbabilities(args[2]);
		app.loadValues(args[3]);
		app.loadRatings(args[4], args[5], args[6]);
		long start = System.currentTimeMillis();
		int theta = app.parameterEstimation(50);
		System.out.println("Parameter Theta " + theta);
		app.seedSet = app.NodeSelection(50, 100000);
		Iterator<Integer> itr = app.seedSet.iterator();
		while (itr.hasNext()) {
			System.out.print(itr.next() + ", ");
		}
		System.out.println();
		System.out.println(app.expectedSpread(app.seedSet, 1000));
		long mid = System.currentTimeMillis();
		System.out.println(mid - start);

		// app.seedSet = app.runCELF(Integer.parseInt(args[0]));
		// app.seedSet = app.runCELF(5);
		// itr = app.seedSet.iterator();
		// while (itr.hasNext()) {
		// System.out.print(itr.next() + ", ");
		// }
		// long end = System.currentTimeMillis();
		// System.out.println(end-mid);
		app.executorService.shutdown();
	}

	public void loadGraph(String edgeList) throws FileNotFoundException {
		File inputFile = new File(edgeList);
		usersList = new ConcurrentHashMap<Integer, Node>();
		Scanner stdin = new Scanner(inputFile);
		noOfEdges = 0;
		while (stdin.hasNext()) {
			noOfEdges++;
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
		userArray = usersList.keySet().toArray();
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

	public Set<Integer> NodeSelection(int k, int theta) {
		inverseSet = new ConcurrentHashMap<Integer, Set<Integer>>();
		Iterator<Integer> itr = usersList.keySet().iterator();
		while (itr.hasNext()) {
			inverseSet.put(itr.next(), new HashSet<Integer>());
		}
		Set<Integer> resultSet = new HashSet<Integer>();

		Set<Callable<Integer>> callables = new HashSet<Callable<Integer>>();
		for (int i = 0; i < theta; i++)
			callables.add(new RRSetExecutor(this, i));
		List<Future<Integer>> futures;
		try {
			futures = executorService.invokeAll(callables);
			for (Future<Integer> future : futures) {
				future.get();
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < k; i++) {
			Entry<Integer, Set<Integer>> max = Collections.max(inverseSet.entrySet(),
					new Comparator<Entry<Integer, Set<Integer>>>() {
						@Override
						public int compare(Entry<Integer, Set<Integer>> o1, Entry<Integer, Set<Integer>> o2) {
							return o1.getValue().size() > o2.getValue().size() ? 1 : -1;
						}
					});
			resultSet.add(max.getKey());
			Set<Integer> setsCovered = max.getValue();
			inverseSet.remove(max.getKey());
			Iterator<Integer> itr2 = inverseSet.keySet().iterator();
			while (itr2.hasNext()) {
				inverseSet.get(itr2.next()).removeAll(setsCovered);
			}
		}
		return resultSet;
	}

	public int parameterEstimation(int k) {
		// approximation is (1 - 1/e - epsilon)
		double e = Math.E;
		double epsilon = 0.1;
		// probability of the solution where n is the size of the network. ( 1 -
		// pow(n, -l))
		// n = 1000000, l = 0.5 gives 0.999 probability.
		// k = 50, epsilon = 0.2, l = 0.7
		double l = 0.5;
		int n = usersList.size();
		System.out.println("Size " + n);
		double logN = Math.log(n);

		double lambda = (8 + 2 * epsilon) * n
				* (l * logN + CombinatoricsUtils.binomialCoefficientLog(n, k) + Math.log(2)) * Math.pow(epsilon, -2);
		System.out.println("Lambda " + lambda);
		System.out.println("Approximation " + (1 - 1 / e - epsilon));
		System.out.println("Probability " + (1 - Math.pow(n, -l)));

		double c = 6 * l * logN + 6 * Math.log(logN / Math.log(2));
		System.out.println("Constant " + c);
		System.out.println("Range " + (logN / Math.log(2)));

		inverseSet = new ConcurrentHashMap<Integer, Set<Integer>>();
		Iterator<Integer> itr = usersList.keySet().iterator();
		while (itr.hasNext()) {
			inverseSet.put(itr.next(), new HashSet<Integer>());
		}
		for (double i = 1; i <= (logN / Math.log(2)) - 1; i++) {
			double c_i = c * Math.pow(2, i);
			double sum = 0;

			Set<Callable<Integer>> callables = new HashSet<Callable<Integer>>();
			for (double j = 1; j < c_i; j++) {
				callables.add(new RRSetExecutor(this, (int) j));
			}
			List<Future<Integer>> futures;
			try {
				futures = executorService.invokeAll(callables);
				for (Future<Integer> future : futures) {
					double width = (double) future.get();
					double k_r = 1 - Math.pow(1 - width / noOfEdges, k);
					sum += k_r;
				}
			} catch (InterruptedException | ExecutionException err) {
				err.printStackTrace();
			}

			System.out.println("Sum " + sum);
			if (sum > c) {
				System.out.println("KPT " + (n * sum / (2 * c_i)));
				return (int) (lambda / (n * sum / (2 * c_i)));
			}

		}
		return (int) lambda;

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

	public Set<Integer> runCELF(int k) {
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

		while (seed.size() < k && gains.size() > 0) {
			MarginalGain tmpSeed = gains.poll();
			seed.add(tmpSeed.nodeId);
			float newCoverage = expectedSpread(seed, noOfMonteCarloSims);
			// if marginal gain is more than the gain of the next element
			if (gains.size() > 0 && newCoverage - totalCoverage >= gains.peek().gain) {
				totalCoverage = newCoverage;
				System.out.println(seed.size() + "\t" + totalCoverage + "\t" + tmpSeed.nodeId);
			} else {
				seed.remove(tmpSeed.nodeId);
				tmpSeed.gain = newCoverage - totalCoverage;
				gains.add(tmpSeed);
			}
		}
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