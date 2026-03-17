package com.ispw.uniride.boundary;

import com.ispw.uniride.bean.UserBean;
import com.ispw.uniride.controller.LoginController;

import java.util.Scanner;

public class LoginCLI {
    private LoginController loginController = new LoginController();

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Login UniRide ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        UserBean bean = new UserBean(username, password);

        if (loginController.login(bean)) {
            System.out.println("Login effettuato con successo!");
            new StudentCLI().start();
        } else {
            System.out.println("Credenziali non valide.");
        }
    }
}
