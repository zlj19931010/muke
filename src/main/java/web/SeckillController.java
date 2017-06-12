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

package web;

import dto.Exposer;
import dto.SeckillExecution;
import dto.SeckillResult;
import entity.Seckill;
import enums.SeckillStatEnum;
import exception.RepeatKillException;
import exception.SeckillCloseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import service.SeckillService;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/6/8.
 */
@Controller
@RequestMapping("/seckill")
public class SeckillController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillService seckillService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("list", list);
        return "list";
    }

    @RequestMapping(value = "/{seckillId}/detail", method = RequestMethod.GET)
    public String detail(Model model, @PathVariable("seckillId") Long seckillId) {
        if (seckillId == null) {
            return "redirect:seckill/list";
        }

        Seckill seckill = seckillService.getById(seckillId);
        if (seckill == null) {
            //查询不到指定的id，重定向到列表页面
            return "forward:/seckill/list";
        }
        model.addAttribute("seckill", seckill);
        return "detail";
    }

    @RequestMapping(value = "/{seckillId}/exposer",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId) {
        SeckillResult<Exposer> result;
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<>(true, exposer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result = new SeckillResult<>(false, e.getMessage());
        }

        return result;
    }

    @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("md5") String md5,
                                                   @CookieValue(value = "killPhone", required = false) Long phone) {
        if (phone == null) {
            return new SeckillResult<>(false, "未注册");
        }

        SeckillResult<SeckillExecution> result;
        try {
            SeckillExecution execution = seckillService.executeSeckill(seckillId, phone, md5);
            result = new SeckillResult<>(true, execution);
        } catch (SeckillCloseException e1) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.END);
            return new SeckillResult<>(true, execution);
        } catch (RepeatKillException e2) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            return new SeckillResult<>(true, execution);
        } catch (Exception e3) {
            logger.error(e3.getMessage(), e3);
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
            return new SeckillResult<>(true, execution);
        }
        return result;
    }

    //获取系统时间
    @RequestMapping(value = "/time/now",method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time() {
        Date now=new Date();
        return new SeckillResult<>(true,now.getTime());
    }

    //获取系统时间
    @RequestMapping(value = "/time/now",method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> tetet() {
        Date now=new Date();
        return new SeckillResult<>(true,now.getTime());
    }

}
