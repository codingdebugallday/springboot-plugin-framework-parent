package com.github.codingdebugallday.plugin.framework.factory.process.pipe.bean.name;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 插件注解名称生成者
 * </p>
 *
 * @author isaac 2020/6/16 14:53
 * @since 1.0
 */
public class PluginAnnotationBeanNameGenerator extends AnnotationBeanNameGenerator {

    /**
     * 插件id
     */
    private final String pluginId;

    /**
     * 后缀名称
     */
    private final String suffixName;

    public PluginAnnotationBeanNameGenerator(String pluginId, String suffixName) {
        if (StringUtils.isEmpty(pluginId)) {
            this.pluginId = "";
        } else {
            this.pluginId = pluginId + "@";
        }
        if (StringUtils.isEmpty(suffixName)) {
            this.suffixName = "";
        } else {
            this.suffixName = "@" + suffixName;
        }
    }

    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        if (definition instanceof AnnotatedBeanDefinition) {
            String beanName = determineBeanNameFromAnnotation((AnnotatedBeanDefinition) definition);
            if (StringUtils.hasText(beanName)) {
                return beanName;
            }
        }
        return pluginId + buildDefaultBeanName(definition, registry) + suffixName;
    }


}
