import database.DatabaseConnection;
import model.ApiException;
import model.asset.Account;
import model.asset.AssetSource;
import model.asset.AssetSourceCredentials;
import model.asset_checker.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import static util.NumberHelper.roundValue;

public class MoneyManager {

    public static void main(String[] args) throws Exception {
        DatabaseConnection databaseConnection = new DatabaseConnection(getEncryptionKey());
        List<AssetSourceCredentials> credentials = databaseConnection.getAssetSourceCredentials();
        AssetSource offlineAccounts = databaseConnection.getOfflineAccountAssetSource();
        List<AssetChecker> assetCheckers = createAssetCheckers(credentials);

        MoneyManager moneyManager = new MoneyManager();
        BigDecimal totalAmount = moneyManager.retrieveAssets(assetCheckers, offlineAccounts);

        System.out.println("--------------------");
        //System.out.println("Old Total: " + oldTotalAmount + " €");
        System.out.println("New Total: " + totalAmount + " €");
        //System.out.println("\t\t\t" + (delta.doubleValue() > 0.0 ? "+" : "") + delta + " €");
    }

    private static List<AssetChecker> createAssetCheckers(List<AssetSourceCredentials> credentials) {
        List<AssetChecker> assetCheckers = new LinkedList<>();
        for (AssetSourceCredentials credentialsEntry : credentials) {
            switch (credentialsEntry.getType()) {
                case "SPARKASSE_HANNOVER":
                    assetCheckers.add(new SparkasseHannoverAssetChecker(credentialsEntry));
                    break;
                case "ING_DIBA":
                    assetCheckers.add(new INGDiBaAssetChecker(credentialsEntry));
                    break;
                case "AUXMONEY":
                    assetCheckers.add(new AuxmoneyAssetChecker(credentialsEntry));
                    break;
                case "BITCOIN_DE":
                    assetCheckers.add(new BitcoinDeAssetChecker(credentialsEntry));
                    break;
                case "KRAKEN":
                    assetCheckers.add(new KrakenAssetChecker(credentialsEntry));
                    break;
                case "COINBASE":
                    assetCheckers.add(new CoinbaseAssetChecker(credentialsEntry));
                    break;
                case "PAYPAL":
                    assetCheckers.add(new PayPalAssetChecker(credentialsEntry));
                    break;
                case "AMAZON_VISA":
                    assetCheckers.add(new AmazonVisaAssetChecker(credentialsEntry));
                    break;
                case "ETHEREUM":
                    assetCheckers.add(new EthereumAssetChecker(credentialsEntry));
                    break;
                case "BITCOIN_CASH":
                    assetCheckers.add(new BitcoinCashAssetChecker(credentialsEntry));
                    break;
                case "EQUATE_PLUS":
                    assetCheckers.add(new EquatePlusAssetChecker(credentialsEntry));
            }
        }
        return assetCheckers;
    }

    public static String getEncryptionKey() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String encryptionKey = null;
        while (encryptionKey == null || encryptionKey.isEmpty()) {
            System.out.print("Enter Encryption Key: ");
            encryptionKey = bufferedReader.readLine();
        }
        System.out.println("Encryption Key Entered.");
        System.out.println();
        return encryptionKey;
    }

    private BigDecimal retrieveAssets(List<AssetChecker> assetCheckers, AssetSource offlineAccounts) throws IOException {
        List<AssetSource> assetSourceList = new LinkedList<>();

        for (AssetChecker checker : assetCheckers) {
            boolean done = false;
            while (!done) {
                System.out.println("Retrieving assets from: " + checker.getName());
                try {
                    assetSourceList.add(checker.retrieveAssets());
                    done = true;
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
        assetSourceList.add(offlineAccounts);

        System.out.println();

        filterOutAssetSources(assetSourceList, "KlassikSparen");
        filterOutAssetSources(assetSourceList, "BonusSparen");

        for (AssetSource assetSource : assetSourceList) {
            System.out.println(assetSource);
        }

        BigDecimal totalAmount = roundValue(assetSourceList.stream().map(AssetSource::getTotalEurValue).reduce(BigDecimal.ZERO, BigDecimal::add));
        return totalAmount;
    }

    private void filterOutAssetSources(List<AssetSource> assetSourceList, String accountNamePart) {
        for (AssetSource assetSource : assetSourceList) {
            if (assetSource == null || assetSource.getAccounts() == null) {
                continue;
            }
            for (Account account : assetSource.getAccounts()) {
                if (account.getName().contains(accountNamePart)) {
                    assetSource.removeAccount(account);
                }
            }
        }
    }

}
