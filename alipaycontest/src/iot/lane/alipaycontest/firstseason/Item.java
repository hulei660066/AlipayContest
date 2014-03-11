package iot.lane.alipaycontest.firstseason;

import java.util.LinkedList;

public class Item {
	private int productID = 0;
	private int dealCount = 0;
	private int weight = 0;

	private LinkedList<Object> users = new LinkedList<Object>();


	/**
	 * @return the productID
	 */
	public int getProductID() {
		return productID;
	}

	/**
	 * @return the dealCount
	 */
	public int getDealCount() {
		return dealCount;
	}

	/**
	 * @return the weight
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * @return the users
	 */
	public LinkedList<Object> getUsers() {
		return users;
	}

	/**
	 * @param productID the productID to set
	 */
	public void setProductID(int productID) {
		this.productID = productID;
	}

	/**
	 * @param dealCount the dealCount to set
	 */
	public void setDealCount(int dealCount) {
		this.dealCount = dealCount;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}

	/**
	 * @param users the users to set
	 */
	public void addUsers(Object user) {
		this.users.add(user);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dealCount;
		result = prime * result + productID;
		result = prime * result + ((users == null) ? 0 : users.hashCode());
		result = prime * result + weight;
		return result;
	}

	/* (non-Javadoc)
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
		Item other = (Item) obj;
		if (dealCount != other.dealCount)
			return false;
		if (productID != other.productID)
			return false;
		if (users == null) {
			if (other.users != null)
				return false;
		} else if (!users.equals(other.users))
			return false;
		if (weight != other.weight)
			return false;
		return true;
	}


	// inner class.
	class User {
		private int userID = 0;
		private int type = 0;
		private java.sql.Date visitDaytime;

		void setBrandID(int iBrandID) {
			this.userID = iBrandID;
		}

		int getBrandID() {
			return userID;
		}

		void setType(int iType) {
			this.type = iType;
		}

		int getType() {
			return type;
		}

		void setVisitDaytime(java.sql.Date iVisitDaytime) {
			this.visitDaytime = iVisitDaytime;
		}

		java.sql.Date getVisitDaytime() {
			return visitDaytime;
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
			result = prime * result + userID;
			result = prime * result + type;
			result = prime * result + visitDaytime.hashCode();
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
			if (!(obj instanceof User))
				return false;
			User other = (User) obj;
			if (userID != other.userID)
				return false;
			if (type != other.type)
				return false;
			if (!visitDaytime.toString().equals(other.visitDaytime.toString()))
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
			return "User [userID=" + userID + ", type=" + type
					+ ", visitDaytime=" + visitDaytime + "]";
		}

	}// end of inner class Product


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Item [productID=" + productID + ", dealCount=" + dealCount
				+ ", weight=" + weight + ", users=" + users + "]";
	}

}
