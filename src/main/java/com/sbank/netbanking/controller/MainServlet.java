package com.sbank.netbanking.controller;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    //POST
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            {
        try {
			requestRouter.route(request, response, "POST");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    //PUT
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            {
        try {
			requestRouter.route(request, response, "PUT");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    //DELETE
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
             {
        try {
			requestRouter.route(request, response, "DELETE");
		} catch ( Exception  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

