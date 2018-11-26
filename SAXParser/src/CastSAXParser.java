
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class CastSAXParser extends DefaultHandler {

	String loginUser = "root";
    String loginPasswd = "database84";
    String loginUrl = "jdbc:mysql://localhost:3306/moviedb?useSSL=true";
    
    List<Cast> castList;

    private String tempVal;

    //to maintain context
    private Cast tempCast;

    public CastSAXParser() {
    	castList = new ArrayList<Cast>();
    }

    public void runExample() throws SQLException {
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
            sp.parse("casts124.xml", this);

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
    private void insertData() throws SQLException {
    	
    	// check parsing result
        System.out.println("No of Casts: " + castList.size() + "'.");

        /*
        Iterator<Cast> it = castList.iterator();
        while (it.hasNext()) {
            System.out.println(it.next().toString());
        }
        */
        
        try {
        	// connect to DB
        	Class.forName("com.mysql.jdbc.Driver").newInstance();
        	System.out.println("connecting...");
        	Connection dbCon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        	System.out.println("Connect success.");
        	
        	// ask for the max id before insert any starname
    		String query = "SELECT MAX(id) from stars";
    		
    		PreparedStatement preparedStatement = dbCon.prepareStatement(query);
    		ResultSet rs = preparedStatement.executeQuery();
    				
    		String star_id = new String("");
    		if (rs.next()) {
    			star_id = rs.getString(1);
    		}
    		// get star ID and plus 1
    		String star_id_str = star_id.substring(2,star_id.length());
    		int star_id_int = Integer.parseInt(star_id_str);
    		star_id_int += 1;
        	
    		// get all movies to find id
    		query = "SELECT id, title FROM movies";
    		preparedStatement = dbCon.prepareStatement(query);
    		rs = preparedStatement.executeQuery();
    		HashMap<String,String> allMovies = new HashMap<String, String>();
    		while(rs.next()) {
    			String title = rs.getString("title");
    			String id = rs.getString("id");
    			
    			allMovies.put(title, id);
    		}    	
    		
    		// get all stars to find id
    		query = "SELECT name, id FROM stars";
    		preparedStatement = dbCon.prepareStatement(query);
    		rs = preparedStatement.executeQuery();
    		HashMap<String,String> allStars = new HashMap<String, String>();
    		while(rs.next()) {
    			String name = rs.getString("name");
    			String id = rs.getString("id");
    			
    			allStars.put(name, id);
    		}
    		
    		// get all stars_in_movies
    		query = "SELECT starId, movieId FROM stars_in_movies";
    		preparedStatement = dbCon.prepareStatement(query);
    		rs = preparedStatement.executeQuery();
    		HashMap<String,String> allPairs = new HashMap<String, String>();
    		while(rs.next()) {
    			String starId = rs.getString("starId");
    			String movieId = rs.getString("movieId");
    			
    			allPairs.put(movieId, starId);
    		}
    		
    		// use batch to optimize
    		dbCon.setAutoCommit(false);
    		//PreparedStatement preparedMVStatement = null;
    		//PreparedStatement preparedACStatement = null;
    		PreparedStatement preparedSTStatement = null;
    		PreparedStatement preparedSMStatement = null;
    		/*
    		String MVsql = "SELECT id"
        			+ " FROM movies"
        			+ " WHERE title=?";
    		preparedMVStatement = dbCon.prepareStatement(MVsql);
    		
    		String ACsql = "SELECT id"
        			+ " FROM stars"
        			+ " WHERE name=?";
    		preparedACStatement = dbCon.prepareStatement(ACsql);
    		*/
    		String STsql = "INSERT INTO stars (id, name) VALUES (?,?)";
    		preparedSTStatement = dbCon.prepareStatement(STsql);
    		
    		String SMsql = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?,?)";
    		preparedSMStatement = dbCon.prepareStatement(SMsql);
    		
            Iterator<Cast> it = castList.iterator();
            while (it.hasNext()) {
            	Cast ca = it.next();
            	
            	String movie = ca.getMovie();
            	String actor = ca.getActor();
            	
            	if (movie.isEmpty()) {
            		System.out.println("Movie(Required) is empty.");
            		continue;
            	}
            	
            	if(actor.isEmpty()) {
            		System.out.println("Star(Required) is empty");
            		continue;
            	}
            	
            	// already in database
            	if (allMovies.containsKey(movie) && allStars.containsKey(actor)) {
            		String mId = allMovies.get(movie);
            		String sId = allStars.get(actor);
            		if(allPairs.containsKey(mId)) {
            			String ssId = allPairs.get(mId);
            			if (ssId.equals(sId)) {
            				System.out.println(movie+ " and " + actor + " pair already exists");
                    		continue;
            			}
            		}
            	}
            	
            	//System.out.println(movie);
            	//System.out.println(actor);
            	
            	// find movieId
            	String movieId;
            	if(allMovies.containsKey(movie)) {
            		movieId = allMovies.get(movie);
            	} else {
            		System.out.println("Movie: " + movie+" doesn't exist");
            		continue;
            	}
            	
            	//System.out.println("get movieId: "+movieId);
            	
            	
            	// find starId
            	String starId;
            	if (allStars.containsKey(actor)) {
            		starId = allStars.get(actor);
            	} else {
            		// star not exist, insert star to stars
            		starId = "nm"+String.valueOf(star_id_int);
            		
            		//sql = "INSERT INTO stars (id, name) VALUES (?,?)";
            		preparedSTStatement.setString(1, starId);
            		preparedSTStatement.setString(2, actor);
            		preparedSTStatement.addBatch();

            		allStars.put(actor, starId);
                	star_id_int += 1;
            	}
            	
            	//System.out.println("get starId: "+starId);
            	
            	// insert data to stars_in_movies tables
            	//sql = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?,?)";
            	preparedSMStatement.setString(1, starId);
            	preparedSMStatement.setString(2, movieId);
            	preparedSMStatement.addBatch();
            	
            	// add to allPairs
            	allPairs.put(movieId, starId);
            	
            }
            preparedSTStatement.executeBatch();
            preparedSMStatement.executeBatch();
            dbCon.commit();
            
            System.out.println("Insert data success");
        } catch(Exception e) {
        	System.out.println("Something wrong");
        }
        
        
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("filmc")) {
            //create a new instance of movie
        	tempCast = new Cast();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("filmc")) {
            //add it to the list
        	castList.add(tempCast);

        } else if (qName.equalsIgnoreCase("t")) {
        	tempCast.setMovie(tempVal);
        } else if (qName.equalsIgnoreCase("a")) {
        	tempCast.setActor(tempVal);
        }

    }

    public static void main(String[] args) throws SQLException {
    	CastSAXParser spe = new CastSAXParser();
        spe.runExample();
    }

}
