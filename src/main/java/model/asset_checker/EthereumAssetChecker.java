package model.asset_checker;

import model.ApiException;
import model.asset.AssetSourceCredentials;
import model.asset_checker.abstract_checker.CryptoCurrencyAssetChecker;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

public class EthereumAssetChecker extends CryptoCurrencyAssetChecker {

    public EthereumAssetChecker(AssetSourceCredentials credentials) {
        super(credentials);
    }

    public EthereumAssetChecker(List<AssetSourceCredentials> credentialsList) {
        super(credentialsList);
    }

    public EthereumAssetChecker(AssetSourceCredentials... credentialsObjects) {
        super(Arrays.asList(credentialsObjects));
    }


    @Override
    protected int getSmallestUnitExponent() {
        return 18;
    }

    @Override
    protected String getCurrency() {
        return "ETH";
    }

    @Override
    protected BigDecimal getAddressBalance(String address) throws ApiException {
        return getAddressListBalances(asList(address)).get(0);
    }

    @Override
    protected List<BigDecimal> getAddressListBalances(List<String> addressList) throws ApiException {
        BigDecimal[] balances = new BigDecimal[addressList.size()];
        String apiToken = ""; // Currently not needed?
        String addressString = String.join(",", addressList);
        String url = "https://api.etherscan.io/api?module=account&action=balancemulti&address=" + addressString + "&tag=latest&apikey=" + apiToken;
        try {
            Content content = getExecutor().execute(Request.Get(url)).returnContent();
            JSONObject jsonObject = new JSONObject(content.toString());
            if (!"OK".equals(jsonObject.getString("message"))) {
                throw new ApiException("Could not get balance for Ethereum addresses. (Invalid API response)");
            }
            JSONArray resultJsonArray = jsonObject.getJSONArray("result");
            for (Object object : resultJsonArray) {
                JSONObject resultJsonObject = (JSONObject) object;
                String address = resultJsonObject.getString("account");
                BigDecimal balance = decimalsToWholeUnit(resultJsonObject.getBigDecimal("balance"));
                balances[addressList.indexOf(address)] = balance;
            }
        } catch (IOException | JSONException e) {
            throw new ApiException("Could not get balance for Ethereum addresses.", e);
        }
        return Arrays.asList(balances);
    }

    @Override
    public String getName() {
        return "Ethereum Address";
    }
}
