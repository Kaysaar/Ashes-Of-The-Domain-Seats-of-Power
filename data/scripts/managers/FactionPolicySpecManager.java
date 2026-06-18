package data.scripts.managers;

import com.fs.starfarer.api.Global;
import data.scripts.models.FactionPolicySpec;
import data.scripts.models.FactionPolicyType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedHashMap;

public class FactionPolicySpecManager {
    static LinkedHashMap<String, FactionPolicySpec> factionPolicySpecs = new LinkedHashMap<>();
    public static final String specsFilename = "data/campaign/aotd_policies.csv";
    public static FactionPolicySpec getSpec(String id){
        if(factionPolicySpecs.isEmpty())loadSpecs();
        return factionPolicySpecs.get(id);
    }

    public static LinkedHashMap<String, FactionPolicySpec> getFactionPolicySpecs() {
        return factionPolicySpecs;
    }

    public static void loadSpecs(){
        if(factionPolicySpecs==null){
            factionPolicySpecs = new LinkedHashMap<>();

        }
        factionPolicySpecs.clear();
        try {
            JSONArray resArray = Global.getSettings().getMergedSpreadsheetDataForMod("id", specsFilename, "aotd_sop");
            for (int i = 0; i < resArray.length(); i++) {
                JSONObject obj = resArray.getJSONObject(i);
                FactionPolicySpec spec = FactionPolicySpec.createSpecFromJson(obj);
                if (spec != null) {
                    factionPolicySpecs.put(spec.getId(), spec);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }
}
