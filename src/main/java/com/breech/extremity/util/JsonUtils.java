package com.breech.extremity.util;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    public static List<String> extractImageUrls(JsonNode node) {
        List<String> urls = new ArrayList<>();
        traverse(node, urls);
        return urls;
    }

    private static void traverse(JsonNode node, List<String> urls) {
        if (node == null || node.isNull()) {
            return;
        }
        // 如果是一个对象节点，可能包含 type/image, attrs/src
        if (node.has("type") && "image".equals(node.get("type").asText())) {
            JsonNode attrs = node.get("attrs");
            if (attrs != null && attrs.has("src")) {
                urls.add(attrs.get("src").asText());
            }
        }
        // 如果有 content 数组，就继续递归
        JsonNode contentNode = node.get("content");
        if (contentNode != null && contentNode.isArray()) {
            for (JsonNode child : contentNode) {
                traverse(child, urls);
            }
        }
    }
}