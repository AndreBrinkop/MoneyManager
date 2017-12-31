package model.asset_checker;

import model.ApiException;
import model.asset.AssetSourceCredentials;
import model.asset.account.Account;
import model.asset.account.BasicAccount;
import model.asset_checker.abstract_checker.HTTPAssetChecker;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


public class AuxmoneyAssetChecker extends HTTPAssetChecker {


    public AuxmoneyAssetChecker(AssetSourceCredentials credentials) {
        super(credentials);
    }

    @Override
    public String getName() {
        return "Auxmoney";
    }

    @Override
    protected List<Account> retrieveAssetsWithActiveSession() throws ApiException {
        List<Account> accountList = new LinkedList<>();
        accountList.add(new BasicAccount("Angebotskonto", retrieveInvestmentAccountBalance()));
        accountList.add(new BasicAccount("Gebundenes Kapital", retrieveFixedCapitalBalance()));
        return accountList;
    }

    private BigDecimal retrieveFixedCapitalBalance() throws ApiException {
        String auxmoneyPortfolioUrl = "https://www.auxmoney.com/anlegercockpit/portfolio";

        try {
            Document doc = Jsoup.parse(executor.execute(Request.Get(auxmoneyPortfolioUrl)).returnContent().asString());
            Optional<Element> rowElementOptional = doc.getElementsByTag("tr").stream().filter(element -> element.text().startsWith("Gebundenes Kapital")).findAny();
            if (rowElementOptional.isPresent()) {
                Element rowElement = rowElementOptional.get();
                String balanceString = rowElement.getElementsByTag("td").last().text();
                balanceString = balanceString.substring(0, balanceString.length() - 2).replaceAll(",", ".");
                return new BigDecimal(balanceString);
            }

            throw new ApiException("Could not retrieve assets.");
        } catch (Exception e) {
            throw new ApiException("Could not retrieve assets.", e);
        }
    }

    private BigDecimal retrieveInvestmentAccountBalance() throws ApiException {
        String auxmoneyInvestmentAccountUrl = "https://www.auxmoney.com/anlegercockpit/investaccount";

        try {
            Document doc = Jsoup.parse(executor.execute(Request.Get(auxmoneyInvestmentAccountUrl)).returnContent().asString());
            Element balanceElement = doc.getElementsByClass("uxTable--number").last();
            String balanceString = balanceElement.text();
            balanceString = balanceString.substring(4).replaceAll(",", ".");
            return new BigDecimal(balanceString);
        } catch (IOException e) {
            throw new ApiException("Could not retrieve assets.", e);
        }
    }

    protected void login() throws ApiException {
        executor.clearCookies();
        String auxmoneyLoginUrl = "https://www.auxmoney.com/login";
        try {

            Document doc = Jsoup.parse(executor.execute(Request.Get(auxmoneyLoginUrl)).returnContent().asString());
            String loginToken = doc.getElementById("login__token").val();
            // System.out.println("loginToken = " + loginToken);

            String auxmoneyLoginCheckUrl = "https://www.auxmoney.com/login_check";
            Response response = executor.execute(Request.Post(auxmoneyLoginCheckUrl).bodyForm(
                    new BasicNameValuePair("login[loginUsername]", credentials.getUser()),
                    new BasicNameValuePair("login[loginPassword]", credentials.getKey()),
                    new BasicNameValuePair("login[token]", loginToken)
            ));
            HttpResponse httpResponse = response.returnResponse();
            // System.out.println("httpResponse = " + httpResponse);

            String locationHeaderValue = httpResponse.getFirstHeader("Location").getValue();
            if (httpResponse.getStatusLine().getStatusCode() != 302 && !"https://www.auxmoney.com/anlegercockpit/".equals(locationHeaderValue)) {
                throw new ApiException("Could not login.");
            }
        } catch (IOException e) {
            throw new ApiException("Could not login.", e);
        }
    }

    protected void logout() throws ApiException {
        String auxMoneyLogutUrl = "https://www.auxmoney.com/logout";
        try {
            Response response = executor.execute(Request.Get(auxMoneyLogutUrl));
            HttpResponse httpResponse = response.returnResponse();
            String locationHeaderValue = httpResponse.getFirstHeader("Location").getValue();
            // System.out.println("httpResponse = " + httpResponse);

            if (httpResponse.getStatusLine().getStatusCode() != 301 || !"https://www.auxmoney.com/login?expired=0".equals(locationHeaderValue)) {
                throw new ApiException("Could not logout.");
            }

        } catch (IOException e) {
            throw new ApiException("Could not logout.", e);
        }
    }

}
