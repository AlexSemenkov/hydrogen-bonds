package com.asemenkov.gromacs.frame;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;

import com.asemenkov.gromacs.annotations.ResidueAtom;
import com.asemenkov.gromacs.exceptions.GmxFrameException;
import com.asemenkov.gromacs.exceptions.GmxIoException;
import com.asemenkov.gromacs.io.GmxGroFileAtomLine;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.utils.Logger;

/**
 * @author asemenkov
 * @since Apr 23, 2018
 */
public class GmxFrameStructureBuilder {

    private @Autowired Map<String, Class<? extends GmxAtom>> atomClassesMap;
    private @Autowired Map<String, Class<? extends GmxResidue>> residueClassesMap;
    private @Autowired Map<Class<? extends GmxResidue>, Field[]> residueAtomsMap;

    private String description;
    private float[] box;

    private List<GmxGroFileAtomLine> groFileAtomLines;
    private Map<String, Integer> freeAtomsCountMap = new HashMap<>();
    private Map<String, Integer> residuesCountMap = new HashMap<>();
    private GmxResidue[] residues;
    private GmxAtom[] atoms;

    // ======== INTERFACE ========

    public GmxFrameStructureBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public GmxFrameStructureBuilder withBox(float[] box) {
        this.box = Arrays.copyOf(box, 3);
        return this;
    }

    public GmxFrameStructureBuilder withGroFileAtomLines(List<GmxGroFileAtomLine> groFileAtomLines) {
        this.groFileAtomLines = groFileAtomLines;
        return this;
    }

    public GmxFrameStructureBuilder withFreeAtoms(String atomAbbreviation, int count) {
        Integer atomsCount = freeAtomsCountMap.get(atomAbbreviation);
        this.freeAtomsCountMap.put(atomAbbreviation, atomsCount == null ? count : atomsCount + count);
        return this;
    }

    public GmxFrameStructureBuilder withResidues(String residueAbbreviation, int count) {
        Integer residuesCount = residuesCountMap.get(residueAbbreviation);
        this.residuesCountMap.put(residueAbbreviation, residuesCount == null ? count : residuesCount + count);
        return this;
    }

    public GmxFrameStructureBuilder withAtomsArray(GmxAtom[] atoms) {
        this.atoms = new GmxAtom[atoms.length];
        System.arraycopy(atoms, 0, this.atoms, 0, atoms.length);
        return this;
    }

    public GmxFrameStructureBuilder withResiduesArray(GmxResidue[] residues) {
        this.residues = new GmxResidue[residues.length];
        System.arraycopy(residues, 0, this.residues, 0, residues.length);
        return this;
    }

    // ======== BUILD METHODS ========

    public GmxFrameStructure buildFromGroFile() {
        validateGroFileAtomLines();
        validateBox();

        int atomsNum = groFileAtomLines.size();
        GmxFrameStructure frameStructure = new GmxFrameStructure(atomsNum);
        frameStructure.setDescription(description);
        frameStructure.setBox(box);

        reindexAtoms();
        groFileAtomLines.parallelStream().forEach(line -> {
            String abbreviation = line.getAtomAbbreviation();
            Class<? extends GmxAtom> atomClass = atomClassesMap.get(abbreviation);
            if (atomClass == null) throw new GmxIoException("No GmxAtom class found for: " + abbreviation);
            frameStructure.setAtomAbbreviation(line.getAtomNo(), abbreviation);
            frameStructure.setAtomsClass(line.getAtomNo(), atomClass);
        });

        List<GmxGroFileAtomLine> groFileResidueLines = groFileAtomLines.parallelStream() //
                .filter(line -> null != residueClassesMap.get(line.getResidueAbbreviation())) //
                .collect(Collectors.toList());

        if (groFileResidueLines.size() == 0) {
            resetBuilder();
            return frameStructure;
        }

        reindexResidues(groFileResidueLines);
        groFileResidueLines.stream() //
                .collect(Collectors.groupingByConcurrent(l -> residueClassesMap.get(l.getResidueAbbreviation())))//
                .forEach((key, value) -> frameStructure.setResidueIndexes(key, value.stream() //
                        .mapToInt(GmxGroFileAtomLine::getResidueNo) //
                        .distinct().toArray()));

        groFileResidueLines.stream() //
                .collect(Collectors.groupingByConcurrent(GmxGroFileAtomLine::getResidueNo)) //
                .forEach((key, value) -> frameStructure.setResidueAtoms(key, value.stream() //
                        .mapToInt(GmxGroFileAtomLine::getAtomNo) //
                        .sorted().toArray()));

        Logger.log("Frame structure successfully created from .gro file");
        resetBuilder();
        return frameStructure;
    }

    public GmxFrameStructure buildFromScratch() {
        validateAtleastOneMapIsNotEmpty();
        validateBox();

        int atomsNum = calculateAtomsNumFromMaps();
        GmxFrameStructure frameStructure = new GmxFrameStructure(atomsNum);
        frameStructure.setDescription(description);
        frameStructure.setBox(box);

        AtomicInteger atomsCounter = new AtomicInteger(0);
        AtomicInteger residuesCounter = new AtomicInteger(0);

        validateFreeAtomsMap();
        freeAtomsCountMap.forEach((key, value) -> IntStream.range(0, value).parallel().forEach(i -> {
            int atomNo = atomsCounter.getAndIncrement();
            frameStructure.setAtomsClass(atomNo, atomClassesMap.get(key));
            frameStructure.setAtomAbbreviation(atomNo, key);
        }));

        validateResiduesMap();
        residuesCountMap.forEach((key, value) -> {
            Class<? extends GmxResidue> residueClass = residueClassesMap.get(key);
            String[] abbreviations = Arrays.stream(residueAtomsMap.get(residueClass)) //
                    .map(field -> field.getAnnotation(ResidueAtom.class).value()) //
                    .toArray(String[]::new);

            int[] residueIndexes = IntStream.range(0, value).parallel().map(i -> {
                int residueLength = abbreviations.length;
                int residueNo = residuesCounter.getAndIncrement();
                int atomNo = atomsCounter.getAndAdd(residueLength);
                int sum = atomNo + residueLength;

                for (int j = 0; j < residueLength; j++) {
                    frameStructure.setAtomsClass(atomNo + j, atomClassesMap.get(abbreviations[j]));
                    frameStructure.setAtomAbbreviation(atomNo + j, abbreviations[j]);
                }

                frameStructure.setResidueAtoms(residueNo, IntStream.range(atomNo, sum).toArray());
                return residueNo;
            }).toArray();

            frameStructure.setResidueIndexes(residueClass, residueIndexes);
        });

        Logger.log("Frame structure successfully created from scratch");
        resetBuilder();
        return frameStructure;
    }

    public GmxFrameStructure buildFromArrays() {
        validateAtomsArray();
        validateBox();

        int atomsNum = atoms.length;
        GmxFrameStructure frameStructure = new GmxFrameStructure(atomsNum);
        frameStructure.setDescription(description);
        frameStructure.setBox(box);

        Arrays.stream(atoms).parallel() //
                .peek(atom -> frameStructure.setAtomAbbreviation(atom.getAtomNo(), atom.getAbbreviation())) //
                .forEach(atom -> frameStructure.setAtomsClass(atom.getAtomNo(), atom.getClass()));

        if (residues == null || residues.length == 0) {
            resetBuilder();
            return frameStructure;
        }

        validateResiduesArray();
        Arrays.stream(residues).parallel() //
                .collect(Collectors.groupingByConcurrent(GmxResidue::getClass)) //
                .forEach((key, value) -> frameStructure.setResidueIndexes(key, //
                        value.stream().mapToInt(GmxResidue::getResidueNo).toArray()));

        Arrays.stream(residues).parallel() //
                .collect(Collectors.groupingByConcurrent(GmxResidue::getResidueNo)) //
                .forEach((key, value) -> frameStructure.setResidueAtoms(key, //
                        Arrays.stream(value.get(0).getAllAtoms()).mapToInt(GmxAtom::getAtomNo).toArray()));

        Logger.log("Frame structure successfully created from arrays of atoms and residues");
        resetBuilder();
        return frameStructure;
    }

    // ======== SUPPORT METHODS ========

    private void reindexAtoms() {
        for (int i = 0; i < groFileAtomLines.size(); i++)
            groFileAtomLines.get(i).setAtomNo(i);
    }

    private void reindexResidues(List<GmxGroFileAtomLine> groFileResidueLines) {
        int oldResidueNo = groFileResidueLines.get(0).getResidueNo();
        int newResidueNo = groFileResidueLines.get(0).getAtomNo();

        for (GmxGroFileAtomLine groFileResidueLine : groFileResidueLines) {
            if (groFileResidueLine.getResidueNo() != oldResidueNo) {
                oldResidueNo = groFileResidueLine.getResidueNo();
                newResidueNo = groFileResidueLine.getAtomNo();
            }
            groFileResidueLine.setResidueNo(newResidueNo);
        }
    }

    private int calculateAtomsNumFromMaps() {
        int freeAtoms = freeAtomsCountMap.values().stream().mapToInt(Integer::intValue).sum();
        int residueAtoms = residuesCountMap.entrySet().stream().mapToInt(entry -> //
                residueAtomsMap.get(residueClassesMap.get(entry.getKey())).length * entry.getValue()).sum();
        return freeAtoms + residueAtoms;
    }

    private void resetBuilder() {
        Logger.log("Resetting frame structure builder...");
        groFileAtomLines = null;
        description = null;
        box = null;
        residues = null;
        atoms = null;
        freeAtomsCountMap = new HashMap<>();
        residuesCountMap = new HashMap<>();
        System.gc();
    }

    // ======== VALIDATION METHODS ========

    private void validateBox() {
        if (box == null || box.length != 3 || box[0] == 0 || box[1] == 0 || box[2] == 0) //
            throw new GmxIoException("Invalid box from .gro file: " + Arrays.toString(box));
    }

    private void validateGroFileAtomLines() {
        if (groFileAtomLines == null || groFileAtomLines.isEmpty()) //
            throw new GmxFrameException("gro file atom lines list is empty.");

        List<GmxGroFileAtomLine> errorLines = groFileAtomLines.parallelStream() //
                .filter(line -> atomClassesMap.get(line.getAtomAbbreviation()) == null) //
                .collect(Collectors.toList());

        if (errorLines.size() > 0) //
            throw new GmxIoException("The following lines in .gro file have unknown abbreviation:\n" + //
                    errorLines.stream().map(GmxGroFileAtomLine::toString).collect(Collectors.joining("\n")));
    }

    private void validateAtleastOneMapIsNotEmpty() {
        if (freeAtomsCountMap == null) throw new GmxFrameException("Free atoms count map is missing.");
        if (residuesCountMap == null) throw new GmxFrameException("Residues count map is missing.");
        if (freeAtomsCountMap.isEmpty() && residuesCountMap.isEmpty()) //
            throw new GmxFrameException("Both maps (atoms and residues) are empty.");
    }

    private void validateFreeAtomsMap() {
        List<String> errorAbbrs = freeAtomsCountMap.keySet().stream() //
                .filter(abbr -> atomClassesMap.get(abbr) == null) //
                .collect(Collectors.toList());

        if (errorAbbrs.size() > 0) //
            throw new GmxFrameException("The following GmxAtom abbreviations aren't recognized: " + //
                    errorAbbrs.stream().collect(Collectors.joining(", ")));
    }

    private void validateResiduesMap() {
        List<String> errorAbbrs = residuesCountMap.keySet().stream() //
                .filter(abbr -> residueClassesMap.get(abbr) == null) //
                .collect(Collectors.toList());

        if (errorAbbrs.size() > 0) //
            throw new GmxFrameException("The following GmxResidue abbreviations aren't recognized: " + //
                    errorAbbrs.stream().collect(Collectors.joining(", ")));
    }

    private void validateAtomsArray() {
        if (atoms == null || atoms.length == 0) throw new GmxFrameException("Atoms array is empty.");

        int[] errorAtoms = IntStream.range(0, atoms.length).parallel() //
                .filter(i -> atoms[i].getAtomNo() != i).toArray();

        if (errorAtoms.length > 0) //
            throw new GmxFrameException("The following GmxAtoms indexes are out of order: " + //
                    Arrays.toString(errorAtoms));
    }

    private void validateResiduesArray() {
        List<String> errorAtoms = Arrays.stream(residues).parallel() //
                .flatMap(residue -> Arrays.stream(residue.getAllAtoms())) //
                .filter(atom -> atom != atoms[atom.getAtomNo()]) //
                .map(GmxAtom::toString) //
                .collect(Collectors.toList());

        if (errorAtoms.size() > 0) //
            throw new GmxFrameException("The following residue's atoms aren't in atoms array:\n" + //
                    errorAtoms.stream().collect(Collectors.joining("\n")));

        AtomicInteger totalResidueAtoms = new AtomicInteger(0);
        long distinctResidueAtoms = Arrays.stream(residues).parallel() //
                .peek(residue -> totalResidueAtoms.getAndAdd(residue.getAllAtoms().length)) //
                .flatMap(residue -> Arrays.stream(residue.getAllAtoms())) //
                .distinct().count();

        if (distinctResidueAtoms != totalResidueAtoms.get()) //
            throw new GmxFrameException("Some residues compose the same GmxAtom object");
    }

}
