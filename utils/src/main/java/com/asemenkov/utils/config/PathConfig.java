package com.asemenkov.utils.config;

import com.asemenkov.utils.io.FileUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author asemenkov
 * @since Sep 23, 2018
 */
@Configuration
@PropertySource("classpath:path.properties")
public class PathConfig {

//    @Autowired @Qualifier("simulation-name") //
    @Value("${simulation_name:${default.simulation_name}}") //
    private String simulationName;

    @Value("${working_directory:${default.working_directory}}") //
    private String workingDirectory;

    @Value("${gromacs_folder:${default.gromacs_folder}}") //
    private String gromacsFolder;

    @Value("${datetime_format:${default.datetime_format}}") //
    private String datetimeFormat;

    @Value("${input_folder:${default.input_folder}}") //
    private String inputFolder;

    @Value("${output_folder:${default.output_folder}}") //
    private String outputFolder;

    private @Autowired Path simulationPath;

    @Bean
    public Path simulationPath() {
        String datetime = DateTime.now().toString(datetimeFormat);
        Path path = Paths.get(workingDirectory, gromacsFolder, simulationName, datetime);
        FileUtils.createDirectoryIfNotExist(path);
        return path;
    }

    @Bean
    public Path inputPath() {
        Path path = Paths.get(simulationPath.toString(), inputFolder);
        FileUtils.createDirectoryIfNotExist(path);
        return path;
    }

    @Bean
    public Path outputPath() {
        Path path = Paths.get(simulationPath.toString(), outputFolder);
        FileUtils.createDirectoryIfNotExist(path);
        return path;
    }

}
