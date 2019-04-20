package com.asemenkov.tests.frame;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.exceptions.GmxFrameException;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.frame.utils.GmxFrameUpdater;
import com.asemenkov.gromacs.io.gro.GmxGroFileAtomLine;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.particles.residues.GmxResidueH2O;
import com.asemenkov.tests.config.GmxAbstractTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author asemenkov
 * @since Mar 12, 2019
 */
@Test
public class GmxFrameUpdaterTest extends GmxAbstractTest {

    private static final String ATOMS_DUPLICATE = ".+ duplicates in the frame atoms: \\d+";
    private static final String RESIDUES_DUPLICATE = ".+ duplicates in the frame residues: \\d+";
    private GmxFrame frame1, frame2; // frame1 for positive testing, frame2 for negative

    @BeforeClass
    public void initFrame() {
        List<GmxGroFileAtomLine> groAtomLines = groFileReader.readGroFileAtomLines(GRO_WATER_IN_ARGON_PATH);

        GmxFrameStructure frameStructure = frameStructureFromGroFileBuilderSupplier.get() //
                .withDescription(groFileReader.readGroFileDescription(GRO_WATER_IN_ARGON_PATH)) //
                .withBox(groFileReader.readGroFileBox(GRO_WATER_IN_ARGON_PATH)) //
                .withGroFileAtomLines(groAtomLines) //
                .build();

        GmxFrameCoordinates frameCoordinates = frameCoordinatesFromGroFileBuilderSupplier.get() //
                .withGroFileAtomLines(groAtomLines) //
                .withFrameNo(1) //
                .build();

        frame1 = frameFactory.get(frameStructure, frameCoordinates);
        frame2 = frameFactory.get(frameStructure, frameCoordinates);
    }

    @Test
    public void testUpdateFrameCoordinates() {
        GmxAtom atom = frame1.getAtoms()[100];
        atom.setCoordinates(new float[] { 1.f, 2.f, 3.f });
        GmxFrameUpdater.updateFrameCoordinates(frame1);
        Assert.assertEquals(frame1.getFrameCoordinates().getCoordinates()[100][0], 1.f, "Wrong X.");
        Assert.assertEquals(frame1.getFrameCoordinates().getCoordinates()[100][1], 2.f, "Wrong Y.");
        Assert.assertEquals(frame1.getFrameCoordinates().getCoordinates()[100][2], 3.f, "Wrong Z.");
    }

    @Test
    public void testUpdateFrameStructure() {
        GmxResidue[] newResidues = new GmxResidue[frame1.getResiduesNum() + 1];
        newResidues[0] = frame1.getResidues()[0];
        newResidues[1] = residueByCoordsFactory.get(GmxResidueH2O.class, 9, new float[] { 1.f, 2.f, 3.f });

        GmxAtom[] newAtoms = new GmxAtom[frame1.getAtomsNum() + 3];
        System.arraycopy(newResidues[1].getAllAtoms(), 0, newAtoms, 0, 3);
        System.arraycopy(frame1.getAtoms(), 0, newAtoms, 3, frame1.getAtomsNum());

        frame1.setAtoms(newAtoms);
        frame1.setResidues(newResidues);
        GmxFrameUpdater.updateFrameStructure(frame1);

        Assert.assertEquals(frame1.getFrameStructure().getResiduesNum(), Integer.valueOf(2));
        Assert.assertEquals(frame1.getFrameStructure().getAtomsNum(), Integer.valueOf(875));
        Assert.assertEquals(frame1.getFrameStructure().getAtomsSequence().length, 875);
        Assert.assertEquals(frame1.getFrameStructure().getAtomsSequence()[1].getSimpleName(), "GmxAtomH");
        Assert.assertEquals(frame1.getFrameStructure().getResidueAtomsMap().keySet().size(), 2);
        Assert.assertEquals(frame1.getFrameStructure().getAtomAbbreviationsSequence()[0], "OW");
    }

    @Test
    public void testReindexAtoms() {
        frame1.getAtoms()[100].setAtomNo(123);
        GmxFrameUpdater.reindexAtoms(frame1);
        Assert.assertEquals(frame1.getAtoms()[100].getAtomNo(), 100, "Atom index not updated.");
    }

    @Test
    public void testReindexResidues() {
        frame1.getResidues()[0].setResidueNo(123);
        GmxFrameUpdater.reindexResidues(frame1);
        Assert.assertEquals(frame1.getResidues()[0].getResidueNo(), 0, "Residue index not updated.");
    }

    // ======== NEGATIVE TESTS ========

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = ATOMS_DUPLICATE)
    public void testReindexDuplicateAtoms() {
        GmxAtom[] newAtoms = frame2.getAtoms();
        newAtoms[100] = newAtoms[99];
        frame2.setAtoms(newAtoms);
        GmxFrameUpdater.reindexAtoms(frame2);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = RESIDUES_DUPLICATE)
    public void testReindexDuplicateResidues() {
        GmxResidue[] newResidues = new GmxResidue[2];
        newResidues[0] = newResidues[1] = frame2.getResidues()[0];
        frame2.setResidues(newResidues);
        GmxFrameUpdater.reindexResidues(frame2);
    }
}
