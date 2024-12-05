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
import java.util.*;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {
    private final NewsRepository newsRepository;
    String url = "https://www.hankyung.com/all-news-economy";

    @Override
    public void newsCrawling() {
        List<News> headlines = new ArrayList<>();
        Document doc = null;

        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Elements newsHeadlines = Objects.requireNonNull(doc).select("h2.news-tit");
        List<Long> ids = new ArrayList<>();
        Map<Long, News> newsMap = new HashMap<>();

        // 크롤링된 뉴스 데이터 준비
        for (Element headline : newsHeadlines) {
            Element link = headline.selectFirst("a");
            String title = Objects.requireNonNull(link).text();
            String articleUrl = link.attr("href");
            Long id = Long.parseLong(articleUrl.substring(33, 45));

            ids.add(id);
            newsMap.put(id, new News(id, title, articleUrl));
        }

        // 데이터베이스에서 이미 존재하는 ID 확인
        List<Long> existingIds = newsRepository.findAllById(ids).stream()
                .map(News::getId)
                .toList();

        // 존재하지 않는 ID에 해당하는 뉴스만 저장
        for (Long id : ids) {
            if (!existingIds.contains(id)) {
                headlines.add(newsMap.get(id));
            }
        }

        newsRepository.saveAll(headlines);
    }


    @Override
    public List<News> getNews() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        Page<News> newsPage = newsRepository.findAll(pageable);
        return newsPage.getContent();
    }
}
