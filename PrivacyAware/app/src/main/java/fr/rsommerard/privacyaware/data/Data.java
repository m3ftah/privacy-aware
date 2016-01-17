package fr.rsommerard.privacyaware.data;

public class Data {

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

        if (data.getContent().equals(mContent)) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return mContent;
    }
}
