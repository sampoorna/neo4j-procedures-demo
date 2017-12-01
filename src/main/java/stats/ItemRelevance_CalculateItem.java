package stats;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ItemRelevance_CalculateItem {

    @Context
    public org.neo4j.graphdb.GraphDatabaseService db;

    @Procedure
    public Stream<Output> itemRelevanceCalculateItem(@Name("ID") Long itemID, @Name("Related Items List") List<Long> relatedItemIDs, @Name("Item Type Weight") Double commonItemTypeWeight, @Name("Common Tags Weight") Double commonTagsWeight) {

        ArrayList<Output> result = new ArrayList<>();

        // Get the target item
        Item targetItem = new Item(itemID);

        for (Long relatedItemID : relatedItemIDs) {
            Item candidateItem = new Item(relatedItemID);
            result.add(new Output(candidateItem.GetNode(), targetItem.getSimilarityScore(candidateItem, commonItemTypeWeight, commonTagsWeight)));
        }

        return result.stream();
    }

    public class Output{
        public Node relatedItem;
        public Double score;

        // Constructors
        Output(Node item, Double score) {
            this.relatedItem = item;
            this.score = score;
        }
    }

    public class Item {
        private Long ID;
        private Node node;
        private int itemType;
        private Set<String> tags;

        // Constructors
        Item(Long ID) {
            this.ID = ID;
            this.node = db.findNode(Label.label("Item"), "ID", ID);
        }

        private void setItemType() {
            Relationship hasAnRelationship = this.node.getRelationships(RelationshipType.withName("HAS_AN"), Direction.OUTGOING).iterator().next();
            Node targetItemType = hasAnRelationship.getOtherNode(this.node);

            this.itemType = Integer.parseInt(targetItemType.getProperty("ID").toString());
        }

        public int getItemType() {
            if (itemType == 0)
                setItemType();

            return itemType;
        }

        public Node getNode(){
            return node;
        }

        private void setTags() {
            Iterable<Relationship> taggedRelationships = this.node.getRelationships(RelationshipType.withName("TAGGED"), Direction.OUTGOING);

            Stream<Relationship> relationshipStream = StreamSupport.stream(taggedRelationships.spliterator(), false);
            this.tags = relationshipStream.map(r -> r.getEndNode().getProperty("Label").toString()).collect(Collectors.toSet());
        }

        public Set<String> getTags() {

            if (tags == null)
                setTags();

            return tags;
        }

        public int getTagCount() {
            if (tags == null)
                setTags();

            return tags.size();
        }

        public Double getSimilarityScore(Item relatedItem, Double commonItemTypeWeight, Double commonTagsWeight) {
            double score = 0.0;

            if (itemType == 0)
                setItemType();

            if (tags == null)
                setTags();

            if (this.itemType == relatedItem.getItemType())
                score += 100 * commonItemTypeWeight;

            Set commonTags = new HashSet<String>(this.tags);
            commonTags.retainAll(relatedItem.getTags());
            score += (200 * commonTags.size() * commonTagsWeight) / (this.tags.size() + relatedItem.getTagCount());
            //System.out.println(score / (commonItemTypeWeight + commonTagsWeight));
            return score / (commonItemTypeWeight + commonTagsWeight);
        }
    }
}
