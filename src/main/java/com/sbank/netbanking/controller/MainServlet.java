package com.sbank.netbanking.controller;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sbank.netbanking.dto.ErrorResponse;
import com.sbank.netbanking.exceptions.TaskException;
import com.sbank.netbanking.util.ErrorResponseUtil;

@WebServlet("/*")
public class MainServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    //GET
    RequestRouter requestRouter = new RequestRouter();
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
             {
        try {
  


			requestRouter.route(request, response, "GET");
		} catch (Exception e) {
	        e.printStackTrace();

	        try {
	            Throwable rootCause = e;
	            while (rootCause.getCause() != null) {
	                rootCause = rootCause.getCause();
	            }

	            String detailedMessage = " Caused by: " + rootCause.getClass().getName() + ": " + rootCause.getMessage();

	            ErrorResponseUtil.send(response, 400,
	                new ErrorResponse(detailedMessage, 400, e.getMessage()));
	        } catch (TaskException e1) {
	            e1.printStackTrace();
	        }
			}
        
    }

    //POST
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            {
        try {
			requestRouter.route(request, response, "POST");
		} catch (Exception e) {
        e.printStackTrace();

        try {
            Throwable rootCause = e;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }

            String detailedMessage = " Caused by: " + rootCause.getClass().getName() + ": " + rootCause.getMessage();

            ErrorResponseUtil.send(response, 400,
                new ErrorResponse(detailedMessage, 400, e.getMessage()));
        } catch (TaskException e1) {
            e1.printStackTrace();
        }
		}
    }

    //PUT
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            {
        try {
			requestRouter.route(request, response, "PUT");
		} catch (Exception e){
	        e.printStackTrace();

	        try {
	            Throwable rootCause = e;
	            while (rootCause.getCause() != null) {
	                rootCause = rootCause.getCause();
	            }

	            String detailedMessage = " Caused by: " + rootCause.getClass().getName() + ": " + rootCause.getMessage();

	            ErrorResponseUtil.send(response, 400,
	                new ErrorResponse(detailedMessage, 400, e.getMessage()));
	        } catch (TaskException e1) {
	            e1.printStackTrace();
	        }
			}
    }
    
    //DELETE
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
             {
        try {
			requestRouter.route(request, response, "DELETE");
		} catch ( Exception  e) {
	        e.printStackTrace();

	        try {
	            Throwable rootCause = e;
	            while (rootCause.getCause() != null) {
	                rootCause = rootCause.getCause();
	            }

	            String detailedMessage = " Caused by: " + rootCause.getClass().getName() + ": " + rootCause.getMessage();

	            ErrorResponseUtil.send(response, 400,
	                new ErrorResponse(detailedMessage, 400, e.getMessage()));
	        } catch (TaskException e1) {
	            e1.printStackTrace();
	        }
			}
    }
}

