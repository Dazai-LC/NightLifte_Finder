package com.example.nightlife_finder.repositories;

import com.example.nightlife_finder.constants.FirebaseConstants;
import com.example.nightlife_finder.firebase.FirebaseManager;
import com.example.nightlife_finder.interfaces.OnChatActionListener;
import com.example.nightlife_finder.interfaces.OnConversationLoadedListener;
import com.example.nightlife_finder.interfaces.OnMessageLoadedListener;
import com.example.nightlife_finder.models.ChatMessage;
import com.example.nightlife_finder.models.Conversation;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ChatRepository – xử lý tất cả thao tác Firestore liên quan đến chat.
 *
 * Schema:
 *   conversations/{conversationId}
 *       id, title, createdBy, createdByEmail, lastMessage, createdAt, lastMessageAt
 *
 *   conversations/{conversationId}/messages/{messageId}
 *       id, conversationId, senderId, senderEmail, text, createdAt
 */
public class ChatRepository {

    private final FirebaseFirestore db;

    public ChatRepository() {
        this.db = FirebaseManager.getInstance().getFirestore();
    }

    // -------------------------------------------------------
    // Tạo cuộc trò chuyện mới + lưu tin nhắn đầu tiên
    // -------------------------------------------------------
    public void createConversation(String title,
                                   String uid,
                                   String email,
                                   String firstMessageText,
                                   OnChatActionListener listener) {

        long now = System.currentTimeMillis();

        // Tạo document tham chiếu để lấy ID trước khi write
        DocumentReference convRef = db
                .collection(FirebaseConstants.COLLECTION_CONVERSATIONS)
                .document();

        String convId = convRef.getId();

        // Tạo map đúng theo schema
        Map<String, Object> convData = new HashMap<>();
        convData.put("id", convId);
        convData.put("title", title);
        convData.put("createdBy", uid);
        convData.put("createdByEmail", email);
        convData.put("lastMessage", firstMessageText.isEmpty() ? "" : firstMessageText);
        convData.put("createdAt", now);
        convData.put("lastMessageAt", now);

        convRef.set(convData)
                .addOnSuccessListener(unused -> {
                    if (firstMessageText.isEmpty()) {
                        listener.onSuccess(convId);
                        return;
                    }
                    // Lưu tin nhắn đầu tiên vào subcollection messages
                    sendMessage(convId, uid, email, firstMessageText, new OnChatActionListener() {
                        @Override
                        public void onSuccess(String msgId) {
                            listener.onSuccess(convId);
                        }

                        @Override
                        public void onError(String error) {
                            // Conversation đã tạo xong — vẫn mở màn hình chat
                            listener.onSuccess(convId);
                        }
                    });
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Gửi tin nhắn mới + cập nhật lastMessage của conversation
    // -------------------------------------------------------
    public void sendMessage(String conversationId,
                            String uid,
                            String email,
                            String text,
                            OnChatActionListener listener) {

        long now = System.currentTimeMillis();

        DocumentReference msgRef = db
                .collection(FirebaseConstants.COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .collection(FirebaseConstants.COLLECTION_MESSAGES)
                .document();

        String msgId = msgRef.getId();

        Map<String, Object> msgData = new HashMap<>();
        msgData.put("id", msgId);
        msgData.put("conversationId", conversationId);
        msgData.put("senderId", uid);
        msgData.put("senderEmail", email);
        msgData.put("text", text);
        msgData.put("createdAt", now);

        msgRef.set(msgData)
                .addOnSuccessListener(unused -> {
                    // Cập nhật lastMessage + lastMessageAt của conversation
                    Map<String, Object> update = new HashMap<>();
                    update.put("lastMessage", text);
                    update.put("lastMessageAt", now);

                    db.collection(FirebaseConstants.COLLECTION_CONVERSATIONS)
                            .document(conversationId)
                            .update(update)
                            .addOnSuccessListener(u -> listener.onSuccess(msgId))
                            .addOnFailureListener(e -> listener.onSuccess(msgId)); // message đã lưu
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Load danh sách conversations (sắp xếp theo lastMessageAt giảm dần)
    // -------------------------------------------------------
    public void getConversations(OnConversationLoadedListener listener) {
        db.collection(FirebaseConstants.COLLECTION_CONVERSATIONS)
                .orderBy("lastMessageAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Conversation> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Conversation conv = doc.toObject(Conversation.class);
                        if (conv != null) {
                            list.add(conv);
                        }
                    }
                    listener.onSuccess(list);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Load messages của một conversation (sắp xếp theo createdAt tăng dần)
    // -------------------------------------------------------
    public void getMessages(String conversationId, OnMessageLoadedListener listener) {
        db.collection(FirebaseConstants.COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .collection(FirebaseConstants.COLLECTION_MESSAGES)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<ChatMessage> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        if (msg != null) {
                            list.add(msg);
                        }
                    }
                    listener.onSuccess(list);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }
}
