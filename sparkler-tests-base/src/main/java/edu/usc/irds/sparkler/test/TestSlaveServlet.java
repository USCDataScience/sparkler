package edu.usc.irds.sparkler.test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This servlet acts as a slave to test cases by doing whatever the action they request.
 * @author Thamme Gowda
 *
 */
public class TestSlaveServlet extends HttpServlet {

    private static Map<String, Class<? extends TestAction>> actionsRegistry = new HashMap<>();
    static {
        actionsRegistry.put("read-timeout", ReadTimeoutAction.class);
        actionsRegistry.put(null, DefaultAction.class);
        actionsRegistry.put("", DefaultAction.class);
    }

    interface TestAction {
        void run(HttpServletRequest req, HttpServletResponse resp) throws IOException;
    }

    public static class DefaultAction implements TestAction{
        @Override
        public void run(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setContentType("text/html");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println("<h1>This is default action.</h1> <br/>");
            resp.getWriter().println("<p>Available actions: "+ actionsRegistry.keySet()
                    + ". </br>. use '?action=' as query parameter</p> ");
        }
    }

    /**
     * This action causes read timeout. Query parameter to be used to specify value is 'timeout'
     */
    public static class ReadTimeoutAction implements TestAction {

        @Override
        public void run(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setContentType("text/html");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println("<h1>Read Timeout</h1> <br/>");
            long timeout = 30000;
            String value = req.getParameter("timeout");
            if (value != null){
                timeout = Long.parseLong(value);
            }
            resp.getWriter().println("<h2>Starting to wait : "+ timeout +"ms </h2> <br/>");
            resp.getWriter().flush();
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            resp.getWriter().println("<h2>Done</h2> <br/>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            actionsRegistry.get(req.getParameter("action"))
                    .newInstance()
                    .run(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
