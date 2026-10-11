package project3;


import java.io.*;
import java.sql.*;
import java.util.Properties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class RootUserServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String sqlCommand = request.getParameter("sqlcommand");

        // Load root properties
        Properties props = new Properties();
        String propsPath = getServletContext().getRealPath("/WEB-INF/conf/root.properties");
        try (FileInputStream fis = new FileInputStream(propsPath)) {
            props.load(fis);
        }

        String dbUrl = props.getProperty("MYSQL_DB_URL=jdbc:mysql://localhost:3306/project3?useSSL=false");
        String dbUser = props.getProperty("root");
        String dbPass = props.getProperty("root");

        // Build the HTML response page
        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Root User - Results</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; background-color: #1a1a2e; color: #e0e0e0; margin: 0; padding: 20px; }");
        out.println(".container { max-width: 950px; margin: 0 auto; background-color: #16213e; padding: 30px; border-radius: 10px; }");
        out.println("h2 { color: #e94560; text-align: center; }");
        out.println("table { border-collapse: collapse; width: 100%; margin-top: 15px; }");
        out.println("th { background-color: #e94560; color: white; padding: 10px; text-align: left; }");
        out.println("td { padding: 8px 10px; border-bottom: 1px solid #333; }");
        out.println("tr:nth-child(even) { background-color: #0f3460; }");
        out.println("tr:hover { background-color: #1a4a7a; }");
        out.println(".command-box { background-color: #0f3460; padding: 15px; border-radius: 5px; margin-bottom: 15px; font-family: monospace; word-wrap: break-word; }");
        out.println(".success { color: #00ff88; font-weight: bold; }");
        out.println(".error { color: #ff4444; font-weight: bold; }");
        out.println(".info { color: #f0a500; font-weight: bold; }");
        out.println("a { color: #e94560; text-decoration: none; }");
        out.println("a:hover { text-decoration: underline; }");
        out.println("</style></head><body>");
        out.println("<div class=\"container\">");
        out.println("<h2>Root-Level User Results</h2>");
        out.println("<div class=\"command-box\"><b>SQL Command:</b><br>" + escapeHtml(sqlCommand) + "</div>");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                Statement stmt = conn.createStatement();
                String trimmedCommand = sqlCommand.trim().toLowerCase();

                if (trimmedCommand.startsWith("select")) {
                    // Execute a query
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
                } else {
                    // Execute an update/insert/delete/replace
                    int rowsAffected = stmt.executeUpdate(sqlCommand);
                    out.println("<p class=\"success\">Command executed successfully. " + rowsAffected + " row(s) affected.</p>");

                    // *** BUSINESS LOGIC ***
                    // Check if the command involves the shipments table
                    // If insert/update on shipments with quantity >= 100, increment status by 5
                    // for all suppliers who have at least one shipment with quantity >= 100
                    if (trimmedCommand.contains("shipment")) {
                        // Check if any supplier now has a shipment with quantity >= 100
                        String checkSql = "SELECT DISTINCT snum FROM shipments WHERE quantity >= 100";
                        ResultSet checkRs = stmt.executeQuery(checkSql);
                        boolean hasQualifyingShipments = checkRs.next();
                        checkRs.close();

                        if (hasQualifyingShipments) {
                            // Update the status of ALL suppliers who have at least one shipment
                            // with quantity >= 100
                            String updateSql = "UPDATE suppliers SET status = status + 5 " +
                                               "WHERE snum IN (SELECT DISTINCT snum FROM shipments WHERE quantity >= 100)";
                            int suppliersUpdated = stmt.executeUpdate(updateSql);
                            out.println("<p class=\"info\">Business Logic Triggered! " +
                                        suppliersUpdated + " supplier(s) had their status incremented by 5.</p>");
                        } else {
                            out.println("<p class=\"info\">Business Logic Not Triggered.</p>");
                        }
                    } else {
                        out.println("<p class=\"info\">Business Logic Not Triggered.</p>");
                    }
                }
                stmt.close();
            }
        } catch (ClassNotFoundException e) {
            out.println("<p class=\"error\">Error: MySQL JDBC Driver not found.<br>" + escapeHtml(e.getMessage()) + "</p>");
        } catch (SQLException e) {
            out.println("<p class=\"error\">SQL Error: " + escapeHtml(e.getMessage()) + "</p>");
        }

        out.println("<br><hr style=\"border-color:#333;\">");
        out.println("<p style=\"text-align:center;\"><a href=\"rootHome.jsp\">Back to Command Entry</a></p>");
        out.println("</div></body></html>");
    }

    private String escapeHtml(String s) {
        if (s == null) return "NULL";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
