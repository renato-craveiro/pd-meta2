package pt.isec.pd.spring_boot.exemplo3.controllers;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pt.isec.pd.server.databaseManagement.EventDatabaseManager;
import pt.isec.pd.server.databaseManagement.UserDatabaseManager;
import pt.isec.pd.server.userManagment;

import pt.isec.pd.server.eventManagement;
import pt.isec.pd.types.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static pt.isec.pd.server.server.SQLITEDB;


@RestController
@RequestMapping("usercalls")
public class UserCallsController {


    @GetMapping("createdEvents")
    public ResponseEntity getCreatedEvents(Authentication authentication) {
        String subject = authentication.getName();
        if(authentication.getAuthorities().toString().contains("ADMIN")){
            String DB_PATH = SQLITEDB;

            eventManagement eventManager = new eventManagement(new EventDatabaseManager(DB_PATH));
            //eventManager.getEvents().toString();
            return ResponseEntity.ok(eventManager.getEvents().toString());

        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized.");
        }
    }

    @PostMapping("createEvent")
    public ResponseEntity createEvent(Authentication authentication,
                                      @RequestBody String body) throws ParseException {
        //String subject = authentication.getName();
        //Jwt userDetails = (Jwt) authentication.getPrincipal();
        //String scope = userDetails.getClaim("scope");

        if(authentication.getAuthorities().toString().contains("ADMIN")){
            String DB_PATH = SQLITEDB;

            eventManagement eventManager = new eventManagement(new EventDatabaseManager(DB_PATH));
            String[] parts = body.split(" ");
            String name = parts[0];
            String local = parts[1];
            String date = parts[2];
            String start = parts[3];
            String end = parts[4];

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat hourFormat = new SimpleDateFormat("hh:mm");
            Date dateTime = null, startHourTime = null, endHourTime = null;
            dateTime = dateFormat.parse(date);
            Calendar eventDate = Calendar.getInstance();
            eventDate.setTime(dateTime);
            startHourTime = hourFormat.parse(start);
            Calendar eventStart = Calendar.getInstance();
            eventStart.set(Calendar.HOUR_OF_DAY,startHourTime.getHours());
            eventStart.set(Calendar.MINUTE,startHourTime.getMinutes());
            endHourTime = hourFormat.parse(end);
            Calendar eventEnd = Calendar.getInstance();
            eventEnd.set(Calendar.HOUR_OF_DAY,endHourTime.getHours());
            eventEnd.set(Calendar.MINUTE,endHourTime.getMinutes());

            eventManager.createEvent(name, local, eventDate, eventStart, eventEnd);
            return ResponseEntity.ok("Event created successfully. ("+name+")");

        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized.");
        }
    }



    @DeleteMapping("deleteEvent")
    public ResponseEntity deleteEvent(Authentication authentication,
                                      @RequestBody String body) throws ParseException {
        String subject = authentication.getName();
        Jwt userDetails = (Jwt) authentication.getPrincipal();
        String scope = userDetails.getClaim("scope");

        if(authentication.getAuthorities().toString().contains("ADMIN")){
            String DB_PATH = SQLITEDB;

            eventManagement eventManager = new eventManagement(new EventDatabaseManager(DB_PATH));
            String[] parts = body.split(" ");
            int id = Integer.parseInt(parts[0]);

            if(eventManager.getEventById(id)==null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");

            if(eventManager.getEventById(id).getUsersPresentString().equals("[]")) {
                if (eventManager.removeEvent(id))
                    return ResponseEntity.ok("Event deleted successfully. (" + id + ")");
            } else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event has presences");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");


        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized.");
        }
    }

    @GetMapping("getEventCode/")
    public ResponseEntity getEventCode(Authentication authentication,
                                       @RequestParam(value = "id", required = true) String id) throws ParseException {
        String subject = authentication.getName();
        Jwt userDetails = (Jwt) authentication.getPrincipal();
        String scope = userDetails.getClaim("scope");

        if(authentication.getAuthorities().toString().contains("ADMIN")){
            String DB_PATH = SQLITEDB;

            eventManagement eventManager = new eventManagement(new EventDatabaseManager(DB_PATH));
            String[] parts = id.split(" ");
            int idEvent = Integer.parseInt(parts[0]);

            if(eventManager.getEventById(idEvent)==null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");
            else
                return ResponseEntity.ok(eventManager.getEventById(idEvent).getCode());

        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized.");
        }
    }


    @GetMapping("getEventPresences/")
    public ResponseEntity getEventPresences(Authentication authentication,
                                            @RequestParam(value = "id", required = true) String id) throws ParseException {
        String subject = authentication.getName();
        Jwt userDetails = (Jwt) authentication.getPrincipal();
        String scope = userDetails.getClaim("scope");

        if (authentication.getAuthorities().toString().contains("ADMIN")) {
            String DB_PATH = SQLITEDB;

            eventManagement eventManager = new eventManagement(new EventDatabaseManager(DB_PATH));
            String[] parts = id.split(" ");
            int idEvent = Integer.parseInt(parts[0]);

            if(eventManager.getEventById(idEvent) == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");

            return ResponseEntity.ok(eventManager.getEventById(idEvent).getUsersPresentString());

        }else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized.");
        }

    }

    @PostMapping("registerPresence")
    public ResponseEntity registerPresence(Authentication authentication,
                                           @RequestBody String body) throws ParseException {
        String subject = authentication.getName();
        Jwt userDetails = (Jwt) authentication.getPrincipal();
        String scope = userDetails.getClaim("scope");

        if (authentication.getAuthorities().toString().contains("USER")) {
            String DB_PATH = SQLITEDB;

            eventManagement eventManager = new eventManagement(new EventDatabaseManager(DB_PATH));
            userManagment userManager = new userManagment(new UserDatabaseManager(DB_PATH));
            String[] parts = body.split(" ");
            String code = parts[0];
            //String email = parts[1];
            event ev = eventManager.getEventByCode(code);
            if(ev!=null) {
                if (ev.checkPresenceEmail(subject))
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Already registred.");
                ev.addPresence(userManager.getUser(authentication.getName()));
                eventManager.editEvent(ev.getId(), ev);
            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");
            }
            //eventManager.getEventById(idEvent).addUserPresent(email);
            return ResponseEntity.ok("Presence registered successfully.");

        }else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized.");
        }

    }

    @GetMapping("eventsPresent")
    public ResponseEntity getEventsPresent(Authentication authentication) {
        String subject = authentication.getName();

        if(authentication.getAuthorities().toString().contains("USER")){
            String DB_PATH = SQLITEDB;

            eventManagement eventManager = new eventManagement(new EventDatabaseManager(DB_PATH));
            StringBuilder sb = new StringBuilder();
            int counter = 0;
            for (event e : eventManager.getEvents()) {
                if (e.checkPresenceEmail(subject)) {
                    sb.append(e.toClientString());
                    sb.append("\n");
                    counter++;
                }
            }
            if (counter == 0)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No events found");


            return ResponseEntity.ok(sb);

        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized.");
        }
    }

    @GetMapping("getRole")
    public ResponseEntity getRole(Authentication authentication) {

        if(authentication.getAuthorities().toString().contains("USER"))
            return  ResponseEntity.ok("User");
        else if (authentication.getAuthorities().toString().contains("ADMIN"))
            return ResponseEntity.ok("Admin");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized.");

    }


    @PostMapping("filterPresences")
    public ResponseEntity filterPresences(Authentication authentication,
                                      @RequestBody String body) throws ParseException {


        String subject = authentication.getName();
        Jwt userDetails = (Jwt) authentication.getPrincipal();
        String scope = userDetails.getClaim("scope");

        if(authentication.getAuthorities().toString().contains("USER")){
            String DB_PATH = SQLITEDB;

            eventManagement eventManager = new eventManagement(new EventDatabaseManager(DB_PATH));
            StringBuilder sb = new StringBuilder();
            int counter = 0;

            String[] parts = body.split(" ");
            String code = parts[0];
            String name = null, local = null, date = null;
            boolean getName=false, getLocal=false, getDate=false;

            for (int i=0;i<parts.length;i+=2) {
                if (parts[i].equals("1")) {
                    getName=true;
                    name = parts[i + 1];
                }if (parts[i].equals("2")) {
                    getLocal=true;
                    local = parts[i + 1];
                }if (parts[i].equals("3")) {
                    getDate=true;
                    date = parts[i + 1];
                }
            }


            if (getName && getLocal && getDate){
                for (event e : eventManager.getEvents()) {
                    if (e.checkPresenceEmail(subject)) {
                        if (e.getName().equals(name) && e.getFormatDate(e.getDate()).equals(date) && e.getLocal().equals(local)) {
                            sb.append(e.toClientString());
                            sb.append("\n");
                            counter++;
                        }
                    }
                }
            }else if (getName && getLocal){
                for (event e : eventManager.getEvents()) {
                    if (e.checkPresenceEmail(subject)) {
                        if (e.getName().equals(name) && e.getLocal().equals(local)) {
                            sb.append(e.toClientString());
                            sb.append("\n");
                            counter++;
                        }
                    }
                }
            }else if (getName && getLocal){
                for (event e : eventManager.getEvents()) {
                    if (e.checkPresenceEmail(subject)) {
                        if (e.getName().equals(name) && e.getLocal().equals(local)) {
                            sb.append(e.toClientString());
                            sb.append("\n");
                            counter++;
                        }
                    }
                }
            }else if (getDate && getLocal){
                for (event e : eventManager.getEvents()) {
                    if (e.checkPresenceEmail(subject)) {
                        if (e.getFormatDate(e.getDate()).equals(date) && e.getLocal().equals(local)) {
                            sb.append(e.toClientString());
                            sb.append("\n");
                            counter++;
                        }
                    }
                }
            }else if (getName){
                for (event e : eventManager.getEvents()) {
                    if (e.checkPresenceEmail(subject)) {
                        if (e.getName().equals(name)) {
                            sb.append(e.toClientString());
                            sb.append("\n");
                            counter++;
                        }
                    }
                }
            }else if (getDate){
                for (event e : eventManager.getEvents()) {
                    if (e.checkPresenceEmail(subject)) {
                        if (e.getFormatDate(e.getDate()).equals(date)) {
                            sb.append(e.toClientString());
                            sb.append("\n");
                            counter++;
                        }
                    }
                }
            }else if (getLocal){
                for (event e : eventManager.getEvents()) {
                    if (e.checkPresenceEmail(subject)) {
                        if (e.getLocal().equals(local)) {
                            sb.append(e.toClientString());
                            sb.append("\n");
                            counter++;
                        }
                    }
                }
            }

            if (counter == 0)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No events found");


            return ResponseEntity.ok(sb);

        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized.");
        }
    }



    @PostMapping("filterEvents")
    public ResponseEntity filterEvents(Authentication authentication,
                                          @RequestBody String body) throws ParseException {


        String subject = authentication.getName();
        Jwt userDetails = (Jwt) authentication.getPrincipal();
        String scope = userDetails.getClaim("scope");

        if(authentication.getAuthorities().toString().contains("ADMIN")){
            String DB_PATH = SQLITEDB;

            eventManagement eventManager = new eventManagement(new EventDatabaseManager(DB_PATH));
            StringBuilder sb = new StringBuilder();
            int counter = 0;

            String[] parts = body.split(" ");
            String code = parts[0];
            String name = null, local = null, date = null;
            boolean getName=false, getLocal=false, getDate=false;

            for (int i=0;i<parts.length;i+=2) {
                if (parts[i].equals("1")) {
                    getName=true;
                    name = parts[i + 1];
                }if (parts[i].equals("2")) {
                    getLocal=true;
                    local = parts[i + 1];
                }if (parts[i].equals("3")) {
                    getDate=true;
                    date = parts[i + 1];
                }
            }

            if (getName && getLocal && getDate){
                for (event e : eventManager.getEvents()) {
                    if (e.getName().equals(name) && e.getFormatDate(e.getDate()).equals(date) && e.getLocal().equals(local)) {
                        sb.append(e.toClientRESTString());
                        sb.append("\n");
                        counter++;
                    }
                }
            }else if (getName && getLocal){
                for (event e : eventManager.getEvents()) {

                        if (e.getName().equals(name) && e.getLocal().equals(local)) {
                            sb.append(e.toClientRESTString());
                            sb.append("\n");
                            counter++;
                        }
                }
            }else if (getName && getLocal){
                for (event e : eventManager.getEvents()) {
                        if (e.getName().equals(name) && e.getLocal().equals(local)) {
                            sb.append(e.toClientRESTString());
                            sb.append("\n");
                            counter++;
                        }

                }
            }else if (getDate && getLocal){
                for (event e : eventManager.getEvents()) {
                        if (e.getFormatDate(e.getDate()).equals(date) && e.getLocal().equals(local)) {
                            sb.append(e.toClientRESTString());
                            sb.append("\n");
                            counter++;
                        }

                }
            }else if (getName){
                for (event e : eventManager.getEvents()) {
                        if (e.getName().equals(name)) {
                            sb.append(e.toClientRESTString());
                            sb.append("\n");
                            counter++;
                        }

                }
            }else if (getDate){
                for (event e : eventManager.getEvents()) {
                        if (e.getFormatDate(e.getDate()).equals(date)) {
                            sb.append(e.toClientRESTString());
                            sb.append("\n");
                            counter++;
                        }

                }
            }else if (getLocal){
                for (event e : eventManager.getEvents()) {
                        if (e.getLocal().equals(local)) {
                            sb.append(e.toClientRESTString());
                            sb.append("\n");
                            counter++;
                        }

                }
            }

            if (counter == 0)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No events found");


            return ResponseEntity.ok(sb);

        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized.");
        }
    }



}
