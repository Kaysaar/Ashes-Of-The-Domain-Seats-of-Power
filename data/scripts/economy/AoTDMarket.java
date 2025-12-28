package data.scripts.economy;

import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.PriceVariability;
import com.fs.starfarer.campaign.econ.CommodityOnMarket;
import com.fs.starfarer.campaign.econ.Economy;
import com.fs.starfarer.campaign.econ.Market;

public class AoTDMarket extends Market {
    public AoTDMarket(String id, String name, int i, Economy economy) {
        super(id, name, i, economy);
    }

    @Override
    public float getDemandPrice(String comID, double quantity, boolean isPlayer) {
        return getDemandPriceAssumingExistingTransaction(
                comID, quantity, 0d, isPlayer
        );
    }
//    @Override
//    public float getDemandPriceAssumingExistingTransaction(
//            String comID, double quantity, double existingTransactionValue, boolean isPlayer
//    ) {
//        return getDemandPriceAssumingStockpileUtility(EconomyEngine
//                getCommodityData(comID), 0, quantity + existingTransactionValue, isPlayer
//        );
//    }
//
//    @Override
//    public float getDemandPriceAssumingStockpileUtility(CommodityOnMarket commodityOnMarket, double stockpiles, double quantity, boolean isPLayer) {
//        final CommoditySpecAPI spec = com.getCommodity();
//        if (spec.getPriceVariability() == PriceVariability.V0) {
//            return spec.getBasePrice() * (float) quantity;
//        }
//
//        final CommodityStats stats = EconomyEngine.getInstance().getComStats(
//                com.getId(), com.getMarket().getId()
//        );
//
//        return stats.computeVanillaPrice(
//                (int) quantity, true, isPlayer
//        );
//    }
}
