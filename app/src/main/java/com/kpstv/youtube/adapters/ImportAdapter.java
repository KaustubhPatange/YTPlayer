package com.kpstv.youtube.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kpstv.youtube.R;
import com.kpstv.youtube.models.ImportModel;
import com.kpstv.youtube.models.ImportShowModel;
import com.kpstv.youtube.utils.YTutils;

import java.util.ArrayList;

import rm.com.longpresspopup.LongPressPopup;
import rm.com.longpresspopup.LongPressPopupBuilder;

public class ImportAdapter extends RecyclerView.Adapter<ImportAdapter.ImportHolder> {

    Context context;
    ArrayList<ImportModel> models;
    ImportListener listener; String[] items;
    private static final String TAG = "ImportAdapter";

    public ImportAdapter(Context context, ArrayList<ImportModel> models,String[] items) {
        this.context = context;
        this.models = models;
        this.items = items;
    }

    @NonNull
    @Override
    public ImportHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) viewGroup.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_import,viewGroup,false);

        return new ImportHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ImportHolder holder, int i) {
        final ImportModel model = models.get(i);
        RecyclerView recyclerView1;ImportShowAdapter adapter1;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ArrayList<ImportShowModel> models1 = new ArrayList<>();
        View view1 = inflater.inflate(R.layout.alert_recyclerview,null);
        recyclerView1 = view1.findViewById(R.id.recyclerView);
        recyclerView1.setLayoutManager(new LinearLayoutManager(context));

        String[] song = items[i].split(",");
        for (int u=2;u<song.length;u++) {
            String[] childs = song[u].split("\\|");
            models1.add(new ImportShowModel(
                    childs[2],childs[3],childs[4]
            ));
        }
        adapter1 = new ImportShowAdapter(context,models1);
        recyclerView1.setAdapter(adapter1);

        LongPressPopup popup = new LongPressPopupBuilder(context)// A Context object for the builder constructor
                .setTarget(holder.layout)
                .setPopupView(view1)
                .build();

        popup.register();

        if (model.isChecked())
            holder.checkBox.setChecked(true);
        else holder.checkBox.setChecked(false);

        holder.title.setText(model.getTitle());
        holder.songText.setText(model.getSongCount()+ " songs "+ Html.fromHtml("&#8226;") +" "+
                YTutils.getTimeString((long)model.getDuration()));
        holder.CircleText.setText(model.getTitle().toUpperCase().charAt(0)+"");
        holder.layout.setOnClickListener(view -> listener.onClick(model,i));


       /* holder.layout.setOnLongClickListener(view -> {
            longListener.onClick(holder.layout,model,i);
            return true;
        });*/
    }

    public interface ImportListener {
        void onClick(ImportModel model, int position);
    }


    public void setListener(ImportListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class ImportHolder extends RecyclerView.ViewHolder {
        TextView title,songText,CircleText;
        CheckBox checkBox;
        ConstraintLayout layout;
        public ImportHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.mainLayout);
            checkBox = itemView.findViewById(R.id.pcheckBox);
            title = itemView.findViewById(R.id.pTitle);
            songText = itemView.findViewById(R.id.pSongText);
            CircleText = itemView.findViewById(R.id.pFrameText);
        }
    }
}
