package asset_checker.abstract_checker;

import model.ApiException;
import model.Asset;
import model.AssetChecker;
import model.AssetList;
import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.GV_Result.GVRWPDepotList;
import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.HBCIPassportPinTan;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.BigDecimalValue;
import org.kapott.hbci.structures.Konto;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

public abstract class HBCIAssetChecker implements AssetChecker {

    protected String user;
    protected String password;

    public HBCIAssetChecker(String user, String password) {
        this.user = user;
        this.password = password;
    }

    @Override
    public abstract String getName();

    public abstract HBCIAssetCheckerPassport fillPassport(HBCIAssetCheckerPassport passport);

    private HBCIPassport createPassport() {
        HBCIAssetCheckerPassport passport = new HBCIAssetCheckerPassport();
        passport = fillPassport(passport);
        passport.setHBCIVersion("300");
        passport.setFilterType("Base64");
        passport.setUserId(user);
        passport.setPIN(password);
        passport.setPort(new Integer(443));
        passport.setCountry("DE");
        return passport;
    }

    @Override
    public AssetList retrieveAssets() throws ApiException {
        AssetList assetList = new AssetList(getName());
        HBCIPassport passport = null;
        HBCIHandler hbciHandle = null;

        try {
            Properties properties = new Properties();
            properties.setProperty("client.passport.default", "PinTan");
            properties.setProperty("default.hbciversion", "3.0");
            properties.setProperty("log.loglevel.default", "0");
            properties.setProperty("client.passport.PinTan.filename", "");
            properties.setProperty("client.passport.PinTan.init", "0");


            HBCIUtils.init(properties, new HBCICallbackConsole() {
                public void callback(HBCIPassport passport, int reason, String msg, int dataType, StringBuffer retData) {
                    if (reason == HBCICallback.CLOSE_CONNECTION || reason == HBCICallback.NEED_CONNECTION)
                        return;

                    System.out.println("Callback für folgendes Passport: " + passport.getClientData("init").toString() + ", reason=" + reason);
                    super.callback(passport, reason, msg, dataType, retData);
                }

                @Override
                protected PrintStream getOutStream() {
                    PrintStream devZero = new PrintStream(new ByteArrayOutputStream());
                    return devZero;
                }
            });

            passport = createPassport();
            //passport.clearBPD();

            String version = passport.getHBCIVersion();
            hbciHandle = new HBCIHandler((version.length() != 0) ? version : "plus", passport);

            Konto[] konten = passport.getAccounts();

            for (Konto konto : konten) {
                // System.out.println("konto = " + konto);
                if (konto.type.contains("Depot")) {
                    String depotDescription = konto.type + " " + konto.number;
                    Double depotValue = getDepotValue(passport, hbciHandle, konto).getValue().doubleValue();
                    assetList.add(new Asset(depotValue, depotDescription));
                } else {
                    String accountDescription = konto.type + " " + konto.number;
                    Double accountValue = getAccountValue(passport, hbciHandle, konto);
                    assetList.add(new Asset(accountValue, accountDescription));
                }
            }

        } catch (Exception e) {
            throw new ApiException("Could not retrieve assets.", e);
        } finally {
            if (hbciHandle != null) {
                hbciHandle.close();
            } else if (passport != null) {
                passport.close();
            }
        }

        return assetList;
    }

    private BigDecimalValue getDepotValue(HBCIPassport hbciPassport, HBCIHandler hbciHandle, Konto depot) throws ApiException {
        HBCIJob depotAssetsJob = hbciHandle.newJob("WPDepotList");
        depotAssetsJob.setParam("my", depot);
        depotAssetsJob.addToQueue();

        HBCIExecStatus ret = hbciHandle.execute();

        GVRWPDepotList result = (GVRWPDepotList) depotAssetsJob.getJobResult();

        if (result.isOK() && result.getEntries().length == 1) {
            //System.out.println(result.toString());
            return result.getEntries()[0].total;

        } else {
            String error = "";
            if (result.getJobStatus().getErrorString() != null && !result.getJobStatus().getErrorString().isEmpty()) {
                error = "Job-Error: " + result.getJobStatus().getErrorString();
            } else if (ret.getErrorString() != null && !ret.getErrorString().isEmpty()) {
                error = "Job-Error: " + ret.getErrorString();
            }
            throw new ApiException("Could not retrieve Depot value. " + error);
        }
    }

    private Double getAccountValue(HBCIPassport hbciPassport, HBCIHandler hbciHandle, Konto account) throws ApiException {
        HBCIJob auszug = hbciHandle.newJob("SaldoReq");
        auszug.setParam("my", account);

        auszug.addToQueue();
        HBCIExecStatus ret = hbciHandle.execute();

        GVRSaldoReq result = (GVRSaldoReq) auszug.getJobResult();

        if (result.isOK()) {
            Double bookedValue = Double.valueOf(result.getResultData().getProperty("content.booked.BTG.value"));
            String pendingValueProperty = result.getResultData().getProperty("content.pending.BTG.value");
            Double pendingValue = pendingValueProperty != null ? Double.valueOf(pendingValueProperty) : Double.MIN_VALUE;
            return Math.max(bookedValue, pendingValue);

        } else {
            String error = "";
            if (result.getJobStatus().getErrorString() != null && !result.getJobStatus().getErrorString().isEmpty()) {
                error = "Job-Error: " + result.getJobStatus().getErrorString();
            } else if (ret.getErrorString() != null && !ret.getErrorString().isEmpty()) {
                error = "Job-Error: " + ret.getErrorString();
            }
            throw new ApiException("Could not retrieve Depot value. " + error);
        }
    }

    protected class HBCIAssetCheckerPassport extends HBCIPassportPinTan {

        private HBCIAssetCheckerPassport(Object init, int dummy) {
            super(init);
        }

        public HBCIAssetCheckerPassport() {
            this("HBCIAssetChecker Passport", 0);
        }

        public void read() {
        }

        public void saveChanges() {
        }
    }

}

