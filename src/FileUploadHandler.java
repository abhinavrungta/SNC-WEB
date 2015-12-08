import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Servlet implementation class FileUploadHandler
 */
@WebServlet("/FileUploadHandler")
public class FileUploadHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final String UPLOAD_DIRECTORY = "/tmp";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FileUploadHandler() {
		super();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// process only if its multipart content
		if (ServletFileUpload.isMultipartContent(request)) {
			try {
				List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
				String fileName = "";
				List<String> models = new ArrayList<String>();
				for (FileItem item : multiparts) {
					if (!item.isFormField()) {
						String name = new File(item.getName()).getName();
						fileName = UPLOAD_DIRECTORY + File.separator + "input.txt";
						item.write(new File(fileName));
					} else {
						if ("on".equals(item.getString()))
							models.add(item.getFieldName());
					}
				}
				String modelString = String.join(",", models);
				int size = 30;
				// System.out.println(modelString);
				// get these from user input.
				String[] arg = new String[4];
				// arg[0] = "/tmp/yahoo.txt";
				arg[0] = fileName;
				arg[1] = modelString;
				arg[2] = Integer.toString(size);
				arg[3] = "1";
				// Main.main(arg);

				// File uploaded successfully
				request.setAttribute("models", modelString);
				request.setAttribute("seedSet", size);
				request.setAttribute("message", "File Uploaded Successfully");

				// ChartServlet chartServlet = new ChartServlet();
				// chartServlet.doGet(request, response);
			} catch (Exception ex) {
				request.setAttribute("message", "File Upload Failed due to " + ex);
			}

		} else {
			request.setAttribute("message", "Sorry this Servlet only handles file upload request");
		}
		System.out.println("Here");
		request.getRequestDispatcher("/DataSetDisplay.jsp").forward(request, response);

	}

}
