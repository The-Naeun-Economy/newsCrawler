package economy.news.news.controller;

import economy.news.news.domain.News;
import economy.news.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;

    @GetMapping("/crawling")
    public String newsCrawling() {
        newsService.newsCrawling();
        return "success";
    }
    @GetMapping
    public List<News> getNews() {
        return newsService.getNews();
    }
    @GetMapping("/ping")
    public String ping() {
        String url = "https://www.hankyung.com/all-news-economy";
        Document doc = null;

        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements newsHeadlines = Objects.requireNonNull(doc).select("div.news-item");
        return newsHeadlines.toString();
    }

    @GetMapping("img/news-images/{fileName}")
    public ResponseEntity<InputStreamResource> getImage(@PathVariable String fileName) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.IMAGE_JPEG) // 파일 타입에 따라 변경 (예: IMAGE_PNG)
                .body(newsService.imgSend(fileName));
    }
}

