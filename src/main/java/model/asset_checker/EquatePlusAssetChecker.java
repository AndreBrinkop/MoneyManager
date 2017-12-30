package model.asset_checker;

import model.ApiException;
import model.asset.AssetSourceCredentials;
import model.asset.account.Account;
import model.asset.account.Depot;
import model.asset.account.DepotPosition;
import model.asset_checker.abstract_checker.HTTPAssetChecker;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;

public class EquatePlusAssetChecker extends HTTPAssetChecker {


    public EquatePlusAssetChecker(AssetSourceCredentials credentials) {
        super(credentials);
    }

    @Override
    public String getName() {
        return "EquatePlus";
    }

    @Override
    public List<Account> retrieveAssetsWithActiveSession() throws ApiException {
        List<Account> accounts;
        try {
            Map<String, BigDecimal> sharePrices = retrieveSharePrices();
            accounts = retrieveAssets(sharePrices);
        } catch (Exception e) {
            throw new ApiException("Could not retrieve assets.", e);
        }
        return accounts;
    }

    private List<Account> retrieveAssets(Map<String, BigDecimal> sharePrices) throws IOException {
        List<Account> accountList = new LinkedList<>();
        String planSummaryUrl = "https://www.equateplus.com/EquatePlusParticipant2/services/planSummary/get";

        Response response = executor.execute(Request.Get(planSummaryUrl));
        Content content = response.returnContent();
        // System.out.println("content = " + content);

        JSONObject jsonObject = new JSONObject(content.toString());
        JSONArray planEntries = jsonObject.getJSONArray("entries");
        planEntries.forEach(planEntry -> {
            JSONArray summaryEntries = ((JSONObject) planEntry).getJSONArray("entries");
            summaryEntries.forEach(summaryEntry -> {
                String summaryType = ((JSONObject) summaryEntry).getString("type");
                if ("TOTAL".equals(summaryType)) {
                    BigDecimal amount = ((JSONObject) summaryEntry).getJSONObject("quantity").getBigDecimal("amount");
                    BigDecimal totalValue = ((JSONObject) summaryEntry).getJSONObject("value").getBigDecimal("amount");

                    String stockDescription;

                    if (sharePrices.keySet().size() == 1) {
                        stockDescription = sharePrices.keySet().stream().findAny().get();
                    } else {
                        BigDecimal calculatedValue = totalValue.divide(amount);
                        stockDescription = sharePrices.entrySet().stream().filter(v -> Objects.equals(v.getValue(), calculatedValue)).findAny().get().getKey();
                    }
                    accountList.add(new Depot("Depot", asList(new DepotPosition(stockDescription, amount, totalValue.divide(amount)))));
                }
            });
        });
        return accountList;
    }

    private Map<String, BigDecimal> retrieveSharePrices() throws IOException, ApiException {
        String sharePricesUrl = "https://www.equateplus.com/EquatePlusParticipant2/services/sharePrice/vehicles";
        Map<String, BigDecimal> sharePrices = new HashMap<>();

        Response response = executor.execute(Request.Get(sharePricesUrl));
        Content content = response.returnContent();
        // System.out.println("content = " + content);

        JSONObject jsonObject = new JSONObject(content.toString());
        JSONArray entries = jsonObject.getJSONArray("entries");
        entries.forEach(entry -> {
            String stockDescription = ((JSONObject) entry).getString("vehicleDescription");
            JSONObject currentPriceObject = ((JSONObject) entry).getJSONObject("currentPrice");
            BigDecimal stockPrice = currentPriceObject.getBigDecimal("amount");
            sharePrices.put(stockDescription, stockPrice);
        });

        if (sharePrices.isEmpty()) {
            throw new ApiException("Could not retrieve assets.");
        }
        return sharePrices;
    }

    protected void login() throws ApiException {
        String equateLoginUrl = "https://www.equateplus.com/EquatePlusParticipant/?login";
        executor.clearCookies();
        try {
            executor.execute(Request.Get(equateLoginUrl));

            Response response = executor.execute(Request.Post(equateLoginUrl).bodyForm(
                    new BasicNameValuePair("isiwebuserid", user),
                    new BasicNameValuePair("result", "Continue Login")
            ));
            HttpResponse httpResponse = response.returnResponse();
            // System.out.println("httpResponse = " + httpResponse);

            response = executor.execute(Request.Post(equateLoginUrl).bodyForm(
                    new BasicNameValuePair("isiwebuserid", user),
                    new BasicNameValuePair("isiwebpasswd", password),
                    new BasicNameValuePair("result", "Continue")
            ));
            httpResponse = response.returnResponse();
            // System.out.println("httpResponse = " + httpResponse);

            response = executor.execute(Request.Get("https://www.equateplus.com/EquatePlusParticipant/"));
            httpResponse = response.returnResponse();
            // System.out.println("httpResponse = " + httpResponse);

            String locationHeaderValue = httpResponse.getFirstHeader("Location").getValue();
            // System.out.println("locationHeaderValue = " + locationHeaderValue);
            if (httpResponse.getStatusLine().getStatusCode() != 302 || !"https://www.equateplus.com/EquatePlusParticipant2/start".equals(locationHeaderValue)) {
                throw new ApiException("Could not login.");
            }

        } catch (IOException e) {
            throw new ApiException("Could not login.", e);
        }
    }

    protected void logout() throws ApiException {
        String equateLogoutUrl = "https://www.equateplus.com/EquatePlusParticipant2/services/participant/?logout";
        try {
            Response response = executor.execute(Request.Get(equateLogoutUrl));
            HttpResponse httpResponse = response.returnResponse();
            // System.out.println("httpResponse = " + httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ApiException("Could not logout.");
            }
        } catch (IOException e) {
            throw new ApiException("Could not logout.", e);
        }

    }

}
