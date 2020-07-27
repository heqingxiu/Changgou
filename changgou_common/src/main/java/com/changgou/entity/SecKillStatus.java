package com.changgou.entity;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

/**
 * Author: QX_He
 * DATA: 2020/7/12-21:45
 * Description:  用户排队抢单信息
 **/

public class SecKillStatus implements Serializable {
    // Seckill username
    private String userName;
    // Create time
    private Date createTime;
    //Seckill status  1:be queueing , 2:Waiting pay(has ordered) ,3: pay timeout ,4: seckill fail, 5: pay success
    private Integer status;
    // seckill goods id   (Use to rollback store)
    private Long goodsId;
    // need to pay
    private Float money;
    // order id
    private Long orderId;
    // timer-period
    private String time;

    //constructor
    public SecKillStatus() {

    }

    public SecKillStatus(String userName, Date createTime, Integer status, Long goodsId, String time) {
        this.userName = userName;
        this.createTime = createTime;
        this.status = status;
        this.goodsId = goodsId;
        this.time = time;

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Float getMoney() {
        return money;
    }

    public void setMoney(Float money) {
        this.money = money;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "SecKillStatus{" +
                "userName='" + userName + '\'' +
                ", createTime=" + createTime +
                ", status=" + status +
                ", goodsId=" + goodsId +
                ", money=" + money +
                ", orderId=" + orderId +
                ", time='" + time + '\'' +
                '}';
    }
}
