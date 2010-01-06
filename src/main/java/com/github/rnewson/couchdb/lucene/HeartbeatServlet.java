package com.github.rnewson.couchdb.lucene;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Heartbeat
 * 
 * @author sakrafd
 * 
 */
public class HeartbeatServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final Writer writer = resp.getWriter();
        try {
            writer.write("OK");
        } finally {
            writer.close();
        }
    }
}
