package com.asemenkov.command.execution;

import com.asemenkov.command.exceptions.CmdIoException;
import com.asemenkov.command.gmx.CmdGmxAbstractCommand;
import com.asemenkov.utils.io.Logger;

import java.io.IOException;

/**
 * @author asemenkov
 * @since May 03, 2019
 */
public class CmdExecutor {

    public void executeGromacsCommand(CmdGmxAbstractCommand command) {
        Logger.log("Executing GROMACS command:\n>>> " + command.toString());
        Process process = executeCommand(command.toString());
        Logger.logGmxOutput(process);
    }

    private Process executeCommand(String command) {
        try {
            return Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new CmdIoException("IO exception during command execution");
        }
    }

}
