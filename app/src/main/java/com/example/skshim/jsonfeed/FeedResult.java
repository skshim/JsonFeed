package com.example.skshim.jsonfeed;

import java.util.ArrayList;

/**
 * Created by Sungki Shim on 2/08/15.
 */
public class FeedResult {
    private String title;
    private ArrayList<Fact> rows;

    public FeedResult(){
        rows = new ArrayList<Fact>();
    }

    public String getTitle() {
        return title;
    }

    public FeedResult setTitle(String title) {
        this.title = title;
        return this;
    }

    public ArrayList<Fact> getRows() {
        return rows;
    }

    public FeedResult setRows(ArrayList<Fact> rows) {
        this.rows = rows;
        return this;
    }

    public FeedResult addItem(Fact item){
        rows.add(item);
        return this;
    }


    @Override
    public String toString() {
        return "FeedResult{" +
                "title='" + title + '\'' +
                ", rows=" + rows +
                '}';
    }

    public static FeedResult create(String title){
        FeedResult feedResult = new FeedResult();
        feedResult.setTitle(title);
        return feedResult;
    }
}
