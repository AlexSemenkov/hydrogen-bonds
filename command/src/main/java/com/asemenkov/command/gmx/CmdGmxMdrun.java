package com.asemenkov.command.gmx;

import com.asemenkov.command.enums.*;
import com.asemenkov.utils.io.FileUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * The mdrun program reads the run input file (-s) and distributes the topology over ranks if needed.
 * mdrun produces at least four output files. A single log file (-g) is written.
 * The trajectory file (-o), contains coordinates, velocities and optionally forces.
 * The structure file (-c) contains the coordinates and velocities of the last step.
 * The energy file (-e) contains energies, the temperature, pressure, etc,
 * a lot of these things are also printed in the log file.
 * Optionally coordinates can be written to a compressed trajectory file (-x).
 * <p>
 *
 * @author asemenkov
 * @see <a href="http://manual.gromacs.org/documentation/2018/onlinehelp/gmx-mdrun.html">GROMACS manual</a>
 * @since May 02, 2019
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class CmdGmxMdrun extends CmdGmxAbstractCommand {

    public CmdGmxMdrun() {
        super("mdrun");
    }

    // ======== Options to specify input files ========

    /**
     * -s [<.tpr>] (topol.tpr)
     * Portable xdr run input file
     */
    public CmdGmxMdrun withXdrInputFile(Path s) {
        FileUtils.verifyFileExists(s);
        FileUtils.verifyExtension(s, ".tpr");
        commandsMap.put("-s", s.toString());
        return this;
    }

    /**
     * -cpi [<.cpt>] (state.cpt) (Optional)
     * Checkpoint file
     */
    public CmdGmxMdrun withCheckpointInputFile(Path cpi) {
        FileUtils.verifyFileExists(cpi);
        FileUtils.verifyExtension(cpi, ".cpt");
        commandsMap.put("-cpi", cpi.toString());
        return this;
    }

    /**
     * -table [<.xvg>] (table.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withTableInputFile(Path table) {
        FileUtils.verifyFileExists(table);
        FileUtils.verifyExtension(table, ".xvg");
        commandsMap.put("-table", table.toString());
        return this;
    }

    /**
     * -tablep [<.xvg>] (tablep.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withTablepInputFile(Path tablep) {
        FileUtils.verifyFileExists(tablep);
        FileUtils.verifyExtension(tablep, ".xvg");
        commandsMap.put("-tablep", tablep.toString());
        return this;
    }

    /**
     * -tableb [<.xvg> […]] (table.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withTablebInputFile(Path tableb) {
        FileUtils.verifyFileExists(tableb);
        FileUtils.verifyExtension(tableb, ".xvg");
        commandsMap.put("-tableb", tableb.toString());
        return this;
    }

    /**
     * -rerun [<.xtc/.trr/…>] (rerun.xtc) (Optional)
     * Trajectory: xtc trr cpt gro g96 pdb tng
     */
    public CmdGmxMdrun withRerunTrajectoryInputFile(Path rerun) {
        FileUtils.verifyFileExists(rerun);
        FileUtils.verifyExtension(rerun, ".xtc", ".trr", ".cpt", ".gro", ".pdb", ".tng");
        commandsMap.put("-rerun", rerun.toString());
        return this;
    }

    /**
     * -ei [<.edi>] (sam.edi) (Optional)
     * ED sampling input
     */
    public CmdGmxMdrun withEdSamplingInputFile(Path ei) {
        FileUtils.verifyFileExists(ei);
        FileUtils.verifyExtension(ei, ".edi");
        commandsMap.put("-ei", ei.toString());
        return this;
    }

    /**
     * -multidir [<dir> […]] (rundir) (Optional)
     * Run directory
     */
    public CmdGmxMdrun withRunDirectory(Path multidir) {
        commandsMap.put("-multidir", multidir.toString());
        return this;
    }

    /**
     * -awh [<.xvg>] (awhinit.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withAwhInitInputFile(Path awh) {
        FileUtils.verifyFileExists(awh);
        FileUtils.verifyExtension(awh, ".xvg");
        commandsMap.put("-awh", awh.toString());
        return this;
    }

    /**
     * -membed [<.dat>] (membed.dat) (Optional)
     * Generic data file
     */
    public CmdGmxMdrun withMembedDataInputFile(Path membed) {
        FileUtils.verifyFileExists(membed);
        FileUtils.verifyExtension(membed, ".dat");
        commandsMap.put("-membed", membed.toString());
        return this;
    }

    /**
     * -mp [<.top>] (membed.top) (Optional)
     * Topology file
     */
    public CmdGmxMdrun withMembedTopologyInputFile(Path mp) {
        FileUtils.verifyFileExists(mp);
        FileUtils.verifyExtension(mp, ".top");
        commandsMap.put("-mp", mp.toString());
        return this;
    }

    /**
     * -mn [<.ndx>] (membed.ndx) (Optional)
     * Index file
     */
    public CmdGmxMdrun withMembedIndexInputFile(Path mn) {
        FileUtils.verifyFileExists(mn);
        FileUtils.verifyExtension(mn, ".ndx");
        commandsMap.put("-mn", mn.toString());
        return this;
    }

    // ======== Options to specify output files ========

    /**
     * -o [<.trr/.cpt/…>] (traj.trr)
     * Full precision trajectory: trr cpt tng
     */
    public CmdGmxMdrun withFullPrecisionTrajectoryOutputFile(Path o) {
        FileUtils.verifyExtension(o, ".trr", ".cpt", ".tng");
        commandsMap.put("-o", o.toString());
        return this;
    }

    /**
     * -x [<.xtc/.tng>] (traj_comp.xtc) (Optional)
     * Compressed trajectory (tng format or portable xdr format)
     */
    public CmdGmxMdrun withCompressedTrajectoryOutputFile(Path x) {
        FileUtils.verifyExtension(x, ".xtc", ".tng");
        commandsMap.put("-x", x.toString());
        return this;
    }

    /**
     * -cpo [<.cpt>] (state.cpt) (Optional)
     * Checkpoint file
     */
    public CmdGmxMdrun withCheckpointOutputFile(Path cpo) {
        FileUtils.verifyExtension(cpo, ".cpt");
        commandsMap.put("-cpo", cpo.toString());
        return this;
    }

    /**
     * -c [<.gro/.g96/…>] (confout.gro)
     * Structure file: gro g96 pdb brk ent esp
     */
    public CmdGmxMdrun withStructureOutputFile(Path c) {
        FileUtils.verifyExtension(c, ".gro", ".g96", ".pdb", ".brk", ".ent", ".esp");
        commandsMap.put("-c", c.toString());
        return this;
    }

    /**
     * -e [<.edr>] (ener.edr)
     * Energy file
     */
    public CmdGmxMdrun withEnergyOutputFile(Path e) {
        FileUtils.verifyExtension(e, ".edr");
        commandsMap.put("-e", e.toString());
        return this;
    }

    /**
     * -g [<.log>] (md.log)
     * Log file
     */
    public CmdGmxMdrun withMdLogOutputFile(Path g) {
        FileUtils.verifyExtension(g, ".log");
        commandsMap.put("-g", g.toString());
        return this;
    }

    /**
     * -dhdl [<.xvg>] (dhdl.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withDhdlOutputFile(Path dhdl) {
        FileUtils.verifyExtension(dhdl, ".xvg");
        commandsMap.put("-dhdl", dhdl.toString());
        return this;
    }

    /**
     * -field [<.xvg>] (field.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withFieldOutputFile(Path field) {
        FileUtils.verifyExtension(field, ".xvg");
        commandsMap.put("-field", field.toString());
        return this;
    }

    /**
     * -tpi [<.xvg>] (tpi.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withTpiOutputFile(Path tpi) {
        FileUtils.verifyExtension(tpi, ".xvg");
        commandsMap.put("-tpi", tpi.toString());
        return this;
    }

    /**
     * -tpid [<.xvg>] (tpidist.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withTpiDistOutputFile(Path tpid) {
        FileUtils.verifyExtension(tpid, ".xvg");
        commandsMap.put("-tpid", tpid.toString());
        return this;
    }

    /**
     * -eo [<.xvg>] (edsam.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withEdsamOutputFile(Path eo) {
        FileUtils.verifyExtension(eo, ".xvg");
        commandsMap.put("-eo", eo.toString());
        return this;
    }

    /**
     * -devout [<.xvg>] (deviatie.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withDeviatieOutputFile(Path devout) {
        FileUtils.verifyExtension(devout, ".xvg");
        commandsMap.put("-devout", devout.toString());
        return this;
    }

    /**
     * -runav [<.xvg>] (runaver.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withRunAverOutputFile(Path runav) {
        FileUtils.verifyExtension(runav, ".xvg");
        commandsMap.put("-runav", runav.toString());
        return this;
    }

    /**
     * -px [<.xvg>] (pullx.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withPullxOutputFile(Path px) {
        FileUtils.verifyExtension(px, ".xvg");
        commandsMap.put("-px", px.toString());
        return this;
    }

    /**
     * -pf [<.xvg>] (pullf.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withPullfOutputFile(Path pf) {
        FileUtils.verifyExtension(pf, ".xvg");
        commandsMap.put("-pf", pf.toString());
        return this;
    }

    /**
     * -ro [<.xvg>] (rotation.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withRotationOutputFile(Path ro) {
        FileUtils.verifyExtension(ro, ".xvg");
        commandsMap.put("-ro", ro.toString());
        return this;
    }

    /**
     * -ra [<.log>] (rotangles.log) (Optional)
     * Log file
     */
    public CmdGmxMdrun withRotAnglesOutputFile(Path ra) {
        FileUtils.verifyExtension(ra, ".log");
        commandsMap.put("-ra", ra.toString());
        return this;
    }

    /**
     * -rs [<.log>] (rotslabs.log) (Optional)
     * Log file
     */
    public CmdGmxMdrun withRotSlabsLogOutputFile(Path rs) {
        FileUtils.verifyExtension(rs, ".log");
        commandsMap.put("-rs", rs.toString());
        return this;
    }

    /**
     * -rt [<.log>] (rottorque.log) (Optional)
     * Log file
     */
    public CmdGmxMdrun withRotTorqueLogOutputFile(Path rt) {
        FileUtils.verifyExtension(rt, ".log");
        commandsMap.put("-rt", rt.toString());
        return this;
    }

    /**
     * -mtx [<.mtx>] (nm.mtx) (Optional)
     * Hessian matrix
     */
    public CmdGmxMdrun withHessianMatrixOutputFile(Path mtx) {
        FileUtils.verifyExtension(mtx, ".mtx");
        commandsMap.put("-mtx", mtx.toString());
        return this;
    }

    /**
     * -if [<.xvg>] (imdforces.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withImdForcesOutputFile(Path imdforces) {
        FileUtils.verifyExtension(imdforces, ".xvg");
        commandsMap.put("-if", imdforces.toString());
        return this;
    }

    /**
     * -swap [<.xvg>] (swapions.xvg) (Optional)
     * xvgr/xmgr file
     */
    public CmdGmxMdrun withSwapionsOutputFile(Path swap) {
        FileUtils.verifyExtension(swap, ".xvg");
        commandsMap.put("-swap", swap.toString());
        return this;
    }

    // ======== Other options ========

    /**
     * -deffnm <string>
     * Set the default filename for all file options
     */
    public CmdGmxMdrun withDefaultOptionsFilename(String deffnm) {
        commandsMap.put("-deffnm", deffnm);
        return this;
    }

    /**
     * -xvg <enum> (xmgrace)
     * xvg plot formatting: xmgrace, xmgr, none
     */
    public CmdGmxMdrun withPlotFormatting(CmdGmxPlotFormating xvg) {
        commandsMap.put("-xvg", xvg.name().toLowerCase());
        return this;
    }

    /**
     * -dd <vector> (0 0 0)
     * Domain decomposition grid, 0 is optimize
     */
    public CmdGmxMdrun withDomainDecompositionGrid(String dd) {
        commandsMap.put("-dd", dd);
        return this;
    }

    /**
     * -ddorder <enum> (interleave)
     * DD rank order: interleave, pp_pme, cartesian
     */
    public CmdGmxMdrun withDomainDecompositionRankOrder(CmdGmxDomainDecompositionRankOrder ddorder) {
        commandsMap.put("-ddorder", ddorder.name().toLowerCase());
        return this;
    }

    /**
     * -npme <int> (-1)
     * Number of separate ranks to be used for PME, -1 is guess
     */
    public CmdGmxMdrun withSeparateRanksNumberForPme(Integer npme) {
        commandsMap.put("-npme", npme.toString());
        return this;
    }

    /**
     * -nt <int> (0)
     * Total number of threads to start (0 is guess)
     */
    public CmdGmxMdrun withTotalThreadsNumber(Integer nt) {
        commandsMap.put("-nt", nt.toString());
        return this;
    }

    /**
     * -ntmpi <int> (0)
     * Number of thread-MPI ranks to start (0 is guess)
     */
    public CmdGmxMdrun withThreadMpiRanksNumber(Integer ntmpi) {
        commandsMap.put("-ntmpi", ntmpi.toString());
        return this;
    }

    /**
     * -ntomp <int> (0)
     * Number of OpenMP threads per MPI rank to start (0 is guess)
     */
    public CmdGmxMdrun withOpenMpThreadsPerMpiRankNumber(Integer ntomp) {
        commandsMap.put("-ntomp", ntomp.toString());
        return this;
    }

    /**
     * -pin <enum> (auto)
     * Whether mdrun should try to set thread affinities: auto, on, off
     */
    public CmdGmxMdrun withTryToSetThreadAffinities(CmdGmxToggle pin) {
        commandsMap.put("-pin", pin.name().toLowerCase());
        return this;
    }

    /**
     * -pinoffset <int> (0)
     * The lowest logical core number to which mdrun should pin the first thread
     */
    public CmdGmxMdrun withPinOffset(Integer pinoffset) {
        commandsMap.put("-pinoffset", pinoffset.toString());
        return this;
    }

    /**
     * -pinstride <int> (0)
     * Pinning distance in logical cores for threads,
     * use 0 to minimize the number of threads per physical core
     */
    public CmdGmxMdrun withPinStride(Integer pinstride) {
        commandsMap.put("-pinstride", pinstride.toString());
        return this;
    }

    /**
     * -gpu_id <string>
     * List of unique GPU device IDs available to use
     */
    public CmdGmxMdrun withGpuIds(String gpuid) {
        commandsMap.put("-gpu_id", gpuid);
        return this;
    }

    /**
     * -gputasks <string>
     * List of GPU device IDs, mapping each PP task on each node to a device
     */
    public CmdGmxMdrun withGpuTasks(String gputasks) {
        commandsMap.put("-gputasks", gputasks);
        return this;
    }

    /**
     * -[no]ddcheck (yes)
     * Check for all bonded interactions with DD
     */
    public CmdGmxMdrun withDomainDecompositionCheck(Boolean ddcheck) {
        commandsMap.put(ddcheck ? "-ddcheck" : "-noddcheck", "\\b");
        return this;
    }

    /**
     * -rdd <real> (0)
     * The maximum distance for bonded interactions with DD (nm),
     * 0 is determine from initial coordinates
     */
    public CmdGmxMdrun withDomainDecompositionDistance(Double rdd) {
        commandsMap.put("-rdd", rdd.toString());
        return this;
    }

    /**
     * -rcon <real> (0)
     * Maximum distance for P-LINCS (nm), 0 is estimate
     */
    public CmdGmxMdrun withPlincsDistance(Double rcon) {
        commandsMap.put("-rcon", rcon.toString());
        return this;
    }

    /**
     * -dlb <enum> (auto)
     * Dynamic load balancing (with DD): auto, no, yes
     */
    public CmdGmxMdrun withDynamicLoadBalancing(CmdGmxBoolean dlb) {
        commandsMap.put("-dlb", dlb.name().toLowerCase());
        return this;
    }

    /**
     * -dds <real> (0.8)
     * Fraction in (0,1) by whose reciprocal the initial DD cell size will be increased
     * in order to provide a margin in which dynamic load balancing can act
     * while preserving the minimum cell size.
     */
    public CmdGmxMdrun withDomainDecompositionCellSize(Double dds) {
        commandsMap.put("-dds", dds.toString());
        return this;
    }

    /**
     * -gcom <int> (-1)
     * Global communication frequency
     */
    public CmdGmxMdrun withGlobalCommunicationFrequency(Integer gcom) {
        commandsMap.put("-gcom", gcom.toString());
        return this;
    }

    /**
     * -nb <enum> (auto)
     * Calculate non-bonded interactions on: auto, cpu, gpu
     */
    public CmdGmxMdrun withNonBondedInteractions(CmdGmxProcessorUnit nb) {
        commandsMap.put("-nb", nb.name().toLowerCase());
        return this;
    }

    /**
     * -nstlist <int> (0)
     * Set nstlist when using a Verlet buffer tolerance (0 is guess)
     */
    public CmdGmxMdrun withNstList(Integer nstlist) {
        commandsMap.put("-nstlist", nstlist.toString());
        return this;
    }

    /**
     * -[no]tunepme (yes)
     * Optimize PME load between PP/PME ranks or GPU/CPU (only with the Verlet cut-off scheme)
     */
    public CmdGmxMdrun withPmeTuning(Boolean tunepme) {
        commandsMap.put(tunepme ? "-tunepme" : "-notunepme", "\\b");
        return this;
    }

    /**
     * -pme <enum> (auto)
     * Perform PME calculations on: auto, cpu, gpu
     */
    public CmdGmxMdrun withPmeCalculationsUnit(CmdGmxProcessorUnit pme) {
        commandsMap.put("-pme", pme.name().toLowerCase());
        return this;
    }

    /**
     * -pmefft <enum> (auto)
     * Perform PME FFT calculations on: auto, cpu, gpu
     */
    public CmdGmxMdrun withPmeFftCalculationsUnit(CmdGmxProcessorUnit pmefft) {
        commandsMap.put("-pmefft", pmefft.name().toLowerCase());
        return this;
    }

    /**
     * -[no]v (no)
     * Be loud and noisy
     */
    public CmdGmxMdrun withLoudAndNoisy(Boolean v) {
        commandsMap.put(v ? "-v" : "-nov", "\\b");
        return this;
    }

    /**
     * -pforce <real> (-1)
     * Print all forces larger than this (kJ/mol nm)
     */
    public CmdGmxMdrun withForcesPrintValue(Double pforce) {
        commandsMap.put("-pforce", pforce.toString());
        return this;
    }

    /**
     * -[no]reprod (no)
     * Try to avoid optimizations that affect binary reproducibility
     */
    public CmdGmxMdrun withReproducibility(Boolean reprod) {
        commandsMap.put(reprod ? "-reprod" : "-noreprod", "\\b");
        return this;
    }

    /**
     * -cpt <real> (15)
     * Checkpoint interval (minutes)
     */
    public CmdGmxMdrun withCheckpointIntervalInMinutes(Double cpt) {
        commandsMap.put("-cpt", cpt.toString());
        return this;
    }

    /**
     * -[no]cpnum (no)
     * Keep and number checkpoint files
     */
    public CmdGmxMdrun withKeepAndNumberCheckpointFiles(Boolean cpnum) {
        commandsMap.put(cpnum ? "-cpnum" : "-nocpnum", "\\b");
        return this;
    }

    /**
     * -[no]append (yes)
     * Append to previous output files when continuing from checkpoint
     * instead of adding the simulation part number to all file names
     */
    public CmdGmxMdrun withAppendToOutputFilesWhenCpt(Boolean append) {
        commandsMap.put(append ? "-append" : "-noappend", "\\b");
        return this;
    }

    /**
     * -nsteps <int> (-2)
     * Run this number of steps, overrides .mdp file option
     * -1 means infinite, -2 means use mdp option, smaller is invalid
     */
    public CmdGmxMdrun withNumberOfStepsToRun(Integer nsteps) {
        commandsMap.put("-nsteps", nsteps.toString());
        return this;
    }

    /**
     * -maxh <real> (-1)
     * Terminate after 0.99 times this time (hours)
     */
    public CmdGmxMdrun withTerminationTimeInHours(Double maxh) {
        commandsMap.put("-maxh", maxh.toString());
        return this;
    }

    /**
     * -multi <int> (0)
     * Do multiple simulations in parallel
     */
    public CmdGmxMdrun withNumberOfParallelSimulations(Integer multi) {
        commandsMap.put("-multi", multi.toString());
        return this;
    }

    /**
     * -replex <int> (0)
     * Attempt replica exchange periodically with this period (steps)
     */
    public CmdGmxMdrun withReplicaExchangePeriodInSteps(Integer replex) {
        commandsMap.put("-replex", replex.toString());
        return this;
    }

    /**
     * -nex <int> (0)
     * Number of random exchanges to carry out each exchange interval (N^3 is one suggestion)
     * -nex zero or not specified gives neighbor replica exchange
     */
    public CmdGmxMdrun withRandomExchangesNumber(Integer nex) {
        commandsMap.put("-nex", nex.toString());
        return this;
    }

    /**
     * -reseed <int> (-1)
     * Seed for replica exchange, -1 is generate a seed
     */
    public CmdGmxMdrun withReplicaExchangeSeed(Integer reseed) {
        commandsMap.put("-reseed", reseed.toString());
        return this;
    }

}
