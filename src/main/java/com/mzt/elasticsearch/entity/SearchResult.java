package com.mzt.elasticsearch.entity;

/**
 * @ClassName SearchResult
 * @Description TODO
 * @Author mazhitao
 * @Date 2019/12/19 15:26
 * @Version 1.0
 **/
public class SearchResult {
    /**标题*/
    private String title ;

    /**文件路径*/
    private String filePath;

    /**文件内容*/
    private String context ;

    /**高亮显示部分*/
    private String highlightText;

    /**
     * 资源ID
     */
    private String resId;

    /**
     * 供应商名称
     */
    private String supplierName;

    private String id;

    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getHighlightText() {
        return highlightText;
    }

    public void setHighlightText(String highlightText) {
        this.highlightText = highlightText;
    }

    public String getResId() {
        return resId;
    }

    public void setResId(String resId) {
        this.resId = resId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
}
