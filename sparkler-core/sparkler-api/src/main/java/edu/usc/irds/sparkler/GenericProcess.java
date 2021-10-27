package edu.usc.irds.sparkler;


public interface GenericProcess extends ExtensionPoint{

    enum Event {
        SHUTDOWN,
        STARTUP,
        ITERATION_COMPLETE,
    }
    void executeProcess(Event e, Object spark, Object fetchedRdds) throws Exception;
}
