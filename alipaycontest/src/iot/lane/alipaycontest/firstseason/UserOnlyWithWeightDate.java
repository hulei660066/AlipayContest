package iot.lane.alipaycontest.firstseason;

import java.util.LinkedList;

public class UserOnlyWithWeightDate {

	private int userID = 0;
	private LinkedList<Object> ItemWithWeights = new LinkedList<Object>();

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UserWithItems [userID=" + userID + ", ItemWithWeights="
				+ ItemWithWeights + "]";
	}

	/**
	 * @return the userID
	 */
	public int getUserID() {
		return userID;
	}

	/**
	 * @return the itemWithWeights
	 */
	public LinkedList<Object> getItemWithWeights() {
		return ItemWithWeights;
	}

	/**
	 * @param userID the userID to set
	 */
	public void setUserID(int userID) {
		this.userID = userID;
	}

	/**
	 * @param itemWithWeights the itemWithWeights to set
	 */
	public void setItemWithWeights(LinkedList<Object> itemWithWeights) {
		ItemWithWeights = itemWithWeights;
	}

	public class ItemWithWeight {
		private int productID = 0;
		private int weight = 0;

		/**
		 * @return the productID
		 */
		public int getProductID() {
			return productID;
		}

		/**
		 * @return the weight
		 */
		public int getWeight() {
			return weight;
		}

		/**
		 * @param productID
		 *            the productID to set
		 */
		public void setProductID(int productID) {
			this.productID = productID;
		}

		/**
		 * @param weight
		 *            the weight to set
		 */
		public void setWeight(int weight) {
			this.weight = weight;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + productID;
			result = prime * result + weight;
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ItemWithWeight other = (ItemWithWeight) obj;
			if (productID != other.productID)
				return false;
			if (weight != other.weight)
				return false;
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "ItemWithWeight [productID=" + productID + ", weight="
					+ weight + "]";
		}
	}
}
