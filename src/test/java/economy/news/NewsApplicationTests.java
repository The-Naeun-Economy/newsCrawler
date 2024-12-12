package economy.news;

import economy.news.news.domain.News;
import economy.news.news.repository.NewsRepository;
import economy.news.news.schedule.ScheduledTasks;
import economy.news.news.service.NewsService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.MySQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class NewsApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    private final NewsRepository newsRepository;
    private final NewsService newsService;
    private final ScheduledTasks scheduledTasks;

    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("news")
            .withUsername("testuser")
            .withPassword("testpassword");

    static {
        mysqlContainer.start();
        System.setProperty("DB_URL", mysqlContainer.getJdbcUrl());
        System.setProperty("DB_USERNAME", mysqlContainer.getUsername());
        System.setProperty("DB_PASSWORD", mysqlContainer.getPassword());
    }

    @Autowired
    public NewsApplicationTests(NewsRepository newsRepository, NewsService newsService, ScheduledTasks scheduledTasks) {
        this.newsRepository = newsRepository;
        this.newsService = newsService;
        this.scheduledTasks = scheduledTasks;
    }

    @Test
    void testRepository() {
        newsRepository.deleteAll();
        News news = new News(
                202412031208L,
                "호프만에이전시코리아, 2024 ‘올해의 PR에이전시’ 은상 수상",
                "https://www.hankyung.com/article/202412031208i",
                "2024.12.05 17:35",
                "news-images/202412055717.jpg"
                );

        News news1 = newsRepository.save(news);
        List<News> newss = newsRepository.findAll();

        assertEquals(news.getId(), news1.getId());
        assertEquals(1, newss.size());
    }

    @Test
    void testServiceGetNews() {
        newsRepository.deleteAll();
        News news1 = new News(
                202412031208L,
                "호프만에이전시코리아, 2024 ‘올해의 PR에이전시’ 은상 수상",
                "https://www.hankyung.com/article/202412031208i",
                "2024.12.05 17:35",
                "news-images/202412055717.jpg"
        );
        News news2 = new News(
                202412031209L,
                "호프만에이전시코리아, 2024 ‘올해의 PR에이전시’ 금상 수상",
                "https://www.hankyung.com/article/202412031208i",
                "2024.12.05 17:35",
                "news-images/202412055717.jpg"
        );
        newsRepository.save(news1);
        newsRepository.save(news2);

        List<News> news = newsService.getNews();

        assertEquals(202412031208L, news.get(1).getId());
        assertEquals("호프만에이전시코리아, 2024 ‘올해의 PR에이전시’ 금상 수상", news.get(0).getTitle());
        assertEquals(2, news.size());
    }

    @Test
    void testServiceNewsCrawling() {
        newsRepository.deleteAll();
        newsService.newsCrawling();

        List<News> news = newsRepository.findAll();

        assertNotEquals(0, news.size());
        assertNotEquals(null, news.get(0).getId());
        assertNotEquals(null, news.get(1).getTitle());
        assertNotEquals(null, news.get(2).getUrl());
    }

    @Test
    void testSchedule() {

        scheduledTasks.runAtEveryHour();

        List<News> news = newsRepository.findAll();

        assertNotEquals(0, news.size());
        assertNotEquals(null, news.get(0).getId());
        assertNotEquals(null, news.get(1).getTitle());
        assertNotEquals(null, news.get(2).getUrl());
    }

    @Test
    void testControllerNewsCrawling() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/news/crawling", String.class);

        assertThat(response.getBody()).isEqualTo("success");
    }
}
