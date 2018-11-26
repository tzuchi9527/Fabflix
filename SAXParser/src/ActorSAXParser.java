
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

public class ActorSAXParser extends DefaultHandler {

	String loginUser = "root";
    String loginPasswd = "database84";
    String loginUrl = "jdbc:mysql://localhost:3306/moviedb?useSSL=true";
   
    List<Actor> actorList;

    private String tempVal;

    //to maintain context
    private Actor tempAct;

    public ActorSAXParser() {
    	actorList = new ArrayList<Actor>();
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
            sp.parse("actors63.xml", this);

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
        System.out.println("No of Actors '" + actorList.size() + "'.");
        /*
        Iterator<Actor> it = actorList.iterator();
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
    		
    		// get all stars to check duplicate
    		query = "SELECT name, birthYear FROM stars";
    		preparedStatement = dbCon.prepareStatement(query);
    		rs = preparedStatement.executeQuery();
    		HashMap<String,Actor> allStars = new HashMap<String, Actor>();
    		while(rs.next()) {
    			String name = rs.getString("name");
    			int birthYear = rs.getInt("birthYear");
    			Actor tempStar = new Actor();
    			tempStar.setName(name);
    			tempStar.setBirthYear(birthYear);
    			
    			allStars.put(name, tempStar);
    		}
    		
    		// use batch to optimize
    		dbCon.setAutoCommit(false);
            PreparedStatement preparedACBYStatement = null;
            PreparedStatement preparedACStatement = null;
            Iterator<Actor> it = actorList.iterator();
            
            String ACBYsql = "INSERT INTO stars (id, name, birthYear)"
        			+ " VALUES(?, ?, ?)";
            preparedACBYStatement = dbCon.prepareStatement(ACBYsql);
            
            String ACsql = "INSERT INTO stars (id, name)"
        			+ " VALUES(?, ?)";
            preparedACStatement = dbCon.prepareStatement(ACsql);
        	
            
            while (it.hasNext()) {
            	Actor ac = it.next();
            	int result;
            	
            	if (ac.getName().isEmpty()) {
            		continue;
            	}
            	
            	// check duplicate
            	if (allStars.containsKey(ac.getName())) {
            		//System.out.println("star name already exists: "+ ac.getName());
            		Actor tempStar = new Actor();
            		tempStar = allStars.get(ac.getName());
            		
            		if (tempStar.getBirthYear()==ac.getBirthYear()) {
            			System.out.println("Star:" + ac.getName() + " already exists");
            			continue;
            		}
            	}
            	
            	// insert data to database
            	if (ac.getBirthYear()!=0) {
	            	//String sql = "INSERT INTO stars (id, name, birthYear)"
	            	//		+ " VALUES(?, ?, ?)";
	            	
	            	//preparedACBYStatement = dbCon.prepareStatement(sql);
            		preparedACBYStatement.setString(1, "nm"+String.valueOf(star_id_int));
            		preparedACBYStatement.setString(2, ac.getName());
            		preparedACBYStatement.setInt(3, ac.getBirthYear());
            		preparedACBYStatement.addBatch();
	            	//result = preparedACBYStatement.executeUpdate(); 
            		
            	} 
            	else {
            		//String sql = "INSERT INTO stars (id, name)"
	            	//		+ " VALUES(?, ?)";
	            	
	            	//preparedACStatement = dbCon.prepareStatement(sql);
            		preparedACStatement.setString(1, "nm"+String.valueOf(star_id_int));
            		preparedACStatement.setString(2, ac.getName());
            		preparedACStatement.addBatch();
	            	
	            	//result = preparedACStatement.executeUpdate(); 
            	}
            	star_id_int += 1;
            	//System.out.println("Star: " + ac.getName());
            }
            preparedACBYStatement.executeBatch();
            preparedACStatement.executeBatch();
            dbCon.commit();
            
            System.out.println("Insert star data success!");
        } catch(Exception e) {
        	System.out.println("Something wrong!");
        }
        
        
        
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("actor")) {
            //create a new instance of movie
        	tempAct = new Actor();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("actor")) {
            //add it to the list
        	actorList.add(tempAct);

        } else if (qName.equalsIgnoreCase("stagename")) {
        	tempAct.setName(tempVal);
        	//System.out.println(tempVal);
        } else if (qName.equalsIgnoreCase("dob")) {
        	try {
        		tempAct.setBirthYear(Integer.parseInt(tempVal));
        	} catch(Exception e) {
        		
        	}
        }

    }

    public static void main(String[] args) throws SQLException {
    	ActorSAXParser spe = new ActorSAXParser();
        spe.runExample();
    }

}
