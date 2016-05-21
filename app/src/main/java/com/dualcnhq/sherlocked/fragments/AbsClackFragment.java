package com.dualcnhq.sherlocked.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dualcnhq.sherlocked.R;
import com.dualcnhq.sherlocked.models.Post;
import com.dualcnhq.sherlocked.utils.AppUtils;
import com.dualcnhq.sherlocked.views.RoundedImageView;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public abstract class AbsClackFragment extends AbsCommonFragment
    implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = AbsClackFragment.class.getSimpleName();

    private ClacksAdapter mClacksAdapter;
    private ListView mClacksList;
    private TextView emptyText;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;
    private LocalMenuClickListener mLocalMenuClickListener;

    //private HashMap<String, String> mClacksLocatiliesCache = new HashMap<>();
    private LruCache mLocationCache;

    public AbsClackFragment() {
        int cacheSize = 1024 * 1024;
        mLocationCache = new LruCache(cacheSize) {
            protected int sizeOf(String key, String value) {
                return value.length() * 2;
            }};
    }

    protected abstract ParseQuery<ParseObject> getQuery();

    protected int getLayoutId() {
        return R.layout.fragment_abs_clack;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);

        mProgressBar = (ProgressBar) view.findViewById(R.id.loading_progress);

        //init clack list adapter
        ParseQueryAdapter.QueryFactory<ParseObject> factory =
                new ParseQueryAdapter.QueryFactory<ParseObject>() {
                    public ParseQuery<ParseObject> create() {
                        return getQuery();
                    }
                };

        if (mClacksAdapter == null) {
            mClacksAdapter = new ClacksAdapter(getActivity(), factory);
            mClacksAdapter.setAutoload(false);
            mClacksAdapter.addOnQueryLoadListener(new ClackLoadedListener());
        }
        mClacksList = (ListView) view.findViewById(R.id.clacks_list_view);
        mClacksList.setAdapter(mClacksAdapter);

        emptyText = (TextView) view.findViewById(R.id.empty_list_item);

        //swipe to refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mLocalMenuClickListener = new LocalMenuClickListener();
        return view;
    }

    protected void setLocalMenuButton(View v, Post post) {
        v.setTag(post);
        v.setOnClickListener(mLocalMenuClickListener);
    }

    private class ClackLoadedListener implements ParseQueryAdapter.OnQueryLoadListener<ParseObject> {
        @Override
        public void onLoading() {
        }

        @Override
        public void onLoaded(List list, Exception e) {
            if (getActivity() == null) {
                return;
            }
            hideProgress();
            mSwipeRefreshLayout.setRefreshing(false);
            //mClacksLocatiliesCache.clear();
            if (e != null) {
                if (!e.getMessage().equals("results not cached")) {
                    AppUtils.showToast(getActivity(), getResources().getString(R.string.loading_post_failed) + ": " + e.getMessage());
                }
            } else {
                if (AppUtils.DBG) {
                    AppUtils.showToast(getActivity(), getResources().getString(R.string.post_list_updated));
                }
            }
            onClacksLoaded(list);

            emptyText.setText(getEmptyText());
            mClacksList.setEmptyView(emptyText);
        }
    }

    protected void onClacksLoaded(List list) {
    }

    protected abstract String getEmptyText();

    protected void showOptionsMenu(final Post post, View v) {
        if (getClackOptionsMenu(post) == -1) {
            return;
        }
        PopupMenu popupMenu = new PopupMenu(getActivity(), v) {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                optionSelected(post, item.getItemId());
                return true;
            }
        };

        popupMenu.inflate(getClackOptionsMenu(post));
        prepareClacksOptionsMenu(post, popupMenu);
        popupMenu.show();
    }

    protected int getClackOptionsMenu(Post post) {
        return -1;
    }

    protected void prepareClacksOptionsMenu(Post post, PopupMenu popupMenu) {
    }

    protected void optionSelected(Post post, int optionId) {
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeActions();
    }

    protected void resumeActions() {
        showProgress();
        loadPosts();
    }

    protected void stopRefreshingAnim() {
        //mSwipeRefreshLayout.setRefreshing(false);
    }

    //swipe-to-refresh callback
    @Override
    public void onRefresh() {
        loadPosts();
    }

    protected void loadPosts() {
        if (getActivity() != null && !AppUtils.isInternetOn(getActivity())) {
            hideProgress();
            AppUtils.showToast(getActivity(), getResources().getString(R.string.network_disabled));
            stopRefreshingAnim();
            return;
        }
        if (AppUtils.DBG) {
            //AppUtils.showToast(getActivity(), getResources().getString(R.string.clacks_updating));
            Log.d(TAG, getResources().getString(R.string.clacks_updating));
        }
        mClacksAdapter.loadObjects();
    }

    protected void showProgress() {
        if (mClacksAdapter.getCount() == 0) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    protected void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    protected void getPlaceDescription(final Post post, final TextView place) {
        if (mLocationCache.get(post.getObjectId()) != null) {
            place.setText((String) mLocationCache.get(post.getObjectId()));
        } else {
            place.setText("...");
            place.setTag(post.getObjectId());
            final ParseGeoPoint latLng = post.getLocation();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Geocoder gcd = new Geocoder(getActivity(), Locale.getDefault());
                    try {
                        final List<Address> addresses = gcd.getFromLocation(latLng.getLatitude(), latLng.getLongitude(), 1);
                        if (addresses != null && addresses.size() > 0) {
                            String locality = addresses.get(0).getLocality();
                            if (locality == null) {
                                locality = addresses.get(0).getFeatureName();
                            }
                            if (locality == null) {
                                locality = addresses.get(0).getAdminArea();
                            }
                            String address = addresses.get(0).getThoroughfare();
                            if (address == null) {
                                address = addresses.get(0).getSubLocality();
                            }
                            final String locationDescription = (address == null ? "" : address + ", ") + locality;
                            mLocationCache.put(post.getObjectId(), locationDescription);
                            place.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (post.getObjectId().equals(place.getTag())) {
                                        place.setText(locationDescription);
                                    }
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    protected abstract void bindClackView(final ParseObject object, final ClacksAdapter.ViewHolder holder);

    protected class ClacksAdapter extends ParseQueryAdapter<ParseObject> {

        Typeface tf;
        public ClacksAdapter(Context context, QueryFactory<ParseObject> queryFactory) {
            super(context, queryFactory);
            tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Medium.ttf");
        }

        @Override
        public View getItemView(final ParseObject object, View view, ViewGroup parent) {

            ViewHolder holder;
            if (view == null) {
                view = View.inflate(getActivity(), R.layout.layout_clack_list_item, null);
                holder = new ViewHolder(view);
                holder.userName.setTypeface(tf, Typeface.BOLD);
                holder.timeText.setTypeface(tf);
                holder.place.setTypeface(tf, Typeface.ITALIC);
                holder.textPost.setTypeface(tf);
//                holder.count.setTypeface(tf);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            bindClackView(object, holder);
            return view;
        }

        public class ViewHolder {
            final RoundedImageView userIcon;
            final TextView userName;
            final View inapprLabel;
            final TextView timeText;
            final TextView place;
            final TextView textPost;
//            final TextView count;
//            final View localMenuButton;
//            final ImageView upvoteButton;
//            final ImageView downButton;
//            final ImageView snapButton;

            public ViewHolder(View v) {
                userIcon = (RoundedImageView) v.findViewById(R.id.userIcon);
                userName = (TextView) v.findViewById(R.id.userName);
                inapprLabel = v.findViewById(R.id.inappr_label);
                timeText = (TextView) v.findViewById(R.id.timeText);
                place = (TextView) v.findViewById(R.id.place);
                textPost = (TextView) v.findViewById(R.id.postText);
//                count = (TextView) v.findViewById(R.id.countText);
//                localMenuButton = v.findViewById(R.id.replyIcon);
//                upvoteButton = (ImageView) v.findViewById(R.id.upvote);
//                downButton = (ImageView) v.findViewById(R.id.downvote);
//                snapButton = (ImageView) v.findViewById(R.id.snap);
            }

        }
    }

    private class LocalMenuClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final Post post = (Post) v.getTag();
            showOptionsMenu(post, v);
        }
    }

    protected void notifyDataSetChanged() {
        mClacksAdapter.notifyDataSetChanged();
    }

}
