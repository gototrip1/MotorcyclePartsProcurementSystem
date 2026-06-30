package com.motorparts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.motorparts.entity.StockOutOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 领用单 Mapper 接口
 */
@Mapper
public interface StockOutOrderMapper extends BaseMapper<StockOutOrder> {

}
