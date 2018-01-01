package model.asset_checker;

import model.ApiException;
import model.asset.AssetSourceCredentials;
import model.asset.account.Account;
import model.asset.account.BasicAccount;
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
import java.util.LinkedList;
import java.util.List;

public class AmazonVisaAssetChecker extends HTTPAssetChecker {

    private String sessionTicket;
    private String logoutUrl;

    @Override
    public String getName() {
        return "Amazon Visa Karte";
    }

    @Override
    protected void login(AssetSourceCredentials credentials) throws ApiException {
        String lbbLoginUrl = "https://kreditkarten-banking.lbb.de/Amazon/cas/";
        try {
            Response response = executor.execute(Request.Get(lbbLoginUrl + "dispatch.do?bt_PRELON=1&ref=1200_AMAZON&service=COS"));
            Content content = response.returnContent();

            Document doc = Jsoup.parse(content.asString());
            Element formElement = doc.getElementsByTag("form").first();
            Element tokenElement = doc.getElementsByAttributeValue("name", "org.apache.struts.taglib.html.TOKEN").first();
            String lbbLoginUrlWithSessionId = lbbLoginUrl + formElement.attr("action");
            String token = tokenElement.val();

            response = executor.execute(Request.Post(lbbLoginUrlWithSessionId).bodyForm(
                    new BasicNameValuePair("org.apache.struts.taglib.html.TOKEN", token),
                    new BasicNameValuePair("ref", "1200_AMAZON"),
                    new BasicNameValuePair("service2", "COS"),
                    new BasicNameValuePair("user", credentials.getUser()),
                    new BasicNameValuePair("password", credentials.getKey()),
                    new BasicNameValuePair("intred", "bt_REG"),
                    new BasicNameValuePair("postprocess", "PARAM_postprocess_NOT_FOUND"),
                    new BasicNameValuePair("registration", "false"),
                    new BasicNameValuePair("bt_LOGON", "Kreditkarten-Banking starten")
            ));
            content = response.returnContent();

            doc = Jsoup.parse(content.asString());
            Element ticketElement = doc.getElementsByTag("form").first().getElementsByAttributeValue("name", "ticket").first();
            this.sessionTicket = ticketElement.val();
        } catch (Exception e) {
            throw new ApiException("Could not login.", e);
        }

    }

    @Override
    protected void logout() throws ApiException {
        try {
            Response response = executor.execute(Request.Get(logoutUrl));
            HttpResponse httpResponse = response.returnResponse();
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ApiException("Could not logout.");
            }
        } catch (Exception e) {
            throw new ApiException("Could not logout.", e);
        }
    }

    @Override
    protected List<Account> retrieveAssetsWithActiveSession() throws ApiException {
        List<Account> accountList = new LinkedList<>();

        String lbbCosUrl = "https://kreditkarten-banking.lbb.de/lbb/cos_lbb/";
        String lbbPreLogonUrl = lbbCosUrl + "dispatch.do?bt_PRELOGON=-1";
        try {
            Response response = executor.execute(Request.Post(lbbPreLogonUrl).bodyForm(
                    new BasicNameValuePair("ticket", this.sessionTicket),
                    new BasicNameValuePair("clientCode", "9600"),
                    new BasicNameValuePair("ref", "1200_AMAZON"),
                    new BasicNameValuePair("registration", "false")
            ));
            Content content = response.returnContent();

            Document doc = Jsoup.parse(content.asString());

            Element logoutLinkElement = doc.getElementById("nav.logout");
            logoutUrl = lbbCosUrl + logoutLinkElement.attr("href");

            Elements trElements = doc.getElementsByTag("tr");
            for (Element trElement : trElements) {
                Elements tdElement = trElement.getElementsByTag("td");
                if (tdElement.first().text().equals("Kontostand:") && "tabtext".equals(tdElement.first().className())) {
                    String balanceString = tdElement.last().text();
                    balanceString = balanceString.substring("EUR  ".length(), balanceString.length());
                    balanceString = balanceString.replaceAll(",", ".");
                    BigDecimal balance = new BigDecimal(balanceString);
                    accountList.add(new BasicAccount(getName(), balance));
                }
            }

        } catch (Exception e) {
            throw new ApiException("Could not retrieve Assets.", e);
        }

        return accountList;
    }
}
