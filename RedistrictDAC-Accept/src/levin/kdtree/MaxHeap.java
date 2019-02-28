package levin.kdtree;
//https://bitbucket.org/rednaxela/knn-benchmark/src/de97871b1569fdc63b30ac60da210352ac935a04/ags/utils/dataStructures/?at=default
/**
 *
 */
public interface MaxHeap<T> {
    public int size();
    public void offer(double key, T value);
    public void replaceMax(double key, T value);
    public void removeMax();
    public T getMax();
    public double getMaxKey();
}