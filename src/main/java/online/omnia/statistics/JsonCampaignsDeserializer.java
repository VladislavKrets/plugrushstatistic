package online.omnia.statistics;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lollipop on 03.04.2018.
 */
public class JsonCampaignsDeserializer implements JsonDeserializer<List<CampaignEntity>>{
    @Override
    public List<CampaignEntity> deserialize(JsonElement jsonElement, Type type,
                                            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonArray array = jsonElement.getAsJsonObject().get("data").getAsJsonArray();
        ArrayList<CampaignEntity> campaignEntities = new ArrayList<>();
        CampaignEntity campaignEntity;
        for (JsonElement element : array) {
            campaignEntity = new CampaignEntity();
            campaignEntity.setId(element.getAsJsonObject().get("id").getAsInt());
            campaignEntity.setTitle(element.getAsJsonObject().get("title").getAsString());
            campaignEntity.setUrl(element.getAsJsonObject().get("url").getAsString());
            campaignEntities.add(campaignEntity);
        }
        return campaignEntities;
    }
}
