
package org.brandroid.openmanager.util;

import org.brandroid.openmanager.util.SortType.Type;

public class SortType {

    Type mWhich = Type.ALPHA;
    boolean mFoldersFirst = true;

    public SortType(Type which) {
        mWhich = which;
    }

    public SortType(String s) {
        String t = s;
        if (s.indexOf(" ") > -1)
            t = s.substring(0, s.indexOf(" ")).trim();
        for (Type type : SortType.Type.values())
            if (type.toString().equalsIgnoreCase(t))
                mWhich = type;
        if (s.indexOf("FM") > -1)
            mFoldersFirst = false;
    }

    public boolean foldersFirst() {
        return mFoldersFirst;
    }

    public Type getType() {
        return mWhich;
    }

    @Override
    public String toString() {
        return getType().toString() + " (" + (mFoldersFirst ? "FF" : "FM") + ")";
    }

    public enum Type {
        NONE, ALPHA, TYPE, SIZE, // smallest
        SIZE_DESC, // largest
        DATE, // newest
        DATE_DESC, // oldest
        ALPHA_DESC
    }

    public static final SortType NONE = new SortType(Type.NONE), ALPHA = new SortType(Type.ALPHA),
            TYPE = new SortType(Type.TYPE), SIZE = new SortType(Type.SIZE),
            SIZE_DESC = new SortType(Type.SIZE_DESC), DATE = new SortType(Type.DATE),
            DATE_DESC = new SortType(Type.DATE_DESC), ALPHA_DESC = new SortType(Type.ALPHA_DESC);

    public SortType setFoldersFirst(Boolean first) {
        if (first != null)
            mFoldersFirst = first;
        return this;
    }

    public SortType setType(Type which) {
        if (which != null)
            mWhich = which;
        return this;
    }
}
