package net.develish.noted.noted;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.develish.noted.noted.net.develish.db.DatabaseManager;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by deifyed on 04.03.18.
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private Activity mActivity;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    private PorterDuffColorFilter SELECTION_FILTER;

    private List<Note> mDataset;

    NoteAdapter(Activity activity, List<Note> dataset) {
        mActivity = activity;
        mInflater = LayoutInflater.from(mActivity);

        SELECTION_FILTER = new PorterDuffColorFilter(Color.parseColor("#EEEEEE"),
                                                     PorterDuff.Mode.MULTIPLY);

        mDataset = dataset;
    }

    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.open_note_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Note current = mDataset.get(position);

        holder.itemView.getBackground().clearColorFilter();
        holder.lblTitle.setText(current.getTitle());
        holder.lblChanged.setText(prettifyNow(current.last_change));
        holder.imgAvatar.setImageResource(R.mipmap.ic_launcher_round);

        if(OpenNoteActivity.inActionMode) {
            if(OpenNoteActivity.selectionList.contains(mDataset.get(position))) {
                holder.itemView.getBackground().setColorFilter(SELECTION_FILTER);
            }

        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                                                                       View.OnLongClickListener {
        TextView lblTitle;
        TextView lblChanged;
        ImageView imgAvatar;

        ViewHolder(View itemView) {
            super(itemView);

            lblTitle = (TextView) itemView.findViewById(R.id.lblTitle);
            lblChanged = (TextView) itemView.findViewById(R.id.lblChanged);
            imgAvatar = (ImageView) itemView.findViewById(R.id.imgAvatar);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(OpenNoteActivity.inActionMode) {
                ((OpenNoteActivity) mActivity).prepareSelection(getAdapterPosition());

                notifyDataSetChanged();
            }
            else
                if(mClickListener != null)
                    mClickListener.onItemClick(view, getAdapterPosition());
        }
        @Override
        public boolean onLongClick(View view) {
            if(!OpenNoteActivity.inActionMode)
                ((OpenNoteActivity) mActivity).prepareToolbar(getAdapterPosition());

            return true;
        }
    }

    Note getItem(UUID id) {
        return null;
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    private String prettifyNow(String now) {
        String result = now;
        Locale lc = Locale.US;

        try {
            Date parsed = DatabaseManager.DATE_FORMATTER.parse(now);
            Calendar c = new GregorianCalendar();
            c.setTime(parsed);

            String month = c.getDisplayName(Calendar.MONTH, Calendar.SHORT, lc);
            int day = c.get(Calendar.DAY_OF_MONTH);

            String hours = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
            if(hours.length() == 1)
                hours = "0" + hours;

            String minutes = Integer.toString(c.get(Calendar.MINUTE));
            if(minutes.length() == 1)
                minutes = "0" + minutes;


            result = String.format(lc, "%d %s, %s:%s", day, month, hours, minutes);
        }
        catch (ParseException e) {

        }

        return result;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
