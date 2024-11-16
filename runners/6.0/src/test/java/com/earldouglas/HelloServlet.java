package com.earldouglas;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
