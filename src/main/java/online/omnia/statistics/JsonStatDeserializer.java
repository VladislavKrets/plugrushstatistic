package online.omnia.statistics;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lollipop on 03.04.2018.
 */
public class JsonStatDeserializer implements JsonDeserializer<List<CampaignStatEntity>>{
    @Override
    public List<CampaignStatEntity> deserialize(JsonElement jsonElement, Type type,
                                                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonArray array = jsonElement.getAsJsonObject().get("data").getAsJsonArray();
        List<CampaignStatEntity> campaignStatEntities = new ArrayList<>();
        CampaignStatEntity campaignStatEntity;
        for (JsonElement element : array) {
            campaignStatEntity = new CampaignStatEntity();
            campaignStatEntity.setCampaignId(element.getAsJsonObject().get("id").getAsInt());
            campaignStatEntity.setCpc(element.getAsJsonObject().get("costPerClick").getAsDouble());
            campaignStatEntity.setCost(element.getAsJsonObject().get("cost").getAsDouble());
            campaignStatEntity.setUniques(element.getAsJsonObject().get("uniques").getAsInt());
            campaignStatEntities.add(campaignStatEntity);
        }
        return campaignStatEntities;
    }
}
