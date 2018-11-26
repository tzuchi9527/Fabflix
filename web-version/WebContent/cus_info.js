/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    resultDataJson = JSON.parse(resultDataString);

    console.log("handle info response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login success, redirect to success_for_check_info.html page
    if (resultDataJson["status"] === "success") {
        window.location.replace("confirm.html");
    	console.log("success");
    }
    // If the info is wrong, display error message on <div> with id "info_error_message"
    else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        jQuery("#info_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit info form");

    // Important: disable the default action of submitting the form
    //   which will cause the page to refresh
    //   see jQuery reference for details: https://api.jquery.com/submit/
    formSubmitEvent.preventDefault();

    jQuery.post(
        "api/info",
        // Serialize the login form to the data sent by POST request
        jQuery("#info_form").serialize(),
        (resultDataString) => handleLoginResult(resultDataString));

}

// Bind the submit action of the form to a handler function
jQuery("#info_form").submit((event) => submitLoginForm(event));