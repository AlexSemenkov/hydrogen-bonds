package com.asemenkov.utils.config;

import com.asemenkov.utils.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author asemenkov
 * @since Sep 23, 2018
 */
@Configuration
public class PathConfig {

    @Value("${simulation_name:default-simulation}") //
    private String simulationName;

    @Value("${working_directory:${user.dir}}") //
    private String workingDirectory;

    @Value("${gromacs_folder:gromacs}") //
    private String gromacsFolder;

    @Value("${datetime_format:[yyyy-MM-dd][HH_mm_ss]}") //
    private String datetimeFormat;

    @Value("${datetime_zone:Europe/Minsk}") //
    private String datetime_zone;

    @Value("${index_format:[No %d]}") //
    private String indexFormat;

    @Value("${index_regex:^\\[No (\\d+)\\].*}") //
    private String indexRegex;

    @Bean
    public Path simulationPath() {
        Path simulationDir = Paths.get(workingDirectory, gromacsFolder, simulationName);

        int index = FileUtils.getFoldersInDirectory(simulationDir).stream() //
                .map(Path::getFileName).map(Path::toString) //
                .map(Pattern.compile(indexRegex)::matcher) //
                .filter(Matcher::find) //
                .mapToInt(matcher -> Integer.valueOf(matcher.group(1))) //
                .max().orElse(0);

        String pathName = String.format(indexFormat, ++index).concat(DateTime //
                .now(DateTimeZone.forID(datetime_zone)) //
                .toString(datetimeFormat));

        return Paths.get(simulationDir.toString(), pathName);
    }

}
