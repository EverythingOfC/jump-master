package com.example.jump;

import com.example.jump.domain.MetaApi;
import com.example.jump.repository.MetaRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest // 해당 클래스가 스프링부트 테스트 클래스임을 알려줌.
public class JumpApplicationTests {

    @Autowired  // 해당 클래스에 MetaRepository객체 주입
    private MetaRepository metaRepository;

    @Test
    void testJpa() {

        // JArray배열
        JSONArray jArray = null;

        // 보도자료 원본의 value 리스트
        List<JSONObject> values = new ArrayList<>();

        // 보도자료 매핑 후 value 리스트 ( Title ~ Right 칼럼들의 값 )
        String[] mappingValue = new String[12];

        String originUrl = "http://apis.data.go.kr/1371000/pressReleaseService/pressReleaseList";
        String service = "OyfKMEU9NFp%2FBjVq6X4XzOKgG0iCkwCWtmQNFtDKPlfCOoqhQBo6DhgyLTsJxe5JNjyRns4f2IZ0DmneSFw0Xw%3D%3D";

        LocalDate now = LocalDate.now();    // 현재 날짜
        LocalDate start = LocalDate.of(2020,10,6);    // 처음 시작 날짜
        LocalDate end = start;

        StringBuffer result = new StringBuffer();
        DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");       // 날짜 포멧
        String[] brackets = {"{", "[{", "}", "}]", ":"};    // 대괄호
        char quotes = '"';          // 매핑시 ""안에 "을 넣기 위해 선언
        String org = "{\"org\":\""; // 매핑될 때 접두사

        try {

            while(end.isBefore(now) || end.isEqual(now)){    // 종료 날짜가 현재 날짜보다 이전이면

                end = start.plusDays(2);    // 시작날짜의 + 2

                System.out.println(start);
                String startDate = start.format(yyyyMMdd);
                String endDate = end.format(yyyyMMdd);

                StringBuilder urlBuilder = new StringBuilder(originUrl);    // url정보 저장
                urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + service);  //  Service Key
                urlBuilder.append("&" + URLEncoder.encode("startDate", "UTF-8") + "=" + startDate); //  startDate
                urlBuilder.append("&" + URLEncoder.encode("endDate", "UTF-8") + "=" + endDate);     //  endDate
                URL url = new URL(urlBuilder.toString());

                // Http연결을 위한 객체 생성
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-type", "application/json");
                urlConnection.setConnectTimeout(20000); //서버 연결 제한 시간
                urlConnection.setReadTimeout(20000);    //읽기 제한 시간
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String re = null;
                while ((re = br.readLine()) != null)    // 받아올 값이 있으면
                    result.append(re);  //  StringBuffer 객체에 데이터 추가

                br.close();
                urlConnection.disconnect();

                JSONObject jsonObject = XML.toJSONObject(result.toString());    // StringBuffer -> String으로 형 변환 후 XML데이터를 Json객체로 생성
                JSONObject jsonObject2 = jsonObject.getJSONObject("response");  //  key값이 response인 jsonObject를 찾음

                try {
                    JSONObject jsonObject3 = jsonObject2.getJSONObject("body");    // 없는 데이터면 다시 for문으로 돌아감.
                    jArray = (JSONArray) jsonObject3.get("NewsItem");  //  key값이 NewsItem인 객체들을 JSON 배열로 만듬
                } catch (JSONException e) {
                    continue;
                }

                result.setLength(0);

                int length = jArray.length();
                int size = metaRepository.findAll().size();
                for (int i = 0; i < length; i++) {  // key값이 NewsItem인 객체들의 갯수만큼 반복

                    JSONObject item = (JSONObject) jArray.get(i);    // JsonArray의 i+1번째 객체를 얻어옴.
                    values.add(item);   // list에 JsonObject객체의 값을 하나씩 저장

                    try {
                        // 받아온 데이터에 {와 "를 붙이기 위한 로직들
                        mappingValue[0] = org + item.get("Title").toString() + quotes + brackets[2];
                        if (!item.get("SubTitle1").equals("")) {
                            mappingValue[1] = (org+ item.get("SubTitle1") + quotes + brackets[2]);
                        } else if (!item.get("SubTitle2").equals("")) {
                            mappingValue[1] = (org + item.get("SubTitle2") + quotes + brackets[2]);
                        } else if (!item.get("SubTitle3").equals("")) {
                            mappingValue[1] = (org + item.get("SubTitle3") + quotes + brackets[2]);
                        } else {
                            mappingValue[1] = (org + item.get("Title") + quotes + brackets[2]);
                        }
                        mappingValue[2] = ((brackets[0]  + quotes + "summary" + quotes + ":"+ org + quotes + item.get("DataContents") + quotes + brackets[2]));
                        mappingValue[3] = (org + item.get("MinisterCode").toString() + quotes + brackets[2]);
                        mappingValue[4] = (org + item.get("MinisterCode") + "\",\"role\""  + brackets[4] + quotes + "author" + quotes + brackets[3]);

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

                        mappingValue[5] = (brackets[0] + quotes + "modified" + quotes + brackets[4] + quotes + dateTemp + ",\"available" + quotes + brackets[4] + quotes + dateTemp2 + quotes + brackets[2]);
                        mappingValue[6] = (org + quotes + "ko" + quotes + brackets[2]);
                        mappingValue[7] = (brackets[0] + quotes + "site" + quotes + brackets[4] + quotes + item.get("NewsItemId").toString() + ",view:" + item.get("OriginalUrl").toString() + quotes + brackets[2]);
                        mappingValue[8] = (org + quotes + quotes + brackets[2]);
                        mappingValue[9] = (brackets[0] + quotes + "related" + quotes + ":[" + quotes + item.get("FileName").toString() + quotes + "," + quotes + item.get("FileUrl").toString() + quotes + brackets[3]);
                        mappingValue[10] = (org + quotes + quotes + brackets[2]);
                        mappingValue[11] = (org + quotes + quotes + brackets[2]);

                        // 정규 표현식으로 태그 및 특수문자들 제거
                        for (int j = 0; j < 12; j++) {
                            mappingValue[j] = mappingValue[j].replaceAll("<[^>]*>|&[^;]*;", "");     // <나 &로 시작하는 html태그와 특수문자들을 제거한다.
                        }


                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    MetaApi meta = new MetaApi(String.valueOf(i + size), "", "보도자료",    // 생성자를 통한 객체 초기화
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
                jArray.clear();
                start = end.plusDays(1);  // 종료날짜 + 1
            }
        }
        catch(SocketException e){
            System.out.println("통신오류입니다.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}