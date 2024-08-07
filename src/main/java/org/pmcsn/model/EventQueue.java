package org.pmcsn.model;

import java.util.AbstractCollection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class EventQueue {
    private static final Comparator<MsqEvent> CMP = Comparator.comparingDouble(o -> o.time);

    private final List<PriorityQueue<MsqEvent>> priority = List.of(new PriorityQueue<>(CMP), new PriorityQueue<>(CMP));
    private final PriorityQueue<MsqEvent> noPriority = new PriorityQueue<>(CMP);

    public void add(MsqEvent event) {
        noPriority.add(event);
    }

    public void addPriority(MsqEvent event) {
        if (event.hasPriority) {
            priority.getFirst().add(event);
        } else {
            priority.getLast().add(event);
        }
    }

    public boolean isEmpty() {
        return noPriority.isEmpty() && priority.stream().allMatch(AbstractCollection::isEmpty);
    }

    public int size() {
        return noPriority.size() + priority.stream().mapToInt(AbstractCollection::size).sum();
    }

    // returns the event with the smallest time among ALL queues and removes it
    public MsqEvent pop() throws Exception {
        MsqEvent e1 = noPriority.peek();
        MsqEvent e2 = peek(priority);
        if (e1 == null && e2 == null) {
            throw new Exception("No events in queue");
        }
        if (e1 == null) {
            return poll(priority);
        } else if (e2 == null) {
            return noPriority.poll();
        } else if (e1.time <= e2.time) {
            return noPriority.poll();
        } else {
            return poll(priority);
        }
    }

    // returns the event with the smallest time without removing it
    private MsqEvent peek(List<PriorityQueue<MsqEvent>> priorityQueues) {
        for (PriorityQueue<MsqEvent> queue : priorityQueues) {
            if (!queue.isEmpty()) {
                return queue.peek();
            }
        }
        return null;
    }

    // returns the event with the smallest time among all the priority queues and removes it
    private MsqEvent poll(List<PriorityQueue<MsqEvent>> priorityQueues) {
        for (PriorityQueue<MsqEvent> queue : priorityQueues) {
            if (!queue.isEmpty()) {
                return queue.poll();
            }
        }
        return null;
    }
}
