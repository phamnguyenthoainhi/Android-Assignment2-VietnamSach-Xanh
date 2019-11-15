package android.rmit.androidass2;

public class User {
    private String firstname;
    private String lastname;
    private String phone;
    private String gender;

    public User() {
    }

    public User(String firstname, String lastname, String phone, String gender) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.phone = phone;
        this.gender = gender;
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