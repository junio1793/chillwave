package com.wrapper.infrastructure.handler.handle;

import jakarta.servlet.http.HttpServletRequest;

import javax.sql.DataSource;

public interface IJdbcQueryHandle {

    Object handleQueryInternal(DataSource dataSource, HttpServletRequest httpServletRequest);

}
