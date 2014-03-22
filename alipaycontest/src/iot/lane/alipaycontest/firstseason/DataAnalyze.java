package iot.lane.alipaycontest.firstseason;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.Map;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.Text;
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
	private static Logger logger = LogManager.getLogger(DataAnalyze.class
			.getName());
	private static Hashtable<Integer, UserWithPredictCountData> myUserBuyingItemsNPerMonth = null;
	private static Hashtable<Integer, LinkedList<Integer>> myFinalResultTable = new Hashtable<Integer, LinkedList<Integer>>();

	public static void main(String[] args) {
		try {
//			fPGrowthSaveDataToDatFile("2013-05-15");
			getUserbuyingItemsNPerMonth("2013-04-15");
			// /////////////////////write behind this Cutting
			// line////////////////////

//			 predictOptimizeResultTest();
//			 fPGrowthOptimizeResultTest();

			 Hashtable<Integer, LinkedList<Integer>> result2 =
					 leastDayNotBuyinyAdd2Table(1, myFinalResultTable);
			 StatisticsResultData leastDayNotBuyinyAdd2Table =
			 statisticsResult(result2);
			 System.out.printf(leastDayNotBuyinyAdd2Table.toString()+"\n");
			 
			 
			 Hashtable<Integer, LinkedList<Integer>> resultl =
			 leastDayBuyinyAdd2Table(8, myFinalResultTable);
			 StatisticsResultData leastDayBuyinyAdd2Table =
			 statisticsResult(resultl);
			System.out.printf(leastDayBuyinyAdd2Table.toString()+"\n");

			 Hashtable<Integer, LinkedList<Integer>> resultc =
			 cycleBuyinyAdd2Table("2013-04-15", myFinalResultTable);
			 StatisticsResultData mycycleBuyinyAdd2TableStatistic =
			 statisticsResult(resultc);
				System.out.printf(mycycleBuyinyAdd2TableStatistic.toString()+"\n");
			
			Hashtable<Integer, LinkedList<Integer>> resultf = fPGrowthFilterAdd2Table(
					723, "fList.seq", "frequentpatterns.seq", 0.002, 0.2,
					myFinalResultTable);
			StatisticsResultData myfPGrowthFilterAdd2TableStatistic = statisticsResult(resultf);
			System.out.printf(myfPGrowthFilterAdd2TableStatistic.toString()+"\n");

			StatisticsResultData myFinalStatistic = statisticsResult(myFinalResultTable);
			System.out.printf(myFinalStatistic.toString());
			
			writeResultToTXT(myFinalResultTable);
			int i = 0;

		} catch (Exception e2) {
			// do nothing
			e2.printStackTrace();

		}

	}

	public static void writeResultToTXT( Hashtable<Integer, LinkedList<Integer>> myResult) throws Exception {
        BufferedWriter out = new BufferedWriter(new FileWriter("seasonOne_lane_Mike_guanggen.txt"));
		Enumeration<Integer> key = myResult.keys();
		int userId = 0;
		while (key.hasMoreElements()) {
			userId = key.nextElement();
			LinkedList<Integer> items = myResult.get(userId);
			out.write(userId + " \t ");
			boolean isFirstItem = true;
			for (Integer item : items) {
				if(true == isFirstItem){
					out.write(item+"");
					isFirstItem = false;
				}else{
					out.write(" , " + item);
				}
				}
			out.write(" "+"\n");
			}
		out.close();
		int i = 0;
	}
	
	public static void predictOptimizeResultTest() throws Exception {
		// LinkedList<Object> myStatistics = new LinkedList<Object>();
		LinkedList<StatisticsResultData> myStatistics = new LinkedList<StatisticsResultData>();

		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(MYSQLCONFIG.dateFormat.parse(MYSQLCONFIG.DateThreshold));
		} catch (ParseException e1) {
			// do nothing
		}

		for (int i = 1; i <= 13; i++) {
			try {
				cal.add(Calendar.DAY_OF_YEAR, -7);
				Hashtable<Integer, LinkedList<Integer>> predictBuyItemsTable = getPredictItems(
						MYSQLCONFIG.dateFormat.format(cal.getTime()),
						MYSQLCONFIG.isAllAction);
				Hashtable<Integer, LinkedList<Integer>> mycycleBuyinyAdd2Table = cycleBuyinyAdd2Table(
						"2013-04-15", predictBuyItemsTable);

				Hashtable<Integer, LinkedList<Integer>> resultf = fPGrowthFilterAdd2Table(
						751, "fList.seq", "frequentpatterns.seq", 0.001, 0.1,
						predictBuyItemsTable);
				
				StatisticsResultData myStatistic = statisticsResult(predictBuyItemsTable);
				myStatistics.add(myStatistic);

			} catch (SQLException e) {
				// do nothing
			}
		}

		if (ETLCONFIG.ISDEBUGMODEL) {
			writeStatistics2ExcelFile(myStatistics);
		}

	}

	public static void fPGrowthOptimizeResultTest() throws Exception {

		Random r = new Random();
		LinkedList<StatisticsResultData> myFPGrowthStatistics = new LinkedList<StatisticsResultData>();
		for (int i = 0; i <= 1; i++) {
			// 生成[0,0.005)区间的小数
			double dRecallRandom = r.nextDouble() / 200;
			// System.out.printf("dRecallRandom %.4f", dRecallRandom);
			// 生成[0,1.0)区间的小数
			double dPrecisonRandom = r.nextDouble();

			// Hashtable<Integer, LinkedList<Integer>>
			// myappendFPGrowth2Table = appendFPGrowth2Table(
			// 686, null, "fList.seq", "frequentpatterns.seq", 0.005, 0.1);
			Hashtable<Integer, LinkedList<Integer>> myAppendFPGrowth2Table = fPGrowthFilterAdd2Table(
					686, "fList.seq", "frequentpatterns.seq", dRecallRandom,
					dPrecisonRandom, myFinalResultTable);

			StatisticsResultData myFPGrowthStatistic = statisticsResult(myAppendFPGrowth2Table);

			System.out
					.printf("dRecallRandom:%.4f dPrecisonRandom:%.4f Precison:%.4f recall:%.4f f1score:%.4f \n",
							dRecallRandom, dPrecisonRandom,
							myFPGrowthStatistic.getPrecision(),
							myFPGrowthStatistic.getRecall(),
							myFPGrowthStatistic.getF1Score());

			myFPGrowthStatistics.add(myFPGrowthStatistic);
		}
		if (ETLCONFIG.ISDEBUGMODEL) {
			writeStatistics2ExcelFile(myFPGrowthStatistics);
		}
	}

	/**
	 * This method get useridA next month buying products number
	 * 
	 * @param datTime
	 *            ，between datTime and MYSQLCONFIG.DateThreshold,we can
	 *            calculate the number of products that specific userid buying.
	 */
	public static Hashtable<Integer, UserWithPredictCountData> getUserbuyingItemsNPerMonth(
			String datTime) throws Exception {
		if (null != myUserBuyingItemsNPerMonth) {
			return myUserBuyingItemsNPerMonth;
		}

		myUserBuyingItemsNPerMonth = new Hashtable<Integer, UserWithPredictCountData>();
		int myMonthduration = getMonthduration(datTime,
				MYSQLCONFIG.DateThreshold);
		LinkedList<Object> IndexdByUseridItems = getUsersSimpl(datTime,
				MYSQLCONFIG.isOnlypurchaseAction);

		for (Object IndexdByUseridItem : IndexdByUseridItems) {
			UserWithPredictCountData myUserWithCount = new UserWithPredictCountData();
			int[] userbuyingItemsN = { 0, 0, 0 };
			UserWithItemsData IndexdByUseridItemt = (UserWithItemsData) IndexdByUseridItem;
			myUserWithCount.setUserID(IndexdByUseridItemt.getUserID());
			int[] UserActionCount = IndexdByUseridItemt.getUserActionCount();
			userbuyingItemsN[2] = (UserActionCount[1] + UserActionCount[3])
					/ myMonthduration + myMonthduration;
			myUserWithCount.setUserPredictCount(userbuyingItemsN);

			myUserBuyingItemsNPerMonth.put(IndexdByUseridItemt.getUserID(),
					myUserWithCount);
		}

		return myUserBuyingItemsNPerMonth;
	}

	public static int getMonthduration(String datTime1, String datTime2)
			throws Exception {
		Date d1 = MYSQLCONFIG.dateFormat.parse(datTime1);
		Date d2 = MYSQLCONFIG.dateFormat.parse(datTime2);
		long diff = d1.getTime() - d2.getTime();
		long months = diff / (1000 * 60 * 60 * 24 * 30);

		// make sure month equal or bigger than one
		if (months >= 2) {
			return Math.abs((int) months - 1);
		} else {
			return 1;
		}
	}

	/**
	 * This method get least 7daybuying items
	 */
	public static Hashtable<Integer, LinkedList<Integer>> leastDayBuyinyAdd2Table(
			int leastDayTimeNmuber,
			Hashtable<Integer, LinkedList<Integer>> myHashtable)
			throws SQLException {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(MYSQLCONFIG.dateFormat.parse(MYSQLCONFIG.DateThreshold));
		} catch (ParseException e1) {
			// do nothing
		}
		// TODO least N days purchaseed items will add to predictTable
		cal.add(Calendar.DAY_OF_YEAR, -leastDayTimeNmuber);

		Hashtable<Integer, LinkedList<Integer>> userItemsTable = getPredictItems(
				MYSQLCONFIG.dateFormat.format(cal.getTime()),
				MYSQLCONFIG.isOnlypurchaseAction);

		Enumeration<Integer> key = userItemsTable.keys();
		while (key.hasMoreElements()) {
			int userId = key.nextElement();
			int count = 0;
			LinkedList<Integer> items = userItemsTable.get(userId);
			for (Integer item : items) {
				boolean isAlreadyInThere = addResult2HashTable(myHashtable,
						userId, item, MYSQLCONFIG.isPredictType);
				if (false == isAlreadyInThere) {
					count++;
				}
			}

		}
		return userItemsTable;
	}
	
	/**
	 * This method get least 7daybuying items
	 */
	public static Hashtable<Integer, LinkedList<Integer>> leastDayNotBuyinyAdd2Table(
			int leastDayTimeNmuber,
			Hashtable<Integer, LinkedList<Integer>> myHashtable)
			throws SQLException {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(MYSQLCONFIG.dateFormat.parse(MYSQLCONFIG.DateThreshold));
		} catch (ParseException e1) {
			// do nothing
		}
		// TODO least N days purchaseed items will add to predictTable
		cal.add(Calendar.DAY_OF_YEAR, -leastDayTimeNmuber);

		Hashtable<Integer, LinkedList<Integer>> userItemsTable = getPredictItems(
				MYSQLCONFIG.dateFormat.format(cal.getTime()),
				MYSQLCONFIG.isOnlyClickAction);

		Enumeration<Integer> key = userItemsTable.keys();
		while (key.hasMoreElements()) {
			int userId = key.nextElement();
			int count = 0;
			LinkedList<Integer> items = userItemsTable.get(userId);
			for (Integer item : items) {
				boolean isAlreadyInThere = addResult2HashTable(myHashtable,
						userId, item, MYSQLCONFIG.isPredictType);
				if (false == isAlreadyInThere) {
					count++;
				}
			}

		}
		return userItemsTable;
	}

	/**
	 * This method get periodbuying items
	 */
	public static Hashtable<Integer, LinkedList<Integer>> cycleBuyinyAdd2Table(
			String dayTime, Hashtable<Integer, LinkedList<Integer>> myHashtable)
			throws SQLException {
		LinkedList<Object> myUsers = getUsersSimpl(dayTime,
				MYSQLCONFIG.isOnlypurchaseAction);
		Hashtable<Integer, LinkedList<Integer>> mycycleBuyinyTable = new Hashtable<Integer, LinkedList<Integer>>();
		for (Object myUser : myUsers) {
			UserWithItemsData myUsert = (UserWithItemsData) myUser;
			int userId = myUsert.getUserID();
			int count = 0;
			LinkedList<Object> myItems = myUsert.getProducts();

			for (int i = 0; i < myItems.size(); i++) {
				UserWithItemsData.Product itemI = (UserWithItemsData.Product) myItems
						.get(i);
				for (int j = i + 1; j < myItems.size(); j++) {
					UserWithItemsData.Product itemJ = (UserWithItemsData.Product) myItems
							.get(j);
					if (itemI.getBrandID() == itemJ.getBrandID()) {

						long timeI = itemI.getVisitDaytime().getTime();
						long timeJ = itemJ.getVisitDaytime().getTime();
						// TODO 7 days interval is enough?
						if (Math.abs((timeI - timeJ) / (1000 * 60 * 60 * 24)) >= 7) {
							addResult2HashTable(mycycleBuyinyTable,
									myUsert.getUserID(), itemI.getBrandID(),
									MYSQLCONFIG.isNilType);

							boolean isAlreadyInThere = addResult2HashTable(
									myHashtable, myUsert.getUserID(),
									itemI.getBrandID(),
									MYSQLCONFIG.isPredictType);
							if (false == isAlreadyInThere) {
								count++;
							}
							continue;
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
		return mycycleBuyinyTable;

	}

	/**
	 * This method put every <key value> result to one big final hashtable，
	 * 
	 * @param userId
	 *            ，represent key
	 * @param brandId
	 *            ，represent values
	 */
	public static boolean addResult2HashTable(
			Hashtable<Integer, LinkedList<Integer>> myTable, int userId,
			int brandId, int resultType) throws SQLException {
		boolean isAlreadyInThere = true;
		if (myTable.containsKey(userId)) {
			LinkedList<Integer> items = myTable.get(userId);
			boolean isContain = items.contains(brandId);
			if (false == isContain) {
				items.add(brandId);
				myTable.put(userId, items);
				isAlreadyInThere = false;
			}
		} else {
			LinkedList<Integer> items = new LinkedList<Integer>();
			items.add(brandId);
			myTable.put(userId, items);
			isAlreadyInThere = false;
		}
//
//		if (false == isAlreadyInThere && resultType != -1) {
//
//			UserWithPredictCountData myUserWithPredictCountData = myUserBuyingItemsNPerMonth
//					.get(userId);
//			int[] fPGrowthFilterItemsN = myUserWithPredictCountData
//					.getUserPredictCount();
//			fPGrowthFilterItemsN[resultType]++;
//			myUserWithPredictCountData
//					.setUserPredictCount(fPGrowthFilterItemsN);
//			myUserBuyingItemsNPerMonth.put(userId, myUserWithPredictCountData);
//		}

		return isAlreadyInThere;
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
	public static StatisticsResultData statisticsResult(
			Hashtable<Integer, LinkedList<Integer>> predictBuyItemsNTable)
			throws SQLException {
		StatisticsResultData hitedbuyingitemsStatistics = new StatisticsResultData();

		try {

			Calendar cal = Calendar.getInstance();
			try {
				cal.setTime(MYSQLCONFIG.dateFormat
						.parse(MYSQLCONFIG.DateThreshold));
			} catch (ParseException e) {
				// do nothing
			}
			cal.add(Calendar.MONTH, 1);
			LinkedList<Object> actualBuyItemsN = getUsersSimpl(
					MYSQLCONFIG.dateFormat.format(cal.getTime()),
					MYSQLCONFIG.isOnlypurchaseAction);

			// LinkedList<Object> StaticsUsers = new LinkedList<Object>();
			// allHitBrands对用户i预测的品牌列表与用户i真实购买的品牌交集的个数
			double allHitBrands = 0;
			// allpBrands为对用户i 预测他(她)会购买的品牌列表个数
			double allpBrands = 0;
			// allbBrands为用户i 真实购买的品牌个数
			double allbBrands = 0;

			for (Object actualBuyNumItem : actualBuyItemsN) {
				UserWithItemsData usert = (UserWithItemsData) actualBuyNumItem;
				int userId = usert.getUserID();
				allbBrands += usert.getProducts().size();

				boolean isContainsKey = predictBuyItemsNTable
						.containsKey(userId);
				if (isContainsKey) {
					allpBrands += predictBuyItemsNTable.get(userId).size();
					;
					for (Object product : usert.getProducts()) {
						UserWithItemsData.Product productt = (UserWithItemsData.Product) product;
						LinkedList<Integer> products = predictBuyItemsNTable
								.get(userId);
						boolean isContainThisProduct = products
								.contains(productt.getBrandID());
						if (isContainThisProduct) {
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

			hitedbuyingitemsStatistics.setPrecision(allPrecision);
			hitedbuyingitemsStatistics.setRecall(allRecall);
			hitedbuyingitemsStatistics.setF1Score(allF1);
			// predictBuy.setDayTime(dayTime);

			if (ETLCONFIG.ISDEBUGMODEL) {
				// write2File(StaticsUsers);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return hitedbuyingitemsStatistics;
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
			LinkedList<ItemOnlyWithWeightData> tmpItems = new LinkedList<ItemOnlyWithWeightData>();
			LinkedList<Integer> resultItems = new LinkedList<Integer>();
			UserWithItemsData usert = (UserWithItemsData) user;
			Hashtable<Integer, Integer> userItemsTable = new Hashtable<Integer, Integer>();

			for (Object product : usert.getProducts()) {
				UserWithItemsData.Product productt = (UserWithItemsData.Product) product;
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
				ItemOnlyWithWeightData userWithItems = new ItemOnlyWithWeightData();
				userWithItems.setProductID(myk);
				userWithItems.setWeight(userItemsTable.get(myk));
				tmpItems.add(userWithItems);
			}
			Collections.sort(tmpItems,
					new Comparator<ItemOnlyWithWeightData>() {
						@Override
						public int compare(ItemOnlyWithWeightData o1,
								ItemOnlyWithWeightData o2) {
							return Integer.valueOf(o2.getWeight()).compareTo(
									o1.getWeight());
						}
					});

			// TODO
			// int forecastItemN = usert.getWeight() / 26;
			int tmp[] = usert.getUserActionCount();
			int forecastItemN = tmp[1] + tmp[3];
			for (ItemOnlyWithWeightData tmpItem : tmpItems) {

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
				UserWithItemsData user = (UserWithItemsData) tmp;

				UserWithItemsData.Product product = user.new Product();
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
				UserWithItemsData user = new UserWithItemsData();
				user.setUserID(user_id);
				UserWithItemsData.Product product = user.new Product();
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

	// write to excelFile
	public static void writeStatistics2ExcelFile(
			LinkedList<StatisticsResultData> datas) {
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
		for (StatisticsResultData data : datas) {
			int cellnum = 0;
			row = sheet.createRow(rownum++);
			cell = row.createCell(cellnum++);
			cell.setCellValue(data.getPrecision());
			cell = row.createCell(cellnum++);
			cell.setCellValue(data.getRecall());
			cell = row.createCell(cellnum++);
			cell.setCellValue(data.getF1Score());
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

	// in order to add double quote:"2013-01-03"->""2013-01-03""
	private static String addDoubleQuote(String str) {

		StringBuilder ConnSQLStrBld = new StringBuilder();
		ConnSQLStrBld.append(str);
		ConnSQLStrBld.insert(ConnSQLStrBld.length(), '"');
		ConnSQLStrBld.insert(0, '"');
		return ConnSQLStrBld.toString();
	}

	/**
	 * This method write fPGrowth recommend items to user hashtalbe
	 * 
	 * @param configuration
	 * @param frequentPatternsFileName
	 * @param transactionCount
	 *            ,overall users's number who buying products.
	 * @param frequency
	 * @param minSupport
	 * @param minConfidence
	 * @return parameters Hashtable<Integer, LinkedList<Integer>>
	 */
	public static Hashtable<Integer, LinkedList<Integer>> fPGrowthFilterAdd2Table(
			int itemsN, String frequencyFile, String frequentPatternsFile,
			double support, double confidence,
			Hashtable<Integer, LinkedList<Integer>> myHashtable) {

		Hashtable<Integer, LinkedList<Integer>> myFPGrowthUsersTable = new Hashtable<Integer, LinkedList<Integer>>();
		Hashtable<Integer, LinkedList<Integer>> myFPGrowthItemsTable = null;
		LinkedList<Object> UserHasAlreadyBuyitems = null;
		try {
			UserHasAlreadyBuyitems = getUsersSimpl("2013-04-15",
					MYSQLCONFIG.isOnlypurchaseAction);

			Configuration configuration = new Configuration();
			Map<Integer, Long> frequency = fPGrowthFilterReadFrequency(
					configuration, frequencyFile);
			myFPGrowthItemsTable = fPGrowthFilterReadFrequentPatterns(
					configuration, frequentPatternsFile, itemsN, frequency,
					support, confidence);

			for (Object UserHasAlreadyBuyitem : UserHasAlreadyBuyitems) {
				UserWithItemsData UserHasAlreadyBuyitemt = (UserWithItemsData) UserHasAlreadyBuyitem;
				int userId = UserHasAlreadyBuyitemt.getUserID();
				int count = 0;

				for (Object product : UserHasAlreadyBuyitemt.getProducts()) {
					UserWithItemsData.Product productt = (UserWithItemsData.Product) product;
					if (myFPGrowthItemsTable.containsKey(productt.getBrandID())) {
						
						 addResult2HashTable(myFPGrowthUsersTable, userId,
						 productt.getBrandID(), MYSQLCONFIG.isNilType);
						 
						 addResult2HashTable(myHashtable, userId,
								 productt.getBrandID(),
								 MYSQLCONFIG.isFPGrowthType);
						 
//						LinkedList<Integer> items = myFPGrowthItemsTable
//								.get(productt.getBrandID());

//						for (Integer item : items) {
//							addResult2HashTable(myFPGrowthUsersTable, userId,
//									item, MYSQLCONFIG.isNilType);
//
//							// add to myOverallResult hashtable
//							boolean isAlreadyInThere = addResult2HashTable(
//									myHashtable, userId, item,
//									MYSQLCONFIG.isFPGrowthType);
//
//							if (false == isAlreadyInThere) {
//								count++;
//							}
//							 addResult2HashTable(myFPGrowthUsersTable, userId,
//							 productt.getBrandID(), MYSQLCONFIG.isNilType);
//							
//							 // add to myOverallResult hashtable
//							 boolean isAlreadyInThere = addResult2HashTable(
//							 myHashtable, userId,
//							 productt.getBrandID(),
//							 MYSQLCONFIG.isFPGrowthType);
//							
//							 if (false == isAlreadyInThere) {
//							 count++;
//							 }
//						}
					}
				}

			}
		} catch (Exception e) {
			// do nothing
		}

		return myFPGrowthUsersTable;
	}

	/**
	 * This method get the Frequency Patterns.
	 * 
	 * @param configuration
	 * @param frequentPatternsFileName
	 * @param transactionCount
	 *            ,overall users's number who buying products.
	 * @param frequency
	 * @param minSupport
	 * @param minConfidence
	 * @return parameters Hashtable<Integer, LinkedList<Integer>>
	 */
	public static Hashtable<Integer, LinkedList<Integer>> fPGrowthFilterReadFrequentPatterns(
			Configuration configuration, String frequentPatternsFileName,
			int transactionCount, Map<Integer, Long> frequency,
			double minSupport, double minConfidence) throws Exception {
		FileSystem fs = FileSystem.get(configuration);

		Reader frequentPatternsReader = new SequenceFile.Reader(fs, new Path(
				frequentPatternsFileName), configuration);
		Text key = new Text();
		TopKStringPatterns value = new TopKStringPatterns();
		Hashtable<Integer, LinkedList<Integer>> myreadFrequentPatternsTable = new Hashtable<Integer, LinkedList<Integer>>();
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

						for (String itemId : itemList) {
							if (!itemId.equals(firstItemId)) {

								addResult2HashTable(
										myreadFrequentPatternsTable,
										Integer.parseInt(firstItemId),
										Integer.parseInt(itemId),
										MYSQLCONFIG.isNilType);
							}
						}

					}
				}
				i++;
			}
		}
		frequentPatternsReader.close();
		return myreadFrequentPatternsTable;
	}

	/**
	 * This method get the Frequency
	 * items.like{[brand_idA,3],[brand_idB,8].....}
	 * 
	 * @param configuration
	 * @param frequecnyFileName
	 *            ,fList.seq file name
	 * @return parameters Map<Integer, Long>, structure
	 *         like{[brand_idA,3],[brand_idB,8].....}
	 */
	public static Map<Integer, Long> fPGrowthFilterReadFrequency(
			Configuration configuration, String frequecnyFileName)
			throws Exception {
		FileSystem fs = FileSystem.get(configuration);
		Reader frequencyReader = new SequenceFile.Reader(fs, new Path(
				frequecnyFileName), configuration);
		Map<Integer, Long> frequency = new HashMap<Integer, Long>();
		Text key = new Text();
		LongWritable value = new LongWritable();
		while (frequencyReader.next(key, value)) {
			frequency.put(Integer.parseInt(key.toString()), value.get());
		}
		return frequency;
	}

	/**
	 * This method get data indexd by userid and save it to output.dat file
	 * 
	 * @param null
	 * @return parameters null
	 */
	public static void fPGrowthSaveDataToDatFile(String dayTime) throws Exception {

		LinkedList<Object> IndexdByUseridItems = getUsersSimpl(dayTime,
				MYSQLCONFIG.isOnlypurchaseAction);

		Hashtable<Integer, LinkedList<Integer>> myTable = new Hashtable<Integer, LinkedList<Integer>>();
		for (Object IndexdByUseridItem : IndexdByUseridItems) {
			UserWithItemsData IndexdByUseridItemt = (UserWithItemsData) IndexdByUseridItem;
			LinkedList<Object> products = IndexdByUseridItemt.getProducts();
			for (Object product : products) {
				UserWithItemsData.Product productt = (UserWithItemsData.Product) product;
				addResult2HashTable(myTable, IndexdByUseridItemt.getUserID(),
						productt.getBrandID(), MYSQLCONFIG.isNilType);
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

	// /**
	// * This method get the date LinkedList<Object> structure indexed by
	// brandid.
	// *
	// * @param dayTime
	// * ,predict after this daytime.
	// * @param userActionType
	// * ,predict to specific user action,like
	// * onlyclick,onlypurchase,allaction.
	// * @return parameters LinkedList<Object>, structure like<brandid1,(userid1
	// * ,userid1...);brandid2,(userid1,userid1...)...>
	// */
	// public static LinkedList<Object> getItemsSimpl(String dayTime,
	// String userActionType) throws SQLException {
	// long begintime = System.currentTimeMillis();
	// String sqlStat = null;
	//
	// try {
	// Date date1 = MYSQLCONFIG.dateFormat
	// .parse(MYSQLCONFIG.DateThreshold);
	// Date date2 = MYSQLCONFIG.dateFormat.parse(dayTime);
	// int com = date1.compareTo(date2);
	// if (com > 0) {
	//
	// sqlStat = "select * from tmail_firstseason where visit_datetime <= "
	// + addDoubleQuote(MYSQLCONFIG.DateThreshold)
	// + " and visit_datetime >= " + addDoubleQuote(dayTime);
	// } else {
	//
	// sqlStat = "select * from tmail_firstseason where visit_datetime <= "
	// + addDoubleQuote(dayTime)
	// + " and visit_datetime >= "
	// + addDoubleQuote(MYSQLCONFIG.DateThreshold);
	// }
	//
	// } catch (ParseException e) {
	// // do nothing
	// }
	//
	// LinkedList<Object> items = new LinkedList<Object>();
	// Hashtable<Integer, Object> ItemsTable = new Hashtable<Integer, Object>();
	//
	// java.sql.Statement statement = null;
	// ResultSet resultSet = null;
	// Connection connection = DriverManager.getConnection(MYSQLCONFIG.DBURL,
	// MYSQLCONFIG.USRNAME, MYSQLCONFIG.PASSWORD);
	//
	// sqlStat = sqlStat + userActionType;
	// // preparedStatement = connection.prepareStatement(sqlStat);
	// statement = connection.createStatement();
	// statement.executeQuery(sqlStat);
	// resultSet = statement.getResultSet();
	//
	// while (resultSet.next()) {
	// int brand_id = resultSet.getInt(3);
	// boolean isContainsKey = ItemsTable.containsKey(brand_id);
	// if (isContainsKey) {
	// Object tmp = ItemsTable.get(brand_id);
	// ItemWithUsersData item = (ItemWithUsersData) tmp;
	//
	// ItemWithUsersData.User user = item.new User();
	// user.setUserID(resultSet.getInt(2));
	// int type = resultSet.getInt(4);
	// user.setType(type);
	// user.setVisitDaytime(resultSet.getDate(5));
	// item.setUsers(user);
	//
	// int itemActionCount[] = item.getItemActionCount();
	// itemActionCount[type]++;
	// item.setItemActionCount(itemActionCount);
	//
	// int itemPopular = item.getWeight();
	// itemPopular = getItemWeight(itemPopular, type);
	// item.setWeight(itemPopular);
	//
	// ItemsTable.put(item.getProductID(), item);
	//
	// } else {
	//
	// int itemActionCount[] = { 0, 0, 0, 0 };
	// ItemWithUsersData item = new ItemWithUsersData();
	// ItemWithUsersData.User user = item.new User();
	// int type = resultSet.getInt(4);
	//
	// int userActive = getItemWeight(0, type);
	// itemActionCount[type]++;
	// user.setUserID(resultSet.getInt(2));
	// user.setType(type);
	// user.setVisitDaytime(resultSet.getDate(5));
	//
	// item.setWeight(userActive);
	// item.setItemActionCount(itemActionCount);
	// item.setProductID(brand_id);
	// item.setUsers(user);
	//
	// ItemsTable.put(item.getProductID(), item);
	// }
	//
	// }
	//
	// // form the resultItems list and sort by weight descend.
	// Enumeration<Integer> key = ItemsTable.keys();
	// while (key.hasMoreElements()) {
	// int myk = key.nextElement();
	// items.add(ItemsTable.get(myk));
	// }
	//
	// logger.debug("load items compeletely");
	//
	// if (ETLCONFIG.ISDEBUGMODEL) {
	// write2File(items);
	// }
	//
	// connection.close();
	// long endtime = System.currentTimeMillis();
	// long costTime = (endtime - begintime) / 1000;
	// // System.out.println("getUsersSimple function use seconds:" +
	// // costTime);
	// logger.debug("getUsersSimple function use seconds:", costTime);
	// return items;
	// }
	//
	// // write to file with and without append model
	// private static void write2File(List<Object> items, boolean isAppendMode)
	// {
	// File file = new File(ETLCONFIG.TMPPATH
	// + items.get(0).getClass().getName());
	//
	// try {
	// if (isAppendMode) {
	// FileWriter writer = new FileWriter(file, true);
	// for (Object object : items) {
	// writer.write(object.toString() + "\n");
	// }
	// writer.close();
	// } else {
	// FileWriter writer = new FileWriter(file);
	// for (Object object : items) {
	// writer.write(object.toString() + "\n");
	// }
	// writer.close();
	// }
	// } catch (Exception e) {
	// // ignore the exception
	// }
	// }

	// // get top100 items
	// public static HashMap<Integer, Integer> getHotItems() throws SQLException
	// {
	// java.sql.Statement statement = null;
	// ResultSet resultSet = null;
	// HashMap<Integer, Integer> itemsMap = new HashMap<Integer, Integer>();
	//
	// // TODO
	// String sqlStat =
	// "select * from item_sort where weight >=100 group by brand_id";
	// Connection connection = DriverManager.getConnection(MYSQLCONFIG.DBURL,
	// MYSQLCONFIG.USRNAME, MYSQLCONFIG.PASSWORD);
	//
	// statement = connection.createStatement();
	// statement.executeQuery(sqlStat);
	// resultSet = statement.getResultSet();
	// while (resultSet.next()) {
	// itemsMap.put(resultSet.getInt(2), resultSet.getInt(3));
	// }
	//
	// return itemsMap;
	// }

	// /**
	// * This method get the brand name which mapping to brand_id
	// *
	// * @param mappingFileName
	// * ,mapping File in which there are structures like {[brand_idA,brand
	// nameA],[brand_idB,brand nameB].....}
	// * @return parameters Map<Integer, String>
	// */
	// public static Map<Integer, String> fPGrowthReadMapping(String
	// mappingFileName)
	// throws Exception {
	// Map<Integer, String> itemById = new HashMap<Integer, String>();
	// BufferedReader csvReader = new BufferedReader(new
	// FileReader(mappingFileName));
	// while (true) {
	// String line = csvReader.readLine();
	// if (line == null) {
	// break;
	// }
	//
	// String[] tokens = line.split(",", 2);
	// itemById.put(Integer.parseInt(tokens[1]), tokens[0]);
	// }
	// return itemById;
	// }
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
