package org.pmcsn.model;

public class MsqEvent {
    public double service;
    public double time;   //time
    public final EventType type; //type
    public int serverId;
    public int nodeId;
    public boolean hasPriority = false;     //if the event has priority

    public MsqEvent(EventType type, double time, double service, int serverId, int nodeId,  boolean hasPriority) {
        this.type = type;
        this.service = service;
        this.time = time;
        this.serverId = serverId;
        this.nodeId = nodeId;
        this.hasPriority = hasPriority;
    }

    public MsqEvent(EventType type, double time, int serverId) {
        this(type, time, 0, serverId, 0, false);
    }

    public MsqEvent(EventType type, double time) {
        this(type, time, 0, false);
    }

    public MsqEvent(EventType type, double time, int serverId, boolean hasPriority) {
        this(type, time, 0, serverId, 0, hasPriority);
    }

    public MsqEvent(EventType type, double time, boolean hasPriority) {
        this(type, time, 0, hasPriority);
    }

    public MsqEvent(EventType type, double time, double service, int serverId) {
        this(type, time, service, serverId, 0,false);
    }

    public MsqEvent(EventType eventType, double time, double service, int serverId, int nodeId) {
        this(eventType, time, service, serverId, nodeId, false);
    }

    public MsqEvent(EventType type, double time, double service) {
        this(type, time, service, 0, 0);
    }

    public double getTime(){
        return time;
    }
}
