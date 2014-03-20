package iot.lane.alipaycontest.firstseason;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.common.Pair;
import org.apache.mahout.fpm.pfpgrowth.convertors.string.TopKStringPatterns;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class DataAnalyze {

	// private static LinkedList<Object> users = null;
	// private static LinkedList<Object> items = null;
	static Logger logger = LogManager.getLogger(DataAnalyze.class.getName());
	static Hashtable<Integer, LinkedList<Integer>> myCompositeResultTable = new Hashtable<Integer, LinkedList<Integer>>();

	public static void main(String[] args) {
		try {
			LinkedList<Object> FPGrowthItems = getFPGrowth(751, null, "fList.seq",
					"frequentpatterns.seq", 0.001, 0.1);
//			saveDateToDatFile();
			appendLeast7DayBuying();
			appendCycleBuying();
			LinkedList<Object> myItems = getItemsSimpl("2013-04-15",
					MYSQLCONFIG.isOnlypurchaseAction);
			LinkedList<Object> myUsers = getUsersSimpl("2013-04-15",
					MYSQLCONFIG.isAllAction);
			if (ETLCONFIG.ISDEBUGMODEL) {
				write2File(myItems);
				write2File(myUsers);
			}
		} catch (Exception e2) {
			// do nothing
		}
		// LinkedList<Object> myStatistics = new LinkedList<Object>();
		LinkedList<StatisticsResultDate> myStatistics = new LinkedList<StatisticsResultDate>();
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(MYSQLCONFIG.dateFormat.parse(MYSQLCONFIG.DateThreshold));
		} catch (ParseException e1) {
			// do nothing
		}

		for (int i = 1; i <= 90; i++) {
			try {
				cal.add(Calendar.DAY_OF_YEAR, -1);
				StatisticsResultDate myStatistic = statisticsResult(MYSQLCONFIG.dateFormat
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

	public static void saveDateToDatFile() throws Exception {

		LinkedList<Object> IndexdByUseridItems = getUsersSimpl("2013-04-15",
				MYSQLCONFIG.isOnlypurchaseAction);

		Hashtable<Integer, LinkedList<Integer>> myTable = new Hashtable<Integer, LinkedList<Integer>>();
		for (Object IndexdByUseridItem : IndexdByUseridItems) {
			UserWithItemsDate IndexdByUseridItemt = (UserWithItemsDate) IndexdByUseridItem;
			LinkedList<Object> products = IndexdByUseridItemt.getProducts();
			for (Object product : products) {
				UserWithItemsDate.Product productt = (UserWithItemsDate.Product) product;
				add2myCompositeResultTable(myTable,
						IndexdByUseridItemt.getUserID(), productt.getBrandID());
			}
		}

		int transactionCount = 0;
		FileWriter datWriter = new FileWriter("output.dat");
		boolean isFirstElement = true;
		Enumeration<Integer> key = myTable.keys();
		while (key.hasMoreElements()) {
			int UserId = key.nextElement();
			LinkedList<Integer> items = myTable.get(UserId);
			isFirstElement = true;
			for (Integer item : items) {
				if (isFirstElement) {
					isFirstElement = false;
				} else {
					datWriter.append(",");
				}
				datWriter.append(item + "");
			}
			datWriter.append("\n");
			transactionCount++;
		}

		datWriter.close();
		System.out.println("Wrote " + transactionCount + " transactions.");

	}

	/**
	 * This method get least 7daybuying items
	 */
	public static void appendLeast7DayBuying() throws SQLException {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(MYSQLCONFIG.dateFormat.parse(MYSQLCONFIG.DateThreshold));
		} catch (ParseException e1) {
			// do nothing
		}
		// TODO least N days purchaseed items will add to predictTable
		cal.add(Calendar.DAY_OF_YEAR, -7);

		Hashtable<Integer, LinkedList<Integer>> userItemsTable = getPredictItems(
				MYSQLCONFIG.dateFormat.format(cal.getTime()),
				MYSQLCONFIG.isOnlypurchaseAction);

		Enumeration<Integer> key = userItemsTable.keys();
		while (key.hasMoreElements()) {
			int UserId = key.nextElement();
			LinkedList<Integer> items = userItemsTable.get(UserId);
			for (Integer item : items) {
				add2myCompositeResultTable(myCompositeResultTable, UserId, item);
			}
		}
	}

	/**
	 * This method get periodbuying items
	 */
	public static void appendCycleBuying() throws SQLException {
		LinkedList<Object> myUsers = getUsersSimpl("2013-04-15",
				MYSQLCONFIG.isOnlypurchaseAction);
		for (Object myUser : myUsers) {
			UserWithItemsDate myUsert = (UserWithItemsDate) myUser;
			LinkedList<Object> myItems = myUsert.getProducts();
			// Hashtable<Integer, Integer> periodUsersTable = new
			// Hashtable<Integer, Integer>();

			for (int i = 0; i < myItems.size(); i++) {
				UserWithItemsDate.Product itemI = (UserWithItemsDate.Product) myItems.get(i);
				for (int j = i + 1; j < myItems.size(); j++) {
					UserWithItemsDate.Product itemJ = (UserWithItemsDate.Product) myItems.get(j);
					if (itemI.getBrandID() == itemJ.getBrandID()) {

						long timeI = itemI.getVisitDaytime().getTime();
						long timeJ = itemJ.getVisitDaytime().getTime();
						// TODO 7 days interval is enough?
						if (Math.abs((timeI - timeJ) / (1000 * 60 * 60 * 24)) >= 7) {
							add2myCompositeResultTable(myCompositeResultTable,
									myUsert.getUserID(), itemI.getBrandID());
							break;
						}
					}
				}
			}
			// for (Object myItem : myItems) {
			// User.Product myItemt = (User.Product) myItem;
			//
			// boolean isAlreadyHas = periodUsersTable.containsKey(myItemt
			// .getBrandID());
			// if (isAlreadyHas) {
			// int N = periodUsersTable.get(myItemt.getBrandID());
			// periodUsersTable.put((Integer) myItemt.getBrandID(), ++N);
			// } else {
			// periodUsersTable.put((Integer) myItemt.getBrandID(), 1);
			// }
			// }
			//
			// // form the resultItems list and sort by weight descend.
			// Enumeration<Integer> key = periodUsersTable.keys();
			// while (key.hasMoreElements()) {
			// int myItemId = key.nextElement();
			// int myItemNumber = periodUsersTable.get(myItemId);
			// if (myItemNumber > 1) {
			// add2myCompositeResultTable(myUsert.getUserID(), myItemId);
			// }
			// // users.add(userItemsTable.get(myk));
			// }

		}

	}

	/**
	 * This method put every <key value> result to one big final hashtable，
	 * 
	 * @param userId
	 *            ，represent key
	 * @param brandId
	 *            ，represent values
	 */
	public static void add2myCompositeResultTable(
			Hashtable<Integer, LinkedList<Integer>> myTable, int userId,
			int brandId) throws SQLException {
		if (myTable.containsKey(userId)) {
			LinkedList<Integer> items = myTable.get(userId);
			boolean isContain = items.contains(brandId);
			if (false == isContain) {
				items.add(brandId);
				myTable.put(userId, items);
			}
		} else {
			LinkedList<Integer> items = new LinkedList<Integer>();
			items.add(brandId);
			myTable.put(userId, items);
		}
	}

	/**
	 * This method calculates the result's precision/recall/f1score.
	 * 
	 * @param dayTime
	 *            ,predictBuyItems daytime interval, between
	 *            MYSQLCONFIG.DateThreshold and dayTime,like from "2013-07-15"
	 *            to "2013-06-15"
	 * @return parameters PredictDateHolder, includes precision/recall/f1score
	 *         scores.
	 */
	public static StatisticsResultDate statisticsResult(String dayTime)
			throws SQLException {
		StatisticsResultDate predictBuy = new StatisticsResultDate();

		try {
			Hashtable<Integer, LinkedList<Integer>> predictBuyItemsNTable = getPredictItems(
					dayTime, MYSQLCONFIG.isAllAction);

			Calendar cal = Calendar.getInstance();
			try {
				cal.setTime(MYSQLCONFIG.dateFormat
						.parse(MYSQLCONFIG.DateThreshold));
			} catch (ParseException e) {
				// do nothing
			}
			cal.add(Calendar.MONTH, 1);
			LinkedList<Object> actualBuyNumItems = getUsersSimpl(
					MYSQLCONFIG.dateFormat.format(cal.getTime()),
					MYSQLCONFIG.isOnlypurchaseAction);

			// LinkedList<Object> StaticsUsers = new LinkedList<Object>();
			double allHitBrands = 0;
			double allpBrands = 0;
			double allbBrands = 0;

			for (Object actualBuyNumItem : actualBuyNumItems) {
				UserWithItemsDate usert = (UserWithItemsDate) actualBuyNumItem;
				int userId = usert.getUserID();
				// pBrandsi为对用户i 预测他(她)会购买的品牌列表个数
				double pBrands = 0;
				// bBrandsi为用户i 真实购买的品牌个数
				double bBrands = usert.getProducts().size();
				allbBrands += bBrands;
				// hitBrandsi对用户i预测的品牌列表与用户i真实购买的品牌交集的个数
				double hitBrands = 0;

				boolean isContainsKey = predictBuyItemsNTable
						.containsKey(userId);
				if (isContainsKey) {
					pBrands = predictBuyItemsNTable.get(userId).size();
					allpBrands += pBrands;
					for (Object product : usert.getProducts()) {
						UserWithItemsDate.Product productt = (UserWithItemsDate.Product) product;
						LinkedList<Integer> products = predictBuyItemsNTable
								.get(userId);
						boolean isContainThisProduct = products
								.contains(productt.getBrandID());
						if (isContainThisProduct) {
							hitBrands++;
							allHitBrands++;
						}
					}
				}
				// PredictBuyStatist staticsUser = new PredictBuyStatist();
				// double precision = hitBrands / pBrands;
				// double recall = hitBrands / bBrands;
				// double F1 = 2 * precision * recall / (recall + precision);
				// System.out.println("use_id:" + userId + "\tF1: " + F1
				// + "\tprecision: " + precision + "\tallprecision: "
				// + precision);

			}
			double allPrecision = allHitBrands / allpBrands;
			double allRecall = allHitBrands / allbBrands;
			double allF1 = 2 * allPrecision * allRecall
					/ (allRecall + allPrecision);

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

	/**
	 * This method get predict items from MYSQLCONFIG.DateThreshold to dayTime
	 * 
	 * @param dayTime
	 *            ,predict after this daytime.
	 * @param userActionType
	 *            ,predict to specific user action,like
	 *            onlyclick,onlypurchase,allaction.
	 * @return Hashtable<Integer, LinkedList<Integer>>, structure
	 *         like<userid1,(brandid1
	 *         ,brandid2...);userid2,(brandid1,brandid2...)...>
	 */
	public static Hashtable<Integer, LinkedList<Integer>> getPredictItems(
			String dayTime, String userActionType) throws SQLException {
		LinkedList<Object> users = getUsersSimpl(dayTime, userActionType);
		Hashtable<Integer, LinkedList<Integer>> resultUserItems = new Hashtable<Integer, LinkedList<Integer>>();

		for (Object user : users) {
			LinkedList<ItemOnlyWithWeightDate> tmpItems = new LinkedList<ItemOnlyWithWeightDate>();
			LinkedList<Integer> resultItems = new LinkedList<Integer>();
			UserWithItemsDate usert = (UserWithItemsDate) user;
			Hashtable<Integer, Integer> userItemsTable = new Hashtable<Integer, Integer>();

			for (Object product : usert.getProducts()) {
				UserWithItemsDate.Product productt = (UserWithItemsDate.Product) product;
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
				ItemOnlyWithWeightDate userWithItems = new ItemOnlyWithWeightDate();
				userWithItems.setProductID(myk);
				userWithItems.setWeight(userItemsTable.get(myk));
				tmpItems.add(userWithItems);
			}
			Collections.sort(tmpItems, new Comparator<ItemOnlyWithWeightDate>() {
				@Override
				public int compare(ItemOnlyWithWeightDate o1, ItemOnlyWithWeightDate o2) {
					return Integer.valueOf(o2.getWeight()).compareTo(
							o1.getWeight());
				}
			});

			// TODO Auto-generated catch block
			// int forecastItemN = usert.getWeight() / 26;
			int tmp[] = usert.getUserActionCount();
			int forecastItemN = tmp[1] + tmp[3];
			for (ItemOnlyWithWeightDate tmpItem : tmpItems) {

				// if (forecastItemN-- <= 0)break;
				resultItems.add(tmpItem.getProductID());
			}

			resultUserItems.put(usert.getUserID(), resultItems);
		}

		return resultUserItems;
	}

	/**
	 * This method get the date LinkedList<Object> structure indexed by userid.
	 * 
	 * @param dayTime
	 *            ,predict after this daytime.
	 * @param userActionType
	 *            ,predict to specific user action,like
	 *            onlyclick,onlypurchase,allaction.
	 * @return parameters LinkedList<Object>, structure like<userid1,(brandid1
	 *         ,brandid2...);userid2,(brandid1,brandid2...)...>
	 */
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
						+ " and visit_datetime >= " + addDoubleQuote(dayTime);
			} else {

				sqlStat = "select * from tmail_firstseason where visit_datetime <= "
						+ addDoubleQuote(dayTime)
						+ " and visit_datetime >= "
						+ addDoubleQuote(MYSQLCONFIG.DateThreshold);
			}

		} catch (ParseException e) {
			// do nothing
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
				UserWithItemsDate user = (UserWithItemsDate) tmp;

				UserWithItemsDate.Product product = user.new Product();
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
				UserWithItemsDate user = new UserWithItemsDate();
				user.setUserID(user_id);
				UserWithItemsDate.Product product = user.new Product();
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
		// System.out.println("getUsersSimple function use seconds:" +
		// costTime);
		logger.debug("getUsersSimple function use seconds:", costTime);
		return users;
	}

	/**
	 * This method get the date LinkedList<Object> structure indexed by brandid.
	 * 
	 * @param dayTime
	 *            ,predict after this daytime.
	 * @param userActionType
	 *            ,predict to specific user action,like
	 *            onlyclick,onlypurchase,allaction.
	 * @return parameters LinkedList<Object>, structure like<brandid1,(userid1
	 *         ,userid1...);brandid2,(userid1,userid1...)...>
	 */
	public static LinkedList<Object> getItemsSimpl(String dayTime,
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
						+ " and visit_datetime >= " + addDoubleQuote(dayTime);
			} else {

				sqlStat = "select * from tmail_firstseason where visit_datetime <= "
						+ addDoubleQuote(dayTime)
						+ " and visit_datetime >= "
						+ addDoubleQuote(MYSQLCONFIG.DateThreshold);
			}

		} catch (ParseException e) {
			// do nothing
		}

		LinkedList<Object> items = new LinkedList<Object>();
		Hashtable<Integer, Object> ItemsTable = new Hashtable<Integer, Object>();

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
			int brand_id = resultSet.getInt(3);
			boolean isContainsKey = ItemsTable.containsKey(brand_id);
			if (isContainsKey) {
				Object tmp = ItemsTable.get(brand_id);
				ItemWithUsersDate item = (ItemWithUsersDate) tmp;

				ItemWithUsersDate.User user = item.new User();
				user.setUserID(resultSet.getInt(2));
				int type = resultSet.getInt(4);
				user.setType(type);
				user.setVisitDaytime(resultSet.getDate(5));
				item.setUsers(user);

				int itemActionCount[] = item.getItemActionCount();
				itemActionCount[type]++;
				item.setItemActionCount(itemActionCount);

				int itemPopular = item.getWeight();
				itemPopular = getItemWeight(itemPopular, type);
				item.setWeight(itemPopular);

				ItemsTable.put(item.getProductID(), item);

			} else {

				int itemActionCount[] = { 0, 0, 0, 0 };
				ItemWithUsersDate item = new ItemWithUsersDate();
				ItemWithUsersDate.User user = item.new User();
				int type = resultSet.getInt(4);

				int userActive = getItemWeight(0, type);
				itemActionCount[type]++;
				user.setUserID(resultSet.getInt(2));
				user.setType(type);
				user.setVisitDaytime(resultSet.getDate(5));

				item.setWeight(userActive);
				item.setItemActionCount(itemActionCount);
				item.setProductID(brand_id);
				item.setUsers(user);

				ItemsTable.put(item.getProductID(), item);
			}

		}

		// form the resultItems list and sort by weight descend.
		Enumeration<Integer> key = ItemsTable.keys();
		while (key.hasMoreElements()) {
			int myk = key.nextElement();
			items.add(ItemsTable.get(myk));
		}

		logger.debug("load items compeletely");

		if (ETLCONFIG.ISDEBUGMODEL) {
			write2File(items);
		}

		connection.close();
		long endtime = System.currentTimeMillis();
		long costTime = (endtime - begintime) / 1000;
		// System.out.println("getUsersSimple function use seconds:" +
		// costTime);
		logger.debug("getUsersSimple function use seconds:", costTime);
		return items;
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

	// write to file with and without append model
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

	// write to excelFile
	public static void write2ExcelFile(LinkedList<StatisticsResultDate> datas) {
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
		for (StatisticsResultDate data : datas) {
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

	// get top100 items
	public static HashMap<Integer, Integer> getHotItems() throws SQLException {
		java.sql.Statement statement = null;
		ResultSet resultSet = null;
		HashMap<Integer, Integer> itemsMap = new HashMap<Integer, Integer>();

		// TODO
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

	
	public static Map<Integer, Long> readFrequency(Configuration configuration,
			String fileName) throws Exception {
		FileSystem fs = FileSystem.get(configuration);
		Reader frequencyReader = new SequenceFile.Reader(fs,
				new Path(fileName), configuration);
		Map<Integer, Long> frequency = new HashMap<Integer, Long>();
		Text key = new Text();
		LongWritable value = new LongWritable();
		while (frequencyReader.next(key, value)) {
			frequency.put(Integer.parseInt(key.toString()), value.get());
		}
		return frequency;
	}

	public static Map<Integer, String> readMapping(String fileName)
			throws Exception {
		Map<Integer, String> itemById = new HashMap<Integer, String>();
		BufferedReader csvReader = new BufferedReader(new FileReader(fileName));
		while (true) {
			String line = csvReader.readLine();
			if (line == null) {
				break;
			}

			String[] tokens = line.split(",", 2);
			itemById.put(Integer.parseInt(tokens[1]), tokens[0]);
		}
		return itemById;
	}

	public static LinkedList<Object> readFrequentPatterns(
			Configuration configuration, String fileName, int transactionCount,
			Map<Integer, Long> frequency, Map<Integer, String> itemById,
			double minSupport, double minConfidence) throws Exception {
		FileSystem fs = FileSystem.get(configuration);

		Reader frequentPatternsReader = new SequenceFile.Reader(fs, new Path(
				fileName), configuration);
		Text key = new Text();
		TopKStringPatterns value = new TopKStringPatterns();
		LinkedList<Object> Itemsccurrence = new LinkedList<Object>();

		while (frequentPatternsReader.next(key, value)) {
			long firstFrequencyItem = -1;
			String firstItemId = null;
			List<Pair<List<String>, Long>> patterns = value.getPatterns();
			int i = 0;
			for (Pair<List<String>, Long> pair : patterns) {
				List<String> itemList = pair.getFirst();
				Long occurrence = pair.getSecond();
				if (i == 0) {
					firstFrequencyItem = occurrence;
					firstItemId = itemList.get(0);
				} else {
					double support = (double) occurrence / transactionCount;
					double confidence = (double) occurrence
							/ firstFrequencyItem;
					if (support > minSupport && confidence > minConfidence) {
						List<String> listWithoutFirstItem = new ArrayList<String>();
						FPGrowthDate dateFPGrowth = new FPGrowthDate();

						// for (String itemId : itemList) {
						// if (!itemId.equals(firstItemId)) {
						// listWithoutFirstItem.add(itemById.get(Integer
						// .parseInt(itemId)));
						// dateFPGrowth.setToItems(Integer
						// .parseInt(itemId));
						// }
						// }
						//
						// String firstItem = itemById.get(Integer
						// .parseInt(firstItemId));
						dateFPGrowth
								.setFromItems(Integer.parseInt(firstItemId));
						dateFPGrowth.setConfidence(confidence);
						dateFPGrowth.setSupport(support);
						Itemsccurrence.add(dateFPGrowth);
						// listWithoutFirstItem.remove(firstItemId);
						System.out.printf("supp=%.3f, conf=%.3f", support,
								confidence);

						if (itemList.size() == 2) {
							// we can easily compute the lift and the conviction
							// for set of
							// size 2, so do it
							int otherItemId = -1;
							for (String itemId : itemList) {
								if (!itemId.equals(firstItemId)) {
									otherItemId = Integer.parseInt(itemId);
									break;
								}
							}
							long otherItemOccurrence = frequency
									.get(otherItemId);

							double lift = ((double) occurrence * transactionCount)
									/ (firstFrequencyItem * otherItemOccurrence);
							double conviction = (1.0 - (double) otherItemOccurrence
									/ transactionCount)
									/ (1.0 - confidence);
							System.out.printf(", lift=%.3f, conviction=%.3f",
									lift, conviction);
						}
						System.out.printf("\n");
					}
				}
				i++;
			}
		}
		frequentPatternsReader.close();
		return Itemsccurrence;
	}


	public static LinkedList<Object> getFPGrowth(int itemsN,
			String mappingFile, String frequencyFile,
			String frequentPatternsFile, double support, double confidence) {
		int transactionCount = itemsN;
		String mappingCsvFilename = mappingFile;
		String frequencyFilename = frequencyFile;
		String frequentPatternsFilename = frequentPatternsFile;
		double minSupport = support;
		double minConfidence = confidence;
		LinkedList<Object> FPGrowthItems = null;

		Map<Integer, String> itemById;
		try {
			if (null != mappingCsvFilename) {
				itemById = readMapping(mappingCsvFilename);
			} else {
				itemById = null;
			}
			Configuration configuration = new Configuration();
			Map<Integer, Long> frequency = readFrequency(configuration,
					frequencyFilename);
			FPGrowthItems = readFrequentPatterns(configuration,
					frequentPatternsFilename, transactionCount, frequency,
					itemById, minSupport, minConfidence);
		} catch (Exception e) {
			// do nothing
		}

		return FPGrowthItems;
	}
	
	// //
	// public static LinkedList<Object> getItems() throws SQLException {
	// if (items != null) {
	// return items;
	// }
	// // if the companies is null then make it happen
	// items = new LinkedList<Object>();
	// LinkedList<Integer> itemIds = new LinkedList<Integer>();
	// // PreparedStatement preparedStatement;
	// java.sql.Statement statement = null;
	// ResultSet resultSet = null;
	// String sqlStat =
	// "select brand_id from tmail_firstseason group by brand_id;";
	//
	// Connection connection = DriverManager.getConnection(MYSQLCONFIG.DBURL,
	// MYSQLCONFIG.USRNAME, MYSQLCONFIG.PASSWORD);
	//
	// statement = connection.createStatement();
	// statement.executeQuery(sqlStat);
	// resultSet = statement.getResultSet();
	// while (resultSet.next()) {
	// itemIds.add(resultSet.getInt(1));
	// }
	//
	// for (int itemId : itemIds) {
	// sqlStat = "select * from tmail_firstseason where brand_id="
	// + itemId + ";";
	// // preparedStatement = connection.prepareStatement(sqlStat);
	// statement = connection.createStatement();
	// statement.executeQuery(sqlStat);
	// resultSet = statement.getResultSet();
	// int clickCount = 0;
	// int purchaseCount = 0;
	// int FavoriteCount = 0;
	// int ShopcartCount = 0;
	// int itemPopular = 0;
	// Item item = new Item();
	//
	// while (resultSet.next()) {
	//
	// Item.User user = item.new User();
	// user.setBrandID(resultSet.getInt(3));
	// int type = resultSet.getInt(4);
	// user.setType(resultSet.getInt(4));
	// user.setVisitDaytime(resultSet.getDate(5));
	//
	// itemPopular = getItemWeight(itemPopular, type);
	//
	// switch (type) {
	// case 0: {
	// clickCount++;
	// }
	// break;
	//
	// case 1: {
	// purchaseCount++;
	// }
	// break;
	//
	// case 2: {
	// FavoriteCount++;
	// }
	// break;
	//
	// case 3: {
	// purchaseCount++;
	// // ShopcartCount++;
	// }
	// break;
	// }
	// item.addUsers(user);
	//
	// }
	//
	// item.setProductID(itemId);
	// item.setClickCount(clickCount);
	// item.setPurchaseCount(purchaseCount);
	// item.setFavoriteCount(FavoriteCount);
	// item.setShopcartCount(ShopcartCount);
	//
	// if (clickCount != 0) {
	// double temp = (double) (purchaseCount + ShopcartCount)
	// / clickCount;
	// BigDecimal b = new BigDecimal(temp);
	// // 小数取四位
	// temp = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
	// item.setClick2purchase(temp);
	// } else {
	// // 点击转化率设为平均值
	// item.setClick2purchase(0.0390);
	// }
	//
	// item.setWeight(itemPopular);
	// items.add(item);
	// }
	//
	// logger.debug("load users compeletely");
	//
	// if (ETLCONFIG.ISDEBUGMODEL) {
	// write2File(items);
	// }
	//
	// connection.close();
	//
	// return items;
	// }

	// public static LinkedList<Object> getUsers(int month, String
	// userActionType)
	// throws SQLException {
	// long begintime = System.currentTimeMillis();
	// // if (users != null) {
	// // return users;
	// // }
	// // if the companies is null then make it happen
	// LinkedList<Object> users = new LinkedList<Object>();
	// LinkedList<Integer> userIds = new LinkedList<Integer>();
	// // PreparedStatement preparedStatement;
	//
	// // String sqlStat =
	// // "select user_id from tmail_firstseason group by user_id;";
	// String sqlStat =
	// "select * from tmail_firstseason where MONTH(visit_datetime) ="
	// + month + userActionType + "group by user_id;";
	//
	// java.sql.Statement statement = null;
	// ResultSet resultSet = null;
	// Connection connection = DriverManager.getConnection(MYSQLCONFIG.DBURL,
	// MYSQLCONFIG.USRNAME, MYSQLCONFIG.PASSWORD);
	//
	// statement = connection.createStatement();
	// statement.executeQuery(sqlStat);
	// resultSet = statement.getResultSet();
	// while (resultSet.next()) {
	// userIds.add(resultSet.getInt(2));
	// }
	//
	// for (int userId : userIds) {
	// sqlStat = "select * from tmail_firstseason where user_id=" + userId
	// + " and MONTH(visit_datetime) = " + month + userActionType;
	//
	// // preparedStatement = connection.prepareStatement(sqlStat);
	// statement = connection.createStatement();
	// statement.executeQuery(sqlStat);
	// resultSet = statement.getResultSet();
	// int userActionCount[] = { 0, 0, 0, 0 };
	// int userActive = 0;
	//
	// DateUser user = new DateUser();
	//
	// while (resultSet.next()) {
	//
	// DateUser.Product product = user.new Product();
	// product.setBrandID(resultSet.getInt(3));
	// int type = resultSet.getInt(4);
	// userActive = getItemWeight(userActive, type);
	// product.setType(resultSet.getInt(4));
	// product.setVisitDaytime(resultSet.getDate(5));
	// userActionCount[type]++;
	// user.setProducts(product);
	//
	// }
	//
	// user.setUserID(userId);
	// user.setUserActionCount(userActionCount);
	// user.setWeight(userActive);
	//
	// double temp = (double) (userActionCount[1] + userActionCount[3])
	// / userActionCount[0];
	// BigDecimal b = new BigDecimal(temp);
	// // 小数取四位
	// temp = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
	// user.setClick2purchase(temp);
	//
	// users.add(user);
	// }
	//
	// logger.debug("load users compeletely");
	//
	// if (ETLCONFIG.ISDEBUGMODEL) {
	// write2File(users);
	// }
	//
	// connection.close();
	// long endtime = System.currentTimeMillis();
	// long costTime = (endtime - begintime) / 1000;
	// System.out.println("getUsers function use seconds:" + costTime);
	// logger.debug("getUsers function use seconds:", costTime);
	// return users;
	// }

}
