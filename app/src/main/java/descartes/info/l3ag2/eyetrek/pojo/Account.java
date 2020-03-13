package descartes.info.l3ag2.eyetrek.pojo;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Ayaz ABDUL CADER on 19/02/2018.
 */

public class Account {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("last_name")
    @Expose
    private String lastName;
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @SerializedName("birthday")
    @Expose
    private String birthday;
    @SerializedName("year_exp")
    @Expose
    private Integer yearExp;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("password")
    @Expose
    private String password;
    public Account(){

    }
    public Account(Integer id, String lastName, String firstName, String birthday, Integer yearExp, String email, String password) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
        this.yearExp = yearExp;
        this.email = email;
        this.password = password;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public Integer getYearExp() {
        return yearExp;
    }

    public void setYearExp(Integer yearExp) {
        this.yearExp = yearExp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**

     * Génère un JsonObject de la classe

     * @return

     */

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        try{
            jsonObject.addProperty("id", getId());
            jsonObject.addProperty("last_name", getLastName());
            jsonObject.addProperty("first_name",getFirstName());
            jsonObject.addProperty("birthday", getBirthday());
            jsonObject.addProperty("year_exp",getYearExp());
            jsonObject.addProperty("email",getEmail());
            jsonObject.addProperty("password",getPassword());
            return jsonObject;

        }catch (JsonIOException e){
            e.printStackTrace();
        }

        return null;
    }
    public JsonObject toJson2(){
        JsonObject jsonObject = new JsonObject();
        try{
            jsonObject.addProperty("last_name", getLastName());
            jsonObject.addProperty("first_name",getFirstName());
            jsonObject.addProperty("birthday", getBirthday());
            jsonObject.addProperty("year_exp",getYearExp());
            jsonObject.addProperty("email",getEmail());
            jsonObject.addProperty("password",getPassword());
            return jsonObject;

        }catch (JsonIOException e){
            e.printStackTrace();
        }

        return null;
    }

}
