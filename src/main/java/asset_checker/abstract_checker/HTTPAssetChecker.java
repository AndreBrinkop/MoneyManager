package asset_checker.abstract_checker;

import model.ApiException;
import model.AssetChecker;
import model.AssetList;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;


public abstract class HTTPAssetChecker implements AssetChecker {

    protected String user;
    protected String password;

    protected Executor executor;

    public HTTPAssetChecker(String user, String password) {
        super();
        this.user = user;
        this.password = password;
        this.executor = getExecutor();
    }

    public abstract String getName();

    protected abstract void login() throws ApiException;

    protected abstract void logout() throws ApiException;

    protected abstract AssetList retrieveAssetsWithActiveSession() throws ApiException;

    @Override
    public AssetList retrieveAssets() throws ApiException {
        login();
        AssetList assets = retrieveAssetsWithActiveSession();
        logout();
        return assets;
    }

    private static Executor getExecutor() {
        CookieStore cookieStore = new BasicCookieStore();
        HttpClient client = HttpClientBuilder.create().disableRedirectHandling().build();
        Executor executor = Executor.newInstance(client);
        executor.use(cookieStore);
        return executor;
    }

}
