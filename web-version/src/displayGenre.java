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
@WebServlet(name = "displayGenre", urlPatterns = "/displayGenre")
public class displayGenre extends HttpServlet {
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
        	
        	
            // set query
            String sql = "SELECT name FROM genres";
         
            // establish preparedStatement
            PreparedStatement preparedStatement = dbCon.prepareStatement(sql);
    		ResultSet rs = preparedStatement.executeQuery(); 
    		
            JsonArray jsonArray = new JsonArray();
            
            // Iterate through each row of rs and store the data to jsonArray
            while (rs.next()) {
            	String genre = rs.getString(1);
                
                // Create a JsonObject based on the data we retrieve from rs
                //JsonObject jsonObject = new JsonObject();
                //jsonObject.addProperty("genre", genre);
                
                //jsonArray.add(jsonObject);
                jsonArray.add(genre);

            }
            
            // write JSON string to output
            out.write(jsonArray.toString());            
            // set response status to 200 (OK)
            response.setStatus(200);
            
            // Close all structures
            rs.close();
            preparedStatement.close();
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