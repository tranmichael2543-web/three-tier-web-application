package project3;



import java.io.*;
import java.sql.*;
import java.util.Properties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class ClientUserServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String sqlCommand = request.getParameter("sqlcommand");

        // Load client properties
        Properties props = new Properties();
        String propsPath = getServletContext().getRealPath("/WEB-INF/conf/client.properties");
        try (FileInputStream fis = new FileInputStream(propsPath)) {
            props.load(fis);
        }

        String dbUrl = props.getProperty("MYSQL_DB_URL");
        String dbUser = props.getProperty("MYSQL_DB_USERNAME");
        String dbPass = props.getProperty("MYSQL_DB_PASSWORD");

        // Build the HTML response page
        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Client User - Results</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; background-color: #1a1a2e; color: #e0e0e0; margin: 0; padding: 20px; }");
        out.println(".container { max-width: 950px; margin: 0 auto; background-color: #16213e; padding: 30px; border-radius: 10px; }");
        out.println("h2 { color: #4ec9b0; text-align: center; }");
        out.println("table { border-collapse: collapse; width: 100%; margin-top: 15px; }");
        out.println("th { background-color: #4ec9b0; color: #1a1a2e; padding: 10px; text-align: left; }");
        out.println("td { padding: 8px 10px; border-bottom: 1px solid #333; }");
        out.println("tr:nth-child(even) { background-color: #0f3460; }");
        out.println("tr:hover { background-color: #1a4a7a; }");
        out.println(".command-box { background-color: #0f3460; padding: 15px; border-radius: 5px; margin-bottom: 15px; font-family: monospace; word-wrap: break-word; }");
        out.println(".success { color: #00ff88; font-weight: bold; }");
        out.println(".error { color: #ff4444; font-weight: bold; }");
        out.println(".denied { color: #ff8800; font-weight: bold; font-size: 16px; }");
        out.println("a { color: #4ec9b0; text-decoration: none; }");
        out.println("a:hover { text-decoration: underline; }");
        out.println("</style></head><body>");
        out.println("<div class=\"container\">");
        out.println("<h2>Client-Level User Results</h2>");
        out.println("<div class=\"command-box\"><b>SQL Command:</b><br>" + escapeHtml(sqlCommand) + "</div>");

        // Client can only execute SELECT statements
        String trimmedCommand = sqlCommand.trim().toLowerCase();
        if (!trimmedCommand.startsWith("select")) {
            out.println("<p class=\"denied\">ERROR: You are not authorized to execute this command.<br>");
            out.println("Client-level users can only execute SELECT (query) statements.</p>");
        } else {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");

                try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sqlCommand);
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int numCols = rsmd.getColumnCount();

                    out.println("<table>");
                    out.println("<tr>");
                    for (int i = 1; i <= numCols; i++) {
                        out.println("<th>" + escapeHtml(rsmd.getColumnLabel(i)) + "</th>");
                    }
                    out.println("</tr>");

                    int rowCount = 0;
                    while (rs.next()) {
                        out.println("<tr>");
                        for (int i = 1; i <= numCols; i++) {
                            out.println("<td>" + escapeHtml(rs.getString(i)) + "</td>");
                        }
                        out.println("</tr>");
                        rowCount++;
                    }
                    out.println("</table>");
                    out.println("<p class=\"success\">Query successful. " + rowCount + " row(s) returned.</p>");

                    rs.close();
                    stmt.close();
                }
            } catch (ClassNotFoundException e) {
                out.println("<p class=\"error\">Error: MySQL JDBC Driver not found.<br>" + escapeHtml(e.getMessage()) + "</p>");
            } catch (SQLException e) {
                out.println("<p class=\"error\">SQL Error: " + escapeHtml(e.getMessage()) + "</p>");
            }
        }

        out.println("<br><hr style=\"border-color:#333;\">");
        out.println("<p style=\"text-align:center;\"><a href=\"clientHome.jsp\">Back to Command Entry</a></p>");
        out.println("</div></body></html>");
    }

    private String escapeHtml(String s) {
        if (s == null) return "NULL";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
