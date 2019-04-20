package com.asemenkov.procedure.config;

import com.asemenkov.gromacs.particles.annotations.Atom;
import com.asemenkov.gromacs.particles.annotations.Residue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * @author asemenkov
 * @since Sep 08, 2018
 */
@Configuration
@PropertySource("classpath:experiment.properties")
public class ProceduresConfig {

    private @Value("${package.residues}") String residuesPackage;
    private @Value("${package.atoms}") String atomsPackage;

    @Bean
    public Set<BeanDefinition> atomBeanDefinitions() {
        if (atomsPackage == null || atomsPackage.length() == 0) return null;
        ClassPathScanningCandidateComponentProvider scanner = //
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Atom.class));
        return scanner.findCandidateComponents(atomsPackage);
    }

    @Bean
    public Set<BeanDefinition> residueBeanDefinitions() {
        if (residuesPackage == null || residuesPackage.length() == 0) return null;
        ClassPathScanningCandidateComponentProvider scanner = //
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Residue.class));
        return scanner.findCandidateComponents(residuesPackage);
    }
}
