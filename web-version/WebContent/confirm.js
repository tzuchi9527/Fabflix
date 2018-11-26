/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    //resultDataJson = JSON.parse(resultDataString);

    console.log("handle confirm response");
    //console.log(resultDataJson);
    console.log(resultDataString["status"]);

    // If login success, redirect to success_for_check_info.html page
    if (resultDataString["status"] === "success") {
        //window.location.replace("confirm.html");
    	console.log("success");
    	jQuery("#confirm_error_message").text(resultDataString["message"]);
    }
    // If the info is wrong, display error message on <div> with id "info_error_message"
    else {
        console.log("show error message");
        console.log(resultDataString["message"]);
        jQuery("#confirm_error_message").text(resultDataString["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */


// Bind the submit action of the form to a handler function
//jQuery("#confirm_form").submit((event) => submitLoginForm(event));

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "POST", // Setting request method
    url: "api/confirm", // Setting request url, which is mapped by FormServlet in FormServlet.java
    success: (resultData) => handleLoginResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});