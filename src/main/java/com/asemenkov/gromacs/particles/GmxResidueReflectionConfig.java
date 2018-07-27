package com.asemenkov.gromacs.particles;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.asemenkov.gromacs.exceptions.GmxAtomTypeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.asemenkov.gromacs.annotations.Acceptor;
import com.asemenkov.gromacs.annotations.Donor;
import com.asemenkov.gromacs.annotations.Pivot;
import com.asemenkov.gromacs.annotations.PivotDeltas;
import com.asemenkov.gromacs.annotations.Residue;
import com.asemenkov.gromacs.annotations.ResidueAtom;
import com.asemenkov.gromacs.exceptions.GmxAnnotationException;
import com.asemenkov.utils.Logger;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */

@Configuration
@PropertySource("classpath:gromacs.properties")
public class GmxResidueReflectionConfig {

    private Map<String, Class<? extends GmxResidue>> residueClassesMap;
    private Map<Class<? extends GmxResidue>, Field[]> residueAtomsMap;
    private Map<Class<? extends GmxResidue>, Field[]> residueDonorsMap;
    private Map<Class<? extends GmxResidue>, Field> residueAcceptorMap;
    private Map<Class<? extends GmxResidue>, Field> residuePivotMap;
    private Map<Class<? extends GmxResidue>, float[][]> residuePivotDeltasMap;

    @Value("${package.residues}") private String residuesPackage;

    @Bean
    public Map<String, Class<? extends GmxResidue>> residueClassesMap() {
        return residueClassesMap;
    }

    @Bean
    public Map<Class<? extends GmxResidue>, Field[]> residueAtomsMap() {
        return residueAtomsMap;
    }

    @Bean
    public Map<Class<? extends GmxResidue>, Field[]> residueDonorsMap() {
        return residueDonorsMap;
    }

    @Bean
    public Map<Class<? extends GmxResidue>, Field> residueAcceptorMap() {
        return residueAcceptorMap;
    }

    @Bean
    public Map<Class<? extends GmxResidue>, Field> residuePivotMap() {
        return residuePivotMap;
    }

    @Bean
    public Map<Class<? extends GmxResidue>, float[][]> residuePivotDeltasMap() {
        return residuePivotDeltasMap;
    }

    // ======== INITIALIZATION ========

    @PostConstruct
    private void init() {
        if (residuesPackage == null || residuesPackage.length() == 0) return;

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Residue.class));
        Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(residuesPackage);

        if (beanDefinitions == null || beanDefinitions.isEmpty()) //
            throw new GmxAnnotationException("There are no @Residue classes in " + residuesPackage);

        residueClassesMap = getResidueClassesMap(beanDefinitions);
        residueAtomsMap = getResidueAtomsMap(residueClassesMap.values());
        residueDonorsMap = getResidueDonorsMap(residueClassesMap.values());
        residueAcceptorMap = getResidueAcceptorMap(residueClassesMap.values());
        residuePivotMap = getResiduePivotMap(residueClassesMap.values());
        residuePivotDeltasMap = getResiduePivotDeltasMap(residueClassesMap.values());

        Logger.log("residueClassesMap:");
        residueClassesMap.entrySet().forEach(Logger::log);

        Logger.log("residueAtomsMap:");
        residueAtomsMap.forEach((key, value) -> Logger.log(key.getSimpleName() + " = " + //
                Arrays.stream(value).map(Field::getName).collect(Collectors.joining(" | "))));

        Logger.log("residueDonorsMap:");
        residueDonorsMap.forEach((key, value) -> Logger.log(key.getSimpleName() + " = " + //
                Arrays.stream(value).map(Field::getName).collect(Collectors.joining(" | "))));

        Logger.log("residueAcceptorMap:");
        residueAcceptorMap.forEach((key, value) -> Logger.log(key.getSimpleName() + " = " + value.getName()));

        Logger.log("residuePivotMap:");
        residuePivotMap.forEach((key, value) -> Logger.log(key.getSimpleName() + " = " + value.getName()));

        validateResidueAtomsMap();
        validateResidueDonorsMap();
        validateResidueAcceptorMap();
        validateResidueDonorAndAcceptorInterception();
        validateResiduePivotMap();
        validateResiduePivotDeltasMap();
    }

    // ======== SUPPORT METHODS ========

    private Map<String, Class<? extends GmxResidue>> getResidueClassesMap(Set<BeanDefinition> beans) {
        return beans.stream().map(this::mapBeanToResdueClass) //
                .collect(Collectors.toMap(clas -> clas.getAnnotation(Residue.class).value(), clas -> clas));
    }

    @SuppressWarnings("unchecked")
    private Class<? extends GmxResidue> mapBeanToResdueClass(BeanDefinition beanDefinition) {
        try {
            return (Class<? extends GmxResidue>) Class.forName(beanDefinition.getBeanClassName());
        } catch (ClassNotFoundException exception) {
            throw new GmxAnnotationException(exception);
        } catch (ClassCastException exception) {
            throw new GmxAnnotationException("Some @Residue class isn't GmxResidue", exception);
        }
    }

    private Map<Class<? extends GmxResidue>, Field[]> getResidueAtomsMap(Collection<Class<? extends GmxResidue>> residueClasses) {
        return residueClasses.stream() //
                .collect(Collectors.toMap(clas -> clas, //
                        clas -> Arrays.stream(clas.getDeclaredFields()) //
                                .filter(field -> field.isAnnotationPresent(ResidueAtom.class)) //
                                .toArray(Field[]::new)));
    }

    private Map<Class<? extends GmxResidue>, Field[]> getResidueDonorsMap(Collection<Class<? extends GmxResidue>> residueClasses) {
        return residueClasses.stream() //
                .collect(Collectors.toMap(clas -> clas, //
                        clas -> Arrays.stream(clas.getDeclaredFields()) //
                                .filter(field -> field.isAnnotationPresent(Donor.class)) //
                                .toArray(Field[]::new)));
    }

    private Map<Class<? extends GmxResidue>, Field> getResidueAcceptorMap(Collection<Class<? extends GmxResidue>> residueClasses) {
        return residueClasses.stream() //
                .filter(clas -> Arrays.stream(clas.getDeclaredFields()) //
                        .anyMatch(field -> field.isAnnotationPresent(Acceptor.class))).collect(Collectors.toMap(clas -> clas, //
                        clas -> Arrays.stream(clas.getDeclaredFields()) //
                                .filter(field -> field.isAnnotationPresent(Acceptor.class)) //
                                .findFirst().orElseThrow(GmxAnnotationException::new)));
    }

    private Map<Class<? extends GmxResidue>, Field> getResiduePivotMap(Collection<Class<? extends GmxResidue>> residueClasses) {
        return residueClasses.stream() //
                .filter(clas -> Arrays.stream(clas.getDeclaredFields()) //
                        .anyMatch(field -> field.isAnnotationPresent(Pivot.class))).collect(Collectors.toMap(clas -> clas, //
                        clas -> Arrays.stream(clas.getDeclaredFields()) //
                                .filter(field -> field.isAnnotationPresent(Pivot.class)) //
                                .findFirst().orElseThrow(GmxAnnotationException::new)));
    }

    private Map<Class<? extends GmxResidue>, float[][]> getResiduePivotDeltasMap(Collection<Class<? extends GmxResidue>> residueClasses) {
        return residueClasses.stream().collect(Collectors.toMap(clas -> clas, clas -> //
                Arrays.stream(residueAtomsMap.get(clas)).map(field -> {
                    float[] toReturn = new float[3];
                    PivotDeltas pivotDeltas;
                    if ((pivotDeltas = field.getAnnotation(PivotDeltas.class)) != null) {
                        toReturn[0] = pivotDeltas.x();
                        toReturn[1] = pivotDeltas.y();
                        toReturn[2] = pivotDeltas.z();
                    }
                    return toReturn;
                }).toArray(float[][]::new)));
    }

    // ======== VALIDATIONS ========

    private void validateResidueAtomsMap() {
        Set<Class<? extends GmxResidue>> errorClasses = residueAtomsMap.entrySet().stream() //
                .filter(entry -> entry.getValue().length == 0) //
                .map(Entry::getKey).collect(Collectors.toSet());

        if (!errorClasses.isEmpty()) //
            throw new GmxAnnotationException("@ResidueAtoms needed: " + errorClasses.stream() //
                    .map(Class::getSimpleName).collect(Collectors.joining(", ")));
    }

    private void validateResidueDonorsMap() {
        Set<Class<? extends GmxResidue>> errorClasses = residueDonorsMap.entrySet().stream() //
                .filter(entry -> Arrays.stream(entry.getValue()) //
                        .anyMatch(field -> !field.isAnnotationPresent(ResidueAtom.class))) //
                .map(Entry::getKey) //
                .collect(Collectors.toSet());

        if (!errorClasses.isEmpty()) throw new GmxAnnotationException("@Dondor must be @ResidueAtom as well: " + //
                errorClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));
    }

    private void validateResidueAcceptorMap() {
        Set<Class<? extends GmxResidue>> errorClasses = residueAcceptorMap.entrySet().stream() //
                .filter(entry -> !entry.getValue().isAnnotationPresent(ResidueAtom.class)) //
                .map(Entry::getKey).collect(Collectors.toSet());

        if (!errorClasses.isEmpty()) //
            throw new GmxAnnotationException("@Acceptor must be @ResidueAtom as well: " + //
                    errorClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));
    }

    private void validateResidueDonorAndAcceptorInterception() {
        Set<Class<? extends GmxResidue>> errorClasses = residueDonorsMap.keySet().stream() //
                .filter(key -> Arrays.stream(residueDonorsMap.get(key)) //
                        .anyMatch(donor -> donor == residueAcceptorMap.get(key))) //
                .collect(Collectors.toSet());

        if (!errorClasses.isEmpty()) //
            throw new GmxAnnotationException("@Donor cannot be @Acceptor in the same time: " + //
                    errorClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));
    }

    private void validateResiduePivotMap() {
        Set<Class<? extends GmxResidue>> errorClasses = residuePivotMap.keySet().stream() //
                .filter(clas1 -> residueAtomsMap.keySet().stream().noneMatch(clas2 -> clas1 == clas2)) //
                .collect(Collectors.toSet());

        if (!errorClasses.isEmpty()) throw new GmxAnnotationException("GmxResidue must have @Pivot atom: " + //
                errorClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));
    }

    private void validateResiduePivotDeltasMap() {
        Set<Class<? extends GmxResidue>> errorClasses = residuePivotDeltasMap.entrySet().stream() //
                .filter(entry -> Arrays.stream(entry.getValue()) //
                        .filter(coord -> coord[0] == 0 && coord[1] == 0 && coord[2] == 0) //
                        .count() != 1) //
                .map(Entry::getKey) //
                .collect(Collectors.toSet());

        if (!errorClasses.isEmpty()) throw new GmxAnnotationException("Wrong num of @PivotDelta in classes: " + //
                errorClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));
    }

}
