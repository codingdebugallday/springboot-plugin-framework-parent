package com.github.codingdebugallday.plugin.framework.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import com.github.codingdebugallday.plugin.framework.exceptions.PluginException;
import com.github.codingdebugallday.plugin.framework.factory.process.pipe.PluginPipeProcessorExtend;
import com.github.codingdebugallday.plugin.framework.factory.process.pipe.classs.PluginClassGroupExtend;
import com.github.codingdebugallday.plugin.framework.factory.process.post.PluginPostProcessorExtend;
import com.github.codingdebugallday.plugin.framework.loader.PluginResourceLoader;
import com.github.codingdebugallday.plugin.framework.utils.CommonUtils;
import com.github.codingdebugallday.plugin.framework.utils.OrderPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * <p>
 * 静态的扩展初始化器
 * </p>
 *
 * @author isaac 2020/6/16 11:51
 * @since 1.0
 */
public class ExtensionInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionInitializer.class);

    private static final AtomicBoolean IS_INIT = new AtomicBoolean(false);

    private static final List<PluginResourceLoader> RESOURCE_LOADERS_EXTENDS = new ArrayList<>();
    private static final List<PluginPipeProcessorExtend> PIPE_PROCESSOR_EXTENDS = new ArrayList<>();
    private static final List<PluginClassGroupExtend> CLASS_GROUP_EXTENDS = new ArrayList<>();
    private static final List<PluginPostProcessorExtend> POST_PROCESSOR_EXTENDS = new ArrayList<>();

    private ExtensionInitializer() {
    }

    public static synchronized void initialize(ApplicationContext applicationContext) {
        if (applicationContext == null) {
            LOG.error("ApplicationContext is null, cannot initialize");
            return;
        }
        if (IS_INIT.get()) {
            throw new PluginException("The extension has been initialized");
        }
        Map<String, AbstractExtension> pluginExtension = ExtensionFactory.getPluginExtension();
        for (Map.Entry<String, AbstractExtension> entry : pluginExtension.entrySet()) {
            AbstractExtension abstractExtension = entry.getValue();
            if (abstractExtension == null) {
                continue;
            }
            try {
                abstractExtension.initialize(applicationContext);
                initialize(abstractExtension, applicationContext);
            } catch (Exception e) {
                LOG.error("Plugin extension '{}' initialize exception. {}", abstractExtension.key(), e.getMessage(), e);
            }
        }
        IS_INIT.set(true);
    }

    private static void initialize(AbstractExtension abstractExtension, ApplicationContext applicationContext) {
        StringBuilder debug = new StringBuilder();
        String key = abstractExtension.key();
        debug.append("Plugin extension '").append(key).append("'")
                .append(" are [");
        iteration(abstractExtension.getPluginResourceLoader(), pluginResourceLoader -> {
            RESOURCE_LOADERS_EXTENDS.add(pluginResourceLoader);
            debug.append(pluginResourceLoader.key()).append("、");
        }, PluginResourceLoader::order);

        iteration(abstractExtension.getPluginPipeProcessor(applicationContext), pluginPipeProcessorExtend -> {
            PIPE_PROCESSOR_EXTENDS.add(pluginPipeProcessorExtend);
            debug.append(pluginPipeProcessorExtend.key()).append("、");
        }, PluginPipeProcessorExtend::order);

        iteration(abstractExtension.getPluginClassGroup(applicationContext), pluginClassGroupExtend -> {
            CLASS_GROUP_EXTENDS.add(pluginClassGroupExtend);
            debug.append(pluginClassGroupExtend.key()).append("、");
        }, null);

        iteration(abstractExtension.getPluginPostProcessor(applicationContext), pluginResourceLoader -> {
            POST_PROCESSOR_EXTENDS.add(pluginResourceLoader);
            debug.append(pluginResourceLoader.key());
        }, PluginPostProcessorExtend::order);

        debug.append("] is registered");
        LOG.info("Plugin extension '{}' is registered", key);
        LOG.debug("{}", debug);
    }

    public static List<PluginResourceLoader> getResourceLoadersExtends() {
        return RESOURCE_LOADERS_EXTENDS;
    }

    public static List<PluginPipeProcessorExtend> getPipeProcessorExtends() {
        return PIPE_PROCESSOR_EXTENDS;
    }

    public static List<PluginClassGroupExtend> getClassGroupExtends() {
        return CLASS_GROUP_EXTENDS;
    }

    public static List<PluginPostProcessorExtend> getPostProcessorExtends() {
        return POST_PROCESSOR_EXTENDS;
    }

    /**
     * 迭代器
     *
     * @param list     当前处理的集合
     * @param consumer 消费集合中的数据项
     * @param order    排序集合。传入 null 表示不需要排序
     */
    private static <T> void iteration(List<T> list, Consumer<T> consumer, final Function<T, OrderPriority> order) {
        if (list == null || list.isEmpty()) {
            return;
        }
        if (order != null) {
            list.stream()
                    .filter(Objects::nonNull)
                    .sorted(CommonUtils.orderPriority(order))
                    .forEach(consumer);
        } else {
            for (T t : list) {
                if (t != null) {
                    consumer.accept(t);
                }
            }
        }
    }

}
