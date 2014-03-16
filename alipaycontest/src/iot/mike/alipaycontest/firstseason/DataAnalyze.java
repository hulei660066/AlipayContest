package iot.mike.alipaycontest.firstseason;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataAnalyze {

    static Logger logger = LogManager.getLogger(DataAnalyze.class.getName());

    public static void main(String[] args) {
        // analyzeUserAction();
        // analyzeBrandTimes();
        analyzeTBrand();
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
        // 这边的数据为，某个用户买了多少商品，找出最多的人
        for (Integer userid : users) {
            LinkedList<DataItem> detailsItem = DataHolder.getDataByUserID(userid);
            HashMap<Integer, DetailsItem> userDetails = new HashMap<>();

            for (DataItem item : detailsItem) {
                DetailsItem userItem;

                if (userDetails.containsKey(userid)) {
                    userItem = userDetails.get(userid);
                } else {
                    userItem = new DetailsItem();
                }

                userItem.setUserid(userid);

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
                    userDetails.put(userid, userItem);
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
             * (即购买次数比较大，三个月中购物次数大于30)。
             */
            for (Integer user : users) {
                try {
                    if (userDetails.get(user).getBuynum() >= 30
                        && userDetails.get(user).getClicknum() / userDetails.get(user).getBuynum() <= 20) {
                        System.out.println(userDetails.get(user));
                    }
                } catch (Exception e) {

                }
            }

        }
    }

    /**
     * 用來分析特定品牌的數據，寻找对于大部分用户的刚性需求
     */
    public static void analyzeBrandTimes() {
        Integer[] brands = DataHolder.getBrandID();

        StringBuilder builder = new StringBuilder();
        // 这边的数据为，某一个品牌的商品被买了多少次，找出买的最多的牌子
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

    /**
     * 找到周期性的商品品牌
     */
    public static void analyzeTBrand() {
        Integer[] users = DataHolder.getUserID();
        HashMap<String, LinkedList<DataItem>> userdata = new HashMap<>();
        for (Integer user : users) {
            LinkedList<DataItem> useritems = DataHolder.getDataByUserID(user);
            LinkedList<DataItem> buyitems = new LinkedList<>();
            for (DataItem useritem : useritems) {
                if (useritem.getType() == 1
                    || useritem.getType() == 3) {
                    buyitems.add(useritem);
                }
            }
            
            
            
            if (buyitems.size() > 20) {
                System.out.println(buyitems);
            }
        }
    }
}

class DetailsItem {
    private int  userid;
    private int  brandid;
    private Date data;

    /**
     * @return the data
     */
    public Date getData() {
        return data;
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(Date data) {
        this.data = data;
    }

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
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + marknum;
        result = prime * result + userid;
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
        if (data == null) {
            if (other.data != null) return false;
        } else if (!data.equals(other.data)) return false;
        if (marknum != other.marknum) return false;
        if (userid != other.userid) return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DetailsItem [userid=" + userid
               + ", brandid=" + brandid
               + ", data=" + data
               + ", clicknum=" + clicknum
               + ", marknum=" + marknum
               + ", buynum=" + buynum + "]";
    }
}

/*
 * TODO 算出周期性，用户，购买周期。
 * TODO 8月数据作为最近时间的分析。不同时间数据的权重不同
 * TODO 九月的数据分为8-15~9-1和9-2~9-15, 新旧商品的比例，新商品购买转换率 
 *              相似用户分类
 * TODO 无意义的点击数的删除，比如购买之后好奇心态的点击， 注意与周期性的购买进行区分
 */
