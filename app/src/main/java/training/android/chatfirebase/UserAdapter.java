package training.android.chatfirebase;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {


    private static final String TAG = "UserAdapter";
    private List<User> users;
    private OnUserClickListener listener;

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User currentUser = users.get(position);
        holder.userNameTextView.setText(currentUser.getName());
        if(!currentUser.getAvatarURL().equals("default")){
            Picasso.get().load(currentUser.getAvatarURL()).fit().centerInside().placeholder(R.drawable.user_image).error(R.drawable.user_image).into(holder.avatarImageView, new Callback() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "onSuccess: TRUE");
                }

                @Override
                public void onError(Exception e) {
                    Log.i(TAG, "onError: TRUE");
                }
            });
        }
        // todo повинно теж працювати
//        Glide.with(holder.avatarImageView.getContext()).load(currentUser.getAvatarURL()).into(holder.avatarImageView);

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public interface OnUserClickListener {
        void onUserClick(int position);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatarImageView;
        public TextView userNameTextView;


        public UserViewHolder(@NonNull View itemView, final OnUserClickListener listener) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onUserClick(position);
                        }
                    }
                }
            });
        }
    }
}
