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

package dto;

/**
 * Created by Administrator on 2017/6/8.
 */
//封装json结果
public class SeckillResult<T> {
    //请求是否成功
    private boolean success;
    private T data;
    private String error;

    public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
