package com.dualcnhq.sherlocked.utils;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;

public class Observable {

    public static final int NOTIFICATION_LOCATION_CHANGED = 100;
    public static final int NOTIFICATION_FAILED_TO_GET_LOCATION = 101;
    public static final int NOTIFICATION_LOGGED_OUT = 102;
    public static final int NOTIFICATION_USER_PHOTO_CHANGED = 103;
    public static final int NOTIFICATION_USER_KARMA_CHANGED = 104;

    private HashMap<Integer, ArrayList<Observer>> mObservers = new HashMap<Integer, ArrayList<Observer>>();
    private static Observable mInstance;

    private Observable() {}

    public static Observable getInstance() {
        if (mInstance == null) {
            mInstance = new Observable();
        }
        return mInstance;
    }

    public void registerObserver(int notification, Observer observer) {
        ArrayList<Observer> list;
        if (mObservers.containsKey(notification)) {
            list = mObservers.get(notification);
        } else {
            list = new ArrayList<>();
        }
        list.add(observer);
        mObservers.put(notification, list);
    }

    public void unregisterObserver(int notification, Observer observer) {
        if (mObservers.containsKey(notification)) {
            ArrayList<Observer> list = mObservers.get(notification);
            if (list.contains(observer)) {
                list.remove(observer);
            }
        }
    }

    public void notifyObservers(int notification, Bundle data) {
        if (mObservers.containsKey(notification)) {
            ArrayList<Observer> list = mObservers.get(notification);
            for (Observer o : list) {
                o.notify(notification, data);
            }
        }
    }

    public static interface Observer {
        public void notify(int notification, Bundle data);
    }
}
