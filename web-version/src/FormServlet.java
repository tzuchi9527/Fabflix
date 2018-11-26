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
import java.io.File;
import java.io.FileWriter;
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
@WebServlet(name = "FormServlet", urlPatterns = "/search")
public class FormServlet extends HttpServlet {
    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    
    public String getServletInfo() {
        return "Servlet connects to MySQL database and displays result of a SELECT";
    }
    
    // Use http GET
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");	// Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        
        // create file name "test" in root 
        // WRITING TO A FILE FROM A SERVLET IN A JAVA EE WEB APPLICATION
        String contextPath = getServletContext().getRealPath("/");
        String xmlFilePath = contextPath+"test";
        System.out.println("xmlFilePath: "+ xmlFilePath);
        File myfile = new File(xmlFilePath);
        myfile.createNewFile();
        boolean firstrecord = true;

        try {
        	// start time of a query
        	long startTime = System.nanoTime();
        	
            // Create a new connection to database
            //Connection dbCon = dataSource.getConnection();
        	
            // the following few lines are for connection pooling
            // Obtain our environment naming context
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

            // the following commented lines are direct connections without pooling
            //Class.forName("org.gjt.mm.mysql.Driver");
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //Connection dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

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
    		// start time of JDBC
    		long startTimeJDBC = System.nanoTime();         	
        	
            // Retrieve parameter from the http request, which refers to the value in search.html                     
            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String starname = request.getParameter("starname");
          
            // query for sort and order by, default is sort by rating DESC
            String query2 = new String("");
                
            String sort = request.getParameter("sort");
            String order = request.getParameter("order");
            if (sort.equals("rating")) {
                query2 = String.format(" ORDER BY r.%s %s", sort, order);
            }else {
                query2 = String.format(" ORDER BY m.%s %s", sort, order);
            }
            
            // for pagination and the numOfpage, default is 10 results per page        
            String page_string = request.getParameter("page");
            int pagenumber = Integer.parseInt(page_string);            
            String numOfpage = request.getParameter("numOfpage");
            int numofpage = Integer.parseInt(numOfpage);
                                  
            int pageVolum = (pagenumber-1)*numofpage;            
            String query3 = String.format(" LIMIT %s OFFSET %s ", String.valueOf(numofpage+5), String.valueOf(pageVolum));
            
            // parse title into different parameters and combine into format '+word* '
            String[] split_title = title.split("\\s+");
            String title_match = "";
            for (int i=0; i<split_title.length; i++) {
            	String temp = "+" + split_title[i] + "* ";
            	title_match += temp;
            }
            
            System.out.println(title_match);
                        
            // Generate query for preparedStatement
            String sql = "SELECT m.*, group_concat(distinct s.id), group_concat(distinct s.name), group_concat(distinct g.name), r.rating"
            		+ " FROM movies m, stars s, stars_in_movies sm, genres_in_movies gm, genres g, ratings r"
            		+ " WHERE s.id = sm.starId and m.id = sm.movieId and g.id = gm.genreId and m.id = gm.movieId and r.movieId = m.id"
            		+ " and m.id IN"
            		+ " (SELECT m.id FROM movies m, stars so"
            		+ " WHERE ((MATCH (m.title) AGAINST(? IN BOOLEAN MODE)) or (ed(title, ?) <= ?) or (? = '') )"
            		//+ " WHERE ((MATCH (m.title) AGAINST(? IN BOOLEAN MODE)) or (? = '') )"
            		+ " and (m.year like ? or ? ='')"
            		+ " and (m.director like ? or ? ='')"
            		+ " and (s.name like ? or ? =''))"
            		+ " GROUP BY m.id, r.rating ";

            sql = sql + query2 + query3;
            
            System.out.println(sql);
            
            // preparedStatement
            PreparedStatement preparedStatement = dbCon.prepareStatement(sql);
    		preparedStatement.setString(1, title_match);
    		preparedStatement.setString(2, title);
    		
    		int len = Math.floorDiv(title.length(), 3);
    		preparedStatement.setInt(3, len);
    		preparedStatement.setString(4, title);
    		preparedStatement.setString(5, year);
    		preparedStatement.setString(6, year);
    		preparedStatement.setString(7, '%'+director+'%');
    		preparedStatement.setString(8, director);
    		preparedStatement.setString(9, '%'+starname+'%');
    		preparedStatement.setString(10, starname);
    		
    		/*
    		preparedStatement.setString(3, year);
    		preparedStatement.setString(4, year);
    		preparedStatement.setString(5, '%'+director+'%');
    		preparedStatement.setString(6, director);
    		preparedStatement.setString(7, '%'+starname+'%');
    		preparedStatement.setString(8, starname);
            */
            System.out.println(preparedStatement);
            
            // Perform the query
    		ResultSet rs = preparedStatement.executeQuery(); 
    		
    		// end time of a JDBC
    		long endTimeJDBC = System.nanoTime();
    		
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
    		// end time of a query
    		long endTime = System.nanoTime();
            
            // write JSON string to output
            out.write(jsonArray.toString());            
            // set response status to 200 (OK)
            response.setStatus(200);
            System.out.println("preparedStatement Success");

            // Close all structures
            rs.close();
            preparedStatement.close();
            dbCon.close();
            
            // compute the time it uses for query and only JDBC part
            long elapsedTime = endTime - startTime; // elapsed time in nano seconds. Note: print the values in nano seconds 
            long elapsedTimeJDBC = endTimeJDBC - startTimeJDBC; // elapsed time in nano seconds. Note: print the values in nano seconds 
                        
            // write time period to test file
            FileWriter writer;
            //if (firstrecord == true) {
            //	writer = new FileWriter(myfile);
            //	firstrecord = false;
            //} else {
            	writer = new FileWriter(myfile, true);
            //}
            writer.write(String.valueOf(elapsedTime)+" "+ String.valueOf(elapsedTimeJDBC) + "\n");
            writer.close();
            
            System.out.println("TS: "+ String.valueOf(elapsedTime)+ " TJ: "+ String.valueOf(elapsedTimeJDBC));

        }catch (SQLException ex) {
            ex.printStackTrace();
            while (ex != null) {
                System.out.println("SQL Exception:  " + ex.getMessage());
                ex = ex.getNextException();
            } // end while
        } // end catch SQLException 
        //Exception e
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