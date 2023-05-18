package com.example.jump.service;

import com.example.jump.domain.MetaApi;
import com.example.jump.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.asm.Advice;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


@RequiredArgsConstructor
@Service    // 스프링의 서비스
public class MetaDataImpl implements MetaData {

    private final MetaRepository metaRepository;    // 메타정보 테이블에 접근

    public Page<MetaApi> getList(int page, String type) {   // 페이지로 분할해서 전체 조회

        Pageable pageable = null;    //  조회할 페이지 번호와 한 페이지에 보여줄 데이터의 개수를 객체로 저장
        try {
            pageable = PageRequest.of(page, 10);
        } catch (IllegalArgumentException e) {      // api 리스트가 비어있을 시에, 이전, 다음 버튼을 누르면 오류가 나므로 예외처리
            this.metaRepository.findAll();
        }
        return this.metaRepository.findAllByMetaType(pageable, type);
    }

    @Override
    public List<MetaApi> findAll(String type) {     // 전체 조회 (Json으로 출력하기 위해서)

        return this.metaRepository.findAllByMetaType(type);
    }

    public MetaApi getView(String id) {     // 상세
        Optional<MetaApi> ID = this.metaRepository.findById(id);
        return ID.isPresent() ? ID.get() : null;       // id에 해당하는 데이터가 있으면 불러옴
    }

    public void delete(String[] id) {       // 삭제
        int length = id.length;

        for (int i = 0; i < length; i++) {  // 삭제할 id 값들을 반복함.
            Optional<MetaApi> ID = this.metaRepository.findById(id[i]);
            if (ID.isPresent())  // 값이 있다면
                this.metaRepository.delete(ID.get());   //  해당 객체 삭제
        }
    }

    public void save(MetaApi meta) {   // 수정
        this.metaRepository.save(meta);
    }

    public void getApi(String type) {   // 해당 API를 저장 및 출력

        Map<String, String> map = new HashMap<>();
        map.put("보도자료", "http://apis.data.go.kr/1371000/pressReleaseService/pressReleaseList?serviceKey=OyfKMEU9NFp%2FBjVq6X4XzOKgG0iCkwCWtmQNFtDKPlfCOoqhQBo6DhgyLTsJxe5JNjyRns4f2IZ0DmneSFw0Xw%3D%3D");
        map.put("정책뉴스", "http://apis.data.go.kr/1371000/policyNewsService/policyNewsList?serviceKey=OyfKMEU9NFp%2FBjVq6X4XzOKgG0iCkwCWtmQNFtDKPlfCOoqhQBo6DhgyLTsJxe5JNjyRns4f2IZ0DmneSFw0Xw%3D%3D");
        map.put("포토", "http://apis.data.go.kr/1371000/photoNewsService/photoNewsList?serviceKey=OyfKMEU9NFp%2FBjVq6X4XzOKgG0iCkwCWtmQNFtDKPlfCOoqhQBo6DhgyLTsJxe5JNjyRns4f2IZ0DmneSFw0Xw%3D%3D");
        map.put("코트라", "http://openknowledge.kotra.or.kr/handle/2014.oak/");
        map.put("현행법령", "http://www.law.go.kr/DRF/lawSearch.do?OC=helena0809&target=law&type=XML&page=");

        // json 배열
        JSONArray jArray = null;

        // 데이터 매핑 후 value 리스트 ( Title ~ Right 칼럼들의 값 )
        final char quotes = '"';          // 매핑시 ""안에 "을 넣기 위해 선언
        final String org = "\"org\"";     // 매핑시 ""안에 \을 넣기 위해 선언

        if (type.equals("현행법령")) {
            try {
                for (int num = 1; num < 264; num++) {
                    URL url = new URL(map.get(type) + num);
                    // Http연결을 위한 객체 생성
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Content-type", "application/json");
                    urlConnection.setConnectTimeout(20000); //서버 연결 제한 시간
                    urlConnection.setReadTimeout(20000);    //읽기 제한 시간

                    // url에서 불러온 데이터를 InputStream -> InputStreamReader -> BufferedReader -> readLine()로 받아옴.
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                    StringBuilder result = new StringBuilder();
                    String re = null;
                    while ((re = br.readLine()) != null)// 받아올 값이 있으면
                        result.append(re);  //  StringBuffer 객체에 데이터 추가

                    br.close();
                    urlConnection.disconnect();

                    JSONObject jsonObject = XML.toJSONObject(result.toString());
                    JSONObject jsonObject2 = jsonObject.getJSONObject("LawSearch");
                    jsonObject.clear();
                    jArray = jsonObject2.getJSONArray("law");
                    jsonObject2.clear();
                    for (int i = 0; i < jArray.length(); i++) {  // for문을 통해 JsonArray의 수만큼 리스트 안의 객체를 분리

                        JSONObject item = (JSONObject) jArray.get(i);
                        // 원하는 항목명을 Parsing 해서 형식에 맞춰 변수에 저장
                        try {
                            final List<String> mappingValue = new ArrayList<>();
                            mappingValue.add("{" + org + ":" + quotes + item.get("법령명한글").toString() + quotes + "}"); // Title
                            if (item.get("법령약칭명").equals("")) {    // 값이 없으면
                                mappingValue.add(("{" + org + ":" + quotes + item.get("법령명한글").toString() + quotes + "}"));   // Subject
                            } else {
                                mappingValue.add(("{" + org + ":" + quotes + item.get("법령약칭명").toString() + quotes + "}"));   // Subject
                            }
                            mappingValue.add(("{" + quotes + "summary" + quotes + ":{" + org + ":" + quotes + item.get("법령명한글") + quotes + "}}")); // 설명
                            mappingValue.add("{" + org + ":" + quotes + item.get("소관부처명").toString() + quotes + "}");
                            mappingValue.add(("[{" + org + ":" + quotes + item.get("소관부처명") + quotes + "," + quotes + "role" + quotes + ":" + quotes + "author" + quotes + "}]"));
                            mappingValue.add("{" + quotes + "issued" + quotes + ":" + quotes + item.get("시행일자").toString() + quotes + "," + quotes + "created" + quotes + ":" + quotes + item.get("공포일자").toString() + quotes + "}");
                            mappingValue.add("{" + org + ":" + quotes + "ko" + quotes + "}");
                            mappingValue.add("{" + quotes + "site" + quotes + ":" + quotes + item.get("법령일련번호").toString() + quotes + "," + quotes + "url" + quotes + ":" + quotes + item.get("법령상세링크").toString() + quotes + "}");
                            mappingValue.add(("{" + org + ":" + quotes + quotes + "}"));
                            mappingValue.add("{" + quotes + "isPartOF" + quotes + ":" + quotes + item.get("제개정구분명").toString() + quotes + "}");
                            mappingValue.add(("{" + org + ":" + quotes + quotes + "}"));
                            mappingValue.add(("{" + org + ":" + quotes + quotes + "}"));

                            MetaApi meta = new MetaApi(item.get("법령ID").toString(), "", type,    // 생성자를 통한 객체 초기화
                                    mappingValue.get(0),
                                    mappingValue.get(1),
                                    mappingValue.get(2),
                                    mappingValue.get(3),
                                    mappingValue.get(4),
                                    mappingValue.get(5),
                                    mappingValue.get(6),
                                    mappingValue.get(7),
                                    mappingValue.get(8),
                                    mappingValue.get(9),
                                    mappingValue.get(10),
                                    mappingValue.get(11)
                            );
                            metaRepository.save(meta);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (type.equals("코트라")) {    // Open Access Korea Data (Web Crawling)
            int num = 0;
            String[] menu = {"Title", "Corporate Author", "Personal Author", "Issue Date", "Publisher", "Series/Report No.", "Description", "Table Of Contents", "ISBN", "Citation", "URI", "Language", "Subject", "Appears in Collections"};
            String[] key = new String[14];      // 정제 후 칼럼
            String[] value = new String[14];    // 정제 후 데이터
            String[] rawValue = new String[15]; // 정제 후 DB 형식에 맞는 데이터

            try {
                for (num = 2000; num < 3000; num++) {

                    final List<String> mappingValue = new ArrayList<>();
                    // Jsoup: 자바에서 html 파싱을 위한 라이브러리, url 접속을 위한 connection 객체를 반환
                    Document doc = null;
                    try {
                        doc = Jsoup.connect(map.get("코트라") + num).get();
                    } catch (Exception e) {
                        continue;
                    }
                    Elements[] column = new Elements[14];
                    Elements[] content = new Elements[14];
                    // html 파싱 후 칼럼과 내용을 담음.
                    try {
                        for (int i = 1; i < 15; i++) {
                            column[i] = doc.select("#con_h > div.item_dl1 > dl:nth-child(" + i + ") > dt");
                            content[i] = doc.select("#con_h > div.item_dl1 > dl:nth-child(" + i + ") > dd");
                        }
                    } catch (Exception e) {
                        continue;
                    }

                    for (int i = 0; i < 14; i++) {
                        key[i] = column[i].toString().replaceAll("<[^>]*>|&[^;]*;|\n", "").trim();
                        value[i] = content[i].toString().replaceAll("<[^>]*>|&[^;]*;|\n", "");
                    }
                    for (int i = 0; i < 14; i++) {      // 데이터의 순서가 다르므로 반복하면서 맞는 값을 찾아줌.
                        for (int j = 0; j < 14; j++) {
                            if (key[i].equals(menu[j])) {
                                rawValue[j] = value[i];
                                break;
                            }
                        }
                    }

                    try {

                        // 받아온 데이터에 {와 "를 붙이기 위한 로직들
                        mappingValue.add("{" + org + ":" + quotes + rawValue[0] + quotes + "}");

                        if (rawValue[12] != null) {
                            mappingValue.add("[{" + org + ":" + quotes + rawValue[12] + quotes + "}]");
                        } else {
                            mappingValue.add("[{" + org + ":" + quotes + rawValue[0] + quotes + "}]");
                        }
                        mappingValue.add(("{" + quotes + "toc" + quotes + ":{" + org + ":" + quotes + rawValue[7] + quotes + "}," + quotes + "summary" + quotes + ":{" + org + ":" + quotes + rawValue[6] + quotes + "}"));
                        mappingValue.add("{" + org + ":" + quotes + rawValue[4] + quotes + "}");
                        mappingValue.add(("[{" + "role" + quotes + ":" + quotes + "author" + quotes + "," + org + ":" + quotes + rawValue[2] + quotes + "," + quotes + "affiliation" + quotes + ":" + "[{" + org + ":" + quotes + rawValue[2] + quotes + "}]"));
                        mappingValue.add("{" + quotes + "issued" + quotes + ":" + quotes + rawValue[3] + quotes + "}");
                        mappingValue.add("{" + org + ":" + quotes + rawValue[11] + quotes + "}");
                        mappingValue.add("{" + quotes + "ibsn" + quotes + ":" + quotes + rawValue[8] + "," + "view:" + rawValue[10] + quotes + "}");
                        mappingValue.add(("{" + org + ":" + quotes + quotes + "}"));
                        mappingValue.add("{" + quotes + "citation" + quotes + ":[" + quotes + rawValue[9] + quotes + "]}");
                        mappingValue.add(("{" + org + ":" + quotes + quotes + "}"));
                        mappingValue.add(("{" + org + ":" + quotes + quotes + "}"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    MetaApi meta = new MetaApi(String.valueOf("co" + num), "", type,    // 생성자를 통한 객체 초기화
                            mappingValue.get(0), mappingValue.get(1),
                            mappingValue.get(2), mappingValue.get(3),
                            mappingValue.get(4), mappingValue.get(5),
                            mappingValue.get(6), mappingValue.get(7),
                            mappingValue.get(8), mappingValue.get(9),
                            mappingValue.get(10),
                            mappingValue.get(11)
                    );
                    metaRepository.save(meta);  // Entity에 Meta데이터를 저장한다.
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LocalDate now = LocalDate.of(2022, 8, 1);    // 현재 날짜
            LocalDate start = LocalDate.of(2020, 8, 1);    // 처음 시작 날짜
            LocalDate end = start;

            StringBuilder result = new StringBuilder();
            final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");       // 날짜 포멧
            HttpURLConnection urlConnection;
            BufferedReader br;
            final String mType = map.get(type);
            try {
                // map의 api유형으로 url값을 받아옴
                while (true) {

                    end = start.plusDays(2);

                    String startDate = start.format(yyyyMMdd);
                    String endDate = end.format(yyyyMMdd);

                    System.out.println(endDate);
                    URL url = new URL(mType + "&startDate=" + startDate + "&endDate=" + endDate);

                    // Http 연결을 위한 객체 생성
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Content-type", "application/json");
                    urlConnection.setConnectTimeout(10000); //서버 연결 제한 시간
                    urlConnection.setReadTimeout(10000);    //읽기 제한 시간

                    // url에서 불러온 데이터를 InputStream -> InputStreamReader -> BufferedReader -> readLine()로 받아옴.
                    br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                    String re = null;
                    while ((re = br.readLine()) != null)    // 받아올 값이 있으면
                        result.append(re);   //  StringBuffer 객체에 데이터 추가

                    br.close();
                    urlConnection.disconnect();

                    JSONObject jsonObject3;
                    try {
                        JSONObject jsonObject = XML.toJSONObject(result.toString());    // StringBuilder -> String으로 형 변환 후 XML데이터를 Json객체로 생성
                        JSONObject jsonObject2 = jsonObject.getJSONObject("response");  //  key값이 response인 jsonObject를 찾음
                        jsonObject.clear();
                        jsonObject3 = jsonObject2.getJSONObject("body");            // key값이 body인 jsonObject를 찾음
                        jsonObject2.clear();
                    } catch (JSONException e) {
                        System.out.println("해당 날짜의 데이터가 존재하지 않습니다.");
                        start = end.plusDays(1);
                        result.setLength(0);
                        continue;
                    }

                    jArray = (JSONArray) jsonObject3.get("NewsItem");  //  key값이 NewsItem인 객체들을 JSON 배열로 만듬
                    result.setLength(0);
                    jsonObject3.clear();
                    int length = jArray.length();
                    for (int i = 0; i < length; i++) {  // key값이 NewsItem인 객체들의 갯수만큼 반복

                        List<String> mappingValue = new ArrayList<>();
                        JSONObject item = (JSONObject) jArray.get(i);    // JsonArray의 i+1번째 객체를 얻어옴.
                        try {
                            // 받아온 데이터에 {와 "를 붙이기 위한 로직들
                            mappingValue.add("{" + org + ":" + quotes + item.get("Title").toString() + quotes + "}");

                            if (!item.get("SubTitle1").equals("")) {
                                mappingValue.add(("[{" + org + ":" + quotes + item.get("SubTitle1") + quotes + "}]"));
                            } else if (!item.get("SubTitle2").equals("")) {
                                mappingValue.add(("[{" + org + ":" + quotes + item.get("SubTitle2") + quotes + "}]"));
                            } else if (!item.get("SubTitle3").equals("")) {
                                mappingValue.add(("[{" + org + ":" + quotes + item.get("SubTitle3") + quotes + "}]"));
                            } else {
                                mappingValue.add(("[{" + org + ":" + quotes + item.get("Title") + quotes + "}]"));
                            }
                            mappingValue.add(("{" + quotes + "summary" + quotes + ":{" + org + ":" + quotes + item.get("DataContents") + quotes + "}"));
                            mappingValue.add("{" + org + ":" + quotes + item.get("MinisterCode").toString() + quotes + "}");
                            mappingValue.add(("[{" + org + ":" + quotes + item.get("MinisterCode") + quotes + "," + quotes + "role" + quotes + ":" + quotes + "author" + quotes + "}]"));

                            // 날짜 변환 로직
                            SimpleDateFormat dfFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");    // 파싱 전 형식
                            SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy-MM-dd"); // 파싱 후 형식
                            String strDate = item.get("ModifyDate").toString();   // jsonObject의 get메소드로 ModifyDate를 String으로 변환
                            String strDate2 = item.get("ApproveDate").toString(); // jsonObject의 get메소드로 ApproveDate을 String으로 변환
                            String dateTemp = "";
                            String dateTemp2 = "";

                            if (!strDate.equals("")) {    // ModifyDate가 값이 있으면 날짜 변환
                                Date formatDate = dfFormat.parse(strDate);  // 기존의 날짜 형식으로 Date객체 생성
                                dateTemp = newDtFormat.format(formatDate);  // 기존의 날짜 형식을 새로운 날짜 형식으로 변환
                            }
                            if (!strDate2.equals("")) {   // ApproveDate가 값이 있으면 날짜 변환
                                Date formatDate2 = dfFormat.parse(strDate2);    // 기존의 날짜 형식으로 Date객체 생성
                                dateTemp2 = newDtFormat.format(formatDate2);    // 기존의 날짜 형식을 새로운 날짜 형식으로 변환
                            }

                            mappingValue.add("{" + quotes + "modified" + quotes + ":" + quotes + dateTemp + "," + quotes + "available" + quotes + ":" + quotes + dateTemp2 + quotes + "}");
                            mappingValue.add("{" + quotes + "org" + quotes + ":" + quotes + "ko" + quotes + "}");
                            mappingValue.add("{" + quotes + "site" + quotes + ":" + quotes + item.get("NewsItemId").toString() + "," + "view:" + item.get("OriginalUrl").toString() + quotes + "}");
                            mappingValue.add(("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));

                            try {
                                mappingValue.add("{" + quotes + "related" + quotes + ":[" + quotes + item.get("FileName").toString() + quotes + "," + quotes + item.get("FileUrl").toString() + quotes + "]}");
                            } catch (JSONException e) {
                                mappingValue.add("{\"related\":\"\"}");     // FileName이나 FileUrl이 없는 경우에는
                            }

                            mappingValue.add(("{" + org + ":" + quotes + quotes + "}"));
                            mappingValue.add(("{" + org + ":" + quotes + quotes + "}"));

                            // 정규 표현식으로 태그 및 특수문자들 제거
                            for (int j = 0; j < 12; j++) {
                                mappingValue.set(j, mappingValue.get(j).replaceAll("<[^>]*>|&[^;]*;", ""));
                            }
                        } catch (JSONException e) {
                            continue;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        MetaApi meta = new MetaApi(item.get("NewsItemId").toString(), "", type,    // 생성자를 통한 객체 초기화
                                mappingValue.get(0),
                                mappingValue.get(1),
                                mappingValue.get(2),
                                mappingValue.get(3),
                                mappingValue.get(4),
                                mappingValue.get(5),
                                mappingValue.get(6),
                                mappingValue.get(7),
                                mappingValue.get(8),
                                mappingValue.get(9),
                                mappingValue.get(10),
                                mappingValue.get(11));
                        metaRepository.save(meta);  // Entity에 Meta데이터를 저장한다.
                    }
                    jArray.clear();
                    start = end.plusDays(1);
                    if (start.isAfter(now))
                        break;
                }
            } catch (SocketException e) {
                System.out.println("통신 오류입니다.");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void getApiUpdate(String type,String startDate,String endDate) {  // 증분 데이터 처리

        String uType = type.replace(" update","");

        Map<String, String> map = new HashMap<>();
        map.put("보도자료", "http://apis.data.go.kr/1371000/pressReleaseService/pressReleaseList?serviceKey=OyfKMEU9NFp%2FBjVq6X4XzOKgG0iCkwCWtmQNFtDKPlfCOoqhQBo6DhgyLTsJxe5JNjyRns4f2IZ0DmneSFw0Xw%3D%3D");
        map.put("정책뉴스", "http://apis.data.go.kr/1371000/policyNewsService/policyNewsList?serviceKey=OyfKMEU9NFp%2FBjVq6X4XzOKgG0iCkwCWtmQNFtDKPlfCOoqhQBo6DhgyLTsJxe5JNjyRns4f2IZ0DmneSFw0Xw%3D%3D");
        map.put("포토", "http://apis.data.go.kr/1371000/photoNewsService/photoNewsList?serviceKey=OyfKMEU9NFp%2FBjVq6X4XzOKgG0iCkwCWtmQNFtDKPlfCOoqhQBo6DhgyLTsJxe5JNjyRns4f2IZ0DmneSFw0Xw%3D%3D");
        JSONArray jArray = null;

        // 데이터 매핑 후 value 리스트 ( Title ~ Right 칼럼들의 값 )
        final char quotes = '"';          // 매핑시 ""안에 "을 넣기 위해 선언
        final String org = "\"org\"";     // 매핑시 ""안에 \을 넣기 위해 선언

        LocalDate start = LocalDate.of(Integer.parseInt(startDate.substring(0,4)),
                Integer.parseInt(startDate.substring(4,6)), Integer.parseInt(startDate.substring(6,8)));    // 검사 시작 날짜
        LocalDate end;  //  3일씩 끊어지는 종료날짜

        LocalDate checkEnd = LocalDate.of(Integer.parseInt(endDate.substring(0,4)),
                Integer.parseInt(endDate.substring(4,6)), Integer.parseInt(endDate.substring(6,8)));        // 검사 종료 날짜

        StringBuilder result = new StringBuilder();
        final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");       // 날짜 포멧
        HttpURLConnection urlConnection;
        BufferedReader br;
        final String mType = map.get(uType);
        try {
            // map의 api유형으로 url값을 받아옴
            while (!start.isAfter(checkEnd)) {  // 시작 날짜가 종료 날짜 이전일 경우에만 돌아감.

                end = start.plusDays(2);

                String s = start.format(yyyyMMdd);
                String d = end.format(yyyyMMdd);

                System.out.println(end);
                URL url = new URL(mType + "&startDate=" + s + "&endDate=" + d);

                // Http 연결을 위한 객체 생성
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-type", "application/json");
                urlConnection.setConnectTimeout(10000); //서버 연결 제한시간
                urlConnection.setReadTimeout(10000);    //읽기 제한시간

                // url에서 불러온 데이터를 InputStream -> InputStreamReader -> BufferedReader -> readLine()로 받아옴.
                br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String re = null;
                while ((re = br.readLine()) != null)    // 받아올 값이 있으면
                    result.append(re);   //  StringBuffer 객체에 데이터 추가

                br.close();
                urlConnection.disconnect();

                JSONObject jsonObject3;
                try {
                    JSONObject jsonObject = XML.toJSONObject(result.toString());    // StringBuilder -> String으로 형 변환 후 XML데이터를 Json객체로 생성
                    JSONObject jsonObject2 = jsonObject.getJSONObject("response");  //  key값이 response인 jsonObject를 찾음
                    jsonObject.clear();
                    jsonObject3 = jsonObject2.getJSONObject("body");            // key값이 body인 jsonObject를 찾음
                    jsonObject2.clear();
                } catch (JSONException e) {
                    System.out.println("해당 날짜 데이터의 response태그가 존재하지 않습니다.");
                    start = end.plusDays(1);
                    result.setLength(0);
                    continue;
                }

                jArray = (JSONArray) jsonObject3.get("NewsItem");  //  key값이 NewsItem인 객체들을 JSON 배열로 만듬
                result.setLength(0);
                jsonObject3.clear();
                int length = jArray.length();
                for (int i = 0; i < length; i++) {  // key값이 NewsItem인 객체들의 갯수만큼 반복

                    List<String> mappingValue = new ArrayList<>();
                    JSONObject item = (JSONObject) jArray.get(i);    // JsonArray의 i+1번째 객체를 얻어옴.
                    if (!item.get("ModifyDate").toString().equals(item.get("ApproveDate").toString())) {
                        try {
                            // 받아온 데이터에 {와 "를 붙이기 위한 로직들
                            mappingValue.add("{" + org + ":" + quotes + item.get("Title").toString() + quotes + "}");

                            if (!item.get("SubTitle1").equals("")) {
                                mappingValue.add(("[{" + org + ":" + quotes + item.get("SubTitle1") + quotes + "}]"));
                            } else if (!item.get("SubTitle2").equals("")) {
                                mappingValue.add(("[{" + org + ":" + quotes + item.get("SubTitle2") + quotes + "}]"));
                            } else if (!item.get("SubTitle3").equals("")) {
                                mappingValue.add(("[{" + org + ":" + quotes + item.get("SubTitle3") + quotes + "}]"));
                            } else {
                                mappingValue.add(("[{" + org + ":" + quotes + item.get("Title") + quotes + "}]"));
                            }
                            mappingValue.add(("{" + quotes + "summary" + quotes + ":{" + org + ":" + quotes + item.get("DataContents") + quotes + "}"));
                            mappingValue.add("{" + org + ":" + quotes + item.get("MinisterCode").toString() + quotes + "}");
                            mappingValue.add(("[{" + org + ":" + quotes + item.get("MinisterCode") + quotes + "," + quotes + "role" + quotes + ":" + quotes + "author" + quotes + "}]"));

                            // 날짜 변환 로직
                            SimpleDateFormat dfFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");    // 파싱 전 형식
                            SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy-MM-dd"); // 파싱 후 형식
                            String strDate = item.get("ModifyDate").toString();   // jsonObject의 get메소드로 ModifyDate를 String으로 변환
                            String strDate2 = item.get("ApproveDate").toString(); // jsonObject의 get메소드로 ApproveDate을 String으로 변환
                            String dateTemp = "";
                            String dateTemp2 = "";

                            if (!strDate.equals("")) {    // ModifyDate가 값이 있으면 날짜 변환
                                Date formatDate = dfFormat.parse(strDate);  // 기존의 날짜 형식으로 Date객체 생성
                                dateTemp = newDtFormat.format(formatDate);  // 기존의 날짜 형식을 새로운 날짜 형식으로 변환
                            }
                            if (!strDate2.equals("")) {   // ApproveDate가 값이 있으면 날짜 변환
                                Date formatDate2 = dfFormat.parse(strDate2);    // 기존의 날짜 형식으로 Date객체 생성
                                dateTemp2 = newDtFormat.format(formatDate2);    // 기존의 날짜 형식을 새로운 날짜 형식으로 변환
                            }

                            mappingValue.add("{" + quotes + "modified" + quotes + ":" + quotes + dateTemp + "," + quotes + "available" + quotes + ":" + quotes + dateTemp2 + quotes + "}");
                            mappingValue.add("{" + quotes + "org" + quotes + ":" + quotes + "ko" + quotes + "}");
                            mappingValue.add("{" + quotes + "site" + quotes + ":" + quotes + item.get("NewsItemId").toString() + "," + "view:" + item.get("OriginalUrl").toString() + quotes + "}");
                            mappingValue.add(("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));

                            try {
                                mappingValue.add("{" + quotes + "related" + quotes + ":[" + quotes + item.get("FileName").toString() + quotes + "," + quotes + item.get("FileUrl").toString() + quotes + "]}");
                            } catch (JSONException e) {
                                mappingValue.add("{\"related\":\"\"}");     // FileName이나 FileUrl이 없는 경우에는
                            }

                            mappingValue.add(("{" + org + ":" + quotes + quotes + "}"));
                            mappingValue.add(("{" + org + ":" + quotes + quotes + "}"));

                            // 정규 표현식으로 태그 및 특수문자들 제거
                            for (int j = 0; j < 12; j++) {
                                mappingValue.set(j, mappingValue.get(j).replaceAll("<[^>]*>|&[^;]*;", ""));
                            }
                        } catch (JSONException e) {
                            continue;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        MetaApi meta = new MetaApi(item.get("NewsItemId").toString(), "", type,    // 생성자를 통한 객체 초기화
                                mappingValue.get(0),
                                mappingValue.get(1),
                                mappingValue.get(2),
                                mappingValue.get(3),
                                mappingValue.get(4),
                                mappingValue.get(5),
                                mappingValue.get(6),
                                mappingValue.get(7),
                                mappingValue.get(8),
                                mappingValue.get(9),
                                mappingValue.get(10),
                                mappingValue.get(11));
                        metaRepository.save(meta);  // Entity에 Meta데이터를 저장한다.
                    }
                    start = end.plusDays(1);
                }
                jArray.clear();
            }
        } catch (SocketException e) {
            System.out.println("통신 오류입니다.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

}

    public ResponseEntity<byte[]> saveCsv(String type) {    // CSV로 저장

        List<MetaApi> meta = metaRepository.findAllByMetaType(type);     // 유형에 해당하는 데이터를 받아옴

        String[] menu = {"Title", "Subject", "Description", "Publisher", "Contributors", "Date",
                "Language", "Identifier", "Format", "Relation", "Coverage", "Right"};   // CSV의 Header로 사용할 column들

        byte[] csvFile = null;          // csv 데이터를 담을 배열
        CSVPrinter csvPrinter = null;   // csv 형식의 값을 출력
        StringWriter sw = new StringWriter();   // 문자열 writer 객체 생성

        try {
            csvPrinter = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader(menu));         // csv 헤더 생성
            for (MetaApi m : meta) {
                List<String> data = Arrays.asList(m.getMetaTitle(), m.getMetaSubjects(), // csv 파일에 추가하고 싶은 데이터를 임의로 대입
                        m.getMetaDescription(), m.getMetaPublisher(),
                        m.getMetaContributor(), m.getMetaDate(),
                        m.getMetaLanguage(), m.getMetaIdentifier(),
                        m.getMetaFormat(), m.getMetaRelation(),
                        m.getMetaCoverage(), m.getMetaRight()
                );
                csvPrinter.printRecord(data);   // 실제 데이터 넣기
            }
            sw.flush();
            csvFile = sw.toString().getBytes("ms949");  // ms949로 받아옴.

            // csv파일 return
            HttpHeaders header = new HttpHeaders();
            header.add("Content-Type", "text/csv;charset=ms949");    // 파일내용 한글 처리
            header.setContentType(MediaType.valueOf("plain/text"));

            type = new String(type.getBytes("utf-8"), "ISO-8859-1"); // 파일명 한글 처리
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + ".csv");
            header.setContentLength(csvFile.length);

            return new ResponseEntity<>(csvFile, header, HttpStatus.OK);  // HttpRequest에 대한 응답 데이터를 포함함.
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                csvPrinter.close(); // 닫아줌.
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;    // 없으면 null
    }

}