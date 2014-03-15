package iot.lane.alipaycontest.firstseason;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class DataETL {

	// private static LinkedList<Object> users = null;
	private static LinkedList<Object> items = null;
	static Logger logger = LogManager.getLogger(DataETL.class.getName());

	public static void main(String[] args) {
		// LinkedList<Object> myStatistics = new LinkedList<Object>();
		LinkedList<PredictBuyStatist> myStatistics = new LinkedList<PredictBuyStatist>();
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(MYSQLCONFIG.dateFormat.parse(MYSQLCONFIG.DateThreshold));
		} catch (ParseException e1) {
			// do nothing
		}

		for (int i = 1; i <= 11; i++) {
			try {
				cal.add(Calendar.DAY_OF_YEAR, -1);
				PredictBuyStatist myStatistic = statisticsResult(MYSQLCONFIG.dateFormat
						.format(cal.getTime()));
				myStatistics.add(myStatistic);

			} catch (SQLException e) {
				// do nothing
			}
		}

		if (ETLCONFIG.ISDEBUGMODEL) {
			write2ExcelFile(myStatistics);
		}
	}

	public static void write2ExcelFile(LinkedList<PredictBuyStatist> datas) {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Sample sheet");

		int rownum = 0;
		Row row = null;
		Cell cell = null;

		row = sheet.createRow(rownum++);
		cell = row.createCell(0);
		cell.setCellValue("precision");
		cell = row.createCell(1);
		cell.setCellValue("recall");
		cell = row.createCell(2);
		cell.setCellValue("f1score");
		cell = row.createCell(3);
		cell.setCellValue("datetime");
		for (PredictBuyStatist data : datas) {
			int cellnum = 0;
			row = sheet.createRow(rownum++);
			cell = row.createCell(cellnum++);
			cell.setCellValue(data.getPrecision());
			cell = row.createCell(cellnum++);
			cell.setCellValue(data.getRecall());
			cell = row.createCell(cellnum++);
			cell.setCellValue(data.getF1Score());
			cell = row.createCell(cellnum++);
			cell.setCellValue((String) data.getDayTime());
		}

		try {
			FileOutputStream out = new FileOutputStream(new File(
					ETLCONFIG.TMPPATH + datas.get(0).getClass().getName()
							+ ".xls"));
			workbook.write(out);
			out.close();
			System.out.println("Excel written successfully..");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static PredictBuyStatist statisticsResult(String dayTime)
			throws SQLException {
		PredictBuyStatist predictBuy = new PredictBuyStatist();

		try {

			HashMap<Integer, LinkedList<Integer>> predictBuyNumItems = getForecast(
					dayTime, MYSQLCONFIG.isAllAction);

			LinkedList<Object> actualBuyNumItems = getUsersSimpl("2013-08-15",
					MYSQLCONFIG.isOnlypurchaseAction);

			// LinkedList<Object> StaticsUsers = new LinkedList<Object>();
			double allHitBrands = 0;
			double allpBrands = 0;
			double allbBrands = 0;

			for (Object actualBuyNumItem : actualBuyNumItems) {
				User usert = (User) actualBuyNumItem;
				int userId = usert.getUserID();
				// pBrandsi为对用户i 预测他(她)会购买的品牌列表个数
				double pBrands = 0;
				// bBrandsi为用户i 真实购买的品牌个数
				double bBrands = usert.getProducts().size();
				allbBrands += bBrands;
				// hitBrandsi对用户i预测的品牌列表与用户i真实购买的品牌交集的个数
				double hitBrands = 0;

				boolean isContainsKey = predictBuyNumItems.containsKey(userId);
				if (isContainsKey) {
					pBrands = predictBuyNumItems.get(userId).size();
					allpBrands += pBrands;
					for (Object product : usert.getProducts()) {
						User.Product productt = (User.Product) product;
						LinkedList<Integer> products = predictBuyNumItems
								.get(userId);
						boolean isContainThisProduct = products
								.contains(productt.getBrandID());
						if (isContainThisProduct) {
							hitBrands++;
							allHitBrands++;
						}
					}
				}
				PredictBuyStatist staticsUser = new PredictBuyStatist();
				double precision = hitBrands / pBrands;
				double recall = hitBrands / bBrands;
				double F1 = 2 * precision * recall / (recall + precision);
				System.out.println("use_id:" + userId + "\tF1: " + F1 + "\tprecision: " + precision
						+ "\tallprecision: " + precision);

			}
			double allPrecision = allHitBrands / allpBrands;
			double allRecall = allHitBrands / allbBrands;
			double allF1 = 2 * allPrecision * allRecall / allRecall
					+ allPrecision;

			predictBuy.setPrecision(allPrecision);
			predictBuy.setRecall(allRecall);
			predictBuy.setF1Score(allF1);
			predictBuy.setDayTime(dayTime);

			if (ETLCONFIG.ISDEBUGMODEL) {
				// write2File(StaticsUsers);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return predictBuy;
	}

	public static HashMap<Integer, LinkedList<Integer>> getForecast(
			String dayTime, String userActionType) throws SQLException {
		LinkedList<Object> users = getUsersSimpl(dayTime, userActionType);
		HashMap<Integer, LinkedList<Integer>> resultUserItems = new HashMap<Integer, LinkedList<Integer>>();

		for (Object user : users) {
			LinkedList<ItemWithWeight> tmpItems = new LinkedList<ItemWithWeight>();
			LinkedList<Integer> resultItems = new LinkedList<Integer>();
			User usert = (User) user;
			Hashtable<Integer, Integer> userItemsTable = new Hashtable<Integer, Integer>();

			for (Object product : usert.getProducts()) {
				User.Product productt = (User.Product) product;
				int brandId = productt.getBrandID();

				boolean isContainsKey = userItemsTable.containsKey(brandId);
				int itemWeight;
				if (isContainsKey) {
					itemWeight = getItemWeight(userItemsTable.get(brandId),
							productt.getType());
				} else {
					itemWeight = getItemWeight(0, productt.getType());
				}

				userItemsTable.put(brandId, itemWeight);
			}

			// form the resultItems list and sort by weight descend.
			Enumeration<Integer> key = userItemsTable.keys();
			while (key.hasMoreElements()) {
				int myk = key.nextElement();
				ItemWithWeight userWithItems = new ItemWithWeight();
				userWithItems.setProductID(myk);
				userWithItems.setWeight(userItemsTable.get(myk));
				tmpItems.add(userWithItems);
			}
			Collections.sort(tmpItems, new Comparator<ItemWithWeight>() {
				@Override
				public int compare(ItemWithWeight o1, ItemWithWeight o2) {
					return Integer.valueOf(o2.getWeight()).compareTo(
							o1.getWeight());
				}
			});

			// TODO Auto-generated catch block
			// int forecastItemN = usert.getWeight() / 26;
			int tmp[] = usert.getUserActionCount();
			int forecastItemN = tmp[1] + tmp[3];
			for (ItemWithWeight tmpItem : tmpItems) {

				// if (forecastItemN-- <= 0) break;
				resultItems.add(tmpItem.getProductID());
			}

			resultUserItems.put(usert.getUserID(), resultItems);
		}

		return resultUserItems;
	}

	public static LinkedList<Object> getUsersSimpl(String dayTime,
			String userActionType) throws SQLException {
		long begintime = System.currentTimeMillis();
		String sqlStat = null;

		try {
			Date date1 = MYSQLCONFIG.dateFormat
					.parse(MYSQLCONFIG.DateThreshold);
			Date date2 = MYSQLCONFIG.dateFormat.parse(dayTime);
			int com = date1.compareTo(date2);
			if (com > 0) {

				sqlStat = "select * from tmail_firstseason where visit_datetime <= "
						+ addDoubleQuote(MYSQLCONFIG.DateThreshold)
						+ " and visit_datetime > " + addDoubleQuote(dayTime);
			} else {

				sqlStat = "select * from tmail_firstseason where visit_datetime <= "
						+ addDoubleQuote(dayTime)
						+ " and visit_datetime > "
						+ addDoubleQuote(MYSQLCONFIG.DateThreshold);
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LinkedList<Object> users = new LinkedList<Object>();
		Hashtable<Integer, Object> userItemsTable = new Hashtable<Integer, Object>();

		java.sql.Statement statement = null;
		ResultSet resultSet = null;
		Connection connection = DriverManager.getConnection(MYSQLCONFIG.DBURL,
				MYSQLCONFIG.USRNAME, MYSQLCONFIG.PASSWORD);

		sqlStat = sqlStat + userActionType;
		// preparedStatement = connection.prepareStatement(sqlStat);
		statement = connection.createStatement();
		statement.executeQuery(sqlStat);
		resultSet = statement.getResultSet();

		while (resultSet.next()) {
			int user_id = resultSet.getInt(2);
			boolean isContainsKey = userItemsTable.containsKey(user_id);
			if (isContainsKey) {
				Object tmp = userItemsTable.get(user_id);
				User user = (User) tmp;

				User.Product product = user.new Product();
				product.setBrandID(resultSet.getInt(3));
				int type = resultSet.getInt(4);
				product.setType(type);
				product.setVisitDaytime(resultSet.getDate(5));
				user.setProducts(product);

				int userActionCount[] = user.getUserActionCount();
				userActionCount[type]++;
				user.setUserActionCount(userActionCount);

				int userActive = user.getWeight();
				userActive = getItemWeight(userActive, type);
				user.setWeight(userActive);

				userItemsTable.put(user.getUserID(), user);

			} else {

				int userActionCount[] = { 0, 0, 0, 0 };
				User user = new User();
				user.setUserID(user_id);
				User.Product product = user.new Product();
				product.setBrandID(resultSet.getInt(3));
				int type = resultSet.getInt(4);
				int userActive = getItemWeight(0, type);
				product.setType(type);
				product.setVisitDaytime(resultSet.getDate(5));
				userActionCount[type]++;
				user.setProducts(product);
				user.setUserActionCount(userActionCount);
				user.setWeight(userActive);

				userItemsTable.put(user.getUserID(), user);
			}

		}

		// form the resultItems list and sort by weight descend.
		Enumeration<Integer> key = userItemsTable.keys();
		while (key.hasMoreElements()) {
			int myk = key.nextElement();
			users.add(userItemsTable.get(myk));
		}

		logger.debug("load users compeletely");

		if (ETLCONFIG.ISDEBUGMODEL) {
			write2File(users);
		}

		connection.close();
		long endtime = System.currentTimeMillis();
		long costTime = (endtime - begintime) / 1000;
//		System.out.println("getUsersSimple function use seconds:" + costTime);
		logger.debug("getUsersSimple function use seconds:", costTime);
		return users;
	}

	public static LinkedList<Object> getUsers(int month, String userActionType)
			throws SQLException {
		long begintime = System.currentTimeMillis();
		// if (users != null) {
		// return users;
		// }
		// if the companies is null then make it happen
		LinkedList<Object> users = new LinkedList<Object>();
		LinkedList<Integer> userIds = new LinkedList<Integer>();
		// PreparedStatement preparedStatement;

		// String sqlStat =
		// "select user_id from tmail_firstseason group by user_id;";
		String sqlStat = "select * from tmail_firstseason where MONTH(visit_datetime) ="
				+ month + userActionType + "group by user_id;";

		java.sql.Statement statement = null;
		ResultSet resultSet = null;
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
					+ " and MONTH(visit_datetime) = " + month + userActionType;

			// preparedStatement = connection.prepareStatement(sqlStat);
			statement = connection.createStatement();
			statement.executeQuery(sqlStat);
			resultSet = statement.getResultSet();
			int userActionCount[] = { 0, 0, 0, 0 };
			int userActive = 0;

			User user = new User();

			while (resultSet.next()) {

				User.Product product = user.new Product();
				product.setBrandID(resultSet.getInt(3));
				int type = resultSet.getInt(4);
				userActive = getItemWeight(userActive, type);
				product.setType(resultSet.getInt(4));
				product.setVisitDaytime(resultSet.getDate(5));
				userActionCount[type]++;
				user.setProducts(product);

			}

			user.setUserID(userId);
			user.setUserActionCount(userActionCount);
			user.setWeight(userActive);

			double temp = (double) (userActionCount[1] + userActionCount[3])
					/ userActionCount[0];
			BigDecimal b = new BigDecimal(temp);
			// 小数取四位
			temp = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
			user.setClick2purchase(temp);

			users.add(user);
		}

		logger.debug("load users compeletely");

		if (ETLCONFIG.ISDEBUGMODEL) {
			write2File(users);
		}

		connection.close();
		long endtime = System.currentTimeMillis();
		long costTime = (endtime - begintime) / 1000;
		System.out.println("getUsers function use seconds:" + costTime);
		logger.debug("getUsers function use seconds:", costTime);
		return users;
	}

	/*
	 * 大赛给出的182,880条交易数据中， 总的点击行为次数为：174,539,占百分比为0.954390857；
	 * 总的购买行为次数为：6,984,占百分比为0.038188976；总的收藏行为次数为：1,204,占百分比为0.006583552；
	 * 总的购物车行为车次数为：153，占百分比为0.000836614;
	 */
	public static int getItemWeight(int iuserActive, int iType) {

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
			int clickCount = 0;
			int purchaseCount = 0;
			int FavoriteCount = 0;
			int ShopcartCount = 0;
			int itemPopular = 0;
			Item item = new Item();

			while (resultSet.next()) {

				Item.User user = item.new User();
				user.setBrandID(resultSet.getInt(3));
				int type = resultSet.getInt(4);
				user.setType(resultSet.getInt(4));
				user.setVisitDaytime(resultSet.getDate(5));

				itemPopular = getItemWeight(itemPopular, type);

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
				item.addUsers(user);

			}

			item.setProductID(itemId);
			item.setClickCount(clickCount);
			item.setPurchaseCount(purchaseCount);
			item.setFavoriteCount(FavoriteCount);
			item.setShopcartCount(ShopcartCount);

			if (clickCount != 0) {
				double temp = (double) (purchaseCount + ShopcartCount)
						/ clickCount;
				BigDecimal b = new BigDecimal(temp);
				// 小数取四位
				temp = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
				item.setClick2purchase(temp);
			} else {
				// 点击转化率设为平均值
				item.setClick2purchase(0.0390);
			}

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
		// write2File(items, 1);
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
	}

	// write the list to file
	private static void write2File(List<Object> items, boolean isAppendMode) {
		File file = new File(ETLCONFIG.TMPPATH
				+ items.get(0).getClass().getName());

		try {
			if (isAppendMode) {
				FileWriter writer = new FileWriter(file, true);
				for (Object object : items) {
					writer.write(object.toString() + "\n");
				}
				writer.close();
			} else {
				FileWriter writer = new FileWriter(file);
				for (Object object : items) {
					writer.write(object.toString() + "\n");
				}
				writer.close();
			}
		} catch (Exception e) {
			// ignore the exception
		}
	}

	public static HashMap<Integer, Integer> getHotItems() throws SQLException {
		java.sql.Statement statement = null;
		ResultSet resultSet = null;
		HashMap<Integer, Integer> itemsMap = new HashMap<Integer, Integer>();

		String sqlStat = "select * from item_sort where weight >=100 group by brand_id";
		Connection connection = DriverManager.getConnection(MYSQLCONFIG.DBURL,
				MYSQLCONFIG.USRNAME, MYSQLCONFIG.PASSWORD);

		statement = connection.createStatement();
		statement.executeQuery(sqlStat);
		resultSet = statement.getResultSet();
		while (resultSet.next()) {
			itemsMap.put(resultSet.getInt(2), resultSet.getInt(3));
		}

		return itemsMap;
	}

	// in order to add double quote:"2013-01-03"->""2013-01-03""
	private static String addDoubleQuote(String str) {

		StringBuilder ConnSQLStrBld = new StringBuilder();
		ConnSQLStrBld.append(str);
		ConnSQLStrBld.insert(ConnSQLStrBld.length(), '"');
		ConnSQLStrBld.insert(0, '"');
		return ConnSQLStrBld.toString();
	}

}
