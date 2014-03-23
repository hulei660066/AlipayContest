/**
 * @author mike
 * 
 *         this is just a config file
 */
package iot.lane.alipaycontest.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class MYSQLCONFIG {

	// there are some configuration about mysql
	public static final java.lang.String USRNAME = "root";
	public static final java.lang.String PASSWORD = "";
	public static final java.lang.String DBURL = "jdbc:mysql://127.0.0.1:3306/tmailcontest";
	public static final java.lang.String DRIVER = "com.mysql.jdbc.Driver";
	// ----------------------------------------------------------------------

	public static final java.lang.String DBNAME = "tmailcontest";
	public static final java.lang.String TABLENAME = "tmail_firstseason";

	// ----------------------------------------------------------------------
	public static final java.lang.String isOnlypurchaseAction = " and (type=1 or type=3) ";
	public static final java.lang.String isOnlyClickAction = " and (type=0 or type=2) ";
	public static final java.lang.String isAllAction = "";

	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");     
<<<<<<< .merge_file_F0bu26
	public static final java.lang.String DateThreshold = "2013-06-15";   
=======
	public static final java.lang.String DateThreshold = "2013-07-15";   
>>>>>>> .merge_file_zHo9B4
//	public static final java.lang.String DateThreshold = "2013-07-15";

}

class ETLCONFIG {
	// there are config about the file
	public static final boolean ISDEBUGMODEL = true;

	public static final java.lang.String SQLFILEPATH = "sql"
			+ java.io.File.separator + "cnpc2013.sql";
	public static final java.lang.String TMPPATH = "tmp"
			+ java.io.File.separator;
	public static final java.lang.String RESOURCE = "resource"
			+ java.io.File.separator;
}
