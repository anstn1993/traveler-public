package me.moonsoo.travelerapplication.deserialize;

import lombok.AllArgsConstructor;
import org.springframework.hateoas.Link;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

//rest api서버로부터 받은 리소스들을 deserialize하는 역할을 담당한다.
@AllArgsConstructor
public class CustomDeserializer<T> {
    //리소스 목록들의 정보에 대한 deserialize수행
    public CustomPagedModel<T> deseriazizePagedModel(Object responseBody, String contentFieldName, Class type) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        CustomPagedModel<T> customPagedModel = new CustomPagedModel<>();
        customPagedModel.setContent(new ArrayList<>());
        customPagedModel.setLinks(new ArrayList<>());
        customPagedModel.setPage(new Page());
        LinkedHashMap<String, LinkedHashMap> body = (LinkedHashMap<String, LinkedHashMap>) responseBody;
        ArrayList<LinkedHashMap<Integer, T>> contentList = body.get("_embedded") == null ? null : (ArrayList<LinkedHashMap<Integer, T>>) body.get("_embedded").get(contentFieldName);
        if (contentList != null) {
            deserializeResourceList(type, customPagedModel, contentList);//리소스 리스트 deserialize
        }

        LinkedHashMap<String, LinkedHashMap> links = (LinkedHashMap<String, LinkedHashMap>) body.get("_links");
        deserializeLinks(customPagedModel, links);//링크 정보들 deserialize
        LinkedHashMap<String, Integer> page = (LinkedHashMap<String, Integer>) body.get("page");
        customPagedModel.getPage().setSize(page.get("size"));
        deserializePageInfo(customPagedModel, page);//page정보 deserialize
        return customPagedModel;
    }

    private void deserializePageInfo(CustomPagedModel<T> customPagedModel, LinkedHashMap<String, Integer> page) {
        customPagedModel.getPage().setTotalElements(page.get("totalElements"));
        customPagedModel.getPage().setTotalPages(page.get("totalPages"));
        customPagedModel.getPage().setNumber(page.get("number"));
    }

    private void deserializeLinks(CustomPagedModel<T> customPagedModel, LinkedHashMap<String, LinkedHashMap> links) {
        for (String relation : links.keySet()) {
            LinkedHashMap<String, String> href = links.get(relation);
            Link link = new Link(href.get("href"), relation);
            customPagedModel.getLinks().add(link);
        }
    }

    private void deserializeResourceList(Class type, CustomPagedModel<T> customPagedModel, ArrayList<LinkedHashMap<Integer, T>> contentList) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        //리소스 리스트 반복
        for (LinkedHashMap content : contentList) {
            Object resource = type.getDeclaredConstructor().newInstance();//리소스 모델 객체 생성
            //리소스 멤벼 변수 deserialize
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
                    type.getDeclaredField("links").set(resource, resourceLinks);
                } else {
                    type.getDeclaredField((String) key).set(resource, content.get(key));
                }
            }
            customPagedModel.getContent().add((T) resource);
        }
    }
}
