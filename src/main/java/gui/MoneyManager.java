package gui;

import database.DatabaseConnection;
import model.ApiException;
import model.asset.AssetSource;
import model.asset.AssetSourceCredentials;
import model.asset.account.Account;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import static util.NumberHelper.roundValue;

public class MoneyManager {

    public static void main(String[] args) throws Exception {
        DatabaseConnection databaseConnection = new DatabaseConnection(getEncryptionKeyFromConsoleInput());
        List<AssetSourceCredentials> credentials = databaseConnection.getAssetSourceCredentials();
        AssetSource offlineAccounts = databaseConnection.getOfflineAccountAssetSource();

        MoneyManager moneyManager = new MoneyManager();
        List<AssetSource> assetSources = moneyManager.retrieveAssets(credentials, offlineAccounts);
        System.out.println();

        printAssets(assetSources);

        System.out.println();
        System.out.println("New:");

        for (int i = 0; i < assetSources.size(); i++) {
            AssetSource assetSource = assetSources.get(i);
            boolean done = false;
            while (!done) {
                System.out.println("Update assets from: " + assetSource.getName());
                try {
                    assetSource.updateAssets(credentials.size() > i ? credentials.get(i) : null);
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

        BigDecimal totalAmount = roundValue(assetSources.stream().map(assetSource -> assetSource.getCurrentEurBalance().getEuroBalanceValue()).reduce(BigDecimal.ZERO, BigDecimal::add));
        System.out.println("--------------------");
        //System.out.println("Old Total: " + oldTotalAmount + " €");
        System.out.println("Total: " + totalAmount + " €");
        //System.out.println("\t\t\t" + (delta.doubleValue() > 0.0 ? "+" : "") + delta + " €");
    }

    public static String getEncryptionKeyFromConsoleInput() throws IOException {
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

    public static List<AssetSource> retrieveAssets(List<AssetSourceCredentials> credentialsList, AssetSource offlineAccounts) throws IOException {
        List<AssetSource> assetSourceList = new LinkedList<>();

        for (int i = 0; i < credentialsList.size(); i++) {
            AssetSourceCredentials credentials = credentialsList.get(i);
            boolean done = false;
            while (!done) {
                AssetSource assetSource = new AssetSource(credentials);
                System.out.println("Retrieving assets from: " + assetSource.getName());
                try {
                    assetSource.updateAssets(credentialsList.get(i));
                    assetSourceList.add(assetSource);
                    done = true;
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            offlineAccounts.updateAssets(null);
        } catch (ApiException e) {
            e.printStackTrace();
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
