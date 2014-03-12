/**
 * @author mike
 * 
 *         this is just a config file
 */
package iot.mike.alipaycontest.firstseason;

class MYSQLCONFIG {

    // there are some configuration about mysql
    public static final java.lang.String USRNAME   = "root";
    public static final java.lang.String PASSWORD  = "delta-boyMIKE";
    public static final java.lang.String DBURL     = "jdbc:mysql://127.0.0.1:3306/tmailcontest";
    public static final java.lang.String DRIVER    = "com.mysql.jdbc.Driver";
    // ----------------------------------------------------------------------

    public static final java.lang.String DBNAME    = "tmailcontest";
    public static final java.lang.String TABLENAME = "tmail_firstseason";
}

class ETLCONFIG {
    public static final java.lang.String BRANDS_FILE = "result" + java.io.File.separator + "brands";
    public static final java.lang.String USERS_FILE  = "result" + java.io.File.separator + "users";
}
