package db;

import domain.Code;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lazar Vujadinovic
 */
public final class DBBroker {

    private static DBBroker INSTANCE;
    private Connection connection;

    private String url;
    private String user;
    private String password;
    private String driver;

    private DBBroker() {
        url = Util.getINSTANCE().getDBURL();
        user = Util.getINSTANCE().getDBUser();
        password = Util.getINSTANCE().getDBPassword();
        driver = Util.getINSTANCE().getDBDriver();
    }

    public static DBBroker getINSTANCE() throws Exception {
        if (INSTANCE == null) {
            INSTANCE = new DBBroker();
        }
        return INSTANCE;
    }

    public void loadDriver() throws Exception {
        try {
            Class.forName(driver);
            System.out.println("Driver loaded.");
        } catch (ClassNotFoundException ex) {
            throw new Exception("Driver not found");
        }
    }

    public void openConnection() throws Exception {
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connection opened.");
        } catch (SQLException ex) {
            throw new Exception("Connecting to base failed.");
        }
    }

    public void closeConnection() throws Exception {
        try {
            connection.close();
            System.out.println("Connection closed.");
        } catch (SQLException ex) {
            throw new Exception("Connection closing failed.");
        }
    }

    public List<Code> getCodesToTranslate() {
        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM PMKFORMULA";
            List<Code> lc = new ArrayList<>();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Code c = new Code();
                String code = rs.getString("FORMULATEXTWORK");
                if (code.startsWith("\"")) {
                    c.setCode(code.substring(1, code.length() - 1));
                } else {
                    c.setCode(code);
                }
                c.setDomainID(rs.getString("DOMAINID"));
                c.setObjectID(rs.getString("OBJECTID"));
                lc.add(c);
            }
            rs.close();
            stmt.close();
            System.out.println("German codes loaded.");
            return lc;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void saveEnglishCodes(List<Code> codesToTranslate) throws Exception {
        Statement statement = connection.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS PMKFORUMULA_ENG"
                + "(OBJECTID CHAR(10) NOT NULL,"
                + " DOMAINID CHAR(10) NOT NULL,"
                + " FORMULATEXTWORK TEXT,"
                + " PRIMARY KEY(OBJECTID,DOMAINID))";
        statement.executeUpdate(sql);
        statement.close();

        Statement deleteStatement = connection.createStatement();
        String sqlDelete = "DELETE FROM PMKFORUMULA_ENG";
        deleteStatement.executeUpdate(sqlDelete);
        deleteStatement.close();

        String query = "INSERT INTO PMKFORUMULA_ENG(OBJECTID,DOMAINID,FORMULATEXTWORK) VALUES(?,?,?)";
        for (Code c : codesToTranslate) {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, c.getObjectID());
            ps.setString(2, c.getDomainID());
            ps.setString(3, c.getCode());
            System.out.println("Code: " + c.getObjectID() + ", " + c.getDomainID() + " saved.");
            ps.executeUpdate();
            ps.close();
        }
        System.out.println("English codes saved.");
    }

}
