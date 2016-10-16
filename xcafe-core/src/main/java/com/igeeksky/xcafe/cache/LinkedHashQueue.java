package com.igeeksky.xcafe.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class LinkedHashQueue<K, V> {
	
	private HashMap<K,V> map = new HashMap<K,V>();
	
	private Node<K> head;
	
	private Node<K> tail;

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(Object o) {
		return map.containsKey(o);
	}
	
	public boolean containsValue(Object o) {
		return map.containsKey(o);
	}

	public Iterator<K> iterator() {
		return null;
	}

	public K[] keysArray() {
		return null;
	}
	
	public V[] valuesArray() {
		return null;
	}

/*	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}*/

	public boolean remove(Object o) {
		return false;
	}

	public boolean containsAll(Collection<?> c) {
		return false;
	}

	public boolean addAll(Collection<? extends V> c) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	public void clear() {
		// TODO Auto-generated method stub
		
	}

	public boolean add(K k, V v) {
		if(map.containsKey(k)){
			return false;
		}
		
		Node<K> cur;
		
		if(head == null){
			head = cur = new Node<K>(null, k, tail);
			tail = new Node<K>(head, null, null);
		}else{
			Node<K> prev = tail.prev;
			cur = new Node<K>(prev, k, tail);
			prev.next = cur;
			tail.prev = cur;
		}
		
		if(null != cur && null != map.put(k, v)) return true;
		return false;
	}

	public boolean offer(V v) {
		// TODO Auto-generated method stub
		return false;
	}

	public V remove() {
		// TODO Auto-generated method stub
		return null;
	}

	public V poll() {
		// TODO Auto-generated method stub
		return null;
	}

	public V element() {
		// TODO Auto-generated method stub
		return null;
	}

	public V peek() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static class Node<K> {
        K item;
        Node<K> next;
        Node<K> prev;

        Node(Node<K> prev, K element, Node<K> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }


}
