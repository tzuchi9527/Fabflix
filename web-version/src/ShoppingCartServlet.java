import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;

/**
 * This servlet only display shopping cart info
 */
@WebServlet(name = "shoppingCart", urlPatterns = "/shoppingCart")
public class ShoppingCartServlet extends HttpServlet {
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
        
        // 
        synchronized(previousItems) {
        	
        	JsonArray jsonArray = new JsonArray();
        	
        	for (Object item:previousItems.keySet()) {
        		String[] values = previousItems.get(item);
        		String m_id = (String) item;
        		String m_title = values[0];
        		String m_quantity = values[1];
        		
        		// 
        		JsonObject jsonObject = new JsonObject();
        		jsonObject.addProperty("m_id", m_id);
        		jsonObject.addProperty("m_title", m_title);
        		jsonObject.addProperty("m_quantity", m_quantity);
        		
        		jsonArray.add(jsonObject);
        	}
        	
            // write JSON string to output
        	response.getWriter().write(jsonArray.toString());            
            // set response status to 200 (OK)
            response.setStatus(200);
        }

    }
}
