package com.wrapper.mvc.services;

import com.wrapper.infrastructure.handler.handle.IJdbcQueryHandle;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Component
public class TesteService {

    @Autowired
    private IJdbcQueryHandle iHandleJDBCQueryInternal;

    @Autowired
    private DataSource dataSource;

    public <T> T handleRequest(HttpServletRequest httpServletRequest) {
        Object result = iHandleJDBCQueryInternal.handleQueryInternal(dataSource, httpServletRequest);
        return (T) result;
    }
}
