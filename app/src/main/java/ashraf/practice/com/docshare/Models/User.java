package ashraf.practice.com.docshare.Models;

import java.io.Serializable;

/**
 * Created by Ashraf_Patel on 2/15/2016.
 */
public class User implements Serializable{
private String name,image;

    public User(){};


    public User(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
