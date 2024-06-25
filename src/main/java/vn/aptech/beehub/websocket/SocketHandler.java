package vn.aptech.beehub.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import vn.aptech.beehub.dto.LikeDto;
import vn.aptech.beehub.dto.NotificationDto;
import vn.aptech.beehub.models.GroupMember;
import vn.aptech.beehub.models.Post;
import vn.aptech.beehub.models.User;
import vn.aptech.beehub.payload.response.GetMessageResponse;
import vn.aptech.beehub.repository.GroupMemberRepository;
import vn.aptech.beehub.repository.PostRepository;
import vn.aptech.beehub.repository.UserRepository;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketHandler extends TextWebSocketHandler {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    SocketHandler(UserRepository userRepository, ObjectMapper objectMapper,PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.objectMapper = objectMapper;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketHandler.class);
    List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {
        handleMessage(session, message);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        LOGGER.info(session.getId());
        CustomSocketMessage messageSent = new CustomSocketMessage("PROFILE", session.getId());
        String json = objectMapper.writeValueAsString(messageSent);
        session.sendMessage(new TextMessage(json));
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        LOGGER.info("{} disconnect", session.getId());
        sessions.remove(session);
    }

    private void handleMessage(WebSocketSession session, TextMessage message) throws IOException {
        CustomSocketMessage socketMessage = getCustomSocketMessage(message);

        switch (socketMessage.getType()) {
            case "PROFILE": {
                UserDetails userDetails = userDetailsService.loadUserByUsername(socketMessage.getData()); // email in this case
                Principal principal =  new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());
                session.getAttributes().put("principal", principal);
                session.sendMessage(getTextMessage("PROFILE_SUCCESS", "Setup profile success"));
                break;
            }
            case "SEND_USER_MESSAGE": {
                GetMessageResponse userMessage = getMessageResponse(socketMessage.getData());
                var ou = userRepository.findById(userMessage.getRecipientId());
                if (ou.isPresent()) {
                    var user = ou.get();
                    var email = user.getEmail(); // receiver email
                    for (WebSocketSession webSocketSession : sessions) { // loop through every socket
                        if (webSocketSession.isOpen()) {
                            Principal principal = null;
                            Object object = webSocketSession.getAttributes().get("principal");
                            if (object instanceof Principal) principal = (Principal) object;

                            if (principal != null && email.equals(principal.getName())) { // looking for a principal match the email of receiver
                                LOGGER.info("sent");
                                webSocketSession.sendMessage(getTextMessage("RECEIVE_USER_MESSAGE", userMessage)); // send
                            }
                        }
                    }
                }
                break;
            }
            case "SEND_GROUP_MESSAGE": {
                GetMessageResponse userMessage = getMessageResponse(socketMessage.getData());
                List<GroupMember> ogm = groupMemberRepository.findByGroup_id(userMessage.getRecipientId());
                ogm.forEach(gm -> {
                    var user = gm.getUser();
                    var userEmail = user.getEmail();
                    if (!Objects.equals(user.getId(), userMessage.getCreatorId())) { // if user is not sender
                        LOGGER.info(user.getId().toString());
                        LOGGER.info(userMessage.getCreatorId().toString());
                        for (WebSocketSession webSocketSession : sessions) { // loop through every socket
                            if (webSocketSession.isOpen()) {
                                Principal principal = null;
                                Object object = webSocketSession.getAttributes().get("principal");
                                if (object instanceof Principal) principal = (Principal) object;
                                if (principal != null) {
                                    String principalEmail = principal.getName();
                                    if (principalEmail.equals(userEmail)) {
                                        try {
                                            webSocketSession.sendMessage(getTextMessage("RECEIVE_GROUP_MESSAGE", userMessage)); // send
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            }
                        }
                    }

                });
                break;
            }
            case "SEND_NOTI":{
            	NotificationDto noti = getNotificationDtoResponse(socketMessage.getData());
            	String receiverEmail = userRepository.findById(noti.getUser()).map(User::getEmail).get();
            	for (WebSocketSession webSocketSession : sessions) { // loop through every socket
                    if (webSocketSession.isOpen()) {
                        Principal principal = null;
                        Object object = webSocketSession.getAttributes().get("principal");
                        if (object instanceof Principal) principal = (Principal) object;

                        if (principal != null && receiverEmail.equals(principal.getName())) { // looking for a principal match the email of receiver
                            try {
                                webSocketSession.sendMessage(getTextMessage("RECEIVE_NOTI", noti)); // send
                            } catch (IOException e) {
                            	LOGGER.info(e.getMessage());
                            }
                        }
                    }
                }
            	LOGGER.info(receiverEmail);
            	break;
            }
            case "SEND_LIKE": {
                LikeDto like = getLikeDtoResponse(socketMessage.getData());
                String receiverEmail = postRepository.findById(like.getPost())
                    .map(Post::getUser)  // Lấy người dùng sở hữu bài post
                    .map(User::getEmail) // Lấy email của người dùng đó
                    .orElse(null);

                if (receiverEmail != null) { // Nếu tìm thấy email của chủ sở hữu bài post
                    for (WebSocketSession webSocketSession : sessions) { // Lặp qua tất cả các socket
                        if (webSocketSession.isOpen()) {
                            Principal principal = null;
                            Object object = webSocketSession.getAttributes().get("principal");
                            if (object instanceof Principal) principal = (Principal) object;

                            if (principal != null && receiverEmail.equals(principal.getName())) { // Tìm principal khớp với email của người nhận
                                try {
                                    webSocketSession.sendMessage(getTextMessage("RECEIVE_LIKE", like)); // Gửi
                                } catch (IOException e) {
                                    LOGGER.info(e.getMessage());
                                }
                            }
                        }
                    }
                } else {
                    LOGGER.info("Email of the post owner not found");
                }
                break;
            }
            default:
                break;
        }
    }

    private TextMessage getTextMessage(String type, Object data) throws JsonProcessingException {
        CustomSocketMessage messageSent = new CustomSocketMessage(type, objectMapper.writeValueAsString(data));
        String json = objectMapper.writeValueAsString(messageSent);
        return new TextMessage(json);
    }

    private CustomSocketMessage getCustomSocketMessage(TextMessage message) throws JsonProcessingException {
        return objectMapper.readValue(message.getPayload(), CustomSocketMessage.class);
    }

    private GetMessageResponse getMessageResponse(String data) throws JsonProcessingException {
        return objectMapper.readValue(data, GetMessageResponse.class);
    }

    private NotificationDto getNotificationDtoResponse(String data) throws JsonProcessingException {
    	return objectMapper.readValue(data, NotificationDto.class);
    }
    private LikeDto getLikeDtoResponse(String data) throws JsonProcessingException {
    	return objectMapper.readValue(data, LikeDto.class);
    }
}
