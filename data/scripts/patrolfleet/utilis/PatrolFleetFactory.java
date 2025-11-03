package data.scripts.patrolfleet.utilis;

import ashlib.data.plugins.misc.AshMisc;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;

import java.awt.*;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3.addCommanderAndOfficers;

public class PatrolFleetFactory {

    /**
     * Build a fleet strictly from template.assignedShipsThatShouldSpawn,
     * then finish (inflater, commander, officers, etc.) using FleetParamsV3.
     *
     * @param template  hullId -> count source (required)
     * @param params    FleetParamsV3 with faction/source/quality/etc.
     * @param source    optional MarketAPI fallback if params.source is null
     * @param rng       optional RNG for officer creation/autofit; null -> new Random()
     */
    public static CampaignFleetAPI createFleetFromAssigned(
            BasePatrolFleet template,
            FleetParamsV3 params,
            MarketAPI source,
            Random rng
    ) {
        if (template == null || template.assignedShipsThatShouldSpawn == null) {
            return null;
        }
        if (rng == null) rng = new Random();

            template.performReplacement();

        // Ensure params.source is set (vanilla inflaters/officer logic often rely on it)
        if (params.source == null) {
            params.source = source;
        } else {
            source = params.source; // prefer params.source if provided
        }

        // Resolve faction id
        String factionId = params.factionId;
        if (factionId == null) {
            if (params.source != null) {
                factionId = params.source.getFactionId();
            } else {
                // Last-ditch fallback; avoids NPE if user forgot to set factionId and source
                factionId = Global.getSector().getPlayerFaction().getId();
            }
        }

        // Create empty fleet
        CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(factionId, params.fleetType, source);

        // Name it
        if (template.getNameOfTemplate() != null) {
            fleet.setName(template.getNameOfTemplate());
        } else if (source != null) {
            fleet.setName(source.getFaction().getDisplayName() + " " + Misc.ucFirst(params.fleetType));
        }

        // Add ships exactly as provided by the template
        for (Map.Entry<String, Integer> e : template.assignedShipsThatShouldSpawn.entrySet()) {
            final String hullId = e.getKey();
            final int count = Math.max(0, e.getValue());
            if (count <= 0) continue;

            ShipHullSpecAPI spec = null;
            try {
                spec = Global.getSettings().getHullSpec(hullId);
            } catch (RuntimeException ex) {
                // Hull not found; skip gracefully
            }
            if (spec == null) {
                Global.getLogger(PatrolFleetFactory.class).warn("Unknown hullId: " + hullId + " (skipping)");
                continue;
            }

            for (int i = 0; i < count; i++) {
                FleetMemberAPI member = Global.getFactory().createFleetMember(
                        FleetMemberType.SHIP,
                        AshMisc.getVaraint(spec) // your helper returns a variant id for this hull
                );
                // mark as refit source so inflater is allowed to change it
                member.getVariant().setSource(VariantSource.REFIT);
                fleet.getFleetData().addFleetMember(member);
            }
        }

        // Minor sorting/cleanup
        fleet.getFleetData().setOnlySyncMemberLists(false);
        fleet.getFleetData().sort();
        if (params.withOfficers) {
            addCommanderAndOfficers(fleet, params, rng);
        }

        if (fleet.getFlagship() != null) {
            if (params.flagshipVariantId != null) {
                fleet.getFlagship().setVariant(Global.getSettings().getVariant(params.flagshipVariantId), false, true);
            } else if (params.flagshipVariant != null) {
                fleet.getFlagship().setVariant(params.flagshipVariant, false, true);
            }
            if (fleet.getFlagship() != null && fleet.getFlagship().getStatus() != null) {
                fleet.getFlagship().getStatus().updateNumStatusesFromMember();
            }
        }

        if (params.onlyRetainFlagship != null && params.onlyRetainFlagship) {
            for (FleetMemberAPI curr : fleet.getFleetData().getMembersListCopy()) {
                if (curr.isFlagship()) continue;
                fleet.getFleetData().removeFleetMember(curr);
            }
        }
        //fleet.getFlagship()
        fleet.forceSync();
        // Put at market location if available
        if (source != null && source.getPrimaryEntity() != null) {
            SectorEntityToken loc = source.getPrimaryEntity();
            fleet.setLocation(loc.getLocation().x, loc.getLocation().y);
        }

        // CR = max
        for (FleetMemberAPI m : fleet.getFleetData().getMembersListCopy()) {
            if (m.getRepairTracker() != null) {
                m.getRepairTracker().setCR(m.getRepairTracker().getMaxCR());
            }
        }

        // --- Inflater setup using params ---
        // Figure out quality to pass to inflater (respect qualityOverride if set upstream)
        float resolvedQuality = params.quality + params.qualityMod;
        if (params.qualityOverride != null) {
            resolvedQuality = params.qualityOverride;
        }

        DefaultFleetInflaterParams ip = new DefaultFleetInflaterParams();
        ip.quality = resolvedQuality;
        if (params.averageSMods != null) ip.averageSMods = params.averageSMods;
        ip.persistent = true;
        ip.seed = (params.random != null ? params.random : rng).nextLong();
        ip.mode = params.mode;           // use whatever mode you’ve set in params
        ip.timestamp = params.timestamp; // carry through for persistence/consistency
        ip.allWeapons = params.allWeapons;
        if (params.factionId != null) {
            ip.factionId = params.factionId;
        }

        FleetInflater inflater = Misc.getInflater(fleet, ip);
        fleet.setInflater(inflater);

        // Officers & commander using vanilla logic (uses params knobs)
        // If your codebase exposes a V2/overload, swap the call below accordingly.

        // Scale the spawn FP to actual content (optional, but mirrors vanilla at the end)
        float requestedPoints = params.getTotalPts();
        float actualPoints = fleet.getFleetPoints();
        Misc.setSpawnFPMult(fleet, actualPoints / Math.max(1f, requestedPoints));
        if(template.getPatrolType().equals(FleetFactory.PatrolType.FAST)){
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_TYPE, FleetTypes.PATROL_SMALL);
        }
        if(template.getPatrolType().equals(FleetFactory.PatrolType.COMBAT)){
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_TYPE, FleetTypes.PATROL_MEDIUM);
        }
        if(template.getPatrolType().equals(FleetFactory.PatrolType.HEAVY)){
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_TYPE, FleetTypes.PATROL_LARGE);
        }
        return fleet;
    }
    public static void addMiniForCurrentQuality(TooltipMakerAPI tooltip, float pad, FactionAPI faction, int typicalCombatShips) {
        final FactionDoctrineAPI doctrine = (faction != null ? faction.getDoctrine() : Global.getSector().getPlayerFaction().getDoctrine());
        final int q = Math.max(1, doctrine.getOfficerQuality());

        final float mercMult           = Global.getSettings().getFloat("officerAIMaxMercsMult");
        final int   baseMaxOfficerLvl  = Global.getSettings().getInt("officerMaxLevel");
        final float baseShipsForMaxLvl = Global.getSettings().getFloat("baseCombatShipsForMaxOfficerLevel");

        // +Officers (merc cap bump)
        int extraOfficers = (int) Math.floor(q * mercMult);

        // Coverage scaling (caps at Q=5)
        float oqMult = Math.min(1f, (q - 1f) / 4f);

        // Estimate average officer level for the given fleet size
        float shipsNeededForCap = baseShipsForMaxLvl * (1f - 0.5f * oqMult);
        float fleetFactor = Math.min(1f, typicalCombatShips / Math.max(1f, shipsNeededForCap));
        int maxLvl = Math.round((q / 2f) + fleetFactor * baseMaxOfficerLvl);
        if (maxLvl < 1) maxLvl = 1;
        int avgLvl = Math.max(2, Math.min(maxLvl, Math.round(maxLvl * 0.75f)));

        tooltip.addPara("Current level: %s",pad, Color.ORANGE,""+q);
        tooltip.setBulletedListMode(BaseIntelPlugin.BULLET);
        tooltip.addPara("Additional officers in fleet : %s",3f,Color.ORANGE,""+extraOfficers);
        tooltip.addPara("Average officer level : %s",3f,Color.ORANGE,""+avgLvl);
        tooltip.setBulletedListMode(null);
    }

    /** Simple estimator: count non-fighter members; optionally exclude civilian hulls. */
    private static int estimateCombatShips(CampaignFleetAPI fleet, boolean includeCivilians) {
        if (fleet == null) return Math.round(Global.getSettings().getFloat("baseCombatShipsForMaxOfficerLevel"));
        int n = 0;
        for (FleetMemberAPI m : fleet.getFleetData().getMembersListCopy()) {
            if (m.isFighterWing()) continue;
            if (!includeCivilians && m.isCivilian()) continue;
            n++;
        }
        return Math.max(1, n);
    }

}
