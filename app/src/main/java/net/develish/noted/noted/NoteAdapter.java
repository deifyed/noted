package net.develish.noted.noted;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
        holder.lblChanged.setText(prettifyDate(current.last_change));

        if(OpenNoteActivity.inActionMode)
            if(OpenNoteActivity.selectionList.contains(mDataset.get(position)))
                holder.itemView.getBackground().setColorFilter(SELECTION_FILTER);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                                                                       View.OnLongClickListener {
        TextView lblTitle;
        TextView lblChanged;

        ViewHolder(View itemView) {
            super(itemView);

            lblTitle = (TextView) itemView.findViewById(R.id.lblTitle);
            lblChanged = (TextView) itemView.findViewById(R.id.lblChanged);

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

    private String prettifyDate(String then) {
        String result = then;
        Locale lc = Locale.US;

        try {
            Date parsed = DatabaseManager.DATE_FORMATTER.parse(then);
            Calendar c_then = new GregorianCalendar();
            c_then.setTime(parsed);

            Calendar c_now = new GregorianCalendar();
            c_now.setTime(new Date());

            String month_then = c_then.getDisplayName(Calendar.MONTH, Calendar.LONG, lc);
            String month_now = c_now.getDisplayName(Calendar.MONTH, Calendar.LONG, lc);
            int day_then = c_then.get(Calendar.DAY_OF_MONTH);
            int day_now = c_now.get(Calendar.DAY_OF_MONTH);

            String hours = Integer.toString(c_then.get(Calendar.HOUR_OF_DAY));
            if(hours.length() == 1)
                hours = "0" + hours;

            String minutes = Integer.toString(c_then.get(Calendar.MINUTE));
            if(minutes.length() == 1)
                minutes = "0" + minutes;

            if(month_now.equals(month_then) && day_now == day_then)
                result = String.format(lc, "%s:%s", hours, minutes);
            else
                result = String.format(lc, "%d%s of %s", day_then, getDayOfMonthSuffix(day_then),
                                       month_then);
        }
        catch (ParseException e) {

        }

        //return "Modified: " + result;
        return result;
    }
    private String getDayOfMonthSuffix(final int n) {
        if(n >= 11 && n <= 13)
            return "th";
        switch(n % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
