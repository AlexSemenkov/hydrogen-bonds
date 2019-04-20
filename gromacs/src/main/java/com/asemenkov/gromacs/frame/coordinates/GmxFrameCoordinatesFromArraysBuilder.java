package com.asemenkov.gromacs.frame.coordinates;

import com.asemenkov.gromacs.frame.exceptions.GmxFrameException;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.utils.io.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author asemenkov
 * @since Aug 12, 2018
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GmxFrameCoordinatesFromArraysBuilder extends GmxAbstractFrameCoordinatesBuilder<GmxFrameCoordinatesFromArraysBuilder> {

    private GmxAtom[] atoms;

    // ======== INTERFACE ========

    public GmxFrameCoordinatesFromArraysBuilder withAtomsArray(GmxAtom[] atoms) {
        this.atoms = new GmxAtom[atoms.length];
        System.arraycopy(atoms, 0, this.atoms, 0, atoms.length);
        return this;
    }

    @Override
    public GmxFrameCoordinates build() {
        validateAtomsArray();
        float[][] coordinates = Arrays.stream(atoms) //
                .map(GmxAtom::getCoordinates) //
                .toArray(float[][]::new);

        GmxFrameCoordinates toReturn = new GmxFrameCoordinates(coordinates, frameNo);
        Logger.log("Frame coordinates successfully created from array of atoms");
        return toReturn;
    }

    // ======== VALIDATION METHODS ========

    private void validateAtomsArray() {
        if (atoms == null || atoms.length == 0) //
            throw new GmxFrameException("Atoms array is missing.");
    }
}
