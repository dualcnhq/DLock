package com.dualcnhq.dlock.fragments;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dualcnhq.dlock.R;
import com.dualcnhq.dlock.activities.PostActivity;
import com.dualcnhq.dlock.data.Constants;
import com.dualcnhq.dlock.models.Post;
import com.dualcnhq.dlock.utils.AppUtils;
import com.dualcnhq.dlock.utils.ClackLocationManager;
import com.dualcnhq.dlock.utils.Observable;
import com.dualcnhq.dlock.utils.Prefs;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PostFragment extends AbsClackFragment
        implements Observable.Observer{

    private HashMap<String, ParseObject> mUpvoteRelations = new HashMap<>();
    private HashMap<String, ParseObject> mDownvoteRelations = new HashMap<>();
    private HashMap<String, ParseObject> mInappropriateRelations = new HashMap<>();
    private HashMap<String, ParseObject> mSnapRelations = new HashMap<>();
    private List<String> mCurrentPostList = new ArrayList<>();
    private Location mCurrentLocation;
    private SnapClickListener mSnapClickListener;
    private boolean mCacheShown = false;

    private ImageView mProfileImage;
    private TextView mCreditsTextView;
    private DrawerLayout mDrawerLayout;
    private ListView mNotificationList;

    private boolean mIsNotiListLoading;

    private RotateAnimation mRotateAnim;
    private String mAnimatedId;
    private boolean mIsUpdating;

    private ClackLocationManager mClackLocationManager;

    private static PostFragment instance;
    public static PostFragment getInstance() {
        if (instance == null) {
            synchronized (PostFragment.class) {
                if (instance == null) instance = new PostFragment();
            }
        }
        return instance;
    }

    public static PostFragment newInstance() {
        return new PostFragment();
    }

    @Override
    public int getFragmentId() {
        return Constants.FRAGMENT_HOME;
    }

    public PostFragment() {
        mCurrentLocation = Prefs.getInstance().getLocation();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Observable.getInstance().registerObserver(Observable.NOTIFICATION_LOCATION_CHANGED, this);
        Observable.getInstance().registerObserver(Observable.NOTIFICATION_FAILED_TO_GET_LOCATION, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Observable.getInstance().unregisterObserver(Observable.NOTIFICATION_LOCATION_CHANGED, this);
        Observable.getInstance().unregisterObserver(Observable.NOTIFICATION_FAILED_TO_GET_LOCATION, this);
        if (mCurrentLocation != null) {
            Prefs.getInstance().setLocation(mCurrentLocation);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_post;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mClackLocationManager = ClackLocationManager.getInstance(getActivity());

        //mRotateAnim = (RotateAnimation) AnimationUtils.loadAnimation(getActivity(), R.anim.counter_rotation);

        Location detectedLoc = mClackLocationManager.getCurrentLocation();
        if (detectedLoc != null) {
            mCurrentLocation = detectedLoc;
        }

        //mVoteClickListener = new VoteClickListener();
        //mSnapClickListener = new SnapClickListener();

        View v = super.onCreateView(inflater, container, savedInstanceState);

        ParseUser user = ParseUser.getCurrentUser();

        //add post
        FloatingActionButton newClackBtn = (FloatingActionButton)  v.findViewById(R.id.new_clack);
        newClackBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Location myLoc = mClackLocationManager.getCurrentLocation();
                if (myLoc == null) {
                    AppUtils.showToast(getActivity(), getResources().getString(R.string.location_not_detected));
                    return;
                }
                Intent intent = new Intent(getActivity().getApplicationContext(), PostActivity.class);
                intent.putExtra(Constants.LOCATION, myLoc);
                startActivity(intent);
            }
        });

        return v;
    }

    //swipe-to-refresh callback
    @Override
    public void onRefresh() {
        if (mCurrentLocation == null) {
            stopRefreshingAnim();
            AppUtils.showToast(getActivity(), getResources().getString(R.string.location_not_detected));
            if (!mClackLocationManager.isConnecting()) {
                mClackLocationManager.connect();
            }
            return;
        }
        super.onRefresh();
    }

    @Override
    protected void resumeActions() {
        if (mCurrentLocation == null && !mClackLocationManager.isConnecting()) {
            mClackLocationManager.connect();
        }
        if (mCurrentLocation != null || !mClackLocationManager.isFailed()) {
            super.resumeActions();
        }
    }

    protected void loadPosts() {
        if (mCurrentLocation != null) {
            mIsUpdating = true;
            super.loadPosts();
        }
    }

    @Override
    protected String getEmptyText(){
        return getActivity().getResources().getString(R.string.text_no_post_items);
    }

    public void notify(int notification, Bundle data) {
        switch (notification) {
            case Observable.NOTIFICATION_LOCATION_CHANGED:
                mCurrentLocation = data.getParcelable(Constants.LOCATION);
                if (isAdded()) {
                    loadPosts();
                }
                break;
            case Observable.NOTIFICATION_FAILED_TO_GET_LOCATION:
                hideProgress();
                break;
            case Observable.NOTIFICATION_USER_KARMA_CHANGED:
                ParseObject user = ParseUser.getCurrentUser();
                if (user != null) {
                    ParseObject credits = user.getParseObject(Constants.USER_CREDITS);
                    if (credits != null) {
                        mCreditsTextView.setText(String.valueOf(credits.getInt(Constants.USER_CREDITS)));
                    }
                }
                break;
        }
    }

    @Override
    public boolean goBack() {
        if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
            return true;
        }
        return false;
    }

    protected void setToolBar(Toolbar toolbar) {
        if (toolbar == null) {
            return;
        }
        toolbar.setVisibility(View.VISIBLE);
    }

    protected boolean hasBackButton() {
        return false;
    }

    @Override
    protected ParseQuery<ParseObject> getQuery() {
        ParseQuery<ParseObject> query;

        if (!mCacheShown) {
            ArrayList<String> cachedClackIds = Prefs.getInstance().getCachedClacks();
            if (cachedClackIds != null && cachedClackIds.size() > 0) {
                query = getPostCacheQuery(cachedClackIds);
                return query;
            }
        }

        query = Post.getQuery().
                include(Constants.USER)
                .orderByDescending(Constants.CREATED_AT)
                //.orderByDescending(Constants.COUNT)
                //.addDescendingOrder(Constants.CREATED_AT)
                .whereEqualTo(Constants.DELETED, false)
                .whereWithinKilometers(Constants.LOCATION, AppUtils.geoPointFromLocation(mCurrentLocation), AppUtils.RADIUS)
                .setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        if (AppUtils.SHOW_POSTS_WITHIN_EXPIRY_TIME) {
            long postTime = System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(AppUtils.POST_EXPIRY_TIME_HOURS, TimeUnit.HOURS);
            query.whereGreaterThan(Constants.CREATED_AT, new Date(postTime));
        }
        Prefs.getInstance().setLocation(mCurrentLocation);
        return query;
    }

    private boolean hasRelationToPost(Post post, HashMap<String, ParseObject> cache) {
        return cache != null && cache.containsKey(post.getObjectId());
    }

//    @Override
//    protected void prepareClacksOptionsMenu(Post post, PopupMenu popupMenu) {
//        if (hasRelationToPost(post, mInappropriateRelations)) {
//            popupMenu.getMenu().removeItem(R.id.action_inappropriate);
//        } else {
//            popupMenu.getMenu().removeItem(R.id.action_undo_inappropriate);
//        }
//
//        if (hasRelationToPost(post, mSnapRelations)) {
//            popupMenu.getMenu().removeItem(R.id.action_saves);
//        }
//    }

    @Override
    protected void onClacksLoaded(List list) {

        if (!mCacheShown) {
            mCacheShown = true;
            loadPosts();
            return;
        }

        getUserRelationsInBackground(list);
        if (list != null && list.size() > 0) {
            ArrayList<String> cacheIds = new ArrayList<>(list.size());
            for (ParseObject obj : (List<ParseObject>) list) {
                cacheIds.add(obj.getObjectId());
            }
            //cache clacks ids in shared prefs
            Prefs.getInstance().setCachedClacks(cacheIds);
            //call caching query
            cachePostQuery(cacheIds);
        }
    }

    private ParseQuery<ParseObject> getPostCacheQuery(List list) {
        return Post.getQuery()
                .include(Constants.USER)
                .whereContainedIn(Constants.OBJECT_ID, list)
                .setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
    }

    private void cachePostQuery(List list) {
        ParseQuery<ParseObject> query = getPostCacheQuery(list);
        query.findInBackground();
    }

    protected void optionSelected(Post post, int optionId) {
//        switch (optionId) {
//            case R.id.action_delete:
//                ClackUtils.deletePost(post, new SaveCallback() {
//                    @Override
//                    public void done(ParseException e) {
//                        if (e == null) {
//                            AppUtils.showToast(getActivity(), R.string.delete_post);
//                            loadPosts();
//                        } else {
//                            AppUtils.showToast(getActivity(), R.string.delete_post_failed);
//                        }
//                    }
//                });
//                break;
//            case R.id.action_edit:
//                AppUtils.editPost(getActivity(), post);
//                break;
//            case R.id.action_inappropriate:
//                putRelation(mInappropriateRelations, Constants.RELATION_TYPE_REPORT_INAPPROPRIATE, post, true);
//                post.incrementInappropriate(1);
//                post.saveInBackground();
//                notifyDataSetChanged();
//                break;
//            case R.id.action_undo_inappropriate:
//                putRelation(mInappropriateRelations, Constants.RELATION_TYPE_REPORT_INAPPROPRIATE, post, false);
//                post.incrementInappropriate(-1);
//                post.saveInBackground();
//                notifyDataSetChanged();
//                break;
//            case R.id.action_saves:
//                putRelation(mSnapRelations, Constants.RELATION_TYPE_SAVES, post, true);
//                notifyDataSetChanged();
//                break;
//            case R.id.action_share:
//                AppUtils.shareVia(getActivity(), post.getPost());
//                break;
//            default:
//                super.optionSelected(post, optionId);
//                break;
//        }
    }

    private void putRelation(HashMap<String, ParseObject> relCache, int relType, Post post, boolean save) {
        String postId = post.getObjectId();
        if (save) {
            if (relCache.containsKey(postId)) {
                return;
            }
            ParseObject rel = new ParseObject(Constants.OBJECT_TYPE_RELATION);
            rel.put(Constants.USER, ParseUser.getCurrentUser());
            rel.put(Constants.POST, post.getObject());
            rel.put(Constants.RELATION_TYPE, relType);
            rel.saveInBackground();
            relCache.put(postId, rel);

        } else {
            if (!relCache.containsKey(postId)) {
                return;
            }
            ParseObject rel = relCache.get(postId);
            rel.deleteInBackground();
            relCache.remove(postId);
        }
    }

    private class SnapClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mSnapRelations == null) {
                return;
            }
            Post post = (Post) v.getTag();
            boolean add = !mSnapRelations.containsKey(post.getObjectId());
            putRelation(mSnapRelations, Constants.RELATION_TYPE_SAVES, post, add);
            notifyDataSetChanged();
        }
    }
//
//    private class VoteClickListener implements View.OnClickListener {
//        @Override
//        public void onClick(View v) {
//            Post post = (Post) v.getTag();
//            vote(post, v.getId() == R.id.upvote);
//        }
//    }

//    @Override
//    protected int getClackOptionsMenu(Post post) {
//        if (AppUtils.isMyPost(post)) {
//            return R.menu.my_clack_options;
//        } else {
//            return R.menu.clack_options;
//        }
//    }

    protected void bindClackView(final ParseObject object, final ClacksAdapter.ViewHolder holder) {
        final Post post = new Post(object);
        holder.userIcon.setImageResource(R.drawable.ic_no_avatar);
        ParseUser user = post.getUser();
        if (user != null) {
            holder.userName.setText(user.getUsername());
        }
        holder.textPost.setText(post.getPost());

        getPlaceDescription(post, holder.place);

        holder.timeText.setText(AppUtils.getTimeDiff(post.getCreatedAt()));

        //inappropriate label
        //holder.inapprLabel.setVisibility(post.getInappropriateCount() == 0 ? View.INVISIBLE : View.VISIBLE);

        String objectId = post.getObjectId();
        boolean relVisible = mCurrentPostList.contains(objectId);
        boolean allowAddRel = !mIsUpdating && relVisible;

        //local menu button
//        setLocalMenuButton(holder.localMenuButton, post);
//        holder.localMenuButton.setVisibility(relVisible ? View.VISIBLE : View.INVISIBLE);

        //Snap button
//        holder.snapButton.setEnabled(allowAddRel);
//        holder.snapButton.setVisibility(relVisible ? View.VISIBLE : View.INVISIBLE);
//        holder.snapButton.setImageResource(mSnapRelations == null || !mSnapRelations.containsKey(post.getObjectId())
//                ? R.drawable.snap : R.drawable.snap_active);
//        holder.snapButton.setTag(post);
//        holder.snapButton.setOnClickListener(mSnapClickListener);

        //upvote/downvote buttons
//        if (AppUtils.isMyPost(post)) {
//            holder.upvoteButton.setVisibility(View.INVISIBLE);
//            holder.downButton.setVisibility(View.INVISIBLE);
//        } else {
//            holder.upvoteButton.setTag(post);
//            holder.upvoteButton.setOnClickListener(mVoteClickListener);
//            holder.upvoteButton.setEnabled(allowAddRel);
//            holder.upvoteButton.setVisibility(relVisible ? View.VISIBLE : View.INVISIBLE);
//            holder.upvoteButton.setImageResource(!mUpvoteRelations.containsKey(objectId)
//                    ? R.drawable.homescreen_thumbs_up : R.drawable.homescreen_thumbs_up_active);
//
//            holder.downButton.setTag(post);
//            holder.downButton.setOnClickListener(mVoteClickListener);
//            holder.downButton.setEnabled(allowAddRel);
//            holder.downButton.setVisibility(relVisible ? View.VISIBLE : View.INVISIBLE);
//            holder.downButton.setImageResource(!mDownvoteRelations.containsKey(objectId)
//                    ? R.drawable.homescreen_thumbs_down : R.drawable.homescreen_thumbs_down_active);
//        }
//
//        ClackUtils.loadUserPhoto(getActivity().getApplicationContext(), user, holder.userIcon,
//                (int) getResources().getDimension(R.dimen.ab_profile_image_size));
    }

//    private void vote(final Post post, final boolean like) {
//
//        if (mCurrentPostList.isEmpty()) {
//            return;
//        }
//
//        String postId = post.getObjectId();
//        HashMap<String, ParseObject> voteCache = like ? mUpvoteRelations : mDownvoteRelations;
//        HashMap<String, ParseObject> oppositeVoteCache = like ? mDownvoteRelations : mUpvoteRelations;
//        int increment = 0;
//
//        if (voteCache.containsKey(postId)) {
//            ParseObject vote = voteCache.get(postId);
//            vote.deleteInBackground();
//            voteCache.remove(postId);
//            increment += like ? -1 : 1;
//        } else {
//            if (oppositeVoteCache.containsKey(postId)) {
//                ParseObject vote = oppositeVoteCache.get(postId);
//                vote.deleteInBackground();
//                oppositeVoteCache.remove(postId);
//                increment += like ? 1 : -1;
//            }
//            ParseObject rel = new ParseObject(Constants.OBJECT_TYPE_RELATION);
//            rel.put(Constants.USER, ParseUser.getCurrentUser());
//            rel.put(Constants.POST, post.getObject());
//            rel.put(Constants.RELATION_TYPE, like ? Constants.RELATION_TYPE_UPVOTE : Constants.RELATION_TYPE_DOWNVOTE);
//            rel.saveInBackground();
//            increment += like ? 1 : -1;
//            voteCache.put(postId, rel);
//
//            //save relation to notification table
//            ClackUtils.saveNotification(getActivity(), post, rel.getInt(Constants.RELATION_TYPE));
//        }
//
//        post.incrementCounter(increment);
//        //check if need to autodelete
//        if (post.getCount() <= AppUtils.POST_VOTES_FOR_AUTODELETE) {
//            post.setDeleted(true);
//            AppUtils.showToast(getActivity(), R.string.clack_auto_deleted_low_rating);
//            loadPosts();
//        }
//        mAnimatedId = post.getObjectId();
//        notifyDataSetChanged();
//        post.saveInBackground();
//
//        //increment user karma
//        final ParseUser user = post.getUser();
//        ClackUtils.incrementUserCredits(user, like ? Constants.USER_KARMA_FOR_UPVOTE : Constants.USER_KARMA_FOR_DOWNVOTE, null);
//    }

    private void getUserRelationsInBackground(final List<ParseObject> postList) {

        if (postList == null || postList.size() == 0) {
            return;
        }
        //notifyDataSetChanged();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.OBJECT_TYPE_RELATION);
        query.whereEqualTo(Constants.USER, ParseUser.getCurrentUser());
        query.whereContainedIn(Constants.POST, postList);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> relList, ParseException e) {

                mCurrentPostList.clear();
                for (ParseObject post : postList) {
                    mCurrentPostList.add(post.getObjectId());
                }

                mUpvoteRelations.clear();
                mDownvoteRelations.clear();
                mInappropriateRelations.clear();
                mSnapRelations.clear();
                if (relList != null) {
                    for (ParseObject rel : relList) {
                        String objId = rel.getParseObject(Constants.POST).getObjectId();
                        switch (rel.getInt(Constants.RELATION_TYPE)) {
                            case Constants.RELATION_TYPE_UPVOTE:
                                mUpvoteRelations.put(objId, rel);
                                break;
                            case Constants.RELATION_TYPE_DOWNVOTE:
                                mDownvoteRelations.put(objId, rel);
                                break;
                            case Constants.RELATION_TYPE_REPORT_INAPPROPRIATE:
                                mInappropriateRelations.put(objId, rel);
                                break;
                            case Constants.RELATION_TYPE_SAVES:
                                mSnapRelations.put(objId, rel);
                                break;
                        }
                    }
                }
                mIsUpdating = false;
                notifyDataSetChanged();
            }
        });
    }

}
