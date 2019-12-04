package android.rmit.androidass2;

import java.util.List;

public class User {
    private String firstname, lastname,phone,gender,email;
    private List<String> sites;

    public User() {

    }

    public User(String firstname, String lastname, String phone, String gender, String email) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.phone = phone;
        this.gender = gender;
        this.email = email;
    }

    public User(String firstname, String lastname, String phone, String gender) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.phone = phone;
        this.gender = gender;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public List<String> getSites() {
        return sites;
    }

    public void setSites(List<String> sites) {
        this.sites = sites;
    }

    @Override
    public String toString() {
        return
                "firstname: " + firstname + '\n' +
                        ", lastname: " + lastname + '\n' +
                        ", phone: " + phone + '\n' +
                        ", gender: " + gender
                ;
    }
}