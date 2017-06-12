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

package service.impl;

import dao.SeckillDao;
import dao.SuccesskilledDao;
import dao.cache.RedisDao;
import dto.Exposer;
import dto.SeckillExecution;
import entity.Seckill;
import entity.SuccessKilled;
import enums.SeckillStatEnum;
import exception.RepeatKillException;
import exception.SeckillCloseException;
import exception.SeckillException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import service.SeckillService;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/6/7.
 */
@Service
public class SeckillServiceImpl implements SeckillService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillDao seckillDao;
    @Autowired
    private SuccesskilledDao successkilledDao;
    @Autowired
    private RedisDao redisDao;

    private final String salt = "ASDsdfF@$T#UYHsdfWE#V$^sdfBUa&*$&*R>a:*gaDsEdf$gZafWgEdf$>SX<&MSZ$&<Lss";

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        Seckill seckill = redisDao.getSeckill(seckillId);
        if( seckill == null ){
            //缓存中没有，从数据库中查询
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            }else{
                //将查询到的数据放入到缓存中
                redisDao.putSeckill(seckill);
            }
        }
        //有查询到秒杀记录
        long startTime = seckill.getStartTime().getTime();
        long endTime = seckill.getEndTime().getTime();
        long nowTime = new Date().getTime();
        if (nowTime < startTime || nowTime > endTime) {
            return new Exposer(false, seckillId, nowTime, startTime, endTime);
        }
        //否者说明可以返回秒杀地址
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Override
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if(md5 == null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");
        }

        try {
            //插入购买详情
            int res = successkilledDao.insertSuccessKilled(seckillId, userPhone);
            if (res <= 0) {
                //重复插入失败
                throw new RepeatKillException("seckill repeat!");
            }

            //更新库存表
            res = seckillDao.reduceNumber(seckillId, new Date());
            if (res <= 0) {
                //更新库存失败,1、库存没了 2、未在秒杀时间内
                throw new SeckillCloseException("seckill closed!");
            }else{
                //秒杀成功
                SuccessKilled successKilled = successkilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
            }

        } catch (RepeatKillException e1) {
            throw e1;
        } catch (SeckillCloseException e2) {
            throw e2;
        } catch (Exception e3) {
            logger.error(e3.getMessage(), e3);
            //将所有的异常封装到 SeckillException
            throw new SeckillException("seckill inner error:" + e3.getMessage());
        }
    }
}
