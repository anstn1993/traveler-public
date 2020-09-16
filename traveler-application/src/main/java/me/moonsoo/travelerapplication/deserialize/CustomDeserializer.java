package me.moonsoo.travelerapplication.deserialize;

import org.springframework.hateoas.Link;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

//rest api서버로부터 받은 리소스들을 deserialize하는 역할을 담당한다.
public interface CustomDeserializer<T> {

    T deserializeModel(Object responseBody) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException;

    default void deserializeResourceList(CustomPagedModel<T> customPagedModel, ArrayList<LinkedHashMap<Integer, T>> contentList) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        //리소스 리스트 반복
        for (LinkedHashMap content : contentList) {
            T resource = deserializeModel(content);
            customPagedModel.getContent().add(resource);
        }
    }

    //리소스 목록들의 정보에 대한 deserialize수행
    default CustomPagedModel<T> deseriazizePagedModel(Object responseBody, String contentFieldName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        CustomPagedModel<T> customPagedModel = new CustomPagedModel<>();
        customPagedModel.setContent(new ArrayList<>());
        customPagedModel.setLinks(new ArrayList<>());
        customPagedModel.setPage(new Page());
        LinkedHashMap<String, LinkedHashMap> body = (LinkedHashMap<String, LinkedHashMap>) responseBody;
        ArrayList<LinkedHashMap<Integer, T>> contentList = body.get("_embedded") == null ? null : (ArrayList<LinkedHashMap<Integer, T>>) body.get("_embedded").get(contentFieldName);
        if (contentList != null) {
            deserializeResourceList(customPagedModel, contentList);//리소스 리스트 deserialize
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
}
