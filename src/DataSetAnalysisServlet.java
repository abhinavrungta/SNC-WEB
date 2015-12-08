import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;

@WebServlet("/datasetanalysis")
public class DataSetAnalysisServlet extends DataSourceServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public DataTable generateDataTable(Query query, HttpServletRequest request) throws DataSourceException {
		// TODO Auto-generated method stub
		Map<Double, Integer> countMap = new HashMap<Double, Integer>();
		Map<Double, Integer> userMap = new HashMap<Double, Integer>();
		readFromFile(countMap, userMap);
		DataTable dataTable = new DataTable();
		List<ColumnDescription> cd = new ArrayList<ColumnDescription>();
		cd.add(new ColumnDescription("rating", ValueType.NUMBER, "Rating"));
		cd.add(new ColumnDescription("count", ValueType.NUMBER, "No. of Ratings"));
		cd.add(new ColumnDescription("user", ValueType.NUMBER, "No. of Users"));
		dataTable.addColumns(cd);
		Set<Double> keys = new TreeSet(countMap.keySet());
		for (double key : keys) {
			TableRow row = new TableRow();
			row.addCell(key);
			row.addCell(countMap.get(key));
			row.addCell(userMap.get(key));
			dataTable.addRow(row);
		}
		return dataTable;
	}

	private void readFromFile(Map<Double, Integer> countMap, Map<Double, Integer> userMap) {
		FileReader fileReader;
		try {
			fileReader = new FileReader("/tmp/input.txt");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = bufferedReader.readLine();
			Map<Double, Set<Integer>> users = new HashMap<Double, Set<Integer>>();

			while ((line = bufferedReader.readLine()) != null) {
				String[] attributes = line.split(",");
				addElementToMap(countMap, Double.parseDouble(attributes[2]));
				addElementToMap(users, Double.parseDouble(attributes[2]), Integer.parseInt(attributes[0]));
			}

			Set<Double> keys = users.keySet();
			for (double key : keys)
				userMap.put(key, users.get(key).size());

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void addElementToMap(Map<Double, Set<Integer>> map, double key, int val) {
		Set<Integer> users = map.get(key);
		if (users == null)
			users = new HashSet<Integer>();
		users.add(val);
		map.put(key, users);
	}

	private void addElementToMap(Map<Double, Integer> map, double key) {
		int val = 0;
		if (map.get(key) != null)
			val = map.get(key);
		map.put(key, val + 1);
	}

}
