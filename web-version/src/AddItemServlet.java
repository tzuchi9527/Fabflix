import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;

@WebServlet(name = "AddItemServlet", urlPatterns = "/addItem")
public class AddItemServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	
        HttpSession session = request.getSession(); // Get a instance of current session on the request
        HashMap<String, String[]> previousItems = (HashMap<String, String[]>) session.getAttribute("previousItems");
        
        // If "previousItems" is not found on session, means this is a new user, thus we create a new previousItems ArrayList for the user
        if (previousItems == null) {
            previousItems = new HashMap<String, String[]>();
            session.setAttribute("previousItems", previousItems); // Add the newly created ArrayList to session, so that it could be retrieved next time
        }        

        String m_id = request.getParameter("m_id"); 
        String m_title = request.getParameter("m_title");
        		
        // In order to prevent multiple clients, requests from altering previousItems ArrayList at the same time, we lock the ArrayList while updating   	
        synchronized (previousItems) {       		
        		
        	// if item does not exist, set number to 1
        	// if item exists, add one to previous number
        	if (!previousItems.containsKey(m_id)) {
        		String[] values = {m_title, "1"};
        		previousItems.put(m_id, values);
            }else {
                String[] values = previousItems.get(m_id);
                int num = Integer.parseInt(values[1]);
               	num += 1;
               	String[] update = {m_title, String.valueOf(num)};
                previousItems.put(m_id, update);
                System.out.println(num);
       		}
        	
       		JsonObject responseJsonObject = new JsonObject();
    		responseJsonObject.addProperty("status", "success");
    		response.getWriter().write(responseJsonObject.toString());                

        }
        
        System.out.println(m_id);
        System.out.println(m_title);
    }
}