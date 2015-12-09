import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;

@WebServlet("/spreadservlet")
public class SpreadServlet extends DataSourceServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public DataTable generateDataTable(Query query, HttpServletRequest request) throws DataSourceException {
		// TODO Auto-generated method stub
		DataTable dataTable = new DataTable();
		// String[] models =
		// ((String)request.getAttribute("models")).split(",");
		// System.out.println(models[0]);
		// int size = (Integer)request.getAttribute("seedSet");
		Map<Integer, Double> colorMap = readFromFile("/tmp/output_ltc.txt");
		Map<Integer, Double> classicalMap = readFromFile("/tmp/output_ltclassic.txt");
		Map<Integer, Double> ratingsMap = readFromFile("/tmp/output_ltratings.txt");
		Map<Integer, Double> tattleMap = readFromFile("/tmp/output_lttattle.txt");
		int size = 0;
		List<ColumnDescription> cd = new ArrayList<ColumnDescription>();
		cd.add(new ColumnDescription("seed_set_size", ValueType.NUMBER, "Seed Set Size"));
		if (colorMap != null) {
			cd.add(new ColumnDescription("color", ValueType.NUMBER, "LT-Color"));
			size = colorMap.size();
		}
		if (classicalMap != null) {
			cd.add(new ColumnDescription("classic", ValueType.NUMBER, "LT-Classical"));
			size = classicalMap.size();
		}
		if (ratingsMap != null) {
			cd.add(new ColumnDescription("ratings", ValueType.NUMBER, "LT-Ratings"));
			size = ratingsMap.size();
		}
		if (tattleMap != null) {
			cd.add(new ColumnDescription("tattle", ValueType.NUMBER, "LT-Tattle"));
			size = tattleMap.size();
		}
		dataTable.addColumns(cd);

		System.out.println("Size is " + size);
		for (int i = 1; i <= size; i++) {
			TableRow row = new TableRow();
			row.addCell(i);
			// System.out.println("----"+colorMap.get(i));
			if (colorMap != null)
				row.addCell(colorMap.get(i));
			if (classicalMap != null)
				row.addCell(classicalMap.get(i));
			if (ratingsMap != null)
				row.addCell(ratingsMap.get(i));
			if (tattleMap != null)
				row.addCell(tattleMap.get(i));
			try {
				dataTable.addRow(row);
			} catch (TypeMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Done");
		return dataTable;
	}

	private Map<Integer, Double> readFromFile(String fileName) {
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = bufferedReader.readLine();
			while ((line = bufferedReader.readLine()) != null) {
				String[] attributes = line.split("\t");
				map.put(Integer.parseInt(attributes[0]), Double.parseDouble(attributes[1]));
			}
			bufferedReader.close();
			fileReader.close();
		} catch (IOException ex) {
			return null;
		}
		return map;
	}
}
