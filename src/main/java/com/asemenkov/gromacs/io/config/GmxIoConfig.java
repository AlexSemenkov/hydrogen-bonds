package com.asemenkov.gromacs.io.config;

import com.asemenkov.gromacs.io.GmxXtcFileNativeReader;
import com.asemenkov.gromacs.io.gro.GmxGroFileReader;
import com.asemenkov.gromacs.io.gro.GmxGroFileWriter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author asemenkov
 * @since Apr 22, 2018
 */
@Configuration
public class GmxIoConfig {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public GmxGroFileReader groFileReader() {
        return new GmxGroFileReader();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public GmxGroFileWriter groFileWriter() {
        return new GmxGroFileWriter();
    }

    @Bean
    public GmxXtcFileNativeReader xtcFileNativeReader() {
        return new GmxXtcFileNativeReader();
    }

}
