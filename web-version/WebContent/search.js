/**
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */



/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
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


//Get id from URL
let title = getParameterByName('title');
let year = getParameterByName('year');
let director = getParameterByName('director');
let starname = getParameterByName('starname');
let sort = getParameterByName('sort');
let order = getParameterByName('order');
let page = getParameterByName('page');
let numOfpage = getParameterByName('numOfpage');


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");
    
    let prevPageElement = jQuery("#prevPage");
    prevPageElement.empty();
    let prevPageHTML = '';
    prevPageHTML += '<input type="button" class="btn btn-info btn-xs" value="PrevPage" onclick="prevPage()"/>';

    prevPageElement.append(prevPageHTML);
    
    let nextPageElement = jQuery("#nextPage");
    nextPageElement.empty();
    let nextPageHTML = '';
    nextPageHTML += '<input type="button" class="btn btn-info btn-xs" value="NextPage" onclick="nextPage()"/>';
    
    nextPageElement.append(nextPageHTML);
    
    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    movieTableBodyElement.empty();

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {

        // Split stars name by , and store in array
        var stars = resultData[i]["s_starname"];
        var starList = new Array();
        starList = stars.split(",");
        
        // Split stars name by , and store in array
        var starsId = resultData[i]["s_id"];
        var starIdList = new Array();
        starIdList = starsId.split(",");
    	
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<td>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="singleMovie.html?movieId=' + resultData[i]['m_id'] + '">'
            + resultData[i]["m_title"] +     // display movie title for the link text
            '</a>' +
            "</td>";
        rowHTML += "<td>" + resultData[i]["m_year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["m_director"] + "</td>";
                
        rowHTML += "<td>";
        for (let j=0; j<starList.length-1; j++){
        	rowHTML += '<a href="singleStar.html?starId=' + starIdList[j] +'">';
        	rowHTML += starList[j];
        	rowHTML += '</a>';
        	rowHTML += ', ';
        }

        // add last genre with comma
        rowHTML += '<a href="singleStar.html?starId=' + starIdList[starIdList.length-1] +'">';
        rowHTML += starList[starList.length-1];
        rowHTML += '</a> </td>';
        
        rowHTML += "<td>" + resultData[i]["g_name"] + "</td>";
        rowHTML += "<td>" + resultData[i]["r_rating"] + "</td>";
        rowHTML += '<td> ' +
        		'<button id="addItem'+i+'" class="btn btn-info" type="submit" value="'+ resultData[i]['m_id'] +', '+ resultData[i]['m_title'] +'"> Add to Cart </button> ' +
        		'</td>';
        
        
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
        
        // trigger addItem function when click the addItem button
        jQuery("#addItem"+i).click((event) => addItem(event,i));       
    }
    
}


function prevPage(){
	
	page = (Number(page) - 1).toString();
	
	if (page==0){
		page = 1;
	}
	
	jQuery.get(
		"search",
		// Serialize the to the data sent by GET request
		{title: title,
		year: year,
		director: director,
		starname: starname,
		sort: sort,
		order: order,
		page: page,
		numOfpage: numOfpage},
		(resultData) => handleStarResult(resultData)
	);
	//location.reload();
}

function nextPage(){
	
	page = (Number(page) + 1).toString();
	
	jQuery.get(
		"search",
		// Serialize the to the data sent by GET request
		{title: title,
		year: year,
		director: director,
		starname: starname,
		sort: sort,
		order: order,
		page: page,
		numOfpage: numOfpage},
		(resultData) => handleStarResult(resultData)
	);
	//location.reload();
}

/**
 * Handle the data returned by AddItemServlet
 * @param resultDataString jsonObject
 */
/*
function handleAddItem(resultDataString) {
    // If login success, redirect to index.html page
    if (resultDataJson["status"] === "success") {
    	console.log("add item to session success");
    }
    // If login fail, display error message on <div> with id "login_error_message"
    else {
        console.log("show error message");
        //console.log(resultDataJson["message"]);
    }
}
*/

/**
 * Handle 'addItem' function
 * @param addCartEvent
 */

function addItem(addItemEvent,i){
    console.log("add item to cart");

    // Important: disable the default action of submitting the form
    //   which will cause the page to refresh
    //   see jQuery reference for details: https://api.jquery.com/submit/
    addItemEvent.preventDefault();
    
    // Split movie info by , and store in array
    var items = document.getElementById('addItem'+i).value;
    var itemList = new Array();
    itemList = items.split(",");
    
    var m_id = itemList[0];
    var m_title = itemList[1];

    //var item = document.getElementById('addItem'+i).value;
    
    jQuery.get(
        "addItem",
        // Serialize the to the data sent by GET request
        {m_id: m_id,
        m_title: m_title}
        );
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "search?page="+ page + "&numOfpage=" + numOfpage + "&sort=" + sort + "&order=" + order + "&title=" + title + "&year=" + year + "&director=" + director + "&starname=" + starname, // Setting request url, which is mapped by FormServlet in FormServlet.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});