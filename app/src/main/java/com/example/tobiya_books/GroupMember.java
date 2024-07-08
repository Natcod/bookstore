package com.example.tobiya_books;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupMember implements Parcelable {
    private String firstName;
    private String lastName;
    private String profilePictureUrl;

    public GroupMember(String firstName, String lastName, String profilePictureUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePictureUrl = profilePictureUrl;
    }

    protected GroupMember(Parcel in) {
        firstName = in.readString();
        lastName = in.readString();
        profilePictureUrl = in.readString();
    }

    public static final Creator<GroupMember> CREATOR = new Creator<GroupMember>() {
        @Override
        public GroupMember createFromParcel(Parcel in) {
            return new GroupMember(in);
        }

        @Override
        public GroupMember[] newArray(int size) {
            return new GroupMember[size];
        }
    };

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(profilePictureUrl);
    }
}
