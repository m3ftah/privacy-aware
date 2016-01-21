package fr.rsommerard.privacyaware.data;

import java.io.Serializable;

public class Data implements Serializable {

    private final String mContent;

    public Data(final String content) {
        mContent = content;
    }

    public String getContent() {
        return mContent;
    }

    @Override
    public boolean equals(final Object obj) {
        Data data = (Data) obj;

        return data.getContent().equals(mContent);
    }

    @Override
    public String toString() {
        return mContent;
    }
}
