package com.cxp.springboot2rabbitmq.rabbitConfig;

import com.cxp.springboot2rabbitmq.rabbitConverter.JPGMessageConverter;
import com.cxp.springboot2rabbitmq.rabbitConverter.TextMessageConverter;
import org.apache.commons.collections4.MapUtils;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 程
 * @date 2019/7/1 下午8:17
 */
@Configuration
public class RabbitConfig {

    @Value(value ="${scan.idClass.packageName}")
    private String scanIdClassPackageName;

    @Bean
    public ContentTypeDelegatingMessageConverter messageConverter(){
        /**
         * 从Spring-Rabbit 1.4.2开始,它提供了ContentTypeDelegatingMessageConverter,
         * 它能根据不同的MessageProperties属性(contentType)决定来委托给具体的哪一个MessageConverter
         *
         */
        ContentTypeDelegatingMessageConverter contentTypeDelegatingMessageConverter =
                new ContentTypeDelegatingMessageConverter();

        TextMessageConverter textMessageConverter = new TextMessageConverter();
        contentTypeDelegatingMessageConverter.addDelegate("text",textMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("html/text",textMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("xml/text",textMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("text/plain",textMessageConverter);

        MessageConverter jsonMessageConverter = jsonMessageConverter();
        contentTypeDelegatingMessageConverter.addDelegate("json",jsonMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("application/json",jsonMessageConverter);

        JPGMessageConverter jpgMessageConverter = jpgMessageConverter();
        contentTypeDelegatingMessageConverter.addDelegate("image/jpg",jpgMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("image/jpeg", jpgMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("image/png", jpgMessageConverter);
        return contentTypeDelegatingMessageConverter;
    }

    /**
     * 定义消息转换实例  转化成 JSON 传输  传输实体就可以不用实现序列化
     * 注意:如果发送方和接收方都用Jackson2JsonMessageConverter的,那么消费者在接收消息把json转换为对象时,消费者端对象的
     *      全路径名必须和生产者一致，否则会转换失败。我们发现生产者和消费者的耦合度太高,优化方法:
     *     参考:https://www.jianshu.com/p/83861676051c
     * @return
     */
    @Bean(autowireCandidate = false)
    public Jackson2JsonMessageConverter jsonMessageConverter(){
        //指定Json转换器
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();

        //消费端配置映射(此配置一定是在消费端的实体类映射关系)
        Map<String, Class<?>> idClassMapping = idClassMapping(scanIdClassPackageName);

        if (MapUtils.isNotEmpty(idClassMapping)){
            DefaultJackson2JavaTypeMapper jackson2JavaTypeMapper = new DefaultJackson2JavaTypeMapper();
            jackson2JavaTypeMapper.setIdClassMapping(idClassMapping);
            //在jackson2JsonMessageConverter转换器中指定映射配置
            jackson2JsonMessageConverter.setJavaTypeMapper(jackson2JavaTypeMapper);
        }

        return jackson2JsonMessageConverter;
    }

    /**
     * 两个对象继承自同一接口autowire-candidate="false" 表示该对象不参与自动注入
     * @return
     */
    @Bean(autowireCandidate = false)
    public JPGMessageConverter jpgMessageConverter(){
        JPGMessageConverter jpgMessageConverter = new JPGMessageConverter();
        return jpgMessageConverter;
    }

    private Map<String, Class<?>> idClassMapping(String scanPackageName){
        Map<String, Class<?>> idClass = null;
        if (!StringUtils.isEmpty(scanPackageName)){
            idClass = new HashMap<>();
            scanPackages(scanPackageName,idClass);
        }
        return idClass;
    }

    /**
     * 扫描配置的包路径下的实体类,并配置映射关系
     * @param packageName1
     * @param idClass
     */
    private void scanPackages(String packageName1,Map<String, Class<?>> idClass)  {
        String packageName = !StringUtils.isEmpty(packageName1)?
                packageName1.replace(".","/"):null;
        String packagesUrl = this.getClass().getClassLoader().getResource(packageName).getFile();
        File scanFile=new File(packagesUrl);
        String[] filenameList=scanFile.list();
        for (String filename :filenameList){
            File file=new File(scanFile,filename);
            if (file.isDirectory()){
                scanPackages(packageName+"."+filename,idClass);
            }else {
                if (filename.indexOf(".class") > 0){
                    Class<?> aClass = null;
                    try {
                        aClass = Class.forName(packageName1 + "." +
                                filename.substring(0,filename.lastIndexOf(".class")));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (aClass != null){
                        idClass.put(aClass.getSimpleName(),aClass);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        RabbitConfig config =new RabbitConfig();

        Map<String, Class<?>> idClass = new HashMap<>();
        config.scanPackages("com.cxp.springboot2rabbitmq.rabbitProducer.pojo",idClass);
        System.out.println(idClass);
    }
}
