package economy.news.news.service;

import economy.news.news.domain.News;
import economy.news.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {
    private final NewsRepository newsRepository;
    String url = "https://www.hankyung.com/all-news-economy";

    @Override
    public void newsCrawling() {
        List<News> headlines = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();
            Elements newsHeadlines = doc.select("h2.news-tit");
            for (Element headline : newsHeadlines) {
                Element link = headline.selectFirst("a");
                if (link != null) {
                    String title = link.text();
                    String articleUrl = link.attr("href");
                    if (articleUrl.length() >= 45) {
                        try {
                            Long id = Long.parseLong(articleUrl.substring(33, 45));
                            if (!newsRepository.existsById(id)) {
                                headlines.add(new News(id, title, articleUrl));
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid ID format: " + e.getMessage());
                        }
                    }
                }
            }
            newsRepository.saveAll(headlines);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<News> getNews() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        Page<News> newsPage = newsRepository.findAll(pageable);
        return newsPage.getContent();
    }
}
