package asset_checker;

import model.ApiException;
import model.AssetChecker;
import model.asset.Account;
import model.asset.BasicAccount;
import model.asset.CurrencyAccount;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static util.CryptoHelper.hmacSHA256;

public class CoinbaseAssetChecker extends AssetChecker {

    private String apiKey;
    private String apiSecret;

    private Executor executor;

    public CoinbaseAssetChecker(String apiKey, String apiSecret) {
        super();
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();
        executor = Executor.newInstance(client);
    }

    @Override
    public String getName() {
        return "Coinbase";
    }

    @Override
    protected List<Account> retrieveAccounts() throws ApiException {
        try {
            // just the first 100 accounts are displayed; Pagination is not implemented yet
            String apiVersion = "2017-08-07";
            String path = "/v2/accounts?&limit=100";
            String url = "https://api.coinbase.com/" + path;
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            String preHashSignature = timeStamp + "GET" + path;
            Response response = executor.execute(Request.Get(url)
                    .addHeader("CB-VERSION", apiVersion)
                    .addHeader("CB-ACCESS-KEY", apiKey)
                    .addHeader("CB-ACCESS-SIGN", hmacSHA256(preHashSignature, apiSecret))
                    .addHeader("CB-ACCESS-TIMESTAMP", timeStamp));

            Content content = response.returnContent();
            JSONObject jsonObject = new JSONObject(content.toString());
            JSONArray jsonAccountArray = jsonObject.getJSONArray("data");
            return parseJsonAccounts(jsonAccountArray);
        } catch (Exception e) {
            throw new ApiException("Could not retrieve assets.", e);
        }
    }

    private List<Account> parseJsonAccounts(JSONArray jsonAccountArray) throws ApiException {
        List<Account> accounts = new LinkedList<>();
        for (Object object : jsonAccountArray) {
            JSONObject jsonAccountObject = (JSONObject) object;
            JSONObject balanceJsonObject = jsonAccountObject.getJSONObject("balance");

            String name = jsonAccountObject.getString("name");
            String currency = balanceJsonObject.getString("currency");
            Double amount = balanceJsonObject.getDouble("amount");

            if (!"EUR".equals(currency)) {
                Double exchangeRate = 0.0D;
                if (amount >= 0.0D) {
                    exchangeRate = retrieveExchangeRateToEuro(currency);
                }
                accounts.add(new CurrencyAccount(name, currency, amount, amount * exchangeRate));
            } else {
                accounts.add(new BasicAccount(name, amount));
            }
        }
        return accounts;
    }

    private Double retrieveExchangeRateToEuro(String currency) throws ApiException {
        String url = "https://api.coinbase.com/v2/prices/" + currency + "-EUR/spot";
        try {
            Content content = executor.execute(Request.Get(url)).returnContent();
            JSONObject jsonObject = new JSONObject(content.toString());
            JSONObject jsonDataObject = jsonObject.getJSONObject("data");
            Double amount = jsonDataObject.getDouble("amount");
            return amount;
        } catch (IOException e) {
            throw new ApiException("Could not retrieve exchange rate for: " + currency + ".");
        }

    }


}
