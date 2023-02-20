package com.example.jump.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


@NoArgsConstructor
@DynamicInsert  // 값이 null인 필드를 제외하고 insert함 (default값이 적용됨)
@Getter
@Setter
@Entity // DB와 직접적으로 연결됨.
public class SearchApi {
    @Id
    private long id;        // 아이디

    @Column(columnDefinition = "TEXT")
    private String title;   // 검색할 타이틀

    public SearchApi(long id, String title){    // 생성자
        this.id = id;
        this.title = title;
    }
}
