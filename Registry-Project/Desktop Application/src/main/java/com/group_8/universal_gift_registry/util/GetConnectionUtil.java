package com.group_8.universal_gift_registry.util;
/**@application: UniversalGiftRegistry
 * @author: Alexander Schoolcraft, Benjamin King, Brandon King, Gabe Woolums
 * @date: 3/21/2024
 * @version: 3.0
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 
 */
public class GetConnectionUtil {
    /**getConnection uses the information for the SQL database to generate a connection with
     * the back-end server to store information.
     * @return DriverManager connection object using the required information to connect
     * @throws SQLException
     * @throws ClassNotFoundException
     */
	public static Connection getConnection() throws SQLException, ClassNotFoundException {
        String url = "jdbc:sqlserver://ugr.database.windows.net:1433;database=universal_gift_registry;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
        String user = "UGRAdmin@ugr";
        String password = "UGRP@ssw0rd!";
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(url, user, password);
    }
}
