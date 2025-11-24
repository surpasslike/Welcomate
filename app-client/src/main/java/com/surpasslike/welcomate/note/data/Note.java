package com.surpasslike.welcomate.note.data;

/**
 * 数据模型类，代表一个笔记实体
 * POJO (Plain Old Java Object)，用于封装用户数据
 */
public class Note {
    private final String id;  // 添加 id 字段
    private final String title;
    private final String content;
    private final String createTime;
    private final String updateTime;

    public Note(String id, String title, String content, String createTime, String updateTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }
}