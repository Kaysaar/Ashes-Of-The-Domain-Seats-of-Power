package data.scripts.timelineevents.research_explo;

import data.scripts.models.TimelineEventType;
import data.scripts.timelineevents.templates.FirstIndustryEvent;

public class HexagonEvent extends FirstIndustryEvent {
    public HexagonEvent( String entityId) {
        super("aotd_hexagon", entityId);
    }

    @Override
    public String getTitleOfEvent() {
        return "Hexagons are Bestagons";
    }

    @Override
    public TimelineEventType getEventType() {
        return TimelineEventType.MILITARY;
    }

    @Override
    public int getPointsForGoal() {
        return 70;
    }
}
