/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 *
 * @author FIDELE
 */
public enum HButtonStyle {    

    PRIMARY(new Color(13, 110, 253),   
            new Color(4, 57, 128),   
            new Color(10, 88, 202),    
            Color.WHITE),              
    
    SECONDARY(new Color(108, 117, 125), 
              new Color(92, 99, 106),   
              new Color(86, 94, 100),   
              Color.WHITE),    

    SUCCESS(new Color(25, 135, 84),    
            new Color(15, 90, 55),   
            new Color(20, 108, 67),    
            Color.WHITE),    
 
    DANGER(new Color(220, 53, 69),     
           new Color(148, 30, 41),     
           new Color(176, 42, 55),    
           Color.WHITE),    

    WARNING(new Color(193, 142, 0),    
            new Color(255, 205, 57),   
            new Color(255, 213, 85),   
            Color.BLACK),             
      
    INFO(new Color(13, 202, 240),      
         new Color(7, 114, 135),      
         new Color(10, 162, 192),      
         Color.BLACK),               
        
    LIGHT(new Color(248, 249, 250),    
          new Color(233, 236, 239),    
          new Color(221, 224, 227),    
          Color.BLACK),
     
    DARK(new Color(33, 37, 41),        
         new Color(41, 46, 51),        
         new Color(52, 58, 64),       
         Color.WHITE),
       
    LINK(Color.WHITE,                  
         new Color(248, 249, 250),     
         new Color(233, 236, 239),     
         new Color(13, 110, 253)),
    
    FIELD(new Color(237, 244, 255),                  
         new Color(150, 155, 222),     
         new Color(14, 50, 138),     
         new Color(13, 110, 253)),
    
    ;     
    
    private final Color baseColor;
    private final Color hoverColor;
    private final Color pressColor;
    private final Color textColor;
    
    private HButtonStyle(Color base, Color hover, Color press, Color text) {
        this.baseColor = base;
        this.hoverColor = hover;
        this.pressColor = press;
        this.textColor = text;
    }
    
    public Color getBaseColor() { return baseColor; }
    public Color getHoverColor() { return hoverColor; }
    public Color getPressColor() { return pressColor; }
    public Color getTextColor() {return textColor;}
    
  
    
}