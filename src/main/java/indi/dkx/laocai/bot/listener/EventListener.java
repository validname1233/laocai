package indi.dkx.laocai.bot.listener;

import indi.dkx.laocai.bot.model.event.Event;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public record EventListener(
        Object bean,
        Method method
) {
        // 可以在这里封装 invoke 逻辑
        public void handle(Event<?> event) throws Exception {
            if (!match(event)) return;
            method.invoke(bean, event);
        }

        /**
        * 判断方法参数类型是否与事件数据匹配
        * @param event 事件数据
        * @return 是否匹配
        */
        private boolean match(Event<?> event) {
            // 获取方法第一个参数的泛型类型
            Type genericParam = method.getGenericParameterTypes()[0];
            // 检查是否是带泛型的 Event<T> 类型
            if (genericParam instanceof ParameterizedType parameterizedType
                    && parameterizedType.getRawType() == Event.class) {
                // 获取泛型的实际类型参数 T
                Type actualType = parameterizedType.getActualTypeArguments()[0];
                // 如果 T 是具体的 Class，检查 data 是否是该类型的实例
                if (actualType instanceof Class<?> actualClass) {
                    return actualClass.isInstance(event.data());
                }
            }
            // 如果参数是原始 Event 类型（无泛型），只要 data 不为空就匹配
            Class<?> paramType = method.getParameterTypes()[0];
            return paramType == Event.class && event.data() != null;
        }
    }