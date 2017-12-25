package model;

import model.asset.Account;
import model.asset.AssetSource;
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

}