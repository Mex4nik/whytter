package com.mex.whytter.service;

import com.mex.whytter.domain.Message;
import com.mex.whytter.domain.User;
import com.mex.whytter.domain.UserSubscription;
import com.mex.whytter.domain.Views;
import com.mex.whytter.dto.EventType;
import com.mex.whytter.dto.MessagePageDto;
import com.mex.whytter.dto.MetaDto;
import com.mex.whytter.dto.ObjectType;
import com.mex.whytter.repository.MessageRepository;
import com.mex.whytter.repository.UserSubscriptionRepository;
import com.mex.whytter.util.WebSocketSender;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MessageService {
    private static String URL_PATTERN = "https?:\\/\\/?[\\w\\d\\._\\-%\\/\\?=&#]+";
    private static String IMAGE_PATTERN = "\\.(jpeg|jpg|gif|png)$";

    private static Pattern URL_REGEX = Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE);
    private static Pattern IMG_REGEX = Pattern.compile(IMAGE_PATTERN, Pattern.CASE_INSENSITIVE);

    private final MessageRepository messageRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final BiConsumer<EventType, Message> webSocketSender;

    @Autowired
    public MessageService(
            MessageRepository messageRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            WebSocketSender webSocketSender
    ) {
        this.messageRepository = messageRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.webSocketSender = webSocketSender.getSender(ObjectType.MESSAGE, Views.FullMessage.class);
    }

    public MessagePageDto findForUser(Pageable pageable, User user) {
        List<User> channels = userSubscriptionRepository.findBySubscriber(user)
                .stream()
                .map(UserSubscription::getChannel)
                .collect(Collectors.toList());

        channels.add(user);

        Page<Message> page = messageRepository.findByAuthorIn(channels, pageable);
        return new MessagePageDto(
                page.getContent(),
                pageable.getPageNumber(),
                page.getTotalPages()
        );
    }

    public Message create(Message message, User user) throws IOException {
        message.setDateTime(LocalDateTime.now());
        fillMeta(message);
        message.setAuthor(user);
        Message updatedMessage = messageRepository.save(message);

        webSocketSender.accept(EventType.CREATE, updatedMessage);

        return updatedMessage;
    }

    public Message update(Message messageDB, Message message, User user) throws IOException {
        messageDB.setText(message.getText());
        fillMeta(messageDB);
        messageDB.setAuthor(user);
        Message updatedMessage = messageRepository.save(messageDB);

        webSocketSender.accept(EventType.UPDATE, updatedMessage);

        return updatedMessage;
    }

    public void delete(Message message) {
        messageRepository.delete(message);
        webSocketSender.accept(EventType.REMOVE, message);
    }


    private void fillMeta(Message message) throws IOException {
        String text = message.getText();
        Matcher matcher = URL_REGEX.matcher(text);

        if (matcher.find()) {
            String url = text.substring(matcher.start(), matcher.end());

            matcher = IMG_REGEX.matcher(url);

            message.setLink(url);

            if (matcher.find()) {
                message.setLinkCover(url);
            } else if (!url.contains("youtu")) {
                MetaDto meta = getMeta(url);

                message.setLinkCover(meta.getCover());
                message.setLinkTitle(meta.getTitle());
                message.setLinkDescription(meta.getDescription());
            }
        }
    }

    private MetaDto getMeta(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements title = doc.select("meta[name$=title],meta[property$=title]");
        Elements description = doc.select("meta[name$=description],meta[property$=description]");
        Elements cover = doc.select("meta[name$=image],meta[property$=image]");

        return new MetaDto(
                getContent(title.first()),
                getContent(description.first()),
                getContent(cover.first())
        );
    }

    private String getContent(Element element) {
        return element == null ? "" : element.attr("content");
    }

}
