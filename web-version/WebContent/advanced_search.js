/*
 * Search and auto-complete suggestion
 */

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleSearchResult(resultDataString) {
	resultDataString.preventDefault();
	
    console.log("handle search response in main_search");

    var title = document.getElementsByName("title")[0].value;
    var year = document.getElementsByName("year")[0].value;
    var director = document.getElementsByName("director")[0].value;
    var starname = document.getElementsByName("starname")[0].value;
    var sort = document.getElementsByName("sort")[0].value;
    var order = document.getElementsByName("order")[0].value;
    var page = 1;
    var numOfpage = 10;
    
    window.location.replace("search.html?page="+ page + "&numOfpage=" + numOfpage+ "&sort="+sort+"&order="+order+"&title="+title+"&year="+year+"&director="+director+"&starname="+starname);
    console.log("success");
}

jQuery("#search_form").submit((event) => handleSearchResult(event));


/*
 * This Javascript code uses this library: https://github.com/devbridge/jQuery-Autocomplete
 * 
 * To read this code, start from the line "$('#autocomplete').autocomplete" and follow the callback functions.
 * 
 */


/*
 * This function is called by the library when it needs to lookup a query.
 * 
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */

// for cached result
var cached = [];

function handleLookup(query, doneCallback) {
	console.log("autocomplete initiated")
	
	let result = cached.find(o => o.query===query);
	// if you want to check past query results first, you can do it here
	if(typeof result!="undefined"){
		console.log("Query from cached result");
		console.log(result.jsonResult);
		doneCallback( { suggestions: result.jsonResult } );
	}
	else{
		// sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
		// with the query data
		console.log("sending AJAX request to backend Java Servlet")
		jQuery.ajax({
			"method": "GET",
			// generate the request url from the query.
			// escape the query string to avoid errors caused by special characters 
			"url": "auto-suggestion?query=" + escape(query),
			"success": function(data) {
				// pass the data, query, and doneCallback function into the success handler
				handleLookupAjaxSuccess(data, query, doneCallback) 
			},
			"error": function(errorData) {
				console.log("lookup ajax error")
				console.log(errorData)
			}
		})
	}
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 * 
 * data is the JSON data string you get from your Java Servlet
 * 
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
	console.log("lookup ajax successful")
	
	// parse the string into JSON
	var jsonData = JSON.parse(data);
	console.log(jsonData)
	
	// if you want to cache the result into a global variable you can do it here
	cached.push({query:query,jsonResult:jsonData});
	
	// call the callback function provided by the autocomplete library
	// add "{suggestions: jsonData}" to satisfy the library response format according to
	//   the "Response Format" section in documentation
	doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function. 
 * When a suggestion is selected, this function is called by the library.
 * 
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
	// jump to the specific result page based on the selected suggestion
	
	console.log("you select " + suggestion["title"])
	var url = "singleMovie.html?movieId=" + suggestion["data"]["movieId"]
	console.log(url)
	window.location.replace(url);
}


/*
 * This statement binds the autocomplete library with the input box element and 
 *   sets necessary parameters of the library.
 * 
 * The library documentation can be find here: 
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 * 
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
	// documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
    		handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
    		handleSelectSuggestion(suggestion)
    },
    // set the groupby name in the response json data field
    groupBy: "category",
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // add other parameters, such as minimum characters
    minChars: 3
});


/*
 * do normal full text search if no suggestion is selected 
 */
function handleNormalSearch(query) {
	console.log("doing normal search with query: " + query);
	// TODO: you should do normal search here
}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
	// keyCode 13 is the enter key
	if (event.keyCode == 13) {
		// pass the value of the input box to the handler function
		handleNormalSearch($('#autocomplete').val())
	}
})

// TODO: if you have a "search" button, you may want to bind the onClick event as well of that button


