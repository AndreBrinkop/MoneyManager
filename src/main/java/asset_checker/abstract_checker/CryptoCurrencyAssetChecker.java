package asset_checker.abstract_checker;

import asset_checker.CoinbaseAssetChecker;
import com.google.common.math.LongMath;
import model.ApiException;
import model.AssetChecker;
import model.asset.Account;
import model.asset.CurrencyAccount;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;

public abstract class CryptoCurrencyAssetChecker extends AssetChecker {

    protected static final int CONFIRMATIONS_NEEDED = 6;

    protected abstract int getSmallestUnitExponent();

    protected abstract String getCurrency();

    private List<String> addressList = new LinkedList<>();

    public CryptoCurrencyAssetChecker(String address) {
        super();
        this.addressList.add(address);
    }

    public CryptoCurrencyAssetChecker(List<String> addressList) {
        super();
        this.addressList.addAll(addressList);
    }

    public List<Account> retrieveAccounts() throws ApiException {
        BigDecimal exchangeRateToEur = getExchangeRate();
        return retrieveAccounts(exchangeRateToEur);
    }

    private BigDecimal getExchangeRate() throws ApiException {
        return CoinbaseAssetChecker.retrieveExchangeRateToEuro(getExecutor(), getCurrency());
    }

    private List<Account> retrieveAccounts(BigDecimal exchangeRateToEur) throws ApiException {
        List<Account> accountList = new LinkedList<>();
        List<BigDecimal> addressListBalances = getAddressListBalances(this.addressList);
        if (addressListBalances == null || addressListBalances.size() != this.addressList.size()) {
            for (String address : addressList) {
                BigDecimal currencyValue = getAddressBalance(address);
                accountList.add(new CurrencyAccount(address, getCurrency(), currencyValue, currencyValue.multiply(exchangeRateToEur)));
            }
        } else {
            for (int i = 0; i < this.addressList.size(); i++) {
                BigDecimal currencyValue = addressListBalances.get(i);
                accountList.add(new CurrencyAccount(this.addressList.get(i), getCurrency(), currencyValue, currencyValue.multiply(exchangeRateToEur)));
            }
        }

        return accountList;
    }

    protected long getCoinValue() {
        return LongMath.pow(10, getSmallestUnitExponent());
    }

    protected BigDecimal decimalsToWholeUnit(BigDecimal decimalValue) {
        final BigDecimal decimalsPerCoinDecimal = new BigDecimal(getCoinValue(), new MathContext(0, RoundingMode.UNNECESSARY));
        return decimalValue.divide(decimalsPerCoinDecimal, getSmallestUnitExponent(), RoundingMode.UNNECESSARY);
    }

    protected abstract BigDecimal getAddressBalance(String address) throws ApiException;

    protected abstract List<BigDecimal> getAddressListBalances(List<String> addressList) throws ApiException;


}