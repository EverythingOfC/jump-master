package com.example.jump.service;

import com.example.jump.domain.ClientSupportApi;
import com.example.jump.domain.RegisterDTO;
import com.example.jump.entity.ClubMember;
import com.example.jump.entity.ClubMemberRole;
import com.example.jump.repository.ClientSupportApiRepository;
import com.example.jump.repository.ClubMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;

@RequiredArgsConstructor
@Service
public class MetaMemberImpl implements MetaMember{

    private final ClientSupportApiRepository clientSupportApiRepository;    // 고객지원 테이블에 접근
    private final ClubMemberRepository clubMemberRepository;    // 회원정보 테이블에 접근

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ClientSupportApi supportSave(String category, String title, String name, String content, String method) {     // 고객지원 데이터 저장
        ClientSupportApi api = new ClientSupportApi(category, title, name, content, method);      // 엔티티 객체 초기화
        this.clientSupportApiRepository.save(api);
        return api;
    }

    public ClubMember getUser(String email) {   // 고객지원에 표시할 회원 정보

        Optional<ClubMember> user = clubMemberRepository.findById(email);

        if (user.isPresent()) {
            return user.get();
        } else {
            return null;
        }
    }

    public boolean register(RegisterDTO registerDTO){  // 회원가입 및 Java Mail 라이브러리와 SMTP 프로토콜을 통한 이메일 발송 기능

        Optional<ClubMember> result = clubMemberRepository.findByEmail(registerDTO.getEmail(), false);
        String host = "smtp.naver.com";
        String port = "465";
        String userName = "tkflwk23@naver.com";
        String password = "@tjdwns12";

        if (result.isPresent()) {
            return false;
        } else {
            ClubMember clubMember = ClubMember.builder()
                    .email(registerDTO.getEmail())
                    .name(registerDTO.getName())
                    .fromSocial(false)
                    .roleSet(new HashSet<>())
                    .password(passwordEncoder.encode(registerDTO.getPassword()))
                    .build();

            clubMember.addMemberRole(ClubMemberRole.USER);
            clubMemberRepository.save(clubMember);

            Properties props = System.getProperties();
            props.put("mail.smtp.host", host);  // SMTP 서버 주소
            props.put("mail.smtp.port", port);  // SMTP 포트 번호
            props.put("mail.smtp.auth", "true");    // 권한 설정
            props.put("mail.smtp.ssl.enable", "true");          // ssl 보안연결 사용 ( 인터넷 보안 프로토콜 )
            //  props.put("mail.smtp.starttls.enable","true");  // tcl 보안연결

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, "@tjdwns12");   // 송신자의 이메일 주소 및 비밀번호
                }
            });

            session.setDebug(true);   //for debug

            try {
                Message mimeMessage = new MimeMessage(session);
                mimeMessage.setFrom(new InternetAddress(userName));
                mimeMessage.setRecipient(Message.RecipientType.TO,
                        new InternetAddress(clubMember.getEmail()));    // 지정된 이메일로 회원가입 성공 메시지 발송
                mimeMessage.setSubject(clubMember.getName() + "님 가입을 축하합니다.");
                mimeMessage.setText("회원가입이 완료되었습니다.");

                Transport transport = session.getTransport();
                transport.connect(host,userName,password);  // SMTP 서버 주소, 발송자 ID, 비밀번호 와 연결
                transport.sendMessage(mimeMessage,mimeMessage.getAllRecipients());
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
    }
    public void delete(String email){
        ClubMember member = this.clubMemberRepository.findByEmail(email);   // 해당 회원 조회
        this.clubMemberRepository.delete(member);   // 삭제
    }
}
