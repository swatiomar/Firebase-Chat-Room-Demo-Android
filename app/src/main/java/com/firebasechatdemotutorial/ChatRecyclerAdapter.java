package com.firebasechatdemotutorial;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebasechatdemotutorial.model.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by mobua01 on 25/4/18.
 */

public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;
    private final ArrayList<Chat> mChats;
    private final Context context;

    public ChatRecyclerAdapter(Context context, ArrayList<Chat> chats) {
        this.mChats = chats;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_ME:
                View viewChatMine = layoutInflater.inflate(R.layout.item_chat_mine, parent, false);
                viewHolder = new MyChatViewHolder(viewChatMine);
                break;
            case VIEW_TYPE_OTHER:
                View viewChatOther = layoutInflater.inflate(R.layout.item_chat_other, parent, false);
                viewHolder = new OtherChatViewHolder(viewChatOther);
                break;
        }
        return viewHolder;
    }

    public void add(Chat chat) {
        mChats.add(chat);
        notifyItemInserted(mChats.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (TextUtils.equals(mChats.get(position).senderUid,
                FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return VIEW_TYPE_ME;
        } else {
            return VIEW_TYPE_OTHER;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (TextUtils.equals(mChats.get(position).senderUid,
                FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            configureMyChatViewHolder((MyChatViewHolder) holder, position);
        } else {
            configureOtherChatViewHolder((OtherChatViewHolder) holder, position);
        }


    }

    private void configureOtherChatViewHolder(OtherChatViewHolder otherChatViewHolder, int position) {
        final Chat chat = mChats.get(position);

        String alphabet = chat.sender.substring(0, 1);
        otherChatViewHolder.txtChatMessage.setText(chat.message);
        otherChatViewHolder.txtUserAlphabet.setText(alphabet);

       /* if (typeChoose.equalsIgnoreCase("image_file")) {

            otherChatViewHolder.imageLocation.setVisibility(View.VISIBLE);
            Picasso.get().load(downloadedFile).into(otherChatViewHolder.imageLocation);
        } else if (typeChoose.equalsIgnoreCase("audio_file")) {


        } else if (typeChoose.equalsIgnoreCase("video_file")) {


        } else {


        }*/
    }

    private void configureMyChatViewHolder(MyChatViewHolder myChatViewHolder, int position) {
        final Chat chat = mChats.get(position);

        String alphabet = chat.sender.substring(0, 1);
        myChatViewHolder.txtChatMessage.setText(chat.message);
        myChatViewHolder.txtUserAlphabet.setText(alphabet);

        /*if (typeChoose.equalsIgnoreCase("image_file")) {

            myChatViewHolder.imageLocation.setVisibility(View.VISIBLE);
            Picasso.get().load(downloadedFile).into(myChatViewHolder.imageLocation);
        } else if (typeChoose.equalsIgnoreCase("audio_file")) {


        } else if (typeChoose.equalsIgnoreCase("video_file")) {


        } else {


        }*/
    }

    public static Bitmap getGoogleMapThumbnail(double lati, double longi) {

        String URL = "http://maps.google.com/maps/api/staticmap?center=" + lati + "," + longi + "&markers=size:mid%7Ccolor:red&zoom=10&size=200x200&sensor=false";
//        String URL = "http://maps.google.com/maps/api/staticmap?center=25.3176452,82.97391440000001,&zoom=15&markers=icon:http://www.megaadresse.com/images/icons/google-maps.png|25.3176452,82.97391440000001&path=color:0x0000FF80|weight:5|25.3176452,82.97391440000001&size=175x175";

        Bitmap bmp = null;
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet request = new HttpGet(URL);

        InputStream in = null;

        try {
            in = httpclient.execute(request).getEntity().getContent();
            bmp = BitmapFactory.decodeStream(in);
            in.close();

        } catch (IllegalStateException e) {
            e.printStackTrace();

        } catch (ClientProtocolException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bmp;
    }

    @Override
    public int getItemCount() {
        if (mChats != null) {
            return mChats.size();
        }
        return 0;
    }

    private static class MyChatViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageLocation;
        private TextView txtChatMessage, txtUserAlphabet;

        public MyChatViewHolder(View itemView) {
            super(itemView);
            txtChatMessage = (TextView) itemView.findViewById(R.id.text_view_chat_message);
            txtUserAlphabet = (TextView) itemView.findViewById(R.id.text_view_user_alphabet);
            imageLocation = (ImageView) itemView.findViewById(R.id.image_view_chat_message);
        }
    }

    private static class OtherChatViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageLocation;
        private TextView txtChatMessage, txtUserAlphabet;

        public OtherChatViewHolder(View itemView) {
            super(itemView);
            txtChatMessage = (TextView) itemView.findViewById(R.id.text_view_chat_message);
            txtUserAlphabet = (TextView) itemView.findViewById(R.id.text_view_user_alphabet);
            imageLocation = (ImageView) itemView.findViewById(R.id.image_view_chat_message);
        }
    }
}
/*  try {
            if (chat.type.equalsIgnoreCase("location")) {
                otherChatViewHolder.txtChatMessage.setVisibility(View.GONE);
                otherChatViewHolder.imageLocation.setVisibility(View.VISIBLE);
                Bitmap googleMapThumbnail = getGoogleMapThumbnail(Double.parseDouble(chat.latitude), Double.parseDouble(chat.longitude));
                otherChatViewHolder.imageLocation.setImageBitmap(googleMapThumbnail);

               *//* otherChatViewHolder.imageLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String uri = String.format(Locale.ENGLISH, "geo:%f,%f", chat.latitude, chat.longitude);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        context.startActivity(intent);
                    }
                });*//*
            }
        } catch (Exception e) {
            Log.d("Tag", e.getMessage());
        }*/



        /*try {
            if (chat.type.equalsIgnoreCase("location")) {
                myChatViewHolder.txtChatMessage.setVisibility(View.GONE);
                myChatViewHolder.imageLocation.setVisibility(View.VISIBLE);
                Bitmap googleMapThumbnail = getGoogleMapThumbnail(Double.parseDouble(chat.latitude), Double.parseDouble(chat.longitude));
                myChatViewHolder.imageLocation.setImageBitmap(googleMapThumbnail);

                *//*myChatViewHolder.imageLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String uri = String.format(Locale.ENGLISH, "geo:%f,%f", chat.latitude, chat.longitude);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        context.startActivity(intent);
                    }
                });*//*
            }

        } catch (Exception e) {
            e.getMessage();
            Log.d("TAg", e.getMessage() + "");
        }
*/