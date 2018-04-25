package com.firebasechatdemotutorial;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebasechatdemotutorial.model.User;

import java.util.List;

/**
 * Created by mobua01 on 25/4/18.
 */

public class UserListingRecyclerAdapter extends RecyclerView.Adapter<UserListingRecyclerAdapter.ViewHolder> {
    private final List<User> mUsers;

    public UserListingRecyclerAdapter(List<User> users) {
        this.mUsers = users;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.a_user_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = mUsers.get(position);

        if (user.email != null) {
            String alphabet = user.email.substring(0, 1);

            holder.txtUsername.setText(user.email);
            holder.txtUserAlphabet.setText(alphabet);
        }
    }

    @Override
    public int getItemCount() {
        if (mUsers != null) {
            return mUsers.size();
        }
        return 0;
    }

    public User getUser(int position) {
        return mUsers.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtUserAlphabet;
        private final TextView txtUsername;

        public ViewHolder(View itemView) {
            super(itemView);
            txtUserAlphabet = (TextView) itemView.findViewById(R.id.text_view_user_alphabet);
            txtUsername = (TextView) itemView.findViewById(R.id.text_view_username);
        }
    }
}
