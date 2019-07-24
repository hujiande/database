package net.easipay.datades;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

/**
 * @Author: jiande.hu
 * @Date: 2019/7/23 19:06
 * @Description:
 */
public class Handle {

    private static InputStream is = null;

    private static final Logger log = Logger.getLogger(Handle.class.getName());

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            log.info("Please Use the specified configuration file [" + args[0] + "]");
            try {
                is = new FileInputStream(args[0]);
            } catch (Exception e) {
                log.info("the specified configuration file is error, check it please!");
                return;
            }
        } else {
            log.info("no file input!");
            return;
        }
        try {
            ConnectInstance.getConnect();
        } catch (Exception e) {
            log.info("connect failed!");
            e.printStackTrace();
        }

        log.info("===============Start ENCRYPTION=================");
        String table_name = PropertiesUtil.getPropertiesVaue("table_name");
        String column_name = PropertiesUtil.getPropertiesVaue("column_name");
        String primary_key_name = PropertiesUtil.getPropertiesVaue("primary_key_name");
        String encKey = PropertiesUtil.getPropertiesVaue("sec_key");
        String[] split_table_name = table_name.split(",");
        String[] split_column_name = column_name.split(",");
        String[] pk_split = primary_key_name.split(",");
        log.info("========Tables requiring encryption is : " + Arrays.toString(split_table_name) + "  ========");
        log.info("========Column requiring encryption is : " + Arrays.toString(split_column_name) + "  ========");
        log.info("========PK is : " + Arrays.toString(pk_split) + "  ========");
        log.info("========Sec_key is : [" + encKey + "]========");
        for (String tableName : split_table_name) {
            for (String columnName : split_column_name) {
                if (columnName.split("=")[0].equals(tableName)) {
                    for (String pk : pk_split) {
                        if (pk.split("=")[0].equals(tableName)) {
                            try {
                                log.info("========Tables is : [" + tableName + "] column is :[" + columnName.split("=")[1] + "] primary key is [" + pk.split("=")[1] + "]  is encrypting ========");
                                DBUtil.encrypt(tableName, columnName.split("=")[1], pk.split("=")[1], encKey);
                                log.info("========Tables is : [" + tableName + "] column is :[" + columnName.split("=")[1] + "] primary key is [" + pk.split("=")[1] + "]  is encrypted ========");
                            } catch (Exception e) {
                                log.info("encryption error!! Table is :[" + tableName + "] column is :[" + columnName.split("=")[1] + "] , please check");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        log.info("===============ALL ENCRYPTION END=================");
    }

    public static Properties getProperties(String propFileKey) throws Exception {
        Properties prop = new Properties();
        prop.load(is);
        return prop;
    }
}
