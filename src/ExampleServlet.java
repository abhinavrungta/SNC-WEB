import java.util.ArrayList;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;

@WebServlet("/simpleexample")
public class ExampleServlet extends DataSourceServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DataTable generateDataTable(Query query, HttpServletRequest request) {
		// Create a data table.
		DataTable data = new DataTable();
		ArrayList<ColumnDescription> cd = new ArrayList<ColumnDescription>();
		cd.add(new ColumnDescription("name", ValueType.TEXT, "Animal name"));
		cd.add(new ColumnDescription("link", ValueType.TEXT, "Link to wikipedia"));
		cd.add(new ColumnDescription("population", ValueType.NUMBER, "Population size"));
		cd.add(new ColumnDescription("vegeterian", ValueType.BOOLEAN, "Vegetarian?"));

		data.addColumns(cd);

		// Fill the data table.
		try {
			data.addRowFromValues("Aye-aye", "http://en.wikipedia.org/wiki/Aye-aye", 100, true);
			data.addRowFromValues("Sloth", "http://en.wikipedia.org/wiki/Sloth", 300, true);
			data.addRowFromValues("Leopard", "http://en.wikipedia.org/wiki/Leopard", 50, false);
			data.addRowFromValues("Tiger", "http://en.wikipedia.org/wiki/Tiger", 80, false);
		} catch (TypeMismatchException e) {
			System.out.println("Invalid type!");
		}
		System.out.println(data);
		return data;
	}
}
