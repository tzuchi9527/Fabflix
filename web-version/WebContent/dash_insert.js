/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleStarFinishResult(resultDataString) {
    resultDataJson = JSON.parse(resultDataString);

    console.log("handle addstar finish response");
    console.log(resultDataJson);

    // If login success, redirect to success_for_check_info.html page
    if (resultDataJson["status"] === "success") {
        //window.location.replace("confirm.html");
    	console.log("success");
    	jQuery("#add_star_message").text(resultDataJson["message"]);
    }
    // If the info is wrong, display error message on <div> with id "info_error_message"
    else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        jQuery("#add_star_message").text(resultDataJson["message"]);
    }
}
function handleMovieFinishResult(resultDataString) {
    resultDataJson = JSON.parse(resultDataString);

    console.log("handle addmovie finish response");
    console.log(resultDataJson);

    // If login success, redirect to success_for_check_info.html page
    if (resultDataJson["status"] === "success") {
        //window.location.replace("confirm.html");
    	console.log("success");
    	jQuery("#add_movie_message").text(resultDataJson["message"]);
    }
    // If the info is wrong, display error message on <div> with id "info_error_message"
    else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        jQuery("#add_movie_message").text(resultDataJson["message"]);
    }
}


function handleStarResult(resultDataString) {
	resultDataString.preventDefault();
	
    console.log("handle add star response");

    var starname = document.getElementsByName("starname")[0].value;
    var year = document.getElementsByName("year")[0].value;

    jQuery.get(
            "api/dash_addStar",
            {starname: starname,
            year: year},
            (resultData) => handleStarFinishResult(resultData)
            );    
    console.log("success");  
}

function handleMovieResult(resultDataString) {
	resultDataString.preventDefault();
	
    console.log("handle add movie response");

    var title = document.getElementsByName("title")[0].value;
    var director = document.getElementsByName("director")[0].value;
    var star = document.getElementsByName("star")[0].value;
    var genre = document.getElementsByName("genre")[0].value;
    var year = document.getElementsByName("year1")[0].value;
    console.log(year);
    jQuery.get(
            "api/dash_addMovie",
            {title: title,
            year: year,
            director: director,
            star: star,
            genre: genre},
            (resultData) => handleMovieFinishResult(resultData)
            );    
    console.log("success");  
}



// Bind the submit action of the form to a handler function
jQuery("#add_star").submit((event) => handleStarResult(event));
jQuery("#add_movie").submit((event) => handleMovieResult(event));