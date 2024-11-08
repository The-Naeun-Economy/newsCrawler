## 주의사항 
MySQL 루트 비밀번호가 131072로 설정되어있으니

src/main/resources/application.yaml 에서 루트 비밀번호 수정 요망.

## 뉴스 크롤링 api
### GET http://localhost:8080/api/news/crawling
https://www.hankyung.com/all-news-economy 의 뉴스 제목과 url을 크롤링 후 MySQL에 저장

## 뉴스 받아오기
### GET http://localhost:8080/api/news
MySQL에 저장된 뉴스 최신순부터 10개를 받아온다. (추후 인피니티스크롤과 연동예정)
