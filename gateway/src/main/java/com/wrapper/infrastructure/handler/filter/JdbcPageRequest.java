package com.wrapper.infrastructure.handler.filter;

import com.wrapper.infrastructure.handler.enuns.EnumJdbcPaginationStrategy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JdbcPageRequest {

    public static final String POSTGRES_PAGINATOR_TEMPLATE = "SELECT tmp.* FROM (:query) AS tmp LIMIT :size OFFSET :offset";

    private int page = 0;
    private int size = 0;
    private int maxSize = 0;
    private int offset = 0;
    private int first = 0;
    private int last = 0;

    private boolean counting = false;
    private boolean pageable = false;

    private EnumJdbcPaginationStrategy integradorJDBCPaginationStrategy = EnumJdbcPaginationStrategy.DEFAULT;

    public JdbcPageRequest() {
    }

    public JdbcPageRequest(int page, int size, int maxSize, boolean counting, boolean pageable) {
        this.page = page;
        this.size = size;
        this.maxSize = maxSize;
        this.counting = counting;
        this.pageable = pageable;
    }
}
