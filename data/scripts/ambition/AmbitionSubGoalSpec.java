package data.scripts.ambition;

import ashlib.data.plugins.misc.AshMisc;
import com.fs.starfarer.api.Global;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashSet;

public class AmbitionSubGoalSpec {
    String id,name;
    int weight;
    LinkedHashSet<String>reqTasksForFirst = new LinkedHashSet<>();
    public String ambitionTaskScript;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public LinkedHashSet<String> getReqTasksForFirst() {
        return reqTasksForFirst;
    }

    public static AmbitionSubGoalSpec getSpecFromJson(JSONObject object) throws JSONException {
        String id = object.optString("id");
        if(!AshMisc.isStringValid(id))return null;
        String name = object.optString("name");
        LinkedHashSet<String>reqTasksForFirst = new LinkedHashSet<>();
        String rqTasks = object.optString("reqTaskToDoFirst");
        if(AshMisc.isStringValid(rqTasks)){
            reqTasksForFirst.addAll(AshMisc.loadEntries(rqTasks,","));
        }
        int weight = object.getInt("weightOfTask");
        String className = object.optString("ambitionTaskScript");
        AmbitionSubGoalSpec spec = new AmbitionSubGoalSpec();
        spec.id = id;
        spec.name = name;
        spec.weight = weight;
        spec.ambitionTaskScript = className;
        spec.reqTasksForFirst.addAll(reqTasksForFirst);
        return spec;

    }
    public AmbitionSubGoal getAmbitionTaskScript() {
        try {
            return (AmbitionSubGoal) Global.getSettings().getScriptClassLoader().loadClass(ambitionTaskScript).newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
