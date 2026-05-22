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
 *       shopName, shopAvatarText, shopCategory, address, openTime, closeTime
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
                                   String shopName,
                                   String shopAvatarText,
                                   String shopCategory,
                                   String address,
                                   String openTime,
                                   String closeTime,
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
        convData.put("shopName", shopName);
        convData.put("shopAvatarText", shopAvatarText);
        convData.put("shopCategory", shopCategory);
        convData.put("address", address);
        convData.put("openTime", openTime);
        convData.put("closeTime", closeTime);
        convData.put("isRead", false);  // mới tạo → chưa đọc

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

    /**
     * Overload tương thích ngược (không có shop fields).
     */
    public void createConversation(String title,
                                   String uid,
                                   String email,
                                   String firstMessageText,
                                   OnChatActionListener listener) {
        createConversation(title, uid, email, firstMessageText,
                title, "💬", "general", "Chưa có địa chỉ", "18:00", "02:30", listener);
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
                            // Firebase dùng Java Bean convention: getter isRead() → field "read"
                            // nên toObject() không map đúng field "isRead" → đọc thủ công
                            Boolean readValue = doc.getBoolean("isRead");
                            conv.setRead(readValue != null && readValue);
                            list.add(conv);
                        }
                    }
                    listener.onSuccess(list);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Xóa conversation: xóa messages subcollection trước, rồi xóa document
    // -------------------------------------------------------
    public void deleteConversation(String conversationId, OnChatActionListener listener) {
        // Bước 1: lấy tất cả messages
        db.collection(FirebaseConstants.COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .collection(FirebaseConstants.COLLECTION_MESSAGES)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Bước 2: xóa từng message document
                    if (querySnapshot.isEmpty()) {
                        // Không có messages, xóa thẳng conversation
                        deleteConversationDocument(conversationId, listener);
                        return;
                    }

                    final int[] remaining = {querySnapshot.size()};
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete()
                                .addOnCompleteListener(task -> {
                                    remaining[0]--;
                                    if (remaining[0] == 0) {
                                        // Xóa xong tất cả messages → xóa conversation
                                        deleteConversationDocument(conversationId, listener);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Nếu lấy messages lỗi, vẫn thử xóa conversation
                    deleteConversationDocument(conversationId, listener);
                });
    }

    private void deleteConversationDocument(String conversationId, OnChatActionListener listener) {
        db.collection(FirebaseConstants.COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .delete()
                .addOnSuccessListener(unused -> listener.onSuccess(conversationId))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Cập nhật conversation (chỉ update các field cho phép sửa)
    // -------------------------------------------------------
    public void updateConversation(String conversationId, Map<String, Object> updates,
                                   OnChatActionListener listener) {
        db.collection(FirebaseConstants.COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .update(updates)
                .addOnSuccessListener(unused -> listener.onSuccess(conversationId))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // -------------------------------------------------------
    // Đánh dấu conversation đã đọc
    // -------------------------------------------------------
    public void markAsRead(String conversationId) {
        Map<String, Object> update = new HashMap<>();
        update.put("isRead", true);
        db.collection(FirebaseConstants.COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .update(update);
        // Không cần callback — fire-and-forget
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
