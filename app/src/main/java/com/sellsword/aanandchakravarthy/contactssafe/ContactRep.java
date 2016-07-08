package com.sellsword.aanandchakravarthy.contactssafe;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by aanand.chakravarthy on 7/4/2016.
 */
public class ContactRep implements Parcelable,Comparable<ContactRep> {
    String name;

    @Override
    public int compareTo(ContactRep another) {
        return this.name.compareTo(another.name);
    }

    ArrayList<String> phoneno;

    public ContactRep(){
    phoneno = new ArrayList<String>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public ContactRep(Parcel in){
        this();
         name = in.readString();
         int num_phones = in.readInt();
         for(int i=0;i<num_phones;i++){
            phoneno.add(in.readString());
         }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(phoneno.size());
        for(int i=0;i<phoneno.size();i++){
            dest.writeString(phoneno.get(i));
        }
    }


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ContactRep createFromParcel(Parcel in) {
            return new ContactRep(in);
        }

        public ContactRep[] newArray(int size) {
            return new ContactRep[size];
        }
    };
}
