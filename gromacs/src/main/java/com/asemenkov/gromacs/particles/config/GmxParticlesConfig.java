package com.asemenkov.gromacs.particles.config;

import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.gromacs.particles.annotations.ResidueAtom;
import com.asemenkov.gromacs.particles.exceptions.GmxAtomTypeException;
import com.asemenkov.gromacs.particles.utils.GmxAnglePredicate;
import com.asemenkov.gromacs.particles.utils.GmxAtomUtils;
import com.asemenkov.gromacs.particles.utils.GmxResidueReflectionData;
import com.asemenkov.utils.config.Factories.DuoFactory;
import com.asemenkov.utils.config.Factories.PentaFactory;
import com.asemenkov.utils.config.Factories.TetraFactory;
import com.asemenkov.utils.config.Factories.TriFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author asemenkov
 * @since Apr 22, 2018
 */
@Configuration
@Import(GmxParticlesReflectionConfig.class)
public class GmxParticlesConfig {

    private @Autowired GmxResidueReflectionData residueReflectionData;

    @Bean
    public TetraFactory<GmxAtom, Class<? extends GmxAtom>, String, Integer, float[]> atomFactory() {
        return this::getAtom;
    }

    @Bean
    public DuoFactory<GmxAtom[], Class<? extends GmxResidue>, float[]> residueAtomsFactory() {
        return this::getResidueAtoms;
    }

    @Bean
    public TriFactory<GmxResidue, Class<? extends GmxResidue>, Integer, float[]> residueFactoryWithoutAtoms() {
        return this::getResidue;
    }

    @Bean
    public TriFactory<GmxResidue, Class<? extends GmxResidue>, Integer, GmxAtom[]> residueFactoryWithAtoms() {
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
        Iterator<float[]> itr = Arrays.stream(residueReflectionData.getResiduePivotDeltas(clas)).iterator();
        return Arrays.stream(residueReflectionData.getResidueAtomsFields(clas)) //
                .map(field -> getAtom((Class<? extends GmxAtom>) field.getType(), //
                        field.getAnnotation(ResidueAtom.class).value(), 0, xyz)) //
                .peek(atom -> {
                    float[] deltas = itr.next();
                    GmxAtomUtils.shift(deltas[0], deltas[1], deltas[2], atom);
                }).toArray(GmxAtom[]::new);
    }

    private GmxResidue getResidue(Class<? extends GmxResidue> clas, Integer residueNo, float[] coordinates) {
        GmxAtom[] atoms = getResidueAtoms(clas, coordinates);
        return getResidue(clas, residueNo, atoms);
    }

    private GmxResidue getResidue(Class<? extends GmxResidue> clas, Integer residueNo, GmxAtom[] atoms) {
        try {
            GmxResidue residue = clas.getDeclaredConstructor().newInstance();
            residue.setAllAtomFields(residueReflectionData.getResidueAtomsFields(clas));
            residue.setAcceptorField(residueReflectionData.getResidueAcceptorField(clas));
            residue.setDonorFields(residueReflectionData.getResidueDonorsFields(clas));
            residue.setPivotField(residueReflectionData.getResiduePivotField(clas));
            residue.setResidueNo(residueNo);
            residue.setAllAtoms(atoms);
            return residue;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException //
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new GmxAtomTypeException(e);
        }
    }

    private GmxResidue[] getResidues(Class<? extends GmxResidue> clas, int[] residueNos, GmxAtom[][] atoms) {
        Field[] allAtomFields = residueReflectionData.getResidueAtomsFields(clas);
        Field[] donorFields = residueReflectionData.getResidueDonorsFields(clas);
        Field acceptorField = residueReflectionData.getResidueAcceptorField(clas);
        Field pivotField = residueReflectionData.getResiduePivotField(clas);

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

    private GmxAnglePredicate getAnglePredicate(GmxAtom vertex, GmxAtom left, GmxAtom right,
            Double expectedCos, Double precision) {

        if (expectedCos < -1 || expectedCos > 1) //
            throw new IllegalArgumentException("Invalid expected cosine value " + expectedCos);

        if (vertex == null || left == null || right == null) //
            throw new IllegalArgumentException("All 3 atoms must be specified for angle predicate");

        GmxAnglePredicate anglePredicate = new GmxAnglePredicate();
        anglePredicate.setVertex(vertex);
        anglePredicate.setLeft(left);
        anglePredicate.setRight(right);
        anglePredicate.setExpectedCos(expectedCos);
        anglePredicate.setPrecision(precision);
        return anglePredicate;
    }

}
