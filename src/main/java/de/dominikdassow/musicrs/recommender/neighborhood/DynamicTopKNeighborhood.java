package de.dominikdassow.musicrs.recommender.neighborhood;

import es.uam.eps.ir.ranksys.nn.neighborhood.Neighborhood;
import es.uam.eps.ir.ranksys.nn.neighborhood.TopKNeighborhood;
import es.uam.eps.ir.ranksys.nn.sim.Similarity;
import lombok.extern.slf4j.Slf4j;
import org.ranksys.core.util.tuples.Tuple2id;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
public class DynamicTopKNeighborhood
    implements Neighborhood {

    private final Similarity sim;
    private final Predicate<Tuple2id> filter;
    private final Function<List<Tuple2id>, Boolean> isAcceptable;

    public DynamicTopKNeighborhood(
        Similarity sim,
        Predicate<Tuple2id> filter,
        Function<List<Tuple2id>, Boolean> isAcceptable
    ) {
        this.sim = sim;
        this.isAcceptable = isAcceptable;
        this.filter = filter;
    }

    @Override
    public Stream<Tuple2id> getNeighbors(int idx) {
        List<Tuple2id> neighbors = new ArrayList<>(0);

        int k = 1; // TODO
        boolean accepted = false;

        while (!accepted) {
            neighbors.clear();

            new TopKNeighborhood(sim, k).getNeighbors(idx)
                .filter(filter)
                .forEach(neighbors::add);

            k += 1;
            accepted = this.isAcceptable.apply(neighbors);
        }

        return neighbors.stream();
    }

}
