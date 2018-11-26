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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

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
        //rowHTML += "<td>" + resultData[i]["m_title"] + "</td>";
        rowHTML +=
            "<td>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="singleMovie.html?movieId=' + resultData[i]['m_id'] + '">'
            + resultData[i]["m_title"] +     // display movie title for the link text
            '</a>' +
            "</td>";
        rowHTML += "<td>" + resultData[i]["m_year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["m_director"] + "</td>";
        //rowHTML += "<td>" + resultData[i]["s_starname"] + "</td>";
        
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
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
    
    // check out button
    let checkOutButton = jQuery("#checkout");
    let buttonHTML = "";
    buttonHTML += '<button onclick="location.href=shoppingCart.html" type="button"> Check Out </button>';
    checkOutButton.append(buttonHTML);

}


//Get id from URL
let genre = getParameterByName('genre');
console.log(genre);

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "browseGenre?genre=" + genre, // Setting request url, which is mapped by FormServlet in FormServlet.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});
