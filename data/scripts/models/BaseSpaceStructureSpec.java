package data.scripts.models;

import java.util.LinkedHashSet;

public class BaseSpaceStructureSpec {
    String id,name,downgradeId,desc,scriptPath;
    int cost,income,upkeep;
    boolean usesStablePoint;
    LinkedHashSet<String>upgrades,tags;
}
