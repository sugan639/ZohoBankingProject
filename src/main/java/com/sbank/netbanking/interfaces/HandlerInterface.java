

package com.sbank.netbanking.interfaces;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface HandlerInterface {
    void handle(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
