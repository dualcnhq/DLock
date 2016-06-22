package com.dualcnhq.dlock.utils;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import com.dualcnhq.dlock.data.Constants;
import com.dualcnhq.dlock.models.Post;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ClackUtils {

    private static final String TAG = ClackUtils.class.getSimpleName();

    public static void loadUserPhoto(Context context, ParseUser user, ImageView userImage, int size) {
        if (AppUtils.DBG) {
            Log.d(TAG, "loadUserPhoto");
        }
        if (user == null) {
            return;
        }

        ParseFile file = (ParseFile) user.get(Constants.USER_PHOTO);
        if (file == null) {
            return;
        }
        if (AppUtils.DBG) {
            Log.d(TAG, "file: " + file.getUrl());
        }

//        Picasso.with(context)
//                .load(file.getUrl())
//                .centerCrop()
//                .error(R.drawable.ic_no_avatar)
//                .placeholder(R.drawable.ic_no_avatar)
//                .resize(size, size)
//                .into(userImage);
    }

    public static void incrementUserCredits(final ParseUser user, final int inc, final Runnable doneCallback) {

        if (!user.has(Constants.USER_CREDITS)) {
            return;
        }
        ParseObject credits = user.getParseObject(Constants.USER_CREDITS);
        credits.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                object.increment(Constants.USER_CREDITS, inc);
                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            if (doneCallback != null) {
                                doneCallback.run();
                            }
                        }
                    }
                });
            }
        });
    }

//    public static void getUserCreditsToTextView(final TextView creditsTextView) {
//        if (creditsTextView == null) {
//            return;
//        }
//        ParseUser user = ParseUser.getCurrentUser();
//        if (user.has(Constants.USER_CREDITS)) {
//            ParseObject credits = user.getParseObject(Constants.USER_CREDITS);
//            credits.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
//                @Override
//                public void done(ParseObject object, ParseException e) {
//                    if (e == null && object != null) {
//                        creditsTextView.setText(String.valueOf(object.getInt(Constants.USER_CREDITS)));
//                    }
//                }
//            });
//        } else {
//            ParseObject credits = new ParseObject(Constants.OBJECT_TYPE_CREDITS);
//            credits.put(Constants.USER_CREDITS, Constants.USER_INITIAL_KARMA);
//            credits.saveInBackground();
//            user.put(Constants.USER_CREDITS, credits);
//            user.saveInBackground();
//            creditsTextView.setText(String.valueOf(Constants.USER_INITIAL_KARMA));
//        }
//    }

    public static void subscribeForNotifications() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(Constants.USER, ParseUser.getCurrentUser());
        installation.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (AppUtils.DBG) {
                    if (e != null) {
                        Log.e(TAG, "Failed to subscribe for push notifications", e);
                    } else {
                        Log.e(TAG, "Successfully subscribed for push notifications");
                    }
                }
            }
        });
    }

    public static void unsubscribeFromNotifications() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.remove(Constants.USER);
        installation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (AppUtils.DBG) {
                    if (e != null) {
                        Log.e(TAG, "Failed to unsubscribe from push notifications", e);
                    } else {
                        Log.e(TAG, "Successfully unsubscribed from push notifications");
                    }
                }
            }
        });
    }

    public static void pushNotification(final Context context, ParseObject noti, Post post, final int pushType) {
        if (!AppUtils.ENABLE_PUSH_NOTIFICATIONS) {
            return;
        }
        ParseUser user = post.getUser();
        ParseQuery pushQuery = ParseInstallation.getQuery();
        pushQuery.whereEqualTo(Constants.USER, user);

        ParsePush push = new ParsePush();
        push.setQuery(pushQuery);
        int stringId = -1;
        switch (pushType) {
//            case Constants.PUSH_TYPE_UPVOTE:
//                stringId = R.string.push_notification_upvoted;
//                break;
//            case Constants.PUSH_TYPE_DOWNVOTE:
//                stringId = R.string.push_notification_downvoted;
//                break;
//            case Constants.PUSH_TYPE_SAVED:
//                stringId = R.string.push_notification_saved;
//                break;
//            case Constants.PUSH_TYPE_SHARED:
//                stringId = R.string.push_notification_shared;
//                break;
        }
        String message = String.format(context.getResources().getString(stringId),
                ParseUser.getCurrentUser().getUsername(), post.getPost());

        try {
            JSONObject data = new JSONObject("{\"" + Constants.NOTIFICATION_ID + "\": \""
                    + noti.getObjectId() + "\",\"alert\": \"" + message + "\"}");
            push.setData(data);
        } catch (JSONException e) {
            if (AppUtils.DBG) {
                Log.e(TAG, "Unable to send notification id", e);
            }
        }
        ;

        push.sendInBackground(new SendCallback() {
            @Override
            public void done(ParseException e) {
                if (AppUtils.DBG) {
                    if (e != null) {
                        Log.e(TAG, "Failed to send push notification for type: " + pushType, e);
                    } else {
                        Log.e(TAG, "Push notification for type: " + pushType + " successfully sent");
                    }
                }
            }
        });
    }

    public static void saveNotification(final Context context, final Post post, final int relType) {
        ParseUser user = post.getUser();
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null || currentUser.getObjectId().equals(user.getObjectId())) {
            return;
        }
        final ParseObject noti = new ParseObject(Constants.OBJECT_TYPE_NOTIFICATION);
        noti.put(Constants.USER, user);
        noti.put(Constants.USER_SENDER, currentUser);
        noti.put(Constants.RELATION_TYPE, relType);
        noti.put(Constants.POST, post.getObject());
        noti.put(Constants.SEEN, false);
        noti.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                //notify clack sender by push notification
                ClackUtils.pushNotification(context, noti, post, relType);
            }
        });
    }

    public static void deletePost(Post post, SaveCallback callback) {
        if (!AppUtils.isMyPost(post)) {
            return;
        }
        //delete all relations of the post
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.OBJECT_TYPE_RELATION);
        query.whereEqualTo(Constants.POST, post.getObject());
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> relList, ParseException e) {
                if (e == null && relList != null) {
                    for (ParseObject rel : relList) {
                        rel.deleteInBackground();
                    }
                }
            }
        });

        //mark the post as deleted
        post.setDeleted(true);
        post.saveInBackground(callback);
    }
}
