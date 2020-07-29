package me.moonsoo.travelerapplication.account;

import com.icegreen.greenmail.util.GreenMail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.mail.internet.MimeMessage;

@Getter
@AllArgsConstructor
public class SmtpServerExtension implements BeforeAllCallback, AfterAllCallback, AfterEachCallback {

    private GreenMail greenMail;

    public MimeMessage[] getMessages() {
        return greenMail.getReceivedMessages();
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {

        greenMail.start();//smtp 서버 시작
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        greenMail.stop();//smtp 서버 종료
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        greenMail.reset();
    }
}
