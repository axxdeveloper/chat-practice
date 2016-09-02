package chatpractice;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class ChatSample extends ReceiverAdapter {

    private List<String> state = new LinkedList<>();
    private JChannel channel;
    private String user_name=System.getProperty("user.name", "n/a");

    public void start() throws Exception {
        channel = new JChannel();
        channel.getState();
        channel.setReceiver(this);
        channel.connect("Test Cluster");
        channel.getState(null, 100000);
        eventLoop();
        channel.close();
    }

    @Override
    public void viewAccepted(View view) {
        System.out.println("** view: " + view);
    }

    @Override
    public void receive(Message msg) {
        String line = msg.getSrc() + ":" + msg.getObject();
        System.out.println(line);
        synchronized (state) {
            state.add(line);
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (state) {
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        List<String> list = (List<String>) Util.objectFromStream(new DataInputStream(input));
        synchronized (state) {
            state.clear();
            state.addAll(list);
        }
        System.out.println(list.size() + " messages in chat history");
        for (String m: list) {
            System.out.println(m);
        }
    }

    private void eventLoop() {
        BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                System.out.print("> "); System.out.flush();
                String line=in.readLine().toLowerCase();
                if(line.startsWith("quit") || line.startsWith("exit"))
                    break;
                line="[" + user_name + "] " + line;
                Message msg=new Message(null, null, line);
                channel.send(msg);
            }
            catch(Exception e) {
            }
        }
    }

    public static void main(String[] params) throws Exception {
        new ChatSample().start();
    }

}
