
package dao;

import entity.SuccessKilled;
import org.apache.ibatis.annotations.Param;

/**
 * @author Administrator
 */
public interface SuccesskilledDao {
    /**
     * 插入购买明细 可过滤重复
     *
     * @param seckillId
     * @param userPhone
     * @return 插入的行数
     */
    int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

    /**
     * 根据ID查询Successkilled并携带秒杀产品对象实体
     *
     * @param seckillId
     * @return
     */
    SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

}
