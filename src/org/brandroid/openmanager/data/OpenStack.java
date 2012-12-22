
package org.brandroid.openmanager.data;

import java.util.Stack;

public class OpenStack extends Stack<OpenPath> {
    private static final long serialVersionUID = 7729448469762390812L;

    private Stack<OpenPath> mStack;

    public static OpenStack DefaultStack = new OpenStack();

    public OpenStack() {
        mStack = new Stack<OpenPath>();
        mStack.add(new OpenFile("/"));
    }

    public OpenPath pop() {
        return mStack.pop();
    }

    public OpenPath push(OpenPath file) {
        if (mStack.size() == 0 || !mStack.peek().getPath().equals(file.getPath()))
            mStack.push(file);
        return file;
    }

    public OpenPath peek() {
        if (mStack.size() > 0)
            return mStack.peek();
        else
            return null;
    }
}
