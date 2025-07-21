/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailattacker;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Lenovo
 */
public class receive extends Thread
{
    attacker obj;
    
    receive(attacker a)
    {
        super();
        this.obj=a;
        start();
    }
    
    public void run()
    {
        try
        {
           ServerSocket ss=new ServerSocket(5000); 
           
           while (true)
           {
               Socket soc=ss.accept();
                
                ObjectOutputStream oos=new ObjectOutputStream(soc.getOutputStream());
                ObjectInputStream oin=new ObjectInputStream(soc.getInputStream());
                
                
                String req=(String)oin.readObject();
                
                if (req.equals("MAIL"))
                {
                    String sender=(String)oin.readObject();
                    String receiver=(String)oin.readObject();
                    String sub=(String)oin.readObject();
                    String msg=(String)oin.readObject();
                    String sign=(String)oin.readObject();
                    
                    obj.jTextField1.setText(sender);
                    obj.jTextField2.setText(receiver);
                    obj.jTextField3.setText(sub);
                    obj.jTextArea1.setText(msg);
                    obj.sign=sign;
                    
                    oos.writeObject("SUCCESS");
                }
               
           }
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
