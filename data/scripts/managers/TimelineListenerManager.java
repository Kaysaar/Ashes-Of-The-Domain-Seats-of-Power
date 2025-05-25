package data.scripts.managers;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.IntervalUtil;
import data.listeners.timeline.models.BaseTimelineListener;

import java.util.ArrayList;

public class TimelineListenerManager implements EveryFrameScript {
    public IntervalUtil intervalCheck = new IntervalUtil(2f, 2f);
    public ArrayList<BaseTimelineListener> transientListeners = new ArrayList<>();
    protected ArrayList<BaseTimelineListener>listenersInQueue = new ArrayList<>();
    protected boolean needsResetAfterInterval = false;

    public void setNeedsResetAfterInterval(boolean needsResetAfterInterval) {
        this.needsResetAfterInterval = needsResetAfterInterval;
    }

    public void addNewListener(BaseTimelineListener listener) {
        if(listener.isDone())return;
        if(listenersInQueue==null)listenersInQueue = new ArrayList<>();
        listenersInQueue.add(listener);
    }

    public void pruneListeners() {
        transientListeners.removeIf(BaseTimelineListener::isDone);
    }
    public void pruneAllListeners() {
        transientListeners.clear();
    }


    public static TimelineListenerManager getInstance() {
        TimelineListenerManager manager = (TimelineListenerManager) Global.getSector().getScripts().stream().filter(x -> x instanceof TimelineListenerManager).findFirst().orElse(new TimelineListenerManager());
        if (!Global.getSector().hasScript(manager.getClass())) {
            Global.getSector().addScript(manager);
            manager.intervalCheck.advance(2f);
        }
        return manager;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        intervalCheck.advance(amount);
        if (intervalCheck.intervalElapsed()) {
            if(needsResetAfterInterval){
                needsResetAfterInterval = false;
                pruneAllListeners();
                transientListeners.addAll(listenersInQueue);
                listenersInQueue.clear();
            }
            executeAllListeners(amount);
        }
    }
    public void executeAllListeners(float amount) {
        if (!AoTDFactionManager.getMarketsUnderPlayer().isEmpty()) {
            transientListeners.forEach(x -> x.advance(amount));
            Global.getSector().getEconomy().getMarketsCopy().forEach(x -> x.getMemoryWithoutUpdate().set("$aotd_was_colonized",true));
        }
        pruneListeners();
        intervalCheck.nextInterval();
    }
    public void executeListenersOfClass(float amount,Class<?>clazz) {
        if (!AoTDFactionManager.getMarketsUnderPlayer().isEmpty()) {
            transientListeners.stream().filter(x->x.getClass().equals(clazz)).forEach(x->x. advanceImpl(amount));
        }
        pruneListeners();
    }


}
