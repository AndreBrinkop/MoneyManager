package model.asset_checker;

import edu.self.kraken.api.KrakenApi;
import model.ApiException;
import model.asset.AssetSourceCredentials;
import model.asset.account.Account;
import model.asset.account.BasicAccount;
import model.asset.account.CurrencyAccount;
import model.asset_checker.abstract_checker.AssetChecker;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class KrakenAssetChecker extends AssetChecker {

    private KrakenApi api;

    public KrakenAssetChecker(AssetSourceCredentials credentials) {
        super();
        this.api = new edu.self.kraken.api.KrakenApi();
        this.api.setKey(credentials.getKey());
        this.api.setSecret(credentials.getSecret());
    }

    public String getName() {
        return "kraken.com";
    }

    public List<Account> retrieveAccounts() throws ApiException {
        Map<String, BigDecimal> assets = getAssetValues();
        Map<String, BigDecimal> conversionRates = getConversionRatesToEur(
                assets.entrySet()
                        .stream()
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet())
        );
        return createAssetObjects(assets, conversionRates);
    }

    private List<Account> createAssetObjects(Map<String, BigDecimal> assets, Map<String, BigDecimal> conversionRates) throws ApiException {
        List<Account> accountList = new LinkedList<>();
        assets.forEach((String currency, BigDecimal value) -> {
            String currencyWithoutPrefix = currency.substring(1);
            String walletName = currencyWithoutPrefix + " Wallet";
            if ("EUR".equals(currencyWithoutPrefix)) {
                accountList.add(new BasicAccount(walletName, value));
            } else {
                BigDecimal exchangeRate = conversionRates.get(currency);
                accountList.add(new CurrencyAccount(walletName, currencyWithoutPrefix, value, exchangeRate));
            }
        });
        return accountList;
    }

    private Map<String, BigDecimal> getConversionRatesToEur(Set<String> currencies) throws ApiException {
        Map<String, BigDecimal> conversionRates = new HashMap<>();
        conversionRates.put("ZEUR", BigDecimal.ONE);
        currencies = currencies.stream().filter(currency -> !"ZEUR".equals(currency)).collect(Collectors.toSet());

        if (currencies.isEmpty()) {
            return conversionRates;
        }

        try {
            Map<String, String> input = new HashMap<>();
            StringBuilder pairs = new StringBuilder();
            for (String currency : currencies) {
                pairs.append(currency).append("ZEUR,");
            }
            pairs = new StringBuilder(pairs.substring(0, pairs.length() - 1));

            input.put("pair", pairs.toString());
            JSONObject response = new JSONObject(api.queryPublic(edu.self.kraken.api.KrakenApi.Method.TICKER, input));
            JSONArray errors = response.getJSONArray("error");
            if (!errors.toList().isEmpty()) {
                throw new Exception("API Errors: " + errors.toList());
            }

            for (String currency : currencies) {
                if ("ZEUR".equals(currency)) {
                    continue;
                }

                JSONObject resultPair = response.getJSONObject("result").getJSONObject(currency + "ZEUR");
                BigDecimal askPrice = resultPair.getJSONArray("a").getBigDecimal(0);
                BigDecimal bidPrice = resultPair.getJSONArray("b").getBigDecimal(0);
                BigDecimal averagePrice = (askPrice.add(bidPrice)).divide(new BigDecimal(2.0f));
                conversionRates.put(currency, averagePrice);
            }

            return conversionRates;
        } catch (Exception e) {
            throw new ApiException("Can't retrieve Conversion Rates.", e);
        }
    }

    private Map<String, BigDecimal> getAssetValues() throws ApiException {
        try {
            Map<String, BigDecimal> assets = new HashMap<>();

            Map<String, String> input = new HashMap<>();
            input.put("asset", "");

            JSONObject response = new JSONObject(api.queryPrivate(edu.self.kraken.api.KrakenApi.Method.BALANCE, input));
            JSONArray errors = response.getJSONArray("error");
            if (!errors.toList().isEmpty()) {
                throw new Exception("API Errors: " + errors.toList());
            }

            JSONObject retrievedAssets = response.getJSONObject("result");
            retrievedAssets.keySet().forEach(currency -> {
                BigDecimal amount = retrievedAssets.getBigDecimal(currency);
                assets.put(currency, amount);
            });

            return assets;

        } catch (Exception e) {
            throw new ApiException("Can't retrieve Kraken Assets.", e);
        }
    }

}
