package com.hxl.plugin.springboot.invoke.model;

public abstract class SpringInvokeEndpoint {
    private String id;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}