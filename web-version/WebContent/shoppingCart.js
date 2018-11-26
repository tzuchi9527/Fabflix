function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleShopResult(resultData) {
    console.log("handleStarResult: populating shopping table from resultData");
    
    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let shoppingTableBodyElement = jQuery("#shopping_body_table");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + resultData[i]["m_title"] + "</td>";
        // change quantity
        rowHTML += "<td> ";
        rowHTML += '<button id="plus'+i+'" type="submit" value="'+ resultData[i]['m_id'] +', '+ resultData[i]['m_quantity'] +'"> + </button> ';
        rowHTML += '</td>';
        
        rowHTML += '"<td>' + resultData[i]["m_quantity"] + '</td>"';
        
        rowHTML += "<td> ";
        rowHTML += '<button id="minus'+i+'" type="submit" value="'+ resultData[i]['m_id'] +', '+ resultData[i]['m_quantity'] +'"> - </button> ';
        rowHTML += '</td>';
                            
        // Delete button
        rowHTML += '<td> ' +
		'<button id="delItem'+i+'" type="submit" value="'+ resultData[i]['m_id'] +'"> Delete </button> ' +
		'</td>';
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        shoppingTableBodyElement.append(rowHTML);
        
        jQuery("#minus"+i).click((event) => minusQuan(event,i));  
        jQuery("#plus"+i).click((event) => plusQuan(event,i));  
        jQuery("#delItem"+i).click((event) => delItem(event,i));     
    }
    
    
    // checkout
    let checkoutElement = jQuery("#checkout");
    
}

/**
 * Handle 'minusQuan' function
 * @param minusItemEvent
 */

function minusQuan(minusItemEvent,i){
    console.log("change quantity of item in cart");

    // Important: disable the default action of submitting the form
    //   which will cause the page to refresh
    //   see jQuery reference for details: https://api.jquery.com/submit/
    minusItemEvent.preventDefault();
    
    // Split movie info by , and store in array
    var items = document.getElementById('minus'+i).value;
    var itemList = new Array();
    itemList = items.split(",");
    
    var m_id = itemList[0];
    var m_quantity = Number(itemList[1]);
    
    m_quantity -= 1;
    if (m_quantity!=0){
	    jQuery.get(
	        "shoppingCartBuy",
	        // Serialize the to the data sent by GET request
	        {m_id: m_id,
	        m_quantity: m_quantity});
	    // refresh the page
	    location.reload();
    }
    else{
    	jQuery.get(
    		"shoppingCartDel",
    		// Serialize the to the data sent by GET request
    		{m_id: m_id});
    	    // refresh the page
    	location.reload();
    }
}

/**
 * Handle 'plusQuan' function
 * @param plusItemEvent
 */

function plusQuan(plusItemEvent,i){
    console.log("change quantity of item in cart");

    // Important: disable the default action of submitting the form
    //   which will cause the page to refresh
    //   see jQuery reference for details: https://api.jquery.com/submit/
    plusItemEvent.preventDefault();
    
    // Split movie info by , and store in array
    var items = document.getElementById('minus'+i).value;
    var itemList = new Array();
    itemList = items.split(",");
    
    var m_id = itemList[0];
    var m_quantity = Number(itemList[1]);
    
    m_quantity += 1;
    
    jQuery.get(
        "shoppingCartBuy",
        // Serialize the to the data sent by GET request
        {m_id: m_id,
        m_quantity: m_quantity});
    // refresh the page
    location.reload();
}

/**
 * Handle 'delItem' function
 * @param delItemEvent
 */

function delItem(delItemEvent,i){
    console.log("delete item from cart");

    // Important: disable the default action of submitting the form
    //   which will cause the page to refresh
    //   see jQuery reference for details: https://api.jquery.com/submit/
    delItemEvent.preventDefault();
    
    // Split movie info by , and store in array
    var m_id = document.getElementById('delItem'+i).value;
    
    jQuery.get(
        "shoppingCartDel",
        // Serialize the to the data sent by GET request
        {m_id: m_id});
    // refresh the page
    location.reload();
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "shoppingCart", // Setting request url, which is mapped by FormServlet in FormServlet.java
    success: (resultData) => handleShopResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});
