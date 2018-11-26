import com.google.gson.JsonObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.IOException;
import java.sql.*;;

//
@WebServlet(name = "dash_addMovie", urlPatterns = "/api/dash_addMovie")
public class dash_addMovie extends HttpServlet {
    private static final long serialVersionUID = 1L;   
    private String dropStoredProcedure() {
        return "DROP PROCEDURE IF EXISTS add_movie;";
    }
    private String createStoredProcedure() {
    	String procedure = 
                "CREATE PROCEDURE add_movie(IN movieID varchar(10), IN title varchar(100), IN year int(11), IN director varchar(100), " + 
                "    IN starID varchar(10), IN star varchar(100), IN genre varchar(32)) "+
                "BEGIN" + 
                "    INSERT INTO movies VALUES(movieID, title, year, director);" + 
                "    INSERT INTO stars(id, name) SELECT * FROM (SELECT starID, star) AS tstars WHERE NOT EXISTS (SELECT * FROM stars WHERE name = star);" + 
                "    INSERT INTO stars_in_movies VALUES(starID, movieID);" + 
                "    INSERT INTO genres (name) SELECT * FROM (SELECT genre) AS tg WHERE NOT EXISTS (SELECT * FROM genres WHERE name = genre);" + 
                "    INSERT INTO genres_in_movies VALUES((SELECT id FROM genres WHERE name = genre), movieID);"+ 
                "END";
        
        return procedure;
    }

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
        		
        		// get all parameters
        		String title = request.getParameter("title");
        		String year = request.getParameter("year");
        		String director = request.getParameter("director");
        		String starname = request.getParameter("star");
        		String genre = request.getParameter("genre");
     		
        		// check if title, year, director, starname, genre is not null
        		boolean blank = false;
        		String message = new String("");
        		if (title=="") {
        			blank = true;
        			message += "Movie title. ";
        		}if (year == "") {
        			blank = true;
        			message += "year. ";
        		}if (director == "") {
        			blank = true;
        			message += "director. ";
        		}if (starname == "") {
        			blank = true;
        			message += "starname. ";
        		}if (genre == "") {
        			blank = true;
        			message += "genre. ";
        		}
        		if (blank == true) {
        			System.out.println("fail");
        			message += "are required.";
    				JsonObject responseJsonObject = new JsonObject();
    	    		responseJsonObject.addProperty("status", "fail");
    	    		responseJsonObject.addProperty("message", message);
    	    		response.getWriter().write(responseJsonObject.toString());
        		}
        		else {
        			System.out.println("here");
        			PreparedStatement preparedStatement = null;
                    ResultSet rs = null;
                    String sql = "";
        			// check if movie already exist
        			sql = "SELECT * FROM movies WHERE title=? and year=? and director=?";
        	
        			preparedStatement = dbCon.prepareStatement(sql);
        			preparedStatement.setString(1, title);
        			preparedStatement.setString(2, year);
        			preparedStatement.setString(3, director);
            		rs = preparedStatement.executeQuery();
        			// movie already in DB
            		if (rs.next()) {
            			JsonObject responseJsonObject = new JsonObject();
        	    		responseJsonObject.addProperty("status", "fail");
        	    		responseJsonObject.addProperty("message", "Movie alreday exists.");
        	    		response.getWriter().write(responseJsonObject.toString());
            		} // add new movie
            		else {
            			System.out.println("here1");
            			sql = dropStoredProcedure();
            			preparedStatement = dbCon.prepareStatement(sql);
            			preparedStatement.execute();
            			System.out.println("here2");
                        sql = createStoredProcedure();
                        preparedStatement = dbCon.prepareStatement(sql);
                        preparedStatement.execute();
                        System.out.println("here3");
            			// get movieID --> final_mID
            			sql = "SELECT MAX(id) from movies";
                		preparedStatement = dbCon.prepareStatement(sql);
                		rs = preparedStatement.executeQuery();     		
                		String movie_id = new String("");
                		if (rs.next()) {
                			movie_id = rs.getString(1);
                		}
                		String movie_id_str = movie_id.substring(2, movie_id.length());
                		int movie_id_int = Integer.parseInt(movie_id_str);
                		movie_id_int += 1;
                		String final_mID = movie_id.substring(0, 2) + String.valueOf(movie_id_int);
                		
                		System.out.println(final_mID);
            			
                		// get movie star ID, starID --> final_starID
                		String final_starID = new String("");
                		sql = "SELECT id from stars where name=? ";
                		preparedStatement = dbCon.prepareStatement(sql);
                		preparedStatement.setString(1, starname);
                		rs = preparedStatement.executeQuery(); 
                		// movie star exist
                		boolean starExist = false;
                		if (rs.next()) {
                			final_starID = rs.getString(1);
                			starExist = true;
                			System.out.println("exist star");
                		}// movie star not exist
                		else {
                			sql = "SELECT MAX(id) from stars";
                    		preparedStatement = dbCon.prepareStatement(sql);
                    		rs = preparedStatement.executeQuery();   
                    		rs.next();
                    		String star_id = rs.getString(1);
                    		String star_id_str = star_id.substring(2, star_id.length());
                    		int star_id_int = Integer.parseInt(star_id_str);
                    		star_id_int += 1;
                    		final_starID = star_id.substring(0,2) + String.valueOf(star_id_int);
                		}
                		System.out.println(final_starID);
                		System.out.println("star Exist "+starExist);
                		
                		sql = "{call add_movie(?,?,?,?,?,?,?)}";
                		
                	
                		
                		CallableStatement cstmt = dbCon.prepareCall(sql);
                		cstmt.setString(1, final_mID);
                		cstmt.setString(2, title);
                		cstmt.setInt(3, Integer.parseInt(year));
                		cstmt.setString(4, director);
                		cstmt.setString(5, final_starID);
                		cstmt.setString(6, starname);
                		cstmt.setString(7, genre);
                		
                		System.out.println(cstmt);
                		
                		cstmt.executeUpdate();
                		
                		float rating = 0;
                		int numVote = 0;
                		sql = "INSERT INTO ratings (movieId, rating, numVotes)"
                    			+ " VALUES(?, ?, ?)";
            			preparedStatement = dbCon.prepareStatement(sql);
            			preparedStatement.setString(1, final_mID);
            			preparedStatement.setFloat(2, rating);
            			preparedStatement.setInt(3,  numVote);
            			preparedStatement.executeUpdate();
            			
            			System.out.println(preparedStatement);
                		
                		cstmt.close();
                		
                		JsonObject responseJsonObject = new JsonObject();
        	    		responseJsonObject.addProperty("status", "success");
        	    		responseJsonObject.addProperty("message", "Add movie success");
        	    		response.getWriter().write(responseJsonObject.toString());
            		}
            		rs.close();
                    preparedStatement.close();
                    dbCon.close();
        		}
        	}
        	catch (SQLException ex) {
                ex.printStackTrace();
                while (ex != null) {
                    System.out.println("SQL Exception:  " + ex.getMessage());
                    ex = ex.getNextException();
                } // end while
            } // end catch SQLException 
	        catch(Exception e) {
	        	System.out.println("fail");
				JsonObject responseJsonObject = new JsonObject();
	    		responseJsonObject.addProperty("status", "fail");
	    		responseJsonObject.addProperty("message", "fail to add new movie");
	    		response.getWriter().write(responseJsonObject.toString());
	        }  
    }
}