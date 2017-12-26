package asset_checker;

import asset_checker.abstract_checker.CryptoCurrencyAssetChecker;
import model.ApiException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class BitcoinCashAssetChecker extends CryptoCurrencyAssetChecker {

    public BitcoinCashAssetChecker(String address) {
        super(address);
    }

    public BitcoinCashAssetChecker(List<String> addressList) {
        super(addressList);
    }

    @Override
    protected int getSmallestUnitExponent() {
        return 8;
    }

    @Override
    protected String getCurrency() {
        return "BCH";
    }

    @Override
    public String getName() {
        return "Bitcoin Cash Address";
    }

    @Override
    protected BigDecimal getAddressBalance(String address) throws ApiException {
        String url = "https://api.blockchair.com/bitcoin-cash/dashboards/address/" + address;
        Executor executor = getExecutor();
        try {
            Content content = executor.execute(Request.Get(url)).returnContent();
            JSONObject responseObject = new JSONObject(content.asString());
            JSONArray dataArray = responseObject.getJSONArray("data");
            if (dataArray.length() != 1) {
                throw new ApiException("Could not get balance for Bitcoin Cash address: " + address + " (Invalid API response)");
            }
            JSONObject dataObject = dataArray.getJSONObject(0);
            BigDecimal decimalBalance = dataObject.getBigDecimal("sum_value_unspent");
            BigDecimal bchBalance = decimalsToWholeUnit(decimalBalance);
            return bchBalance;
        } catch (IOException | JSONException e) {
            throw new ApiException("Could not get balance for Bitcoin Cash address: " + address, e);
        }
    }

    @Override
    protected List<BigDecimal> getAddressListBalances(List<String> addressList) throws ApiException {
        return null;
    }

}
