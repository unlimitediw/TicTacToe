package PlayHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * PlayHelper is a class help game connect to the server, send team and move information to server, and get the moves information back from server
 * @author Ruotong Wu
 * @version 1.2.0
 */
public class PlayHelper {
    private String link="http://www.notexponential.com/aip2pgaming/api/index.php";      //The server URL
    private String api_key="124ea51087aa8f0e86e1";                                      //The api_key, belongs to Ruotong Wu.
    private String userId="409";                                                        //The user id, belongs to Ruotong Wu.
    private String teamId="1052";                                                       //The team id, belongs to BicentennialMen.
    private int gameId;                                                                 //The game id. need obtain from server or user.

    /**
     * get the gameId of the game.
     * @return the gameId, Integer
     */
    public int GetGameId() {

        return this.gameId;
    }

    /**
     * Initialize the PlayHelper to connect to server with gameId
     * @param gameId the gameId which the would play
     */
    public PlayHelper(int gameId) {

        this.gameId=gameId;
    }
    /**
     * Initialize the PlayHelper to connect to server without gameId, two teamId who will play the game required
     * @param teamId1 the first team Id
     * @param teamId2 the second team Id
     * @throws Exception HTTP request may fail. or fail to create new game
     */
    public PlayHelper(String teamId1, String teamId2) throws Exception{
       boolean newGame= CreateGame(teamId1,teamId2);
       teamId=teamId1;
       if(!newGame){
           throw new Exception("Fail to create a new game!");
       }
    }

    /**
     * make a move, when you trying to make a move, that means you already have a teamid and a gameid.
     * @param x the row number of the move
     * @param y the column number of the move
     * @return move id, -1 means fail to make a move
     * @throws Exception HTTP request may fail.
     */

    public int MakeMoves(int x, int y) throws Exception {

        //parameters
        Map<String,String> arguments = new HashMap<>();
        arguments.put("type", "move");
        arguments.put("gameId", Integer.toString(gameId));
        arguments.put("teamId", teamId);
        arguments.put("move", Integer.toString(x)+","+Integer.toString(y));

        //Do the Post request and get the result in JSON.
        String jsonString=HttpPOST(arguments);

        //decode the result
        JSONObject json=new JSONObject(jsonString);
        if(json.getString("code").equals("OK")) {
            //  System.out.println(Integer.toString(moveid));
            return json.getInt("moveId");
        }else {
            // return -1 if the code is "FAIL"
            return -1;
        }
    }

    /**
     * Get the last "count" moves of the game
     * @param count the number of moves you want get
     * @return a list of move. The first element of the list is the last move of the game. If all information of the first element is -1, that means the code of the result is "FAIL"
     * @throws Exception HTTP request may fail.
     */
    public List<Move> GetLastMoves(int count) throws Exception {
        // format the link with game id and count
        String getLink=this.link+"?type=moves&gameId="+Integer.toString(this.gameId)+"&count="+Integer.toString(count);
        List<Move> move=new ArrayList<>();

        //request information from server, the result should in JSON
        String jsonString=HttpGET(getLink);
        JSONObject json=new JSONObject(jsonString);

        //decode the result
        if(json.getString("code").equals("OK")) {
            JSONArray jsa=json.getJSONArray("moves");
            for(int i=0;i<jsa.length();i++) {
                JSONObject js=jsa.getJSONObject(i);
                Move m=new Move(js.getInt("teamId"),js.getInt("gameId"),js.getInt("moveId"),js.getString("move"));
                move.add(m);
            }
        }else {
            //if the "code" if "FAIL", all the information of the first element of the result should be -1
            move.add(new Move(-1,-1,-1,-1,-1));
        }
        return move;
    }
    /**
     * Create a game with two team id
     * @param teamId1 the first team id
     * @param teamId2 the second team id
     * @return a logical variable, indicate if the game created successfully
     * @throws Exception HTTP request may fail.
     */
    private boolean CreateGame(String teamId1, String teamId2) throws Exception {

        //parameters
        Map<String,String> arguments=new HashMap<>();
        arguments.put("type", "game");
        arguments.put("teamId1", teamId1);
        arguments.put("teamId2", teamId2);

        //send the post request
        String jsonString=HttpPOST(arguments);

        //decode the return JSON string
        JSONObject json=new JSONObject(jsonString);
        if(json.getString("code").equals("OK")) {
            this.gameId=json.getInt("gameId"); //set the gamid to this.gameid
            return true;
        }else {
            return false;
        }
    }
    /**
     * the function help to send POST request to server
     * @param parameters the parameters of the request
     * @return the return information in JSON
     * @throws IOException HTTP request may fail.
     */
    private String HttpPOST(Map<String,String> parameters) throws Exception {

        StringBuilder content = new StringBuilder();
        while(content.toString().equals("")) {
            try {
                //setup the connection
                URL url = new URL(link);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);

                /* setup the parameters */
                StringJoiner sj;
                sj = new StringJoiner("&");
                for (Map.Entry<String, String> entry : parameters.entrySet())
                    sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                            + URLEncoder.encode(entry.getValue(), "UTF-8"));
                byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
                int length = out.length;

                //setup he header, which contains the api-key and userid
                con.setFixedLengthStreamingMode(length);
                con.setRequestProperty("x-api-key", api_key);
                con.setRequestProperty("userid", userId);

                //connect to the server
                con.connect();

                //send information to server
                try (OutputStream os = con.getOutputStream()) {
                    os.write(out);
                }

                //read return information
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                //disconnect from the server
                con.disconnect();
            }catch(Exception e){
                continue;
            }
        }

        return content.toString();
    }

    /**
     * the function help to send GET request to server
     * @param link the link this program will connect to
     * @return the result, should in JSON
     * @throws Exception HTTP request may fail.
     */
    private String HttpGET(String link) throws Exception {
        StringBuilder content = new StringBuilder();
        while(content.toString().equals("")) {
            try {
                //setup the connection
                URL url = new URL(link);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                //setup the header
                con.setRequestProperty("x-api-key", api_key);
                con.setRequestProperty("userid", userId);

                //read return information
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
            }catch (Exception e){
                continue;
            }
        }

        return content.toString();
    }
}
