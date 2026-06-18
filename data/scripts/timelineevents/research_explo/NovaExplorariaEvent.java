package data.scripts.timelineevents.research_explo;

;
import data.scripts.models.TimelineEventType;
import data.scripts.timelineevents.templates.FirstIndustryEvent;

public class NovaExplorariaEvent extends FirstIndustryEvent {
    public NovaExplorariaEvent( String entityId) {
        super("aotd_nova_exploraria", entityId);
    }

    @Override
    public String getTitleOfEvent() {
        return "Rebirth of Explorarium";
    }

    @Override
    public TimelineEventType getEventType() {
        return TimelineEventType.RESEARCH_AND_EXPLORATION;
    }
    @Override
    public int getPointsForGoal() {
        return 70;
    }
}
