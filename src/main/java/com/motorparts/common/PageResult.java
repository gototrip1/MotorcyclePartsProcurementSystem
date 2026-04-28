package com.motorparts.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 从MyBatis Plus分页对象转换
     */
    public static <T> PageResult<T> from(IPage<T> page) {
        if (page == null) {
            return empty();
        }

        return PageResult.<T>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .records(page.getRecords())
                .build();
    }

    /**
     * 空分页结果
     */
    public static <T> PageResult<T> empty() {
        return PageResult.<T>builder()
                .current(1L)
                .size(10L)
                .total(0L)
                .pages(0L)
                .records(Collections.emptyList())
                .build();
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(Long current, Long size, Long total, List<T> records) {
        Long pages = size == 0 ? 0 : (total + size - 1) / size;

        return PageResult.<T>builder()
                .current(current)
                .size(size)
                .total(total)
                .pages(pages)
                .records(records)
                .build();
    }
}