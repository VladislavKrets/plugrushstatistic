package online.omnia.statistics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.digest.DigestUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by lollipop on 02.04.2018.
 */
public class Main {
    //a1fabb82300eb5d0a96e01b04777369eebd1826386d4d93bfa76edc6ac62
    //admin@omni-a.com
    public static int days;
    public static long deltaTime = 24 * 60 * 60 * 1000;

    public static void main(String[] args) {
        if (args.length != 1) {
            return;
        }
        if (!args[0].matches("\\d+")) return;
        if (Integer.parseInt(args[0]) == 0) {
            deltaTime = 0;
        }
        days = Integer.parseInt(args[0]);

        List<AccountsEntity> accountsEntities = MySQLDaoImpl.getInstance().getAccountsEntities("plugrush");
        long currentSeconds = System.currentTimeMillis() / 1000;
        String email = "admin@omni-a.com";
        String token = "a1fabb82300eb5d0a96e01b04777369eebd1826386d4d93bfa76edc6ac62";
        System.out.println(currentSeconds);

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        List<CampaignStatEntity> campaignStatEntities;
        List<CampaignEntity> campaignEntities;
        String sha256hex;
        SourceStatisticsEntity sourceStatisticsEntity;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, String> parameters;
        int afid;
        SourceStatisticsEntity entity;
        for (AccountsEntity accountsEntity : accountsEntities) {
            token = accountsEntity.getApiKey();
            email = accountsEntity.getUsername();
            sha256hex = DigestUtils.sha256Hex(token + "|" + currentSeconds + "|" + email);
            System.out.println(sha256hex);
            String answer = HttpMethodUtils.getMethod("https://admin.plugrush.com/api/v2/campaigns?" + "&email="
                    + email + "&timestamp=" + currentSeconds + "&hash=" + sha256hex);
            System.out.println(answer);
            gsonBuilder.registerTypeAdapter(List.class, new JsonCampaignsDeserializer());
            gson = gsonBuilder.create();
            campaignEntities = gson.fromJson(answer, List.class);
            gsonBuilder.registerTypeAdapter(List.class, new JsonStatDeserializer());
            gson = gsonBuilder.create();

            for (int i = 0; i <= days; i++) {
                answer = HttpMethodUtils.getMethod("https://admin.plugrush.com/api/v2/stats/advertiser/campaigns?fromDate="
                        + simpleDateFormat.format(new Date(System.currentTimeMillis() - deltaTime - i * 24L * 60 * 60 * 1000))
                        + "&toDate="
                        + simpleDateFormat.format(new Date(System.currentTimeMillis() - deltaTime - i * 24L * 60 * 60 * 1000))
                        + "&email="
                        + email + "&timestamp=" + currentSeconds + "&hash=" + sha256hex);
                try {
                    campaignStatEntities = gson.fromJson(answer, List.class);
                }
                catch (Exception e) {
                    continue;
                }
                for (CampaignStatEntity campaignStatEntity : campaignStatEntities) {
                    for (CampaignEntity campaignEntity : campaignEntities) {
                        if (campaignEntity.getId() == campaignStatEntity.getCampaignId()) {
                            sourceStatisticsEntity = new SourceStatisticsEntity();
                            sourceStatisticsEntity.setCampaignId(String.valueOf(campaignEntity.getId()));
                            sourceStatisticsEntity.setCampaignName(campaignEntity.getTitle());
                            sourceStatisticsEntity.setCpc(campaignStatEntity.getCpc());
                            sourceStatisticsEntity.setSpent(campaignStatEntity.getCost());
                            sourceStatisticsEntity.setReceiver("API");
                            sourceStatisticsEntity.setAccount_id(accountsEntity.getAccountId());
                            sourceStatisticsEntity.setBuyerId(accountsEntity.getBuyerId());
                            sourceStatisticsEntity.setClicks(campaignStatEntity.getUniques());
                            sourceStatisticsEntity.setDate(new java.sql.Date(System.currentTimeMillis() - deltaTime - i * 24L * 60 * 60 * 1000));
                            parameters = Utils.getUrlParameters(campaignEntity.getUrl());
                            if (parameters.containsKey("cab")) {
                                if (parameters.get("cab").matches("\\d+")
                                        && MySQLDaoImpl.getInstance().getAffiliateByAfid(Integer.parseInt(parameters.get("cab"))) != null) {
                                    afid = Integer.parseInt(parameters.get("cab"));
                                } else {
                                    afid = 0;
                                }
                            } else afid = 2;
                            sourceStatisticsEntity.setAfid(afid);
                            if (Main.days != 0) {
                                entity = MySQLDaoImpl.getInstance().getSourceStatistics(sourceStatisticsEntity.getAccount_id(),
                                        sourceStatisticsEntity.getCampaignName(), sourceStatisticsEntity.getDate());
                                if (entity != null) {
                                    sourceStatisticsEntity.setId(entity.getId());
                                    MySQLDaoImpl.getInstance().updateSourceStatistics(sourceStatisticsEntity);
                                    System.out.println(sourceStatisticsEntity);
                                    entity = null;
                                } else MySQLDaoImpl.getInstance().addSourceStatistics(sourceStatisticsEntity);

                                System.out.println(sourceStatisticsEntity);

                            } else {
                                if (MySQLDaoImpl.getInstance().isDateInTodayAdsets(new java.util.Date(sourceStatisticsEntity.getDate().getTime()), sourceStatisticsEntity.getAccount_id(),
                                        sourceStatisticsEntity.getCampaignId()) != null) {
                                    MySQLDaoImpl.getInstance().updateTodayAdset(Utils.getAdset(sourceStatisticsEntity));
                                } else MySQLDaoImpl.getInstance().addTodayAdset(Utils.getAdset(sourceStatisticsEntity));

                            }
                            break;
                        }
                    }
                }

                System.out.println(answer);
            }
        }
        MySQLDaoImpl.getSessionFactory().close();
    }
}
