package asset_checker;

import asset_checker.abstract_checker.CryptoCurrencyAssetChecker;
import model.ApiException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class BitcoinAssetChecker extends CryptoCurrencyAssetChecker {

    public BitcoinAssetChecker(String address) {
        super(address);
    }

    public BitcoinAssetChecker(List<String> addressList) {
        super(addressList);
    }

    @Override
    protected int getSmallestUnitExponent() {
        return 8;
    }

    @Override
    protected String getCurrency() {
        return "BTC";
    }

    @Override
    public String getName() {
        return "Bitcoin Address";
    }

    @Override
    protected BigDecimal getAddressBalance(String address) throws ApiException {
        String url = "https://blockchain.info/q/addressbalance/" + address + "?confirmations=" + CONFIRMATIONS_NEEDED;
        Executor executor = getExecutor();
        try {
            Content content = executor.execute(Request.Get(url)).returnContent();
            BigDecimal satoshiBalance = new BigDecimal(content.asString());
            BigDecimal btcBalance = decimalsToWholeUnit(satoshiBalance);
            return btcBalance;
        } catch (IOException e) {
            throw new ApiException("Could not get balance for Bitcoin address: " + address, e);
        }
    }

    @Override
    protected List<BigDecimal> getAddressListBalances(List<String> addressList) throws ApiException {
        return null;
    }

}
