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

            if(eventManager.removeEvent(id)){
                return ResponseEntity.ok("Event deleted successfully. ("+id+")");
            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");
            }

        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized.");
        }
    }

    @GetMapping("getEventCode")
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


            return ResponseEntity.ok(eventManager.getEventById(idEvent).getCode());

        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized.");
        }
    }


    @GetMapping("getEventPresences")
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


            return ResponseEntity.ok(eventManager.getEventById(idEvent).getUsersPresent());

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
                ev.addPresence(userManager.getUser(authentication.getName()));
                eventManager.editEvent(ev.getId(), ev);
            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");
            }
            //eventManager.getEventById(idEvent).addUserPresent(email);
            return ResponseEntity.ok("Presence registered successfully.)");

        }else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authorized.");
        }

    }



    /*@GetMapping("{type}")
    public ResponseEntity getText(@PathVariable("type") String type,
                                  @RequestParam(value="length", required=false) Integer length) {


        if (length == null)
            length = 1;

        return this.generateLorem(type, length, null);
    }

    @PostMapping
    public ResponseEntity postText(@RequestBody LoremConfig config) {
        if (config.getType() == null)
            return ResponseEntity.badRequest().body("Type is mandatory.");

        if (config.getLength() == null)
            config.setLength(1);

        return this.generateLorem(config.getType(), config.getLength(), null);
    }

    private ResponseEntity generateLorem(String type, Integer length, String prefix) {
        Lorem lorem = LoremIpsum.getInstance();

        switch(type.toLowerCase()) {
            case "word" -> {
                return ResponseEntity.ok((prefix==null ? "" : prefix+" -> ") + lorem.getWords(length));
            }
            case "paragraph" -> {
                return ResponseEntity.ok((prefix==null ? "" : prefix+" -> ") + lorem.getParagraphs(length, length));
            }
            default -> {
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Invalid type: " + type +".");
            }
        }
    }*/

}
