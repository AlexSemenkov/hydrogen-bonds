package com.asemenkov.gromacs.particles.utils;

import com.asemenkov.gromacs.io.gro.GmxGroFileAtomLine;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.annotations.Atom;
import com.asemenkov.gromacs.particles.exceptions.GmxAnnotationException;
import com.asemenkov.utils.io.Logger;
import org.springframework.beans.factory.config.BeanDefinition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author asemenkov
 * @since Jan 06, 2019
 */
public class GmxAtomReflectionData {

    private final Map<String, Class<? extends GmxAtom>> atomClassesMap = new HashMap<>();

    public GmxAtomReflectionData(Set<BeanDefinition> atomBeanDefinitions) {
        if (atomBeanDefinitions == null || atomBeanDefinitions.isEmpty()) //
            throw new GmxAnnotationException("There are no @Atom classes in atom package");

        initAtomClassesMap(atomBeanDefinitions);
        Logger.log(atomClassesMap);
        validateAtomsMap();
    }

    // ======== INTERFACE ========

    public Class<? extends GmxAtom> getAtomClass(String abbreviation) {
        return atomClassesMap.get(abbreviation);
    }

    public Class<? extends GmxAtom> getAtomClass(GmxGroFileAtomLine groFileAtomLine) {
        return atomClassesMap.get(groFileAtomLine.getAtomAbbreviation());
    }

    public boolean isAbbreviationAbsent(String abbreviation) {
        return atomClassesMap.get(abbreviation) == null;
    }

    public boolean isAbbreviationAbsent(GmxGroFileAtomLine groFileAtomLine) {
        return atomClassesMap.get(groFileAtomLine.getAtomAbbreviation()) == null;
    }

    // ======== INITIALIZATION ========

    private void initAtomClassesMap(Set<BeanDefinition> beans) {
        Set<Class<? extends GmxAtom>> atomClasses = beans.stream() //
                .map(this::mapBeanToAtomClass).collect(Collectors.toSet());

        atomClasses.forEach(clas -> Arrays.stream(clas.getAnnotation(Atom.class).abbreviations()) //
                .forEach(abbreviation -> atomClassesMap.put(abbreviation, clas)));
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
                .map(Map.Entry::getKey).collect(Collectors.joining(", "));

        if (ambiguousAbbreviations != null && ambiguousAbbreviations.length() > 0) //
            throw new GmxAnnotationException("The following abbreviations are present in multiple" + //
                    " GmxAtom classes: " + ambiguousAbbreviations);
    }
}
