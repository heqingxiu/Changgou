package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.dao.OrderItemMapper;
import com.changgou.order.dao.OrderMapper;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.OrderService;
import com.changgou.order.pojo.Order;
import com.changgou.user.feign.UserFeign;
import com.changgou.util.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.netflix.discovery.converters.Auto;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    private static final String CART = "cart_";

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 删除订单，回滚库存
     *
     * @param outtradeno
     */
    @Override
    public void deleteOrder(String outtradeno) {
        //查询订单对象
        Order order = orderMapper.selectByPrimaryKey(outtradeno);
        //修改订单状态
        order.setUpdateTime(new Date());
        order.setPayType("2");
        //更新修改数据到数据库中
        orderMapper.updateByPrimaryKeySelective(order);
        /**
         * 回滚库存-》调用goods微服务（回滚） , 关闭订单
         */
        //通过订单号查询该订单号下所有的订单明细，  再通过订单明细查询对应的sku_id
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(outtradeno);
        List<OrderItem> orderItems = orderItemMapper.select(orderItem);
        for (OrderItem orderItem1 : orderItems) {
            String skuId = orderItem1.getSkuId();
            Integer number = orderItem1.getNum();
            skuFeign.resumeStockNum(skuId, number);
            System.out.println("订单回滚成功");
        }
        // 关闭订单，实现退钱 ？？？？？  不，这里不是退钱
        //
    }

    /**
     * 更新支付状态
     * 1.修改支付时间
     * 2.修改支付状态
     * 3.更新交易流水号
     *
     * @param outtradeno
     * @param paytime
     * @param transactionid
     */
    @Override
    public void updateStatus(String outtradeno, String paytime, String transactionid) throws Exception {
        // 查询订单
        Order order = orderMapper.selectByPrimaryKey(outtradeno);// 给的订单号在数据库中不存在导致了错误。
        //时间转换
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date payTimeInfo = simpleDateFormat.parse(paytime);
        //修改状态
        order.setPayTime(payTimeInfo);  //  这里报错的原因是因为给的 交易流水号在数据库中都没有，因此是一个空值。就出现了 nullPointException
        order.setPayType("1");
        order.setTransactionId(transactionid);
        //更新数据
        orderMapper.updateByPrimaryKeySelective(order);
    }

    public static void main(String[] args) throws ParseException {
        Order order =  new Order();
        String payTime = "20200714223212";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date payTimeInfo = simpleDateFormat.parse(payTime);
        order.setPayTime(payTimeInfo);
        System.out.println("finish");
    }


    /**
     * 查询全部列表
     *
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    @Override
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }


    /**
     * 加入购物车           复写该方法
     *
     * @param order
     */
    @Override
    public void add(Order order) {
        //设置订单的主键
        order.setId(String.valueOf(idWorker.nextId()));

        //定义个临时变量，用于保存已勾选的商品 (即订单明细)
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        for (Long skuId : order.getSkuIds()) {
            //先将选择的订单明细（商品）勾选上    (redis 我们之前是使用 HashMap的形式存入的,因此这里使用boundHashOps)
            orderItems.add((OrderItem) redisTemplate.boundHashOps(CART + order.getUsername()).get(skuId.toString())); //  注意 添加到redis中的key是String类型
            //将已下单的商品从购物车中移除
            redisTemplate.boundHashOps(CART + order.getUsername()).delete(skuId.toString());
        }

        int totalNum = 0; // 总数量
        int totalMoney = 0; //总金额
        //封装Map<Long,Integer> 递减数据
        Map<String, Integer> decrmap = new HashMap<String, Integer>();
        for (OrderItem orderItem : orderItems) {
            totalMoney += orderItem.getPayMoney();
            totalNum += orderItem.getNum();
            //订单明细的id (商品id)
            orderItem.setId(String.valueOf(idWorker.nextId()));
            //订单明细所属订单（商品属于那一订单）
            orderItem.setOrderId(order.getId());  //  订单（1） 对 订单明细（多） 关系 ，因此将 1 的id 作为多的外键 。在多的一方写入。
            //是否退货      刚下单，所以默认的状态肯定是 未退货
            orderItem.setIsReturn("0");
            //封装递减数据
            decrmap.put(orderItem.getSkuId(), orderItem.getNum());
        }

        //订单值添加一次   （上面的是商品，循环添加）
        order = OtherConfigOrder(order);
        // 订单购买商品总数量 = 每个商品的总数量之和
        order.setTotalNum(totalNum);
        // 订单总金额 = 每个商品的总金额之和
        order.setTotalMoney(totalMoney);
        // 实付金额
        order.setPayMoney(totalMoney);

        //添加订单消息
        orderMapper.insertSelective(order); // 忽略null 值
        // 循环添加订单明细 （商品详细信息）
        for (OrderItem item : orderItems) {
            orderItemMapper.insertSelective(item);
        }

        // 增加个人积分  (每下一单增加一积分)
        userFeign.addPoints(1);

        // 实现库存递减 和 增加销售数量
        skuFeign.decrCount(decrmap);

        //设置30分钟订单超时process
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("创建订单时间:" + simpleDateFormat.format(new Date()));
        rabbitTemplate.convertAndSend("orderDelayQueue", (Object) order.getId(), new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //设置延时读取时间
                message.getMessageProperties().setExpiration("10000"); // 单位是 ms
                return message; //修改下属性值，之后再返回本体
            }
        });
    }

    /**
     * 订单的创建时间和支付等默认状态
     *
     * @param order
     * @return
     */
    public Order OtherConfigOrder(Order order) {
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setSourceType("1");// 订单来源 1：Web
        order.setOrderStatus("0");// 未支付
        order.setPayStatus("0");//未支付
        order.setIsDelete("0");//未删除
        return order;
    }


    /**
     * 修改
     *
     * @param order
     */
    @Override
    public void update(Order order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     *
     * @param searchMap
     * @return
     */
    @Override
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Order> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        return (Page<Order>) orderMapper.selectAll();
    }

    /**
     * 条件+分页查询
     *
     * @param searchMap 查询条件
     * @param page      页码
     * @param size      页大小
     * @return 分页结果
     */
    @Override
    public Page<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        return (Page<Order>) orderMapper.selectByExample(example);
    }

    /**
     * 构建查询对象
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 订单id
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andEqualTo("id", searchMap.get("id"));
            }
            // 支付类型，1、在线支付、0 货到付款
            if (searchMap.get("payType") != null && !"".equals(searchMap.get("payType"))) {
                criteria.andEqualTo("payType", searchMap.get("payType"));
            }
            // 物流名称
            if (searchMap.get("shippingName") != null && !"".equals(searchMap.get("shippingName"))) {
                criteria.andLike("shippingName", "%" + searchMap.get("shippingName") + "%");
            }
            // 物流单号
            if (searchMap.get("shippingCode") != null && !"".equals(searchMap.get("shippingCode"))) {
                criteria.andLike("shippingCode", "%" + searchMap.get("shippingCode") + "%");
            }
            // 用户名称
            if (searchMap.get("username") != null && !"".equals(searchMap.get("username"))) {
                criteria.andLike("username", "%" + searchMap.get("username") + "%");
            }
            // 买家留言
            if (searchMap.get("buyerMessage") != null && !"".equals(searchMap.get("buyerMessage"))) {
                criteria.andLike("buyerMessage", "%" + searchMap.get("buyerMessage") + "%");
            }
            // 是否评价
            if (searchMap.get("buyerRate") != null && !"".equals(searchMap.get("buyerRate"))) {
                criteria.andLike("buyerRate", "%" + searchMap.get("buyerRate") + "%");
            }
            // 收货人
            if (searchMap.get("receiverContact") != null && !"".equals(searchMap.get("receiverContact"))) {
                criteria.andLike("receiverContact", "%" + searchMap.get("receiverContact") + "%");
            }
            // 收货人手机
            if (searchMap.get("receiverMobile") != null && !"".equals(searchMap.get("receiverMobile"))) {
                criteria.andLike("receiverMobile", "%" + searchMap.get("receiverMobile") + "%");
            }
            // 收货人地址
            if (searchMap.get("receiverAddress") != null && !"".equals(searchMap.get("receiverAddress"))) {
                criteria.andLike("receiverAddress", "%" + searchMap.get("receiverAddress") + "%");
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if (searchMap.get("sourceType") != null && !"".equals(searchMap.get("sourceType"))) {
                criteria.andEqualTo("sourceType", searchMap.get("sourceType"));
            }
            // 交易流水号
            if (searchMap.get("transactionId") != null && !"".equals(searchMap.get("transactionId"))) {
                criteria.andLike("transactionId", "%" + searchMap.get("transactionId") + "%");
            }
            // 订单状态
            if (searchMap.get("orderStatus") != null && !"".equals(searchMap.get("orderStatus"))) {
                criteria.andEqualTo("orderStatus", searchMap.get("orderStatus"));
            }
            // 支付状态
            if (searchMap.get("payStatus") != null && !"".equals(searchMap.get("payStatus"))) {
                criteria.andEqualTo("payStatus", searchMap.get("payStatus"));
            }
            // 发货状态
            if (searchMap.get("consignStatus") != null && !"".equals(searchMap.get("consignStatus"))) {
                criteria.andEqualTo("consignStatus", searchMap.get("consignStatus"));
            }
            // 是否删除
            if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                criteria.andEqualTo("isDelete", searchMap.get("isDelete"));
            }

            // 数量合计
            if (searchMap.get("totalNum") != null) {
                criteria.andEqualTo("totalNum", searchMap.get("totalNum"));
            }
            // 金额合计
            if (searchMap.get("totalMoney") != null) {
                criteria.andEqualTo("totalMoney", searchMap.get("totalMoney"));
            }
            // 优惠金额
            if (searchMap.get("preMoney") != null) {
                criteria.andEqualTo("preMoney", searchMap.get("preMoney"));
            }
            // 邮费
            if (searchMap.get("postFee") != null) {
                criteria.andEqualTo("postFee", searchMap.get("postFee"));
            }
            // 实付金额
            if (searchMap.get("payMoney") != null) {
                criteria.andEqualTo("payMoney", searchMap.get("payMoney"));
            }

        }
        return example;
    }

}
