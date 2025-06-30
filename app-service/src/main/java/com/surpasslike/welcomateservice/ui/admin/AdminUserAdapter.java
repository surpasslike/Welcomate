package com.surpasslike.welcomateservice.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.surpasslike.welcomateservice.R;
import com.surpasslike.welcomateservice.data.model.User;

import java.util.List;

/**
 * 用于在 RecyclerView 中显示用户列表的适配器
 */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private List<User> userList;

    /**
     * 构造函数
     *
     * @param userList 初始的用户数据列表
     */
    public AdminUserAdapter(List<User> userList) {
        this.userList = userList;
    }

    /**
     * 当 RecyclerView 需要一个新的 ViewHolder 时调用
     *
     * @param parent   新的 View 将被添加到的 ViewGroup
     * @param viewType View 的类型
     * @return 一个新的 UserViewHolder 实例
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * 将数据绑定到指定位置的 ViewHolder
     *
     * @param holder   要绑定数据的 ViewHolder
     * @param position 列表中的位置
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvUsername.setText(user.getUsername());
        holder.tvAccount.setText(user.getAccount());
        // 安全起见，我们不在此处显示密码或密码哈希
    }

    /**
     * 返回列表中的项目总数
     *
     * @return 列表的大小
     */
    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    /**
     * 更新适配器的数据集
     *
     * @param newUserList 新的用户列表
     */
    public void setUserList(List<User> newUserList) {
        this.userList = newUserList;
        // 刷新整个列表注意：对于大型列表，使用 DiffUtil 会更高效
        notifyDataSetChanged();
    }

    /**
     * ViewHolder 类，用于缓存 item view 中的视图
     */
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvAccount;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvAccount = itemView.findViewById(R.id.tvAccount);
            // 移除了 tvPassword 的绑定
        }
    }
}
