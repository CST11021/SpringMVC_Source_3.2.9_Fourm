package com.whz.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;

// 注意：servlet规范中3.0以上版本才支持注解，并且Tomcat需要7以上版本
@WebServlet(name = "MyServlet", urlPatterns = { "/my" })
public class MyServlet implements Servlet {
    
    private transient ServletConfig servletConfig;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        this.servletConfig = servletConfig;
    }
    
    @Override
    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    @Override
    public String getServletInfo() {
        return "My Servlet";
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        String servletName = servletConfig.getServletName();
        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        writer.print(
                "<html>" +
                    "<head></head>" +
                    "<body>" + "Hello from " + servletName + "</body>" +
                "</html>");
    }

    @Override
    public void destroy() {
    }
}