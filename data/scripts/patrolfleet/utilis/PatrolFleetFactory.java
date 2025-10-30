package data.scripts.patrolfleet.utilis;

import ashlib.data.plugins.misc.AshMisc;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import data.scripts.patrolfleet.models.BasePatrolFleet;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;

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
            BasePatrolFleetTemplate template,
            FleetParamsV3 params,
            MarketAPI source,
            Random rng
    ) {
        if (template == null || template.assignedShipsThatShouldSpawn == null) {
            return null;
        }
        if (rng == null) rng = new Random();
        if(template instanceof BasePatrolFleet fleet){
            fleet.performReplacement();
        }
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
        CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(factionId, params.fleetType, true);

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
        if(template.getTotalFleetPoints()>=25){
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_TYPE, FleetTypes.PATROL_SMALL);
        }
        if(template.getTotalFleetPoints()>=50){
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_TYPE, FleetTypes.PATROL_MEDIUM);
        }
        if(template.getTotalFleetPoints()>=75){
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_TYPE, FleetTypes.PATROL_LARGE);
        }
        return fleet;
    }

    /**
     * Overload: wrap BasePatrolFleet to a template and forward to the main builder.
     */
    public static CampaignFleetAPI createFleetFromAssigned(
            BasePatrolFleet fleetModel,
            FleetParamsV3 params,
            MarketAPI source,
            Random rng
    ) {
        BasePatrolFleetTemplate t = new BasePatrolFleetTemplate(
                new java.util.LinkedHashMap<>(fleetModel.assignedShipsThatShouldSpawn),
                fleetModel.getNameOfFleet()
        );
        return createFleetFromAssigned(t, params, source, rng);
    }
}
