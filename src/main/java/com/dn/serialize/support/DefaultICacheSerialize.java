package com.dn.serialize.support;

import com.dn.serialize.ICacheSerialize;
import com.dn.utils.JsonUtils;

import java.util.List;
import java.util.Map;

public class DefaultICacheSerialize implements ICacheSerialize {
    @Override
    public String serialize(Object bean) {
        return JsonUtils.toJson(bean);
    }

    @Override
    public Object deserialization(String json, Class methodReturnType, List<Class> genericTypes) {
        if (List.class.isAssignableFrom(methodReturnType)) {
            if (genericTypes != null && genericTypes.size() > 0) {
                return JsonUtils.toList(json, genericTypes.get(0));
            }
            return JsonUtils.toList(json);
        }
        if (Map.class.isAssignableFrom(methodReturnType)) {
            if (genericTypes != null && genericTypes.size() > 2) {
                return JsonUtils.toMap(json, genericTypes.get(0), genericTypes.get(1));
            }
            return JsonUtils.toList(json);
        }
        return JsonUtils.toBean(json, methodReturnType);
    }

}
