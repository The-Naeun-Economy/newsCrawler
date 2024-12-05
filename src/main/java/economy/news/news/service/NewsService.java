package economy.news.news.service;

import economy.news.news.domain.News;

import java.io.IOException;
import java.util.List;

public interface NewsService {
    void newsCrawling();
    List<News> getNews();
}
