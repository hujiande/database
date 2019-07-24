package net.easipay.datades;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.InputStream;
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
        log.info("===============Start=================");
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
        String table_name = PropertiesUtil.getPropertiesVaue("table_name");
        String[] split_table_name = table_name.split(",");
        for (String tableName : split_table_name) {
            String column_name = PropertiesUtil.getPropertiesVaue("column_name");
            String[] split_column_name = column_name.split(",");
            for (String columName : split_column_name) {
                if (columName.split("=")[0].equals(tableName)) {
                    String primary_key_name = PropertiesUtil.getPropertiesVaue("primary_key_name");
                    String[] pk_split = primary_key_name.split(",");
                    for (String pk : pk_split) {
                        if (pk.split("=")[0].equals(tableName)) {
                            DBUtil.encrypt(tableName, columName.split("=")[1], pk.split("=")[1]);
                        }
                    }
                }
            }
        }
        log.info("===============End=================");
    }

    public static Properties getProperties(String propFileKey) throws Exception {
        Properties prop = new Properties();
        prop.load(is);
        return prop;
    }
}
