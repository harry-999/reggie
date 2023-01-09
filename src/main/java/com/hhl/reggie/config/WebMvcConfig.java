package com.hhl.reggie.config;

import com.hhl.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /**
     * 设置静态资源映射（也就是告诉服务器，静态资源的位置）
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始静态资源映射。。。。。。");
//        /**表示多层目录  classpath代表resources的目录
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    /**
     * 扩展mvc框架的消息转化器
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("拓展消息转换器");
        //创建消息转化器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jackson将Java对象转为Json(设置自己的自定义消息转化器)
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转化器对象追加到mvc框架的转化器集合中，这里第一个形参表示转化器的执行顺序，设置为0表示应用自定义的转化器
        //转换器是有优先级顺序的，这里我们把自己定义的消息转换器设置为第一优先级，所以会优先使用我们的转换器来进行相关数据进行转换，如果我们的转换器没有匹配到相应的数据来转换，那么就会去寻找第二个优先级的转换器，以此类推
        converters.add(0,messageConverter);
    }
}
