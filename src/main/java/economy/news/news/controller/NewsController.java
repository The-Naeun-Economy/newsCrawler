package economy.news.news.controller;

import economy.news.news.domain.News;
import economy.news.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
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
}
