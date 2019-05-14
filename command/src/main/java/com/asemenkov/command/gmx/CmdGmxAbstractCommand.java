package com.asemenkov.command.gmx;

import com.asemenkov.command.config.CmdGmxArgumentsComparator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author asemenkov
 * @since May 02, 2019
 */
@SuppressWarnings("WeakerAccess")
public abstract class CmdGmxAbstractCommand {

    protected final Map<String, String> commandsMap;
    private @Autowired CmdGmxArgumentsComparator argsComparator;

    public CmdGmxAbstractCommand(String gmxCommand) {
        commandsMap = new HashMap<>();
        commandsMap.put("", gmxCommand);
    }

    @Override
    public String toString() {
        return commandsMap.entrySet().stream() //
                .sorted((e1, e2) -> argsComparator.compare(e1, e2)) //
                .map(e -> e.getKey() + " " + e.getValue()) //
                .collect(Collectors.joining(" ", "gmx", ""));
    }

}
