package com.asemenkov.command.config;

import com.asemenkov.command.execution.CmdExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author asemenkov
 * @since May 02, 2019
 */
@Configuration
@ComponentScan("com.asemenkov.command.gmx")
public class CmdGmxCommandsConfig {

    @Bean
    public CmdExecutor commandExecutor() {
        return new CmdExecutor();
    }
}
