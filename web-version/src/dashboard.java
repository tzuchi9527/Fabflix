import com.google.gson.JsonObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;



@WebServlet(name = "dashboard", urlPatterns = "/api/dash_login")

public class dashboard extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	    	
    	//String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        // Verify reCAPTCHA
    	/*
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
        	JsonObject responseJsonObject = new JsonObject();
    		responseJsonObject.addProperty("status", "fail");
    		responseJsonObject.addProperty("message", e.getMessage());
    		response.getWriter().write(responseJsonObject.toString());            
            return;
        }
    	*/
        // connect to movieDB
        /*
    	String loginUser = "root";
        String loginPasswd = "database84";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb?useSSL=true";
        */
		System.out.println("I'm in");

    	try {	
    		// get username and password parameters
    		String username = request.getParameter("username");
        	String password = request.getParameter("password");
        	
        	//Class.forName("com.mysql.jdbc.Driver").newInstance();
    		//Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);    		
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null) {
            	System.out.println("envCtx is NULL");
            }
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
        	
    		String sql = "SELECT * from employees e where e.email=?";
    		PreparedStatement preparedStatement = dbCon.prepareStatement(sql);
    		preparedStatement.setString(1, username);
    		
    		ResultSet rs = preparedStatement.executeQuery();		

    		boolean success = false;
    		if (rs.next()) {
    		    // get the encrypted password from the database
    			String encryptedPassword = rs.getString("e.password");
        		String db_username = rs.getString("e.email");
        		String db_fullName = rs.getString("e.fullName");
    			
    			// use the same encryptor to compare the user input password with encrypted password stored in DB
    			success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
    			// username and password are correct
    			if (success == true) {
    				request.getSession().setAttribute("user", new User(username));
              		request.getSession().setAttribute("u_user", db_username);
              		request.getSession().setAttribute("u_password", encryptedPassword);
              		request.getSession().setAttribute("u_fullName", db_fullName);           		

            		JsonObject responseJsonObject = new JsonObject();
            		responseJsonObject.addProperty("status", "success");
            		responseJsonObject.addProperty("message", "success");
            		response.getWriter().write(responseJsonObject.toString());
    			}// password not correct
    			else {
    				JsonObject responseJsonObject = new JsonObject();
            		responseJsonObject.addProperty("status", "fail");
            		responseJsonObject.addProperty("message", "incorrect password");
            		response.getWriter().write(responseJsonObject.toString());
    			}
    		}
    		// username not exist
    		else {
    			JsonObject responseJsonObject = new JsonObject();
        		responseJsonObject.addProperty("status", "fail");
        		responseJsonObject.addProperty("message", "user " + username + " does not exist");
        		response.getWriter().write(responseJsonObject.toString());
    		}
    		
    		System.out.println("verify " + username + " - " + password + " " + success);
        }
        
        catch(Exception e) {
        	JsonObject responseJsonObject = new JsonObject();
    		responseJsonObject.addProperty("status", "fail");
    		responseJsonObject.addProperty("message", "something wrong");
    		response.getWriter().write(responseJsonObject.toString());
        }       
    }
}