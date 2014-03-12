package iot.lane.alipaycontest.firstseason;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataETL {

	private static LinkedList<Object> users = null;
	private static LinkedList<Object> items = null;
	static Logger logger = LogManager.getLogger(DataETL.class.getName());

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			getUsers();
			// getItems();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static LinkedList<Object> getUsers() throws SQLException {
		if (users != null) {
			return users;
		}
		// if the companies is null then make it happen
		users = new LinkedList<Object>();
		LinkedList<Integer> userIds = new LinkedList<Integer>();
		// PreparedStatement preparedStatement;
		java.sql.Statement statement = null;
		ResultSet resultSet = null;
		// String sqlStat =
		// "select user_id from tmail_firstseason group by user_id;";
		String sqlStat = "select * from tmail_firstseason where MONTH(visit_datetime) = 07 group by user_id;";

		Connection connection = DriverManager.getConnection(MYSQLCONFIG.DBURL,
				MYSQLCONFIG.USRNAME, MYSQLCONFIG.PASSWORD);

		statement = connection.createStatement();
		statement.executeQuery(sqlStat);
		resultSet = statement.getResultSet();
		while (resultSet.next()) {
			userIds.add(resultSet.getInt(2));
		}

		for (int userId : userIds) {
			sqlStat = "select * from tmail_firstseason where user_id=" + userId
					+ " and (MONTH(visit_datetime) = 07);";
			// preparedStatement = connection.prepareStatement(sqlStat);
			statement = connection.createStatement();
			statement.executeQuery(sqlStat);
			resultSet = statement.getResultSet();
			int clickCount = 0;
			int purchaseCount = 0;
			int FavoriteCount = 0;
			int ShopcartCount = 0;
			int userActive = 0;
			
			User user = new User();

			while (resultSet.next()) {

				User.Product product = user.new Product();
				product.setBrandID(resultSet.getInt(3));
				int type = resultSet.getInt(4);
				userActive = calculateWeight(userActive, type);
				product.setType(resultSet.getInt(4));
				product.setVisitDaytime(resultSet.getDate(5));

				switch (type) {
				case 0: {
					clickCount++;
				}
					break;

				case 1: {
					purchaseCount++;
				}
					break;

				case 2: {
					FavoriteCount++;
				}
					break;

				case 3: {
					purchaseCount++;
					// ShopcartCount++;
				}
					break;
				}
				user.addProducts(product);

			}

			user.setUserID(userId);
			user.setClickCount(clickCount);
			user.setPurchaseCount(purchaseCount);
			user.setFavoriteCount(FavoriteCount);
			user.setShopcartCount(ShopcartCount);
			user.setWeight(userActive);
			
			if(clickCount !=0){
				double temp = (double)(purchaseCount+ShopcartCount)/clickCount;
				BigDecimal b = new BigDecimal(temp);
				//小数取四位
				temp = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
				user.setClick2purchase(temp);
			}else{
				//点击转化率设为平均值
				user.setClick2purchase(0.0390);
			}
			
			users.add(user);
		}

		logger.debug("load users compeletely");

		if (ETLCONFIG.ISDEBUGMODEL) {
			write2File(users);
		}

		connection.close();

		return users;
	}

	/*
	 * 大赛给出的182,880条交易数据中， 总的点击行为次数为：174,539,占百分比为0.954390857；1
	 * 总的购买行为次数为：6,984,占百分比为0.038188976；26 总的收藏行为次数为：1,204,占百分比为0.006583552；152
	 * 总的购物车行为车次数为：153，占百分比为0.000836614;1195
	 */
	public static int calculateWeight(int iuserActive, int iType) {

		int userActive = iuserActive;
		switch (iType) {
		case 0: {// 点击
			userActive = userActive + 1;
		}
			break;

		case 1: {// 购买，转化率0.038188976
			userActive = userActive + 26;
		}
			break;

		case 2: {// 收藏，转化率0.006583552
			userActive = userActive + 10;
		}
			break;

		case 3: {// 购物车可与购买归为一类
			userActive = userActive + 26;
		}
			break;
		}

		return userActive;
	}

	//
	public static LinkedList<Object> getItems() throws SQLException {
		if (items != null) {
			return items;
		}
		// if the companies is null then make it happen
		items = new LinkedList<Object>();
		LinkedList<Integer> itemIds = new LinkedList<Integer>();
		// PreparedStatement preparedStatement;
		java.sql.Statement statement = null;
		ResultSet resultSet = null;
		String sqlStat = "select brand_id from tmail_firstseason group by brand_id;";

		Connection connection = DriverManager.getConnection(MYSQLCONFIG.DBURL,
				MYSQLCONFIG.USRNAME, MYSQLCONFIG.PASSWORD);

		statement = connection.createStatement();
		statement.executeQuery(sqlStat);
		resultSet = statement.getResultSet();
		while (resultSet.next()) {
			itemIds.add(resultSet.getInt(1));
		}

		for (int itemId : itemIds) {
			sqlStat = "select * from tmail_firstseason where brand_id="
					+ itemId + ";";
			// preparedStatement = connection.prepareStatement(sqlStat);
			statement = connection.createStatement();
			statement.executeQuery(sqlStat);
			resultSet = statement.getResultSet();
			int dealCount = 0;
			int itemPopular = 0;
			Item item = new Item();

			while (resultSet.next()) {

				Item.User user = item.new User();
				user.setBrandID(resultSet.getInt(3));
				int type = resultSet.getInt(4);
				user.setType(resultSet.getInt(4));
				user.setVisitDaytime(resultSet.getDate(5));

				itemPopular = calculateWeight(itemPopular, type);
				item.addUsers(user);
				dealCount++;

			}

			item.setProductID(itemId);
			item.setDealCount(dealCount);
			item.setWeight(itemPopular);
			items.add(item);
		}

		logger.debug("load users compeletely");

		if (ETLCONFIG.ISDEBUGMODEL) {
			write2File(items);
		}

		connection.close();

		return items;
	}

	// write the tmp list to file
	private static void write2File(List<Object> items) {
		write2File(items, 1);
	}

	// write the list to file
	private static void write2File(List<Object> items, int type) {
		if (type == 1) {
			try {
				File file = new File(ETLCONFIG.TMPPATH
						+ items.get(0).getClass().getName());
				FileWriter writer = new FileWriter(file);
				for (Object object : items) {
					writer.write(object.toString() + "\n");
				}
				writer.close();
			} catch (Exception e) {
				// ignore the exception
			}
		} else {
			try {
				File file = new File(ETLCONFIG.RESOURCE
						+ items.get(0).getClass().getName());
				FileWriter writer = new FileWriter(file);
				for (Object object : items) {
					writer.write(object.toString() + "\n");
				}
				writer.close();
			} catch (Exception e) {
				// ignore the exception
			}
		}
	}
}
