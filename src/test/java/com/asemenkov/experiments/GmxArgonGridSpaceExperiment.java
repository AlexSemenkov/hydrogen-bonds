package com.asemenkov.experiments;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.io.gro.GmxGroFileAtomLine;
import com.asemenkov.tests.GmxAbstractTest;
import com.asemenkov.utils.DecimalFormatter;
import com.asemenkov.utils.FileUtils;

/**
 * @author asemenkov
 * @since Jun 8, 2018
 */

@Test
public class GmxArgonGridSpaceExperiment extends GmxAbstractTest {

    private static final Path GRO_ARGON_GRID = Paths.get("D:", "gromacs", "argon-grid", "space", "argon-grid.gro");
    private static final Path CSV_GRID_SPACES = Paths.get("D:", "gromacs", "argon-grid", "space", "grid-spaces.csv");

    @Test
    public void findSpaceBetweenArgonAtoms() {
        List<GmxGroFileAtomLine> groFileAtomLines = groFileReader.readGroFileAtomLines(GRO_ARGON_GRID);

        GmxFrameStructure frameStructure = frameStructureFromGroFileBuilderSupplier.get() //
                .withDescription(groFileReader.readGroFileDescription(GRO_ARGON_GRID)) //
                .withBox(groFileReader.readGroFileBox(GRO_ARGON_GRID)) //
                .withGroFileAtomLines(groFileAtomLines) //
                .build();

        GmxFrameCoordinates frameCoordinates = frameCoordinatesFromGroFileBuilderSupplier.get() //
                .withGroFileAtomLines(groFileAtomLines) //
                .withFrameNo(1) //
                .build();

        GmxFrame frame = frameFactory.get(frameStructure, frameCoordinates);
        String spaces = Arrays.stream(frame.getAtoms()) //
                .map(atom -> frame.getNeighbourAtom(atom.getCoordinates(), 1).euclideanDistanceToAtom(atom))//
                .map(DecimalFormatter.DF_1_8::format) //
                .collect(Collectors.joining(","));

        FileUtils.writeWholeFile(CSV_GRID_SPACES, Collections.singletonList(spaces), true, true);
    }
}
