import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/singleMovie")
public class SingleMovieServlet extends HttpServlet {
	private static final long serialVersionUID = 2L;

	// Create a dataSource which registered in web.xml
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json"); // Response mime type
		
		// Retrieve parameter id from url request.
		String id = request.getParameter("movieId");
		
		// Output stream to STDOUT
		PrintWriter out = response.getWriter();

		try {
			// Get a connection from dataSource
			//Connection dbcon = dataSource.getConnection();
			
			Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null) {
            	//out.println("envCtx is NULL");
            	JsonObject jsonObject = new JsonObject();
    			jsonObject.addProperty("errorMessage", "envCtx is NULL.");
    			out.write(jsonObject.toString());
            }
            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");

            if (ds == null) {
            	//out.println("ds is null.");
        		JsonObject jsonObject = new JsonObject();
    			jsonObject.addProperty("errorMessage", "ds is null.");
    			out.write(jsonObject.toString());
            }
            
            Connection dbCon = ds.getConnection();
            if (dbCon == null)
            {
            	//out.println("dbcon is null.");
            	JsonObject jsonObject = new JsonObject();
     			jsonObject.addProperty("errorMessage", "dbcon is null.");
     			out.write(jsonObject.toString());
            }
			
			// Construct a query with parameter represented by "?"
			String query = "SELECT m.*, group_concat(distinct s.name), group_concat(distinct s.id), group_concat(distinct g.name) "
					+ "FROM stars as s, stars_in_movies as sim, movies as m, genres as g, genres_in_movies as gm "
					+ "where m.id = sim.movieId and sim.starId = s.id and g.id = gm.genreId and m.id = gm.movieId and m.id = ?"; 
			
			// Declare our statement
			PreparedStatement statement = dbCon.prepareStatement(query);

			// Set the parameter represented by "?" in the query to the id we get from url,
			// num 1 indicates the first "?" in the query
			statement.setString(1, id);
			
			// Perform the query
			ResultSet rs = statement.executeQuery();

			JsonArray jsonArray = new JsonArray();

			// Iterate through each row of rs
			while (rs.next()) {

            	String m_id = rs.getString("m.id");
                String m_title = rs.getString("m.title");
                String m_year = rs.getString("m.year");
                String m_director = rs.getString("m.director");
                String s_starname = rs.getString("group_concat(distinct s.name)");
                String s_id = rs.getString("group_concat(distinct s.id)");
                String g_name = rs.getString("group_concat(distinct g.name)");
                
                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("m_id", m_id);
                jsonObject.addProperty("m_title", m_title);
                jsonObject.addProperty("m_year", m_year);
                jsonObject.addProperty("m_director", m_director);
                jsonObject.addProperty("s_starname", s_starname);
                jsonObject.addProperty("s_id", s_id);
                jsonObject.addProperty("g_name", g_name);

				jsonArray.add(jsonObject);
			}
			
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

			rs.close();
			statement.close();
			dbCon.close();
		}catch (SQLException ex) {
            ex.printStackTrace();
            while (ex != null) {
                System.out.println("SQL Exception:  " + ex.getMessage());
                ex = ex.getNextException();
            } // end while
        } // end catch SQLException  
		catch (java.lang.Exception ex) {
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", ex.getMessage());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
		}
		out.close();

	}

}
