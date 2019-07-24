package net.easipay.datades;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @Author: jiande.hu
 * @Date: 2019/7/24 15:23
 * @Description:
 */
public class ConnectInstance {

    private static Connection newConn = null;

    private static final Logger log = Logger.getLogger(DBUtil.class.getName());

    public static Connection getConnect() throws SQLException, ClassNotFoundException {
        if (newConn == null) {
            init();
        }
        if (newConn.isClosed()) {
            init();
        }
        return newConn;
    }

    public static void init() throws SQLException, ClassNotFoundException {
        log.info("========Database connecting========");
        String dbDriver = PropertiesUtil.getPropertiesVaue("db_driver");
        String dbUrl = PropertiesUtil.getPropertiesVaue("db_url");
        String dbUsername = PropertiesUtil.getPropertiesVaue("db_username");
        String dbPassword = PropertiesUtil.getPropertiesVaue("db_password");
        if (StringUtils.isEmpty(dbDriver) || StringUtils.isEmpty(dbUrl) || StringUtils.isEmpty(dbUsername) || StringUtils.isEmpty(dbPassword)) {
            throw new SQLException("configuration file is error !");
        }
        Class.forName(dbDriver);
        newConn = DriverManager.getConnection(dbUrl, dbUsername,
                dbPassword);
        log.info("========Database connected========");
    }
}
