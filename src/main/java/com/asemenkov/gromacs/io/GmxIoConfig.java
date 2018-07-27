package com.asemenkov.gromacs.io;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asemenkov.gromacs.io.GmxGroFileReaderAndWriter;
import org.springframework.context.annotation.Scope;

/**
 * @author asemenkov
 * @since Apr 22, 2018
 */
@Configuration
public class GmxIoConfig {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public GmxGroFileReaderAndWriter groFileReaderAndWriter() {
        return new GmxGroFileReaderAndWriter();
    }

    @Bean
    public GmxXtcFileNativeReader xtcFileNativeReader() {
        return new GmxXtcFileNativeReader();
    }

}
