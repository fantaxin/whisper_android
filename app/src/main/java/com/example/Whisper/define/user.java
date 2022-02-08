
package com.example.Whisper.define;

public class user {
    private String name;
    private String password;

    public user(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toString() {
        return "User{name='" + this.name + '\'' + ", password='" + this.password + '\'' + '}';
    }
}
