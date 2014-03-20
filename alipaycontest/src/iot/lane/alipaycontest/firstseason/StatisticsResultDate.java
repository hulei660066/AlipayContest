package iot.lane.alipaycontest.firstseason;


public class StatisticsResultDate {

	private int dayNumber = 0;
	private double precision = 0;
	private double recall = 0;
	private double F1Score = 0;
	private String dayTime = null;



	/**
	 * @return the dayTime
	 */
	public String getDayTime() {
		return dayTime;
	}

	/**
	 * @param dayTime the dayTime to set
	 */
	public void setDayTime(String dayTime) {
		this.dayTime = dayTime;
	}

	/**
	 * @return the dayNumber
	 */
	public int getDayNumber() {
		return dayNumber;
	}

	/**
	 * @param dayNumber the dayNumber to set
	 */
	public void setDayNumber(int dayNumber) {
		this.dayNumber = dayNumber;
	}


	/**
	 * @return the precision
	 */
	public double getPrecision() {
		return precision;
	}

	/**
	 * @return the recall
	 */
	public double getRecall() {
		return recall;
	}

	/**
	 * @return the f1Score
	 */
	public double getF1Score() {
		return F1Score;
	}


	/**
	 * @param precision
	 *            the precision to set
	 */
	public void setPrecision(double precision) {
		this.precision = precision;
	}

	/**
	 * @param recall
	 *            the recall to set
	 */
	public void setRecall(double recall) {
		this.recall = recall;
	}

	/**
	 * @param f1Score
	 *            the f1Score to set
	 */
	public void setF1Score(double f1Score) {
		F1Score = f1Score;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(F1Score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + dayNumber;
		result = prime * result + ((dayTime == null) ? 0 : dayTime.hashCode());
		temp = Double.doubleToLongBits(precision);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(recall);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		StatisticsResultDate other = (StatisticsResultDate) obj;
		if (Double.doubleToLongBits(F1Score) != Double
				.doubleToLongBits(other.F1Score))
			return false;
		if (dayNumber != other.dayNumber)
			return false;
		if (dayTime == null) {
			if (other.dayTime != null)
				return false;
		} else if (!dayTime.equals(other.dayTime))
			return false;
		if (Double.doubleToLongBits(precision) != Double
				.doubleToLongBits(other.precision))
			return false;
		if (Double.doubleToLongBits(recall) != Double
				.doubleToLongBits(other.recall))
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
		return precision + "\t" + recall + "\t" + F1Score+ "\t" + dayNumber;
	}

}
