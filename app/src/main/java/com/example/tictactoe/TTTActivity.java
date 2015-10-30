package com.example.tictactoe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TTTActivity extends ActionBarActivity {
    String name;
    String group = "";
    String symbol;
    String opponent_symbol;

    // TAG for logging
    private static final String TAG = "TTTActivity";

    // server to connect to
    protected static final int GROUPCAST_PORT = 20000;
    protected static final String GROUPCAST_SERVER = "52.89.44.173";

    // networking
    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;
    boolean connected = false;

    // UI elements
    Button board[][] = new Button[3][3];
    TextView GameStatus = null;
    Button bConnect = null;
    EditText etName = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttt);

        // find UI elements defined in xml
        bConnect = (Button) this.findViewById(R.id.bConnect);
        etName = (EditText) this.findViewById(R.id.etName);
        board[0][0] = (Button) this.findViewById(R.id.b00);
        board[0][1] = (Button) this.findViewById(R.id.b01);
        board[0][2] = (Button) this.findViewById(R.id.b02);
        board[1][0] = (Button) this.findViewById(R.id.b10);
        board[1][1] = (Button) this.findViewById(R.id.b11);
        board[1][2] = (Button) this.findViewById(R.id.b12);
        board[2][0] = (Button) this.findViewById(R.id.b20);
        board[2][1] = (Button) this.findViewById(R.id.b21);
        board[2][2] = (Button) this.findViewById(R.id.b22);
        GameStatus = (TextView) this.findViewById(R.id.GameStatus);

        // hide login controls
        hideLoginControls();

        // make the board non-clickable
        disableBoardClick();

        // hide the board
        hideBoard();

        // assign OnClickListener to connect button
        bConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                name = etName.getText().toString();
                // sanitity check: make sure that the name does not start with an @ character
                if (name == null || name.startsWith("@")) {
                    Toast.makeText(getApplicationContext(), "Invalid name",
                            Toast.LENGTH_SHORT).show();
                } else {
                    send("NAME,"+etName.getText());
                }
            }
        });


        // assign a common OnClickListener to all board buttons
        View.OnClickListener boardClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int x, y;
                disableBoardClick();
                switch (v.getId()) {
                    case R.id.b00:
                        x = 0;
                        y = 0;
                        //send the coordinates to server
                        send("MSG," + group + ",Move*0,0");
                        //disable button b00
                        board[x][y].setEnabled(false);
                        //draw an x or o on the button
                        board[x][y].setText(symbol);
                        break;
                    case R.id.b01:
                        x = 0;
                        y = 1;
                        //send the coordinates to server
                        send("MSG," + group + ",Move*0,1");
                        //disable button b00
                        board[x][y].setEnabled(false);
                        //draw an x or o on the button
                        board[x][y].setText(symbol);
                        break;
                    case R.id.b02:
                        x = 0;
                        y = 2;
                        //send the coordinates to server
                        send("MSG," + group + ",Move*0,2");
                        //disable button b00
                        board[x][y].setEnabled(false);
                        //draw an x or o on the button
                        board[x][y].setText(symbol);
                        break;
                    case R.id.b10:
                        x = 1;
                        y = 0;
                        //send the coordinates to server
                        send("MSG," + group + ",Move*1,0");
                        //disable button b00
                        board[x][y].setEnabled(false);
                        //draw an x or o on the button
                        board[x][y].setText(symbol);
                        break;
                    case R.id.b11:
                        x = 1;
                        y = 1;
                        //send the coordinates to server
                        send("MSG," + group + ",Move*1,1");
                        //disable button b00
                        board[x][y].setEnabled(false);
                        //draw an x or o on the button
                        board[x][y].setText(symbol);
                        break;
                    case R.id.b12:
                        x = 1;
                        y = 2;
                        //send the coordinates to server
                        send("MSG," + group + ",Move*1,2");
                        //disable button b00
                        board[x][y].setEnabled(false);
                        //draw an x or o on the button
                        board[x][y].setText(symbol);
                        break;
                    case R.id.b20:
                        x = 2;
                        y = 0;
                        //send the coordinates to server
                        send("MSG," + group + ",Move*2,0");
                        //disable button b00
                        board[x][y].setEnabled(false);
                        //draw an x or o on the button
                        board[x][y].setText(symbol);
                        break;
                    case R.id.b21:
                        x = 2;
                        y = 1;
                        //send the coordinates to server
                        send("MSG," + group + ",Move*2,1");
                        //disable button b00
                        board[x][y].setEnabled(false);
                        //draw an x or o on the button
                        board[x][y].setText(symbol);
                        break;
                    case R.id.b22:
                        x = 2;
                        y = 2;
                        //send the coordinates to server
                        send("MSG," + group + ",Move*2,2");
                        //disable button b00
                        board[x][y].setEnabled(false);
                        //draw an x or o on the button
                        board[x][y].setText(symbol);
                        break;
                    default:
                        break;
                }
            }
        };

        // assign OnClickListeners to board buttons
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                board[x][y].setOnClickListener(boardClickListener);


        // start the AsyncTask that connects to the server
        // and listens to whatever the server is sending to us
        connect();

    }

    String checkWin(){
        for(int i=0;i<=2;i++) {
            if (board[i][0].getText() == board[i][1].getText() && board[i][1].getText() == board[i][2].getText()){
                return board[i][0].getText().toString();
            }
        }
        for(int i=0;i<=2;i++) {
            if (board[0][i].getText() == board[1][i].getText() && board[1][i].getText() == board[2][i].getText()){
                return board[0][i].getText().toString();
            }
        }
        if (board[0][0].getText() == board[1][1].getText() && board[1][1].getText() == board[2][2].getText()){
            return board[0][0].getText().toString();
        }
        if (board[2][0].getText() == board[1][1].getText() && board[1][1].getText() == board[0][2].getText()){
            return board[0][0].getText().toString();
        }
        return "No Winner Yet";
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy called");
        disconnect();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle menu click events
        if (item.getItemId() == R.id.exit) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ttt, menu);
        return true;
    }




    /***************************************************************************/
    /********* Networking ******************************************************/
    /***************************************************************************/

    /**
     * Connect to the server. This method is safe to call from the UI thread.
     */
    void connect() {

        new AsyncTask<Void, Void, String>() {

            String errorMsg = null;

            @Override
            protected String doInBackground(Void... args) {
                Log.i(TAG, "Connect task started");
                try {
                    connected = false;
                    socket = new Socket(GROUPCAST_SERVER, GROUPCAST_PORT);
                    Log.i(TAG, "Socket created");
                    in = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream());

                    connected = true;
                    Log.i(TAG, "Input and output streams ready");

                } catch (UnknownHostException e1) {
                    errorMsg = e1.getMessage();
                } catch (IOException e1) {
                    errorMsg = e1.getMessage();
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (IOException ignored) {
                    }
                }
                Log.i(TAG, "Connect task finished");
                return errorMsg;
            }

            @Override
            protected void onPostExecute(String errorMsg) {
                if (errorMsg == null) {
                    Toast.makeText(getApplicationContext(),
                            "Connected to server", Toast.LENGTH_SHORT).show();

                    hideConnectingText();
                    showLoginControls();

                    // start receiving
                    receive();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                    // can't connect: close the activity
                    finish();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Start receiving one-line messages over the TCP connection. Received lines are
     * handled in the onProgressUpdate method which runs on the UI thread.
     * This method is automatically called after a connection has been established.
     */

    void receive() {
        new AsyncTask<Void, String, Void>() {

            @Override
            protected Void doInBackground(Void... args) {
                Log.i(TAG, "Receive task started");
                try {
                    while (connected) {

                        String msg = in.readLine();

                        if (msg == null) { // other side closed the
                            // connection
                            break;
                        }
                        publishProgress(msg);
                    }

                } catch (UnknownHostException e1) {
                    Log.i(TAG, "UnknownHostException in receive task");
                } catch (IOException e1) {
                    Log.i(TAG, "IOException in receive task");
                } finally {
                    connected = false;
                    try {
                        if (out != null)
                            out.close();
                        if (socket != null)
                            socket.close();
                    } catch (IOException e) {
                    }
                }
                Log.i(TAG, "Receive task finished");
                return null;
            }

            @Override
            protected void onProgressUpdate(String... lines) {
                // the message received from the server is
                // guaranteed to be not null
                String msg = lines[0];
                Log.i("ServerReply",msg);//print out server reply msg on console for debugging use
                Pattern p = Pattern.compile("OK,NAME");
                Matcher m = p.matcher(msg);
                if(m.find()){
                    //send query for list of groups
                    hideLoginControls();
                    showBoard();
                    GameStatus.setText("Waiting for Opponent...");
                    send("LIST,GROUPS");
                    return;
                }
                //receive a list of available groups
                p = Pattern.compile("OK,LIST,GROUPS");
                m = p.matcher(msg);
                if(m.find()){
                    String delims = "[:]+";
                    String[] tokens = msg.split(delims);
                    String group_to_join="";
                    if(tokens.length == 2) {
                        String groups = tokens[1];
                        delims = "[,]+";
                        tokens = groups.split(delims);
                        for (int i = 0; i < tokens.length; i++) {
                            p = Pattern.compile("1/2");
                            m = p.matcher(tokens[i]);
                            if (m.find()) { //if there is a group with an available spot
                                group_to_join = tokens[i];
                                delims = "[(]+";
                                String[] temp = group_to_join.split(delims);
                                group_to_join = temp[0];
                                group_to_join = group_to_join.replace("[", "");//extract the group name from the msg
                                Log.i("Group Name:", group_to_join);
                                break;//no need to continue searching
                            }
                        }
                    }
                    if(!group_to_join.equals("")){//if there is an available group
                        send("JOIN,"+group_to_join);
                        group = group_to_join;
                    }
                    else{
                        Log.i("XX",name);
                        send("JOIN,@"+name+",2");//use the unique username as the group name to avoid duplication
                        group = name;
                    }
                    return;
                }
                p = Pattern.compile("OK,JOIN");
                m = p.matcher(msg);
                Pattern q = Pattern.compile("2/2");
                Matcher n = q.matcher(msg);
                if(m.find() && n.find()){ //if the game is ready to start
                    send("MSG,"+group+",Start Game!");
                    GameStatus.setText("Game Started!");
                    symbol = "X";
                    opponent_symbol = "O";
                    return;
                }

                //if the another player just joined the group and the group is now full
                //start the game
                p = Pattern.compile("Start Game!");
                m = p.matcher(msg);
                q = Pattern.compile("OK");
                n = q.matcher(msg);
                if(m.find() && !n.find()){
                    enableBoardClick();
                    GameStatus.setText("Game Started!");
                    symbol = "O";
                    opponent_symbol = "X";
                    return;
                }

                //if placing pieces
                //draw x or o on the corresponding button
                //disable the button
                //enable the rest of the buttons
                p = Pattern.compile("Move");
                m = p.matcher(msg);
                q = Pattern.compile("OK");
                n = q.matcher(msg);
                if(m.find() && !n.find()){ //if client receives a message containing a movement action of the opponent
                    String delims = "[*]+";
                    String[] tokens = msg.split(delims);
                    String coordinate = tokens[1];
                    delims = "[,]+";
                    tokens = coordinate.split(delims);
                    int x = Integer.parseInt(tokens[0]);
                    int y = Integer.parseInt(tokens[1]);
                    //draw the opponent's symbol on the corresponding button
                    board[x][y].setText(opponent_symbol);
                    //disable the button
                    board[x][y].setEnabled(false);
                    enableBoardClick();
                    String winner_symbol = checkWin();
                    if(winner_symbol.equals(opponent_symbol)){ //if the opponent has won the game
                        send("MSG," + group + ",You Won!");
                        disableBoardClick();
                        GameStatus.setText("You Lost!");
                    }
                    return;
                }

                p = Pattern.compile("You Win!");
                m = p.matcher(msg);
                q = Pattern.compile("OK");
                n = q.matcher(msg);
                if(m.find() && !n.find()) {
                    disableBoardClick();
                    GameStatus.setText("You Won!");
                }
                if(msg.startsWith("+ERROR,NAME")) {
                    Toast.makeText(getApplicationContext(), msg.substring("+ERROR,NAME,".length()), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(msg.startsWith("+OK")) { //Handle all other acknowledgements from the server
                    return;
                }
                // [ ... and so on for other kinds of messages]


                // if we haven't returned yet, tell the user that we have an unhandled message
                Toast.makeText(getApplicationContext(), "Unhandled message: "+msg, Toast.LENGTH_SHORT).show();
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    /**
     * Disconnect from the server
     */
    void disconnect() {
        new Thread() {
            @Override
            public void run() {
                if (connected) {
                    connected = false;
                }
                // make sure that we close the output, not the input
                if (out != null) {
                    out.print("BYE");
                    out.flush();
                    out.close();
                }
                // in some rare cases, out can be null, so we need to close the socket itself
                if (socket != null)
                    try { socket.close();} catch(IOException ignored) {}

                Log.i(TAG, "Disconnect task finished");
            }
        }.start();
    }

    /**
     * Send a one-line message to the server over the TCP connection. This
     * method is safe to call from the UI thread.
     *
     * @param msg
     *            The message to be sent.
     * @return true if sending was successful, false otherwise
     */
    boolean send(String msg) {
        if (!connected) {
            Log.i(TAG, "can't send: not connected");
            return false;
        }

        new AsyncTask<String, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(String... msg) {
                Log.i(TAG, "sending: " + msg[0]);
                out.println(msg[0]);
                return out.checkError();
            }

            @Override
            protected void onPostExecute(Boolean error) {
                if (!error) {
                    Toast.makeText(getApplicationContext(),
                            "Message sent to server", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error sending message to server",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg);

        return true;
    }

    /***************************************************************************/
    /***** UI related methods **************************************************/
    /***************************************************************************/

    /**
     * Hide the "connecting to server" text
     */
    void hideConnectingText() {
        findViewById(R.id.tvConnecting).setVisibility(View.GONE);
    }

    /**
     * Show the "connecting to server" text
     */
    void showConnectingText() {
        findViewById(R.id.tvConnecting).setVisibility(View.VISIBLE);
    }

    /**
     * Hide the login controls
     */
    void hideLoginControls() {
        findViewById(R.id.llLoginControls).setVisibility(View.GONE);
    }

    /**
     * Show the login controls
     */
    void showLoginControls() {
        findViewById(R.id.llLoginControls).setVisibility(View.VISIBLE);
    }

    /**
     * Hide the tictactoe board
     */
    void hideBoard() {
        findViewById(R.id.llBoard).setVisibility(View.GONE);
    }

    /**
     * Show the tictactoe board
     */
    void showBoard() {
        findViewById(R.id.llBoard).setVisibility(View.VISIBLE);
    }


    /**
     * Make the buttons of the tictactoe board clickable if they are not marked yet
     */
    void enableBoardClick() {
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                if ("".equals(board[x][y].getText().toString()))
                    board[x][y].setEnabled(true);
    }

    /**
     * Make the tictactoe board non-clickable
     */
    void disableBoardClick() {
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                board[x][y].setEnabled(false);
    }


}
