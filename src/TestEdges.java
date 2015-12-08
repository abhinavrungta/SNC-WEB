import java.util.*;
import java.io.*;

public class TestEdges {

	public static void main(String[] args) throws IOException {
		String input = args[0];
		// String output = args[1];
		String line = null;
		// Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		Set<Integer> set = new HashSet<Integer>();

		FileReader fileReader = new FileReader(input);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		while ((line = bufferedReader.readLine()) != null) {
			String[] attributes = line.split("\t");
			int u = Integer.parseInt(attributes[0]);
			set.add(u);
		}

		System.out.println("number of nodes in the network " + set.size());
		bufferedReader.close();
	}

}