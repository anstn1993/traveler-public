package me.moonsoo.travelerapplication.accompany.comment;


import me.moonsoo.travelerapplication.accompany.AccompanyModel;
import me.moonsoo.travelerapplication.account.AccountModel;
import me.moonsoo.travelerapplication.deserialize.CustomDeserializer;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class AccompanyCommentDeserializer implements CustomDeserializer<AccompanyCommentModel> {

    @Override
    public AccompanyCommentModel deserializeModel(Object responseBody) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        LinkedHashMap<String, Object> content = (LinkedHashMap<String, Object>) responseBody;
        AccompanyCommentModel resource = AccompanyCommentModel.class.getDeclaredConstructor().newInstance();
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
                AccompanyCommentModel.class.getDeclaredField("links").set(resource, resourceLinks);
            }
            else if(key.equals("account")) {
                LinkedHashMap<String, Object> accountInfo = (LinkedHashMap<String, Object>) content.get(key);
                AccountModel accountModel = new AccountModel();
                accountModel.setId((Integer) accountInfo.get("id"));
                accountModel.setNickname((String) accountInfo.get("nickname"));
                accountModel.setProfileImageUri((String) accountInfo.get("profileImageUri"));
                AccompanyCommentModel.class.getDeclaredField((String)key).set(resource, accountModel);
            }
            else if(key.equals("accompany")) {
                LinkedHashMap<String, Object> accompanyInfo = (LinkedHashMap<String, Object>) content.get(key);
                AccompanyModel accompanyModel = new AccompanyModel();
                accompanyModel.setId((Integer) accompanyInfo.get("id"));
                AccompanyCommentModel.class.getDeclaredField((String)key).set(resource, accompanyModel);
            }
            else if(key.equals("regDate")) {
                AccompanyCommentModel.class.getDeclaredField((String)key).set(resource, ZonedDateTime.parse((String) content.get(key)));
            }
            else {
                AccompanyCommentModel.class.getDeclaredField((String) key).set(resource, content.get(key));
            }
        }
        return resource;
    }
}
