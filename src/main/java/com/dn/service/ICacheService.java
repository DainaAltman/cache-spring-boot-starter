package com.dn.service;

public interface ICacheService {

    void set(String key, String val, long timeout);

    void set(String key, String val);

    String get(String key);
}
