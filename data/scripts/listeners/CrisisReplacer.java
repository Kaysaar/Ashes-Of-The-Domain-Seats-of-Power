package data.scripts.listeners;

import com.fs.starfarer.api.campaign.listeners.ColonyCrisesSetupListener;
import com.fs.starfarer.api.impl.campaign.intel.events.HegemonyAICoresActivityCause;
import com.fs.starfarer.api.impl.campaign.intel.events.HegemonyHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import data.scripts.cause.AILegalizationCause;
import data.scripts.crisis.AILegalizationCrisis;

public class CrisisReplacer implements ColonyCrisesSetupListener {
    @Override
    public void finishedAddingCrisisFactors(HostileActivityEventIntel intel) {
//        intel.removeActivityCause(HegemonyHostileActivityFactor.class, HegemonyAICoresActivityCause.class);
//        intel.addActivity(new AILegalizationCrisis(intel),new AILegalizationCause(intel));
    }
}
