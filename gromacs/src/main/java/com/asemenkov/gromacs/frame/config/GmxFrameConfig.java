package com.asemenkov.gromacs.frame.config;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.frame.utils.GmxFrameInitializer;
import com.asemenkov.gromacs.particles.config.GmxParticlesConfig;
import com.asemenkov.utils.config.Factories.DuoFactory;
import com.asemenkov.utils.io.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author asemenkov
 * @since Apr 22, 2018
 */
@Configuration
@Import(GmxParticlesConfig.class)
@ComponentScan("com.asemenkov.gromacs.frame")
public class GmxFrameConfig {

    private @Autowired ApplicationContext applicationContext;

    // ======== INTERFACE ========

    @Bean
    public DuoFactory<GmxFrame, GmxFrameStructure, GmxFrameCoordinates> frameFactory() {
        return this::getFrame;
    }

    // ======== REALIZATION ========

    private GmxFrame getFrame(GmxFrameStructure structure, GmxFrameCoordinates coordinates) {
        GmxFrame frame = applicationContext.getBean(GmxFrame.class);
        frame.setFrameStructure(structure);
        frame.setFrameCoordinates(coordinates);
        GmxFrameInitializer.initAtoms(frame);
        GmxFrameInitializer.initResidues(frame);
        Logger.log("Frame No " + frame.getFrameNo() + " successfully created");
        return frame;
    }

}
