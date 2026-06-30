package com.motorparts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.motorparts.entity.StockOutDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 领用明细 Mapper 接口
 */
@Mapper
public interface StockOutDetailMapper extends BaseMapper<StockOutDetail> {

    /**
     * 根据领用单ID查询明细
     */
    @Select("SELECT * FROM stock_out_detail WHERE stock_out_id = #{stockOutId} AND deleted = 0")
    List<StockOutDetail> selectByStockOutId(@Param("stockOutId") Long stockOutId);

    /**
     * 物理删除领用明细（绕过 @TableLogic 软删除，直接从数据库删除）
     */
    @Delete("DELETE FROM stock_out_detail WHERE stock_out_id = #{stockOutId}")
    int physicalDeleteByStockOutId(@Param("stockOutId") Long stockOutId);
}
