package indi.dkx.laocai.bot.binder;

import org.springframework.stereotype.Component;

/**
 * 默认的 BinderManager 实现，当前未接入具体绑定器体系。
 */
@Component
public class SimpleBinderManager implements BinderManager {

    @Override
    public int normalBinderFactorySize() {
        return 0;
    }

    @Override
    public int globalBinderFactorySize() {
        return 0;
    }
}
