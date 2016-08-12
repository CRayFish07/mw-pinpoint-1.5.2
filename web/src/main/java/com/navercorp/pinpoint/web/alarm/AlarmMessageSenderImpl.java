package com.navercorp.pinpoint.web.alarm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.util.MailUtil;

@Service
public class AlarmMessageSenderImpl implements AlarmMessageSender {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private MailUtil mailUtil;

    @Override
    public void sendEmail(AlarmChecker checker, int sequenceCount) {
        List<String> receivers = userGroupService.selectEmailOfMember(checker.getuserGroupId());

        if (receivers.size() == 0) {
            return;
        }

        for (String receiver : receivers) {
            System.out.println("-----------------------------------------------------------");
            logger.error("==================================================================");
            logger.error("receiver: " + receiver + ", msg: " + checker.getEmailMessage());
            mailUtil.sendTextMail("alarm", checker.getEmailMessage());
            System.out.println("-----------------------------------------------------------");
            logger.error("==================================================================");
        }
    }

    @Override
    public void sendSms(AlarmChecker checker, int sequenceCount) {
        List<String> receivers = userGroupService.selectPhoneNumberOfMember(checker.getuserGroupId());

        if (receivers.size() == 0) {
            return;
        }

        for (String message : checker.getSmsMessage()) {
            System.out.println("-----------------------------------------------------------");
            logger.error("==================================================================");
            logger.error("send SMS : {}", message);
            System.out.println("-----------------------------------------------------------");
            logger.error("==================================================================");
            // TODO Implement logic for sending SMS
        }
    }

}
