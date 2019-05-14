package com.asemenkov.utils.config;

import com.asemenkov.utils.io.FileUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Value("${simulation_name:default-simulation}") //
    private String simulationName;

    @Value("${working_directory:${user.home}}") //
    private String workingDirectory;

    @Value("${gromacs_folder:gromacs}") //
    private String gromacsFolder;

    @Value("${datetime_format:yyyy-MM-hh_HH:mm:ss}") //
    private String datetimeFormat;

    @Value("${input_folder:input}") //
    private String inputFolder;

    @Value("${output_folder:output}") //
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
