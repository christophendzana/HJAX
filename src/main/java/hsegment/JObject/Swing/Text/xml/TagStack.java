/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml;

import hsegment.JObject.util.Dictionnary;



/**
 *A stack of tag.Used while parsing XML document.
 * When the start tag is encountered element is pushed onto 
 * the stack, when an end tag is encountered an element is popped 
 * off the stack.
 * @author Ndzana Christophe
 */
public class TagStack {
    private Dictionnary<Element> stack;
    public TagStack(){
        stack = new Dictionnary<Element>();
        stack.setDuplicateStateTo(true);
    }
    
    public void stack(TagElement tag){
        stack.add(tag.getElement());
    }
    /**
     * Pull tag out of stack if this tag is the latest added one 
     * or if this tag was never been added it considered as a Empty 
     * tag by a stack. If this tag is contained into this tag and is not 
     * the latest added tag it cannot be pull out and false is return.
     * 
     * 
     * @param tag tag to pull out
     * @return true if <code>tag</code> can be pull out or false otherwise.
     */
    public boolean pullOut(TagElement tag){
        int index = stack.indexOf(tag.getElement());
        if(index == -1){
            Element element = tag.getElement();
            element.setType(HDTDConstants.EMPTY);
        } else if(index != stack.size() - 1){
            return false;
        }
        
        if(index >= 0)
            stack.remove(index);
       return true;     
    }
    
    /**
     * Return the number of tag into this stack.
     * @return the number of tag into this stack
     */
    public int count(){
        return stack.size();
    }
}
