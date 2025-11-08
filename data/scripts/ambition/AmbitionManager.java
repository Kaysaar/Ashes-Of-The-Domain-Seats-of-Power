package data.scripts.ambition;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.IntervalUtil;

public class AmbitionManager implements EveryFrameScript {
    public BaseAmbition currentAmbition;
    IntervalUtil util = new IntervalUtil(0.5f,1f);
    public static String key = "$aotd_amb_manager";
    @Override
    public boolean isDone() {
        return false;
    }

    public void setCurrentAmbition(BaseAmbition currentAmbition) {
        this.currentAmbition = currentAmbition;
    }

    public BaseAmbition getCurrentAmbition() {
        return currentAmbition;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }
    public static AmbitionManager getInstance() {
        if(!Global.getSector().getPersistentData().containsKey(key))setInstance();
        return (AmbitionManager) Global.getSector().getPersistentData().get(key);

    }
    public static void setInstance(){
        AmbitionManager instance = new AmbitionManager();
        Global.getSector().getPersistentData().put(key, instance);
        Global.getSector().addScript(instance);
    }

    @Override
    public void advance(float amount) {
        util.advance(amount);
        if(util.intervalElapsed()){

        }
    }
}
