var genres = [];
function handleDisplayGenreResult(resultData) {
	console.log("handleDisplayGenreResult: parse each genre from resultData");
	for (let i=0; i<resultData.length; i++){
		genres.push(resultData[i]);
	}	
	let browseGenreForm = jQuery("#browse_genre");
	let genresFirst = '';
	genresFirst += '<h3>Choose the genre of movies: </h3><br>';
	genresFirst += '<div class="btn-group btn-group-sm">';
	browseGenreForm.append(genresFirst);
	for (let i=0;i<genres.length;i++){
		let genresHTML = '';
		genresHTML += '<button class="btn btn-info" id="genre'+i+'" type="submit" value="'+genres[i]+'">';
		genresHTML += genres[i];
		genresHTML += '</button>';
		
		browseGenreForm.append(genresHTML);
		
		jQuery("#genre"+i).click((event) => handleBrowseGenreResult(event,i));
	}
	let div = '';
	div += '</div>';
	browseGenreForm.append(div);
	
}

let browseNameForm = jQuery("#browse_name");
let namesFirst = '';
namesFirst += '<h3>Choose the name of movies:</h3> <br>';
namesFirst += '<div class="btn-group btn-group-sm">';
browseNameForm.append(namesFirst);
for (let i=65;i<=90;i++){
	let namesHTML = '';
	namesHTML += '<button class="btn btn-info" id="names'+i+'" type="submit" value="'+String.fromCharCode(i)+'">';
	namesHTML += String.fromCharCode(i);
	namesHTML += '</button>';
	
	browseNameForm.append(namesHTML);

	jQuery("#names"+i).click((event) => handleBrowseNameResult(event,i));
}
let div = '';
div += '</div>';
browseNameForm.append(div);

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
/*
function handleSearchResult(resultDataString) {
	resultDataString.preventDefault();
	
    console.log("handle search response");

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
*/
function handleBrowseGenreResult(resultDataString,i) {
	resultDataString.preventDefault();
	
    console.log("handle browse genre response");
    
    var genre = document.getElementById("genre"+i).value;
    
    window.location.replace("browseGenre.html?genre="+genre);
    console.log("success");
}

function handleBrowseNameResult(resultDataString,i) {
	resultDataString.preventDefault();
	
    console.log("handle browse name response");

    var name = document.getElementById("names"+i).value;
    
    window.location.replace("browseName.html?name="+name);
    console.log("success");
}

// Bind the submit action of the form to a handler function
//jQuery("#search_form").submit((event) => handleSearchResult(event));
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "./displayGenre", // Setting request url
    success: (resultData) => handleDisplayGenreResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});