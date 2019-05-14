package com.asemenkov.command.config;

import com.asemenkov.command.exceptions.CmdArgumentException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author asemenkov
 * @since May 02, 2019
 */
public class CmdGmxArgumentsComparator implements Comparator<Entry<String, String>> {

    private List<String> gmxCmdArgPriorities = Arrays.asList("", "-f", "-r", "-rb", "-s", "-cpi", "-table",
            "-tablep", "-tableb", "-rerun", "-ei", "-multidir", "-awh", "-membed", "-mp", "-mn", "-o", "-x",
            "-cpo", "-c", "-n", "-p", "-e", "-g", "-t", "-dhdl", "-ref", "-po", "-imd", "-pp", "-field",
            "-tpi", "-tpid", "-eo", "-devout", "-runav", "-px", "-pf", "-ro", "-ra", "-rs", "-rt", "-mtx",
            "-if", "-swap", "-deffnm", "-xvg", "-dd", "-ddorder", "-npme", "-nt", "-ntmpi", "-ntomp",
            "-ntomp_pme", "-pin", "-pinoffset", "-pinstride", "-gpu_id", "-gputasks", "-ddcheck ",
            "-noddcheck", "-rdd", "-rcon", "-dlb", "-dds", "-gcom", "-nb", "-nstlist", "-tunepme",
            "-notunepme", "-pme", "-pmefft", "-v", "-nov", "-pforce", "-reprod", "-noreprod", "-cpt",
            "-cpnum", "-nocpnum", "-append", "-noappend", "-nsteps", "-maxh", "-multi", "-replex", "-nex",
            "-reseed", "-time", "-rmvsbds", "-normvsbds", "-zero", "-nozero", "-renum", "-norenum");

    @Override
    public int compare(Entry<String, String> e1, Entry<String, String> e2) {
        String key1 = e1.getKey();
        String key2 = e2.getKey();

        String firstKey = gmxCmdArgPriorities.stream() //
                .filter(arg -> arg.equals(key1) || arg.equals(key2)) //
                .findFirst().orElse(null);

        if (firstKey == null) return 0;
        else if (key1.equals(key2)) return 0;
        else if (firstKey.equals(key1)) return -1;
        else if (firstKey.equals(key2)) return 1;
        else throw new CmdArgumentException("Unable to compare arguments");
    }
}
