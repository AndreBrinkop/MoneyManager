package asset_checker;

import edu.self.kraken.api.KrakenApi;
import model.ApiException;
import model.AssetChecker;
import model.asset.Account;
import model.asset.CurrencyAccount;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class KrakenAssetChecker extends AssetChecker {

    private KrakenApi api;

    public KrakenAssetChecker(String apiKey, String apiSecret) {
        super();
        this.api = new edu.self.kraken.api.KrakenApi();
        this.api.setKey(apiKey);
        this.api.setSecret(apiSecret);
    }

    public String getName() {
        return "kraken.com";
    }

    public List<Account> retrieveAssets() throws ApiException {
        Map<String, Double> assets = getAssetValues();
        Map<String, Double> conversionRates = getConversionRatesToEur(assets.keySet());
        return createAssetObjects(assets, conversionRates);
    }

    private List<Account> createAssetObjects(Map<String, Double> assets, Map<String, Double> conversionRates) throws ApiException {
        List<Account> accountList = new LinkedList<>();
        assets.forEach((String currency, Double value) -> {
            Double eurValue = getEurValue(currency, value, conversionRates);
            accountList.add(new CurrencyAccount(getName(), currency, value, eurValue));
        });
        return accountList;
    }

    private Double getEurValue(String currency, Double value, Map<String, Double> conversionRates) {
        if ("ZEUR".equals(currency)) {
            return value;
        }
        Double conversionRate = conversionRates.get(currency);
        return value * conversionRate;
    }

    private Map<String, Double> getConversionRatesToEur(Set<String> currencies) throws ApiException {
        Map<String, Double> conversionRates = new HashMap<>();
        if (currencies.isEmpty()) {
            return conversionRates;
        }

        try {
            Map<String, String> input = new HashMap<>();
            StringBuilder pairs = new StringBuilder();
            for (String currency : currencies) {
                if ("ZEUR".equals(currency)) {
                    continue;
                }

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
                Double askPrice = resultPair.getJSONArray("a").getDouble(0);
                Double bidPrice = resultPair.getJSONArray("b").getDouble(0);
                Double averagePrice = (askPrice + bidPrice) / 2.0f;
                conversionRates.put(currency, averagePrice);
            }

            return conversionRates;
        } catch (Exception e) {
            throw new ApiException("Can't retrieve Conversion Rates.", e);
        }
    }

    private Map<String, Double> getAssetValues() throws ApiException {
        try {
            Map<String, Double> assets = new HashMap<>();

            Map<String, String> input = new HashMap<>();
            input.put("asset", "");

            JSONObject response = new JSONObject(api.queryPrivate(edu.self.kraken.api.KrakenApi.Method.BALANCE, input));
            JSONArray errors = response.getJSONArray("error");
            if (!errors.toList().isEmpty()) {
                throw new Exception("API Errors: " + errors.toList());
            }

            JSONObject retrievedAssets = response.getJSONObject("result");
            retrievedAssets.keySet().forEach(currency -> {
                Double amount = retrievedAssets.getDouble(currency);
                assets.put(currency, amount);
            });

            return assets;

        } catch (Exception e) {
            throw new ApiException("Can't retrieve Kraken Assets.", e);
        }
    }

}
