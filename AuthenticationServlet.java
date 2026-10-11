package project3;



import java.io.*;
import java.sql.*;
import java.util.Properties;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class AuthenticationServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Properties props = new Properties();
        String propsPath = getServletContext().getRealPath("/WEB-INF/conf/systemapp.properties");
        try (FileInputStream fis = new FileInputStream(propsPath)) {
            props.load(fis);
        }

        String dbUrl = props.getProperty("MYSQL_DB_URL");
        String dbUser = props.getProperty("MYSQL_DB_USERNAME");
        String dbPass = props.getProperty("MYSQL_DB_PASSWORD");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                String sql = "SELECT * FROM usercredentials WHERE login_username = ? AND login_password = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, password);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            HttpSession session = request.getSession();
                            session.setAttribute("username", username);

                            switch (username) {
                                case "root":
                                    response.sendRedirect("rootHome.jsp");
                                    break;
                                case "client":
                                    response.sendRedirect("clientHome.jsp");
                                    break;
                                case "dataentry":
                                    response.sendRedirect("dataEntryHome.jsp");
                                    break;
                                case "theaccountant":
                                    response.sendRedirect("accountantHome.jsp");
                                    break;
                                default:
                                    response.sendRedirect("errorpage.html");
                                    break;
                            }
                        } else {
                            response.sendRedirect("errorpage.html");
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            response.sendRedirect("errorpage.html");
        } catch (SQLException e) {
            response.sendRedirect("errorpage.html");
        }
    }
}