package com.asemenkov.gromacs.frame;

import java.util.function.Supplier;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.GmxFrameStructureBuilder;
import com.asemenkov.gromacs.particles.GmxParticlesConfig;
import com.asemenkov.utils.Logger;
import com.asemenkov.utils.Factories.DuoFactory;

/**
 * @author asemenkov
 * @since Apr 22, 2018
 */
@Configuration
@Import(GmxParticlesConfig.class)
public class GmxFrameConfig {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    @SuppressWarnings("WeakerAccess")
    protected GmxFrameStructureBuilder frameStructureBuilder() {
        return new GmxFrameStructureBuilder();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    @SuppressWarnings("WeakerAccess")
    protected GmxFrameCoordinatesBuilder frameCoordinatesBuilder() {
        return new GmxFrameCoordinatesBuilder();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    @SuppressWarnings("WeakerAccess")
    protected GmxFrame frame() {
        return new GmxFrame();
    }

    @Bean
    public Supplier<GmxFrameStructureBuilder> frameStructureBuilderSupplier() {
        return this::frameStructureBuilder;
    }

    @Bean
    public Supplier<GmxFrameCoordinatesBuilder> frameCoordinatesBuilderSupplier() {
        return this::frameCoordinatesBuilder;
    }

    @Bean
    public DuoFactory<GmxFrame, GmxFrameStructure, GmxFrameCoordinates> frameFactory() {
        return this::getFrame;
    }

    // ======== REALIZATION ========

    private GmxFrame getFrame(GmxFrameStructure structure, GmxFrameCoordinates coordinates) {
        GmxFrame frame = frame();
        frame.setFrameStructure(structure);
        frame.setFrameCoordinates(coordinates);

        frame.validateFrameStructureAtoms();
        if (structure.getResiduesNum() > 0) frame.validateFrameStructureResidues();

        frame.initAtoms();
        frame.initResidues();
        Logger.log("Frame No " + frame.getFrameNo() + " successfully created");
        return frame;
    }

}
