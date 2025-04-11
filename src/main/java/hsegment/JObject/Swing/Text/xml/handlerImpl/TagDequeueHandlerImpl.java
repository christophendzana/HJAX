package hsegment.JObject.Swing.Text.xml.handlerImpl;

import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import hsegment.JObject.Swing.Text.xml.Constants;
import hsegment.JObject.Swing.Text.xml.TagElement;
import hsegment.JObject.Swing.Text.xml.handler.DequeueHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * An implementation of DequeueHandler interface.
 * This implementation use a <code>ConcurrentLinkedDeque</code>, a queue data structured with multithreading process
 * use to check the correct tag imbrication. A LIFO queue is used to add only open tags and retrieves them while
 * them when close tags with the same name is found.
 */
public class TagDequeueHandlerImpl implements DequeueHandler<TagElement>  {
    //Queue initialisation
    private final ConcurrentLinkedDeque<TagElement> tagsDeque = new ConcurrentLinkedDeque<>();
    // Empty state initialisation
    private int emptyState = 0;

    /**
     * Use to add a TagElement to the queue
     * @param tagElement
     * @return <code>true</code> if element is added, <code>false</code> else.
     * @throws HJAXException
     */
    @Override
    public boolean add(TagElement tagElement) throws HJAXException {
        if(tagElement == null){
            throw new HJAXException("Tag element is null");
        }
        return tagsDeque.add(tagElement);
    }

    /**
     * Use to remove the TagElement in the tail of queue.
     * @return <code>TagElement</code> retrieved
     */
    @Override
    public TagElement remove() {
        if(tagsDeque.isEmpty()){
            throw new HJAXException("Empty stack !");
        }
        return tagsDeque.removeLast();
    }

    /**
     * A process use to add open tags and retrieved them to the queue while corresponding tags with
     * the same name is found.
     * @param tagElement
     */
    @Override
    public void autoProcess(TagElement tagElement) {
        TagElement removedTagElement;
        switch (tagElement.getType()){
            // add open tag
            case Constants.OPEN_TAG -> add(tagElement);
            // retrieve open tag with the same name with close tag
            case Constants.CLOSE_TAG -> {
                removedTagElement = remove();
                // check the emptiness of queue to determine if the file have only one root tag
                if(tagsDeque.isEmpty()){
                    emptyState++;
                }
                // if the open and close tag don't have the same name,
                // then the xml file has an incorrect open or close tag.
                String tagName = removedTagElement.getElement().getName();
                if(!tagName.equals(tagElement.getElement().getName())){
                   throw new HJAXException("Not found close tag of tag '"+tagName+"' or tag with name " +
                           "'"+tagElement.getElement().getName()+"' do not have open tag");
                }
            }
            default -> {}
        }
    }

    /**
     * Get the number TagElement in queue
     * @return <code>The size of queue</code>
     */
    @Override
    public int count() {
        return tagsDeque.size();
    }

    /**
     * Get the elements of queue as a List.
     * @return <code>List</code> of tagElement
     */
    @Override
    public List<TagElement> getElements() {
        List<TagElement> orphanTags = new ArrayList<>(tagsDeque);
        tagsDeque.clear();
        return orphanTags;
    }

    /**
     * Get the number a queue has been empty.
     * @return <code>The number</code> of emptiness
     */
    @Override
    public int getEmptyState() {
        return emptyState;
    }
}
