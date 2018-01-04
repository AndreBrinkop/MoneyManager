package model.asset;

import java.util.List;

public interface AssetObject {

    public String getName();

    public Balance getCurrentEurBalance();

    public Balance getCurrentEurBalance(List<String> ignoredAccountNames);

}
