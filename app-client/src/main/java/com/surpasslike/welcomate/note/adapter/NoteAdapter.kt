package com.surpasslike.welcomate.note.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.surpasslike.welcomate.databinding.RvAdapterNoteBinding
import com.surpasslike.welcomate.note.entity.NoteBean

/**
 * 笔记列表适配器
 *
 * 使用 ListAdapter 而不是普通的 RecyclerView.Adapter：
 * - 自动处理列表差异对比（DiffUtil）
 * - 只刷新变化的项，性能更好
 * - 自动添加列表更新动画
 *
 * 使用方式：
 * ```
 * val adapter = NoteAdapter()
 * recyclerView.adapter = adapter
 *
 * // 更新数据时调用 submitList()
 * adapter.submitList(noteList)
 * ```
 */
class NoteAdapter : ListAdapter<NoteBean, NoteAdapter.ViewHolder>(NoteDiffCallback()) {

    /**
     * 创建 ViewHolder
     * 当 RecyclerView 需要新的 ViewHolder 时调用（例如首次显示或滚动时）
     *
     * @param parent 父视图容器
     * @param viewType 视图类型（当有多种布局时使用）
     * @return 新创建的 ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 使用 ViewBinding 加载布局
        val binding = RvAdapterNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * 绑定数据到 ViewHolder
     * 当 ViewHolder 需要显示数据时调用（例如滚动到某个位置）
     *
     * @param holder 要绑定数据的 ViewHolder
     * @param position 数据在列表中的位置
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // getItem(position) 是 ListAdapter 提供的方法，自动从内部列表获取数据
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder：持有单个列表项的视图
     *
     * 作用：
     * 1. 缓存视图引用，避免重复 findViewById
     * 2. 处理单个列表项的数据绑定
     * 3. 可以添加点击事件等交互逻辑
     *
     * inner class 可以访问外部类的成员（如果需要的话）
     */
    inner class ViewHolder(private val binding: RvAdapterNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * 将数据绑定到视图
         *
         * @param bean 笔记数据对象
         */
        fun bind(bean: NoteBean) {
            binding.tvTitle.text = bean.title
            binding.tvContent.text = bean.content
            binding.tvCreateTime.text = bean.createTime
            binding.tvUpdateTime.text = bean.updateTime

            // 可以在这里添加点击事件：
            // binding.root.setOnClickListener {
            //     // 处理点击事件，例如跳转到编辑页面
            // }
        }
    }

    /**
     * DiffUtil.ItemCallback：用于对比新旧列表的差异
     *
     * ListAdapter 使用这个回调来判断：
     * 1. 哪些项是相同的（通过 id）
     * 2. 相同项的内容是否变化了
     *
     * 这样就能实现局部刷新，不需要刷新整个列表
     */
    private class NoteDiffCallback : DiffUtil.ItemCallback<NoteBean>() {
        /**
         * 判断两个对象是否代表同一个列表项
         * 通常通过唯一标识符（如 id）判断
         *
         * @return true 表示是同一项（只是可能内容变了）
         */
        override fun areItemsTheSame(oldItem: NoteBean, newItem: NoteBean): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * 判断两个对象的内容是否完全相同
         * 当 areItemsTheSame 返回 true 时才会调用此方法
         *
         * @return true 表示内容完全一样，不需要刷新
         *         false 表示内容变了，需要刷新这一项
         */
        override fun areContentsTheSame(oldItem: NoteBean, newItem: NoteBean): Boolean {
            // data class 自动实现了 equals()，可以直接比较
            return oldItem == newItem
        }
    }
}