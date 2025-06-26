package com.wrapper.infrastructure.handler.filter;

import com.wrapper.infrastructure.handler.enuns.EnumJdbcSorterOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JdbcSort {

    private String field;
    private EnumJdbcSorterOrder order = EnumJdbcSorterOrder.ASC;

    public JdbcSort() {
    }
}
