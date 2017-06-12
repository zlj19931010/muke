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

package enums;

/**
 * 使用枚举来标识我的常量数据字典
 * Created by Administrator on 2017/6/7.
 */
public enum SeckillStatEnum {
    SUCCESS(1, "秒杀成功"),
    END(0, "秒杀结束"),
    REPEAT_KILL(-1, "重复秒杀"),
    INNER_ERROR(-2, "系统异常"),
    DATA_REWRITE(-3, "数据篡改");

    private int state;
    private String stateInfo;

    SeckillStatEnum(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public int getState() {
        return state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public static SeckillStatEnum stateOf(int index) {
        for (SeckillStatEnum state : values()) {
            if (state.getState() == index) {
                return state;
            }
        }
        return null;
    }
}
