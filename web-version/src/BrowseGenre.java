import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedb,
 * generates output as a html <table>
 */

// Declaring a WebServlet called FormServlet, which maps to url "/form"
@WebServlet(name = "BrowseGenre", urlPatterns = "/browseGenre")
public class BrowseGenre extends HttpServlet {
    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    

    // Use http GET
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");	// Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        
        try {
            // Create a new connection to database
            //Connection dbCon = dataSource.getConnection();
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
        	
            // for genre                
            String genre = request.getParameter("genre");
            
            /*
            String query = String.format("SELECT m.*, group_concat(distinct s.name), group_concat(distinct s.id), group_concat(distinct g.name), r.rating"
            		+ " FROM movies m, stars s, stars_in_movies sm, genres_in_movies gm, genres g, ratings r"
            		+ " WHERE s.id = sm.starId and m.id = sm.movieId and g.id = gm.genreId and m.id = gm.movieId and r.movieId = m.id"
            		+ " and m.id IN"
            		+ " (SELECT m.id FROM movies m, genres g, genres_in_movies gm"
            		+ " WHERE g.name = '%s' and g.id = gm.genreId and m.id = gm.movieId)"
            		+ " GROUP BY r.rating, m.id "
            		, genre);
            */
            String sql = "SELECT m.*, group_concat(distinct s.name), group_concat(distinct s.id), group_concat(distinct g.name), r.rating"
            		+ " FROM movies m, stars s, stars_in_movies sm, genres_in_movies gm, genres g, ratings r"
            		+ " WHERE s.id = sm.starId and m.id = sm.movieId and g.id = gm.genreId and m.id = gm.movieId and r.movieId = m.id"
            		+ " and m.id IN"
            		+ " (SELECT m.id FROM movies m, genres g, genres_in_movies gm"
            		+ " WHERE g.name = ? and g.id = gm.genreId and m.id = gm.movieId)"
            		+ " GROUP BY r.rating, m.id ";                      
           
            // query for sort, order, default is ORDER BY rating DESC
            String query2 = new String("");
            String sort = new String("");
            String order = new String("");
            if (request.getParameter("sort") != null) {
                sort = request.getParameter("sort");
                order = request.getParameter("order");
                if (sort.equals("rating")) {
                	query2 = String.format(" ORDER BY r.%s %s", sort, order);
                }else {
                	query2 = String.format(" ORDER BY m.%s %s", sort, order);
                }
            }else {
            	sort = "rating";
            	order = "DESC";
            	query2 = String.format(" ORDER BY r.%s %s", sort, order);
            }
            
            // query for pagination, default is first page, 10 records/page
            String page_string = new String("");
            String numOfpage = new String("");
            int pagenumber;
            int numofpage;
            if (request.getParameter("page") != null) {
            	page_string = request.getParameter("page");
            	pagenumber = Integer.parseInt(page_string);
            }else {
            	page_string = "1";
            	pagenumber = 1;
            }
            if (request.getParameter("numOfpage") != null) {
            	numOfpage = request.getParameter("numOfpage");
            	numofpage = Integer.parseInt(numOfpage);
            }else {
            	numOfpage = "10";
            	numofpage = 10;
            }
            int pageVolum = (pagenumber-1)*numofpage;            
            String query3 = String.format(" LIMIT %s OFFSET %s ", String.valueOf(numofpage+5), String.valueOf(pageVolum));
            
            sql = sql + query2 + query3;
            
            // establish preparedStatement
            PreparedStatement preparedStatement = dbCon.prepareStatement(sql);
    		preparedStatement.setString(1, genre);
                              	
    		System.out.println(genre);
            System.out.println(preparedStatement);
    		
            // Perform the query
            //ResultSet rs = statement.executeQuery(query);
    		ResultSet rs = preparedStatement.executeQuery(); 
    		
            JsonArray jsonArray = new JsonArray();
            
            // Iterate through each row of rs and store the data to jsonArray
            int count_result = 0;
            while (rs.next() && (count_result < numofpage)) {
            	String m_id = rs.getString("m.id");
                String m_title = rs.getString("m.title");
                String m_year = rs.getString("m.year");
                String m_director = rs.getString("m.director");
                String s_starname = rs.getString("group_concat(distinct s.name)");
                String s_id = rs.getString("group_concat(distinct s.id)");
                String g_name = rs.getString("group_concat(distinct g.name)");
                String r_rating = rs.getString("r.rating");
                
                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("m_id", m_id);
                jsonObject.addProperty("m_title", m_title);
                jsonObject.addProperty("m_year", m_year);
                jsonObject.addProperty("m_director", m_director);
                jsonObject.addProperty("s_starname", s_starname);
                jsonObject.addProperty("s_id", s_id);
                jsonObject.addProperty("g_name", g_name);
                jsonObject.addProperty("r_rating", r_rating);
                
                jsonArray.add(jsonObject);
                
                count_result += 1;
            }
            System.out.println(jsonArray);
            /*
            if (pagenumber-2 >= 0) {
            	out.println("<p>prev: <a href = \"./browse?page="+String.valueOf(pagenumber-2)+"&numOfpage="+numOfpage+"&sort="+sort+"&order="+order+"&genre="+genre+"\">"+String.valueOf(pagenumber-1)+"</a></p>");          	
            }        
            out.println("<p>now: "+ String.valueOf(pagenumber) +"</p>");
            if (rs.next()) {
            	out.println("<p>next: <a href = \"./browse?page="+String.valueOf(pagenumber)+"&numOfpage="+numOfpage+"&sort="+sort+"&order="+order+"&genre="+genre+"\">"+String.valueOf(pagenumber+1)+"</a></p>");
            }
            */

            // write JSON string to output
            out.write(jsonArray.toString());            
            // set response status to 200 (OK)
            response.setStatus(200);
            
            System.out.println("preparedStatement Success");

            // Close all structures
            rs.close();
            preparedStatement.close();
            dbCon.close();

        } catch (SQLException ex) {
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