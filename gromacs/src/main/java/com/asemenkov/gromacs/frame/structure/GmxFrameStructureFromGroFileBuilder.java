package com.asemenkov.gromacs.frame.structure;

import com.asemenkov.gromacs.frame.exceptions.GmxFrameException;
import com.asemenkov.gromacs.io.exceptions.GmxIoException;
import com.asemenkov.gromacs.io.gro.GmxGroFileAtomLine;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.utils.io.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author asemenkov
 * @since Aug 01, 2018
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GmxFrameStructureFromGroFileBuilder
        extends GmxAbstractFrameStructureBuilder<GmxFrameStructureFromGroFileBuilder> {

    private List<GmxGroFileAtomLine> groFileAtomLines;

    // ======== INTERFACE ========

    public GmxFrameStructureFromGroFileBuilder withGroFileAtomLines(
            List<GmxGroFileAtomLine> groFileAtomLines) {
        this.groFileAtomLines = groFileAtomLines;
        return this;
    }

    @Override
    public GmxFrameStructure build() {
        validateGroFileAtomLines();
        validateBox();

        int atomsNum = groFileAtomLines.size();
        GmxFrameStructure frameStructure = new GmxFrameStructure(atomsNum);
        frameStructure.setDescription(description);
        frameStructure.setBox(box);

        reindexAtoms();
        groFileAtomLines.parallelStream().forEach(line -> {
            String abbreviation = line.getAtomAbbreviation();
            Class<? extends GmxAtom> atomClass = atomReflectionData.getAtomClass(abbreviation);
            if (atomClass == null) throw new GmxIoException("No GmxAtom class found for: " + abbreviation);
            frameStructure.setAtomAbbreviation(line.getAtomNo(), abbreviation);
            frameStructure.setAtomsClass(line.getAtomNo(), atomClass);
        });

        List<GmxGroFileAtomLine> groFileResidueLines = groFileAtomLines.parallelStream() //
                .filter(residueReflectionData::isAbbreviationPresent) //
                .collect(Collectors.toList());

        if (groFileResidueLines.size() == 0) return frameStructure;

        reindexResidues(groFileResidueLines);
        groFileResidueLines.stream() //
                .collect(Collectors.groupingByConcurrent(residueReflectionData::getResidueClass)) //
                .forEach((key, value) -> frameStructure.setResidueIndexes(key, value.stream() //
                        .mapToInt(GmxGroFileAtomLine::getResidueNo) //
                        .distinct().toArray()));

        groFileResidueLines.stream() //
                .collect(Collectors.groupingByConcurrent(GmxGroFileAtomLine::getResidueNo)) //
                .forEach((key, value) -> frameStructure.setResidueAtoms(key, value.stream() //
                        .mapToInt(GmxGroFileAtomLine::getAtomNo) //
                        .sorted().toArray()));

        Logger.log("Frame structure successfully created from .gro file");
        return frameStructure;
    }

    // ======== VALIDATION METHODS ========

    private void validateGroFileAtomLines() {
        if (groFileAtomLines == null || groFileAtomLines.isEmpty()) //
            throw new GmxFrameException("gro file atom lines list is empty.");

        List<GmxGroFileAtomLine> errorLines = groFileAtomLines.parallelStream() //
                .filter(atomReflectionData::isAbbreviationAbsent) //
                .collect(Collectors.toList());

        if (errorLines.size() > 0) //
            throw new GmxIoException("The following lines in .gro file have unknown abbreviation:\n" + //
                    errorLines.stream().map(GmxGroFileAtomLine::toString).collect(Collectors.joining("\n")));
    }

    // ======== SUPPORT METHODS ========

    private void reindexAtoms() {
        IntStream.range(0, groFileAtomLines.size()).forEach(i -> groFileAtomLines.get(i).setAtomNo(i));
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

}
