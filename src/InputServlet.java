import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/input")
public class InputServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		List<String> models = new ArrayList<String>();
		if ("on".equals(request.getParameter("color")))
			models.add("color");
		if ("on".equals(request.getParameter("classic")))
			models.add("classic");
		if ("on".equals(request.getParameter("rating")))
			models.add("ratings");
		if ("on".equals(request.getParameter("tattle")))
			models.add("tattle");
		String modelString = "";
		for (String str : models) {
			modelString = modelString.concat(str + ",");
		}
		System.out.println(modelString);
		modelString = modelString.substring(0, modelString.length() - 1);
		String[] arg = new String[4];
		arg[0] = "/tmp/input.txt";
		arg[1] = modelString;
		arg[2] = request.getParameter("seed_set");
		arg[3] = request.getParameter("monte_carlo");
		Main.main(arg);

		request.setAttribute("models", modelString);
		request.getRequestDispatcher("/chartDisplay.jsp").forward(request, response);
	}

}
