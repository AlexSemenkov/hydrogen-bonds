package com.asemenkov.gromacs.particles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.asemenkov.gromacs.annotations.Atom;
import com.asemenkov.gromacs.exceptions.GmxAnnotationException;
import com.asemenkov.utils.Logger;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */

@Configuration
@PropertySource("classpath:gromacs.properties")
public class GmxAtomReflectionConfig {

    private Map<String, Class<? extends GmxAtom>> atomClassesMap;

    private @Value("${package.atoms}") String atomsPackage;

    @Bean
    public Map<String, Class<? extends GmxAtom>> atomClassesMap() {
        return atomClassesMap;
    }

    // ======== INITIALIZATION ========

    @PostConstruct
    private void init() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Atom.class));
        Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(atomsPackage);

        if (beanDefinitions == null || beanDefinitions.isEmpty()) //
            throw new GmxAnnotationException("There are no @Atom classes in " + atomsPackage);

        atomClassesMap = getAtomClassesMap(beanDefinitions);
        Logger.log(atomClassesMap);
        validateAtomsMap();
    }

    // ======== SUPPORT METHODS ========

    private Map<String, Class<? extends GmxAtom>> getAtomClassesMap(Set<BeanDefinition> beans) {
        Map<String, Class<? extends GmxAtom>> atomClassesMap = new HashMap<>();

        Set<Class<? extends GmxAtom>> atomClasses = beans.stream() //
                .map(this::mapBeanToAtomClass).collect(Collectors.toSet());

        atomClasses //
                .forEach(clas -> Arrays.stream(clas.getAnnotation(Atom.class).abbreviations()) //
                        .forEach(abbreviation -> atomClassesMap.put(abbreviation, clas)));

        return atomClassesMap;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends GmxAtom> mapBeanToAtomClass(BeanDefinition beanDefinition) {
        try {
            return (Class<? extends GmxAtom>) Class.forName(beanDefinition.getBeanClassName());
        } catch (ClassNotFoundException exception) {
            throw new GmxAnnotationException(exception);
        } catch (ClassCastException exception) {
            throw new GmxAnnotationException("Some @Atom class isn't GmxAtom", exception);
        }
    }

    private void validateAtomsMap() {
        Map<String, Integer> map = atomClassesMap.keySet().stream() //
                .collect(Collectors.toMap(key -> key, key -> 0));

        atomClassesMap.values().stream().distinct() //
                .forEach(clas -> Arrays.stream(clas.getAnnotation(Atom.class).abbreviations()) //
                        .forEach(abbreviation -> map.put(abbreviation, map.get(abbreviation) + 1)));

        String ambiguousAbbreviations = map.entrySet().stream() //
                .filter(entry -> entry.getValue() > 1) //
                .map(Entry::getKey).collect(Collectors.joining(", "));

        if (ambiguousAbbreviations != null && ambiguousAbbreviations.length() > 0) //
            throw new GmxAnnotationException("The following abbreviations are present in multiple" + //
                    " GmxAtom classes: " + ambiguousAbbreviations);
    }

}
