package org.neo4j.graphalgo.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.api.GraphFactory;
import org.neo4j.graphalgo.core.heavyweight.HeavyGraphFactory;
import org.neo4j.graphalgo.core.leightweight.LightGraphFactory;
import org.neo4j.graphdb.Direction;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public final class DirectionFilteringTest extends RandomGraphTestCase {

    private Class<? extends GraphFactory> graphImpl;

    @Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[]{HeavyGraphFactory.class, "HeavyGraphFactory"},
                new Object[]{LightGraphFactory.class, "LightGraphFactory"}
        );
    }

    @SuppressWarnings("unchecked")
    public DirectionFilteringTest(
            Class<?> graphImpl,
            String nameIgnoredOnlyForTestName) {
        this.graphImpl = (Class<? extends GraphFactory>) graphImpl;
    }

    @Test
    public void shouldLoadOnlyOutgoingRelationships() {
        testFilter(Direction.OUTGOING, Direction.INCOMING, Direction.BOTH);
    }

    @Test
    public void shouldLoadOnlyIncomingRelationships() {
        testFilter(Direction.INCOMING, Direction.OUTGOING, Direction.BOTH);
    }

    @Test
    public void shouldLoadBothRelationships() {
        testFilter(Direction.BOTH);
    }

    private void testFilter(
            Direction filter,
            Direction... expectedToFail) {
        EnumSet<Direction> failing = EnumSet.noneOf(Direction.class);
        failing.addAll(Arrays.asList(expectedToFail));
        EnumSet<Direction> succeeding = EnumSet.complementOf(failing);

        final Graph graph = new GraphLoader(RandomGraphTestCase.db)
                .withDirection(filter)
                .load(graphImpl);
        graph.forEachNode(node -> {
            for (Direction direction : succeeding) {
                graph.degree(node, direction);
                graph.forEachRelationship(node, direction, (s, t, r) -> true);
            }
            for (Direction direction : failing) {
                try {
                    graph.degree(node, direction);
                    fail("should have failed to load degree for " + direction);
                } catch (NullPointerException ignored) {
                }
                try {
                    graph.forEachRelationship(
                            node,
                            direction,
                            (s, t, r) -> true);
                    fail("should have failed to traverse nodes for " + direction);
                } catch (NullPointerException ignored) {
                }
            }
            return true;
        });
    }
}
