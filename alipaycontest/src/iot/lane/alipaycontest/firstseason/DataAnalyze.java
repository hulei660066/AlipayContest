package iot.lane.alipaycontest.firstseason;

import java.sql.SQLException;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;

public class DataAnalyze {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();

	}

	public static void test() {
		DataETL myDataETL = new DataETL();

		try {

			Collections.sort(myDataETL.getItems(), new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					User u1 = (User) o1;
					User u2 = (User) o2;
					return Collator.getInstance().compare(
							u1.getWeight(), u2.getWeight());
				}
			});

			int i = 1;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
}
