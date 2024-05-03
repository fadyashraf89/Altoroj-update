package com.ibm.security.appscan.altoromutual.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.owasp.encoder.Encode;

public class SurveyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public SurveyServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String step = request.getParameter("step");

		if (step == null || step.isEmpty()) {
			// Redirect to the start page if the step parameter is missing
			response.sendRedirect(request.getContextPath() + "/survey_questions.jsp?step=start");
			return;
		}

		String content = null;
		String previousStep = null;

		// Define the survey steps
		switch (step) {
			case "start":
				content = "<h1>Welcome</h1>" +
						"<div width=\"99%\"><p>If you complete this survey, you have an opportunity to win a smartphone. Would you like to continue?<br /><ul><li><a href=\"survey_questions.jsp?step=a\">Yes</a></li><li><a href=\"survey_questions.jsp?step=a\">No</a></li></ul></p></div>";
				break;
			case "a":
				content = "<h1>Question 1</h1>" +
						"<div width=\"99%\"><p>Which of the following groups includes your age?<ul><li><a href=\"survey_questions.jsp?step=b\">13 years or less</a></li><li><a href=\"survey_questions.jsp?step=b\">14-17</a></li><li><a href=\"survey_questions.jsp?step=b\">18-24</a></li><li><a href=\"survey_questions.jsp?step=b\">25-34</a></li><li><a href=\"survey_questions.jsp?step=b\">35-44</a></li><li><a href=\"survey_questions.jsp?step=b\">45-54</a></li><li><a href=\"survey_questions.jsp?step=b\">55-64</a></li><li><a href=\"survey_questions.jsp?step=b\">65-74</a></li><li><a href=\"survey_questions.jsp?step=b\">75+</a></li></ul></p></div>";
				previousStep = "start";
				break;
			case "b":
				content = "<h1>Question 2</h1>" +
						"<div width=\"99%\"><p>Have you bookmarked our website?<ul><li><a href=\"survey_questions.jsp?step=c\">Yes</a></li><li><a href=\"survey_questions.jsp?step=c\">No</a></li></ul></p></div>";
				previousStep = "a";
				break;
			case "c":
				content = "<h1>Question 3</h1>" +
						"<div width=\"99%\"><p>Are you... <ul><li><a href=\"survey_questions.jsp?step=d\">Male</a></li><li><a href=\"survey_questions.jsp?step=d\">Female</a></li></ul></p>";
				previousStep = "b";
				break;
			case "d":
				content = "<h1>Question 4</h1>" +
						"<div width=\"99%\"><p>Are you impressed with our new design?<ul><li><a href=\"survey_questions.jsp?step=email\">Yes</a></li><li><a href=\"survey_questions.jsp?step=email\">No</a></li></ul></p>";
				previousStep = "c";
				break;
			case "email":
				content = "<h1>Thanks</h1>" +
						"<div width=\"99%\"><p>Thank you for completing our survey. We are always working to improve our status in the eyes of our most important client: YOU. Please enter your email below, and we will notify you soon about your winning status. Thank you.</p><form method=\"get\" action=\"survey_questions.jsp?step=done\"><div style=\"padding-left:30px;\"><input type=\"hidden\" name=\"step\" value=\"done\"/><input type=\"text\" name=\"txtEmail\" style=\"width:200px;\" /> <input type=\"submit\" value=\"Submit\" style=\"width:100px;\" /></div></form></div>";
				previousStep = "d";
				break;
			case "done":
				String email = request.getParameter("txtEmail");
				if (email != null && !email.isEmpty()) {
					// Process the submitted email
					content = "<h1>Thanks</h1>" +
							"<div width=\"99%\"><p>Thanks for your entry. We will contact you shortly at:<br /><br /> <b>" + Encode.forHtml(email) + "</b></p></div>";
				} else {
					// Handle case where email parameter is missing or empty
					content = "<h1>Error</h1>" +
							"<div width=\"99%\"><p>Please provide a valid email address.</p></div>";
					previousStep = "email";
				}
				break;
			default:
				// Handle invalid step parameter
				content = "<h1>Error</h1>" +
						"<div width=\"99%\"><p>Invalid survey step.</p></div>";
		}

		// Set the session attribute to track the survey step
		request.getSession().setAttribute("surveyStep", step);

		// Check for request manipulation or out-of-order navigation
		String referrer = request.getHeader("Referer");
		String allowedReferrer = request.getContextPath() + "/survey_questions.jsp?step=" + previousStep;
		if (previousStep != null && (referrer == null || !referrer.endsWith(allowedReferrer))) {
			content = "<h1>Error</h1>" +
					"<div width=\"99%\"><p>It appears that you attempted to skip or repeat some areas of this survey. Please <a href=\"survey_questions.jsp?step=start\">return to the start page</a> to begin again.</p></div>";
		}

		response.setContentType("text/html");
		response.getWriter().write(Encode.forHtml(content));
	}
}
