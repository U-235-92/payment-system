package aq.project.configurations;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({ "dev-shard", "prod" })
class MigrationConfiguration implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if(beanFactory.containsBeanDefinition("dataSource")) {
            BeanDefinition dataSourceBean = beanFactory.getBeanDefinition("dataSource");
            dataSourceBean.setDependsOn("migrator");
        }
    }
}
