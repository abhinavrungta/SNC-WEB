import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;

@WebServlet("/accuracy")
public class AccuracyServlet extends DataSourceServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public DataTable generateDataTable(Query arg0, HttpServletRequest arg1) throws DataSourceException {
		DataTable dataTable = new DataTable();
		List<ColumnDescription> cd = new ArrayList<ColumnDescription>();
		List<TableRow> rows = new ArrayList<TableRow>();
		cd.add(new ColumnDescription("model", ValueType.TEXT, "model"));
		cd.add(new ColumnDescription("adapters", ValueType.NUMBER, "Coverage"));
		dataTable.addColumns(cd);
		int color = getCount("/tmp/output_ltc.txt");
		int classic = getCount("/tmp/output_ltclassic.txt");
		int ratings = getCount("/tmp/output_ltratings.txt");
		int tattle = getCount("/tmp/output_lttattle.txt");
		rows.add(addRow("Ground Truth", getActualCount("/tmp/input.txt")));
		if (color != -1) {
			cd.add(new ColumnDescription("color", ValueType.NUMBER, "LT-Color"));
			rows.add(addRow("LT-Color", color));
		}
		if (classic != -1) {
			cd.add(new ColumnDescription("classic", ValueType.NUMBER, "LT-Classic"));
			rows.add(addRow("LT-Classic", classic));
		}
		if (ratings != -1) {
			cd.add(new ColumnDescription("ratings", ValueType.NUMBER, "LT-Ratings"));
			rows.add(addRow("LT-Ratings", ratings));
		}
		if (tattle != -1) {
			cd.add(new ColumnDescription("tattle", ValueType.NUMBER, "LT-Tattle"));
			rows.add(addRow("LT-Tattle", tattle));
		}
		dataTable.addRows(rows);
		return dataTable;
	}

	private TableRow addRow(String model, int value) {
		TableRow row = new TableRow();
		row.addCell(model);
		row.addCell(value);
		return row;
	}

	private int getCount(String fileName) {
		File file = new File(fileName);
		if(!file.exists())
			return -1;
		try {
			int count = 0;
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = bufferedReader.readLine();
			while ((line = bufferedReader.readLine()) != null) {
				count = (int) (Double.parseDouble(line.split("\t")[1]));
			}
			bufferedReader.close();
			fileReader.close();
			return count;
		} catch (IOException ex) {
			return -1;
		}
	}

	private int getActualCount(String fileName) {
		int count = 1;
		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = bufferedReader.readLine();
			String movieId = line.split(",")[1];
			while ((line = bufferedReader.readLine()) != null) {
				String[] attributes = line.split(",");
				if (attributes[1].equals(movieId))
					count++;
			}
			bufferedReader.close();
			fileReader.close();
		} catch (IOException ex) {
			return -1;
		}
		return count;
	}

}
