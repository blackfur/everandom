package sample.note;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class NotesAdapter extends ArrayAdapter<Map<String,String>>{

    Context mContext;

    // View lookup cache
    static class ViewHolder {
        TextView txt;
    }

    public void notifyChanged(ArrayList<Map<String, String>> data) {
        clear();
        addAll(data);
        notifyDataSetChanged();
    }
    public NotesAdapter(ArrayList<Map<String,String>> data, Context context) {
        super(context, R.layout.list_item, data);
        this.mContext=context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Map<String,String> dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.txt= convertView.findViewById(R.id.txt);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.txt.setText(dataModel.get(Repository.COLUMN_TXT));
        // Return the completed view to render on screen
        return convertView;
    }
}
