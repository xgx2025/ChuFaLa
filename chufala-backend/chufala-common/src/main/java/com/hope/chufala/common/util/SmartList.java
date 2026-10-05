package com.hope.chufala.common.util;
import java.util.AbstractList;
import java.util.List;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 简易动态数组实现。
 *
 * <p>以 Object[] 承载元素，容量不足时按 2 倍扩容，仅实现 List 的最小子集
 * （add / get / size / iterator）。当前用于性能对比基准（perf.ListBenchmark），
 * 业务代码请优先使用 ArrayList。
 *
 * @author 谢光湘
 */
public class SmartList<T> extends AbstractList<T> implements List<T> {
    private Object[] elements;
    private int size;
    private static final int DEFAULT_CAPACITY = 10;

    public SmartList() {
        this.elements = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    public SmartList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        this.elements = new Object[initialCapacity];
        this.size = 0;
    }

    @Override
    public boolean add(T element) {
        ensureCapacity(size + 1);
        elements[size++] = element;
        return true;
    }

    @Override
    public T get(int index) {
        rangeCheck(index);
        return (T) elements[index];
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * 保证底层数组至少能容纳 minCapacity 个元素，不足则按 2 倍扩容。
     *
     * @param minCapacity 需要的最小容量
     */
    private void ensureCapacity(int minCapacity) {
        if (minCapacity > elements.length) {
            int newCapacity = Math.max(elements.length * 2, minCapacity);
            Object[] newElements = new Object[newCapacity];
            System.arraycopy(elements, 0, newElements, 0, size);
            elements = newElements;
        }
    }

    /**
     * 下标越界检查。
     *
     * @param index 待访问下标
     */
    private void rangeCheck(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return (T) elements[currentIndex++];
            }
        };
    }
}
