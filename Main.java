package Lesson1.example;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        int[] numbers = new int[10000];
        Random rand = new Random();

        for (int i = 0; i < 10000; i++) {
            numbers[i] = rand.nextInt();
        }

        TwoThreeTree tree = new TwoThreeTree(3);
        long startTime, endTime;
        ArrayList<Long> insertTimes = new ArrayList<>();
        ArrayList<Long> searchTimes = new ArrayList<>();
        ArrayList<Long> deleteTimes = new ArrayList<>();

        // Добавление элементов
        for (int number : numbers) {
            startTime = System.nanoTime();
            tree.insert(number);
            endTime = System.nanoTime();
            insertTimes.add(endTime - startTime);
        }

        // Поиск 100 элементов
        Collections.shuffle(Arrays.asList(numbers));
        for (int i = 0; i < 100; i++) {
            startTime = System.nanoTime();
            tree.search(numbers[i]);
            endTime = System.nanoTime();
            searchTimes.add(endTime - startTime);
        }

        // Удаление 1000 элементов
        for (int i = 0; i < 1000; i++) {
            startTime = System.nanoTime();
            tree.delete(numbers[i]);
            endTime = System.nanoTime();
            deleteTimes.add(endTime - startTime);
        }

        // Среднее время вставки, удаления и поиска
        System.out.println("Среднее время вставки: " + average(insertTimes));
        System.out.println("Среднее время поиска: " + average(searchTimes));
        System.out.println("Среднее время удаления: " + average(deleteTimes));
    }

    public static double average(ArrayList<Long> list) {
        long sum = 0;
        for (long element : list) {
            sum += element;
        }
        return (double) sum / list.size();
    }
}

class Node {
    int[] keys;
    Node[] children;
    int n;
    boolean leaf;

    public Node(int t, boolean leaf) {
        this.leaf = leaf;
        keys = new int[2 * t - 1];
        children = new Node[2 * t];
        n = 0;
    }
}

class TwoThreeTree {
    private Node root;
    private int t;

    public TwoThreeTree(int t) {
        this.t = t;
        root = new Node(t, true);
    }

    // ... (прежние методы search, insert и вспомогательные методы)
    public Node search(int k) {
        return search(root, k);
    }

    private Node search(Node x, int k) {
        int i = 0;
        while (i < x.n && k > x.keys[i]) {
            i++;
        }

        if (i < x.n && k == x.keys[i]) {
            return x;
        } else if (x.leaf) {
            return null;
        } else {
            return search(x.children[i], k);
        }
    }

    public void insert(int k) {
        Node r = root;
        if (r.n == 2 * t - 1) {
            Node s = new Node(t, false);
            root = s;
            s.children[0] = r;
            splitChild(s, 0);
            insertNonFull(s, k);
        } else {
            insertNonFull(r, k);
        }
    }

    private void splitChild(Node x, int i) {
        Node z = new Node(t, x.children[i].leaf);
        z.n = t - 1;
        for (int j = 0; j < t - 1; j++) {
            z.keys[j] = x.children[i].keys[j + t];
        }

        if (!x.children[i].leaf) {
            for (int j = 0; j < t; j++) {
                z.children[j] = x.children[i].children[j + t];
            }
        }

        x.children[i].n = t - 1;

        for (int j = x.n; j >= i + 1; j--) {
            x.children[j + 1] = x.children[j];
        }

        x.children[i + 1] = z;

        for (int j = x.n - 1; j >= i; j--) {
            x.keys[j + 1] = x.keys[j];
        }

        x.keys[i] = x.children[i].keys[t - 1];
        x.n++;
    }

    private void insertNonFull(Node x, int k) {
        int i = x.n - 1;
        if (x.leaf) {
            while (i >= 0 && k < x.keys[i]) {
                x.keys[i + 1] = x.keys[i];
                i--;
            }

            x.keys[i + 1] = k;
            x.n++;
        } else {
            while (i >= 0 && k < x.keys[i]) {
                i--;
            }

            i++;

            if (x.children[i].n == 2 * t - 1) {
                splitChild(x, i);

                if (k > x.keys[i]) {
                    i++;
                }
            }

            insertNonFull(x.children[i], k);
        }
    }

    public void delete(int k) {
        delete(root, k);

        if (root.n == 0) {
            if (root.leaf) {
                root = null;
            } else {
                root = root.children[0];
            }
        }
    }

    private void delete(Node x, int k) {
        int idx = 0;
        while (idx < x.n && x.keys[idx] < k) {
            idx++;
        }

        if (idx < x.n && x.keys[idx] == k) {
            if (x.leaf) {
                removeFromLeaf(x, idx);
            } else {
                removeFromNonLeaf(x, idx);
            }
        } else {
            if (x.leaf) {
                System.out.println("The key " + k + " doesn't exist in the tree.");
                return;
            }

            boolean flag = idx == x.n;

            if (x.children[idx].n < t) {
                fill(x, idx);
            }

            if (flag && idx > x.n) {
                delete(x.children[idx - 1], k);
            } else {
                delete(x.children[idx], k);
            }
        }
    }

    private void removeFromLeaf(Node x, int idx) {
        for (int i = idx + 1; i < x.n; i++) {
            x.keys[i - 1] = x.keys[i];
        }
        x.n--;
    }

    private void removeFromNonLeaf(Node x, int idx) {
        int k = x.keys[idx];

        if (x.children[idx].n >= t) {
            int pred = getPredecessor(x, idx);
            x.keys[idx] = pred;
            delete(x.children[idx], pred);
        } else if (x.children[idx + 1].n >= t) {
            int succ = getSuccessor(x, idx);
            x.keys[idx] = succ;
            delete(x.children[idx + 1], succ);
        } else {
            merge(x, idx);
            delete(x.children[idx], k);
        }
    }

    private int getPredecessor(Node x, int idx) {
        Node cur = x.children[idx];
        while (!cur.leaf) {
            cur = cur.children[cur.n];
        }
        return cur.keys[cur.n - 1];
    }

    private int getSuccessor(Node x, int idx) {
        Node cur = x.children[idx + 1];
        while (!cur.leaf) {
            cur = cur.children[0];
        }
        return cur.keys[0];
    }

    private void fill(Node x, int idx) {
        if (idx != 0 && x.children[idx - 1].n >= t) {
            borrowFromPrev(x, idx);
        } else if (idx != x.n && x.children[idx + 1].n >= t) {
            borrowFromNext(x, idx);
        } else {
            if (idx != x.n) {
                merge(x, idx);
            } else {
                merge(x, idx - 1);
            }
        }
    }

    private void borrowFromPrev(Node x, int idx) {
        Node child = x.children[idx];
        Node sibling = x.children[idx - 1];

        for (int i = child.n - 1; i >= 0; i--) {
            child.keys[i + 1] = child.keys[i];
        }

        if (!child.leaf) {
            for (int i = child.n; i >= 0; i--) {
                child.children[i + 1] = child.children[i];
            }
        }

        child.keys[0] = x.keys[idx - 1];

        if (!x.leaf) {
            child.children[0] = sibling.children[sibling.n];
        }

        x.keys[idx - 1] = sibling.keys[sibling.n - 1];

        child.n += 1;
        sibling.n -= 1;
    }

    private void borrowFromNext(Node x, int idx) {
        Node child = x.children[idx];
        Node sibling = x.children[idx + 1];

        child.keys[child.n] = x.keys[idx];

        if (!child.leaf) {
            child.children[child.n + 1] = sibling.children[0];
        }

        x.keys[idx] = sibling.keys[0];

        for (int i = 1; i < sibling.n; i++) {
            sibling.keys[i - 1] = sibling.keys[i];
        }

        if (!sibling.leaf) {
            for (int i = 1; i <= sibling.n; i++) {
                sibling.children[i - 1] = sibling.children[i];
            }
        }

        child.n += 1;
        sibling.n -= 1;
    }

    private void merge(Node x, int idx) {
        Node child = x.children[idx];
        Node sibling = x.children[idx + 1];

        child.keys[t - 1] = x.keys[idx];

        for (int i = 0; i < sibling.n; i++) {
            child.keys[i + t] = sibling.keys[i];
        }

        if (!child.leaf) {
            for (int i = 0; i <= sibling.n; i++) {
                child.children[i + t] = sibling.children[i];
            }
        }

        for (int i = idx + 1; i < x.n; i++) {
            x.keys[i - 1] = x.keys[i];
        }

        for (int i = idx + 2; i <= x.n; i++) {
            x.children[i - 1] = x.children[i];
        }

        child.n += sibling.n + 1;
        x.n--;
    }
}