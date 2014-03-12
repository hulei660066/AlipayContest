package iot.mike.alipaycontest.firstseason;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class used to hold the data instead of get data from the database each
 * time.
 * 
 * @author mike
 * 
 */
public class DataHolder {

    static Logger         logger     = LogManager.getLogger(DataHolder.class.getName());

    static List<DataItem> dataItems  = new LinkedList<>();

    static Connection     connection = null;

    static {
        try {
            connection = DriverManager.getConnection(MYSQLCONFIG.DBURL,
                                                     MYSQLCONFIG.USRNAME,
                                                     MYSQLCONFIG.PASSWORD);
            if (connection != null) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select * from tmail_firstseason;");

                int num = 0;
                while (resultSet.next()) {
                    ++num;
                    int userid = resultSet.getInt("user_id");
                    int brandid = resultSet.getInt("brand_id");
                    int type = resultSet.getInt("type");

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
                    Date date = dateFormat.parse(resultSet.getString("visit_datetime"));

                    DataItem dataItem = new DataItem();
                    dataItem.setBrandid(brandid);
                    dataItem.setUserid(userid);
                    dataItem.setDate(date);
                    dataItem.setType(type);

                    dataItems.add(dataItem);
                }
                logger.info("Data Loaded Success!" + "SUM: " + num);
            }
            
            connection.close();
        } catch (SQLException | ParseException e) {
            // SQL connection failed
            logger.warn(e.getMessage());
        }
    }
    
    /**
     * get the data each event, which has been classfied by the userid
     * 
     * @param userid userid
     * @return list\<DataItem\>
     */
    public static LinkedList<DataItem> getDataByUserID(int userid) {
        LinkedList<DataItem> items = new LinkedList<>();
        for (DataItem iterable : dataItems) {
            if (iterable.getUserid() == userid) {
                items.add(iterable);
            }
        }
        return items;
    }
    

    /**
     * get the data each event, which has been classfied by the userid
     * 
     * @param brandid brandid
     * @return list\<DataItem\>
     */
    public static LinkedList<DataItem> getDataByBrandID(int brandid) {
        LinkedList<DataItem> items = new LinkedList<>();
        for (DataItem iterable : dataItems) {
            if (iterable.getBrandid() == brandid) {
                items.add(iterable);
            }
        }
        return items;
    }
    
    /**
     * Get all user id
     * @return int[]
     */
    public static Integer[] getUserID() {
        Integer[] users = new Integer[10];
        HashSet<Integer> usersMap = new HashSet<>();
        for (DataItem item : dataItems) {
            usersMap.add(item.getUserid());
        }
        return usersMap.toArray(users);
    }
    
    /**
     * Get all brand id
     * @return int[]
     */
    public static Integer[] getBrandID() {
        Integer[] brands = new Integer[10];
        HashSet<Integer> brandsMap = new HashSet<>();
        for (DataItem item : dataItems) {
            brandsMap.add(item.getUserid());
        }
        return brandsMap.toArray(brands);
    }
    
}
