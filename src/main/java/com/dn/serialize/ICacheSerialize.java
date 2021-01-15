package com.dn.serialize;

import java.util.List;

public interface ICacheSerialize {
    String serialize(Object bean);

    Object deserialization(String json, Class methodReturnType, List<Class> genericTypes);
}
