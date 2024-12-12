package economy.news.news.service;

import economy.news.news.domain.News;
import economy.news.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final S3Client s3Client;
    private final ResourceLoader resourceLoader;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;
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

        Elements newsHeadlines = Objects.requireNonNull(doc).select("div.news-item");
        List<Long> ids = new ArrayList<>();
        Map<Long, News> newsMap = new HashMap<>();

        // 크롤링된 뉴스 데이터 준비
        for (Element headline : newsHeadlines) {
            if(headline.selectFirst("div.thumb") == null){continue;}
            Element link = headline.selectFirst("a");
            String title = Objects.requireNonNull(link).text();
            String articleUrl = link.attr("href");
            Long id = Long.parseLong(articleUrl.substring(33, 45));
            Element p = headline.selectFirst("p");
            String time = Objects.requireNonNull(p).text();
            Element img = headline.selectFirst("img");
            String imgUrl = Objects.requireNonNull(img).attr("src");
            String s3Key = "news-images/" + id + ".jpg";
            uploadImageFromResource(id, imgUrl);
            ids.add(id);
            newsMap.put(id, new News(id, title, articleUrl, time ,s3Key));
        }

        List<Long> existingIds = newsRepository.findAllById(ids).stream()
                .map(News::getId)
                .toList();

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

    @Override
    public void uploadImageFromResource(Long id, String imgUrl) {
        try {
            String s3Key = "news-images/" + id + ".jpg";
            InputStream inputStream = resourceLoader.getResource(imgUrl).getInputStream();
            Path tempFile = Files.createTempFile("s3-upload-", ".tmp");
            try (FileOutputStream out = new FileOutputStream(tempFile.toFile())) {
                inputStream.transferTo(out);
            }
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.putObject(putObjectRequest, tempFile);
            Files.delete(tempFile);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image from URL to S3", e);
        }
    }

    @Override
    public InputStreamResource imgSend(String imgUrl) {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key("news-images/"+imgUrl)
                .build();

        InputStream imageStream = s3Client.getObject(getObjectRequest);
        return new InputStreamResource(imageStream);
    }
}
