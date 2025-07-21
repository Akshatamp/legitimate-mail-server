/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbserver;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author Lenovo
 */
public class dbcon {
    
    Connection con;
    
    public dbcon()
    {
        
    }
    
    public Connection connect()
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            con=DriverManager.getConnection("jdbc:mysql://localhost:3306/mailserver", "root","root");
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
        
        return con;
    }
    
}
