package me.moonsoo.travelerapplication.follow;

import me.moonsoo.travelerapplication.account.AccountModel;
import me.moonsoo.travelerapplication.deserialize.CustomDeserializer;
import me.moonsoo.travelerapplication.deserialize.CustomPagedModel;
import org.springframework.hateoas.Link;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class FollowDeserializer implements CustomDeserializer<AccountModel> {
    @Override
    public AccountModel deserializeModel(Object responseBody) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        LinkedHashMap<String, Object> content = (LinkedHashMap<String, Object>) responseBody;
        AccountModel resource = AccountModel.class.getDeclaredConstructor().newInstance();
        for (Object key : content.keySet()) {
            System.out.println("key: " + key + ", value: " + content.get(key));
            if (key.equals("_links")) {
                LinkedHashMap<String, LinkedHashMap<String, String>> links = (LinkedHashMap<String, LinkedHashMap<String, String>>) content.get(key);
                List<Link> resourceLinks = new ArrayList<>();//실제 리소스에 주입해줄 링크 리스트
                for (Object linkRelation : links.keySet()) {
                    LinkedHashMap<String, String> linkInfo = links.get(linkRelation);
                    String href = linkInfo.get("href");
                    Link link = new Link(href, (String) linkRelation);
                    resourceLinks.add(link);
                }
                AccountModel.class.getDeclaredField("links").set(resource, resourceLinks);
            }
            else {
                AccountModel.class.getDeclaredField((String) key).set(resource, content.get(key));
            }
        }
        return resource;
    }

    @Override
    public void deserializeResourceList(CustomPagedModel<AccountModel> customPagedModel, ArrayList<LinkedHashMap<Integer, AccountModel>> contentList) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
//        리소스 리스트 반복
        for (LinkedHashMap content : contentList) {
            AccountModel resource = deserializeModel(content);
            customPagedModel.getContent().add(resource);
        }
    }
}
