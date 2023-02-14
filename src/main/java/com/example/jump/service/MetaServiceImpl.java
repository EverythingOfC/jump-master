package com.example.jump.service;

import com.example.jump.domain.ClientSupportApi;
import com.example.jump.domain.MetaApi;
import com.example.jump.domain.RegisterDTO;
import com.example.jump.domain.SearchApi;
import com.example.jump.entity.ClubMember;
import com.example.jump.repository.ClientSupportApiRepository;
import com.example.jump.repository.ClubMemberRepository;
import com.example.jump.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
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
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


@RequiredArgsConstructor
@Service    // 해당 클래스를 스프링의 서비스로 인식
public class MetaServiceImpl implements MetaService {

    private final MetaRepository metaRepository;    // 레퍼지토리 객체 생성
    private final ClientSupportApiRepository clientSupportApiRepository;    // 고객지원 테이블에 접근하기 위한 객체

    private final ClubMemberRepository clubMemberRepository; // 회원정보를 가져오기 위한 객체

    public Page<MetaApi> getList(int page, String type) {   // 전체 조회

        Pageable pageable = null;    //  조회할 페이지 번호와 한 페이지에 보여줄 데이터의 개수를 객체로 저장
        try {
            pageable = PageRequest.of(page, 10);
        } catch (IllegalArgumentException e) {      // api 리스트가 비어있을 시에, 이전, 다음 버튼을 누르면 오류가 나므로 예외처리
            this.metaRepository.findAll();
        }
        return this.metaRepository.findAllByMetaType(pageable, type);
    }

    public MetaApi getView(String id) {    // 상세
        Optional<MetaApi> ID = this.metaRepository.findById(id);
        return ID.isPresent() ? ID.get() : null;       // id에 해당하는 데이터가 있으면 불러옴
    }

    public void delete(String[] id) {       // 삭제
        int length = id.length;

        for (int i = 0; i < length; i++) {  // 삭제할 id값들을 반복함.
            Optional<MetaApi> ID = this.metaRepository.findById(id[i]);
            if (ID.isPresent())  // 값이 있다면
                this.metaRepository.delete(ID.get());   //  해당 객체 삭제
        }
    }

    public void save(MetaApi meta) {   // 수정

        this.metaRepository.save(meta);
    }

    public void getApi(String type) {   // 해당 API를 저장 및 출력

        // api유형과 url주소의 쌍
        Map<String, String> map = new HashMap<>();
        map.put("보도자료", "http://apis.data.go.kr/1371000/pressReleaseService/pressReleaseList?serviceKey=OyfKMEU9NFp%2FBjVq6X4XzOKgG0iCkwCWtmQNFtDKPlfCOoqhQBo6DhgyLTsJxe5JNjyRns4f2IZ0DmneSFw0Xw%3D%3D&startDate=20211201&endDate=20211203");
        map.put("정책뉴스", "http://apis.data.go.kr/1371000/policyNewsService/policyNewsList?serviceKey=OyfKMEU9NFp%2FBjVq6X4XzOKgG0iCkwCWtmQNFtDKPlfCOoqhQBo6DhgyLTsJxe5JNjyRns4f2IZ0DmneSFw0Xw%3D%3D&startDate=20211201&endDate=20211203");
        map.put("포토", "http://apis.data.go.kr/1371000/photoNewsService/photoNewsList?serviceKey=OyfKMEU9NFp%2FBjVq6X4XzOKgG0iCkwCWtmQNFtDKPlfCOoqhQBo6DhgyLTsJxe5JNjyRns4f2IZ0DmneSFw0Xw%3D%3D&startDate=20211201&endDate=20211203");
        map.put("코트라", "http://openknowledge.kotra.or.kr/handle/2014.oak/");
        map.put("현행법령", "http://www.law.go.kr/DRF/lawSearch.do?OC=helena0809&target=law&");

        // json배열
        JSONArray jArray = null;
        // 데이터 원본의 value 리스트
        List<JSONObject> values = new ArrayList<>();

        // 데이터 매핑 후 value 리스트 ( Title ~ Right 칼럼들의 값 )
        String[] mappingValue = new String[12];
        char quotes = '"';          // 매핑시 ""안에 "을 넣기 위해 선언

        if (type.equals("현행법령")) {
            try {
                for (int num = 1; num < 264; num++) {

                    URL url = new URL(map.get(type) + "type=XML&page=" + num); /*url 주소*/
                    // Http연결을 위한 객체 생성
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Content-type", "application/json");

                    // url에서 불러온 데이터를 InputStream -> InputStreamReader -> BufferedReader -> readLine()로 받아옴.
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                    StringBuffer result = new StringBuffer();
                    String re = null;
                    while ((re = br.readLine()) != null)    // 받아올 값이 있으면
                        result.append(re);  //  StringBuffer 객체에 데이터 추가

                    JSONObject jsonObject = XML.toJSONObject(result.toString());
                    JSONObject jsonObject2 = jsonObject.getJSONObject("LawSearch");
                    jArray = jsonObject2.getJSONArray("law");

                    for (int i = 0; i < jArray.length(); i++) { //for문을 통해 JsonArray의 수 만큼 리스트 안의 객체를 분리

                        JSONObject item = (JSONObject) jArray.get(i);
                        // 원하는 항목명을 Parsing 해서 형식에 맞춰 변수에 저장
                        try {

                            mappingValue[0] = ("{" + quotes + "org" + quotes + ":" + quotes + item.get("법령명한글").toString() + quotes + "}"); // Title

                            if (item.get("법령약칭명").equals("")) {    // 값이 없으면
                                mappingValue[1] = (("{" + quotes + "org" + quotes + ":" + quotes + item.get("법령명한글").toString() + quotes + "}"));   // Subject
                            } else {
                                mappingValue[1] = (("{" + quotes + "org" + quotes + ":" + quotes + item.get("법령약칭명").toString() + quotes + "}"));   // Subject
                            }
                            mappingValue[2] = (("{" + quotes + "summary" + quotes + ":{" + quotes + "org" + quotes + ":" + quotes + item.get("법령명한글") + quotes + "}}")); // 설명
                            mappingValue[3] = ("{" + quotes + "org" + quotes + ":" + quotes + item.get("소관부처명").toString() + quotes + "}");
                            mappingValue[4] = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("소관부처명") + quotes + "," + quotes + "role" + quotes + ":" + quotes + "author" + quotes + "}]"));
                            mappingValue[5] = ("{" + quotes + "issued" + quotes + ":" + quotes + item.get("시행일자").toString() + quotes + "," + quotes + "created" + quotes + ":" + quotes + item.get("공포일자").toString() + quotes + "}");
                            mappingValue[6] = ("{" + quotes + "org" + quotes + ":" + quotes + "ko" + quotes + "}");
                            mappingValue[7] = ("{" + quotes + "site" + quotes + ":" + quotes + item.get("법령일련번호").toString() + quotes + "," + quotes + "url" + quotes + ":" + quotes + item.get("법령상세링크").toString() + quotes + "}");
                            mappingValue[8] = (("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));
                            mappingValue[9] = ("{" + quotes + "isPartOF" + quotes + ":" + quotes + item.get("제개정구분명").toString() + quotes + "}");
                            mappingValue[10] = (("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));
                            mappingValue[11] = (("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));

                            MetaApi meta = new MetaApi(item.get("법령ID").toString(), "", type,    // 생성자를 통한 객체 초기화
                                    (mappingValue[0]),
                                    (mappingValue[1]),
                                    (mappingValue[2]),
                                    (mappingValue[3]),
                                    (mappingValue[4]),
                                    (mappingValue[5]),
                                    (mappingValue[6]),
                                    (mappingValue[7]),
                                    (mappingValue[8]),
                                    (mappingValue[9]),
                                    (mappingValue[10]),
                                    (mappingValue[11]));
                            metaRepository.save(meta);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (type.equals("코트라")) {    // Open Access Korea 데이터
            //웹에서 내용을 가져온다.
            int num = 0;
            try {

                for (num = 2000; num < 3000; num++) {
                    // Jsoup: 자바에서 html파싱을 위한 라이브러리,    url 접속을 위한 connection객체를 반환
                    Document doc = null;
                    try {
                        doc = Jsoup.connect(map.get("코트라") + num).get();
                    } catch (Exception e) {
                        continue;
                    }

                    Elements[] column = new Elements[14];
                    Elements[] content = new Elements[14];

                    // html파싱 후 칼럼과 내용을 담음.
                    try {
                        for (int i = 0; i < 14; i++) {
                            column[i] = doc.select("#con_h > div.item_dl1 > dl:nth-child(" + (i + 1) + ") > dt");
                        }
                        for (int i = 0; i < 14; i++) {
                            content[i] = doc.select("#con_h > div.item_dl1 > dl:nth-child(" + (i + 1) + ") > dd");
                        }
                    } catch (Exception e) {
                        continue;
                    }

                    String a = null;
                    String b = null;
                    String[] menu = {"Title", "Corporate Author", "Personal Author", "Issue Date", "Publisher", "Series/Report No.", "Description", "Table Of Contents", "ISBN", "Citation", "URI", "Language", "Subject", "Appears in Collections"};
                    String[] key = new String[14];      // 정제 전 칼럼
                    String[] value = new String[14];    // 정제 전 데이터
                    String[] key2 = new String[14];     // 정제 후 칼럼
                    String[] value2 = new String[14];   // 정제 후 데이터

                    for (int i = 0; i < 14; i++) {  // 칼럼을 키로 저장
                        key[i] = column[i].toString();
                    }

                    for (int i = 0; i < 14; i++) {  // 데이터를 값으로 저장
                        value[i] = content[i].toString();
                    }

                    for (int k = 0; k < 14; k++) {    // 정규 표현식으로 태그 및 특수문자 삭제
                        a = key[k].replaceAll("<[^>]*>", "").replaceAll("&[^;]*;", "").replace("\n", "");
                        a = a.trim(); // 항목명 앞에 빈칸 삭제
                        b = value[k].replaceAll("<[^>]*>", "").replaceAll("&[^;]*;", "").replace("\n", "");
                        key2[k] = a.toString();      // 정제된 칼럼을 넣음
                        value2[k] = b.toString();   // 정제된 데이터를 넣음
                    }

                    String[] rawValue = new String[15]; // 정제된 데이터를 DB 형식에 맞게 저장

                    for (int i = 0; i < 14; i++) {      // 데이터의 순서가 다르므로 for문으로 반복하면서 맞는 값을 찾아줌.
                        for (int j = 0; j < 14; j++) {
                            if (key2[i].equals(menu[j])) {
                                rawValue[j] = value2[i];
                                break;
                            }
                        }
                    }

                    try {
                        // 받아온 데이터에 {와 "를 붙이기 위한 로직들
                        mappingValue[0] = ("{" + quotes + "org" + quotes + ":" + quotes + rawValue[0] + quotes + "}");

                        if (rawValue[12] != null) {
                            mappingValue[1] = (("[{" + quotes + "org" + quotes + ":" + quotes + rawValue[12] + quotes + "}]"));
                        } else {
                            mappingValue[1] = (("[{" + quotes + "org" + quotes + ":" + quotes + rawValue[0] + quotes + "}]"));
                        }
                        mappingValue[2] = (("{" + quotes + "toc" + quotes + ":{" + quotes + "org" + quotes + ":" + quotes + rawValue[7] + quotes + "}," + quotes + "summary" + quotes + ":{" + quotes + "org" + quotes + ":" + quotes + rawValue[6] + quotes + "}"));
                        mappingValue[3] = ("{" + quotes + "org" + quotes + ":" + quotes + rawValue[4] + quotes + "}");
                        mappingValue[4] = (("[{" + "role" + quotes + ":" + quotes + "author" + quotes + "," + quotes + "org" + quotes + ":" + quotes + rawValue[2] + quotes + "," + quotes + "affiliation" + quotes + ":" + "[{" + quotes + "org" + quotes + ":" + quotes + rawValue[2] + quotes + "}]"));
                        mappingValue[5] = ("{" + quotes + "issued" + quotes + ":" + quotes + rawValue[3] + quotes + "}");
                        mappingValue[6] = ("{" + quotes + "org" + quotes + ":" + quotes + rawValue[11] + quotes + "}");
                        mappingValue[7] = ("{" + quotes + "ibsn" + quotes + ":" + quotes + rawValue[8] + "," + "view:" + rawValue[10] + quotes + "}");
                        mappingValue[8] = (("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));
                        mappingValue[9] = ("{" + quotes + "citation" + quotes + ":[" + quotes + rawValue[9] + quotes + "]}");
                        mappingValue[10] = (("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));
                        mappingValue[11] = (("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    MetaApi meta = new MetaApi(String.valueOf(num), "", type,    // 생성자를 통한 객체 초기화
                            (mappingValue[0]),
                            (mappingValue[1]),
                            (mappingValue[2]),
                            (mappingValue[3]),
                            (mappingValue[4]),
                            (mappingValue[5]),
                            (mappingValue[6]),
                            (mappingValue[7]),
                            (mappingValue[8]),
                            (mappingValue[9]),
                            (mappingValue[10]),
                            (mappingValue[11]));
                    metaRepository.save(meta);  // Entity에 Meta데이터를 저장한다.
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                // map의 api유형으로 url값을 받아옴
                URL url = new URL(map.get(type));

                // Http연결을 위한 객체 생성
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-type", "application/json");

                // url에서 불러온 데이터를 InputStream -> InputStreamReader -> BufferedReader -> readLine()로 받아옴.
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                StringBuffer result = new StringBuffer();
                String re = null;
                while ((re = br.readLine()) != null)    // 받아올 값이 있으면
                    result.append(re);  //  StringBuffer 객체에 데이터 추가

                JSONObject jsonObject = XML.toJSONObject(result.toString());    // StringBuffer -> String으로 형 변환 후 XML데이터를 Json객체로 생성
                JSONObject jsonObject2 = jsonObject.getJSONObject("response");  //  key값이 response인 jsonObject를 찾음
                JSONObject jsonObject3 = jsonObject2.getJSONObject("body"); //  key값이 body인 jsonObject를 찾음
                jArray = (JSONArray) jsonObject3.get("NewsItem");  //  key값이 NewsItem인 객체들을 JSON 배열로 만듬

                int length = jArray.length();
                for (int i = 0; i < length; i++) {  // key값이 NewsItem인 객체들의 갯수만큼 반복

                    JSONObject item = (JSONObject) jArray.get(i);    // JsonArray의 i+1번째 객체를 얻어옴.
                    values.add(item);   // list에 JsonObject객체의 값을 하나씩 저장

                    try {
                        // 받아온 데이터에 {와 "를 붙이기 위한 로직들
                        mappingValue[0] = ("{" + quotes + "org" + quotes + ":" + quotes + item.get("Title").toString() + quotes + "}");
                        if (!item.get("SubTitle1").equals("")) {
                            mappingValue[1] = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("SubTitle1") + quotes + "}]"));
                        } else if (!item.get("SubTitle2").equals("")) {
                            mappingValue[1] = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("SubTitle2") + quotes + "}]"));
                        } else if (!item.get("SubTitle3").equals("")) {
                            mappingValue[1] = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("SubTitle3") + quotes + "}]"));
                        } else {
                            mappingValue[1] = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("Title") + quotes + "}]"));
                        }
                        mappingValue[2] = (("{" + quotes + "summary" + quotes + ":{" + quotes + "org" + quotes + ":" + quotes + item.get("DataContents") + quotes + "}"));
                        mappingValue[3] = ("{" + quotes + "org" + quotes + ":" + quotes + item.get("MinisterCode").toString() + quotes + "}");
                        mappingValue[4] = (("[{" + quotes + "org" + quotes + ":" + quotes + item.get("MinisterCode") + quotes + "," + quotes + "role" + quotes + ":" + quotes + "author" + quotes + "}]"));

                        // 날짜 변환 로직
                        SimpleDateFormat dfFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");    // 파싱 전 형식
                        SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy-MM-dd"); // 파싱 후 형식
                        String strDate = item.get("ModifyDate").toString(); // jsonObject의 get메소드로 ModifyDate를 String으로 변환
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

                        mappingValue[5] = ("{" + quotes + "modified" + quotes + ":" + quotes + dateTemp + "," + quotes + "available" + quotes + ":" + quotes + dateTemp2 + quotes + "}");
                        mappingValue[6] = ("{" + quotes + "org" + quotes + ":" + quotes + "ko" + quotes + "}");
                        mappingValue[7] = ("{" + quotes + "site" + quotes + ":" + quotes + item.get("NewsItemId").toString() + "," + "view:" + item.get("OriginalUrl").toString() + quotes + "}");
                        mappingValue[8] = (("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));

                        try {
                            mappingValue[9] = ("{" + quotes + "related" + quotes + ":[" + quotes + item.get("FileName").toString() + quotes + "," + quotes + item.get("FileUrl").toString() + quotes + "]}");
                        } catch (JSONException e) {
                            mappingValue[9] = "{\"related\":\"\"}";     // FileName이나 FileUrl이 없는 경우에는
                        }

                        mappingValue[10] = (("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));
                        mappingValue[11] = (("{" + quotes + "org" + quotes + ":" + quotes + quotes + "}"));

                        // 정규 표현식으로 태그 및 특수문자들 제거
                        for (int j = 0; j < 12; j++) {
                            mappingValue[j] = mappingValue[j].replaceAll("<[^>]*>", "");   // HTML 태그 형식 삭제
                            mappingValue[j] = mappingValue[j].replaceAll("&[^;]*;", "");   // HTML 특수문자들 제거 ( ex: &nbsp; &middot )
                        }
                        System.out.println(mappingValue[2]);

                    } catch (JSONException e) {
                        continue;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    MetaApi meta = new MetaApi(item.get("NewsItemId").toString(), "", type,    // 생성자를 통한 객체 초기화
                            (mappingValue[0]),
                            (mappingValue[1]),
                            (mappingValue[2]),
                            (mappingValue[3]),
                            (mappingValue[4]),
                            (mappingValue[5]),
                            (mappingValue[6]),
                            (mappingValue[7]),
                            (mappingValue[8]),
                            (mappingValue[9]),
                            (mappingValue[10]),
                            (mappingValue[11]));
                    metaRepository.save(meta);  // Entity에 Meta데이터를 저장한다.
                }
            } catch (SocketException e) {
                System.out.println("통신 오류입니다.");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ResponseEntity<byte[]> saveCsv(String type) {    // CSV로 저장

        List<MetaApi> meta = metaRepository.findAllByMetaType(type);     // 유형에 해당하는 데이터를 받아옴

        String[] menu = {"Title", "Subject", "Description", "Publisher", "Contributors", "Date",
                "Language", "Identifier", "Format", "Relation", "Coverage", "Right"};   // CSV의 Header로 사용할 column들

        byte[] csvFile = null;          // csv데이터를 담을 배열
        CSVPrinter csvPrinter = null;   // csv형식의 값을 출력
        StringWriter sw = new StringWriter();   // 문자열 writer객체 생성

        try {
            csvPrinter = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader(menu));        // csv의 헤더 생성
            for (MetaApi m : meta) {
                List<String> data = Arrays.asList(m.getMetaTitle(), m.getMetaSubjects(), // csv파일에 추가하고 싶은 데이터를 임의로 대입
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

            return new ResponseEntity<byte[]>(csvFile, header, HttpStatus.OK);  // HttpRequest에 대한 응답 데이터를 포함함.
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
    }          // CSV로 저장

    @Override
    public List<SearchApi> searchApi(String title) {
        return null;
    }

    public ClientSupportApi supportSave(String category, String title, String name, String content, String method) {     // 고객지원 저장
        ClientSupportApi api = new ClientSupportApi(category, title, name, content, method);         // 엔티티 객체 초기화
        this.clientSupportApiRepository.save(api);
        return api;
    }    // 고객지원 저장

    @Override
    public List<ClientSupportApi> boardlist() {
        return clientSupportApiRepository.findAll();
    }

    @Override
    public ClientSupportApi boardview(Long CId) {
        return clientSupportApiRepository.findById(CId).get();
    }

    @Override
    public ClubMember getUser(String email) {   // 문의하기에 필요한 회원 정보를 가져옴

        Optional<ClubMember> user = clubMemberRepository.findById(email);

        if (user.isPresent()) {
            return user.get();
        } else {
            return null;
        }
    }
}