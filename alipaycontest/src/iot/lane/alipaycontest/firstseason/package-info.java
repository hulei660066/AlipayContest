/**
 * @author mike
 * 
 *         this is just a config file
 */
package iot.lane.alipaycontest.firstseason;

class MYSQLCONFIG {

    // there are some configuration about mysql
    public static final java.lang.String USRNAME   = "root";
    public static final java.lang.String PASSWORD  = "";
    public static final java.lang.String DBURL     = "jdbc:mysql://127.0.0.1:3306/tmailcontest";
    public static final java.lang.String DRIVER    = "com.mysql.jdbc.Driver";
    // ----------------------------------------------------------------------

    public static final java.lang.String DBNAME    = "tmailcontest";
    public static final java.lang.String TABLENAME = "tmail_firstseason";

}

class ETLCONFIG {
    // there are config about the file
	public static final boolean ISDEBUGMODEL  = true;

    public static final java.lang.String SQLFILEPATH = "sql" + java.io.File.separator
                                                             + "cnpc2013.sql";
    public static final java.lang.String TMPPATH     = "tmp" + java.io.File.separator;
    public static final java.lang.String RESOURCE	= "resource" + java.io.File.separator;
}
