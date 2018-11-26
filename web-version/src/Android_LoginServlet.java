import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasypt.util.password.StrongPasswordEncryptor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

/*
 * We create a separate android login Servlet here because
 *   the recaptcha secret key for web and android are different.
 * 
 */
@WebServlet(name = "Android_LoginServlet", urlPatterns = "/api/android-login")
public class Android_LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public Android_LoginServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String loginUser = "root";
        String loginPasswd = "database84";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb?useSSL=true";
    	
    	String username = request.getParameter("username");
        String password = request.getParameter("password");
        
    	try {
        	Class.forName("com.mysql.jdbc.Driver").newInstance();
    		Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);    		

    		String sql = "SELECT * from customers c where c.email=?";
    		PreparedStatement preparedStatement = connection.prepareStatement(sql);
    		preparedStatement.setString(1, username);
    		
    		ResultSet rs = preparedStatement.executeQuery();		

    		boolean success = false;
    		if (rs.next()) {
    		    // get the encrypted password from the database
    			String encryptedPassword = rs.getString("c.password");
        		String db_username = rs.getString("c.email");
        		String db_id = rs.getString("c.id");
    			
    			// use the same encryptor to compare the user input password with encrypted password stored in DB
    			success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
    			// username and password are correct
    			if (success == true) {
    				request.getSession().setAttribute("user", new User(username));
              		request.getSession().setAttribute("u_user", db_username);
              		request.getSession().setAttribute("u_password", encryptedPassword);
              		request.getSession().setAttribute("u_id", db_id);           		

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
        
        
        
        /*
        JsonObject loginResult = LoginVerifyUtils.verifyUsernamePassword(username, password);
        
        if (loginResult.get("status").getAsString().equals("success")) {
            // login success
            request.getSession().setAttribute("user", new User(username));
            response.getWriter().write(loginResult.toString());
        } else {
            response.getWriter().write(loginResult.toString());
        }*/

    }

}
