
import java.io.IOException;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.*;

// server endpoint URL
@WebServlet("/auto-suggestion")
public class AutoSuggestion extends HttpServlet {
	private DataSource dataSource;
	private static final long serialVersionUID = 1L;
	
    /*
     * 
     * Match the query against movies and return a JSON response.
     * 
     * For example, if the query is "super":
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "category": "movie", "movieId": "tt000000", "director": "Neil Cohen" } },
     * 	{ "value": "Supergirl", "data": { "category": "movie", "movieId": "tt000001", "director": "Raoul Ruiz" } }
     * ]
     * 
     * The format is like this because it can be directly used by the 
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *   
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     * 
     * 
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*String loginUser = "root";
        String loginPasswd = "database84";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb?useSSL=true";
        */
		
		try {
			// setup the response json arrray
			JsonArray jsonArray = new JsonArray();
			
			// get the query string from parameter
			String query = request.getParameter("query");
			
			// return the empty json array if query is null or empty
			if (query == null || query.trim().isEmpty()) {
				response.getWriter().write(jsonArray.toString());
				return;
			}	
			
			// search on marvel heros and DC heros and add the results to JSON Array
			// this example only does a substring match
			// TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars
			
			// connect to DB
        	//Class.forName("com.mysql.jdbc.Driver").newInstance();
    		//Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
    		
			Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                System.out.println("envCtx is NULL");
            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");

            // the following commented lines are direct connections without pooling
            //Class.forName("org.gjt.mm.mysql.Driver");
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //Connection dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

            if (ds == null) {
            	System.out.println("ds is null.");
            }
            
            Connection dbCon = ds.getConnection();
            if (dbCon == null)
            {
            	System.out.println("dbcon is null.");
            }
			
    		
    		String sql = "SELECT * from movies "
    				+ "WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE)"
    				+ " OR ed(title, ?) <= ? "
    				+ " LIMIT 10";
    		
    		PreparedStatement preparedStatement = dbCon.prepareStatement(sql);
    		String [] queries_list = query.split(" ");
    		String queries = "";
    		for (int i=0; i<queries_list.length; i++) {
    			String tmp = "+"+queries_list[i]+"* ";
    			queries += tmp;
    		}
    		
    		preparedStatement.setString(1, queries);
    		preparedStatement.setString(2, query);
    		int len = Math.floorDiv(query.length(), 3);
    		preparedStatement.setInt(3, len);
    		ResultSet rs = preparedStatement.executeQuery(); 
    		
			while(rs.next()) {
				String movieId = rs.getString("id");
				String title = rs.getString("title");
				String director = rs.getString("director");
				
				jsonArray.add(generateJsonObject(movieId, title, director));
			}
			
			response.getWriter().write(jsonArray.toString());
			return;
		}catch (SQLException ex) {
            ex.printStackTrace();
            while (ex != null) {
                System.out.println("SQL Exception:  " + ex.getMessage());
                ex = ex.getNextException();
            } // end while
        } // end catch SQLException  
		catch (java.lang.Exception ex) {
			System.out.println(ex);
			response.sendError(500, ex.getMessage());
		}
	}
	
	/*
	 * Generate the JSON Object from hero and category to be like this format:
	 * {
	 *   "value": "Iron Man",
	 *   "data": { "category": "movie", "movieId": "tt000000", "director": "Neil Cohen" }
	 * }
	 * 
	 */
	private static JsonObject generateJsonObject(String movieId, String movieTitle, String director) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("value", movieTitle);
		
		JsonObject additionalDataJsonObject = new JsonObject();
		additionalDataJsonObject.addProperty("category", "Movie");
		additionalDataJsonObject.addProperty("movieId", movieId);
		additionalDataJsonObject.addProperty("director", director);
		
		jsonObject.add("data", additionalDataJsonObject);
		return jsonObject;
	}


}
