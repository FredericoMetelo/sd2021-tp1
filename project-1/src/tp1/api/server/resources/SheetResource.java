package tp1.api.server.resources;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import tp1.api.*;
import tp1.api.server.Discovery;
import tp1.api.service.rest.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;


//TODO NEEDED DATA STRUCTURES TO SAVE SHEETS


@Singleton
public class SheetResource implements RestSpreadsheets {
    //===========================Server Communications Metadata============================================//
    private static final String USER_SERVER_NAME = "users";
    private static final String SHEET_SERVER_NAME = "sheets";
    public static final int TIMEOUT = 2000;
    public static final int RETRIES_NO = 3;
    public static final int[] ALL_CELLS = null;
    //===========================Internal Logging=========================================================//
    public static final String NO_SERVERS_FOUND = "No Servers found...";
    private static Logger Log = Logger.getLogger(SheetResource.class.getName());

    //===========================Spreadsheet Data Structures=============================================//
    private final Map<String, Spreadsheet> sheetsDB = new HashMap<>();

    //===========================User server comunications===============================================//
    private  final ClientConfig userClientConfig = new ClientConfig(); // declarar como static se calhar faz sentido. Honestamente explorar as ramificacoes disto...
    private  Client userClient = null;
    private  WebTarget userTarget = null;

    //===========================Sheet server comunications=============================================//
    private  final ClientConfig sheetClientConfig = new ClientConfig(); // declarar como static se calhar faz sentido. Honestamente explorar as ramificacoes disto...
    private  Client sheetClient = null;
    private  WebTarget sheetTarget = null;
    //==================================================================================================//




    @Override
    public String createSpreadsheet(Spreadsheet sheet, String password) {

        // Bad Param => 400
        if(sheet.getSheetId() == null || sheet.getOwner() == null || password  == null) {
            Log.info("User object invalid.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST);
        }
        // Validation problems => 400
        String serviceName = sheet.getOwner().split("@")[1] ;
        User u;
        try{
            u = validateUser(password, sheet.getOwner(), serviceName);
            if(u == null){
                Log.info(NO_SERVERS_FOUND);
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
        } catch (WebApplicationException w){
            // Overides the code sent by the user validation
            throw new WebApplicationException(Response.Status.BAD_REQUEST); // TODO Molho da bolonhesa, pq o esparguete esta feito...
        }

        // Business intelligence
        synchronized ( this ) {
            //TODO preencher o sheetId e o sheetURL
            String sheetId = sheet.getOwner() + System.nanoTime();
            String domain = sheet.getOwner().split("@")[0];
            String sheetUrl = compileSheetURL(sheetId, domain);

            sheetsDB.put(sheet.getSheetId(), sheet);
        }

        return sheet.getSheetId();
    }

    private String compileSheetURL(String sheetId, String domain){
        return "http://" + SHEET_SERVER_NAME + "."+ domain +"" /*TODO set port*/ + "/" +sheetId;
    }
    @Override
    public void deleteSpreadsheet(String sheetId, String password) {
        // Bad Param => 400
        if(sheetId == null || password  == null) {
            Log.info("User object invalid.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST);
        }
        // Spreadsheet exists No => 404
        Spreadsheet sheet = sheetsDB.get(sheetId);
        if(sheet == null) throw new WebApplicationException(Response.Status.NOT_FOUND);

        // User is valid wrong pass => 403 User not found =>404 | spirals up from validation
        String serviceName = sheet.getOwner().split("@")[1] ;
        User u = validateUser(password, sheet.getOwner(), serviceName);
        if(u == null){
            Log.info(NO_SERVERS_FOUND);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        // Business intelligence
        sheetsDB.remove(sheetId);
    }

    @Override
    public Spreadsheet getSpreadsheet(String sheetId, String userId, String password) {
        // Bad Param => 400
        if(sheetId == null || password  == null) {
            Log.info("User object invalid.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST);
        }

        // User is valid wrong pass => 403 User not found =>404 | spirals up from validation
        String serviceName = userId.split("@")[1] ;
        User u = validateUser(password, userId, serviceName);
        if(u == null){
            Log.info(NO_SERVERS_FOUND);
        }

        //Business intelligence: LOCAL
        Spreadsheet sLoc = sheetsDB.get(sheetId);
        if(sLoc == null){
            //Buisiness intelligence: REMOTE
            //TODO remote spreadsheet options?


            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }else{
            return sLoc;
        }
    }

    @Override // Versao q computa formulas
    public String[][] getSpreadsheetValues(String sheetId, String userId, String password) {
// Bad Param => 400
        if(sheetId == null || password  == null) {
            Log.info("User object invalid.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST);
        }

        // User is valid wrong pass => 403 User not found =>404 | spirals up from validation
        String serviceName = userId.split("@")[1] ;
        User u = validateUser(password, userId, serviceName);
        if(u == null){
            Log.info(NO_SERVERS_FOUND);
        }

        //Business intelligence: LOCAL
        Spreadsheet sLoc = sheetsDB.get(sheetId);
        if(sLoc == null){
            //Buisiness intelligence: REMOTE
            //TODO remote spreadsheet options?


            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }else{
            return sLoc.getRawValues();
        }
    }

    @Override
    public void updateCell(String sheetId, String cell, String rawValue, String userId, String password) {

    }

    @Override
    public void shareSpreadsheet(String sheetId, String userId, String password) {

    }

    @Override
    public void unshareSpreadsheet(String sheetId, String userId, String password) {

    }

    private User validateUser(String password, String userID, String domain) {

        String serviceName = domain +":"+ USER_SERVER_NAME;
        User u = null;
        URI[] uris = Discovery.getInstance().knownUrisOf(serviceName);
        int noTries = 0;
        while (uris == null && noTries < RETRIES_NO) {

            try {
                Thread.sleep(TIMEOUT);
                uris = Discovery.getInstance().knownUrisOf(serviceName);
                noTries ++;
                if (uris == null) System.out.println("Did Not Found the Server, Trying again in 2sec");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(uris == null) return null;

        String serverUrl = uris[0].toString();
        System.out.println("Sending request to server.");

        synchronized (userClientConfig){
            if(userClient == null)
                userClient = ClientBuilder.newClient(userClientConfig);
            if(userTarget == null && userClient != null)
                userTarget = userClient.target(serverUrl).path(RestUsers.PATH);
        }


        Response r = userTarget.path(userID).queryParam("password", password).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        if (r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity()) {
            System.out.println("Success:");
            u = r.readEntity(User.class);
            System.out.println("User : " + u);
            return u;

        } else if(r.getStatus() == Response.Status.FORBIDDEN.getStatusCode()){
            System.out.println("Error, HTTP error status: " + r.getStatus());
            throw new WebApplicationException(r.getStatus()); // TODO isto funciona? Testar? Como conseguir o erro correto

        }else if(r.getStatus() == Response.Status.NOT_FOUND.getStatusCode()){
            System.out.println("Error, HTTP error status: " + r.getStatus());
            throw new WebApplicationException(r.getStatus()); // TODO isto funciona? Testar? Como conseguir o erro correto

        }else{
            System.out.println("Error, HTTP error status: " + r.getStatus());
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    private Spreadsheet requestRemoteSheet(String domain, String sheetID, String userId, String password, int[] cells){

        String serviceName = domain +":"+ SHEET_SERVER_NAME;
        User u = null;
        URI[] uris = Discovery.getInstance().knownUrisOf(serviceName);
        int noTries = 0;
        while (uris == null && noTries < RETRIES_NO) {
            try {
                Thread.sleep(TIMEOUT);
                uris = Discovery.getInstance().knownUrisOf(serviceName);
                noTries ++;
                if (uris == null) System.out.println("Did Not Found the Server, Trying again in 2sec");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(uris == null) return null;

        String serverUrl = uris[0].toString();
        System.out.println("Sending request to server.");

        synchronized (sheetClientConfig){
            if(sheetClient == null)
                sheetClient = ClientBuilder.newClient(sheetClientConfig);
            if(sheetTarget == null && sheetClient != null)
                sheetTarget = sheetClient.target(serverUrl).path(RestSpreadsheets.PATH);
        }
        Response r;
        if(cells == ALL_CELLS){
            //Whole spreadsheet: Francamente nao sei se isto se faz assim... Nos aqui ja temos garantia
            // de q o utilizador esta correto. Mas nao tenho a certeza tbh, se podemos ignorar ate pq o
            // outro servidor nao tem essa garantia.
            r= sheetTarget.path(sheetID).queryParam("userId", userId).queryParam("password", password).request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();
        }else{
            //An interval
            if(cells.length != 2) throw new WebApplicationException(Response.Status.BAD_REQUEST);
            r= sheetTarget.path(sheetID/*TODO end path and set query param*/).request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();
        }


        if (r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity()) {
            //TODO process sheetTarget
            return s;

        } else if(r.getStatus() == Response.Status.FORBIDDEN.getStatusCode()){
            System.out.println("Error, HTTP error status: " + r.getStatus());
            throw new WebApplicationException(r.getStatus()); // TODO isto funciona? Testar? Como conseguir o erro correto

        }else if(r.getStatus() == Response.Status.NOT_FOUND.getStatusCode()){
            System.out.println("Error, HTTP error status: " + r.getStatus());
            throw new WebApplicationException(r.getStatus()); // TODO isto funciona? Testar? Como conseguir o erro correto

        }else{
            System.out.println("Error, HTTP error status: " + r.getStatus());
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }
}
