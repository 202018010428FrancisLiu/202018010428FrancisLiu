package com.example.smartmeter.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/indexWebSocket")
public class IndexWebSocket {

    private static CopyOnWriteArraySet<Session> sessionList = new CopyOnWriteArraySet<>();

    Logger logger = LoggerFactory.getLogger(IndexWebSocket.class);

    @OnOpen
    public void onOpen(Session session) {
        if (session != null) {
            IndexWebSocket.sessionList.add(session);
        }
        logger.info("indexWebSocket 已连接");
    }

    @OnClose
    public void onClose(Session session) {
        logger.info("indexWebSocket 关闭");
    }

    @OnMessage
    public void onMessage(String message) {
        if (CollectionUtils.isEmpty(sessionList)) {
            return;
        }
        for (Session session : sessionList) {
            try {
                synchronized (session) {
                    if (session.isOpen()) {
                        session.getBasicRemote().sendText(message);
                    } else {
                        sessionList.remove(session);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.info("indexWebSocket error:" + error);
    }

}
