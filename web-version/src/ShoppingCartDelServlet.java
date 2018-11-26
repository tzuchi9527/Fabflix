import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;

@WebServlet(name = "shoppingCartDel", urlPatterns = "/shoppingCartDel")
public class ShoppingCartDelServlet extends HttpServlet {
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
        
        // remove the item
        synchronized (previousItems) {       		
        	if (previousItems.containsKey(m_id)) {
        		previousItems.remove(m_id);
        	}
        }
            
    }
}
