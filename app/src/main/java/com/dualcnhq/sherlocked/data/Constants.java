package com.dualcnhq.sherlocked.data;

public interface Constants {

    int DATABASE_VERSION = 1;
    String DATABASE_NAME = "sherlocked.db";

    String PREFS_NAME = "sherlocked_prefs";

    String DB_TABLENAME_CONTACTS = "contacts";
    String DB_TABLENAME_LOGS = "logs";

    String PRIMARY_CONTACT_NAME = "primary_contact_name";
    String PRIMARY_CONTACT_NUMBER = "primary_contact_number";

    String IS_LOCK_ENABLED = "is_lock_enabled";
    String IS_CONTACT_SET = "is_contact_set";
}