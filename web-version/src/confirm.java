import com.google.gson.JsonObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;

//
@WebServlet(name = "confirm", urlPatterns = "/api/confirm")
public class confirm extends HttpServlet {
    private static final long serialVersionUID = 1L;


    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /*
    	String loginUser = "root";
        String loginPasswd = "database84";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb?useSSL=true";
        */
        HttpSession session = request.getSession(); // Get a instance of current session on the request
        HashMap<String, String[]> previousItems = (HashMap<String, String[]>) session.getAttribute("previousItems");
        
        // If "previousItems" is not found on session, means this is a new user, thus we create a new previousItems ArrayList for the user
        if (previousItems.isEmpty()) {
        	System.out.println("no item");
        	JsonObject responseJsonObject = new JsonObject();
    		responseJsonObject.addProperty("status", "fail");
    		responseJsonObject.addProperty("message", "there is no item in cart");
    		response.getWriter().write(responseJsonObject.toString()); 
        }else {  
        	try {
        		System.out.println(previousItems);
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
                 
                 if (ds == null) {
                 	System.out.println("ds is null.");
                 }
                 
                 Connection dbCon = ds.getConnection();
                 if (dbCon == null)
                 {
                 	System.out.println("dbcon is null.");
                 }
        		
        		// ask for the max id before insert any transaction
        		String query = "SELECT MAX(id) from sales";
        		//ResultSet rs = statement.executeQuery(query);
        		PreparedStatement preparedStatement = dbCon.prepareStatement(query);
        		ResultSet rs = preparedStatement.executeQuery();
        		
        		System.out.println("preparesStaement query Success");
        		
        		String sale_id = new String("");
        		if (rs.next()) {
        			sale_id = rs.getString(1);
        		}
        		int sale_id_int = Integer.parseInt(sale_id);
      		
        		String user_id = (String) request.getSession().getAttribute("u_id");
        		
        		String message = new String("");
        		
        		for(Object item:previousItems.keySet()) { 
        			String[] value = previousItems.get(item);
        			String movie_id = (String) item;
        			String movie_title = value[0];
        			String num = value[1];
        			String saleId = "";
        			for (int n=0; n<Integer.parseInt(num); n++) {
                    	/*
        				String upQuery = String.format("INSERT INTO sales (customerId, movieId, saleDate)"
                    			+ " VALUES('%s', '%s', CURDATE())", user_id, movie_id);
            			int result = statement.executeUpdate(upQuery);
            			*/
            			String upSql = "INSERT INTO sales (customerId, movieId, saleDate)"
                    			+ " VALUES(?, ?, CURDATE())";
            			PreparedStatement preparedStatementUp = dbCon.prepareStatement(upSql);
            			preparedStatementUp.setString(1, user_id);
            			preparedStatementUp.setString(2, movie_id);
            			
            			System.out.println(preparedStatementUp);
            			
            			int result = preparedStatementUp.executeUpdate();
            			
            			if (result > 0) {
            				System.out.println("success insert 1 record");
            				// delete that item in shopping cart
            			}else {
            				System.out.println("fail qq");
            			}	
            			sale_id_int += 1;
            			System.out.println(sale_id_int);
            			saleId += String.valueOf(sale_id_int) + ", ";
        			}
        			message = message + "Sale ID: "+ saleId + " ";
        			message = message + "movie title: " + movie_title + " ";
        			message = message +  "num: " + num + "///  ";
                }    
        		message += "  Success!";
        		previousItems.clear();
	        	JsonObject responseJsonObject = new JsonObject();
	    		responseJsonObject.addProperty("status", "success");
	    		responseJsonObject.addProperty("message", message);
	    		response.getWriter().write(responseJsonObject.toString());
	    		
	    		System.out.println("preparedStatement update Success");
	    		
        	}
        	catch (SQLException ex) {
                ex.printStackTrace();
                while (ex != null) {
                    System.out.println("SQL Exception:  " + ex.getMessage());
                    ex = ex.getNextException();
                } // end while
            } // end catch SQLException 
	        catch(java.lang.Exception ex) {
	        	JsonObject responseJsonObject = new JsonObject();
	    		responseJsonObject.addProperty("status", "fail");
	    		responseJsonObject.addProperty("message", "something wrong");
	    		response.getWriter().write(responseJsonObject.toString());
	        }  
        }
    }
}
