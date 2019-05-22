package com.asemenkov.gromacs.particles.config;

import com.asemenkov.gromacs.particles.annotations.Atom;
import com.asemenkov.gromacs.particles.annotations.Residue;
import com.asemenkov.gromacs.particles.utils.GmxAtomReflectionData;
import com.asemenkov.gromacs.particles.utils.GmxResidueReflectionData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */

@Configuration
class GmxParticlesReflectionConfig {

    private @Value("${package.residues}") String residuesPackage;
    private @Value("${package.atoms}") String atomsPackage;

    @Bean
    public GmxAtomReflectionData atomReflectionData() {
        return new GmxAtomReflectionData(atomBeanDefinitions());
    }

    @Bean
    public GmxResidueReflectionData residueReflectionData() {
        return new GmxResidueReflectionData(residueBeanDefinitions());
    }

    // ======== SUPPORT METHODS ========

    private Set<BeanDefinition> atomBeanDefinitions() {
        if (atomsPackage == null || atomsPackage.length() == 0) return null;
        ClassPathScanningCandidateComponentProvider scanner = //
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Atom.class));
        return scanner.findCandidateComponents(atomsPackage);
    }

    private Set<BeanDefinition> residueBeanDefinitions() {
        if (residuesPackage == null || residuesPackage.length() == 0) return null;
        ClassPathScanningCandidateComponentProvider scanner = //
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Residue.class));
        return scanner.findCandidateComponents(residuesPackage);
    }

}
