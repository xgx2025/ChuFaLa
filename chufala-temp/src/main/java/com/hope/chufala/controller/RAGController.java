package com.hope.chufala.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/rag")
public class RAGController {
    private final VectorStore vectorStore;

    @RequestMapping("/test")
    public String test(){
        List<Document> documents = List.of(
                new Document("青石镇坐落在连绵的青山褶皱里，镇口的老槐树盘根错节，枝桠伸展得像老人枯瘦却温暖的手掌，遮住了大半个街口。镇上的时光好像比别处走得慢，青石板路被岁月磨得发亮，雨后会透出淡淡的苔痕，家家户户的木门扉上，都刻着藏着故事的纹路。", Map.of("meta1", "meta1")),
                new Document("林默就是在这样一个雨后的清晨来到青石镇的，他背着简单的行囊，手里攥着一张泛黄的旧照片，照片上是个穿蓝布长衫的老人，手里捧着一块银质怀表，背景正是镇口的老槐树。"),
                new Document("“就是这块。”陈老爹拿起怀表，递给林默，“三十多年前，一个穿蓝布长衫的先生来我铺子里买东西，付账的时候不小心把这块怀表落在了柜台上。", Map.of("meta2", "meta2"))
        );
        vectorStore.add(documents);
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query("林默是谁？").topK(1).build());
        System.out.println(results);
        return "test";
    }
}
