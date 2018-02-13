package com.jlanka.jltripplanner.Model;

import android.annotation.SuppressLint;
import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by User on 1/11/2018.
 */

@SuppressLint("ParcelCreator")
public class FoundSuggestion implements SearchSuggestion {

    private final String id;
    private String desc;

    public FoundSuggestion(String id, String desc){
        this.id=id;
        this.desc=desc;
    }

    public String getId(){ return id;}

    @Override
    public String getBody() {
        return desc;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
