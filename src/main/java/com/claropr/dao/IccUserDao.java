package com.claropr.dao;

import com.claropr.model.Users;
import java.util.List;

public interface IccUserDao {
    List<Users> getUserByUsername(String username);
}

