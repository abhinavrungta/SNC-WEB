import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;

@WebServlet("/ratings")
public class RatingServlet extends DataSourceServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public DataTable generateDataTable(Query arg0, HttpServletRequest arg1)
			throws DataSourceException {
		Map<Integer, Double> users = getUserRatings("/tmp/input.txt");
		double avgValue = getAvgRating(users, new ArrayList<Integer>(users.keySet()));
		
		DataTable dataTable = new DataTable();
		List<ColumnDescription> cd = new ArrayList<ColumnDescription>();
		cd.add(new ColumnDescription("model", ValueType.TEXT, "model"));
		cd.add(new ColumnDescription("average_ratings", ValueType.NUMBER, "average_ratings"));
		dataTable.addColumns(cd);
		List<TableRow> rows = new ArrayList<TableRow>();
		List<Integer> color = getList("/tmp/output_ltc.txt");
		List<Integer> classic = getList("/tmp/output_ltclassic.txt");
		List<Integer> ratings = getList("/tmp/output_ltratings.txt");
		List<Integer> tattle = getList("/tmp/output_lttattle.txt");
		
		rows.add(addRow("All Users", avgValue));
		if(color!=null) rows.add(addRow("LT-Color{Seed Set}", getAvgRating(users, color)));
		if(classic!=null) rows.add(addRow("LT-Classic{Seed Set}", getAvgRating(users, classic)));
		if(ratings!=null) rows.add(addRow("LT-Ratings{Seed Set}", getAvgRating(users, ratings)));
		if(tattle!=null) rows.add(addRow("LT-Tattle{Seed Set}", getAvgRating(users, tattle)));
		
		dataTable.addRows(rows);
		return dataTable;
	}
	
	private List<Integer> getList(String fileName) {
		try {
				File file = new File(fileName);
				if(!file.exists())
					return null;
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line = bufferedReader.readLine();
				List<Integer> list = new ArrayList<Integer>();
				while((line=bufferedReader.readLine())!=null) {
					String[] attributes = line.split("\t");
					list.add(Integer.parseInt(attributes[2]));
				}
				bufferedReader.close();
				fileReader.close();
				return list;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
	}
	
	private TableRow addRow(String model, double value) {
		TableRow row = new TableRow();
		row.addCell(model);
		row.addCell(value);
		return row;
	}
	
	private Map<Integer, Double> getUserRatings(String fileName) {
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = bufferedReader.readLine();
			String[] attributes = line.split(",");
			String movieId = attributes[1];
			map.put(Integer.parseInt(attributes[0]), Double.parseDouble(attributes[2]));
			while((line=bufferedReader.readLine())!=null) {
				attributes = line.split(",");
				if(movieId.equals(attributes[1]))
					map.put(Integer.parseInt(attributes[0]), Double.parseDouble(attributes[2]));
			}
			bufferedReader.close();
			fileReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	
	private double getAvgRating(Map<Integer, Double> map, List<Integer> list) {
		double avg;
		int count = 0;
		double sum = 0;
		Set<Integer> keys = map.keySet();
		for(Integer key : list) {
			if(keys.contains(key)) {
				sum += map.get(key);
				count++;
			}	
			else {
				//TODO
			}
		}
		avg = (count==0)?0:(sum / count);
		return avg;
	}
	
}
