package com.motorparts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.motorparts.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细 Mapper 接口
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

}