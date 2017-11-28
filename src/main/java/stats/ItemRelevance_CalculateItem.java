package stats;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ItemRelevance_CalculateItem {

    @Context
    public org.neo4j.graphdb.GraphDatabaseService db;

    @Procedure
    public HashMap<Item, Double> itemRelevanceCalculateItem(@Name("ID") Long itemID, ArrayList<Long> relatedItemIDs, Double commonItemTypeWeight, Double commonTagsWeight) {

        HashMap<Item, Double> result = new HashMap<>();

        // Get the target item
        Item targetItem = new Item(itemID);

        for (Long relatedItemID : relatedItemIDs) {
            Item candidateItem = new Item(relatedItemID);
            result.replace(candidateItem, targetItem.GetSimilarityScore(candidateItem, commonItemTypeWeight, commonTagsWeight));
        }

        return result;
    }

    public class Item {
        private Long ID;
        private Node node;
        private int itemType;
        private Set<String> tags;

        // Constructors
        public Item(Long ID) {
            this.ID = ID;
            this.node = db.findNode(Label.label("Item"), "ID", ID);
        }

        private void SetItemType() {
            Relationship hasAnRelationship = this.node.getRelationships(RelationshipType.withName("HAS_AN"), Direction.OUTGOING).iterator().next();
            Node targetItemType = hasAnRelationship.getOtherNode(this.node);

            this.itemType = Integer.parseInt(targetItemType.getProperty("ID").toString());
        }

        public int GetItemType() {
            if (itemType == 0)
                SetItemType();

            return itemType;
        }

        private void SetTags() {
            Iterable<Relationship> taggedRelationships = this.node.getRelationships(RelationshipType.withName("TAGGED"), Direction.OUTGOING);

            Stream<Relationship> relationshipStream = StreamSupport.stream(taggedRelationships.spliterator(), false);
            this.tags = relationshipStream.map(r -> r.getEndNode().getProperty("Label").toString()).collect(Collectors.toSet());
        }

        public Set<String> GetTags() {

            if (tags == null)
                SetTags();

            return tags;
        }

        public int GetTagCount() {
            return tags.size();
        }

        public Double GetSimilarityScore(Item relatedItem, Double commonItemTypeWeight, Double commonTagsWeight) {
            double score = 0.0;

            if (this.itemType == relatedItem.GetItemType())
                score += commonItemTypeWeight;

            Set commonTags = new HashSet<String>(this.tags);
            commonTags.retainAll(relatedItem.GetTags());
            score += (commonTags.size() * commonTagsWeight) / (this.GetTagCount() + relatedItem.GetTagCount());

            return score / (commonItemTypeWeight + commonTagsWeight);
        }
    }
}