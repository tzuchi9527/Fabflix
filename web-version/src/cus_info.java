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
@WebServlet(name = "cus_info", urlPatterns = "/api/info")
public class cus_info extends HttpServlet {
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
    	try {	
    		// get customer info parameter from js
    		String firstname = request.getParameter("firstname");
        	String lastname = request.getParameter("lastname");
        	String ccId = request.getParameter("ccId");
        	String date = request.getParameter("date");
        	String address = request.getParameter("address");

        	// connect to DB
        	//Class.forName("com.mysql.jdbc.Driver").newInstance();
    		//Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
    		
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null) {
            	System.out.println("envCtx is NULL");
            }
            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");

            if (ds == null) {
            	System.out.println("ds is null.");
            }
            
            Connection dbCon = ds.getConnection();
            if (dbCon == null)
            {
            	System.out.println("dbcon is null.");
            }
        	
        	
    		String user_id = (String) request.getSession().getAttribute("u_id");
    		
    		System.out.println(user_id);
    		
    		/*
    		String query = String.format("SELECT c.*, cc.* FROM customers c, creditcards cc"
    				+ " WHERE c.id = '%s' and cc.id = c.ccId",
    				user_id);
    		ResultSet rs = statement.executeQuery(query);
 			*/
    		String sql = "SELECT c.*, cc.* FROM customers c, creditcards cc"
    				+ " WHERE c.id = ? and cc.id = c.ccId";
    		PreparedStatement preparedStatement = dbCon.prepareStatement(sql);
    		preparedStatement.setString(1, user_id);
    		ResultSet rs = preparedStatement.executeQuery(); 
    		
    		
    		if (rs.next()) {
        		String db_firstname = rs.getString("c.firstName");
        		String db_lastname = rs.getString("c.lastName");
        		String db_ccId = rs.getString("c.ccId");
        		String db_address = rs.getString("c.address");
        		String db_date = rs.getString("cc.expiration");
        		
        		// success
        		String mes_temp = new String("please check again: ");
        		boolean correct = true;
        		if (!firstname.equals(db_firstname)) {
        			mes_temp += "first name. ";
        			correct = false;
        		}
        		if (!lastname.equals(db_lastname)) {
        			mes_temp += "last name. ";
        			correct = false;
        		}
        		if (!ccId.equals(db_ccId)) {
        			mes_temp += "credeit card number. ";
        			correct = false;
        		}
        		if (!date.equals(db_date)){
        			mes_temp += "credeit card expiration date. ";
        			correct = false;
        		}
        		if (!address.equals(db_address)) {
        			mes_temp += "address. ";
        			correct = false;
        		}
        		if (correct==true) {
            		JsonObject responseJsonObject = new JsonObject();
            		responseJsonObject.addProperty("status", "success");
            		responseJsonObject.addProperty("message", "success");
            		response.getWriter().write(responseJsonObject.toString());
        		}
        		else {
        			JsonObject responseJsonObject = new JsonObject();
            		responseJsonObject.addProperty("status", "fail");
            		responseJsonObject.addProperty("message", mes_temp);
            		response.getWriter().write(responseJsonObject.toString());
        		}
        		
        	}
        	// no result found from the database
    		else {
        		JsonObject responseJsonObject = new JsonObject();
        		responseJsonObject.addProperty("status", "fail");
        		responseJsonObject.addProperty("message", "user does not exist");
        		response.getWriter().write(responseJsonObject.toString());
        	}
    		System.out.println("preparedStatement Success");
        }catch (SQLException ex) {
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
