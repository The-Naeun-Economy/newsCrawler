package economy.news.news.schedule;

import economy.news.news.repository.NewsRepository;
import economy.news.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final NewsService newsService;

    @Scheduled(cron = "0 0 * * * ?") // 매시 정각에 실행
    public void runAtEveryHour() {
        System.out.println("뉴스 크롤링 실행");
        newsService.newsCrawling();
    }
}
