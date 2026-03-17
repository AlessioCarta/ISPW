package com.ispw.uniride.controller;

import com.ispw.uniride.bean.UserBean;
import com.ispw.uniride.dao.DAOFactory;
import com.ispw.uniride.dao.StudentDAO;
import com.ispw.uniride.model.Student;

public class LoginController {

    public boolean login(UserBean userBean) {
        StudentDAO studentDAO = DAOFactory.getInstance().getStudentDAO();
        Student student = studentDAO.getStudentByUsername(userBean.getUsername());

        if (student != null && student.getPassword().equals(userBean.getPassword())) {
            Session.getInstance().setLoggedUser(student);
            return true;
        }
        return false;
    }

    public void logout() {
        Session.getInstance().logout();
    }
}
