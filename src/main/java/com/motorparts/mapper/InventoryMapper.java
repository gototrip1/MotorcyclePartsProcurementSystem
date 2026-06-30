package com.motorparts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.motorparts.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 库存 Mapper 接口
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 根据零部件ID查询库存
     */
    @Select("SELECT * FROM inventory WHERE part_id = #{partId} AND deleted = 0")
    Inventory selectByPartId(@Param("partId") Long partId);

    /**
     * 库存增加（入库）：current_quantity += qty，并更新最近入库时间
     */
    @Update("UPDATE inventory SET current_quantity = current_quantity + #{qty}, " +
            "last_inbound_time = #{time} WHERE part_id = #{partId} AND deleted = 0")
    int increaseQuantity(@Param("partId") Long partId, @Param("qty") int qty, @Param("time") LocalDateTime time);

    /**
     * 库存减少（出库）：current_quantity -= qty，并更新最近出库时间。
     * <p>WHERE 子句中的 {@code current_quantity >= qty} 在数据库层兜底防止超扣，
     * 返回 0 行即视为库存不足，调用方应回滚事务。</p>
     */
    @Update("UPDATE inventory SET current_quantity = current_quantity - #{qty}, " +
            "last_outbound_time = #{time} WHERE part_id = #{partId} AND current_quantity >= #{qty} AND deleted = 0")
    int decreaseQuantity(@Param("partId") Long partId, @Param("qty") int qty, @Param("time") LocalDateTime time);
}
