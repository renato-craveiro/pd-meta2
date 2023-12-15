package pt.isec.pd.spring_boot.exemplo3.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isec.pd.server.databaseManagement.UserDatabaseManager;
import pt.isec.pd.server.userManagment;
import pt.isec.pd.types.user;

import static pt.isec.pd.server.server.SQLITEDB;

@RestController
public class RegisterController {
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody String body) {
        String DB_PATH = SQLITEDB;
        userManagment userManager = new userManagment(new UserDatabaseManager(DB_PATH));
        String[] parts = body.split(" ");
        String nestudante = parts[0];
        String password = parts[1];
        String email = parts[2];
        String name = parts[3];

        System.out.println(nestudante+password+email+name);

        if(userManager.checkUser(email)){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists.");
        }
        userManager.createUser(new user(name, nestudante, email, password));

        return ResponseEntity.ok("User created successfully. ("+email+")");
    }

}


