package com.example.nightlife_finder.fragments;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.example.nightlife_finder.utils.LocaleHelper;

/**
 * BaseFragment – Áp dụng ngôn ngữ / locale cho mọi Fragment trong app.
 * Mọi Fragment cụ thể nên kế thừa từ class này thay vì Fragment gốc.
 */
public class BaseFragment extends Fragment {

    @Override
    public void onAttach(Context context) {
        // Áp dụng locale trước khi fragment được gắn vào context
        super.onAttach(LocaleHelper.applyLocale(context));
    }
}