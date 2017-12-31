package model.asset_checker;

import model.ApiException;
import model.asset.AssetSourceCredentials;
import model.asset.account.Account;
import model.asset.account.BasicAccount;
import model.asset.account.CurrencyAccount;
import model.asset_checker.abstract_checker.AssetChecker;
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
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import static util.CryptoHelper.hmacSHA256;

public class CoinbaseAssetChecker extends AssetChecker {

    private Executor executor;

    public CoinbaseAssetChecker(AssetSourceCredentials credentials) {
        super(credentials);
        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();
        executor = Executor.newInstance(client);
    }

    @Override
    public String getName() {
        return "Coinbase";
    }

    @Override
    public List<Account> retrieveAccounts() throws ApiException {
        try {
            // just the first 100 accounts are displayed; Pagination is not implemented yet
            String apiVersion = "2017-08-07";
            String path = "/v2/accounts?&limit=100";
            String url = "https://api.coinbase.com/" + path;
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            String preHashSignature = timeStamp + "GET" + path;
            Response response = executor.execute(Request.Get(url)
                    .addHeader("CB-VERSION", apiVersion)
                    .addHeader("CB-ACCESS-KEY", credentials.getKey())
                    .addHeader("CB-ACCESS-SIGN", hmacSHA256(preHashSignature, credentials.getSecret()))
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
            BigDecimal amount = balanceJsonObject.getBigDecimal("amount");

            if (!"EUR".equals(currency)) {
                BigDecimal exchangeRate = BigDecimal.ZERO;
                if (amount.doubleValue() >= 0.0D) {
                    exchangeRate = retrieveExchangeRateToEuro(executor, currency);
                }
                accounts.add(new CurrencyAccount(name, currency, amount, exchangeRate));

            } else {
                accounts.add(new BasicAccount(name, amount));
            }
        }
        return accounts;
    }

    public static BigDecimal retrieveExchangeRateToEuro(Executor executor, String currency) throws ApiException {
        String url = "https://api.coinbase.com/v2/prices/" + currency + "-EUR/spot";
        try {
            Content content = executor.execute(Request.Get(url)).returnContent();
            JSONObject jsonObject = new JSONObject(content.toString());
            JSONObject jsonDataObject = jsonObject.getJSONObject("data");
            BigDecimal amount = jsonDataObject.getBigDecimal("amount");
            return amount;
        } catch (IOException e) {
            throw new ApiException("Could not retrieve exchange rate for: " + currency + ".");
        }

    }


}
