package chengye;

import com.koloboke.collect.set.hash.HashObjSet;
import com.koloboke.collect.set.hash.HashObjSets;
import org.openjdk.jmh.annotations.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.Arrays.stream;
import static org.openjdk.jol.info.GraphLayout.parseInstance;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@OperationsPerInvocation(TestHashSet.TIMES)
@Threads(1)
@Fork(1)
@State(Scope.Thread)
public class TestHashSet {
    public static final int TIMES = 1000000;
    private static final int MAX = 5000000;
    private static long ELEMENTS_SIZE;

    static Long[] add = new Long[TIMES], lookup = new Long[TIMES], remove = new Long[TIMES];
    static {
        for (int ix = 0; ix < TIMES; ix++)
            add[ix] = new Long(Math.round(Math.random() * MAX));
        ELEMENTS_SIZE = stream(add).distinct().count() * parseInstance(add[0]).totalSize();
        for (int ix = 0; ix < TIMES; ix++)
            lookup[ix] = new Long(Math.round(Math.random() * MAX));
        for (int ix = 0; ix < TIMES; ix++)
            remove[ix] = new Long(Math.round(Math.random() * MAX));
    }

    @Benchmark
    public int hashSet() {
        Set<Long> set = new HashSet<Long>();
        for (Long o : add) {
            set.add(o);
        }
        int r = 0;
        for (Long o : lookup) {
            r ^= set.contains(o) ? 1 : 0;
        }
        for (Long o : set) {
            r += o.intValue();
        }
        for (Long o : remove) {
            set.remove(o);
        }
        return r + set.size();
    }

    @Benchmark
    public int kolobokeSet() {
        Set<Long> set = HashObjSets.newMutableSet();
        for (Long o : add) {
            set.add(o);
        }
        int r = 0;
        for (Long o : lookup) {
            r ^= set.contains(o) ? 1 : 0;
        }
        for (Long o : set) {
            r += o.intValue();
        }
        for (Long o : remove) {
            set.remove(o);
        }
        return r + set.size();
    }



    public static void main(String[] argv) {
        HashSet hashSet = new HashSet();
        test("HashSet", hashSet, hashSet::add);
        HashObjSet<Object> kolobokeSet = HashObjSets.newMutableSet();
        test("KolobokeSet", kolobokeSet, kolobokeSet::add);
    }

    public static void test(String name, Object set, Consumer setAdd) {
        for (Long o : add) {
            setAdd.accept(o);
        }
        System.out.printf("%s: %.1f bytes per element\n", name,
                ((parseInstance(set).totalSize() - ELEMENTS_SIZE) * 1.0 / TIMES));

    }
}