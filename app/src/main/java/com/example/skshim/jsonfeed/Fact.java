package com.example.skshim.jsonfeed;

/**
 * Created by Sungki Shim on 1/08/15.
 *
 * This class used to hold json data.
 */
public class Fact {

    private String title;
    private String description;
    private String imageHref;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageHref() {
        return imageHref;
    }

    public void setImageHref(String imageHref) {
        this.imageHref = imageHref;
    }

    @Override
    public String toString() {
        return "Fact{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", imageHref='" + imageHref + '\'' +
                '}';
    }

    public boolean isNull(){
        return title==null && description==null && imageHref==null;
    }
}
