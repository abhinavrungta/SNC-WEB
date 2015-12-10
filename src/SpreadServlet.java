import java.io.BufferedReader;
import java.io.File;
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
	public DataTable generateDataTable(Query query, HttpServletRequest request)
			throws DataSourceException {
		DataTable dataTable = new DataTable();
		Map<Integer, Double> colorMap = readFromFile("/tmp/output_ltc.txt");
		Map<Integer, Double> classicalMap = readFromFile("/tmp/output_ltclassic.txt");
		Map<Integer, Double> ratingsMap = readFromFile("/tmp/output_ltratings.txt");
		Map<Integer, Double> tattleMap = readFromFile("/tmp/output_lttattle.txt");
		List<ColumnDescription> cd = new ArrayList<ColumnDescription>();
		cd.add(new ColumnDescription("seed_set_size", ValueType.NUMBER, "Seed Set Size"));
		if(colorMap!=null) cd.add(new ColumnDescription("color", ValueType.NUMBER, "LT-Color"));
		if(classicalMap!=null) cd.add(new ColumnDescription("classic", ValueType.NUMBER, "LT-Classical"));
		if(ratingsMap!=null) cd.add(new ColumnDescription("ratings", ValueType.NUMBER, "LT-Ratings"));
		if(tattleMap!=null) cd.add(new ColumnDescription("tattle", ValueType.NUMBER, "LT-Tattle"));
		dataTable.addColumns(cd);
		int size = 0;
		if(colorMap!=null)size=colorMap.size();
		else if(classicalMap!=null)size = classicalMap.size();
		else if(ratingsMap != null)size = ratingsMap.size();
		else size=tattleMap.size();
		List<TableRow> rows = new ArrayList<TableRow>();
		for(int i=1; i<=size; i++) {
			TableRow row = new TableRow();
			row.addCell(i);
			addValue(row, colorMap, i);
			addValue(row, classicalMap, i);
			addValue(row, ratingsMap, i);
			addValue(row, tattleMap, i);
			rows.add(row);
		}
		dataTable.addRows(rows);
		return dataTable;
	}
	
	private void addValue(TableRow row, Map<Integer, Double> map, int key) {
		if(map!=null)
			row.addCell(map.get(key));
	}

	private Map<Integer, Double> readFromFile(String fileName){
		File file = new File(fileName);
		if(!file.exists())
			return null;
		 Map<Integer, Double> map = new HashMap<Integer, Double>();
		 try {
			 FileReader fileReader = new FileReader(file);
			 BufferedReader bufferedReader = new BufferedReader(fileReader);
			 String line = bufferedReader.readLine();
			 while((line=bufferedReader.readLine())!=null) {
				 String[] attributes = line.split("\t");
				 map.put(Integer.parseInt(attributes[0]), Double.parseDouble(attributes[1]));
			 }
			 bufferedReader.close();
			 fileReader.close();
		 }
		 catch(IOException ex) {
			 return null;
		 }
		 return map;
	 }
}
