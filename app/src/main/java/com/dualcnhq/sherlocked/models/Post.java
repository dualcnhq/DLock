package com.dualcnhq.sherlocked.models;

import com.dualcnhq.sherlocked.data.Constants;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;


import java.util.Date;

//@ParseClassName("Posts")
public class Post {

    private ParseObject mObject;

    public Post() {
        mObject = new ParseObject("Posts");
    }

    public Post(ParseObject object) {
        mObject = object;
    }

    public Date getCreatedAt() {
        return mObject.getCreatedAt();
    }

    public void setACL(ParseACL acl) {
        mObject.setACL(acl);
    }

    public ParseObject getObject() {
        return mObject;
    }

    public String getObjectId() {
        return mObject.getObjectId();
    }

    public void saveInBackground(SaveCallback callback) {
        mObject.saveInBackground(callback);
    }

    public void saveInBackground() {
        mObject.saveInBackground();
    }

    public void save() throws ParseException {
        mObject.save();
    }

    public String getPost() {
        return mObject.getString(Constants.POST);
    }

    public void setPost(String post) {
        mObject.put(Constants.POST, post);
    }

    public ParseUser getUser() {
        return mObject.getParseUser(Constants.USER);
    }

    public void setUser(ParseUser value) {
        mObject.put(Constants.USER, value);
    }

    public void setCount(int count) {
        mObject.put(Constants.COUNT, count);
    }

    public int getCount() {
        return mObject.getInt(Constants.COUNT);
    }

    public void incrementCounter(int amount) {
        mObject.increment(Constants.COUNT, amount);
    }

    public boolean getDeleted() {
        return mObject.getBoolean(Constants.DELETED);
    }

    public void setDeleted(boolean deleted) {
        mObject.put(Constants.DELETED, deleted);
    }

    public int getInappropriateCount() {
        return mObject.getInt(Constants.INAPPROPRIATE);
    }

    public void setInappropriateCount(int count) {
        mObject.put(Constants.INAPPROPRIATE, count);
    }

    public void incrementInappropriate(int increment) {
        mObject.increment(Constants.INAPPROPRIATE, increment);
    }

    public ParseGeoPoint getLocation() {
        return mObject.getParseGeoPoint(Constants.LOCATION);
    }

    public void setLocation(ParseGeoPoint value) {
        mObject.put(Constants.LOCATION, value);
    }

    public static ParseQuery<ParseObject> getQuery() {
        return ParseQuery.getQuery("Posts");
    }
}
