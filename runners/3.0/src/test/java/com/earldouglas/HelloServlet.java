package com.earldouglas;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

public class HelloServlet extends HttpServlet {

  @Override
  protected void doGet(
      final HttpServletRequest request,
      final HttpServletResponse response
  ) throws IOException {
    response.setContentType("text/plain");
    response.getWriter().println("Hello, world!");
  }
}
