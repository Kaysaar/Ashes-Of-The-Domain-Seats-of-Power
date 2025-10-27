package data.ui.patrolfleet.templates.components;

import ashlib.data.plugins.misc.AshMisc;
import data.scripts.patrolfleet.models.BasePatrolFleetTemplate;

import java.util.Comparator;

public class TemplateSearchBarComparator implements Comparator<BasePatrolFleetTemplate> {
    private String searchString;
    private int threshold;

    public TemplateSearchBarComparator(String searchString, int threshold) {
        this.searchString = searchString.toLowerCase();
        this.threshold = threshold;
    }

    @Override
    public int compare(BasePatrolFleetTemplate s1, BasePatrolFleetTemplate s2) {

        String s1S="";
        String s2S ="";
        s1S = s1.getNameOfTemplate();
        s2S = s2.getNameOfTemplate();


        int distance1 = AshMisc.levenshteinDistance(searchString, s1S);
        int distance2 = AshMisc.levenshteinDistance(searchString, s2S);
        if(distance1==distance2){
            return Integer.compare((int) s1.getTotalFleetPoints(), (int) s2.getTotalFleetPoints());
        }
        boolean s1Contains = s1S.contains(searchString);
        boolean s2Contains = s2S.contains(searchString);

        // Prioritize strings that contain the searchString as a substring
        if (s1Contains && !s2Contains) {
            return -1;
        } else if (!s1Contains && s2Contains) {
            return 1;
        }

        // If both contain the searchString, or both don't, compare by Levenshtein distance
        return Integer.compare(distance1, distance2);
    }

    public boolean isValid(String s) {
        return AshMisc.levenshteinDistance(searchString, s.toLowerCase()) <= threshold || s.toLowerCase().contains(searchString);
    }
}
