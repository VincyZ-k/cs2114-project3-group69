package Laundry;
import java.util.*;

public class LinkedQueue<T>
{
    
    public static class Node<D>
    {
        private D data;
        private Node<D> next;

        public Node(D d) {
            data = d;
        }

    }
    
    //~ Fields ................................................................
    private Node<T> head;
    private Node<T> tail;
    private int size;
    
    //~ Constructors ..........................................................
    public LinkedQueue()
    {
        head = null;
        tail = null;
        size = 0;
    }
    
    //~Public  Methods ........................................................
    
    
    
    public void enqueue(T item)
    {
        if (item == null)
        {
            throw new IllegalArgumentException();
        }
        
        Node<T> newNode = head;
        if (isEmpty()) 
        {
            head = new Node<T>(item);
            tail = head;
        }
        else
        {
            tail.next = newNode;
            tail = newNode;
        }
        size++;

    }
    
    public T dequeue()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException();
        }
        
        T removed = head.data;
        head = head.next;
        size--;
        
        if (head == null)
        {
            tail = null;
        }

        return removed;
        
    }
    
    public T peek()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException();
        }
        return head.data;
    }
    
    public boolean isEmpty()
    {
        return size == 0;
    }
    
    public int size()
    {
        return size;
    }
    
    public List<T> toList()
    {
        List<T> list = new ArrayList<>();
        Node<T> current = head;
        
        while(current != null)
        {
            list.add(current.data);
            current = current.next;
        }
        
        return list;
    }
    
    
}
