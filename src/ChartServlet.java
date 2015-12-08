import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.ValueType;

public class ChartServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// public ChartServlet() {
	// super();
	// }

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("Coming here");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		System.out.println("here?");
		DataTable dataTable = new DataTable();
		String[] models = ((String) request.getAttribute("models")).split(",");
		int size = (Integer) request.getAttribute("seedSet");
		Map<Integer, Double> colorMap = null;
		Map<Integer, Double> classicalMap = null;
		Map<Integer, Double> ratingsMap = null;
		Map<Integer, Double> tattleMap = null;
		List<ColumnDescription> cd = new ArrayList<ColumnDescription>();
		cd.add(new ColumnDescription("seed_set_size", ValueType.NUMBER, "Seed Set Size"));
		for (String model : models) {
			System.out.println(model);
			switch (model) {
			case "color":
				colorMap = readFromFile("/tmp/output_ltc.txt");
				cd.add(new ColumnDescription("color", ValueType.NUMBER, "LT-Color"));
				break;
			case "classical":
				classicalMap = readFromFile("/tmp/output_ltclassic.txt");
				cd.add(new ColumnDescription("classic", ValueType.NUMBER, "LT-Classic"));
				break;
			case "ratings":
				ratingsMap = readFromFile("/tmp/output_ltratings.txt");
				cd.add(new ColumnDescription("ratings", ValueType.NUMBER, "LT-Ratings"));
				break;
			case "tattle":
				tattleMap = readFromFile("/tmp/output_lttattle.txt");
				cd.add(new ColumnDescription("tattle", ValueType.NUMBER, "LT-Tattle"));
			}
		}
		dataTable.addColumns(cd);
		for (int i = 1; i <= size; i++) {
			TableRow row = new TableRow();
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
		System.out.println(dataTable.getColumnDescriptions().size());
		request.setAttribute("chartData", dataTable);
		RequestDispatcher requestDispatcher = request.getRequestDispatcher("/chartDisplay.jsp");
		requestDispatcher.forward(request, response);
	}

	private Map<Integer, Double> readFromFile(String fileName) throws IOException {
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = bufferedReader.readLine();
		while ((line = bufferedReader.readLine()) != null) {
			String[] attributes = line.split("\t");
			map.put(Integer.parseInt(attributes[0]), Double.parseDouble(attributes[1]));
		}
		bufferedReader.close();
		fileReader.close();
		return map;
	}
}
