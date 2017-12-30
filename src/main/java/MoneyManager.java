import database.DatabaseConnection;
import model.ApiException;
import model.asset.AssetSource;
import model.asset.AssetSourceCredentials;
import model.asset.account.Account;
import model.asset_checker.abstract_checker.AssetChecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static util.NumberHelper.roundValue;

public class MoneyManager {

    public static void main(String[] args) throws Exception {
        DatabaseConnection databaseConnection = new DatabaseConnection(getEncryptionKey());
        List<AssetSourceCredentials> credentials = databaseConnection.getAssetSourceCredentials();
        AssetSource offlineAccounts = databaseConnection.getOfflineAccountAssetSource();
        List<AssetChecker> assetCheckers = createAssetCheckers(credentials);

        MoneyManager moneyManager = new MoneyManager();
        List<AssetSource> assetSources = moneyManager.retrieveAssets(assetCheckers, offlineAccounts);
        System.out.println();

        printAssets(assetSources);

        System.out.println();
        System.out.println("New:");

        for (AssetSource assetSource : assetSources) {
            boolean done = false;
            while (!done) {
                System.out.println("Update assets from: " + assetSource.getName());
                try {
                    assetSource.updateAssets();
                    done = true;
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println();

        printAssets(assetSources);

        System.out.println();
    }

    private static void printAssets(List<AssetSource> assetSources) {
        // TODO:
        filterOutAssetSources(assetSources, "KlassikSparen");
        filterOutAssetSources(assetSources, "BonusSparen");

        for (AssetSource assetSource : assetSources) {
            System.out.println(assetSource);
        }

        BigDecimal totalAmount = roundValue(assetSources.stream().map(AssetSource::getTotalEurValue).reduce(BigDecimal.ZERO, BigDecimal::add));
        System.out.println("--------------------");
        //System.out.println("Old Total: " + oldTotalAmount + " €");
        System.out.println("Total: " + totalAmount + " €");
        //System.out.println("\t\t\t" + (delta.doubleValue() > 0.0 ? "+" : "") + delta + " €");
    }

    private static List<AssetChecker> createAssetCheckers(List<AssetSourceCredentials> credentials) {
        return credentials.stream().map(AssetChecker::createAssetChecker).collect(Collectors.toList());
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

    private List<AssetSource> retrieveAssets(List<AssetChecker> assetCheckers, AssetSource offlineAccounts) throws IOException {
        List<AssetSource> assetSourceList = new LinkedList<>();

        for (AssetChecker checker : assetCheckers) {
            boolean done = false;
            while (!done) {
                System.out.println("Retrieving assets from: " + checker.getName());
                try {
                    assetSourceList.add(new AssetSource(checker));
                    done = true;
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
        assetSourceList.add(offlineAccounts);
        return assetSourceList;
    }

    private static void filterOutAssetSources(List<AssetSource> assetSourceList, String accountNamePart) {
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
