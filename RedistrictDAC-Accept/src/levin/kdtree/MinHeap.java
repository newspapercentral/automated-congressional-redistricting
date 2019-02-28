package levin.kdtree;
//https://bitbucket.org/rednaxela/knn-benchmark/src/de97871b1569fdc63b30ac60da210352ac935a04/ags/utils/dataStructures/?at=default
/**
 *
 */
public interface MinHeap<T> {
    public int size();
    public void offer(double key, T value);
    public void replaceMin(double key, T value);
    public void removeMin();
    public T getMin();
    public double getMinKey();
}