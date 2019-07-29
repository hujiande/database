package net.easipay.datades;

import net.easipay.pepp.common.util.DesBase64;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBUtil {

    public static void decrypt(String table_name, String column_name, String id, String decKey) throws Exception {
        Connection conn = ConnectInstance.getConnect();
        PreparedStatement ps;
        ResultSet rs;
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
                    String plain = DesBase64.decrypt_sm4(decKey, columnValue);
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

    public static void encrypt(String table_name, String column_name, String id, String encKey) throws SQLException, ClassNotFoundException {
        Connection conn = ConnectInstance.getConnect();
        PreparedStatement ps;
        ResultSet rs;
        try {
            String sql = "select " + id + "," + column_name + " from " + table_name;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String idStr = rs.getString(id);
                String columnValue =rs.getString(column_name);
                String plain = DesBase64.encrypt_sm4(encKey, columnValue);
                if (plain != null) {
                    String updateSql = "update " + table_name + " set " + column_name + " = '" + plain + "' where " + id + " = '" + idStr + "'";
                    ps = conn.prepareStatement(updateSql);
                    ps.executeUpdate();
                    ps.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
