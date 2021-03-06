/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package seneca.structgen.sa.adaptive;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.structgen.RandomGenerator;
import seneca.core.exception.StructureGeneratorException;
import seneca.judges.Judge;
import seneca.judges.ScoreSummary;
import seneca.structgen.StructureGenerator;
import seneca.structgen.StructureGeneratorResult;
import seneca.structgen.StructureGeneratorStatus;
import seneca.structgen.annealinglog.CommonAnnealingLog;
import seneca.structgen.sa.adaptive.MoleculeState.Acceptance;

/**
 * @author kalai
 */
public class ASAStochasticGenerator extends StructureGenerator implements
        StateListener, TemperatureListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * An object describing the score for the best structure so far.
     */
    protected ScoreSummary bestScoreSummary = null;
    /**
     * An object describing the score for the current structure
     */
    protected ScoreSummary recentScore = null;
    /**
     * An object describing the score for the last structure found
     */
    protected ScoreSummary lastScore = null;
    /**
     * Number of iterations done so far.
     */
    protected long iteration = 0;
    RandomGenerator randomGent = null;
    CommonAnnealingLog annealingLog = null;
    int alStepsize = 0;
    IAtomContainer lastStructure = null;
    IAtomContainer bestStructure = null;
    int lastAnnealingCounter = 0;
    private Thread structGenThread = null;
    boolean debug = false;
    private AnnealingEngineI adaptiveAnnealingEngine;
    private MoleculeAnnealerAdapter anealingAdapter;
    private double temperature = 0;
    private boolean hasStarted = false;
    private boolean updatedWithZero = false;

    public ASAStochasticGenerator() {
        super();
        name = "ASAStochasticGenerator";
        annealingLog = new CommonAnnealingLog();
        annealingLog.addEntry(0d, 0d, 0d);
    }

    
    public Object getStatus() throws java.io.IOException {

        StructureGeneratorStatus sgs = new StructureGeneratorStatus();
        Double[] entry;
        sgs.datasetName = this.datasetName;
        sgs.molecularFormula = molecularFormula;
        sgs.annealingLog = new CommonAnnealingLog();
        if (!updatedWithZero) {
            sgs.annealingLog.addEntry(0d, 0d, 0d);
            updatedWithZero = true;
        }
        int currentCount = this.annealingLog.getTotalEntriesCountIn(0);
        for (int f = lastAnnealingCounter; f < currentCount; f++) {
            entry = this.annealingLog.getEntry(0, f);
            sgs.annealingLog.addEntry(entry[0], entry[1], entry[2]);
        }
        if (lastAnnealingCounter != 0 && lastAnnealingCounter == currentCount) {
            entry = this.annealingLog.getEntry(0, lastAnnealingCounter - 1);
            sgs.annealingLog.addEntry(entry[0], entry[1], entry[2]);
        }
        lastAnnealingCounter = currentCount;
        if (adaptiveAnnealingEngine != null) {
            sgs.bestStructure = bestStructure;
            sgs.bestEvaluation = bestScoreSummary;

            sgs.iteration = adaptiveAnnealingEngine.getIterations();

            if (getStructGenThread() == null && !hasStarted) {
                sgs.status = StructureGeneratorStatus.statusStrings[StructureGeneratorStatus.IDLE];
            } else {
                sgs.status = StructureGeneratorStatus.statusStrings[StructureGeneratorStatus.RUNNING];
            }
            if (stopRunning) {
                sgs.status = StructureGeneratorStatus.statusStrings[StructureGeneratorStatus.STOPPED];
            } else if (hasStarted && adaptiveAnnealingEngine.isFinished()) {
                sgs.status = StructureGeneratorStatus.statusStrings[StructureGeneratorStatus.FINISHED];
            }
        }
        sgs.timeTaken = timeTakenSoFar();
        return sgs;
    }


    public Thread getStructGenThread() {
        return structGenThread;
    }

    
    public void start() {

        if (structGenThread == null) {
            structGenThread = new Thread(this, getName());
            hasStarted = true;
            structGenThread.start();
            structGenLogger.info("started structure generation");
        }
    }

    
    public void stop() {
        if (running) {
            this.stopRunning = true;
            adaptiveAnnealingEngine.setShouldStop(true);
            structGenLogger.info("stopped structure generation");
        }
    }

    
    public void run() {


        Thread.currentThread();
        try {
            execute();

        } catch (StructureGeneratorException exc) {
            exc.printStackTrace();
        }
        structGenThread = null;
    }

    
    public void execute() throws StructureGeneratorException {
        long start = System.currentTimeMillis();
        running = true;
        startTime = start;
        IAtomContainer mol = null;
        try {
            mol = generateSingleRandomStructure();
        } catch (Exception ex) {
            structGenLogger.error("Could not generate single random structure");
        }

        anealingAdapter = new MoleculeAnnealerAdapter(mol, chiefJustice);
        anealingAdapter.addStateListener(this);
        System.out.println("Number of steps using : " + numberOfSteps);
        structGenLogger.info("Number of steps using given by user: " + numberOfSteps);
        initiateProgressLogger();
        adaptiveAnnealingEngine = new AdaptiveAnnealingEngine(anealingAdapter, numberOfSteps, annealingLog, annealingEvolutionLogger);
        adaptiveAnnealingEngine.addTemperatureListener(this);
        adaptiveAnnealingEngine.run();
        System.out.println("Finished annealing run.");
        structGenLogger.info("Finished annealing run");

        int totalStructures = structureGeneratorResult.structures.size();
        System.out.println("total structures obtained: " + totalStructures);
        structGenLogger.info("total structures obtained: " + totalStructures);
        long end = System.currentTimeMillis();
        running = false;
        System.out.println("Finished evolving in : " + (end - start) / 1000 + " seconds");
        structGenLogger.info("Finished evolving in : " + (end - start) / 1000 + " seconds");
    }

    private void initiateProgressLogger() {

        String judges = "";
        for (Object obj : chiefJustice.getJudges()) {
            Judge judge = (Judge) obj;
            judges += ";" + judge.getName();
        }
        System.out.println(judges);
        annealingEvolutionLogger.info("Iteration;" + "Temperature;" + "Cost" + judges);
    }

    /**
     * Randomly breaks a bond and forms another to mutate the structure The rules for this method
     * are described in "Faulon, JCICS 1996, 36, 731"
     */
    protected void mutate() {
    }

    void log(String message) {
    }

    
    public void temperatureChange(double temp) {
        this.temperature = temp;
    }

    
    public void stateChanged(State state) {
        MoleculeState moleculeState = (MoleculeState) state;

        if (moleculeState.acceptance == Acceptance.ACCEPT) {

            IAtomContainer best = anealingAdapter.getBest();
            bestScoreSummary = anealingAdapter.getBestScoreSummary();

            best.setProperty("Score", decimalFormat.format(bestScoreSummary.costValue));
            best.setProperty("Steps so far", anealingAdapter.getBestStepIndex());
            best.setProperty("Temperature", this.temperature);
            bestStructure = best;
            structureGeneratorResult.structures.push(best);
            annealingLog = adaptiveAnnealingEngine.getUpdatedAnnealingLog();
        } else if (moleculeState.acceptance == Acceptance.UNKNOWN) {
//            System.out.println("Best score = " + df.format(1 - anealingAdapter.getBestCost()) + "; Step = "
//                    + state.getStep() + "; Temperature = " + this.temperature);
        }

    }

    
    public StructureGeneratorResult call() throws Exception {
        long start = System.currentTimeMillis();
        running = true;
        startTime = start;
        IAtomContainer mol = null;
        try {
            mol = generateSingleRandomStructure();
        } catch (Exception ex) {
            structGenLogger.error("Could not generate single random structure");
        }

        anealingAdapter = new MoleculeAnnealerAdapter(mol, chiefJustice);
        anealingAdapter.addStateListener(this);
        System.out.println("Number of steps using : " + numberOfSteps);
        structGenLogger.info("Number of steps using given by user: " + numberOfSteps);
        initiateProgressLogger();
        adaptiveAnnealingEngine = new AdaptiveAnnealingEngine(anealingAdapter, numberOfSteps, annealingLog, annealingEvolutionLogger);
        adaptiveAnnealingEngine.addTemperatureListener(this);
        adaptiveAnnealingEngine.run();
        System.out.println("Finished annealing run.");
        structGenLogger.info("Finished annealing run");

        int totalStructures = structureGeneratorResult.structures.size();
        System.out.println("total structures obtained: " + totalStructures);
        structGenLogger.info("total structures obtained: " + totalStructures);
        long end = System.currentTimeMillis();
        running = false;
        System.out.println("Finished evolving in : " + (end - start) / 1000 + " seconds");
        structGenLogger.info("Finished evolving in : " + (end - start) / 1000 + " seconds");
        return structureGeneratorResult;
    }
}
