package me.moonsoo.travelerapplication.accompany;

import me.moonsoo.travelerapplication.account.AccountModel;
import me.moonsoo.travelerapplication.deserialize.CustomDeserializer;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class AccompanyDeserializer implements CustomDeserializer<AccompanyModel> {

    @Override
    public AccompanyModel deserializeModel(Object responseBody) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        LinkedHashMap<String, Object> content = (LinkedHashMap<String, Object>) responseBody;
        AccompanyModel resource = AccompanyModel.class.getDeclaredConstructor().newInstance();
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
                AccompanyModel.class.getDeclaredField("links").set(resource, resourceLinks);
            }
            else if(key.equals("account")) {
                LinkedHashMap<String, Object> accountInfo = (LinkedHashMap<String, Object>) content.get(key);
                AccountModel accountModel = new AccountModel();
                accountModel.setId((Integer) accountInfo.get("id"));
                accountModel.setNickname((String) accountInfo.get("nickname"));
                accountModel.setProfileImageUri((String) accountInfo.get("profileImageUri"));
                AccompanyModel.class.getDeclaredField((String)key).set(resource, accountModel);
            }
            else if(key.equals("regDate")) {
                AccompanyModel.class.getDeclaredField((String)key).set(resource, ZonedDateTime.parse((String) content.get(key)));
            }
            else if(((String)key).contains("Date")) {
                AccompanyModel.class.getDeclaredField((String)key).set(resource, LocalDateTime.parse((String) content.get(key)));
            }
            else {
                AccompanyModel.class.getDeclaredField((String) key).set(resource, content.get(key));
            }
        }
        return resource;
    }
}

