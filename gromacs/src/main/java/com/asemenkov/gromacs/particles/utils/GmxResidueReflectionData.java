package com.asemenkov.gromacs.particles.utils;

import com.asemenkov.gromacs.io.gro.GmxGroFileAtomLine;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.gromacs.particles.annotations.*;
import com.asemenkov.gromacs.particles.exceptions.GmxAnnotationException;
import com.asemenkov.utils.io.Logger;
import org.springframework.beans.factory.config.BeanDefinition;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author asemenkov
 * @since Jan 06, 2019
 */
public class GmxResidueReflectionData {

    private final Map<String, Class<? extends GmxResidue>> residueClassesMap = new HashMap<>();
    private final Map<Class<? extends GmxResidue>, Field[]> residueAtomsMap = new HashMap<>();
    private final Map<Class<? extends GmxResidue>, Field[]> residueDonorsMap = new HashMap<>();
    private final Map<Class<? extends GmxResidue>, Field> residueAcceptorMap = new HashMap<>();
    private final Map<Class<? extends GmxResidue>, Field> residuePivotMap = new HashMap<>();
    private final Map<Class<? extends GmxResidue>, float[][]> residuePivotDeltasMap = new HashMap<>();

    public GmxResidueReflectionData(Set<BeanDefinition> residueBeanDefinitions) {
        if (residueBeanDefinitions == null || residueBeanDefinitions.isEmpty()) //
            throw new GmxAnnotationException("There are no @Residue classes in residues package");

        initResidueClassesMap(residueBeanDefinitions);
        initResidueAtomsMap();
        initResidueDonorsMap();
        initResidueAcceptorMap();
        initResiduePivotMap();
        initResiduePivotDeltasMap();

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

    // ======== INTERFACE ========

    public Class<? extends GmxResidue> getResidueClass(String abbreviation) {
        return residueClassesMap.get(abbreviation);
    }

    public Class<? extends GmxResidue> getResidueClass(GmxGroFileAtomLine groFileAtomLine) {
        return residueClassesMap.get(groFileAtomLine.getResidueAbbreviation());
    }

    public boolean isAbbreviationAbsent(String abbreviation) {
        return residueClassesMap.get(abbreviation) == null;
    }

    public boolean isAbbreviationPresent(GmxGroFileAtomLine groFileAtomLine) {
        return residueClassesMap.get(groFileAtomLine.getResidueAbbreviation()) != null;
    }

    public Field[] getResidueAtomsFields(Class<? extends GmxResidue> residueClass) {
        return residueAtomsMap.get(residueClass);
    }

    public Field[] getResidueDonorsFields(Class<? extends GmxResidue> residueClass) {
        return residueDonorsMap.get(residueClass);
    }

    public Field getResidueAcceptorField(Class<? extends GmxResidue> residueClass) {
        return residueAcceptorMap.get(residueClass);
    }

    public Field getResiduePivotField(Class<? extends GmxResidue> residueClass) {
        return residuePivotMap.get(residueClass);
    }

    public float[][] getResiduePivotDeltas(Class<? extends GmxResidue> residueClass) {
        return residuePivotDeltasMap.get(residueClass);
    }

    // ======== INITIALIZATION ========

    private void initResidueClassesMap(Set<BeanDefinition> beans) {
        beans.stream().map(this::mapBeanToResidueClass).forEach(clas -> //
                residueClassesMap.put(clas.getAnnotation(Residue.class).value(), clas));
    }

    @SuppressWarnings("unchecked")
    private Class<? extends GmxResidue> mapBeanToResidueClass(BeanDefinition beanDefinition) {
        try {
            return (Class<? extends GmxResidue>) Class.forName(beanDefinition.getBeanClassName());
        } catch (ClassNotFoundException exception) {
            throw new GmxAnnotationException(exception);
        } catch (ClassCastException exception) {
            throw new GmxAnnotationException("Some @Residue class isn't GmxResidue", exception);
        }
    }

    private void initResidueAtomsMap() {
        residueClassesMap.values().forEach(clas -> //
                residueAtomsMap.put(clas, Arrays.stream(clas.getDeclaredFields()) //
                        .filter(field -> field.isAnnotationPresent(ResidueAtom.class)) //
                        .toArray(Field[]::new)));
    }

    private void initResidueDonorsMap() {
        residueClassesMap.values().forEach(clas -> //
                residueDonorsMap.put(clas, Arrays.stream(clas.getDeclaredFields()) //
                        .filter(field -> field.isAnnotationPresent(Donor.class)) //
                        .toArray(Field[]::new)));
    }

    private void initResidueAcceptorMap() {
        residueClassesMap.values().stream() //
                .filter(clas -> Arrays.stream(clas.getDeclaredFields()) //
                        .anyMatch(field -> field.isAnnotationPresent(Acceptor.class))) //
                .forEach(clas -> residueAcceptorMap.put(clas, Arrays.stream(clas.getDeclaredFields()) //
                        .filter(field -> field.isAnnotationPresent(Acceptor.class)) //
                        .findFirst().orElseThrow(GmxAnnotationException::new)));
    }

    private void initResiduePivotMap() {
        residueClassesMap.values().stream() //
                .filter(clas -> Arrays.stream(clas.getDeclaredFields()) //
                        .anyMatch(field -> field.isAnnotationPresent(Pivot.class))) //
                .forEach(clas -> residuePivotMap.put(clas, Arrays.stream(clas.getDeclaredFields()) //
                        .filter(field -> field.isAnnotationPresent(Pivot.class)) //
                        .findFirst().orElseThrow(GmxAnnotationException::new)));
    }

    private void initResiduePivotDeltasMap() {
        residueClassesMap.values().forEach(clas -> //
                residuePivotDeltasMap.put(clas, Arrays.stream(residueAtomsMap.get(clas)) //
                        .map(field -> {
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
                .map(Map.Entry::getKey).collect(Collectors.toSet());

        if (!errorClasses.isEmpty()) //
            throw new GmxAnnotationException("@ResidueAtoms needed: " + errorClasses.stream() //
                    .map(Class::getSimpleName).collect(Collectors.joining(", ")));
    }

    private void validateResidueDonorsMap() {
        Set<Class<? extends GmxResidue>> errorClasses = residueDonorsMap.entrySet().stream() //
                .filter(entry -> Arrays.stream(entry.getValue()) //
                        .anyMatch(field -> !field.isAnnotationPresent(ResidueAtom.class))) //
                .map(Map.Entry::getKey) //
                .collect(Collectors.toSet());

        if (!errorClasses.isEmpty()) throw new GmxAnnotationException(
                "@Dondor must be @ResidueAtom as well: " + //
                        errorClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));
    }

    private void validateResidueAcceptorMap() {
        Set<Class<? extends GmxResidue>> errorClasses = residueAcceptorMap.entrySet().stream() //
                .filter(entry -> !entry.getValue().isAnnotationPresent(ResidueAtom.class)) //
                .map(Map.Entry::getKey).collect(Collectors.toSet());

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

        if (!errorClasses.isEmpty()) throw new GmxAnnotationException(
                "GmxResidue must have @Pivot atom: " + //
                        errorClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));
    }

    private void validateResiduePivotDeltasMap() {
        Set<Class<? extends GmxResidue>> errorClasses = residuePivotDeltasMap.entrySet().stream() //
                .filter(entry -> Arrays.stream(entry.getValue()) //
                        .filter(coord -> coord[0] == 0 && coord[1] == 0 && coord[2] == 0) //
                        .count() != 1) //
                .map(Map.Entry::getKey) //
                .collect(Collectors.toSet());

        if (!errorClasses.isEmpty()) throw new GmxAnnotationException(
                "Wrong num of @PivotDelta in classes: " + //
                        errorClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));
    }
}
