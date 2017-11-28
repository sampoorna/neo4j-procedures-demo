package stats;

import java.util.*;
import java.util.stream.*;

import org.neo4j.driver.internal.util.Iterables;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;
import org.neo4j.logging.Log;

public class GetDegree {

    @Context public org.neo4j.graphdb.GraphDatabaseService db;

    public static class Degree {
        public final String label;
        public long count, max, min = Long.MAX_VALUE;

        Degree(String label) { this.label = label; }

        void add(int degree) {
            if (degree < min) min = degree;
            if (degree > max) max = degree;
            count ++;
        }
    }

    @Procedure
    // not needed here @PerformsWrites
    public Stream<Degree> degree(@Name("label") String label) {
        Degree degree = new Degree(label);
        try (ResourceIterator<Node> it = db.findNodes(Label.label(label))) {
            while (it.hasNext()) {
               degree.add(it.next().getDegree());
            }
        }
        return Stream.of(degree);
    }

    @Procedure
    public HashMap<Node, Float> getSimilarItems(Integer itemID, int limit) {
        // Get the target item
        Item targetItem = new Item(itemID);



    }

    public class NodeResult
    {
        //What i return
        public Node node;

        //Constructor
        public NodeResult(Relationship item)
        {
            this.node = item.getEndNode();
        }

    }

    public class Item
    {
        public int ID;
        public Node node;
        public int itemType;
        public ArrayList<String> tags;
        public int addToCartCount;
        public int purchaseCount;
        public int viewCount;

        //Constructor
        public Item(int ID)
        {
            this.ID = ID;
            this.node = db.findNode(Label.label("Item"), "ID", ID);

            Relationship hasAnRelationship = this.node.getRelationships(RelationshipType.withName("HAS_AN"), Direction.OUTGOING).iterator().next();
            Node targetItemType = hasAnRelationship.getOtherNode(this.node);

            this.itemType = Integer.parseInt(targetItemType.getProperty("ID").toString());

            Iterable<Relationship> taggedRelationships = this.node.getRelationships(RelationshipType.withName("TAGGED"), Direction.OUTGOING);

            Stream<Relationship> relsStream = StreamSupport.stream(taggedRelationships.spliterator(), false);
            this.tags = relsStream.map(r -> r.getEndNode().getProperty("Label").toString()).collect(Collectors.toCollection(ArrayList::new));
        }

        public int getItemType(){
            return itemType;
        }

        public ArrayList<String> getTags() {
            return tags;
        }

        public int getTagCount(){
            return tags.size();
        }

        public boolean hasTag(String tag){
            if (tags.contains(tag))
                return true;

            return false;
        }
    }
}