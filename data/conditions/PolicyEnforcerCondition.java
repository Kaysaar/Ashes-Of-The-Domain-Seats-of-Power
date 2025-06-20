package data.conditions;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import data.scripts.managers.AoTDFactionManager;

public class PolicyEnforcerCondition extends BaseMarketConditionPlugin {
    @Override
    public void apply(String id) {
        AoTDFactionManager.getInstance().getCurrentFactionPolicies().forEach(x->x.applyForMarket(this.market));
    }

    @Override
    public void unapply(String id) {
        AoTDFactionManager.getInstance().getCurrentFactionPolicies().forEach(x->x.unapplyForMarket(this.market));

    }

    @Override
    public boolean showIcon() {
        return false;
    }
}
