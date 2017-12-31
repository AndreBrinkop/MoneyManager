package model.asset_checker;

import model.ApiException;
import model.asset.AssetSourceCredentials;
import model.asset.account.Account;
import model.asset.account.CurrencyAccount;
import model.asset_checker.abstract_checker.HTTPAssetChecker;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BitcoinDeAssetChecker extends HTTPAssetChecker {

    private String logoutCsrfToken = "";

    public BitcoinDeAssetChecker(AssetSourceCredentials credentials) {
        super(credentials);
    }
    @Override
    public String getName() {
        return "Bitcoin.de";
    }

    @Override
    protected void login() throws ApiException {
        String bitcoinDeLoginUrl = "https://www.bitcoin.de/de/login";
        logoutCsrfToken = "";

        try {
            Response response = executor.execute(Request.Get(bitcoinDeLoginUrl));
            Content content = response.returnContent();

            Document doc = Jsoup.parse(content.asString());

            Element csrfTokenElement = doc.getElementById("login__csrf_token");
            String csrfToken = csrfTokenElement.val();

            Element redirectUrlElement = doc.getElementsByAttributeValue("name", "s_redirect_url").first();
            String redirectUrl = redirectUrlElement.val();

            response = executor.execute(Request.Post(bitcoinDeLoginUrl).bodyForm(
                    new BasicNameValuePair("login[_csrf_token]", csrfToken),
                    new BasicNameValuePair("login[username]", credentials.getUser()),
                    new BasicNameValuePair("login[password]", credentials.getKey()),
                    new BasicNameValuePair("b_redirect_to_dashboard", "1"),
                    new BasicNameValuePair("s_redirect_url", redirectUrl),
                    new BasicNameValuePair("submit", "Anmelden")
            ));
            HttpResponse httpResponse = response.returnResponse();
            String locationHeaderValue = httpResponse.getFirstHeader("Location").getValue();
            // System.out.println("locationHeaderValue = " + locationHeaderValue);
            if (httpResponse.getStatusLine().getStatusCode() != 302) {
                throw new ApiException("Could not login.");
            }
            if ("https://www.bitcoin.de/de/login_intermediate_captcha_form".equals(locationHeaderValue)) {
                throw new ApiException("Capture needed for login.");
            }

            String bitcoinDeTwoFactorUrl = "https://www.bitcoin.de/de/login_with_otp";
            response = executor.execute(Request.Get(bitcoinDeTwoFactorUrl));
            content = response.returnContent();

            doc = Jsoup.parse(content.asString());

            csrfTokenElement = doc.getElementById("readonly_login__csrf_token");
            csrfToken = csrfTokenElement.val();

            String bitcoinDeReadOnlyLoginUrl = "https://www.bitcoin.de/de/login_readonly";
            response = executor.execute(Request.Post(bitcoinDeReadOnlyLoginUrl).bodyForm(
                    new BasicNameValuePair("readonly_login[_csrf_token]", csrfToken),
                    new BasicNameValuePair("submit", "Anmelden im Nur-Lesen-Modus")
            ));
            httpResponse = response.returnResponse();

            if (httpResponse.getStatusLine().getStatusCode() != 302) {
                throw new ApiException("Could not login.");
            }
            locationHeaderValue = httpResponse.getFirstHeader("Location").getValue();
            if ("https://www.bitcoin.de/de/login_intermediate_captcha_form".equals(locationHeaderValue)) {
                throw new ApiException("Capture needed for login.");
            }
            if (!"https://www.bitcoin.de/de/dashboard".equals(locationHeaderValue)) {
                throw new ApiException("Could not login.");
            }

        } catch (Exception e) {
            throw new ApiException("Could not login.", e);
        }
    }

    @Override
    protected void logout() throws ApiException {
        String bitcoinDeLogutUrl = "https://www.bitcoin.de/de/logout";

        try {
            Response response = executor.execute(Request.Post(bitcoinDeLogutUrl).bodyForm(
                    new BasicNameValuePair("_csrf_token", logoutCsrfToken)
            ));
            HttpResponse httpResponse = response.returnResponse();

            if (httpResponse.getStatusLine().getStatusCode() != 302) {
                throw new ApiException("Could not Logout.");
            }

        } catch (Exception e) {
            throw new ApiException("Could not Logout.", e);
        }
    }

    @Override
    protected List<Account> retrieveAssetsWithActiveSession() throws ApiException {
        List<Account> accountList = new LinkedList<>();
        String bitcoinDeDashboardUrl = "https://www.bitcoin.de/de/dashboard";
        Response response;
        try {
            response = executor.execute(Request.Get(bitcoinDeDashboardUrl));
            Content content = response.returnContent();
            Document doc = Jsoup.parse(content.asString());

            Element logoutElement = doc.getElementsByAttributeValue("href", "/de/logout").first();
            String logoutActionJsString = logoutElement.attr("onclick");

            Pattern logoutCsrfTokenRegex = Pattern.compile("'value', '(.*)'");

            Matcher m = logoutCsrfTokenRegex.matcher(logoutActionJsString);
            m.find();
            logoutCsrfToken = m.group(1);

            Element assetTable = doc.getElementsByClass("bg2").first();
            Elements assetTableRows = assetTable.getElementsByTag("tr");
            // remove header and footer
            assetTableRows.remove(assetTableRows.first());
            assetTableRows.remove(assetTableRows.last());


            for (Element tableRow : assetTableRows) {
                Elements columnStrongElements = tableRow.getElementsByTag("strong");
                String currencyAmountString = columnStrongElements.get(1).text().trim();
                String currency = currencyAmountString.substring(currencyAmountString.indexOf(" ") + 1);
                String amountString = currencyAmountString.substring(0, currencyAmountString.indexOf(" "));
                String euroValueString = columnStrongElements.get(2).text();

                amountString = amountString.replaceAll(",", ".");
                euroValueString = euroValueString.replaceAll("\\.", "");
                euroValueString = euroValueString.replaceAll(",", ".");
                if (euroValueString.contains("€")) {
                    euroValueString = euroValueString.substring(0, euroValueString.length() - 2);
                }

                // Check if currency supported by Bitcoin.de
                if (!"---".equals(euroValueString) && !"-".equals(amountString)) {
                    BigDecimal amount = new BigDecimal(amountString);
                    BigDecimal euroValue = new BigDecimal(euroValueString);
                    if (amount.doubleValue() > 0.0) {
                        accountList.add(new CurrencyAccount(currency + " Wallet", currency, amount, euroValue.divide(amount, MathContext.DECIMAL128)));
                    }
                }
            }

        } catch (Exception e) {
            throw new ApiException("Could not retrieve assets.", e);
        }

        return accountList;
    }

}

