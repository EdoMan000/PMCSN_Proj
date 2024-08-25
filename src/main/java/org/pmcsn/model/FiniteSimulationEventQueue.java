package org.pmcsn.model;

import java.util.NoSuchElementException;

public class FiniteSimulationEventQueue extends EventQueue {
    private final double intervalLength;
    private double lastSaveTime = 0.0;

    public FiniteSimulationEventQueue() {
        this(60);
    }

    public FiniteSimulationEventQueue(int intervalLength) {
        this.intervalLength = intervalLength;
    }

    @Override
    public MsqEvent pop() throws Exception {
        var event = noPriority.peek();
        if (event == null) {
            throw new NoSuchElementException();
        }
        if (event.time > lastSaveTime + intervalLength) {
            lastSaveTime += intervalLength;
            return new MsqEvent(EventType.SAVE_STAT);
        } else {
            return super.pop();
        }
    }
}
