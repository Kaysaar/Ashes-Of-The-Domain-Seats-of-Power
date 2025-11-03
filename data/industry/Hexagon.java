package data.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;
import data.scripts.managers.AoTDFactionManager;

public class Hexagon extends AoTDMilitaryBase {
    @Override
    public boolean isAvailableToBuild() {
        return AoTDFactionManager.getInstance().doesControlCapital()&&AoTDFactionManager.getInstance().getCapitalMarket().getId().equals(market.getId());
    }
    @Override
    public boolean showWhenUnavailable() {
        return market.hasCondition("aotd_capital");
    }
    public void apply() {

        int size = market.getSize();

        boolean patrol = getSpec().hasTag(Industries.TAG_PATROL);
        boolean militaryBase = getSpec().hasTag(Industries.TAG_MILITARY);
        boolean command = getSpec().hasTag(Industries.TAG_COMMAND);

        super.apply(!patrol);
        if (patrol) {
            applyIncomeAndUpkeep(3);
        }

        int extraDemand = 0;

        int light = 1;
        int medium = 0;
        int heavy = 0;

      extraDemand = 4;

        if (patrol) {
            light = 2;
            medium = 0;
            heavy = 0;
        } else {
            if (size <= 3) {
                light = 2;
                medium = 0;
                heavy = 0;
            } else if (size == 4) {
                light = 2;
                medium = 0;
                heavy = 0;
            } else if (size == 5) {
                light = 2;
                medium = 1;
                heavy = 0;
            } else if (size == 6) {
                light = 3;
                medium = 1;
                heavy = 0;
            } else if (size == 7) {
                light = 3;
                medium = 2;
                heavy = 0;
            } else if (size == 8) {
                light = 3;
                medium = 3;
                heavy = 0;
            } else if (size >= 9) {
                light = 4;
                medium = 3;
                heavy = 0;
            }
        }

        if (militaryBase || command) {
            //light++;
            medium = Math.max(medium + 1, size / 2 - 1);
            heavy = Math.max(heavy, medium - 1);
        }

        if (command) {
            medium++;
            heavy++;
        }
        medium+=2;
        heavy+=2;

//		if (market.getId().equals("jangala")) {
//			System.out.println("wefwefwe");
//		}

//		light += 5;
//		medium += 3;
//		heavy += 2;

//		float spawnRateMultStability = getStabilitySpawnRateMult();
//		if (spawnRateMultStability != 1) {
//			market.getStats().getDynamic().getStat(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).modifyMult(getModId(), spawnRateMultStability);
//		}


        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).modifyFlat(getModId(), light);
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(getModId(), medium);
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(getModId(), heavy);


        demand(Commodities.SUPPLIES, size - 1 + extraDemand);
        demand(Commodities.FUEL, size - 1 + extraDemand);
        demand(Commodities.SHIPS, size - 1 + extraDemand);

        supply(Commodities.CREW, size);

        if (!patrol) {
            //demand(Commodities.HAND_WEAPONS, size);
            supply(Commodities.MARINES, size);

//			Pair<String, Integer> deficit = getMaxDeficit(Commodities.HAND_WEAPONS);
//			applyDeficitToProduction(1, deficit, Commodities.MARINES);
        }


        modifyStabilityWithBaseMod();

        float mult = getDeficitMult(Commodities.SUPPLIES);
        String extra = "";
        if (mult != 1) {
            String com = getMaxDeficit(Commodities.SUPPLIES).one;
            extra = " (" + getDeficitText(com).toLowerCase() + ")";
        }
        float bonus = DEFENSE_BONUS_COMMAND;
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
                .modifyMult(getModId(), 1f + bonus * mult, getNameForModifier() + extra);


        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);

        if (militaryBase || command) {
            Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);
        }

        float officerProb = 0.6f;
        market.getStats().getDynamic().getMod(Stats.OFFICER_PROB_MOD).modifyFlat(getModId(0), officerProb);


        if (!isFunctional()) {
            supply.clear();
            unapply();
        }

    }

    @Override
    public void unapply() {
        super.unapply();

        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
        Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), false, -1);

        unmodifyStabilityWithBaseMod();

        //market.getStats().getDynamic().getStat(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).unmodifyMult(getModId());
        //market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MULT).unmodifyFlat(getModId());

        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).unmodifyFlat(getModId());
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).unmodifyFlat(getModId());
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodifyFlat(getModId());

        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());

        market.getStats().getDynamic().getMod(Stats.OFFICER_PROB_MOD).unmodifyFlat(getModId(0));
    }

}
