package project3;



import java.io.*;
import java.sql.*;
import java.util.Properties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class SuppliersInsertServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String snum = request.getParameter("snum");
        String sname = request.getParameter("sname");
        String statusStr = request.getParameter("status");
        String city = request.getParameter("city");

        // Load dataentry properties
        Properties props = new Properties();
        String propsPath = getServletContext().getRealPath("/WEB-INF/conf/dataentry.properties");
        try (FileInputStream fis = new FileInputStream(propsPath)) {
            props.load(fis);
        }

        String dbUrl = props.getProperty("MYSQL_DB_URL");
        String dbUser = props.getProperty("MYSQL_DB_USERNAME");
        String dbPass = props.getProperty("MYSQL_DB_PASSWORD");

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Data Entry - Supplier Insert Result</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; background-color: #1a1a2e; color: #e0e0e0; margin: 0; padding: 20px; }");
        out.println(".container { max-width: 700px; margin: 0 auto; background-color: #16213e; padding: 30px; border-radius: 10px; }");
        out.println("h2 { color: #f0a500; text-align: center; }");
        out.println(".command-box { background-color: #0f3460; padding: 15px; border-radius: 5px; margin-bottom: 15px; font-family: monospace; }");
        out.println(".success { color: #00ff88; font-weight: bold; }");
        out.println(".error { color: #ff4444; font-weight: bold; }");
        out.println("a { color: #f0a500; text-decoration: none; }");
        out.println("a:hover { text-decoration: underline; }");
        out.println("</style></head><body>");
        out.println("<div class=\"container\">");
        out.println("<h2>Supplier Insert Result</h2>");
        out.println("<div class=\"command-box\"><b>Values:</b> snum=" + escapeHtml(snum) +
                    ", sname=" + escapeHtml(sname) + ", status=" + escapeHtml(statusStr) +
                    ", city=" + escapeHtml(city) + "</div>");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            int status = Integer.parseInt(statusStr);

            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                String sql = "INSERT INTO suppliers VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, snum);
                    pstmt.setString(2, sname);
                    pstmt.setInt(3, status);
                    pstmt.setString(4, city);

                    int rowsAffected = pstmt.executeUpdate();
                    out.println("<p class=\"success\">Supplier inserted successfully. " + rowsAffected + " row(s) inserted.</p>");
                }
            }
        } catch (NumberFormatException e) {
            out.println("<p class=\"error\">Error: Status must be a valid integer.</p>");
        } catch (ClassNotFoundException e) {
            out.println("<p class=\"error\">Error: MySQL JDBC Driver not found.<br>" + escapeHtml(e.getMessage()) + "</p>");
        } catch (SQLException e) {
            out.println("<p class=\"error\">SQL Error: " + escapeHtml(e.getMessage()) + "</p>");
        }

        out.println("<br><hr style=\"border-color:#333;\">");
        out.println("<p style=\"text-align:center;\"><a href=\"dataEntryHome.jsp\">Back to Data Entry</a></p>");
        out.println("</div></body></html>");
    }

    private String escapeHtml(String s) {
        if (s == null) return "NULL";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
