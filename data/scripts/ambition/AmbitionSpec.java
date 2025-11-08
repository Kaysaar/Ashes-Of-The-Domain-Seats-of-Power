package data.scripts.ambition;

import ashlib.data.plugins.misc.AshMisc;
import com.fs.starfarer.api.Global;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashSet;

public class AmbitionSpec {
    public String id,name,bannerId,desc,shortExplanation,fullBannerId;
    float order;
    LinkedHashSet<String>taskList = new LinkedHashSet<>();
    LinkedHashSet<String>startingTasks = new LinkedHashSet<>();
    String pluginClass;

    public LinkedHashSet<String> getStartingTasks() {
        return startingTasks;
    }

    public void setPluginClass(String pluginClass) {
        this.pluginClass = pluginClass;
    }

    public String getFullBannerId() {
        return fullBannerId;
    }

    public BaseAmbition getPluginInstance() throws Exception {
        BaseAmbition amb =  (BaseAmbition) Global.getSettings().getScriptClassLoader().loadClass(pluginClass).newInstance();
        amb.init(this.getId());
        return amb;
    }
    public static AmbitionSpec getSpecFromJson(JSONObject object) throws JSONException {
        String id = object.optString("id");
        if(!AshMisc.isStringValid(id))return null;
        String name = object.optString("name");


        LinkedHashSet<String>reqTasksForFirst = new LinkedHashSet<>();
        LinkedHashSet<String>startTask = new LinkedHashSet<>();

        String rqTasks = object.optString("taskList");
        String rqTasks2 = object.optString("startingTaskList");
        if(AshMisc.isStringValid(rqTasks)){
            reqTasksForFirst.addAll(AshMisc.loadEntries(rqTasks,","));
        }
        if(AshMisc.isStringValid(rqTasks2)){
            startTask.addAll(AshMisc.loadEntries(rqTasks,","));
        }

        int weight = object.getInt("order");
        String className = object.optString("ambitionScript");
        String banerId = object.optString("banner_id");
        String fullbanerId = object.optString("full_banner_id");
        AmbitionSpec spec = new AmbitionSpec();
        String desc = object.optString("desc");
        spec.id = id;
        spec.name = name;
        spec.order = weight;
        spec.desc = desc;
        spec.fullBannerId = fullbanerId;
        spec.shortExplanation = object.optString("shortExplanation");
        spec.pluginClass = className;
        spec.bannerId = banerId;
        spec.startingTasks = startTask;

        spec.taskList.addAll(reqTasksForFirst);

        return spec;

    }
    public String getId(){
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBannerId(String bannerId) {
        this.bannerId = bannerId;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setShortExplanation(String shortExplanation) {
        this.shortExplanation = shortExplanation;
    }
    public void setOrder(float order) {
        this.order = order;
    }

    public float getOrder() {
        return order;
    }

    public LinkedHashSet<String> getTaskList() {
        return taskList;
    }

    public String getBannerId() {
        return bannerId;
    }

    public String getDesc() {
        return desc;
    }

    public String getShortExplanation() {
        return shortExplanation;
    }
}
