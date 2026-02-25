package com.smartautorental.platform.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    public void send(String destination, String subject, String content) {
        log.info("Notification stub -> to={} subject={} content={}", destination, subject, content);
    }
}
