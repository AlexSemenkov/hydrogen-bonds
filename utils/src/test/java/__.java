import com.asemenkov.utils.config.PathConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.nio.file.Path;

/**
 * @author asemenkov
 * @since Oct 07, 2018
 */
@ContextConfiguration(classes = PathConfig.class)
public class __ extends AbstractTestNGSpringContextTests {

    private @Autowired Environment environment;
    private @Autowired Path inputPath;


    @Test
    public void _() {
        System.out.println(environment.getProperty("default.gromacs_folder"));
        System.out.println(inputPath);
    }
}
