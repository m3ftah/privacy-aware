package fr.rsommerard.privacyaware;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import fr.rsommerard.privacyaware.dao.Data;

public class DataAdapter extends ArrayAdapter<Data> {

    private int mAddedIndex;
    private int mRemovedIndex;
    private final int mAddedColor;
    private final int mRemovedColor;

    public DataAdapter(final Context context, final int resource, final List<Data> dataList) {
        super(context, resource, dataList);
        mRemovedIndex = -1;
        mAddedIndex = -1;
        mAddedColor = Color.rgb(76, 175, 80); // Green
        mRemovedColor = Color.rgb(244, 67, 54); // Red
    }

    public void setAddedIndex(final int index) {
        mAddedIndex = index;
        notifyDataSetChanged();
    }

    public void setRemovedIndex(final int index) {
        mRemovedIndex = index;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        if(mRemovedIndex != -1 && position == mRemovedIndex) {
            view.setBackgroundColor(mRemovedColor);
        } else if(mAddedIndex != -1 && position == mAddedIndex) {
            view.setBackgroundColor(mAddedColor);
        } else {
            view.setBackgroundColor(this.getItem(position).getColor());
        }

        return view;
    }
}