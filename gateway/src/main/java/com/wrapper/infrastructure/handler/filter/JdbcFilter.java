package com.wrapper.infrastructure.handler.filter;

import com.wrapper.infrastructure.handler.enuns.EnumJdbcFilterModifier;
import com.wrapper.infrastructure.handler.enuns.EnumJdbcFilterType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JdbcFilter {

    private String field;
    private EnumJdbcFilterModifier modifier;
    private String value;
    private EnumJdbcFilterType type;
    private boolean checkNull = false;

    public JdbcFilter() {
    }

}
