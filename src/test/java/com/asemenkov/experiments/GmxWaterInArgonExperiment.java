package com.asemenkov.experiments;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.DoubleStream;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.io.GmxGroFileAtomLine;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.tests.GmxAbstractTest;
import com.asemenkov.waterinargon.GmxResidueH2O;

/**
 * @author asemenkov
 * @since Jun 8, 2018
 */
@Test
public class GmxWaterInArgonExperiment extends GmxAbstractTest {

    private static final Path GRO_ARGON_GRID = Paths.get("D:", "gromacs", "argon-grid", "space", "argon-grid.gro");
    private static final Path GRO_WATER_IN_ARGON = Paths.get("D:", "gromacs", "water-in-argon");
    private static final String WATER_13_STEP_0 = "argon-grid-water-13-step-0.gro";
    private static final String WATER_7_STEP_1 = "argon-grid-water-7-step-1.gro";

    private GmxFrame frame;

    @BeforeMethod
    public void buildFrame() {
        List<GmxGroFileAtomLine> groFileAtomLines = groFileReaderAndWriter.readGroFileAtomLines(GRO_ARGON_GRID);

        GmxFrameStructure frameStructure = frameStructureFromGroFileBuilderSupplier.get() //
                .withDescription(groFileReaderAndWriter.readGroFileDescription(GRO_ARGON_GRID)) //
                .withBox(groFileReaderAndWriter.readGroFileBox(GRO_ARGON_GRID)) //
                .withGroFileAtomLines(groFileAtomLines) //
                .build();

        GmxFrameCoordinates frameCoordinates = frameCoordinatesFromGroFileBuilderSupplier.get() //
                .withGroFileAtomLines(groFileAtomLines) //
                .withFrameNo(1) //
                .build();

        frame = frameFactory.get(frameStructure, frameCoordinates);
    }

    @Test
    public void replaceThirteenAtomsInArgonGridZeroStep() {
        GmxAtom atom = frame.getAtoms()[362];
        GmxAtom[] atoms = frame.getAtomsSortedByDistanceToPoint(atom.getCoordinates());
        frame.replaceAtomsWithResidues(GmxResidueH2O.class, Arrays.copyOf(atoms, 13));
        groFileReaderAndWriter.writeGroFile(frame, GRO_WATER_IN_ARGON, WATER_13_STEP_0);
    }

    @Test
    public void replaceSevenAtomsInArgonGridOneStep() {
        GmxAtom atom = frame.getAtoms()[362];

        GmxAtom[] atoms = frame.getAtomsSortedByDistanceToPoint(atom.getCoordinates());
        GmxAtom[] replace = new GmxAtom[] { atoms[0], atoms[13], atoms[14], atoms[15], atoms[16], atoms[19], atoms[21] };

        frame.replaceAtomsWithResidues(GmxResidueH2O.class, replace);
        groFileReaderAndWriter.writeGroFile(frame, GRO_WATER_IN_ARGON, WATER_7_STEP_1);
    }

    @Test(enabled = false)
    public void helpToFindGridStep() {
        GmxAtom atom = frame.getAtoms()[362];
        Set<GmxAtom> neighbours = new HashSet<>();

        GmxAtom[] sorted = frame.getAtomsSortedByDistanceToPoint(atom.getCoordinates());
        GmxAtom[] neighbours1 = Arrays.copyOfRange(sorted, 1, 13);
        Collections.addAll(neighbours, neighbours1);

        Arrays.stream(neighbours1).forEach(n -> {
            GmxAtom[] neighbours2 = frame.getAtomsSortedByDistanceToPoint(n.getCoordinates());
            neighbours2 = Arrays.copyOfRange(neighbours2, 1, 13);
            Collections.addAll(neighbours, neighbours2);
        });

        System.out.println(neighbours.size());

        for (int i = 13; i < 100; i++) {
            double[] angles = sortedAngles(neighbours, neighbours1, atom, sorted[i]);
            double geometricMean = getGeometricMeanErrorOfFourAngles(angles);

            System.out.println("================ ATOM-" + i + " ================");
            System.out.println("angles: " + Arrays.toString(angles));
            System.out.println("geometric mean error of the first four angles: " + geometricMean);
            System.out.println("distance to core atom: " + atom.euclideanDistanceToAtom(sorted[i]));
            System.out.println();
        }
    }

    private double[] sortedAngles(Set<GmxAtom> neighboursAll, GmxAtom[] neighbours1, GmxAtom atom, GmxAtom atom13) {
        return neighboursAll.stream() //
                .filter(n -> n != atom) //
                .filter(n -> Arrays.stream(neighbours1).anyMatch(n2 -> n == n2)) //
                .mapToDouble(n -> atom.cosOfAngleBetweenTwoAtoms(atom13, n)) //
                .map(cos -> Math.toDegrees(Math.acos(cos))) //
                .sorted() //
                .toArray();
    }

    private double getGeometricMeanErrorOfFourAngles(double[] cosines) {
        double avg = DoubleStream.of(Arrays.copyOf(cosines, 4)).average().orElse(0);
        double multi = DoubleStream.of(Arrays.copyOf(cosines, 4)) //
                .map(cos -> Math.abs(cos - avg)) //
                .reduce(0, (a, b) -> a * b); //
        return Math.pow(multi, 0.25);
    }

}
