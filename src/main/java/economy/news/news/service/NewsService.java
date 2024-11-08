package economy.news.news.service;

import economy.news.news.domain.News;

import java.util.List;

public interface NewsService {
    void newsCrawling();
    List<News> getNews();
}
