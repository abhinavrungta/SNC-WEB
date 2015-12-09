import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.math3.util.CombinatoricsUtils;

public class RIS extends LTC {
	ConcurrentHashMap<Integer, Set<Integer>> inverseSet;
	Object[] userArray = null;

	public RIS() {
		super();
	}

	public static void main(String[] args) throws IOException {
		RIS app = new RIS();
		app.fout = new File("/tmp/ris.txt");
		app.fos = new FileOutputStream(app.fout);
		app.bw = new BufferedWriter(new OutputStreamWriter(app.fos));
		long start = System.currentTimeMillis();
		app.loadGraph(args[1]);
		app.userArray = app.usersList.keySet().toArray();
		app.loadProbabilities(args[2]);
		app.loadRatings(args[3], args[4], args[5]);
		app.productId = app.movieRatings.keySet().iterator().next();
		int theta = app.parameterEstimation(Integer.parseInt(args[0]));
		System.out.println("Parameter Theta " + theta);
		if (theta > 1000000)
			theta = 1000000;
		app.seedSet = app.NodeSelection(Integer.parseInt(args[0]), theta);
		System.out.println("Seed Set Node Ids");
		Iterator<Integer> itr = app.seedSet.iterator();
		while (itr.hasNext()) {
			System.out.print(itr.next() + ", ");
		}
		System.out.println();
		long mid = System.currentTimeMillis();
		System.out.print("Time Taken : ");
		System.out.println((mid - start) / 1000.0);
		System.out.println("Calculating Expected Spread Using Monte Carlo");
		app.totalCoverage = app.expectedSpread(app.seedSet, app.noOfMonteCarloSims);
		app.bw.write("Seed Size\tExpected Spread\tNode Id");
		app.bw.newLine();
		app.bw.write(args[0] + "\t" + app.totalCoverage + "\t" + app.seedSet.iterator().next());
		app.serializeMap("/tmp/graphviz.ser");
		app.executorService.shutdown();
	}

	public Set<Integer> NodeSelection(int k, int theta) {
		inverseSet = new ConcurrentHashMap<Integer, Set<Integer>>();
		Iterator<Integer> itr = usersList.keySet().iterator();
		while (itr.hasNext()) {
			inverseSet.put(itr.next(), new HashSet<Integer>());
		}
		Set<Integer> resultSet = new HashSet<Integer>();

		Set<Callable<Integer>> callables = new HashSet<Callable<Integer>>();
		for (int i = 0; i < theta; i++) {
			callables.add(new RRSetExecutor(this, i));
		}
		List<Future<Integer>> futures = new ArrayList<Future<Integer>>(callables.size());
		try {
			// futures = executorService.invokeAll(callables);
			for (Callable<Integer> task : callables)
				futures.add(executorService.submit(task));

			System.out.println("start");
			for (Future<Integer> future : futures) {
				try {
					future.get(10, TimeUnit.SECONDS);
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		// try {
		// System.out.println("Serializing...");
		// FileOutputStream fileOut = new FileOutputStream("inverset.ser");
		// ObjectOutputStream out = new ObjectOutputStream(fileOut);
		// out.writeObject(inverseSet);
		// out.flush();
		// out.close();
		// fileOut.close();
		// System.out.println("Serialized data");
		// } catch (IOException i) {
		// i.printStackTrace();
		// }

		for (int i = 0; i < k; i++) {
			System.out.println("Sorted" + i);
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
		double epsilon = 0.01;
		// probability of the solution where n is the size of the network. ( 1 -
		// pow(n, -l))
		// n = 1000000, l = 0.5 gives 0.999 probability.
		// k = 50, epsilon = 0.2, l = 1
		double l = 1;
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
}