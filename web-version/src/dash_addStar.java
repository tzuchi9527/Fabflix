import com.google.gson.JsonObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.IOException;
import java.sql.*;

//
@WebServlet(name = "dash_addStar", urlPatterns = "/api/dash_addStar")
public class dash_addStar extends HttpServlet {
    private static final long serialVersionUID = 1L;


    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /*
    	String loginUser = "root";
        String loginPasswd = "database84";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb?useSSL=true";
        */

        	try {

        		// connect to database
        		//Class.forName("com.mysql.jdbc.Driver").newInstance();
        		//Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
                Context initCtx = new InitialContext();
                Context envCtx = (Context) initCtx.lookup("java:comp/env");
                if (envCtx == null) {
                	System.out.println("envCtx is NULL");
                }
                // Look up our data source
                DataSource ds = (DataSource) envCtx.lookup("jdbc/TestWriteDB");

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
        		// ask for the max id before insert any starname
        		String query = "SELECT MAX(id) from stars";
        		
        		PreparedStatement preparedStatement = dbCon.prepareStatement(query);
        		ResultSet rs = preparedStatement.executeQuery();
        		
        		System.out.println("preparesStaement query Success");
        		
        		String star_id = new String("");
        		if (rs.next()) {
        			star_id = rs.getString(1);
        		}
        		// get star ID and pluse 1
        		String star_id_str = star_id.substring(2,star_id.length());
        		int star_id_int = Integer.parseInt(star_id_str);
        		star_id_int += 1;
        		
        		System.out.println(star_id_int);
        		
        		// get parameter starname, birthyear
        		String starname = request.getParameter("starname");
        		
        		// check if starname is not empty
        		if (starname == "") {
        			JsonObject responseJsonObject = new JsonObject();
    	    		responseJsonObject.addProperty("status", "fail");
    	    		responseJsonObject.addProperty("message", "Star Name is required");
    	    		response.getWriter().write(responseJsonObject.toString()); 
        		}
        		else {
	        		String year = new String("");
	        		String upSql = new String("");
	        		PreparedStatement preparedStatementUp = null;
	        		if (request.getParameter("year")!="") {
	        			year = request.getParameter("year");
	      			
	            		upSql = "INSERT INTO stars (id, name, birthYear)"
	                			+ " VALUES(?, ?, ?)";
	        			preparedStatementUp = dbCon.prepareStatement(upSql);
	        			preparedStatementUp.setString(1, "nm"+String.valueOf(star_id_int));
	        			preparedStatementUp.setString(2, starname);
	        			preparedStatementUp.setString(3, year);
	        		}// no year parameter
	        		else {
	        			upSql = "INSERT INTO stars (id, name) VALUES(?, ?)";
	        			preparedStatementUp = dbCon.prepareStatement(upSql);
	        			preparedStatementUp.setString(1, "nm"+String.valueOf(star_id_int));
	        			preparedStatementUp.setString(2, starname);
	        		}  			
	    			System.out.println(preparedStatementUp);
	    			
	    			int result = preparedStatementUp.executeUpdate();
	    			if (result > 0) {
	    				System.out.println("success insert 1 record");
	    				JsonObject responseJsonObject = new JsonObject();
	    	    		responseJsonObject.addProperty("status", "success");
	    	    		responseJsonObject.addProperty("message", "Add "+ starname +" is Success");
	    	    		response.getWriter().write(responseJsonObject.toString());	    	    		
	    			}else {
	    				System.out.println("fail qq");
	    				JsonObject responseJsonObject = new JsonObject();
	    	    		responseJsonObject.addProperty("status", "fail");
	    	    		responseJsonObject.addProperty("message", "fail to add new movie star info");
	    	    		response.getWriter().write(responseJsonObject.toString()); 
	    			}	
		    		
		    		System.out.println("preparedStatement update Success");
        		}
        	}
        	catch (SQLException ex) {
                ex.printStackTrace();
                while (ex != null) {
                    System.out.println("SQL Exception:  " + ex.getMessage());
                    ex = ex.getNextException();
                } // end while
            } // end catch SQLException 
	        catch(Exception ex) {
	        	System.out.println("fail");
				JsonObject responseJsonObject = new JsonObject();
	    		responseJsonObject.addProperty("status", "fail");
	    		responseJsonObject.addProperty("message", "fail to add new movie star info");
	    		response.getWriter().write(responseJsonObject.toString());
	        }  
    }
}
