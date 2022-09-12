package com.coursee.free.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coursee.free.R;
import com.coursee.free.activities.ActivityVideoDetail;
import com.coursee.free.activities.ActivityVideoDetailOffline;
import com.coursee.free.adapters.AdapterFavorite;
import com.coursee.free.databases.DatabaseHandlerFavorite;
import com.coursee.free.models.Video;
import com.coursee.free.utils.Constant;
import com.coursee.free.utils.SharedPref;
import com.coursee.free.utils.Tools;

import java.util.ArrayList;
import java.util.List;

import static com.coursee.free.utils.Constant.VIDEO_LIST_COMPACT;

public class FragmentFavorite extends Fragment {

    private List<Video> data = new ArrayList<>();
    View root_view, parent_view;
    AdapterFavorite mAdapterFavorite;
    DatabaseHandlerFavorite databaseHandler;
    RecyclerView recyclerView;
    LinearLayout linearLayout;
    private CharSequence charSequence = null;
    SharedPref sharedPref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_favorite, container, false);
        parent_view = getActivity().findViewById(R.id.lyt_content);

        sharedPref = new SharedPref(getActivity());

        linearLayout = root_view.findViewById(R.id.lyt_no_favorite);
        recyclerView = root_view.findViewById(R.id.recyclerView);
        if (sharedPref.getVideoViewType() == VIDEO_LIST_COMPACT) {
            recyclerView.setPadding(0, getResources().getDimensionPixelOffset(R.dimen.spacing_small), 0, 0);
        }

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        databaseHandler = new DatabaseHandlerFavorite(getActivity());
        data = databaseHandler.getAllData();

        mAdapterFavorite = new AdapterFavorite(getActivity(), recyclerView, data);
        recyclerView.setAdapter(mAdapterFavorite);

        if (data.size() == 0) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.INVISIBLE);
        }

        return root_view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {

        super.onResume();

        data = databaseHandler.getAllData();
        mAdapterFavorite = new AdapterFavorite(getActivity(), recyclerView, data);
        recyclerView.setAdapter(mAdapterFavorite);

        mAdapterFavorite.setOnItemClickListener((v, obj, position) -> {

            if (Tools.isConnect(getActivity())) {
                Intent intent = new Intent(getActivity(), ActivityVideoDetail.class);
                intent.putExtra(Constant.EXTRA_OBJC, obj);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getActivity(), ActivityVideoDetailOffline.class);
                intent.putExtra(Constant.POSITION, position);
                intent.putExtra(Constant.KEY_VIDEO_CATEGORY_ID, obj.cat_id);
                intent.putExtra(Constant.KEY_VIDEO_CATEGORY_NAME, obj.category_name);
                intent.putExtra(Constant.KEY_VID, obj.vid);
                intent.putExtra(Constant.KEY_VIDEO_TITLE, obj.video_title);
                intent.putExtra(Constant.KEY_VIDEO_URL, obj.video_url);
                intent.putExtra(Constant.KEY_VIDEO_ID, obj.video_id);
                intent.putExtra(Constant.KEY_VIDEO_THUMBNAIL, obj.video_thumbnail);
                intent.putExtra(Constant.KEY_VIDEO_DURATION, obj.video_duration);
                intent.putExtra(Constant.KEY_VIDEO_DESCRIPTION, obj.video_description);
                intent.putExtra(Constant.KEY_VIDEO_TYPE, obj.video_type);
                intent.putExtra(Constant.KEY_TOTAL_VIEWS, obj.total_views);
                intent.putExtra(Constant.KEY_DATE_TIME, obj.date_time);
                startActivity(intent);
            }
        });

        mAdapterFavorite.setOnItemOverflowClickListener((v, obj, position) -> {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_popup, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menu_context_favorite:

                        if (charSequence.equals(getString(R.string.favorite_add))) {
                            databaseHandler.AddtoFavorite(new Video(
                                    obj.category_name,
                                    obj.vid,
                                    obj.video_title,
                                    obj.video_url,
                                    obj.video_id,
                                    obj.video_thumbnail,
                                    obj.video_duration,
                                    obj.video_description,
                                    obj.video_type,
                                    obj.total_views,
                                    obj.date_time
                            ));
                            Toast.makeText(getActivity(), getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();

                        } else if (charSequence.equals(getString(R.string.favorite_remove))) {
                            databaseHandler.RemoveFav(new Video(obj.vid));
                            Toast.makeText(getActivity(), getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
                            refreshFragment();
                        }

                        return true;

                    case R.id.menu_context_share:

                        String share_title = android.text.Html.fromHtml(obj.video_title).toString();
                        String share_content = android.text.Html.fromHtml(getResources().getString(R.string.share_text)).toString();
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, share_title + "\n\n" + share_content + "\n\n" + "https://play.google.com/store/apps/details?id=" + getActivity().getPackageName());
                        sendIntent.setType("text/plain");
                        startActivity(sendIntent);
                        return true;

                    default:
                }
                return false;
            });
            popup.show();

            databaseHandler = new DatabaseHandlerFavorite(getActivity());
            List<Video> data = databaseHandler.getFavRow(obj.vid);
            if (data.size() == 0) {
                popup.getMenu().findItem(R.id.menu_context_favorite).setTitle(R.string.favorite_add);
                charSequence = popup.getMenu().findItem(R.id.menu_context_favorite).getTitle();
            } else {
                if (data.get(0).getVid().equals(obj.vid)) {
                    popup.getMenu().findItem(R.id.menu_context_favorite).setTitle(R.string.favorite_remove);
                    charSequence = popup.getMenu().findItem(R.id.menu_context_favorite).getTitle();
                }
            }

        });

        if (data.size() == 0) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.INVISIBLE);
        }
    }

    public void refreshFragment() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.detach(this).attach(this).commit();
    }

}
