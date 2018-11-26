
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class MovieSAXParser extends DefaultHandler {

	String loginUser = "root";
    String loginPasswd = "database84";
    String loginUrl = "jdbc:mysql://localhost:3306/moviedb?useSSL=true";
    
    List<Movie> movieList;

    private String tempVal;
    private String dirName;

    //to maintain context
    private Movie tempMov;

    public MovieSAXParser() {
    	movieList = new ArrayList<Movie>();
    }

    public void runExample() throws Exception {
    	long startTime = System.currentTimeMillis();
        parseDocument();
        insertData();
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Times: " + estimatedTime);
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("mains243.xml", this);
            
            //System.out.println("Connect");

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Iterate through the list and insert it to the database
     * @throws SQLException 
     */
    private void insertData() throws Exception {        
        /*
        Iterator<Movie> it = movieList.iterator();
        while (it.hasNext()) {
            System.out.println(it.next().toString());
        }
        */
        System.out.println("No of Movies '" + movieList.size() + "'.");
        
        
        try {
        	// connect to DB
        	Class.forName("com.mysql.jdbc.Driver").newInstance();
        	System.out.println("connecting...");
        	Connection dbCon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        	System.out.println("Connect success.");
        	// ask for the max id before insert any movie
    		String query = "SELECT MAX(id) from movies";
    		PreparedStatement preparedStatement = dbCon.prepareStatement(query);
    		ResultSet rs = preparedStatement.executeQuery();
    		
    		String movie_id = new String("");
    		if (rs.next()) {
    			movie_id = rs.getString(1);
    		}
    		// get movie ID and plus 1
    		String movie_id_str = movie_id.substring(2,movie_id.length());
    		int movie_id_int = Integer.parseInt(movie_id_str);
    		movie_id_int += 1;
    		
    		// get all genres  		
    		query = "SELECT id, name FROM genres";
    		preparedStatement = dbCon.prepareStatement(query);
    		rs = preparedStatement.executeQuery();
    		HashMap<String,Integer> allGenres = new HashMap<String, Integer>();
    		String Gid="";
    		while(rs.next()) {
    			Gid = rs.getString("id");
    			String name = rs.getString("name");
    			allGenres.put(name, Integer.parseInt(Gid));
    		}
    		int genreMaxId = Integer.parseInt(Gid);
    		
    		// get all movies to check duplicate
    		query = "SELECT id, title, director, year FROM movies";
    		preparedStatement = dbCon.prepareStatement(query);
    		rs = preparedStatement.executeQuery();
    		HashMap<String,Movie> allMovies = new HashMap<String, Movie>();
    		while(rs.next()) {
    			String title = rs.getString("title");
    			String director = rs.getString("director");
    			String year = rs.getString("year");
    			String id = rs.getString("id");
    			Movie tempMv = new Movie();
    			tempMv.setDirector(director);
    			tempMv.setYear(Integer.parseInt(year));
    			tempMv.setId(id);
    			
    			allMovies.put(title, tempMv);
    		}
    		
    		// use batch to optimize
    		dbCon.setAutoCommit(false);
            PreparedStatement preparedMvStatement = null;
            PreparedStatement preparedGenStatement = null;
            PreparedStatement preparedGMStatement = null;
            PreparedStatement preparedRatingStatement = null;
            Iterator<Movie> it = movieList.iterator();
            
            // insert data to database
            // use preparedStatement to optimize
        	String sql = "INSERT INTO movies (id, title, year, director)"
        			+ " VALUES(?,?,?,?)";
        	preparedMvStatement = dbCon.prepareStatement(sql);
        	String genresSql = "INSERT INTO genres (name) VALUES(?)";
        	preparedGenStatement = dbCon.prepareStatement(genresSql);
        	String GMsql = "INSERT INTO genres_in_movies (genreId, movieId) VALUES(?,?)";
        	preparedGMStatement = dbCon.prepareStatement(GMsql);
        	String ratings = "INSERT INTO ratings (movieId, rating, numVotes) VALUES(?,?,?)";
        	preparedRatingStatement = dbCon.prepareStatement(ratings);
        	
            while (it.hasNext()) {
            	Movie mv = it.next();
            	
            	if(mv.getTitle().isEmpty()) {
            		System.out.println("Movie Title(Required) is missing.");
            		continue;
            	}
            	if(mv.getDirector().isEmpty()) {
            		System.out.println("Movie Director(Required) is missing.");
            		continue;
            	}
            	if(mv.getYear()==0) {
            		System.out.println("Movie Year(Required) is missing.");
            		continue;
            	}
            	
            	// check if movie already exists
            	if (allMovies.containsKey(mv.getTitle())) {
            		//System.out.println("Movie title already exists: "+mv.getTitle());
            		Movie tempMv = new Movie();
            		tempMv = allMovies.get(mv.getTitle());
            		
            		if (tempMv.getDirector().equals(mv.getDirector()) && tempMv.getYear()==mv.getYear()) {
            			System.out.println("Movie:" + mv.getTitle() + " already exists in database");
            			continue;
            		}
            	}
            	
            	// insert data to database
            	//String sql = "INSERT INTO movies (id, title, year, director)"
            	//		+ " VALUES(?,?,?,?)";
            	preparedMvStatement.setString(1, "tt"+String.valueOf(movie_id_int));
            	preparedMvStatement.setString(2, mv.getTitle());
            	preparedMvStatement.setInt(3, mv.getYear());
            	preparedMvStatement.setString(4, mv.getDirector());
            	preparedMvStatement.addBatch();
            	
            	// add to allMovies
            	Movie tmpMv = new Movie();
            	tmpMv.setId("tt"+String.valueOf(movie_id_int));
            	tmpMv.setYear(mv.getYear());
            	tmpMv.setDirector(mv.getDirector());
            	allMovies.put(mv.getTitle(), tmpMv);
            	
            	// insert ratings default 0
            	float rating = 0;
            	int numVote = 0;
            	preparedRatingStatement.setString(1,"tt"+String.valueOf(movie_id_int));
            	preparedRatingStatement.setFloat(2, rating);
            	preparedRatingStatement.setInt(3, numVote);
            	preparedRatingStatement.addBatch();
            	
            	
            	// insert genres
            	ArrayList<String> genres = mv.getGenres();
            	String movieId="";
            	int id;
            	//System.out.println("get genres: " + genres);
            	if(!genres.isEmpty()) {
            		// get movie id
            		Movie tempMv = new Movie();
            		tempMv = allMovies.get(mv.getTitle());
            		movieId = tempMv.getId();
            		
            		for (int i=0; i<genres.size();i++) {
            			String genre = genres.get(i);
            			// check if genre in table genres or not
            			if (allGenres.containsKey(genre)) {
            				id = allGenres.get(genre);
            				//System.out.println("genre exist");
            			}	
            			else {
            				// insert genres into table genres
	            			//String genresSql = "INSERT INTO genres (name) VALUES(?)";
            				preparedGenStatement.setString(1, genres.get(i));
            				preparedGenStatement.addBatch();
            				
	            			genreMaxId += 1;
	            			id = genreMaxId;
	            			allGenres.put(genre, id);
	            			//System.out.println("insert new genre");
            			}
            			// insert into genres_in_movies
            			//sql = "INSERT INTO genres_in_movies (genreId, movieId) VALUES(?,?)";
            			preparedGMStatement.setInt(1, id);
            			preparedGMStatement.setString(2, movieId);
            			preparedGMStatement.addBatch();

            			//System.out.println("insert genres_in_movies");
            		}
            	}
            	
            	movie_id_int += 1;
            	//System.out.println("Title: " + mv.getTitle());
            }
            preparedMvStatement.executeBatch();
            preparedGenStatement.executeBatch();
            preparedGMStatement.executeBatch();
            preparedRatingStatement.executeBatch();
            dbCon.commit();
            
            System.out.println("Insert movie data success!");
        } catch(Exception e) {
        	System.out.println("Something wrong!");
        }
        
    	
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    	//System.out.println("Start element");
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            //create a new instance of movie
        	tempMov = new Movie();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("film")) {
            //add it to the list
        	tempMov.setDirector(dirName);
            movieList.add(tempMov);
            
        } else if (qName.equalsIgnoreCase("t")) {
        	tempMov.setTitle(tempVal);
        	//tempMov.setDirector(dirName);
        } else if (qName.equalsIgnoreCase("dirname")) {
        	dirName = tempVal;
        	//System.out.println(dirName);
        } else if (qName.equalsIgnoreCase("year")) {
        	try {
        		tempMov.setYear(Integer.parseInt(tempVal));
        	} catch(Exception e) {
        		System.out.println(tempVal+" is not integer");
        		char[] newTempVal;
        		for (int i=0; i < tempVal.length(); i++) {
        			if(!Character.isDigit(tempVal.charAt(i))){
        				newTempVal = tempVal.toCharArray();
        				newTempVal[i] = '0';
        				tempVal = String.valueOf(newTempVal);
        			}
        		}
        		tempMov.setYear(Integer.parseInt(tempVal));
        		System.out.println("Convert it to " + tempVal);
        	}
        } else if (qName.equalsIgnoreCase("cat")) {
        	tempMov.setGenres(tempVal);
        }

    }

    public static void main(String[] args) throws Exception {
        MovieSAXParser spe = new MovieSAXParser();
        spe.runExample();
    }

}