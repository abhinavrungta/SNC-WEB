import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;


@WebServlet("/timestamps")
public class TimestampServlet extends DataSourceServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public DataTable generateDataTable(Query arg0, HttpServletRequest arg1)
			throws DataSourceException {
		Map<Integer, Long> users = getTimeStamps("/tmp/input.txt");
		List<Integer> color = getSeeds("/tmp/output_ltc.txt");
		List<Integer> classic = getSeeds("/tmp/output_ltclassic.txt");
		List<Integer> ratings = getSeeds("/tmp/output_ltratings.txt");
		List<Integer> tattle = getSeeds("/tmp/output_lttattle.txt");
		DataTable dataTable = new DataTable();
		List<ColumnDescription> cd = new ArrayList<ColumnDescription>();
		
		cd.add(new ColumnDescription("all", ValueType.NUMBER, "all"));
		cd.add(new ColumnDescription("Rest", ValueType.NUMBER, "Rest"));
		if(color!=null)cd.add(new ColumnDescription("LT-Color{Seed}", ValueType.NUMBER, "LT-Color{Seed}"));
		if(classic!=null)cd.add(new ColumnDescription("LT-Classic{Seed}", ValueType.NUMBER, "LT-Classic{Seed}"));
		if(ratings!=null)cd.add(new ColumnDescription("LT-Ratings{Seed}", ValueType.NUMBER, "LT-Ratings{Seed}"));
		if(tattle!=null)cd.add(new ColumnDescription("LT-Tattle{Seed}", ValueType.NUMBER, "LT-Tattle{Seed}"));
		dataTable.addColumns(cd);
		
		List<TableRow> rows = new ArrayList<TableRow>();
		Set<Integer> nodes = users.keySet();
		for(int node: nodes) {
			TableRow row = new TableRow();
			long value = users.get(node);
			row.addCell(value);
			row.addCell(node);
			if(color!=null)addValue(row, color, node, value);
			if(classic!=null)addValue(row, classic, node, value);
			if(ratings!=null)addValue(row, ratings, node, value);
			if(tattle!=null)addValue(row, tattle, node, value);
			
			rows.add(row);
		}
		dataTable.addRows(rows);
		return dataTable;
	}
	
	private void addValue(TableRow row, List<Integer> list, int key, long value) {
		if(list.contains(key))
			row.addCell(key);
		else
			row.addCell(Value.getNullValueFromValueType(ValueType.NUMBER));
	}
	
	private List<Integer> getSeeds(String fileName) {
		File file = new File(fileName);
		if(!file.exists())
			return null;
		FileReader fileReader;
		try {
			fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			List<Integer> list = new ArrayList<Integer>();
			String line = bufferedReader.readLine();
			while((line=bufferedReader.readLine())!=null) {
				String[] attributes = line.split("\t");
				list.add(Integer.parseInt(attributes[2]));
			}
			bufferedReader.close();
			fileReader.close();
			return list;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Map<Integer, Long> getTimeStamps(String fileName) {
		FileReader fileReader;
		try {
			fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			Map<Integer, Long> map = new HashMap<Integer, Long>();
			String line = bufferedReader.readLine();
			String[] attributes = line.split(",");
			String movieId = attributes[1];
			map.put(Integer.parseInt(attributes[0]), Long.parseLong(attributes[3]));
			while((line=bufferedReader.readLine())!=null) {
				attributes = line.split(",");
				if(attributes[1].equals(movieId))
					map.put(Integer.parseInt(attributes[0]), Long.parseLong(attributes[3]));
			}
			bufferedReader.close();
			fileReader.close();
			return map;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
