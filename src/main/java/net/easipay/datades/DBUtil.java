package net.easipay.datades;

import net.easipay.common.DesBase64;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBUtil {
    private static Connection newConn = null;

    public static Connection getConnect() throws Exception {
        if (newConn == null) {
            init();
        }
        if (newConn.isClosed()) {
            init();
        }
        return newConn;
    }

    public static void init() throws Exception {
        String dbDriver = PropertiesUtil.getPropertiesVaue("db_driver");
        String dbUrl = PropertiesUtil.getPropertiesVaue("db_url");
        String dbUsername = PropertiesUtil.getPropertiesVaue("db_username");
        String dbPassword = PropertiesUtil.getPropertiesVaue("db_password");
        if (StringUtils.isEmpty(dbDriver) || StringUtils.isEmpty(dbUrl) || StringUtils.isEmpty(dbUsername) || StringUtils.isEmpty(dbPassword)) {
            throw new Exception("configuration file is error !");
        }
        Class.forName(dbDriver);
        newConn = DriverManager.getConnection(dbUrl, dbUsername,
                dbPassword);
    }

    public static void decrypt(String table_name, String column_name, String id) throws Exception {
        Connection conn = getConnect();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "select " + id + "," + column_name + " from " +
                    table_name;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String idStr = rs.getString(id);
                String columnValue = rs.getString(column_name);
                String regEx = "^[0-9]*$";
                Pattern pattern = Pattern.compile(regEx);
                Matcher matcher = pattern.matcher(columnValue);
                if (!matcher.matches()) {
                    String plain = DesBase64.decrypt_sm4("bVa7Zr6THLshZIiH", columnValue);
                    if (plain != null) {
                        String updateSql = "update " + table_name + " set " +
                                column_name + " = '" + plain + "' where " + id + " = '" +
                                idStr + "'";
                        ps = conn.prepareStatement(updateSql);
                        ps.executeUpdate();
                        ps.close();
                    }
                }
            }
            System.out.println("deal complete !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void encrypt(String table_name, String column_name, String id) throws Exception {
        String encKey = PropertiesUtil.getPropertiesVaue("sec_key");
        Connection conn = getConnect();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "select " + id + "," + column_name + " from " + table_name;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String idStr = rs.getString(id);
                String columnValue = rs.getString(column_name);
                String plain = DesBase64.encrypt_sm4(encKey, columnValue);
                if (plain != null) {
                    String updateSql = "update " + table_name + " set " + column_name + " = '" + plain + "' where " + id + " = '" + idStr + "'";
                    ps = conn.prepareStatement(updateSql);
                    ps.executeUpdate();
                    ps.close();
                }
            }
            System.out.println("deal complete!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
