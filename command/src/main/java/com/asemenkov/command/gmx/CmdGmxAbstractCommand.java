package com.asemenkov.command.gmx;

import com.asemenkov.command.exceptions.CmdInheritanceException;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author asemenkov
 * @since May 02, 2019
 */
@SuppressWarnings({ "WeakerAccess", "unchecked" })
public abstract class CmdGmxAbstractCommand<T extends CmdGmxAbstractCommand> {

    protected final Map<String, String> commandsMap;
    protected @Autowired Path simulationPath;

    public CmdGmxAbstractCommand(String gmxCommand) {
        commandsMap = new LinkedHashMap<>();
        commandsMap.put("", gmxCommand);

        try {
            withoutStartupInfo(true);
            withCopyright(false);
        } catch (ClassCastException exception) {
            throw new CmdInheritanceException("Wrong generic type found.", exception);
        }
    }

    /**
     * -[no]h (no)
     * Print help and quit
     */
    public T withHelp(Boolean h) {
        commandsMap.put(h ? "-h" : "-noh", "");
        return getThis();
    }

    /**
     * -[no]quiet (no)
     * Do not print common startup info or quotes
     */
    public T withoutStartupInfo(Boolean quiet) {
        commandsMap.put(quiet ? "-quiet" : "-noquiet", "");
        return getThis();
    }

    /**
     * -[no]version (no)
     * Print extended version information and quit
     */
    public T withVersion(Boolean version) {
        commandsMap.put(version ? "-version" : "-noversion", "");
        return getThis();
    }

    /**
     * -[no]copyright (yes)
     * Print copyright information on startup
     */
    public T withCopyright(Boolean copyright) {
        commandsMap.put(copyright ? "-copyright" : "-nocopyright", "");
        return getThis();
    }

    /**
     * -nice <int> (19)
     * Set the nice level (default depends on command)
     */
    public T withNiceLevel(Integer nice) {
        commandsMap.put("-nice", nice.toString());
        return getThis();
    }

    /**
     * -[no]backup (yes)
     * Write backups if output files exist
     */
    public T withBackup(Boolean backup) {
        commandsMap.put(backup ? "-backup" : "-nobackup", "");
        return getThis();
    }

    @Override
    public String toString() {
        return commandsMap.entrySet().stream() //
                .map(e -> e.getKey() + (e.getValue().isEmpty() ? "" : " ") + e.getValue()) //
                .collect(Collectors.joining(" ", "gmx", ""));
    }

    private T getThis() {
        return (T) this;
    }
}
