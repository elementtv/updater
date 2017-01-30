package com.skystreamtv.element_ez_stream.updater.utils.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.utils.TextUtil;

import java.util.List;

public class UpdateItemAdapter extends RecyclerView.Adapter<UpdateItemAdapter.ItemViewHolder> {

    private List<Skin> skins;
    private DoUpdate listener;
    private Button button;

    public UpdateItemAdapter(List<Skin> skins, DoUpdate listener) {
        this.skins = skins;
        this.listener = listener;
    }

    public void setFocus() {
        if (button != null) button.requestFocus();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.update_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        final Skin skin = skins.get(position);
        holder.name.setText(skin.getName());
        holder.details.setText(TextUtil.fromHtml(skin.getDetails()));
        Log.d("Adapter", "skin: " + skin.getName() + " UTD: " + skin.isUpToDate());
        if (skin.isInstalled()) {
            if (!skin.isUpToDate()) {
                holder.status.setText(R.string.update_available);
                holder.status.setTypeface(null, Typeface.BOLD);
                holder.update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.doUpdate(skin);
                    }
                });
            } else {
                holder.status.setText(R.string.up_to_date);
                holder.details.setVisibility(View.GONE);
                holder.update.setVisibility(View.GONE);
                holder.update.setEnabled(false);
            }
        } else {
            holder.status.setText(R.string.not_installed);
            holder.update.setText(R.string.install);
            holder.update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.doUpdate(skin);
                }
            });
        }
        holder.update.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    holder.update.setBackgroundColor(ContextCompat.getColor((Context) listener, R.color.colorPrimary));
                    holder.update.setTextColor(Color.WHITE);
                } else {
                    holder.update.setBackgroundResource(android.R.drawable.btn_default);
                    holder.update.setTextColor(Color.BLACK);
                }
            }
        });
        if (position == 0) this.button = holder.update;
    }

    @Override
    public int getItemCount() {
        return skins.size();
    }

    public interface DoUpdate {
        void doUpdate(Skin skin);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        public Button update;
        TextView name, status, details;

        ItemViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.update_name);
            status = (TextView) itemView.findViewById(R.id.update_status);
            details = (TextView) itemView.findViewById(R.id.details);
            update = (Button) itemView.findViewById(R.id.update_button);
        }
    }
}