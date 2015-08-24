package io.kickflip.sdk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import io.kickflip.sdk.R;
import io.kickflip.sdk.model.kanvas_live.ChatMessage;

public class ChatMessageAdapter extends BaseAdapter {

    private static final int INVISIBLE_COUNT = 20;

    private Context context;
    private List<ChatMessage> mList;

    public ChatMessageAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.mList = messages;
    }

    @Override
    public int getCount() {
        return mList.size() + INVISIBLE_COUNT;
    }

    @Override
    public ChatMessage getItem(int position) {
        if (position < INVISIBLE_COUNT) {
            ChatMessage cm = new ChatMessage();
            cm.setInvisible(true);
            return cm;
        } else
            return mList.get(position - INVISIBLE_COUNT);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder v;
        if (view == null) {
            view = inflater.inflate(R.layout.adapter_chat_message, parent, false);
            if (view == null) return null;
            v = newViewHolder(view);
            view.setTag(v);
        } else {
            v = (ViewHolder) view.getTag();
        }
        ChatMessage chatMessage = getItem(position);
        if (chatMessage.isInvisible()) {
            view.setVisibility(View.INVISIBLE);
            return view;
        }
        view.setVisibility(View.VISIBLE);
        populate(chatMessage, v);
        return view;
    }

    private void populate(ChatMessage chatMessage, ViewHolder vh) {
        vh.message.setText(chatMessage.getMessage());
        vh.elapse.setText(String.valueOf(chatMessage.getElapse()));
    }

    private ViewHolder newViewHolder(View view) {
        ViewHolder vh = new ViewHolder();
        vh.message = (TextView) view.findViewById(R.id.adapter_chat_message_message);
        vh.elapse = (TextView) view.findViewById(R.id.adapter_chat_message_elapse);
        return vh;
    }

    private static class ViewHolder {
        TextView message;
        TextView elapse;
    }
}
