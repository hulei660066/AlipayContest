package iot.lane.alipaycontest.firstseason;

import java.util.LinkedList;

public class Item {
	private int productID = 0;
	private double click2purchase = 0;
	private int clickCount = 0;
	private int purchaseCount = 0;
	private int FavoriteCount = 0;
	private int ShopcartCount = 0;
	private int weight = 0;

	private LinkedList<Object> users = new LinkedList<Object>();


	/**
	 * @return the click2purchase
	 */
	public double getClick2purchase() {
		return click2purchase;
	}

	/**
	 * @return the clickCount
	 */
	public int getClickCount() {
		return clickCount;
	}

	/**
	 * @return the purchaseCount
	 */
	public int getPurchaseCount() {
		return purchaseCount;
	}

	/**
	 * @return the favoriteCount
	 */
	public int getFavoriteCount() {
		return FavoriteCount;
	}

	/**
	 * @return the shopcartCount
	 */
	public int getShopcartCount() {
		return ShopcartCount;
	}

	/**
	 * @param click2purchase the click2purchase to set
	 */
	public void setClick2purchase(double click2purchase) {
		this.click2purchase = click2purchase;
	}

	/**
	 * @param clickCount the clickCount to set
	 */
	public void setClickCount(int clickCount) {
		this.clickCount = clickCount;
	}

	/**
	 * @param purchaseCount the purchaseCount to set
	 */
	public void setPurchaseCount(int purchaseCount) {
		this.purchaseCount = purchaseCount;
	}

	/**
	 * @param favoriteCount the favoriteCount to set
	 */
	public void setFavoriteCount(int favoriteCount) {
		FavoriteCount = favoriteCount;
	}

	/**
	 * @param shopcartCount the shopcartCount to set
	 */
	public void setShopcartCount(int shopcartCount) {
		ShopcartCount = shopcartCount;
	}

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
		result = prime * result + FavoriteCount;
		result = prime * result + ShopcartCount;
		long temp;
		temp = Double.doubleToLongBits(click2purchase);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + clickCount;
		result = prime * result + productID;
		result = prime * result + purchaseCount;
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
		if (FavoriteCount != other.FavoriteCount)
			return false;
		if (ShopcartCount != other.ShopcartCount)
			return false;
		if (Double.doubleToLongBits(click2purchase) != Double
				.doubleToLongBits(other.click2purchase))
			return false;
		if (clickCount != other.clickCount)
			return false;
		if (productID != other.productID)
			return false;
		if (purchaseCount != other.purchaseCount)
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
		return "Item [productID=" + productID + ", click2purchase="
				+ click2purchase + ", clickCount=" + clickCount
				+ ", purchaseCount=" + purchaseCount + ", FavoriteCount="
				+ FavoriteCount + ", ShopcartCount=" + ShopcartCount
				+ ", weight=" + weight + ", users=" + users + "]";
	}



}
