package pt.isec.pd.spring_boot.exemplo3.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import pt.isec.pd.server.databaseManagement.UserDatabaseManager;
import pt.isec.pd.server.userManagment;

import java.util.ArrayList;
import java.util.List;

import static pt.isec.pd.server.server.SQLITEDB;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider
{
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        String DB_PATH = SQLITEDB;
        userManagment userManager = new userManagment(new UserDatabaseManager(DB_PATH));
        userManager.createAdminIfNotExists();

        if (username.equals("admin")) {
            userManager.checkPassword(username, password);
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ADMIN"));
            return new UsernamePasswordAuthenticationToken(username, password, authorities);
        }


        //String[] parts = body.split(" ");

        if(userManager.checkPassword(username, password)){
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("USER"));
            return new UsernamePasswordAuthenticationToken(username, password, authorities);
        }


        return null;
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
