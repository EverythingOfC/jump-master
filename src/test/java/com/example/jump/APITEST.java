package com.example.jump;

import com.example.jump.domain.MetaApi;
import com.example.jump.repository.MetaRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

@SpringBootTest
public class APITEST {
    @Autowired
    MetaRepository metaRepository;

    @Test
    void APITest() {
        try {
            String key = "0522434286344176aae24d07b5f824f6";
            String page = "https://open.assembly.go.kr/portal/openapi/nzmimeepazxkubdpn";
            // 인증키 (개인이 받아와야함)
            // 파싱한 데이터를 저장할 변수

            // 2. 오픈 API의 요청 규격에 맞는 파라미터 생성, 발급받은 인증키.

            String[] mappingValue = new String[12];

            for (int u = 1; u <= 18; u++) {
                BufferedReader bf;
                StringBuffer result1 = new StringBuffer();
                StringBuilder urlBuilder = new StringBuilder(page); //  url정보 저장

                urlBuilder.append("?" + URLEncoder.encode("KEY", "UTF-8") + "=" + key); //Service Key
                urlBuilder.append("&" + URLEncoder.encode("Type", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));
                urlBuilder.append("&" + URLEncoder.encode("pIndex", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(u), "UTF-8"));
                urlBuilder.append("&" + URLEncoder.encode("pSize", "UTF-8") + "=" + URLEncoder.encode("1000", "UTF-8"));
                urlBuilder.append("&" + URLEncoder.encode("AGE", "UTF-8") + "=" + URLEncoder.encode("21", "UTF-8"));

                // 해당 url에 접속해서 정보를 받아옴.
                URL url = new URL(urlBuilder.toString());

                bf = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));  // 문자열을 읽어서 저장한다.
                String returnLine = null;

                while ((returnLine = bf.readLine()) != null) {   // 문자열이 비어있지 않으면
                    result1.append(returnLine); // 정보 추가
                }

                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(result1.toString());
                JSONArray jsonArray = (JSONArray) jsonObject.get("nzmimeepazxkubdpn");
                String a1 = "";
                for (int i = 1; i < 2; i++) {
                    JSONObject a = (JSONObject) jsonArray.get(i);
                    a1 = a.get("row").toString();
                }
                JSONArray jsonArray2 = (JSONArray) jsonParser.parse(a1);
                for (int i = 0; i < jsonArray2.size(); i++) {
                    JSONObject a = (JSONObject) jsonArray2.get(i);
                    MetaApi metaAPI = MetaApi.builder().
                            metaId((String) a.get("BILL_ID")).
                            metaClassifications("사회>정치").
                            metaType("법률").
                            metaTitle("{\"org\":\"" + (String) a.get("BILL_NAME") + "\"}").
                            metaSubjects("{\"org\":\"" + (String) a.get("BILL_NAME") + "\"}").
                            metaDescription("{\"org\":\"" + (String) a.get("BILL_NAME") + "\"}").
                            metaPublisher("{\"org\":\"" + (String) a.get("COMMITTEE") + "\"}").
                            metaContributor("{\"org\":\"" + (String) a.get("RST_PROPOSER") + "\"}").
                            metaDate("{\"proposed\":\"" + (String) a.get("PROPOSE_DT") + "\"}").
                            metaLanguage("{\"org\":\"ko\"}").
                            metaIdentifier("{\"view\":\"" + (String) a.get("DETAIL_LINK") + "\"}").
                            build();
                    metaRepository.save(metaAPI);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();    // 오류 계속 출력
        }
    }

}
