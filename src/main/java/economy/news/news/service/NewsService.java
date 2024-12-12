package economy.news.news.service;

import economy.news.news.domain.News;
import org.springframework.core.io.InputStreamResource;

import java.util.List;

public interface NewsService {
    void newsCrawling();
    List<News> getNews();
    void uploadImageFromResource(Long id, String imgUrl);
    InputStreamResource imgSend(String imgUrl);
}
