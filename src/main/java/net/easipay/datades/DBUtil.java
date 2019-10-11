package net.easipay.datades;

import net.easipay.pepp.common.util.DesBase64;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DBUtil {

    public static void decrypt(String table_name, List<String> columnList, String id, String decKey) throws Exception {

        Connection conn = ConnectInstance.getConnect();
        PreparedStatement ps;
        ResultSet rs;

        String columns = "";
        for (int i = 0; i < columnList.size(); i++) {
            columns = columns + columnList.get(i) + ",";
        }
        try {
            String sql = "select " + columns + id + " from " + table_name;
            System.out.println(" select sql: " + sql);
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            String idStr = "";
            String colsets = "";
            long count = 0;
            while (rs.next()) {
                idStr = rs.getString(id);
                String colVal = "";
                colsets = "";
                for (int j = 0; j < columnList.size(); j++) {
                    colVal = rs.getString(columnList.get(j));
                    if (null != colVal /*&& colVal.lastIndexOf("=") >= 0*/) {
                        String enVal = "";
                        try {
                            enVal = DesBase64.decrypt_sm4(decKey, colVal);
                        } catch (Exception ex) {
                            enVal = colVal;
                        }
                        if (null != enVal) {
                            String replaceEnVal = enVal.replace("'", "''");
                            colsets = colsets + columnList.get(j) + " = '" + replaceEnVal + "', ";
                        }
                    }
                }
                String updateSql = "update " + table_name + " set " + colsets + " ENCRIYPT = '0' where " + id + " = '" + idStr + "'";
                System.out.println(" update sql: " + updateSql);
                ps = conn.prepareStatement(updateSql);
                ps.executeUpdate();
                ps.close();
                count = count + 1;
                if (count % 10000 == 0) {
                    System.out.println("table " + table_name + " have updated " + count);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void encrypt(String table_name, List<String> columnList, String id, String encKey) throws SQLException, ClassNotFoundException {
        Connection conn = ConnectInstance.getConnect();
        PreparedStatement ps;
        ResultSet rs;
        String columns = "";
        for (int i = 0; i < columnList.size(); i++) {
            columns = columns + columnList.get(i) + ",";
        }
        try {
            String sql = "select " + columns + id + " from " + table_name;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            String idStr = "";
            String colsets = "";
            long count = 0;
            while (rs.next()) {
                idStr = rs.getString(id);

                String colVal = "";
                colsets = "";
                for (int j = 0; j < columnList.size(); j++) {
                    colVal = rs.getString(columnList.get(j));
                    String enVal = "";
                    if (null != colVal && colVal.lastIndexOf("=") == -1) {
                        enVal = DesBase64.encrypt_sm4(encKey, colVal);
                        if (null != enVal) {
                            colsets = colsets + columnList.get(j) + " = '" + enVal + "', ";
                        }
                    }
                }
                String updateSql = "update " + table_name + " set " + colsets + " ENCRIYPT = '1' where " + id + " = '" + idStr + "'";
                ps = conn.prepareStatement(updateSql);
                ps.executeUpdate();
                ps.close();
                count = count + 1;
                if (count % 10000 == 0) {
                    System.out.println("table " + table_name + " have updated " + count);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String encKey = "1111222211112222";
        for (int i = 0; i < 1000; i++) {
            String enVal = DesBase64.encrypt_sm4(encKey, "" + i);
            System.out.println(i + ":" + enVal + ":" + enVal.lastIndexOf("=="));
        }
    }
}
