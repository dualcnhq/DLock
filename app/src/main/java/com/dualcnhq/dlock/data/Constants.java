package com.dualcnhq.dlock.data;

public interface Constants {

    String APP_ID = "BtXMKzEEUx13Wv6eTrVcN0iqJaeww7kwsyggGIBo";
    String CLIENT_KEY = "HCRBO2aZdSXIIVRJmJusNYwtBOKfoLpO52X4nIAF";

    int DATABASE_VERSION = 1;
    String DATABASE_NAME = "sherlocked.db";

    String PREFS_NAME = "sherlocked_prefs";

    String DB_TABLENAME_CONTACTS = "contacts";
    String DB_TABLENAME_LOGS = "logs";

    String PRIMARY_CONTACT_NAME = "primary_contact_name";
    String PRIMARY_CONTACT_NUMBER = "primary_contact_number";

    String IS_LOCK_ENABLED = "is_lock_enabled";
    String IS_CONTACT_SET = "is_contact_set";

    /*---------------------------------------------------------------*/
    String POSTS = "Posts";

    String POST_ID = "post_id";
    String OBJECT_ID = "objectId";
    String USER = "user";
    String USER_SENDER = "user_sender";
    String MOBILE_NUMBER = "mobile_number";
    String GENDER = "gender";
    String POST = "post";
    String COUNT = "count";
    String CREATED_AT = "createdAt";
    String DELETED = "deleted";
    String INAPPROPRIATE = "inappropriate_cnt";
    String LOCATION = "location";
    String USER_PHOTO = "userPhoto";
    String USER_CREDITS = "userCredits";
    String FULL_NAME = "fullName";
    String COUNTRY = "country";
    String SEEN = "seen";

    String LOCATION_TITLE = "loc_title";
    String RELATION_TYPE = "reltype";

    String IMAGE_OBJECT_ID = "image_object_id";

    String NOTIFICATION_ID = "noti";

    //Object types
    String OBJECT_TYPE_RELATION = "UserRelation";
    String OBJECT_TYPE_NOTIFICATION = "Notification";
    String OBJECT_TYPE_CREDITS = "userCredits";

    //Relations
    int RELATION_TYPE_UPVOTE = 1;
    int RELATION_TYPE_DOWNVOTE = 2;
    int RELATION_TYPE_REPORT_INAPPROPRIATE = 3;
    int RELATION_TYPE_SAVES = 4;

    //Frags
    int FRAGMENT_HOME = 1;
    int FRAGMENT_SNAP = 2;
    int FRAGMENT_SNOOP_MAP = 3;
    int FRAGMENT_SNOOP_LIST = 4;
    int FRAGMENT_DETAIL = 5;

    String FRAGMENT_HOME_TAG = "home_fragment";
    String FRAGMENT_SNAP_TAG = "saved_fragment";
    String FRAGMENT_SNOOP_MAP_TAG = "snoop_map_fragment";
    String FRAGMENT_SNOOP_LIST_TAG = "snoop_list_fragment";
    String FRAGMENT_DETAIL_TAG = "detail_fragment";
}