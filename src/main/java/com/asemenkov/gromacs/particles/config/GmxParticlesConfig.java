package com.asemenkov.gromacs.particles.config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import com.asemenkov.gromacs.particles.GmxAnglePredicate;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.asemenkov.gromacs.particles.annotations.ResidueAtom;
import com.asemenkov.gromacs.particles.exceptions.GmxAtomTypeException;
import com.asemenkov.utils.Factories.DuoFactory;
import com.asemenkov.utils.Factories.PentaFactory;
import com.asemenkov.utils.Factories.TetraFactory;
import com.asemenkov.utils.Factories.TriFactory;

/**
 * @author asemenkov
 * @since Apr 22, 2018
 */
@Configuration
@Import({ GmxAtomReflectionConfig.class, GmxResidueReflectionConfig.class })
public class GmxParticlesConfig {

    private @Autowired Map<Class<? extends GmxResidue>, Field[]> residueAtomsMap;
    private @Autowired Map<Class<? extends GmxResidue>, Field[]> residueDonorsMap;
    private @Autowired Map<Class<? extends GmxResidue>, Field> residueAcceptorMap;
    private @Autowired Map<Class<? extends GmxResidue>, Field> residuePivotMap;
    private @Autowired Map<Class<? extends GmxResidue>, float[][]> residuePivotDeltasMap;

    @Bean
    public TetraFactory<GmxAtom, Class<? extends GmxAtom>, String, Integer, float[]> atomFactory() {
        return this::getAtom;
    }

    @Bean
    public DuoFactory<GmxAtom[], Class<? extends GmxResidue>, float[]> residueAtomsFactory() {
        return this::getResidueAtoms;
    }

    @Bean
    public TriFactory<GmxResidue, Class<? extends GmxResidue>, Integer, GmxAtom[]> residueFactory() {
        return this::getResidue;
    }

    @Bean
    public TriFactory<GmxResidue[], Class<? extends GmxResidue>, int[], GmxAtom[][]> residuesFactory() {
        return this::getResidues;
    }

    @Bean
    public PentaFactory<GmxAnglePredicate, GmxAtom, GmxAtom, GmxAtom, Double, Double> anglePredicateFactory() {
        return this::getAnglePredicate;
    }

    // ======== REALIZATION ========

    private GmxAtom getAtom(Class<? extends GmxAtom> clas, String abbreviation, Integer atomNo, float[] xyz) {
        try {
            GmxAtom atom = clas.getDeclaredConstructor().newInstance();
            atom.setAbbreviation(abbreviation);
            atom.setCoordinates(xyz);
            atom.setAtomNo(atomNo);
            return atom;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException //
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new GmxAtomTypeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private GmxAtom[] getResidueAtoms(Class<? extends GmxResidue> clas, float[] xyz) {
        Iterator<float[]> deltasIterator = Arrays.stream(residuePivotDeltasMap.get(clas)).iterator();
        return Arrays.stream(residueAtomsMap.get(clas)) //
                .map(field -> getAtom((Class<? extends GmxAtom>) field.getType(), //
                        field.getAnnotation(ResidueAtom.class).value(), 0, xyz)) //
                .peek(atom -> {
                    float[] deltas = deltasIterator.next();
                    atom.shift(deltas[0], deltas[1], deltas[2]);
                }).toArray(GmxAtom[]::new);
    }

    private GmxResidue getResidue(Class<? extends GmxResidue> clas, Integer residueNo, GmxAtom[] atoms) {
        try {
            GmxResidue residue = clas.getDeclaredConstructor().newInstance();
            residue.setAllAtomFields(residueAtomsMap.get(clas));
            residue.setAcceptorField(residueAcceptorMap.get(clas));
            residue.setDonorFields(residueDonorsMap.get(clas));
            residue.setPivotField(residuePivotMap.get(clas));
            residue.setResidueNo(residueNo);
            residue.setAllAtoms(atoms);
            return residue;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException //
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new GmxAtomTypeException(e);
        }
    }

    private GmxResidue[] getResidues(Class<? extends GmxResidue> clas, int[] residueNos, GmxAtom[][] atoms) {
        Field[] allAtomFields = residueAtomsMap.get(clas);
        Field[] donorFields = residueDonorsMap.get(clas);
        Field acceptorField = residueAcceptorMap.get(clas);
        Field pivotField = residuePivotMap.get(clas);

        GmxResidue[] residues = new GmxResidue[residueNos.length];
        try {
            for (int i = 0; i < residueNos.length; i++) {
                residues[i] = clas.getDeclaredConstructor().newInstance();
                residues[i].setAllAtomFields(allAtomFields);
                residues[i].setAcceptorField(acceptorField);
                residues[i].setDonorFields(donorFields);
                residues[i].setPivotField(pivotField);
                residues[i].setResidueNo(residueNos[i]);
                residues[i].setAllAtoms(atoms[i]);
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException //
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new GmxAtomTypeException(e);
        }
        return residues;
    }

    private GmxAnglePredicate getAnglePredicate(GmxAtom vertex, GmxAtom point1, GmxAtom point2, Double expectedCos, Double precision) {

        if (expectedCos < -1 || expectedCos > 1) //
            throw new IllegalArgumentException("Invalid expected cosine value " + expectedCos);

        if (vertex == null || point1 == null || point2 == null) //
            throw new IllegalArgumentException("All 3 atoms must be specified for angle predicate");

        GmxAnglePredicate anglePredicate = new GmxAnglePredicate();
        anglePredicate.setVertex(vertex);
        anglePredicate.setPoint1(point1);
        anglePredicate.setPoint2(point2);
        anglePredicate.setExpectedCos(expectedCos);
        anglePredicate.setPrecision(precision);
        return anglePredicate;
    }

}
