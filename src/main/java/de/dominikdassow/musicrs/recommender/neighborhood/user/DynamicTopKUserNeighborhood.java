package de.dominikdassow.musicrs.recommender.neighborhood.user;

import de.dominikdassow.musicrs.recommender.neighborhood.DynamicTopKNeighborhood;
import es.uam.eps.ir.ranksys.nn.user.neighborhood.UserNeighborhood;
import es.uam.eps.ir.ranksys.nn.user.sim.UserSimilarity;
import org.ranksys.core.util.tuples.Tuple2id;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class DynamicTopKUserNeighborhood<U>
    extends UserNeighborhood<U> {

    public DynamicTopKUserNeighborhood(
        UserSimilarity<U> sim,
        Predicate<Tuple2id> filter,
        Function<List<Tuple2id>, Boolean> isAcceptable
    ) {
        super(sim, new DynamicTopKNeighborhood(sim, filter, isAcceptable));
    }
}
