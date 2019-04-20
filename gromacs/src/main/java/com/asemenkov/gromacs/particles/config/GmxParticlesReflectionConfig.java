package com.asemenkov.gromacs.particles.config;

import com.asemenkov.gromacs.particles.utils.GmxAtomReflectionData;
import com.asemenkov.gromacs.particles.utils.GmxResidueReflectionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */

@Configuration
class GmxParticlesReflectionConfig {

    private @Autowired Set<BeanDefinition> atomBeanDefinitions;
    private @Autowired Set<BeanDefinition> residueBeanDefinitions;

    @Bean
    public GmxAtomReflectionData atomReflectionData() {
        return new GmxAtomReflectionData(atomBeanDefinitions);
    }

    @Bean
    public GmxResidueReflectionData residueReflectionData() {
        return new GmxResidueReflectionData(residueBeanDefinitions);
    }

}
