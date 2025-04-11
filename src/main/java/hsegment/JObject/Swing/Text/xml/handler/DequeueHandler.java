package hsegment.JObject.Swing.Text.xml.handler;

import hsegment.JObject.Swing.Text.ParserException.HJAXException;

import java.util.List;

/**
 An interface for handling queue process with generic object.
 The <code>add(E e)</code> method add object in the queue. The <code>remove()</code> method retrieves object
 in the queue. The <code>autoProcess(E e)</code> implement automatically add and remove actions.
 The <code>count()</code> count the numbers object in the queue. The <code>getElements()</code> retrieve
 all object on the queue and store them on the List. The <code>getEmptyState()</code> check the number
 of occurrence while the queue become empty. This is usually to determine if the xml file contain only single root tag.
 @author Hyacinthe Tsague
 */
public interface DequeueHandler<E> {
    boolean add(E e) throws HJAXException;
    E remove() throws HJAXException;
    void autoProcess(E e) throws HJAXException;
    int count();
    List<E> getElements();
    int getEmptyState();
}
