/*
 *
 *  * 版权所有(C) 浙江大道网络科技有限公司2011-2020
 *  * Copyright 2009-2020 Zhejiang GreatTao Factoring Co., Ltd.
 *  *
 *  * This software is the confidential and proprietary information of
 *  * Zhejiang GreatTao Corporation ("Confidential Information").  You
 *  * shall not disclose such Confidential Information and shall use
 *  * it only in accordance with the terms of the license agreement
 *  * you entered into with Zhejiang GreatTao
 *
 */

package dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by Administrator on 2017/6/9.
 */
public class RedisDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JedisPool jedisPool;

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public RedisDao(String ip, int port) {
        jedisPool = new JedisPool(ip, port);
    }

    public Seckill getSeckill(long seckillId) {
        Jedis jedis = null;
        try {
            //这边的理解相当于打开一个connection
            jedis = jedisPool.getResource();
            String key = "seckill:" + seckillId;
            byte[] bytes = jedis.get(key.getBytes());
            if (bytes != null) {
                //使用工具将取出来的数据反序列化
                Seckill seckill = new Seckill();
                ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
                return seckill;
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public String putSeckill(Seckill seckill) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String key = "seckill:" + seckill.getSeckillId();
            //将对象转换成二进制数组
            byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema,
                    LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
            //将数据放到redis
            if(bytes != null){
                int timeout = 60 * 60;
                String result = jedis.setex(key.getBytes(),timeout,bytes);
                return result;
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

}
