import java.util.*;
import java.io.*;
import java.lang.Math.*;

public class TestEdgeWeights {

	public static void main(String[] args) throws IOException {

		String inputFileName = args[0];
		String outputFileName = args[1];
		String line = null;

		Map<Integer, Double> nodes = new HashMap<Integer, Double>();

		FileReader fileReader = new FileReader(inputFileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		FileWriter fileWriter = new FileWriter(outputFileName);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		while ((line = bufferedReader.readLine()) != null) {
			String[] attributes = line.split("\t");
			if (attributes.length != 3)
				break;
			// System.out.println(line);
			int nodeVal = Integer.parseInt(attributes[1]);
			double weightVal = Double.parseDouble(attributes[2]);

			double existingVal = 0.0;
			if (nodes.get(nodeVal) != null)
				existingVal = nodes.get(nodeVal);

			existingVal += weightVal;
			nodes.put(nodeVal, existingVal);
		}

		Iterator iterator = nodes.entrySet().iterator();
		int count1 = 0;
		int count2 = 0;
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			if (Math.abs((Double) pair.getValue() - 1.0) < 0.00001)
				count1++;
			else {
				bufferedWriter.write(pair.getKey() + "\t" + pair.getValue() + "\n");
				count2++;
			}

		}
		System.out.println("count 1: " + count1 + " and count2: " + count2);
		bufferedReader.close();
		bufferedWriter.close();
	}
}