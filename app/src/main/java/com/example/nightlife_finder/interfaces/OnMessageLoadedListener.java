package com.example.nightlife_finder.interfaces;

import com.example.nightlife_finder.models.ChatMessage;

import java.util.List;

/**
 * OnMessageLoadedListener – callback cho thao tác load danh sách tin nhắn.
 */
public interface OnMessageLoadedListener {
    void onSuccess(List<ChatMessage> messages);
    void onError(String error);
}
