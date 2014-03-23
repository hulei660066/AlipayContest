package iot.lane.alipaycontest.firstseason;

import java.util.Arrays;
import java.util.LinkedList;

public class UserWithPredictCountData {

	private int userID = 0;
	//第一个参数：预测下个月购买个数；第二个参数：推荐下个月购买个数；第三个参数：下个月购买总个数，比例应该为：3：1：4
	private int userPredictCount[] = { 0, 0, 0 };
	
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	public int[] getUserPredictCount() {
		return userPredictCount;
	}
	public void setUserPredictCount(int[] userPredictCount) {
		this.userPredictCount = userPredictCount;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + userID;
		result = prime * result + Arrays.hashCode(userPredictCount);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserWithPredictCountData other = (UserWithPredictCountData) obj;
		if (userID != other.userID)
			return false;
		if (!Arrays.equals(userPredictCount, other.userPredictCount))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "UserWithPredictCountDate [userID=" + userID
				+ ", userPredictCount=" + Arrays.toString(userPredictCount)
				+ "]";
	}

	
}
