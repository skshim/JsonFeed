package com.example.skshim.jsonfeed.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sungki Shim on 1/08/15.
 *
 * This class used to hold json data.
 */
public class Fact  implements Parcelable {

    private String title;
    private String description;
    private String imageHref;

    public Fact(String title, String description, String imageHref) {
        this.title = title;
        this.description = description;
        this.imageHref = imageHref;
    }

    protected Fact(Parcel in){
        title=in.readString();
        description=in.readString();
        imageHref=in.readString();
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(imageHref);
    }

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
