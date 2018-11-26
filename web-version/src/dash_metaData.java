import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonObject;


// this annotation maps this Java Servlet Class to a URL
@WebServlet("/dash_metaData")
public class dash_metaData extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public dash_metaData() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // change this to your own mysql username and password
        /*
		String loginUser = "root";
        String loginPasswd = "database84";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb?useSSL=true";
		*/
        // set response mime type
        response.setContentType("text/html"); 

        // get the printwriter for writing response
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("<meta charset=\"utf-8\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">");
        
        out.println("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\">");
        out.println("<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js\"></script>");
        out.println("<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\"></script>");       
        out.println("<title>MetaData</title>");
        out.println("</head>");
        
        out.println("<body BGCOLOR=\"#FDF5E6\">");
        out.println("<nav class=\"navbar navbar-default\">");
        out.println("<div class=\"container-fluid\">");
        out.println("<div class=\"navbar-header\">");
        out.println(" <a class=\"navbar-brand\" href=\"#\">Fabflix</a>");
        out.println("</div>");
        out.println("<ul class=\"nav navbar-nav\">");
        out.println("<li><a href=\"dash_main.html\">MAIN Employee</a></li>");
        out.println(" <li class=\"active\"><a href=\"#\">MetaData</a></li>");
        out.println("</ul></div></nav>");
        
        try {
        		//Class.forName("com.mysql.jdbc.Driver").newInstance();
        		//Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
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
        		
        		//String sql = "";
        		// execute query
        		//ResultSet resultSet = statement.executeQuery(execQuery);

        		String[] table = {"movies", "stars", "stars_in_movies", "genres", "genres_in_movies", "customers", "sales", "creditcards", "ratings"};
        		out.println("<body>");
        		for (int i=0; i<table.length; i++) {
        			String sql_movies = "describe " + table[i];
            		PreparedStatement preparedStatement = dbCon.prepareStatement(sql_movies);
            		ResultSet rs = preparedStatement.executeQuery();      	
            		
            		out.println("<h3>"+ table[i] +"</h3>");
            		out.println("<table class=\"table table-striped\"");
            		out.println("<tr>");
            		out.println("<th>Field</th>");
            		out.println("<th>Type</th>");
            		out.println("</tr>");            		

            		while (rs.next()) {
            			// get a star from result set
            			String Field = rs.getString("Field");
            			String Type = rs.getString("Type");
            			out.println("<tr>");
            			out.println("<td>" + Field + "</td>");
            			out.println("<td>" + Type + "</td>");
            			out.println("</tr>");
            		}
            		out.println("</table>");
            		rs.close();
            		preparedStatement.close();
        		}
        		out.println("</body>");    		
        		dbCon.close();
        		
        } catch (Exception e) {
        		e.printStackTrace();     		
        		out.println("<body>");
        		out.println("<p>");
        		out.println("Exception in doGet: " + e.getMessage());
        		out.println("</p>");
        		out.print("</body>");
        }
        
        out.println("</html>");
        out.close();
	}


}
