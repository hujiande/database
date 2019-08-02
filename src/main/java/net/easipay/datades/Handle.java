package net.easipay.datades;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        String file = "E:\\database\\target\\handle.properties";
        if (args.length > 0) {
            log.info("Please Use the specified configuration file [" + args[0] + "]");
            file = args[0];
        }
        try {
            is = new FileInputStream(args[0]);
        } catch (Exception e) {
            log.info("the specified configuration file is error, check it please!");
            return;
        }

        try {
            ConnectInstance.getConnect();
        } catch (Exception e) {
            log.info("connect failed!");
            e.printStackTrace();
        }
        String cryptTpye = PropertiesUtil.getPropertiesVaue("cryptTpye");

        log.info("===============Start " + cryptTpye + "=================");
        String column_name = PropertiesUtil.getPropertiesVaue("column_name");
        String primary_key_name = PropertiesUtil.getPropertiesVaue("primary_key_name");
        String encKey = PropertiesUtil.getPropertiesVaue("sec_key");
        //String encKey = new String(PropertiesUtil.getPropertiesVaue("sec_key").getBytes(), "utf-8");
        String[] split_column_name = column_name.split(",");
        String[] pk_split = primary_key_name.split(",");
        log.info("========Table And Column requiring " + cryptTpye + " is : " + Arrays.toString(split_column_name) + "  ========");
        log.info("========PK is : " + Arrays.toString(pk_split) + "  ========");
        log.info("========Sec_key is : [" + encKey + "]========");

        List<String> collist = new ArrayList<String>();
        String strTable = "";
        for (String splitClumn : split_column_name) {
            String[] splitTableColumn = splitClumn.split("=");
            strTable = splitTableColumn[0];
            collist.add(splitTableColumn[1]);
        }

        for (String pk : pk_split) {
            if (pk.split("=")[0].equals(strTable)){
                try {
                    log.info("========Tables is : [" + strTable + "] column is :[" + collist.toString() + "] primary key is [" + pk.split("=")[1] + "]  is " + cryptTpye + "ING ========");
                    if (cryptTpye.equals("ENCRYPT")) {
                        DBUtil.encrypt(strTable, collist, pk.split("=")[1], encKey);
                    } else if (cryptTpye.equals("DECRYPT")) {
                        DBUtil.decrypt(strTable, collist, pk.split("=")[1], encKey);
                    } else {
                        throw new Exception("加解密类型未知！请查看配置文件");
                    }
                    log.info("========Tables is : [" + strTable + "] column is :[" + collist.toString() + "] primary key is [" + pk.split("=")[1] + "]  is " + cryptTpye + "ED ========");
                } catch (Exception e) {
                    log.info(cryptTpye + " error!! Table is :[" + strTable + "], please check");
                    e.printStackTrace();
                }

            }
        }



        //以等号分割 =
/*        for (String splitClumn : split_column_name) {
            String[] splitTableColumn = splitClumn.split("=");
            //以 & 分割
            String[] clumnNames = splitTableColumn[1].split("&");
            for (String clumnName : clumnNames) {
                for (String pk : pk_split) {
                    if (pk.split("=")[0].equals(splitTableColumn[0])) {
                        try {
                            log.info("========Tables is : [" + splitTableColumn[0] + "] column is :[" + clumnName + "] primary key is [" + pk.split("=")[1] + "]  is " + cryptTpye + "ING ========");
                            if (cryptTpye.equals("ENCRYPT")) {
                                DBUtil.encrypt(splitTableColumn[0], clumnName, pk.split("=")[1], encKey);
                            } else if (cryptTpye.equals("DECRYPT")) {
                                DBUtil.decrypt(splitTableColumn[0], clumnName, pk.split("=")[1], encKey);
                            } else {
                                throw new Exception("加解密类型未知！请查看配置文件");
                            }
                            log.info("========Tables is : [" + splitTableColumn[0] + "] column is :[" + clumnName + "] primary key is [" + pk.split("=")[1] + "]  is " + cryptTpye + "ED ========");
                        } catch (Exception e) {
                            log.info(cryptTpye + " error!! Table is :[" + splitTableColumn[0] + "] column is :[" + clumnName + "] , please check");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }*/
        log.info("===============ALL ENCRYPTION END=================");
    }

    public static Properties getProperties(String propFileKey) throws Exception {
        Properties prop = new Properties();
        prop.load(is);
        return prop;
    }
}
