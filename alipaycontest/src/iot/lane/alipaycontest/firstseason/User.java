package iot.lane.alipaycontest.firstseason;

import java.util.LinkedList;

public class User {
	private int userID = 0;
	private double click2purchase = 0;
	private int clickCount = 0;
	private int purchaseCount = 0;
	private int FavoriteCount = 0;
	private int ShopcartCount = 0;
	private int weight = 0;

	private LinkedList<Object> products = new LinkedList<Object>();
	/**
	 * @return the click2purchase
	 */
	public double getClick2purchase() {
		return click2purchase;
	}

	/**
	 * @param click2purchase the click2purchase to set
	 */
	public void setClick2purchase(double click2purchase) {
		this.click2purchase = click2purchase;
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
	 * @return the userID
	 */
	public int getUserID() {
		return userID;
	}

	/**
	 * @return the weight
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * @return the products
	 */
	public LinkedList<Object> getProducts() {
		return products;
	}

	/**
	 * @param userID the userID to set
	 */
	public void setUserID(int userID) {
		this.userID = userID;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}

	/**
	 * @param products the products to set
	 */
	public void addProducts(Object products) {
		this.products.add(products);
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
		result = prime * result
				+ ((products == null) ? 0 : products.hashCode());
		result = prime * result + purchaseCount;
		result = prime * result + userID;
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
		User other = (User) obj;
		if (FavoriteCount != other.FavoriteCount)
			return false;
		if (ShopcartCount != other.ShopcartCount)
			return false;
		if (Double.doubleToLongBits(click2purchase) != Double
				.doubleToLongBits(other.click2purchase))
			return false;
		if (clickCount != other.clickCount)
			return false;
		if (products == null) {
			if (other.products != null)
				return false;
		} else if (!products.equals(other.products))
			return false;
		if (purchaseCount != other.purchaseCount)
			return false;
		if (userID != other.userID)
			return false;
		if (weight != other.weight)
			return false;
		return true;
	}



	// inner class.
	class Product {
		private int brandID = 0;
		private int type = 0;
		private java.sql.Date visitDaytime;


		/**
		 * @return the brandID
		 */
		public int getBrandID() {
			return brandID;
		}

		/**
		 * @return the type
		 */
		public int getType() {
			return type;
		}

		/**
		 * @return the visitDaytime
		 */
		public java.sql.Date getVisitDaytime() {
			return visitDaytime;
		}

		/**
		 * @param brandID the brandID to set
		 */
		public void setBrandID(int brandID) {
			this.brandID = brandID;
		}

		/**
		 * @param type the type to set
		 */
		public void setType(int type) {
			this.type = type;
		}

		/**
		 * @param visitDaytime the visitDaytime to set
		 */
		public void setVisitDaytime(java.sql.Date visitDaytime) {
			this.visitDaytime = visitDaytime;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + brandID;
			result = prime * result + type;
			result = prime * result
					+ ((visitDaytime == null) ? 0 : visitDaytime.hashCode());
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
			Product other = (Product) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (brandID != other.brandID)
				return false;
			if (type != other.type)
				return false;
			if (visitDaytime == null) {
				if (other.visitDaytime != null)
					return false;
			} else if (!visitDaytime.equals(other.visitDaytime))
				return false;
			return true;
		}


		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Product [brandID=" + brandID + ", type=" + type
					+ ", visitDaytime=" + visitDaytime + "]";
		}

		private User getOuterType() {
			return User.this;
		}

	}// end of inner class Product



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "User [userID=" + userID + ", click2purchase=" + click2purchase
				+ ", clickCount=" + clickCount + ", purchaseCount="
				+ purchaseCount + ", FavoriteCount=" + FavoriteCount
				+ ", ShopcartCount=" + ShopcartCount + ", weight=" + weight
				+ ", products=" + products + "]";
	}

	
}
