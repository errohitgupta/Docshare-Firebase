package ashraf.practice.com.docshare.Models;

/**
 * Created by Ashraf_Patel on 1/24/2016.
 */
public class DocInfoModel {
private String title,desc,user,file_url,file_name,created_at,size,extension;

    public DocInfoModel(){}
    public DocInfoModel(String title, String desc, String user, String file_url, String file_name, String created_at, String size, String extension) {
        this.title = title;
        this.desc = desc;
        this.user = user;
        this.file_url = file_url;
        this.file_name = file_name;
        this.created_at = created_at;
        this.size = size;
        this.extension = extension;
    }

    public String getSize() {
        return size;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }
}
