
package com.sbank.netbanking.routing;

import com.sbank.netbanking.interfaces.HandlerInterface;

public class Route {
    private final String method;
    private final String path;
    private final HandlerInterface handler;

 

	public Route(String method, String path, HandlerInterface handler) {
        this.method = method;
        this.path = path;
        this.handler = handler;
    }

    public boolean matches(String method, String path) {
        return this.method.equalsIgnoreCase(method) && this.path.equalsIgnoreCase(path);
    }
    
    
    public HandlerInterface getHandler() {
 		return handler;
 	}
    


}
