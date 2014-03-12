package iot.mike.alipaycontest.firstseason;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataAnalyze {

    static Logger logger = LogManager.getLogger(DataAnalyze.class.getName());

    public static void main(String[] args) {
        analyzeUserAction();
        analyzeBrandTimes();
    }

    /**
     * 用来分析用户的购物喜好，比如浏览几次之后会加入购物车或者购买
     * 
     * @param items
     *            The Whole Data
     */
    public static void analyzeUserAction() {
        Integer[] users = DataHolder.getUserID();
        StringBuilder builder = new StringBuilder();

        for (Integer user : users) {
            LinkedList<DataItem> userItems = DataHolder.getDataByUserID(user);
            HashMap<Integer, DetailsItem> userDetails = new HashMap<>();

            for (DataItem item : userItems) {
                DetailsItem userItem;
                int brandid = item.getBrandid();
                if (userDetails.containsKey(brandid)) {
                    userItem = userDetails.get(brandid);
                } else {
                    userItem = new DetailsItem();
                }
                userItem.setBrandid(brandid);

                switch (item.getType()) {
                    case 0: {// click
                        userItem.setClicknum(userItem.getClicknum() + 1);
                        break;
                    }

                    case 1: {// purchese
                        userItem.setBuynum(userItem.getBuynum() + 1);
                        break;
                    }

                    case 2: {// mark
                        userItem.setMarknum(userItem.getMarknum());
                        break;
                    }
                    case 3: {// shopping car
                        userItem.setBuynum(userItem.getBuynum() + 1);
                        break;
                    }
                }

                if (userItem.getBuynum() != 0) {
                    userDetails.put(brandid, userItem);
                }
            }

            // 输出用户购买的商品的详细条目。
            // System.out.println(userDetails);
            if (userDetails.size() != 0) {
                // System.out.println(user + ":" + userDetails.size());
                // System.out.println(userDetails);
                builder.append(userDetails + "\n");
                write2File(new File(ETLCONFIG.USERS_FILE), builder.toString());
            }

            /*
             * 想到，对于经常购物的人员，我们可以进行细致的分析，而对于大部分购物或者加入购物车的人来说，数据量不够
             * 无法进行详细的分析，所以，我建议只针对部分经常购物的人进行推荐，而不是对所有人。
             * 如果某些用户的习惯比较明显，建议直接单独列出推荐，以及寻找刚性需求
             * (即购买次数比较大，四个月中购物次数大于4)。
             */
        }
    }

    /**
     * 用來分析特定品牌的數據，寻找对于大部分用户的刚性需求
     */
    public static void analyzeBrandTimes() {
        Integer[] brands = DataHolder.getBrandID();

        StringBuilder builder = new StringBuilder();

        for (Integer brandid : brands) {
            LinkedList<DataItem> brandItems = DataHolder.getDataByBrandID(brandid);
            HashMap<Integer, DetailsItem> brandDetails = new HashMap<>();

            for (DataItem item : brandItems) {
                DetailsItem detailsItem;
                if (brandDetails.containsKey(brandid)) {
                    detailsItem = brandDetails.get(brandid);
                } else {
                    detailsItem = new DetailsItem();
                }

                detailsItem.setBrandid(brandid);

                switch (item.getType()) {
                    case 0: {// click
                        detailsItem.setClicknum(detailsItem.getClicknum() + 1);
                        break;
                    }

                    case 1: {// purchese
                        detailsItem.setBuynum(detailsItem.getBuynum() + 1);
                        break;
                    }

                    case 2: {// mark
                        detailsItem.setMarknum(detailsItem.getMarknum());
                        break;
                    }
                    case 3: {// shopping car
                        detailsItem.setBuynum(detailsItem.getBuynum() + 1);
                        break;
                    }
                }

                if (detailsItem.getBuynum() != 0) {
                    brandDetails.put(brandid, detailsItem);
                }
            }
            // 输出商品商标的详细条目。
            // System.out.println(userDetails);
            if (brandDetails.size() != 0) {
                // System.out.println(user + ":" + userDetails.size());
                // System.out.println(brandDetails);
                builder.append(brandDetails + "\n");
                write2File(new File(ETLCONFIG.BRANDS_FILE), builder.toString());
            }
        }
    }

    /**
     * Write the data to file
     * 
     * @param file
     */
    public static void write2File(File file, String message) {
        try {
            File path = new File(file.getParent());
            path.mkdirs();
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.write(message);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }
    }

}

class DetailsItem {
    private int userid;
    private int brandid;
    private int clicknum;
    private int marknum;
    private int buynum;

    /**
     * @return the userid
     */
    public int getUserid() {
        return userid;
    }

    /**
     * @param userid
     *            the userid to set
     */
    public void setUserid(int userid) {
        this.userid = userid;
    }

    /**
     * @return the brandid
     */
    public int getBrandid() {
        return brandid;
    }

    /**
     * @param brandid
     *            the brandid to set
     */
    public void setBrandid(int brandid) {
        this.brandid = brandid;
    }

    /**
     * @return the clicknum
     */
    public int getClicknum() {
        return clicknum;
    }

    /**
     * @param clicknum
     *            the clicknum to set
     */
    public void setClicknum(int clicknum) {
        this.clicknum = clicknum;
    }

    /**
     * @return the marknum
     */
    public int getMarknum() {
        return marknum;
    }

    /**
     * @param marknum
     *            the marknum to set
     */
    public void setMarknum(int marknum) {
        this.marknum = marknum;
    }

    /**
     * @return the buynum
     */
    public int getBuynum() {
        return buynum;
    }

    /**
     * @param buynum
     *            the buynum to set
     */
    public void setBuynum(int buynum) {
        this.buynum = buynum;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + brandid;
        result = prime * result + buynum;
        result = prime * result + clicknum;
        result = prime * result + marknum;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof DetailsItem)) return false;
        DetailsItem other = (DetailsItem) obj;
        if (brandid != other.brandid) return false;
        if (buynum != other.buynum) return false;
        if (clicknum != other.clicknum) return false;
        if (marknum != other.marknum) return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DetailsItem [userid=" + userid
                + ", brandid=" + brandid
                + ", clicknum=" + clicknum
                + ", marknum=" + marknum
                + ", buynum=" + buynum + "]";
    }
}
