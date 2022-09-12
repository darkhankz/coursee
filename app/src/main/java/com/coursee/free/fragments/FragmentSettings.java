package com.coursee.free.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.coursee.free.BuildConfig;
import com.coursee.free.R;
import com.coursee.free.activities.ActivityPrivacyPolicy;
import com.coursee.free.activities.MainActivity;
import com.coursee.free.utils.SharedPref;

import static com.coursee.free.utils.Constant.CATEGORY_GRID_2_COLUMN;
import static com.coursee.free.utils.Constant.CATEGORY_GRID_3_COLUMN;
import static com.coursee.free.utils.Constant.CATEGORY_LIST;
import static com.coursee.free.utils.Constant.VIDEO_LIST_COMPACT;
import static com.coursee.free.utils.Constant.VIDEO_LIST_DEFAULT;

public class FragmentSettings extends Fragment {

    View root_view, parent_view;
    SharedPref sharedPref;
    Switch switch_theme;
    Switch switch_list;
    TextView txt_current_video_list;
    TextView txt_current_category_list;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_settings, container, false);
        parent_view = getActivity().findViewById(R.id.lyt_content);

        sharedPref = new SharedPref(getActivity());

        initComponent();

        return root_view;
    }

    private void initComponent() {

        txt_current_video_list = root_view.findViewById(R.id.txt_current_video_list);
        if (sharedPref.getVideoViewType() == VIDEO_LIST_DEFAULT) {
            txt_current_video_list.setText(getResources().getString(R.string.single_choice_default));
        } else if (sharedPref.getVideoViewType() == VIDEO_LIST_COMPACT) {
            txt_current_video_list.setText(getResources().getString(R.string.single_choice_compact));
        }

        txt_current_category_list = root_view.findViewById(R.id.txt_current_category_list);
        if (sharedPref.getCategoryViewType() == CATEGORY_LIST) {
            txt_current_category_list.setText(getResources().getString(R.string.single_choice_list));
        } else if (sharedPref.getCategoryViewType() == CATEGORY_GRID_2_COLUMN) {
            txt_current_category_list.setText(getResources().getString(R.string.single_choice_grid_2));
        } else if (sharedPref.getCategoryViewType() == CATEGORY_GRID_3_COLUMN) {
            txt_current_category_list.setText(getResources().getString(R.string.single_choice_grid_3));
        }

        onThemeChanged();
        //onCompactListChanged();
        changeVideoListViewType();
        changeCategoryListViewType();

        root_view.findViewById(R.id.btn_privacy_policy).setOnClickListener(view -> startActivity(new Intent(getActivity(), ActivityPrivacyPolicy.class)));
        root_view.findViewById(R.id.btn_rate).setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID))));
        root_view.findViewById(R.id.btn_more).setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps)))));
        root_view.findViewById(R.id.btn_about).setOnClickListener(view -> aboutDialog());
    }

    private void onThemeChanged() {
        switch_theme = root_view.findViewById(R.id.switch_theme);
        if (sharedPref.getIsDarkTheme()) {
            switch_theme.setChecked(true);
        } else {
            switch_theme.setChecked(false);
        }
        switch_theme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPref.setIsDarkTheme(isChecked);
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void changeVideoListViewType() {

        root_view.findViewById(R.id.btn_switch_list).setOnClickListener(view -> {
            String[] items = getResources().getStringArray(R.array.dialog_video_list);
            int itemSelected = sharedPref.getVideoViewType();
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_setting_list)
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, position) -> {
                        sharedPref.updateVideoViewType(position);

                        if (position == 0) {
                            txt_current_video_list.setText(getResources().getString(R.string.single_choice_default));
                        } else if (position == 1) {
                            txt_current_video_list.setText(getResources().getString(R.string.single_choice_compact));
                        }

                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        dialogInterface.dismiss();
                    })
                    .show();
        });
    }

    private void changeCategoryListViewType() {

        root_view.findViewById(R.id.btn_switch_category).setOnClickListener(view -> {
            String[] items = getResources().getStringArray(R.array.dialog_category_list);
            int itemSelected = sharedPref.getCategoryViewType();
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_setting_category)
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, position) -> {
                        sharedPref.updateCategoryViewType(position);

                        if (position == 0) {
                            txt_current_category_list.setText(getResources().getString(R.string.single_choice_list));
                        } else if (position == 1) {
                            txt_current_category_list.setText(getResources().getString(R.string.single_choice_grid_2));
                        } else if (position == 2) {
                            txt_current_category_list.setText(getResources().getString(R.string.single_choice_grid_3));
                        }

                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("category_position", "category_position");
                        startActivity(intent);

                        dialogInterface.dismiss();
                    })
                    .show();
        });
    }

    public void aboutDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity());
        View view = layoutInflaterAndroid.inflate(R.layout.custom_dialog_about, null);
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setView(view);
        alert.setCancelable(false);
        alert.setPositiveButton(R.string.dialog_ok, (dialog, which) -> dialog.dismiss());
        alert.show();
    }

}