package model.asset_checker.abstract_checker;

import model.ApiException;
import model.asset.Account;
import model.asset.AssetSource;
import model.asset.AssetSourceCredentials;
import model.asset_checker.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.List;

public abstract class AssetChecker {

    public abstract String getName();

    protected abstract List<Account> retrieveAccounts() throws ApiException;

    public AssetSource retrieveAssets() throws ApiException {
        return new AssetSource(getName(), retrieveAccounts());
    }

    protected static Executor getExecutor() {
        CookieStore cookieStore = new BasicCookieStore();
        HttpClient client = HttpClientBuilder.create().disableRedirectHandling().build();
        Executor executor = Executor.newInstance(client);
        executor.use(cookieStore);
        return executor;
    }

    public static AssetChecker createAssetChecker(AssetSourceCredentials assetSourceCredentials) {
        switch (assetSourceCredentials.getType()) {
            case "SPARKASSE_HANNOVER":
                return new SparkasseHannoverAssetChecker(assetSourceCredentials);
            case "ING_DIBA":
                return new INGDiBaAssetChecker(assetSourceCredentials);
            case "AUXMONEY":
                return new AuxmoneyAssetChecker(assetSourceCredentials);
            case "BITCOIN_DE":
                return new BitcoinDeAssetChecker(assetSourceCredentials);
            case "KRAKEN":
                return new KrakenAssetChecker(assetSourceCredentials);
            case "COINBASE":
                return new CoinbaseAssetChecker(assetSourceCredentials);
            case "PAYPAL":
                return new PayPalAssetChecker(assetSourceCredentials);
            case "AMAZON_VISA":
                return new AmazonVisaAssetChecker(assetSourceCredentials);
            case "ETHEREUM":
                return new EthereumAssetChecker(assetSourceCredentials);
            case "BITCOIN_CASH":
                return new BitcoinCashAssetChecker(assetSourceCredentials);
            case "EQUATE_PLUS":
                return new EquatePlusAssetChecker(assetSourceCredentials);
        }
        return null;
    }
}
