package com.example.nightlife_finder.interfaces;

import com.example.nightlife_finder.models.Conversation;

import java.util.List;

/**
 * OnConversationLoadedListener – callback cho thao tác load danh sách conversation.
 */
public interface OnConversationLoadedListener {
    void onSuccess(List<Conversation> conversations);
    void onError(String error);
}
