package com.wrapper.infrastructure.endpoint;

import java.util.Map;

public interface IEndpointQueryConfigTable {

    public Map find(String constante, String identify);

    public boolean exists();

    public void create();

    public void drop();

    public void truncate();
}
