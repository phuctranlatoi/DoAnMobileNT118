package com.example.doannt118.repository;


import com.example.doannt118.model.TaiKhoan;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static final List<TaiKhoan> users = new ArrayList<>();

    public boolean addUser(TaiKhoan user) {
        // Kiểm tra trùng username
        for (TaiKhoan u : users) {
            if (u.getTenDangNhap().equalsIgnoreCase(user.getTenDangNhap())) {
                return false; // trùng
            }
        }
        users.add(user);
        return true;
    }

    public TaiKhoan getUser(String username, String password) {
        for (TaiKhoan u : users) {
            if (u.getTenDangNhap().equalsIgnoreCase(username)
                    && u.getMatKhau().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public List<TaiKhoan> getAllUsers() {
        return users;
    }
}

