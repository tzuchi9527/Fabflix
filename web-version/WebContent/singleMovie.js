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

function handleResult(resultData) {

    console.log("handleResult: populating movie info from resultData");

    // populate the movie info h3
    // find the empty h3 body by id "movie_info"
    let movieInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<p>Movie Title: " + resultData[0]["m_title"] + "</p>" +
        "<p>Year: " + resultData[0]["m_year"] + "</p>" +
        "<p>Director: " + resultData[0]["m_director"] + "</p>");

    console.log("handleResult: populating genre and stars table from resultData");

    // Populate the genres table h3
    // Find the empty table body by id "genres_info"
    let genresInfoElement = jQuery("#genres_info");

    // Split genres by , and store in array
    var genres = resultData[0]["g_name"];
    var genreList = new Array();
    genreList = genres.split(",");
    
    // Concatenate the html tags with resultData jsonObject to create table rows
    let rowGenres = "";
    rowGenres += "<p>Genres: ";
    for (let i=0; i < genreList.length-1; i++){
    	rowGenres += '<a href="browseGenre.html?genre=' + genreList[i] +'">';
    	rowGenres += genreList[i];
    	rowGenres += '</a>';
    	rowGenres += ', ';
    }

    // add last genre with comma
    rowGenres += '<a href="browseGenre.html?genre=' + genreList[genreList.length-1] +'">';
    rowGenres += genreList[genreList.length-1];
    rowGenres += '</a>';
    rowGenres += "</p>";
    
    // Append the row created to the table body, which will refresh the page
    genresInfoElement.append(rowGenres);
    
    // Populate the stars table h3
    // Find the empty table body by id "stars_info"
    let starsInfoElement = jQuery("#stars_info");
    
    // Split stars name by , and store in array
    var stars = resultData[0]["s_starname"];
    var starList = new Array();
    starList = stars.split(",");
    
    // Split stars name by , and store in array
    var starsId = resultData[0]["s_id"];
    var starIdList = new Array();
    starIdList = starsId.split(",");
    

    // Concatenate the html tags with resultData jsonObject to create table rows
    let rowStars = "";
    rowStars += "<p>Stars: ";
    for (let i=0; i < starList.length-1; i++){
    	rowStars += '<a href="singleStar.html?starId=' + starIdList[i] +'">';
    	rowStars += starList[i];
    	rowStars += '</a>';
    	rowStars += ', ';
    }

    // add last genre with comma
    rowStars += '<a href="singleStar.html?starId=' + starIdList[starIdList.length-1] +'">';
    rowStars += starList[starList.length-1];
    rowStars += '</a>';
    rowStars += "</p>";

    // Append the row created to the table body, which will refresh the page
    starsInfoElement.append(rowStars);
    
    // Populate the stars table h3
    // Find the empty table body by id "movie_table_body"
    let addItemQuery = jQuery("#add_item");
    
    let addItemHTML = "";
    addItemHTML += '<br><p class="text-center">';
    addItemHTML += '<button id="addItem" class="btn btn-info" type="submit" value="'+ resultData[0]["m_id"] + ', ' + resultData[0]["m_title"] + ' "> Add to Cart </button> ';
    addItemHTML += '</p>';
    
    addItemQuery.append(addItemHTML);
    
    // check out button
    /*
    let checkOutButton = jQuery("#checkout");
    let buttonHTML = "";
    buttonHTML += '<button onclick="location.href=shoppingCart.html" type="button"> Check Out </button>';
    checkOutButton.append(buttonHTML);*/

    
    jQuery("#addItem").click((event) => addItem(event)); 
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

function addItem(addItemEvent){
    console.log("add item to cart");

    // Important: disable the default action of submitting the form
    //   which will cause the page to refresh
    //   see jQuery reference for details: https://api.jquery.com/submit/
    addItemEvent.preventDefault();
    
    // Split movie info by , and store in array
    var items = document.getElementById('addItem').value;
    var itemList = new Array();
    itemList = items.split(",");
    
    var m_id = itemList[0];
    var m_title = itemList[1];
    
    //var item = document.getElementById('addItem').value;
    
    jQuery.get(
        "addItem",
        // Serialize the to the data sent by GET request
        {m_id: m_id,
        m_title: m_title}
        );
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('movieId');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "singleMovie?movieId=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});