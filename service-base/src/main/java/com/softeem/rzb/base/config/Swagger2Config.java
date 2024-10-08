package com.softeem.rzb.base.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2 // 开启Swagger2功能
public class Swagger2Config {


    @Bean
    public Docket adminApiConfig() {

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("adminApi")
                .apiInfo(adminApiInfo())
                .select()
                //只显示admin路径下的页面
                .paths(Predicates.and(PathSelectors.regex("/admin/.*")))
                .build();

    }

    private ApiInfo adminApiInfo() {

        return new ApiInfoBuilder()
                .title("融资宝后台管理系统-API文档")
                .description("本文档描述了融资宝后台管理系统接口")
                .version("1.6")
                .contact(new Contact("peijie", "http://www.softeem.com", "176128798@qq.com"))
                .build();
    }

    @Bean
    public Docket webApiConfig(){
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("webApi")
                .apiInfo(webApiInfo())
                .select()
                .paths(Predicates.and(PathSelectors.regex("/api/.*")))
                .build();
    }

    private ApiInfo webApiInfo(){
        return new ApiInfoBuilder()
                .title("融资宝网站API文档")
                .description("本文档描述了融资宝网站各个模块的接口的调用方式")
                .version("1.6")
                .contact(new Contact("pei", "http://www.softeem.com", "admin@softeem.com"))
                .build();
    }
}