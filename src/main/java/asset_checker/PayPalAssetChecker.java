package asset_checker;

import com.google.common.base.Splitter;
import model.ApiException;
import model.AssetChecker;
import model.asset.Account;
import model.asset.BasicAccount;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PayPalAssetChecker extends AssetChecker {

    private String apiUser;
    private String apiKey;
    private String apiSignature;

    public PayPalAssetChecker(String apiUser, String apiKey, String apiSignature) {
        super();
        this.apiUser = apiUser;
        this.apiKey = apiKey;
        this.apiSignature = apiSignature;
    }

    @Override
    public String getName() {
        return "PayPal";
    }

    @Override
    public List<Account> retrieveAccounts() throws ApiException {

        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();
        Executor executor = Executor.newInstance(client);

        URIBuilder builder = new URIBuilder();
        builder.setScheme("https").setHost("api-3t.paypal.com").setPath("/nvp")
                .setParameter("METHOD", "GetBalance")
                .setParameter("USER", apiUser)
                .setParameter("PWD", apiKey)
                .setParameter("SIGNATURE", apiSignature)
                .setParameter("VERSION", "1.2.0")
                .setParameter("RETURNALLCURRENCIES", "1");
        try {
            String url = builder.build().toString();
            Response response = executor.execute(Request.Get(url));
            Map<String, String> responseValues = splitToMap(response.returnContent().toString());
            return parseResponseValues(responseValues);
        } catch (Exception e) {
            throw new ApiException("Could not retrieve assets.", e);
        }
    }

    private List<Account> parseResponseValues(Map<String, String> responseValues) throws ApiException {
        List<Account> accountList = new LinkedList<>();
        Map<String, BigDecimal> assetValues = new HashMap<>();

        if (responseValues.get("ACK") == null || !responseValues.get("ACK").equals("Success")) {
            throw new ApiException("Could not retrieve assets.");
        }

        try {
            responseValues.keySet().forEach(key -> {
                String amountPrefix = "L_AMT";
                if (key.startsWith(amountPrefix)) {
                    String amountString = responseValues.get(key);
                    BigDecimal amount = new BigDecimal(amountString);
                    String currencyKey = "L_CURRENCYCODE" + key.substring(amountPrefix.length(), key.length());
                    String currency = responseValues.get(currencyKey);
                    assetValues.put(currency, amount);
                }
            });
        } catch (Exception e) {
            throw new ApiException("Could not retrieve assets.");

        }

        String eur = "EUR";
        if (!assetValues.keySet().contains(eur) || assetValues.keySet().size() > 1) {
            // Other currencies are not implemented yet.
            throw new ApiException("Could not retrieve assets.");
        }
        accountList.add(new BasicAccount(getName(), assetValues.get(eur)));

        return accountList;
    }

    private Map<String, String> splitToMap(String nvpString) {
        try {
            nvpString = java.net.URLDecoder.decode(nvpString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return Splitter.on("&").withKeyValueSeparator("=").split(nvpString);
    }

}
