package com.csye6225.spring2018.pojo;
import javax.persistence.*;
import java.sql.Blob;


@Entity
    public class User {

        public User() {
        }

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private long userId;

        @Column(nullable = false, unique = true)
        private String userName;

        @Column
        private String password;

        @Column
        private String name;//Flag to check if image is present for the user. Takes Y or N as values

        @Column
        private String aboutMe;

        @Column
        private byte[] fileContent;


        public User(String userName, String password){
            this.userName = userName;
            this.password = password;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAboutMe() {
            return aboutMe;
        }

        public void setAboutMe(String aboutMe) {
            this.aboutMe = aboutMe;
        }

        public byte[] getFileContent() {
            return fileContent;
        }

        public void setFileContent(byte[] fileContent) {
            this.fileContent = fileContent;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }

