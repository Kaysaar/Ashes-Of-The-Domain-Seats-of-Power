package data.scripts.ambition;

import com.fs.starfarer.api.Global;
import data.scripts.ambition.subgoals.AmbitionSubGoalSpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedHashMap;

public class AmbitionSpecManager {
    public static LinkedHashMap<String, AmbitionSubGoalSpec> specSubGoals = new LinkedHashMap<>();
    public static LinkedHashMap<String,AmbitionSpec> specs = new LinkedHashMap<>();
    public static AmbitionSubGoalSpec getSubGoalSpec(String specId){
        return specSubGoals.get(specId);
    }
    public static AmbitionSpec  getAmbitionSpec(String specId){
        return specs.get(specId);
    }
     public  static void loadSpecs(){
        specSubGoals.clear();
        specs.clear();
        try {
            JSONArray array =Global.getSettings().getMergedSpreadsheetData("id", "data/campaign/aotd_ambitions_tasks.csv");
            JSONArray array2 =Global.getSettings().getMergedSpreadsheetData("id", "data/campaign/aotd_ambitions.csv");
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                AmbitionSubGoalSpec spec = AmbitionSubGoalSpec.getSpecFromJson(object);
                if(spec != null){
                    specSubGoals.put(spec.getId(), spec);
                }

            }
            for (int i = 0; i < array2.length(); i++) {
                JSONObject object = array2.getJSONObject(i);
                AmbitionSpec spec = AmbitionSpec.getSpecFromJson(object);
                if(spec != null){
                    specs.put(spec.getId(), spec);
                }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }
}
